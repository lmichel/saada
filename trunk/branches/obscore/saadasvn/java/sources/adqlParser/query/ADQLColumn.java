package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * It represents a reference to a column of one of the selected tables. The table reference can also be given.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLOperand
 */
public class ADQLColumn implements ADQLOperand {

	/**
	 * Name of the column.
	 */
	protected String column;
	
	/**
	 * Name or alias of the table which is supposed to contain this column.
	 */
	protected String tableRef;
	
	/**
	 * The label of this column.
	 */
	protected String alias = null;
	
	/**
	 * Indicates whether the value of this column must be "negativate" or not.
	 */
	protected boolean negative = false;
		
	
	/**
	 * Creates a column reference only with its name.
	 * 
	 * @param columnName	Name of the column.
	 * 
	 * @see ADQLColumn#ADQLColumn(String, String)
	 */
	public ADQLColumn(String columnName) throws ParseException {
		this(columnName, null);
	}
	
	/**
	 * Creates a column reference with its table reference.
	 * 
	 * @param columnName	Name of the column.
	 * @param prefix		The table name/alias which is supposed to contain this column.
	 */
	public ADQLColumn(String columnName, String prefix) throws ParseException {
		if (columnName == null || (columnName != null && columnName.trim().length() == 0))
			throw new ParseException("A column name can't be empty !");
		
		column = columnName.trim();
		tableRef = (prefix != null && prefix.trim().length() > 0)?prefix.trim():null;
	}

	/**
	 * Gets the name of this column.
	 * 
	 * @return Its name.
	 */
	public String getColumn() {
		return column;
	}

	/**
	 * Gets the table reference of this column.
	 * 
	 * @return Its table reference.
	 */
	public String getPrefix() {
		return tableRef;
	}
	
	/**
	 * Sets the table reference of this column.
	 * 
	 * @param p	The table name/alias which is supposed to contain this column.
	 */
	public void setPrefix(String p){
		tableRef = (p != null && p.trim().length() > 0)?p.trim():null;
	}

	public String getAlias() {
		return alias;
	}
	
	public void setAlias(String a){
		alias = (a != null && a.trim().length() > 0)?a.trim():null;
	}
	
	public void negativate(boolean neg) {
		negative = neg;
	}
	
	public Vector<ADQLColumn> getAllImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		vColumns.add(this);
		return vColumns;
	}

	public String toSQL() throws ParseException {
		return (negative?"-":"")+((tableRef==null)?"":(tableRef+"."))+column+((alias==null)?"":(" AS \""+alias+"\""));
	}
	
	public String toSQL(SQLTranslator translator) throws ParseException{
		return toSQL();
	}
	
	public String toString(){
		return (negative?"-":"")+((tableRef==null)?"\"":("\""+tableRef+"."))+column+"\""+((alias==null)?"":(" AS \""+alias+"\""));
	}

	public String getADQLName() {
		return "COLUMN";
	}

	public ADQLObject getCopy() throws ParseException {
		ADQLColumn copy = new ADQLColumn(column, tableRef);
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		return new Vector<ADQLObject>();
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