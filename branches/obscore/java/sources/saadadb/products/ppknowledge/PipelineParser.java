package saadadb.products.ppknowledge;

import java.util.Map;

import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;
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
	protected ColumnExpressionSetter getColmumnSetter(String key){
		AttributeHandler ah = this.getAttributeHandler(key);
		if( ah == null ){
			return new ColumnExpressionSetter(key);
		} else {
			ColumnExpressionSetter cs = new ColumnExpressionSetter(key);
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
	public ColumnExpressionSetter getCollectionName() throws SaadaException {
		return new ColumnExpressionSetter("obs_collection");
	}
	public ColumnExpressionSetter getObsPublisherDid() throws SaadaException {
		return new ColumnExpressionSetter("obs_publisher_did");
	}
	public ColumnExpressionSetter getCalibLevel() throws SaadaException, Exception {
		return new ColumnExpressionSetter("calib_level");
	}
	public ColumnExpressionSetter getTargetName() throws SaadaException {
		return new ColumnExpressionSetter("target_name");
	}
	public ColumnExpressionSetter getFacilityName() throws SaadaException {
		return new ColumnExpressionSetter("facility_name");
	}
	public ColumnExpressionSetter getExposureName() throws SaadaException {
		return new ColumnExpressionSetter("exposure_name");
	}
	public ColumnExpressionSetter getInstrumentName() throws SaadaException {
		return new ColumnExpressionSetter("instrument_name");
	}
	public ColumnExpressionSetter getObsIdComponents() throws SaadaException {
		return new ColumnExpressionSetter("obs_id");
	}
	/*
	 * space axis
	 */
	public ColumnExpressionSetter getFrame() throws SaadaException {
		return new ColumnExpressionSetter("astroframe");
	}
	public ColumnExpressionSetter getAscension() throws SaadaException {
		return new ColumnExpressionSetter("s_ra");
	}
	public ColumnExpressionSetter getDeclination() {
		return new ColumnExpressionSetter("s_dec");
	}
	public ColumnExpressionSetter getSpatialError() throws SaadaException {
		return new ColumnExpressionSetter("s_resolution");
	}
	public ColumnExpressionSetter getfov() throws SaadaException {
		return new ColumnExpressionSetter("s_fov");
	}
	public ColumnExpressionSetter getRegion() throws SaadaException {
		return new ColumnExpressionSetter("s_region");
	}
	/*
	 * Energy axis
	 */
	public ColumnExpressionSetter getEMin() throws SaadaException, Exception {
		return new ColumnExpressionSetter("em_min");
	}
	public ColumnExpressionSetter getEMax() throws SaadaException, Exception {
		return new ColumnExpressionSetter("em_max");
	}
	public ColumnExpressionSetter getEUnit() throws SaadaException, Exception {
		return new ColumnExpressionSetter("x_unit_org");
	}
	public ColumnExpressionSetter getResPower() throws SaadaException, Exception {
		return new ColumnExpressionSetter("em_res_power");
	}
	/*
	 * time axis
	 */
	public ColumnExpressionSetter getTMin() throws SaadaException, Exception {
		return new ColumnExpressionSetter("t_min");
	}
	public ColumnExpressionSetter getTMax() throws SaadaException {
		return new ColumnExpressionSetter("t_max");
	}
	public ColumnExpressionSetter getExpTime() throws SaadaException {
		return new ColumnExpressionSetter("t_exptime");
	}
	/*
	 * Observable axis
	 */
	public ColumnExpressionSetter getUnitName() throws SaadaException {
		return new ColumnExpressionSetter("o_unit");
	}
	public ColumnExpressionSetter getCalibStatus() throws SaadaException {
		return new ColumnExpressionSetter("o_calib_status");
	}
	public ColumnExpressionSetter getUcdName() throws SaadaException {
		return new ColumnExpressionSetter("o_ucd");
	}
	/*
	 * Polarization axis
	 */
	public ColumnExpressionSetter getPolarizationStates() {
		return new ColumnExpressionSetter("polStates");
	}
}
