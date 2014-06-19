package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ColumnSetter;
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
	protected ColumnSetter getColmumnSetter(String key){
		AttributeHandler ah = this.getAttributeHandler(key);
		if( ah == null ){
			return new ColumnSetter();
		} else {
			ColumnSetter cs = new ColumnSetter();
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
	public ColumnSetter getCollectionName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getTargetName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getFacilityName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getExposureName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getInstrumentName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getObsIdComponents() throws SaadaException {
		return new ColumnSetter();
	}
	/*
	 * space axis
	 */
	public ColumnSetter getFrame() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getAscension() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getDeclination() {
		return new ColumnSetter();
	}
	public ColumnSetter getErrorMin() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getErrorMaj() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getErrorAngle() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getfov() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getRegion() throws SaadaException {
		return new ColumnSetter();
	}
	/*
	 * Energy axis
	 */
	public ColumnSetter getEMin() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getEMax() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getEUnit() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getResPower() throws SaadaException {
		return new ColumnSetter();
	}
	/*
	 * time axis
	 */
	public ColumnSetter getTMin() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getTMax() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getExpTime() throws SaadaException {
		return new ColumnSetter();
	}
	/*
	 * Observable axis
	 */
	public ColumnSetter getUnitName() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getCalibStatus() throws SaadaException {
		return new ColumnSetter();
	}
	public ColumnSetter getUcdName() throws SaadaException {
		return new ColumnSetter();
	}
}
