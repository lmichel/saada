package saadadb.products;

import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * This classs contains both the mapping of a column and the way it has be done.
 * @author michel
 * @version $Id$
 */
public final class ColumnSetter implements Cloneable {
	/**
	 * Description of the column to be set 
	 */
	private AttributeHandler attributeHandler;
	/**
	 * Mode used to get the value
	 */
	protected ColumnSetMode setMode = ColumnSetMode.NOT_SET;
	/**
	 * Log of the mapping process
	 */
	public StringBuffer message = new StringBuffer();
	/**
	 * Value affected to the column but whicg cab be saved as a string within the  AttributeHandler
	 */
	public Object storedValue;
	
	/**
	 * Constructor: instance must always have a not null attribute handler
	 * Make the setter as NOT_SET
	 * @param attributeHandler
	 * @throws FatalException 
	 */
	public ColumnSetter(AttributeHandler attributeHandler) throws FatalException {
		if( attributeHandler == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null attributeHandler");
		}
		this.attributeHandler = attributeHandler;
		this.setNotSet();
	}
	/**
	 * @param attributeHandler
	 * @param setMode
	 * @throws FatalException 
	 */
	public ColumnSetter(AttributeHandler attributeHandler, ColumnSetMode setMode) throws FatalException {
		if( attributeHandler == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null attributeHandler");
		}
		this.attributeHandler = attributeHandler;
		this.setMode = setMode;
		this.setInitMessage();
	}	
	
	/**
	 * Use of boolean flags instead of free text in order to make sure that messages are always the same.
	 * @param attributeHandler
	 * @param setMode
	 * @param fromMapping  column set by a mapping parameter,  byUcd makes no sens when true
	 * @param byUcd take by name if false
	 * @throws FatalException 
	 */
	public ColumnSetter(AttributeHandler attributeHandler, ColumnSetMode setMode, boolean fromMapping, boolean byUcd) throws FatalException {
		this(attributeHandler,setMode );
		this.setMappingMessage(fromMapping);
	}
	/**
	 * Build an attribute handler carrying the value and put the apropriate messages
	 * @param value   constant value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSetter(String value, boolean fromMapping) throws FatalException {
		this.attributeHandler = new AttributeHandler();
		this.setMode = ColumnSetMode.BY_VALUE ;
		this.attributeHandler.setValue(value);
		this.attributeHandler.setNameattr(ColumnMapping.NUMERIC);
		this.attributeHandler.setNameorg(ColumnMapping.NUMERIC);
		this.setInitMessage();
		this.setMappingMessage(fromMapping);
	}
	/**
	 * Build an attribute handler carrying the value and put the apropriate messages
	 * @param value   constant value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSetter(String value, boolean fromMapping, String message) throws FatalException {
		this(value, fromMapping);
		this.completeMessage(message);
	}
	
	/**
	 * Basic constructor
	 */
	public ColumnSetter() {
		this.attributeHandler = new AttributeHandler();
		this.setNotSet();
	}
	/**
	 * Basic constructor
	 */
	public ColumnSetter(String message) {
		this.attributeHandler = new AttributeHandler();
		this.setNotSet();
		this.completeMessage(message);
	}

