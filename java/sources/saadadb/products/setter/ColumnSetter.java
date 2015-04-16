package saadadb.products.setter;

import hecds.wcs.descriptors.CardDescriptor;

import java.util.List;

import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.datafile.DataFile;
import saadadb.products.inference.QuantityDetector;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * This classs contains both the mapping of a column and the way it has be done.
 * @author michel
 * @version $Id$
 */
/**
 * @author michel
 * @version $Id$
 */
public abstract class ColumnSetter implements Cloneable {
	/**
	 * Mode used to get the value
	 */
	protected ColumnSetMode settingMode = ColumnSetMode.NOT_SET;
	/**
	 * Log of the auto-detection process (report {@link ProductMapping} action)
	 */
	public StringBuffer detectionMsg = new StringBuffer();
	/**
	 * Log of the mapping process(report {@link QuantityDetector} action)
	 */
	public StringBuffer userMappingMsg = new StringBuffer();
	/**
	 * Log of the conversion process (report {@link ProductIngestor} action)
	 */
	public StringBuffer conversionMsg = new StringBuffer();
	/**
	 * Normally used to store the value as it will be put inti the DB, can however to used to store 
	 * the value affected to the column but which can be saved as a string within the  AttributeHandler
	 */
	public Object storedValue;

	/**
	 * Constructor: instance must always have a not null attribute handler
	 * Make the setter as NOT_SET
	 * @param attributeHandler
	 * @throws FatalException 
	 */
	public ColumnSetter()  {
		this.settingMode = ColumnSetMode.NOT_SET;
	}
	/**
	 * @param attributeHandler
	 * @param setMode
	 * @throws FatalException 
	 */
	public ColumnSetter(ColumnSetMode setMode) throws FatalException {
		this.settingMode = setMode;
		this.setInitMessage();
	}	

	/**
	 * Basic constructor
	 */
	public ColumnSetter(String message) {
		this.settingMode = settingMode.NOT_SET;
		this.completeUserMappingMsg(message);
	}

	/**
//	 * Log a standrd message indicating wether the vlue has been set by mapping or bu auto detection
//	 * @param fromMapping
//	 */
//	protected void setMappingMessage( boolean fromMapping) {
//		if( fromMapping ) {
//			this.completeUserMappingMsg("user-mapping");
//		} else {
//			this.completeUserMappingMsg("auto-detection");
//		}
//	}
	/**
	 * Starts the log message according the mode used to get the KW value
	 */
	protected  abstract void setInitMessage();
	/**
	 * add "message" to the mapping log
	 * @param message
	 */
	public void completeUserMappingMsg(String message){
		this.completeMessage(message, this.userMappingMsg);
	}
	/**
	 * Take the mapping message of previousSetter as userv mapping message
	 * @param previousSetter
	 */
	public void completeUserMappingMsg(ColumnSetter previousSetter){
		this.completeMessage(((previousSetter == null)? "Nou used": previousSetter.getUserMappingMsg())
				, this.userMappingMsg);
	}
	/**
	 * add "message" to the auto detection log
	 * @param message
	 */
	public void completeDetectionMsg(String message){
		this.completeMessage(message, this.detectionMsg);
	}
	/**
	 * Take the detection message of previousSetter as userv mapping message
	 * @param previousSetter
	 */
	public void completeDetectionMsg(ColumnSetter previousSetter){
		this.completeMessage(((previousSetter == null)? "Not used": previousSetter.getUserMappingMsg())
				, this.detectionMsg);
	}

	/**
	 * set "message" to the conversion log
	 * @param message
	 */
	public void completeConversionMsg(String message){
		this.conversionMsg = new StringBuffer(message);
	}
	

	
	
	/**
	 * Appends properly message to storage
	 * @param message
	 * @param storage Usually tne of the instance message
	 */
	private void completeMessage(String message, StringBuffer storage){
		if( storage.length() > 0 )
			storage.append(", ");
		storage.append(message);
		
	}
	
