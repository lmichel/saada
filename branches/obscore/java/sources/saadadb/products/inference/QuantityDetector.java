package saadadb.products.inference;

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
import saadadb.products.setter.ColumnSingleSetter;

public class QuantityDetector {
	private final PipelineParser pipelineParser;

	private final ObservableKWDetector observableKWDetector;
	private final TimeKWDetector timeKWDetector;
	private final EnergyKWDetector energyKWDetector;
	private final SpaceKWDetector spaceKWDetector;
	private final ObservationKWDetector observationKWDetector;
	private final ProductMapping productMapping;
	public String detectionMessage;

	/**
	 * @param tableAttributeHandlers
	 * @throws SaadaException 
	 */
	public QuantityDetector(Map<String, AttributeHandler> tableAttributeHandlers, List<String> comments, ProductMapping productMapping) throws SaadaException {
		this.observableKWDetector  = new ObservableKWDetector(tableAttributeHandlers, comments);
		this.timeKWDetector        = new TimeKWDetector(tableAttributeHandlers, comments);						
		this.energyKWDetector      = new EnergyKWDetector(tableAttributeHandlers, comments, productMapping);
		this.spaceKWDetector       = new SpaceKWDetector(tableAttributeHandlers, comments);
		this.observationKWDetector = new ObservationKWDetector(tableAttributeHandlers, comments);
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
		this.observableKWDetector  = new ObservableKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.timeKWDetector        = new TimeKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.energyKWDetector      = new EnergyKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments, productMapping, productFile);
		this.spaceKWDetector       = new SpaceKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.observationKWDetector = new ObservationKWDetector(tableAttributeHandlers, entryAttributeHandlers, comments);
		this.pipelineParser = KnowledgeBase.getParser(tableAttributeHandlers, entryAttributeHandlers);
		this.productMapping = productMapping;
	}

	/*
	 * Observation axis
	 */
	public ColumnSingleSetter getCollectionName() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getCollectionName()).notSet() ){
			return this.observationKWDetector.getCollectionName();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getTargetName() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTargetName()).notSet() ){
			return this.observationKWDetector.getTargetName();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getFacilityName() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getFacilityName()).notSet() ){
			return this.observationKWDetector.getFacilityName();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getInstrumentName() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getInstrumentName()).notSet() ){
			return this.observationKWDetector.getInstrumentName();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getExposureName()throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getExposureName()).notSet() ){
			return this.timeKWDetector.getExposureName();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	/*
	 * Space Axe
	 */	
	public ColumnSingleSetter getFrame()  throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getFrame()).notSet() ){
			return this.spaceKWDetector.getFrame();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getAscension()  throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getAscension()).notSet() ){
			return this.spaceKWDetector.getAscension();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getDeclination()  throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getDeclination()).notSet() ){
			return this.spaceKWDetector.getDeclination();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getSpatialError() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getSpatialError()).notSet() ){
			return this.spaceKWDetector.getSpatialError();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getfov() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getfov()).notSet() ){
			return this.spaceKWDetector.getfov();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getRegion() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getRegion()).notSet() ){
			return this.spaceKWDetector.getRegion();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
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
	public ColumnSingleSetter getResPower() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getResPower()).notSet() ){
			try {
				return this.energyKWDetector.getResPower();
			} catch (Exception e) {
				e.printStackTrace();
				IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			}
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getEMin() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEMin()).notSet() ){
			return this.energyKWDetector.getEMin();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getEMax() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEMax()).notSet() ){
			return this.energyKWDetector.getEMax();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getEUnit() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getEUnit()).notSet() ){
			return this.energyKWDetector.getEUnit();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	//	public SpectralCoordinate getSpectralCoordinate() throws SaadaException {
	//		try  {
	//			return this.energyKWDetector.getSpectralCoordinate();
	//		} catch (Exception e) {
	//			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, e);
	//			return null;
	//		}
	//	}

	/*
	 * Time axis
	 */
	public ColumnSingleSetter getTMin() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTMin()).notSet() ){
			return this.timeKWDetector.getTMin();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getTMax() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getTMax()).notSet() ){
			return this.timeKWDetector.getTMax();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getExpTime() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getExpTime()).notSet() ){
			return this.timeKWDetector.getExpTime();
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	/*
	 * Observable axis
	 */
	public ColumnSingleSetter getObservableUcd() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getUcdName()).notSet() ){
			retour = this.observableKWDetector.getUcdName();
			if( retour.notSet() && !getEMin().notSet() && !getEMax().notSet() ) {
				retour.setByValue("phot.count", false);
				retour.completeMessage("Value taken by default since the dispersion axis is set");
			}
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}
	public ColumnSingleSetter getObservableUnit() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getUnitName()).notSet() ){
			retour = this.observableKWDetector.getUnitName();
			if( retour.notSet() &&  !getEMin().notSet() && !getEMax().notSet()) {
				retour.setByValue("counts", false);
				retour.completeMessage("Value taken by default since the dispersion axis is set");
			}
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;

	}
	public ColumnSingleSetter getCalibStatus() throws SaadaException{
		ColumnSingleSetter retour = null;
		if( this.pipelineParser == null ||(retour = this.pipelineParser.getCalibStatus()).notSet() ){
			retour = this.observableKWDetector.getCalibStatus();
			if( retour.notSet() &&  !getEMin().notSet() && !getEMax().notSet() ) {
				if( this.getEUnit().notSet()) {
					retour.setByValue("0", false);
					retour.completeMessage("Value taken by default since the dispersion axis is set but not calibrated");
				} else if( this.getEUnit().getValue().equalsIgnoreCase("channels")) {
					retour.setByValue("1", false);
					retour.completeMessage("Value taken by default since the dispersion axis is set but not calibrated");
				} else {
					retour.setByValue("2", false);
					retour.completeMessage("Value taken by default since the dispersion axis is set");
				}
			}
		} 
		return (retour == null)?new ColumnSingleSetter(): retour;
	}


}