	/**
	 * Log a standrd message indicating wether the vlue has been set by mapping or bu auto detection
	 * @param fromMapping
	 */
	private void setMappingMessage( boolean fromMapping) {
		if( fromMapping ) {
			this.completeMessage("user-mapping");
		} else {
			this.completeMessage("auto-detection");
		}
	}
	/**
	 * Starts the log message according the mode used to get the KW value
	 */
	private void setInitMessage() {
		switch(this.setMode){
		case BY_KEYWORD:
			this.completeMessage("keyword <" + this.attributeHandler.getNameorg()+ ">");
			break;
		case BY_PIXELS:			
			this.completeMessage("pixel value <" +this. attributeHandler.getValue()+ ">");
			break;
		case BY_TABLE_COLUMN: 
			this.completeMessage("content of the column <" + this.attributeHandler.getNameorg()+ ">");
			break;
		case BY_VALUE:
			this.completeMessage("value <" +this. attributeHandler.getValue()+ ">");
			break;
		case BY_WCS:
			this.completeMessage("WCS value <" + this.attributeHandler.getValue()+ ">");
			break;
		case BY_SAADA:
			this.completeMessage("computed value <" + this.attributeHandler.getValue()+ ">");
			break;
		default:
			this.completeMessage("Nothing found");
			break;
		}

	}
	/**
	 * add "message" to the mapping log
	 * @param message
	 */
	public void completeMessage(String message){
		if( this.message.length() > 0 )
			this.message.append(" ");
		this.message.append(message);
	}
	/**
	 * @param value
	 */
	public void setByValue(String value, boolean fromMapping){
		this.setMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(value);
		this.completeMessage("value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/**
	 * @param keyword
	 */
	public void setByKeyword(boolean fromMapping){
		this.setMode = ColumnSetMode.BY_KEYWORD;
		this.completeMessage("keyword <" + attributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/**
	 * @param value
	 */
	public void setByWCS(String value, boolean fromMapping){
		this.setMode = ColumnSetMode.BY_WCS;
		this.attributeHandler.setValue(value);
		this.completeMessage("WCS value <" + attributeHandler.getValue()+ attributeHandler.getUnit() +">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/**
	 * @param value
	 */
	public void setByPixels(String value, boolean fromMapping){
		this.setMode = ColumnSetMode.BY_PIXELS;
		this.attributeHandler.setValue(value);
		this.completeMessage("pixel value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/**
	 * @param value
	 */
	public void setByTabeColumn(String value, boolean fromMapping){
		this.setMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.attributeHandler.setValue(value);
		this.completeMessage("content of the column <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/**
	 * 
	 */
	public void setBySaada(){
		this.setMode = ColumnSetMode.BY_SAADA;
		this.attributeHandler.setValue(null);
	}
	/**
	 * Stores value/unit within the attribute handler without changing the mode.
	 * storedValue is also set with value
	 * @param value
	 * @param unit
	 */
	public void setValue(double value, String unit){
		this.attributeHandler.setValue(String.valueOf(value));
		this.attributeHandler.setUnit(unit);
		this.storedValue = value;
	}
	/**
	 * 
	 */
	public void setNotSet(){
		this.setMode = ColumnSetMode.NOT_SET;
		this.attributeHandler.setValue(null);
	}
	/**
	 * 
	 */
	public void setValue(String value){
		this.setMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(value);
	}
	/**
	 * Set the value as a string  into the AttributeHandler and keep the double value as the storedValue
	 * @param value
	 */
	public void setValue(double value){
		this.setMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(String.valueOf(value));
		this.storedValue = value;
	}
	
	/*
	 * getters for the mode used to set the attribute
	 */
	public boolean byValue() {
		return (this.setMode == ColumnSetMode.BY_VALUE);
	}
	public boolean byKeyword() {
		return (this.setMode == ColumnSetMode.BY_KEYWORD);
	}
	public boolean byPiwels() {
		return (this.setMode == ColumnSetMode.BY_PIXELS);
	}
	public boolean byTableColumn() {
		return (this.setMode == ColumnSetMode.BY_TABLE_COLUMN);
	}
	public boolean byWcs() {
		return (this.setMode == ColumnSetMode.BY_WCS);
	}
	public boolean bySaada() {
		return (this.setMode == ColumnSetMode.BY_SAADA);
	}
	public boolean notSet() {
		return (this.setMode == ColumnSetMode.NOT_SET);
	}
	public void setUnit(String unit) {
		this.attributeHandler.setUnit(unit);
	}
	/**
	 * @return
	 */
	public String getValue() {
		return this.attributeHandler.getValue();
	}
	/**
	 * @return
	 */
	public String getComment() {
		return this.attributeHandler.getComment();
	}
	/**
	 * @return
	 */
	public String getAttNameOrg() {
		return this.attributeHandler.getNameorg();
	}
	/**
	 * @return
	 */
	public String getAttNameAtt() {
		return this.attributeHandler.getNameattr();
	}
	/**
	 * @return
	 */
	public String getUnit() {
		return this.attributeHandler.getUnit();
	}
	/**
	 * @return
	 */
	public String getMessage() {
		return this.message.toString();
	}
	
	/**
	 * @return
	 */
	public String getMode() {
		return (setMode == ColumnSetMode.BY_KEYWORD)? "BY_KEYWORD" :
			   (setMode == ColumnSetMode.BY_PIXELS)? "BY_PIXELS" :
			   (setMode == ColumnSetMode.BY_TABLE_COLUMN)? "BY_PIXELS" :
			   (setMode == ColumnSetMode.BY_VALUE)? "BY_VALUE" :
			   (setMode == ColumnSetMode.BY_SAADA)? "BY_SAADA" :
			   (setMode == ColumnSetMode.BY_WCS)? "BY_WCS" : "NOT_SET";
	}
	
	/**
	 * Returns a clone of the current instance but with value/unit changed
	 * and a message mentioning the conversion 
	 * @param value
	 * @param unit
	 * @return
	 */
	public ColumnSetter getConverted(double value, String unit) {
		ColumnSetter retour;
		try {
			retour = (ColumnSetter) this.clone();
			retour.completeMessage(" Converted to " +  value + unit);
			retour.setValue(value, unit);
			return retour;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour =  this.attributeHandler + " "
		+ this.getMode() + " "
		+ this.message;
		if( this.storedValue != null )
			retour += " storedValue=" + this.storedValue;
		return retour;
	}
}
