package saadadb.products.inference;

import hecds.wcs.Modeler;
import hecds.wcs.transformations.spatial.SpatialProjection;

import java.util.List;
import java.util.Map;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ppknowledge.KnowledgeBase;
import saadadb.products.ppknowledge.PipelineParser;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnSetter;

public class QuantityDetector {
	private final PipelineParser pipelineParser;

	private final ObservableKWDetector observableKWDetector;
	private final TimeKWDetector timeKWDetector;
	private final EnergyKWDetector energyKWDetector;
	private final SpaceKWDetector spaceKWDetector;
	private final ObservationKWDetector observationKWDetector;
	private final PolarizationKWDetector polarizationKWDetector;
	public String detectionMessage;
	protected Modeler wcsModeler;

	/**
	 * @param tableAttributeHandlers
	 * @throws SaadaException 
	 */
	public QuantityDetector(Map<String, AttributeHandler> tableAttributeHandlers
			, List<String> comments
			, ProductMapping productMapping
			, Modeler wcsModeler) throws SaadaException {
		this.wcsModeler = wcsModeler;
		this.observableKWDetector   = new ObservableKWDetector(tableAttributeHandlers, comments);
		this.timeKWDetector         = new TimeKWDetector(tableAttributeHandlers,this.wcsModeler, comments);						
		this.energyKWDetector       = new EnergyKWDetector(tableAttributeHandlers, this.wcsModeler, comments, productMapping);
		this.spaceKWDetector        = new SpaceKWDetector(tableAttributeHandlers, this.wcsModeler, comments);
		this.observationKWDetector  = new ObservationKWDetector(tableAttributeHandlers, comments);
		this.polarizationKWDetector = new PolarizationKWDetector(tableAttributeHandlers,this.wcsModeler, comments);
		this.pipelineParser = KnowledgeBase.getParser(tableAttributeHandlers);
	}
	/**
	 * @param tableAttributeHandlers
	 * @param entryAttributeHandlers
	 * @throws SaadaException 
	 */
	public QuantityDetector(Map<String, AttributeHandler> tableAttributeHandlers
			, Map<String, AttributeHandler> entryAttributeHandlers
			, List<String> comments
			, ProductMapping productMapping
			, Modeler wcsModeler) throws SaadaException {

		this.wcsModeler = wcsModeler;
		this.observableKWDetector   = new ObservableKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.timeKWDetector         = new TimeKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments);
		this.energyKWDetector       = new EnergyKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments, productMapping);
		this.spaceKWDetector        = new SpaceKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments);
		this.observationKWDetector  = new ObservationKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.polarizationKWDetector = new PolarizationKWDetector(tableAttributeHandlers, this.wcsModeler, comments);
		this.pipelineParser = KnowledgeBase.getParser(tableAttributeHandlers, entryAttributeHandlers);
	}

	/*
	 * Observation axis
	 */
	public ColumnExpressionSetter getCollectionName() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getCollectionName()).isNotSet() ){
			return this.observationKWDetector.getCollectionName();
		} 
		return (retour == null)?new ColumnExpressionSetter("obs_collection"): retour;
	}
	public ColumnExpressionSetter getObsPublisherDid() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getObsPublisherDid()).isNotSet() ){
			return this.observationKWDetector.getObsPublisherDid();
		} 
		return (retour == null)?new ColumnExpressionSetter("obs_collection"): retour;
	}
	public ColumnExpressionSetter getCalibLevel() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getCalibLevel()).isNotSet() ){
			return this.observationKWDetector.getCalibLevel();
		} 
		return (retour == null)?new ColumnExpressionSetter("obs_collection"): retour;
	}
	public ColumnExpressionSetter getTargetName() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTargetName()).isNotSet() ){
			return this.observationKWDetector.getTargetName();
		} 
		return (retour == null)?new ColumnExpressionSetter("target_name"): retour;
	}
	public ColumnExpressionSetter getFacilityName() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getFacilityName()).isNotSet() ){
			return this.observationKWDetector.getFacilityName();
		} 
		return (retour == null)?new ColumnExpressionSetter("facility_name"): retour;
	}
	public ColumnExpressionSetter getInstrumentName() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getInstrumentName()).isNotSet() ){
			return this.observationKWDetector.getInstrumentName();
		} 
		return (retour == null)?new ColumnExpressionSetter("instrument_name"): retour;
	}
	public ColumnExpressionSetter getExposureName()throws SaadaException{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getExposureName()).isNotSet() ){
			return this.timeKWDetector.getExposureName();
		} 
		return (retour == null)?new ColumnExpressionSetter("exposure_name"): retour;
	}
	/*
	 * Space Axe
	 */	
	public ColumnExpressionSetter getFrame()  throws SaadaException{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getFrame()).isNotSet() ){
			return this.spaceKWDetector.getFrame();
		} 
		return (retour == null)?new ColumnExpressionSetter("astroframe"): retour;
	}
	public ColumnExpressionSetter getAscension()  throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getAscension()).isNotSet() ){
			return this.spaceKWDetector.getAscension();
		} 
		return (retour == null)?new ColumnExpressionSetter("s_ra"): retour;
	}
	public ColumnExpressionSetter getDeclination()  throws SaadaException{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getDeclination()).isNotSet() ){
			return this.spaceKWDetector.getDeclination();
		} 
		return (retour == null)?new ColumnExpressionSetter("s_dec"): retour;
	}
	public ColumnExpressionSetter getSpatialError() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getSpatialError()).isNotSet() ){
			return this.spaceKWDetector.getSpatialError();
		} 
		return (retour == null)?new ColumnExpressionSetter("s_resolution"): retour;
	}
	public ColumnExpressionSetter getfov() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getfov()).isNotSet() ){
			return this.spaceKWDetector.getfov();
		} 
		return (retour == null)?new ColumnExpressionSetter("s_fov"): retour;
	}
	public ColumnExpressionSetter getRegion() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getRegion()).isNotSet() ){
			return this.spaceKWDetector.getRegion();
		} 
		return (retour == null)?new ColumnExpressionSetter("s_region"): retour;
	}
	/**
	 * @return
	 * @throws FatalException
	 */
	public boolean arePosColFound() throws SaadaException {
		return this.spaceKWDetector.arePosColFound();
	}

	/**
	 * @return the size_alpha_csa value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getSizeRA() throws Exception{
		return this.spaceKWDetector.getSizeRA();
	}

	/**
	 * @return the naxis1 value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getNaxis1() throws Exception{
		return this.spaceKWDetector.getNaxis1();
	}
	/**
	 * @return the naxis2 value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getNaxis2() throws Exception{
		return this.spaceKWDetector.getNaxis2();
	}

	/**
	 * @return the size_delta_csa value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getSizeDEC() throws Exception{
		return this.spaceKWDetector.getSizeDEC();
	}

	/**
	 * @return the CTYPE1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSType1() throws Exception {
		return this.spaceKWDetector.getWCSType1();
	}

	/**
	 * @return the CTYPE2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSType2() throws Exception {
		return this.spaceKWDetector.getWCSType2();
	}
	/**
	 * @return the CRPIX1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrpix1() throws Exception {
		return this.spaceKWDetector.getWCSCrpix1();
	}
	/**
	 * @return the CRPIX2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrpix2() throws Exception {
		return this.spaceKWDetector.getWCSCrpix2();
	}
	/**
	 * @return the CRVAL1 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrval1() throws Exception {
		return this.spaceKWDetector.getWCSCrval1();
	}

	/**
	 * @return the CRVAL2 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCrval2() throws Exception {
		return this.spaceKWDetector.getWCSCrval2();
	}

	/**
	 * @return the CD11 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD11() throws Exception {
		return this.spaceKWDetector.getWCSCD11();
	}
	/**
	 * @return the CD12 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD12() throws Exception {
		return this.spaceKWDetector.getWCSCD12();
	}
	/**
	 * @return the CD22 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD21() throws Exception {
		return this.spaceKWDetector.getWCSCD21();
	}
	/**
	 * @return the CD22 WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCD22() throws Exception {
		return this.spaceKWDetector.getWCSCD22();
	}
	/**
	 * @return the CROTA WCS value if available
	 * @throws Exception 
	 */
	public ColumnExpressionSetter getWCSCROTA() throws Exception {
		return this.spaceKWDetector.getWCSCROTA();
	}
	/*
	 * Energy axis
	 */
	public ColumnExpressionSetter getResPower() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getResPower()).isNotSet() ){
			try {
				return this.energyKWDetector.getResPower();
			} catch (Exception e) {
				e.printStackTrace();
				IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			}
		} 
		return (retour == null)?new ColumnExpressionSetter("em_res_power"): retour;
	}
	public ColumnExpressionSetter getEMin() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEMin()).isNotSet() ){
			return this.energyKWDetector.getEMin();
		} 
		return (retour == null)?new ColumnExpressionSetter("em_min"): retour;
	}
	public ColumnExpressionSetter getEMax() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEMax()).isNotSet() ){
			return this.energyKWDetector.getEMax();
		} 
		return (retour == null)?new ColumnExpressionSetter("em_max"): retour;
	}
	public ColumnExpressionSetter getEUnit() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEUnit()).isNotSet() ){
			return this.energyKWDetector.getEUnit();
		} 
		return (retour == null)?new ColumnExpressionSetter("em_unit"): retour;
	}
	public ColumnExpressionSetter getEbins() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEBins()).isNotSet() ){
			return this.energyKWDetector.getEBins();
		} 
		return (retour == null)?new ColumnExpressionSetter("em_bins"): retour;
	}
	
	/*
	 * Time axis
	 */
	public ColumnExpressionSetter getTMin() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTMin()).isNotSet() ){
			return this.timeKWDetector.getTMin();
		} 
		return (retour == null)?new ColumnExpressionSetter("t_min"): retour;
	}
	public ColumnExpressionSetter getTMax() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTMax()).isNotSet() ){
			return this.timeKWDetector.getTMax();
		} 
		return (retour == null)?new ColumnExpressionSetter("t_max"): retour;
	}
	public ColumnExpressionSetter getExpTime() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getExpTime()).isNotSet() ){
			return this.timeKWDetector.getExpTime();
		} 
		return (retour == null)?new ColumnExpressionSetter("t_exptime"): retour;
	}
	public ColumnExpressionSetter getTResolution() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTResolution()).isNotSet() ){
			return this.timeKWDetector.getTResolution();
		} 
		return (retour == null)?new ColumnExpressionSetter("t_resolution"): retour;
	}
	/*
	 * Observable axis
	 */
	public ColumnExpressionSetter getObservableUcd() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getUcdName()).isNotSet() ){
			retour = this.observableKWDetector.getUcdName();
			if( retour.isNotSet() && !getEMin().isNotSet() && !getEMax().isNotSet() ) {
				retour.setByValue("phot.count", false);
				retour.completeDetectionMsg("Value taken by default since the dispersion axis is set");
			}
		} 
		return (retour == null)?new ColumnExpressionSetter("o_ucd"): retour;
	}
	public ColumnExpressionSetter getObservableUnit() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getUnitName()).isNotSet() ){
			retour = this.observableKWDetector.getUnitName();
			if( retour.isNotSet() &&  !getEMin().isNotSet() && !getEMax().isNotSet()) {
				retour.setByValue("counts", false);
				retour.completeDetectionMsg("Value taken by default since the dispersion axis is set");
			}
		} 
		return (retour == null)?new ColumnExpressionSetter("o_unit"): retour;

	}
	public ColumnExpressionSetter getCalibStatus() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getCalibStatus()).isNotSet() ){
			retour = this.observableKWDetector.getCalibStatus();
//			if( retour.notSet() &&  !getEMin().notSet() && !getEMax().notSet() ) {
//				System.out.println(this.getEUnit());
//				if( this.getEUnit().notSet()) {
//					retour.setByValue("0", false);
//					retour.completeMessage("Value taken by default since the dispersion axis is not set");
//				} else if( this.getEUnit().getValue().equalsIgnoreCase("channels")) {
//					retour.setByValue("1", false);
//					retour.completeMessage("Value taken by default since the dispersion axis is set but not calibrated");
//				} else {
//					retour.setByValue("2", false);
//					retour.completeMessage("Value taken by default since the dispersion axis is set");
//				}
//			}
		} 
		return (retour == null)?new ColumnExpressionSetter("o_calib_status"): retour;
	}
	public ColumnSetter getPolarizationStates() throws Exception {
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getPolarizationStates()).isNotSet() ){
			return this.polarizationKWDetector.getPolarizationStates();
		} 
		return (retour == null)?new ColumnExpressionSetter("t_exptime"): retour;
	}


}
