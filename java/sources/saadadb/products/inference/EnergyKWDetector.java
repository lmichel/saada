package saadadb.products.inference;

import java.util.Map;

import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.enums.PriorityMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
import saadadb.products.DataFile;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

/**
 * The detection of the energy range is tricky. Even in self-detection mode it can use mapping parameters such as unit. 
 * That is why the ProductMapping is  transmitted to that tool
 * @author michel
 * @version $Id$
 */
public class EnergyKWDetector extends KWDetector {
	private SpectralCoordinate spectralCoordinate;
	private DataFile productFile;
	private PriorityMode priority;
	private String defaultUnit;
	private String readUnit;
	public String detectionMessage ="";
	/**
	 * @param productFile
	 * @param productMapping
	 * @throws SaadaException
	 */
	public EnergyKWDetector(DataFile productFile, ProductMapping productMapping) throws SaadaException {
		super(productFile);
		this.productFile = productFile;
		if(productMapping != null ) {
		this.setUnitMode(productMapping);
		}
	}
	/**
	 * @param tableAttributeHandler
	 * @param productMapping
	 * @throws SaadaException
	 */
	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler, ProductMapping productMapping)throws SaadaException {
		super(tableAttributeHandler);
		this.setUnitMode(productMapping);
	}
	/**
	 * @param priority
	 * @param defaultUnit to be used if nothing else
	 * @throws FatalException 
	 */
	private void setUnitMode(ProductMapping productMapping) throws FatalException{
		if( productMapping == null ) return ;
		this.priority = productMapping.getEnergyAxisMapping().getPriority();
		this.defaultUnit = productMapping.getEnergyAxisMapping().getColumnMapping("x_unit_org_csa").getValue();
		switch (this.priority) {
		case ONLY:
			// Consider the default unit as this read
			this.readUnit = this.defaultUnit;
			break;
		case FIRST: 
			// Consider the default unit as this read if not null
			if( this.defaultUnit != null && this.defaultUnit.length() != 0 ) {
				this.readUnit = this.defaultUnit;
				this.priority = PriorityMode.ONLY;
			} else {
				this.readUnit = null;
			}
			break;
		default:
			// Consider that no unit has been read right now
			this.readUnit = null;
			break;
		}
		if( this.defaultUnit == null || this.defaultUnit.length() == 0 ) {
			this.defaultUnit = "channels";
		}
	}
	/**
	 * @return
	 * @throws Exception
	 */
	private boolean mapCollectionSpectralCoordinateAuto() throws Exception {	
		spectralCoordinate = new SpectralCoordinate(Database.getSpect_unit());
		boolean retour = ( this.findSpectralCoordinateByUCD() ||  this.findSpectralCoordinateByKW() || this.findSpectralCoordinateByWCS() ||
				              this.findSpectralCoordinateInPixels());
		if( this.priority == PriorityMode.LAST ) {
			if( this.readUnit == null || this.readUnit.length() == 0) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the mapped unit <" + this.defaultUnit + ">");
				spectralCoordinate.setMappedUnit(this.defaultUnit);
			} else {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the detected unit <" + this.readUnit + ">");
				spectralCoordinate.setMappedUnit(this.readUnit);
			}
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, spectralCoordinate.getOrgMin() + " " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getMappedUnit());
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
			this.spectralCoordinate.setNbBins((int) ds[2]);		
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
			this.detectionMessage = "Take the largest image dimension";
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
		boolean retour =  spectralCoordinate.convertWCS(this.tableAttributeHandler);
		this.readUnit = this.spectralCoordinate.getMappedUnit();
		if( retour )
			this.detectionMessage = spectralCoordinate.detectionMessage;
		return retour;
	}

	/**
	 * @return
	 * @throws IgnoreException 
	 */
	private boolean findSpectralCoordinateByKW() throws Exception{
		/*
		 * If no range set in params, try to find it out from fields
		 */	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Search spectral coordinate in the columns names");
		if( this.entryAttributeHandler != null ){
			ColumnSetter ah = this.searchColumnsByName(RegExp.SPEC_AXIS_KW);
			if( !ah.notSet()  ){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getAttNameOrg()));
				this.readUnit = ah.getUnit();
				this.detectionMessage = ah.message.toString();
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
		ColumnSetter ah = this.searchByUcd(RegExp.SPEC_MIN_UCD);
		if( !ah.notSet() ){
			spectralCoordinate.setOrgMin(Double.parseDouble(ah.getValue()));
			this.detectionMessage = ah.message.toString();
			findMin = true;
			this.readUnit = ah.getUnit();
		}
		ah = this.searchByUcd(RegExp.SPEC_MAX_UCD);
		if( !ah.notSet() ){
			spectralCoordinate.setOrgMax(Double.parseDouble(ah.getValue()));
			this.detectionMessage += ah.message.toString();
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
			if( !ah.notSet() ){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getAttNameOrg()));
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
	
	/**
	 * @return
	 * @throws FatalException
	 */
	public ColumnSetter getResPower() throws Exception{
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the resolution power");
		ColumnSetter retour =  this.search(RegExp.SPEC_RESPOWER_UCD, RegExp.SPEC_RESPOWER_KW);
		if( retour.notSet() ) {
			if( spectralCoordinate == null ){
				this.mapCollectionSpectralCoordinateAuto();
			}
			if( spectralCoordinate.getNbBins() != SaadaConstant.INT) {
				retour =  new ColumnSetter();
				retour.setByValue(Double.toString((1.0/spectralCoordinate.getNbBins())), false);
				retour.completeMessage("Computed from the number of bins ("+ spectralCoordinate.getNbBins() + ")");
				return retour;
			} 
			return  new ColumnSetter();
			
		} else {
			return retour;
		}
	}
	
//	/**
//	 * @return
//	 * @throws Exception
//	 */
//	public ColumnSetter getComputedResPower() throws Exception{
//		if( spectralCoordinate == null ){
//			this.mapCollectionSpectralCoordinateAuto();
//		}
//		if( spectralCoordinate.getNbBins() != SaadaConstant.INT) {
//			ColumnSetter retour =  new ColumnSetter();
//			retour.setByValue(Double.toString((1.0/spectralCoordinate.getNbBins())), false);
//			retour.completeMessage("Computed from the number of bins ("+ spectralCoordinate.getNbBins() + ")");
//			return retour;
//		} 
//		return  new ColumnSetter();
//	}
	
	/**
	 * @return
	 */
	public double getRaWCSCenter() {
		return this.spectralCoordinate.getRaWCSCenter();
	}
	/**
	 * @return
	 */
	public double getDecWCSCenter() {
		return this.spectralCoordinate.getDecWCSCenter();
	}
	public ColumnSetter getEUnit() {
		// TODO Auto-generated method stub
		return null;
	}
	public ColumnSetter getEMax() {
		// TODO Auto-generated method stub
		return null;
	}
	public ColumnSetter getEMin() {
		// TODO Auto-generated method stub
		return null;
	}


}
