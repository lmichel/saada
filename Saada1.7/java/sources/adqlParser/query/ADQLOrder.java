package adqlParser.query;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Represents an item of the ORDER BY list: that's to say a column reference plus a sorting indication (ASC, DESC).
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ColumnReference
 */
public class ADQLOrder extends ColumnReference {
	
	/** Gives an indication about how to order the results of a query. (<i>true</i> for DESCending, <i>false</i> for ASCending) */
	protected boolean descSorting = false;
	
	
	/**
	 * Creates an order indication with the index of the selected column on which an ASCending ordering will be done.
	 * 
	 * @param colIndice			The index of a selected column (from 1).
	 * @throws ParseException	If the index is less or equal 0.
	 * 
	 * @see ADQLOrder#ADQLOrder(int, boolean)
	 */
	public ADQLOrder(int colIndice) throws ParseException {
		this(colIndice, false);
	}
	
	/**
	 * Creates an order indication with the index of the selected column on which the specified ordering will be done.
	 * 
	 * @param colIndice			The index of a selected column (from 1).
	 * @param desc				<i>true</i> means DESCending order, <i>false</i> means ASCending order. 
	 * @throws ParseException	If the index is less or equal 0.
	 */
	public ADQLOrder(int colIndice, boolean desc) throws ParseException {
		super(colIndice);
		descSorting = desc;
	}
	
	/**
	 * Creates an order indication with the name or the alias of the selected column on which an ASCending ordering will be done.
	 * 
	 * @param colName			The name or the alias of a selected column.
	 * @throws ParseException	If the given name is <i>null</i> or is an empty string.
	 * 
	 * @see ADQLOrder#ADQLOrder(String, boolean)
	 */
	public ADQLOrder(String colName) throws ParseException {
		this(colName, false);
	}
	
	/**
	 * Creates an order indication with the name of the alias of the selected column on which the specified ordering will be done.
	 * 
	 * @param colName			The name of the alias of a selected column.
	 * @param desc				<i>true</i> means DESCending order, <i>false</i> means ASCending order. 
	 * @throws ParseException	If the given name is <i>null</i> or is an empty string.
	 */
	public ADQLOrder(String colName, boolean desc) throws ParseException {
		super(colName);
		descSorting = desc;
	}

	/**
	 * Gives the way the results will be sorted.
	 * 
	 * @return <i>true</i> DESCending order, <i>false</i> ASCending order.
	 */
	public boolean isDescSorting() {
		return descSorting;
	}
	
	/**
	 * Updates the current order indication.
	 * 
	 * @param colIndice			The index of a selected column (from 1).
	 * @param desc				<i>true</i> means DESCending order, <i>false</i> means ASCending order.
	 * @throws ParseException	If the given index is less or equal 0.
	 */
	public void setOrder(int colIndice, boolean desc) throws ParseException {
		if (colIndice <= 0)
			throw new ParseException("Impossible to make a reference to the "+colIndice+"th column: a column index must be greater or equal 1 !");
			
		columnIndice = colIndice;
		columnName = null;
		descSorting = desc;
	}
	
	/**
	 * Updates the current order indication.
	 * 
	 * @param colName			The name or the alias of a selected column.
	 * @param desc				<i>true</i> means DESCending order, <i>false</i> means ASCending order.
	 * @throws ParseException	If the given name is <i>null</i> or is an empty string.
	 */
	public void setOrder(String colName, boolean desc) throws ParseException {
		if (colName ==  null || colName.trim().length() == 0)
			throw new ParseException("Impossible to make a reference: the given name is null or is an empty string !");
		
		columnIndice = -1;
		columnName = colName;
		descSorting = desc;
	}
	
	/**
	 * Gets the SQL expression of this ORDER BY item.
	 * 
	 * @return	Its SQL translation.
	 */
	public String toSQL(){
		return toString();
	}

	@Override
	public String toSQL(SQLTranslator altTranslator) throws ParseException {
		return toSQL();
	}
	
	/**
	 * Gets the ADQL expression of this ORDER BY item.
	 * 
	 * @return	Its ADQL expression.
	 */
	public String toString(){
		return ((columnName == null)?columnIndice:("\""+columnName+"\""))+(descSorting?" DESC":" ASC");
	}

	@Override
	public String getADQLName() {
		return "ORDER_ITEM";
	}

	@Override
	public ADQLObject getCopy() throws ParseException {
		return (columnName == null)?(new ADQLOrder(columnIndice, descSorting)):(new ADQLOrder(columnName, descSorting));
	}
	
	
}
