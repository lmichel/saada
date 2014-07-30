package saadadb.products.setter;

import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.enums.ColumnSetMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.SaadaConstant;

/**
 * This classs contains both the mapping of a column and the way it has be done.
 * @author michel
 * @version $Id$
 */
public final class ColumnSingleSetter extends ColumnSetter implements Cloneable {
	/**
	 * Description of the column to be set 
	 */
	private AttributeHandler attributeHandler;


	/**
	 * Constructor: instance must always have a not null attribute handler
	 * Make the setter as NOT_SET
	 * @param attributeHandler
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(AttributeHandler attributeHandler) throws FatalException {
		super();
		if( attributeHandler == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null attributeHandler");
		}
		this.attributeHandler = attributeHandler;
	}
	/**
	 * @param attributeHandler
	 * @param setMode
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(AttributeHandler attributeHandler, ColumnSetMode setMode) throws FatalException {
		super(setMode);
		if( attributeHandler == null ){
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column setter cannot be set with a null attributeHandler");
		}
		this.attributeHandler = attributeHandler;
	}	
	
	/**
	 * Use of boolean flags instead of free text in order to make sure that messages are always the same.
	 * @param attributeHandler
	 * @param setMode
	 * @param fromMapping  column set by a mapping parameter,  byUcd makes no sens when true
	 * @param byUcd take by name if false
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(AttributeHandler attributeHandler, ColumnSetMode setMode, boolean fromMapping, boolean byUcd) throws FatalException {
		this(attributeHandler,setMode );
		this.setInitMessage();
		this.setMappingMessage(fromMapping);
	}
	/**
	 * Build an attribute handler carrying the value and put the appropriate message
	 * Set on BY_VALUE mode
	 * @param value   constant value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(String value, boolean fromMapping) throws FatalException {
		this.attributeHandler = new AttributeHandler();
		this.settingMode = ColumnSetMode.BY_VALUE ;
		this.attributeHandler.setValue(value);
		this.attributeHandler.setNameattr(ColumnMapping.NUMERIC);
		this.attributeHandler.setNameorg(ColumnMapping.NUMERIC);
		this.setInitMessage();
		this.setMappingMessage(fromMapping);
	}
	/**
	 * Build an attribute handler carrying the value
	 * Set on BY_VALUE mode
	 * @param value numeric value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(double value, boolean fromMapping) throws FatalException {
		this.attributeHandler = new AttributeHandler();
		this.settingMode = ColumnSetMode.BY_VALUE ;
		this.attributeHandler.setValue(value);
		this.attributeHandler.setNameattr(ColumnMapping.NUMERIC);
		this.attributeHandler.setNameorg(ColumnMapping.NUMERIC);
		this.setInitMessage();
		this.setMappingMessage(fromMapping);
	}
	/**
	 * Build an attribute handler carrying the value and put the appropriate messages
	 * Set on BY_VALUE mode
	 * @param value   constant value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(String value, boolean fromMapping, String message) throws FatalException {
		this(value, fromMapping);
		this.completeMessage(message);
	}
	/**
	 * Build an attribute handler carrying the value and put the appropriate messages
	 * Set on BY_VALUE mode
	 * @param value   numeric value for the column
	 * @param fromMapping flag for messaging
	 * @throws FatalException 
	 */
	public ColumnSingleSetter(double value, boolean fromMapping, String message) throws FatalException {
		this(value, fromMapping);
		this.completeMessage(message);
	}
	
