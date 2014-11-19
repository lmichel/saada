package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.types.AxeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.DataFile;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnRowSetter;
import saadadb.products.setter.ColumnWcsSetter;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.PriorityMode;

/**
 * The detection of the energy range is tricky. Even in self-detection mode it can use mapping parameters such as unit. 
 * That is why the ProductMapping is  transmitted to that tool
 * The spectral range can be either detected by keyword ot by taking the min/max in a table column or by taking a 
 * pixel range (WCS in that case) 
 * -------------------------------
 *    case         expression
 * -------------------------------
 *     WCS        WCS.getMin(1) 
 *    column     getMinValue(colName) 
 *     row     		getMinRowNumber()
 *      KW             KW
 * -------------------------------
 * 
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
	private ColumnExpressionSetter em_minSetter;
	private ColumnExpressionSetter em_maxSetter;
	private ColumnExpressionSetter x_unit_orgSetter;
	private ColumnExpressionSetter em_res_powerSetter;
	private ColumnExpressionSetter em_binsSetter;

	/**
	 * @param tableAttributeHandler
	 * @param comments
	 * @param productMapping
	 * @throws SaadaException
	 */
	public EnergyKWDetector(Map<String, AttributeHandler> tableAttributeHandler, Modeler wcsModeler, List<String> comments, ProductMapping productMapping)throws SaadaException {
		super(tableAttributeHandler, wcsModeler.getProjection(AxeType.SPECTRAL));
		this.setUnitMode(productMapping);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
		this.mapCollectionSpectralCoordinateAuto();
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
			, Map<String, AttributeHandler> entryAttributeHandler, Modeler wcsModeler, List<String> comments, ProductMapping productMapping, DataFile productFile)throws SaadaException {
		super(tableAttributeHandler, entryAttributeHandler, wcsModeler.getProjection(AxeType.SPECTRAL));
		this.setUnitMode(productMapping);
		this.comments = (comments == null)? new ArrayList<String>(): comments;
		this.productFile = productFile;
		this.mapCollectionSpectralCoordinateAuto();
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
			if( this.findSpectralCoordinateByWCS() || this.findSpectralCoordinateInKeywords() ||  this.findSpectralCoordinateInColumns() ) {
				return true;
			}
//
//			spectralCoordinate = new SpectralCoordinate(Database.getSpect_unit());
//			boolean retour = ( this.findSpectralCoordinateInKeywords() ||  this.findSpectralCoordinateInColumns() || this.findSpectralCoordinateByWCS() ||
//					this.findSpectralCoordinateInPixels());
//			if( this.priority == PriorityMode.LAST ) {
//				if( this.readUnit == null || this.readUnit.length() == 0) {
//					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the mapped unit <" + this.defaultUnit + ">");
//					spectralCoordinate.setMappedUnit(this.defaultUnit);
//				} else {
//					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Take the detected unit <" + this.readUnit + ">");
//					spectralCoordinate.setMappedUnit(this.readUnit);
//				}
//			}
//			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Detected range " + spectralCoordinate.getOrgMin() + " " + spectralCoordinate.getOrgMax() + " " + spectralCoordinate.getMappedUnit());
//			return retour;
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		return false;
	}

	/**
	 * @param ds
	 * @return
	 */
	@Deprecated
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
	@Deprecated
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
	 * Ask the WCS projection for spectral coordinates
	 * @return true if the dispersion has been found
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateByWCS() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for spectral coordinate in WCS");
		if( this.projection.isUsable()){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found spectral coodinate in WCS");
			this.em_minSetter     = new ColumnWcsSetter("em_min"    , "WCS.getMin(1)", this.projection);
			this.em_maxSetter     = new ColumnWcsSetter("em_max"    , "WCS.getMax(1)", this.projection);
			this.em_binsSetter    = new ColumnWcsSetter("em_bins"   , "WCS.getNaxis(1)", this.projection);
			this.x_unit_orgSetter = new ColumnWcsSetter("x_unit_org", "WCS.getUnit(1)", this.projection);
			this.x_unit_orgSetter.calculateExpression();
			/*
			 * WCS can be valid but without unit. In this case, unit is set as notSet, in order to be possibly overridden later
			 */
			if( this.x_unit_orgSetter.getValue() == null || this.x_unit_orgSetter.getValue().length() == 0) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No unit found in WCS");
				this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org");
			}
			return true;
		} else {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No spectral coordinate found in WCS");
			return false;
		}
	}

	/**
	 * Look for the spectral coordinates in the table columns (if exist). 
	 * If there is no explicit dispersion column but a flux column, the row number is taken as dispersion value
	 * @return true if the dispersion has been found
	 * @throws IgnoreException 
	 */
	private boolean findSpectralCoordinateInColumns() throws Exception{
		/*
		 * If no range set in params, try to find it out from fields
		 */	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinate in the columns");
		if( this.entryAttributeHandler != null ){
			ColumnExpressionSetter ah = this.searchColumns(null, RegExp.SPEC_AXIS_KW, RegExp.SPEC_AXIS_DESC);
			if( !ah.isNotSet()  ){
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Found column "+ ah.getAttNameOrg());
				this.em_minSetter = new ColumnRowSetter("em_min", "Column.getMinValue(" + ah.getAttNameOrg() + ")");
				this.em_minSetter.completeMessage("Column "+ ah.getAttNameOrg() + " taken as dispersion axe");
				this.em_maxSetter = new ColumnRowSetter("em_max", "Column.getMaxValue(" + ah.getAttNameOrg() + ")");
				this.em_maxSetter.completeMessage("Column "+ ah.getAttNameOrg() + " taken as dispersion axe");			
				this.em_binsSetter = new ColumnRowSetter("em_bins", "Column.getNbRows(" + ah.getAttNameOrg() + ")");			
				this.em_binsSetter.completeMessage("Take the row number");	
				/*
				 * In this case the column is taken from metadata. Metadata are supposed to be the same for all products
				 * using this instance, so we can set it as a constant 
				 */
				this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org");
				if( ah.getUnit() != null && ah.getUnit().length() > 0 )  {
					this.x_unit_orgSetter.setByValue(ah.getUnit(), false);
					this.x_unit_orgSetter.completeMessage("Unit if the column " + ah.getAttNameOrg());
				} 
				this.detectionMessage = ah.message.toString();
				return true;
			} 		
			/*
			 * If no column look like a dispersion, we look for a flux column, and take the row number as dispersion
			 */
			else {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Check if a column can be a flux");
				ah = this.searchColumns(null, RegExp.SPEC_FLUX_KW, RegExp.SPEC_FLUX_DESC);
				if( !ah.isNotSet()  ){
					this.em_minSetter = new ColumnExpressionSetter("em_min", "0");
					this.em_minSetter.completeMessage("Row number taken as dispersion axe");
					this.em_maxSetter = new ColumnRowSetter("em_max", "Column.getNbRows(" + ah.getAttNameOrg() + ")");				
					this.em_maxSetter.completeMessage("Row number taken as dispersion axe");
					this.em_binsSetter = new ColumnRowSetter("em_bins", "Column.getNbRows(" + ah.getAttNameOrg() + ")");			
					this.em_binsSetter.completeMessage("Take the row number");	
					this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org");
					this.x_unit_orgSetter.setByValue("channel", false);
					this.x_unit_orgSetter.completeMessage("Row number as dispersion: take channel as unit");
					this.detectionMessage = "take row number as dispersion axis";
					return true;
				} 		
			}
		} 
		return  false;
	}

	/**
	 * Look for keywords giving the spectral range.
	 * The unit detection is not required by this method to succeed
	 * @return
	 * @throws Exception 
	 */
	private boolean findSpectralCoordinateInKeywords() throws Exception{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching spectral coordinates by UCDs or keyword ");
		this.em_minSetter = this.search("em_min", RegExp.SPEC_MIN_UCD, RegExp.SPEC_MIN_KW);
		this.em_maxSetter = this.search("em_max", RegExp.SPEC_MAX_UCD, RegExp.SPEC_MAX_KW);
		this.em_res_powerSetter = this.search("em_res_power", RegExp.SPEC_RESPOWER_UCD, RegExp.SPEC_RESPOWER_KW);
		if( !this.em_minSetter.isNotSet() && !this.em_maxSetter.isNotSet() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Spectral range found in keywords");
			String u;
			if( (u = this.em_minSetter.getUnit()) != null && u.length() > 0 ){
				this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org", u);
			} else if( (u = this.em_maxSetter.getUnit()) != null && u.length() > 0 ){
				this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org", u);
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "");
				this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org");
			}
			return true;
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Spectral range not found in keywords");
		return false;
	}


	/**
	 * @return
	 * @throws FatalException
	 */
	public ColumnExpressionSetter getResPower() throws Exception{
		return (this.em_res_powerSetter == null)? new ColumnExpressionSetter("em_res_power"): this.em_res_powerSetter;
		//		String fn = "em_res_power";
		//		if( Messenger.debug_mode ) 
		//			Messenger.printMsg(Messenger.DEBUG, "Search for the resolution power");
		//		ColumnExpressionSetter retour =  this.search(fn, RegExp.SPEC_RESPOWER_UCD, RegExp.SPEC_RESPOWER_KW);
		//		if( retour.notSet() ) {
		//			if( spectralCoordinate == null ){
		//				this.mapCollectionSpectralCoordinateAuto();
		//			}
		//			if( spectralCoordinate.getNbBins() != SaadaConstant.INT 
		//					&&  this.spectralCoordinate.getOrgMin() != SaadaConstant.DOUBLE &&  this.spectralCoordinate.getOrgMax() != SaadaConstant.DOUBLE) {
		//				retour =  new ColumnExpressionSetter(fn);
		//				double v     = ((this.spectralCoordinate.getOrgMax() + this.spectralCoordinate.getOrgMin())/2.);
		//				double delta = ((this.spectralCoordinate.getOrgMax() - this.spectralCoordinate.getOrgMin())/spectralCoordinate.getNbBins());
		//				retour.setByValue(Double.toString(v/delta), false);
		//				retour.completeMessage("Computed from both range and bin number ("+ spectralCoordinate.getNbBins() + ")");
		//				return retour;
		//			} 
		//			return  new ColumnExpressionSetter(fn);
		//
		//		} else {
		//			return retour;
		//		}
	}
	/**
	 * return the number of channels or bins
	 * @return
	 * @throws Exception
	 */
	public ColumnExpressionSetter getEBins() throws Exception{
		return (this.em_binsSetter == null)? new ColumnExpressionSetter("em_bins"): this.em_binsSetter;
	}

	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEUnit() throws SaadaException{
		return (this.x_unit_orgSetter == null)? new ColumnExpressionSetter("x_unit_org"): this.x_unit_orgSetter;
		//		if( spectralCoordinate == null ){
		//			this.mapCollectionSpectralCoordinateAuto();
		//		}
		//		ColumnExpressionSetter retour = new ColumnExpressionSetter("x_unit_org");
		//		if( this.spectralCoordinate.getMappedUnit() != null ){
		//			retour.setByValue(String.valueOf(this.spectralCoordinate.getMappedUnit()), false);
		//			retour.completeMessage(this.spectralCoordinate.detectionMessage);
		//		}
		//		return retour;
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEMax() throws SaadaException{
		return (this.em_maxSetter == null)? new ColumnExpressionSetter("emax"): this.em_maxSetter;
		//		if( spectralCoordinate == null ){
		//			this.mapCollectionSpectralCoordinateAuto();
		//		}
		//		ColumnExpressionSetter retour = new ColumnExpressionSetter("em_max");
		//		if( this.spectralCoordinate.getOrgMax() != SaadaConstant.DOUBLE && !Double.isNaN(this.spectralCoordinate.getOrgMax())){
		//			retour.setByValue(String.valueOf(this.spectralCoordinate.getOrgMax()), false);
		//			retour.completeMessage(this.detectionMessage);	
		//		}
		//		return retour;
	}
	/**
	 * @return
	 * @throws SaadaException
	 */
	public ColumnExpressionSetter getEMin() throws SaadaException {
		return (this.em_minSetter == null)? new ColumnExpressionSetter("emin"): this.em_minSetter;
		//		if( spectralCoordinate == null ){
		//			this.mapCollectionSpectralCoordinateAuto();
		//		}
		//		ColumnExpressionSetter retour = new ColumnExpressionSetter("em_min");
		//		if( this.spectralCoordinate.getOrgMin() != SaadaConstant.DOUBLE && !Double.isNaN(this.spectralCoordinate.getOrgMin())){
		//			retour.setByValue(String.valueOf(this.spectralCoordinate.getOrgMin()), false);
		//			retour.completeMessage(this.detectionMessage);	
		//		}
		//		return retour;
	}
}
