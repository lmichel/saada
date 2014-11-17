package saadadb.products.inference;

import hecds.LibLog;
import hecds.wcs.Modeler;
import hecds.wcs.descriptors.CardDescriptor;
import hecds.wcs.descriptors.CardMap;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.DataFile;
import saadadb.products.ppknowledge.KnowledgeBase;
import saadadb.products.ppknowledge.PipelineParser;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.Messenger;
import saadadb.util.MessengerLogger;

public class QuantityDetector {
	private final PipelineParser pipelineParser;

	private final ObservableKWDetector observableKWDetector;
	private final TimeKWDetector timeKWDetector;
	private final EnergyKWDetector energyKWDetector;
	private final SpaceKWDetector spaceKWDetector;
	private final ObservationKWDetector observationKWDetector;
	private final PolarizationKWDetector polarizationKWDetector;
	private final ProductMapping productMapping;
	public String detectionMessage;
	protected Modeler wcsModeler;

	/**
	 * @param tableAttributeHandlers
	 * @throws SaadaException 
	 */
	public QuantityDetector(Map<String, AttributeHandler> tableAttributeHandlers, List<String> comments, ProductMapping productMapping) throws SaadaException {
		/*
		 * The WCS modeler is external to Saada, it works with CardDescripors instead of AttributeHandler
		 */
		try {
			this.setWcsModeler(tableAttributeHandlers);	
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}
		this.observableKWDetector   = new ObservableKWDetector(tableAttributeHandlers, comments);
		this.timeKWDetector         = new TimeKWDetector(tableAttributeHandlers,this.wcsModeler, comments);						
		this.energyKWDetector       = new EnergyKWDetector(tableAttributeHandlers, this.wcsModeler, comments, productMapping);
		this.spaceKWDetector        = new SpaceKWDetector(tableAttributeHandlers, this.wcsModeler, comments);
		this.observationKWDetector  = new ObservationKWDetector(tableAttributeHandlers, comments);
		this.polarizationKWDetector = new PolarizationKWDetector(tableAttributeHandlers,this.wcsModeler, comments);
		this.pipelineParser = KnowledgeBase.getParser(tableAttributeHandlers);
		this.productMapping = productMapping;
	}
	/**
	 * @param tableAttributeHandlers
	 * @param entryAttributeHandlers
	 * @throws SaadaException 
	 */
	public QuantityDetector(Map<String, AttributeHandler> tableAttributeHandlers
			, Map<String, AttributeHandler> entryAttributeHandlers, List<String> comments
			, ProductMapping productMapping, DataFile productFile) throws SaadaException {
		/*
		 * The WCS modeler is external to Saada, it works with CardDescripors instead of AttributeHandler
		 */
		try {
			this.setWcsModeler(tableAttributeHandlers);	
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}
		this.observableKWDetector   = new ObservableKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.timeKWDetector         = new TimeKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments);
		this.energyKWDetector       = new EnergyKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments, productMapping, productFile);
		this.spaceKWDetector        = new SpaceKWDetector(tableAttributeHandlers, entryAttributeHandlers, this.wcsModeler, comments);
		this.observationKWDetector  = new ObservationKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.polarizationKWDetector = new PolarizationKWDetector(tableAttributeHandlers, this.wcsModeler, comments);
		this.pipelineParser = KnowledgeBase.getParser(tableAttributeHandlers, entryAttributeHandlers);
		this.productMapping = productMapping;
	}

	/**
	 * @param tableAttributeHandlers
	 * @throws Exception
	 */
	private void setWcsModeler(Map<String, AttributeHandler> tableAttributeHandlers) throws Exception{
		CardMap cm = new CardMap(new HashSet<CardDescriptor>(tableAttributeHandlers.values()));
		LibLog.setLogger(new MessengerLogger());
		this.wcsModeler = new Modeler(cm);			
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
	public ColumnExpressionSetter getSpatialError() throws SaadaException{
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
	public ColumnExpressionSetter getRegion() throws SaadaException{
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
		return (retour == null)?new ColumnExpressionSetter("x_unit_org"): retour;
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
	/*
	 * Observable axis
	 */
	public ColumnExpressionSetter getObservableUcd() throws Exception{
		ColumnExpressionSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getUcdName()).isNotSet() ){
			retour = this.observableKWDetector.getUcdName();
			if( retour.isNotSet() && !getEMin().isNotSet() && !getEMax().isNotSet() ) {
				retour.setByValue("phot.count", false);
				retour.completeMessage("Value taken by default since the dispersion axis is set");
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
				retour.completeMessage("Value taken by default since the dispersion axis is set");
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