	/**
	 * Set the value (STring) and set the BY_VALUE mode
	 * @param fromMapping
	 * @param value
	 */
	public abstract void setByValue(String value, boolean fromMapping);

	/**
	 * Set the value (numeric) and set the BY_VALUE mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByValue(double value, boolean fromMapping);

	/**
	 * @param keyword
	 */
	public abstract void setByKeyword(boolean fromMapping);

	/**
	 * Set the value (STring) and set the BY_WCS mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByKeyword(String value, boolean fromMapping);

	/**
	 * Set the value (numeric) and set the BY_WCS mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByKeyword(double value, boolean fromMapping);

	/**
	 * Set the value (STring) and set the BY_WCS mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByWCS(String value, boolean fromMapping);

	/**
	 * Set the value (numeric) and set the BY_WCS mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByWCS(double value, boolean fromMapping);

	/**
	 * Set the value (STring) and set the BY_PIXEL mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract 	void setByPixels(String value, boolean fromMapping);

	/**
	 * Set the value (numeric) and set the BY_PIXEL mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByPixels(double value, boolean fromMapping);

	/**
	 * Set the value (STring) and set the BY_PIXEL mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByTableColumn(String value, boolean fromMapping);

	/**
	 * Set the value (numeric) and set the BY_PIXEL mode
	 * @param value
	 * @param fromMapping
	 */
	public abstract void setByTabeColumn(double value, boolean fromMapping);

	/**
	 * 
	 */
	public abstract void setBySaada();

	/**
	 * Stores value/unit within the attribute handler without changing the mode.
	 * storedValue is also set with value
	 * @param value
	 * @param unit
	 */
	public abstract void setValue(double value, String unit);

	/**
	 * A setter must never be set in N OTSET mode since it can be reused elsewhere in the code.
	 * In case of error we just set the value to NOTSET with a conversion  message
	 * @param conversionMessage
	 */
	public abstract void setFailed(String conversionMessage);
	/**
	 * A setter must never be set in N OTSET mode since it can be reused elsewhere in the code.
	 * In case of error we just set the value to NOTSET with a conversion  message
	 * @param message
	 * @param e
	 */
	public abstract void setFailed(String message, Exception e);

//	/**
//	 * 
//	 */
//	public abstract void setNotSet();
//	/**
//	 * 
//	 */
//	public abstract void setNotSet(String message);
//	
//	/**
//	 * @param message
//	 * @param e
//	 */
//	public abstract void setNotSet(String message, Exception e);
//
	/**
	 * Set also the BY_VALUE mode
	 */
	public abstract void setValue(String value);

	/**
	 * Set the value as a string  into the AttributeHandler and keep the double value as the storedValue
	public void setValue(double value, String unit){
		this.attributeHandler.setValue(value);
		this.attributeHandler.setUnit(unit);
		this.storedValue = value;
	}
	 * @param value
	 */
	public abstract void setValue(double value);

	/**
	 * @return
	 */
	public String getExpression() {
		return null;
	}

	/**
	 * Relevant only for ExpresionSetter
	 * @return
	 */
	public List<AttributeHandler> getExprAttributes() {
		return null;
	}

	/*
	 * getters for the mode used to set the attribute
	 */
	public boolean byValue() {
		return (this.settingMode == ColumnSetMode.BY_VALUE);
	}
	public boolean byKeyword() {
		return (this.settingMode == ColumnSetMode.BY_KEYWORD);
	}
	public boolean byPiwels() {
		return (this.settingMode == ColumnSetMode.BY_PIXELS);
	}
	public boolean byTableColumn() {
		return (this.settingMode == ColumnSetMode.BY_TABLE_COLUMN);
	}
	public boolean byWcs() {
		return (this.settingMode == ColumnSetMode.BY_WCS);
	}
	public boolean bySaada() {
		return (this.settingMode == ColumnSetMode.BY_SAADA);
	}
	public boolean isNotSet() {
		return (this.settingMode == ColumnSetMode.NOT_SET);
	}
	public boolean isSet() {
		return !(this.settingMode == ColumnSetMode.NOT_SET);
	}
	public abstract void setUnit(String unit) ;
	/**
	 * Returns String value stored into the attribute Handler
	 * @return
	 */
	public abstract String getValue() ;