	/**
	 * Basic constructor
	 */
	public ColumnSingleSetter() {
		super();
		this.attributeHandler = new AttributeHandler();
		this.attributeHandler.setValue(null);
	}
	/**
	 * Basic constructor
	 */
	public ColumnSingleSetter(String message) {
		super(message);
		this.attributeHandler = new AttributeHandler();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setInitMessage()
	 */
	protected void setInitMessage() {
		if( this.attributeHandler == null ) {
			return;
		}
		switch(this.settingMode){
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
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByValue(java.lang.String, boolean)
	 */
	@Override
	public void setByValue(String value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(value);
		this.completeMessage("value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByValue(double, boolean)
	 */
	@Override
	public void setByValue(double value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(value);
		this.completeMessage("value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByKeyword(java.lang.String, boolean)
	 */
	@Override
	public void setByKeyword(String value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.attributeHandler.setValue(value);
		this.completeMessage("keyword <" + attributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByKeyword(double, boolean)
	 */
	@Override
	public void setByKeyword(double value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_KEYWORD;
		this.attributeHandler.setValue(value);
		this.completeMessage("keyword <" + attributeHandler.getNameorg()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByWCS(java.lang.String, boolean)
	 */
	@Override
	public void setByWCS(String value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_WCS;
		this.attributeHandler.setValue(value);
		this.completeMessage("WCS value <" + attributeHandler.getValue()+ attributeHandler.getUnit() +">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByWCS(double, boolean)
	 */
	@Override
	public void setByWCS(double value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_WCS;
		this.attributeHandler.setValue(value);
		this.completeMessage("WCS value <" + attributeHandler.getValue()+ attributeHandler.getUnit() +">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByPixels(java.lang.String, boolean)
	 */
	@Override
	public void setByPixels(String value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.attributeHandler.setValue(value);
		this.completeMessage("pixel value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByPixels(double, boolean)
	 */
	@Override
	public void setByPixels(double value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_PIXELS;
		this.attributeHandler.setValue(value);
		this.completeMessage("pixel value <" + attributeHandler.getValue()+ attributeHandler.getUnit()+">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByTableColumn(java.lang.String, boolean)
	 */
	@Override
	public void setByTableColumn(String value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.attributeHandler.setValue(value);
		this.completeMessage("content of the column <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByTabeColumn(double, boolean)
	 */
	@Override
	public void setByTabeColumn(double value, boolean fromMapping){
		this.settingMode = ColumnSetMode.BY_TABLE_COLUMN;
		this.attributeHandler.setValue(value);
		this.completeMessage("content of the column <" + attributeHandler.getValue()+ attributeHandler.getUnit()+ ">");
		if( fromMapping  ) {
			this.completeMessage("user mapping");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setBySaada()
	 */
	@Override
	public void setBySaada(){
		this.settingMode = ColumnSetMode.BY_SAADA;
		this.attributeHandler.setValue(null);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setByKeyword(boolean)
	 */
	@Override
	public void setByKeyword(boolean fromMapping) {
        this.settingMode = ColumnSetMode.BY_KEYWORD;
        this.completeMessage("keyword <" + attributeHandler.getNameorg()+ ">");
        if( fromMapping  ) {
                this.completeMessage("user mapping");
        }
    }
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setValue(double, java.lang.String)
	 */
	@Override
	public void setValue(double value, String unit){
		this.attributeHandler.setValue(value);
		this.attributeHandler.setUnit(unit);
		this.storedValue = value;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setNotSet()
	 */
	@Override
	public void setNotSet(){
		this.settingMode = ColumnSetMode.NOT_SET;
		this.attributeHandler.setValue(null);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value){
		this.attributeHandler.setValue(value);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setValue(double)
	 */
	@Override
	public void setValue(double value){
		this.settingMode = ColumnSetMode.BY_VALUE;
		this.attributeHandler.setValue(value);
		this.storedValue = value;
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#setUnit(java.lang.String)
	 */
	@Override
	public void setUnit(String unit) {
		this.attributeHandler.setUnit(unit);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getValue()
	 */
	@Override
	public String getValue() {
		return this.attributeHandler.getValue();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getNumValue()
	 */
	@Override
	public double getNumValue() {
		return this.attributeHandler.getNumValue();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getComment()
	 */
	@Override
	public String getComment() {
		return this.attributeHandler.getComment();
	}
	/**
	 * @return
	 */
	@Override
	public String getAttNameOrg() {
		return this.attributeHandler.getNameorg();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getAttNameAtt()
	 */
	@Override
	public String getAttNameAtt() {
		return this.attributeHandler.getNameattr();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getUnit()
	 */
	@Override
	public String getUnit() {
		return this.attributeHandler.getUnit();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.setter.ColumnSetter#getUcd()
	 */
	@Override
	public AttributeHandler getAssociateAtttribute() {
		return this.attributeHandler.getAssociateAtttribute();
	}
	@Override
	public String getUcd() {
		return this.attributeHandler.getUcd();
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String retour =  this.attributeHandler + " "
		+ this.getSettingMode() + " "
		+ this.message;
		if( this.storedValue != null )
			retour += " storedValue=" + this.storedValue;
		return retour;
	}
}
