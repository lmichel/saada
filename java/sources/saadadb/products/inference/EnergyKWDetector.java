package saadadb.products.inference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.DataFile;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.PriorityMode;

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
	public List<String> comments;

	/**
	 * @param tableAttributeHandler
	 * @param comments
	 * @param productMapping
	 * @throws SaadaException
	 */
	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler, List<String> comments, ProductMapping productMapping)throws SaadaException {
		super(tableAttributeHandler);
		this.setUnitMode(productMapping);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
	}
	/**
	 * @param tableAttributeHandler
	 * @param entryAttributeHandler
	 * @param comments
	 * @param productMapping
	 * @param productFile   : used to get the ra,age of columns values
	 * @throws SaadaException
	 */
	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler
			, Map<String, AttributeHandler> entryAttributeHandler, List<String> comments, ProductMapping productMapping, DataFile productFile)throws SaadaException {
		super(tableAttributeHandler, entryAttributeHandler);
		this.setUnitMode(productMapping);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
		this.productFile = productFile;
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
	private boolean mapCollectionSpectralCoordinateAuto() throws SaadaException {	
		try {
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
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Detected range " + spectralCoordinate.getOrgMin() + " " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getMappedUnit());
			return retour;
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
			return false;
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
			return false;
		}
	}

	/**
	 * @param ds
	 * @return
	 */
	private void setMinMaxValues(double[] ds) {
		if( ds == null || ds.length != 3 ) {
			this.spectralCoordinate.setOrgMax(SaadaConstant.DOUBLE);
			this.spectralCoordinate.setOrgMin(SaadaConstant.DOUBLE);
		} else {
			this.spectralCoordinate.setOrgMax(ds[1]);
			this.spectralCoordinate.setOrgMin(ds[0]);		
			this.spectralCoordinate.setNbBins((int)(ds[1] - ds[0]));		
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateInPixels() throws Exception {
		Pattern p = Pattern.compile("Image column (.*) is wavelength \\((.*)\\)");
		int dim = 0;
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates in pixel array");
		for( String c: this.comments ) {
			Matcher m = p.matcher(c);
			if( m.find() && m.groupCount() == 2 ) {
				for( AttributeHandler ah : this.tableAttributeHandler.values() ){
					if( ah.getNameorg().matches("NAXIS\\d$")) {
						int v = Integer.parseInt(ah.getValue());
						dim = (v > dim)? v: dim;
					}
				}
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "find range=" + dim + "pixels unit=" + m.group(2).trim());
				this.spectralCoordinate = new SpectralCoordinate();
				this.spectralCoordinate.setOrgMin(0);
				this.spectralCoordinate.setOrgMax(dim);
				this.spectralCoordinate.setNbBins(dim);
				this.spectralCoordinate.setMappedUnit(m.group(2).trim());
				this.readUnit = m.group(2).trim();
				return true;
			}
		}
		this.spectralCoordinate = new SpectralCoordinate();
		this.spectralCoordinate.setOrgMin(SaadaConstant.DOUBLE);
		this.spectralCoordinate.setOrgMax(SaadaConstant.DOUBLE);
		this.spectralCoordinate.setMappedUnit(null);
		return false;
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
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in the columns names");
		if( this.entryAttributeHandler != null ){
			ColumnExpressionSetter ah = this.searchColumns(null, RegExp.SPEC_AXIS_KW, RegExp.SPEC_AXIS_DESC);
			if( !ah.notSet()  ){
				this.setMinMaxValues(this.productFile.getExtrema(ah.getAttNameOrg()));
				this.readUnit = ah.getUnit();
				this.detectionMessage = ah.message.toString();
				return true;
			} 		
			/*
			 * If no column look like a dispersion, we look for a flux column, and take the row number as dispersion
			 */
			else {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Check if a column can be a flux");
				ah = this.searchColumns(null, RegExp.SPEC_FLUX_KW, RegExp.SPEC_FLUX_DESC);
				if( !ah.notSet()  ){
					AttributeHandler na = this.tableAttributeHandler.get("naxis2");
					if( na == null ) {
						Messenger.printMsg(Messenger.TRACE, "No NAXIS2 key found: product format look suspect");
						return false;
					}
					this.setMinMaxValues(new double[]{0, Double.parseDouble(na.getValue())});
					this.readUnit = "channel";
					this.detectionMessage = "take row number as dispersion axis";
					return true;
				} 		
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
		ColumnExpressionSetter colSetter = this.searchByUcd("em_min", RegExp.SPEC_MIN_UCD);
		if( !colSetter.notSet() ){
			spectralCoordinate.setOrgMin(Double.parseDouble(colSetter.getValue()));
			this.detectionMessage = colSetter.message.toString();
			findMin = true;
			this.readUnit = colSetter.getUnit();
		}
		colSetter = this.searchByUcd("em_max", RegExp.SPEC_MAX_UCD);
		if( !colSetter.notSet() ){
			spectralCoordinate.setOrgMax(Double.parseDouble(colSetter.getValue()));
			this.detectionMessage += colSetter.message.toString();
			findMax = true;
			if( this.readUnit == null || this.readUnit.length() == 0) this.readUnit = colSetter.getUnit();
		}

		if ( this.entryAttributeHandler != null && (!findMax  || !findMin) ){
			/*
			 * If no range set in params, try to find it out from fields
			 */	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No Spectral coordinates found in header UCDs or UTypes: explore column definitions");
			findMin = false;
			colSetter = this.searchColumnsByUcd("range", RegExp.SPEC_BAND_UCD);
			if( !colSetter.notSet() ){
				this.setMinMaxValues(this.productFile.getExtrema(colSetter.getAttNameOrg()));
				if( this.readUnit == null || this.readUnit.length() == 0)  this.readUnit = colSetter.getUnit();
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
	public ColumnExpressionSetter getResPower() throws Exception{
		String fn = "em_res_power";
		if( Messenger.debug_mode ) 
			Messenger.printMsg(Messenger.DEBUG, "Search for the resolution power");
		ColumnExpressionSetter retour =  this.search(fn, RegExp.SPEC_RESPOWER_UCD, RegExp.SPEC_RESPOWER_KW);
		if( retour.notSet() ) {
			if( spectralCoordinate == null ){
				this.mapCollectionSpectralCoordinateAuto();
			}
			if( spectralCoordinate.getNbBins() != SaadaConstant.INT 
					&&  this.spectralCoordinate.getOrgMin() != SaadaConstant.DOUBLE &&  this.spectralCoordinate.getOrgMax() != SaadaConstant.DOUBLE) {
				retour =  new ColumnExpressionSetter(fn);
				double v     = ((this.spectralCoordinate.getOrgMax() + this.spectralCoordinate.getOrgMin())/2.);
				double delta = ((this.spectralCoordinate.getOrgMax() - this.spectralCoordinate.getOrgMin())/spectralCoordinate.getNbBins());
				retour.setByValue(Double.toString(v/delta), false);
				retour.completeMessage("Computed from both range and bin number ("+ spectralCoordinate.getNbBins() + ")");
				return retour;
			} 
			return  new ColumnExpressionSetter(fn);

		} else {
			return retour;
		}
	}

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
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEUnit() throws SaadaException{
		if( spectralCoordinate == null ){
			this.mapCollectionSpectralCoordinateAuto();
		}
		ColumnExpressionSetter retour = new ColumnExpressionSetter("x_unit_org");
		if( this.spectralCoordinate.getMappedUnit() != null ){
			retour.setByValue(String.valueOf(this.spectralCoordinate.getMappedUnit()), false);
			retour.completeMessage(this.spectralCoordinate.detectionMessage);
		}
		return retour;
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEMax() throws SaadaException{
		if( spectralCoordinate == null ){
			this.mapCollectionSpectralCoordinateAuto();
		}
		ColumnExpressionSetter retour = new ColumnExpressionSetter("em_max");
		if( this.spectralCoordinate.getOrgMax() != SaadaConstant.DOUBLE && !Double.isNaN(this.spectralCoordinate.getOrgMax())){
			retour.setByValue(String.valueOf(this.spectralCoordinate.getOrgMax()), false);
			retour.completeMessage(this.detectionMessage);	
		}
		return retour;
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEMin() throws SaadaException {
		if( spectralCoordinate == null ){
			this.mapCollectionSpectralCoordinateAuto();
		}
		ColumnExpressionSetter retour = new ColumnExpressionSetter("em_min");
		if( this.spectralCoordinate.getOrgMin() != SaadaConstant.DOUBLE && !Double.isNaN(this.spectralCoordinate.getOrgMin())){
			retour.setByValue(String.valueOf(this.spectralCoordinate.getOrgMin()), false);
			retour.completeMessage(this.detectionMessage);	
		}
		return retour;
	}
}
