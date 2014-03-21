package saadadb.products.inference;

import java.util.Map;

import saadadb.database.Database;
import saadadb.dataloader.mapping.PriorityMode;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ProductFile;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

public class EnergyKWDetector extends KWDetector {
	private SpectralCoordinate spectralCoordinate;
	private ProductFile productFile;
	private PriorityMode priority;
	private String defaultUnit;
	private String readUnit;
	/**
	 * @param productFile
	 * @throws SaadaException
	 */
	public EnergyKWDetector(ProductFile productFile, PriorityMode priority, String defaultUnit) throws SaadaException {
		super(productFile);
		this.productFile = productFile;
		this.setUnitMode(priority, defaultUnit);
	}
	/**
	 * @param tableAttributeHandler
	 */
	public EnergyKWDetector(
			Map<String, AttributeHandler> tableAttributeHandler, PriorityMode priority, String defaultUnit) {
		super(tableAttributeHandler);
		this.setUnitMode(priority, defaultUnit);
	}
	/**
	 * @param priority
	 * @param defaultUnit
	 */
	private void setUnitMode(PriorityMode priority, String defaultUnit){
		this.priority = priority;
		this.defaultUnit = defaultUnit;
		switch (this.priority) {
		case ONLY:
			this.readUnit = this.defaultUnit;
			break;
		case FIRST: 
			if( this.defaultUnit != null && this.defaultUnit.length() != 0 ) {
				this.readUnit = this.defaultUnit;
				this.priority = PriorityMode.ONLY;
			} else {
				this.readUnit = null;
			}
			break;
		default:
			this.readUnit = null;
			break;
		}
	}
	/**
	 * @return
	 * @throws Exception
	 */
	private boolean mapCollectionSpectralCoordinateAuto() throws Exception {	
		spectralCoordinate = new SpectralCoordinate(1, 1
				,SpectralCoordinate.getDispersionCode(Database.getSpect_type())
				, Database.getSpect_unit());
		boolean retour = ( this.findSpectralCoordinateByUCD() ||  this.findSpectralCoordinateByKW() || this.findSpectralCoordinateByWCS() ||
				              this.findSpectralCoordinateInPixels());
		if( this.priority == PriorityMode.LAST ) {
			if( this.readUnit == null || this.readUnit.length() == 0) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the mapped unit <" + this.defaultUnit + ">");
				spectralCoordinate.setOrgUnit(this.defaultUnit);
			} else {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the detected unit <" + this.readUnit + ">");
				spectralCoordinate.setOrgUnit(this.readUnit);
			}
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, spectralCoordinate.getOrgMin() + " " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getOrgUnit());
		return retour;
	}

	/**
	 * @param ds
	 * @return
	 */
	private void setMinMaxValues(double[] ds) {
		if( ds == null || ds.length != 2 ) {
			this.spectralCoordinate.setOrgMax(SaadaConstant.DOUBLE);
			this.spectralCoordinate.setOrgMin(SaadaConstant.DOUBLE);
		} else {
			this.spectralCoordinate.setOrgMax(ds[1]);
			this.spectralCoordinate.setOrgMin(ds[0]);		
		}
	}


	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateInPixels() throws Exception {
		if(  this.productFile == null ) return false;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates in pixel");
		double[] ext = this.productFile.getExtrema(null);
		if( ext != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Spectral coordinates found in FITS image pixels");
			this.setMinMaxValues(ext);	
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateByWCS() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates in WCS keywords");
		return  spectralCoordinate.convertWCS(this.tableAttributeHandler);
	}

	/**
	 * @return
	 * @throws IgnoreException 
	 */
	private boolean findSpectralCoordinateByKW() throws Exception{

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in KWs ");
		/*
		 * If no range set in params, try to find it out from fields
		 */	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No Spectral coordinate found in header: explore columns names");
		if( this.entryAttributeHandler != null ){
			AttributeHandler ah = this.searchColumnsByName(RegExp.SPEC_AXIS_KW);
			if( ah != null ){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getNameorg()));
				this.readUnit = ah.getUnit();
				return true;
			}
		}
		return  false;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateByUCD() throws Exception{
		boolean findMin = false;
		boolean findMax = false;

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates by UCDs or UTypes ");
		AttributeHandler ah = this.searchByUcd(RegExp.SPEC_MIN_UCD);
		if( ah !=null){
			spectralCoordinate.setOrgMin(Double.parseDouble(ah.getValue()));
			findMin = true;
			this.readUnit = ah.getUnit();
		}
		ah = this.searchByUcd(RegExp.SPEC_MAX_UCD);
		if( ah !=null){
			spectralCoordinate.setOrgMax(Double.parseDouble(ah.getValue()));
			findMax = true;
			if( this.readUnit == null || this.readUnit.length() == 0) this.readUnit = ah.getUnit();
		}

		if ( this.entryAttributeHandler != null && (!findMax  || !findMin) ){
			/*
			 * If no range set in params, try to find it out from fields
			 */	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No Spectral coordinates found in header UCDs or UTypes: explore column definitions");
			findMin = false;
			ah = this.searchColumnsByUcd(RegExp.SPEC_BAND_UCD);
			if( ah !=null){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getNameorg()));
				if( this.readUnit == null || this.readUnit.length() == 0)  this.readUnit = ah.getUnit();
				findMin = true;
			}
			return findMin;
		}
		return (findMax & findMax);
	}

	/**
	 * The detector can find anything but the unit which is usually not within the keywords.
	 * If it is not found,, a default unit is given.
	 * ONLY: It is taken in any case 
	 * FIRST: It is taken if valid
	 * LAST: It is taken if not found in meta data
	 * @param priority
	 * @param defaultUnit
	 * @return
	 * @throws Exception
	 */
	public SpectralCoordinate getSpectralCoordinate() throws Exception{
		if( spectralCoordinate == null ){
			this.mapCollectionSpectralCoordinateAuto();
		}
		return spectralCoordinate;
	}
}
