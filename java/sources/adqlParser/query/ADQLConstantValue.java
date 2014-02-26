package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * It represents a string expression (i.e. 'galaxy') or a numeric value (integer (i.e. 12) or double (i.e. 3.14)).<br />
 * If not specified, the default type is {@link ADQLType#STRING}. In other cases you must give explicitly the type thanks to {@link ADQLType}.<br />
 * The NULL value of SQL and ADQL can be created merely by calling the empty constructor.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLOperand
 * @see ADQLType
 */
public class ADQLConstantValue implements ADQLOperand {

	/**
	 * The value of the constant.<br />
	 * <i>Note: whatever the type the value is put in this String variable.</i>
	 */
	protected String value;
	
	/**
	 * The type of the constant value (String, Integer, Double, ...).
	 * @see ADQLType
	 */
	protected ADQLType type;
	
	/**
	 * The label of this operand.
	 */
	protected String alias = null;
	
	/**
	 * Indicates whether this value must be "negativate" or not.
	 */
	protected boolean negative = false;
	
	
	/**
	 * Creates the NULL value of SQL and ADQL.
	 */
	public ADQLConstantValue(){
		this(null, null);
	}
	
	/**
	 * Creates a constant value of type {@link ADQLType#STRING}.<br />
	 * <i><u>note:</u> if </i>val<i> is </i>null<i> this object will represent the NULL value of SQL and ADQL.</i>
	 * 
	 * @param val The string.
	 */
	public ADQLConstantValue(String val){
		this(val, ADQLType.STRING);
	}
	
	/**
	 * Creates a constant value of the given type (see {@link ADQLType}).<br />
	 * <i><u>note:</u> if </i>val<i> or </i>type<i> is </i>null<i> this object will represent the NULL value of SQL and ADQL.</i>
	 * 
	 * @param val	The constant.
	 * @param type	Its type.
	 */
	public ADQLConstantValue(String val, ADQLType type){
		value = val;
		this.type = (type==null)?ADQLType.STRING:type;
	}
	
	/**
	 * Gets the value of this constant.
	 * 
	 * @return Its value.
	 */
	public String getValue(){
		return value;
	}
	
	/**
	 * Gets the type of the constant.
	 * 
	 * @return	Its type.
	 */
	public ADQLType getType(){
		return type;
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String label) {
		alias = (label!=null && label.trim().length()>0)?label.trim():null;
	}
	
	public void negativate(boolean neg) throws ParseException {
		if (neg){
			if (type != ADQLType.STRING)
				negative = neg;
			else
				throw new ParseException("Impossible to negativate a string value(\""+value+"\") !");
		}
	}
	
	public Vector<ADQLColumn> getAllImpliedColumns(){
		return null;
	}

	public String toSQL() throws ParseException {
		return toString();
	}

	public String toSQL(SQLTranslator translator) throws ParseException {
		return toSQL();
	}
	
	public String toString(){
		if (value == null || type == null)
			return "NULL";
		else
			return (negative?"-":"")+((type==ADQLType.STRING)?("'"+value+"'"):value)+((alias==null)?"":(" AS \""+alias+"\""));
	}

	public String getADQLName() {
		return type.toString();
	}

	public ADQLObject getCopy() throws ParseException {
		ADQLConstantValue copy = new ADQLConstantValue(value, ADQLType.valueOf(type.toString()));
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		return null;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		return null;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		return null;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		return null;
	}

}