	/**
	 * Returns double value stored into the attribute Handler
	 * If the AH value has been set with a string, this method can return a {@link SaadaConstant#DOUBLE}
	 * @return
	 */
	public abstract double getNumValue();

	/**
	 * @return
	 */
	public abstract String getComment() ;

	/**
	 * @return
	 */
	public abstract String getAttNameOrg() ;

	/**
	 * @return
	 */
	public abstract String getAttNameAtt() ;

	/**
	 * @return
	 */
	public abstract String getUnit() ;
	/**
	 * @return
	 */
	public abstract CardDescriptor getAssociateAtttribute() ;

	/**
	 * @return
	 */
	public abstract String getUcd();

	/**
	 * @return
	 */
	public String getUserMappingMsg() {
		return this.userMappingMsg.toString();
	}
	
	/**
	 * Gets the detection msg.
	 *
	 * @return the detection msg
	 */
	public String getDetectionMsg() {
		return this.detectionMsg.toString();
	}
	
	/**
	 * Gets the conversion msg.
	 *
	 * @return the conversion msg
	 */
	public String getConversionMsg() {
		return this.conversionMsg.toString();
	}

	/**
	 * @return
	 */
	public ColumnSetMode getSettingMode() {
		return settingMode;
	}


	/**
	 * Set value to the instance value with on demand a message mentioning the conversion
	 * @param value   converted value
	 * @param unitOrg original unit of the value
	 * @param unitDest converted unit
	 * @param addMessage Ask for logging the action
	 * @return
	 */
	public ColumnSetter setConvertedValue(double value, String unitOrg, String unitDest, boolean addMessage) {
		if( addMessage ) this.completeConversionMsg("from " + unitOrg + " to "  + unitDest);
		this.setValue(value, unitDest);
		return this;
	}
	/**
	 * Set value to the instance value with on demand a message mentioning the conversion
	 * @param value   converted value
	 * @param unitOrg original unit of the value
	 * @param unitDest converted unit
	 * @param addMessage Ask for logging the action
	 * @return
	 */
	public ColumnSetter setConvertedValue(String value, String unitOrg, String unitDest, boolean addMessage) {
		if( addMessage ) this.completeConversionMsg("from " + unitOrg + " to "  + unitDest);
		this.setValue(value);
		return this;
	}
	
	/**
	 * Return a merge of the 3 messages (mapping, detection and conversion
	 * @return
	 */
	public String getFullMappingReport() {
		String retour = "USER:" + ((this.userMappingMsg.length() == 0)? "-": this.userMappingMsg.toString())
			   	     + " DETE:" + ((this.detectionMsg.length() == 0)? "-": this.detectionMsg.toString())
				     + " CONV:" + ((this.conversionMsg.length() == 0)? "-": this.conversionMsg.toString());
			return retour;		
	}
	/**
	 * @return
	 */
	public String toString(){
		String retour = this.getSettingMode() + " " + this.getFullMappingReport();
		if( this.storedValue != null )
			retour += " storedValue=" + this.storedValue;
		return retour;
	}

	/**
	 * @throws Exception
	 */
	public void calculateExpression() throws Exception{}
	
	/**
	 * In some cases the AHs are not enough to compute an expression (table columns e.g.)
	 * The values must  be taken from the {@link DataFile}
	 * By default the dataFile param is ignored this method is overloaded in subclasses needing 
	 * to access data provided the the dataFile 
	 * @param datafile
	 * @throws Exception
	 */
	public void  calculateExpression(DataFile datafile)throws Exception{
		this.calculateExpression();
	}

}
