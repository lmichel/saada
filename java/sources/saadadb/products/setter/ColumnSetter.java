package saadadb.products.setter;

import java.awt.event.InvocationEvent;

import hecds.wcs.descriptors.CardDescriptor;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.DataFile;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.ColumnSetMode;

/**
 * This classs contains both the mapping of a column and the way it has be done.
 * @author michel
 * @version $Id$
 */
public abstract class ColumnSetter implements Cloneable {
	/**
	 * Mode used to get the value
	 */
	protected ColumnSetMode settingMode = ColumnSetMode.NOT_SET;
	/**
	 * Log of the mapping process
	 */
	public StringBuffer message = new StringBuffer();
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
	 * Use of boolean flags instead of free text in order to make sure that messages are always the same.
	 * @param attributeHandler
	 * @param setMode
	 * @param fromMapping  column set by a mapping parameter,  byUcd makes no sens when true
	 * @param byUcd take by name if false
	 * @throws FatalException 
	 */
	public ColumnSetter(ColumnSetMode setMode, boolean fromMapping, boolean byUcd) throws FatalException {
		this(setMode );
		this.setMappingMessage(fromMapping);
	}

	/**
	 * Basic constructor
	 */
	public ColumnSetter(String message) {
		this.setNotSet();
		this.completeMessage(message);
	}

	/**
	 * Log a standrd message indicating wether the vlue has been set by mapping or bu auto detection
	 * @param fromMapping
	 */
	protected void setMappingMessage( boolean fromMapping) {
		if( fromMapping ) {
			this.completeMessage("user-mapping");
		} else {
			this.completeMessage("auto-detection");
		}
	}
	/**
	 * Starts the log message according the mode used to get the KW value
	 */
	protected  abstract void setInitMessage();
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
	 * 
	 */
	public abstract void setNotSet();
	/**
	 * 
	 */
	public abstract void setNotSet(String message);
	
	/**
	 * @param message
	 * @param e
	 */
	public abstract void setNotSet(String message, Exception e);

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
	public String getMessage() {
		return this.message.toString();
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
		if( addMessage ) this.replaceMessageTail("Converted", "Converted from " + unitOrg + " to "  + unitDest);
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
		if( addMessage ) this.replaceMessageTail("Converted", "Converted from " + unitOrg + " to "  + unitDest);
		this.setValue(value);
		//this.setUnit(unitDest);
		return this;
	}

	/**
	 * Replace the tail of the instance message, starting with marker with message
	 * @param marker
	 * @param message
	 */
	private void replaceMessageTail(String marker, String message){
		int pos = this.message.indexOf(marker);
		if( pos == -1){
			this.completeMessage(message);
		} else {
			this.message.replace(pos,  this.message.length(), message);
		}
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
