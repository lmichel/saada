package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

public abstract class PipelineParser {
	protected final  Map<String, AttributeHandler> attributeHandlers;
	protected final  Map<String, AttributeHandler> entryAttributeHandlers;
	
	/**
	 * @param tableAttributeHandlers
	 */
	public PipelineParser(Map<String, AttributeHandler> attributeHandlers) {
		this.attributeHandlers = attributeHandlers;
		this.entryAttributeHandlers = null;
	}
	/**
	 * @param tableAttributeHandlers
	 * @param entryAttributeHandlers
	 */
	public PipelineParser(Map<String, AttributeHandler> attributeHandlers, Map<String, AttributeHandler> entryAttributeHandlers) {
		this.attributeHandlers = attributeHandlers;
		this.entryAttributeHandlers = entryAttributeHandlers;
	}
	
	/**
	 * @param key: supposed to be a nameOrg
	 * @return
	 */
	protected AttributeHandler getAttributeHandler(String key){
		for( AttributeHandler ah: this.attributeHandlers.values() ){
			if( key.equals(ah.getNameattr()) ||  key.equals(ah.getNameorg()) ) {
				 return ah;
			}
		}
		if( Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Missing keyword " + key + " in product identified as " + this.getClass().getName());
		return null;
	}
	/**
	 * Returns a ColumnSetter ready to be used set with the value/unit of the AH matching the key
	 * @param key
	 * @return
	 */
	protected ColumnSingleSetter getColmumnSetter(String key){
		AttributeHandler ah = this.getAttributeHandler(key);
		if( ah == null ){
			return new ColumnSingleSetter();
		} else {
			ColumnSingleSetter cs = new ColumnSingleSetter();
			cs.setByWCS(ah.getValue(), false);
			cs.setUnit(ah.getUnit());
			cs.completeMessage("Issued from the knowledge base");
			return cs;
		}
	}
	
	/**
	 * @param key: supposed to be a nameOrg
	 * @return
	 */
	protected double getValue(String key) throws IgnoreException{
		try {
			return Double.parseDouble(this.getAttributeHandler(key).getValue());
		} catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, e)		;
		}
		return SaadaConstant.DOUBLE;
	}
	/**
	 * @param key supposed to be a nameOrg
	 * @return
	 * @throws IgnoreException
	 */
	protected String getStringValue(String key) throws IgnoreException{
		try {
			return this.getAttributeHandler(key).getValue();
		} catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, e)		;
		}
		return SaadaConstant.STRING;
	}

	/*
	 * Observation axis
	 */
	public ColumnSingleSetter getCollectionName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getTargetName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getFacilityName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getExposureName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getInstrumentName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getObsIdComponents() throws SaadaException {
		return new ColumnSingleSetter();
	}
	/*
	 * space axis
	 */
	public ColumnSingleSetter getFrame() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getAscension() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getDeclination() {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getSpatialError() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getfov() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getRegion() throws SaadaException {
		return new ColumnSingleSetter();
	}
	/*
	 * Energy axis
	 */
	public ColumnSingleSetter getEMin() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getEMax() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getEUnit() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getResPower() throws SaadaException {
		return new ColumnSingleSetter();
	}
	/*
	 * time axis
	 */
	public ColumnSingleSetter getTMin() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getTMax() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getExpTime() throws SaadaException {
		return new ColumnSingleSetter();
	}
	/*
	 * Observable axis
	 */
	public ColumnSingleSetter getUnitName() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getCalibStatus() throws SaadaException {
		return new ColumnSingleSetter();
	}
	public ColumnSingleSetter getUcdName() throws SaadaException {
		return new ColumnSingleSetter();
	}
}
