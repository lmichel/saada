package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Represents a reference to a selected column either by an index or by a name/alias.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public class ColumnReference implements ADQLObject {
	
	/** Index of a selected column. */
	protected int columnIndice;
	
	/** Name or alias of a selected column. */
	protected String columnName;
	
	
	/**
	 * Creates a column reference with an index of a selected column.
	 * 
	 * @param indice			Index of a selected column (from 1).
	 * @throws ParseException	If the given index is less or equal 0.
	 */
	public ColumnReference(int indice) throws ParseException {
		if (indice <= 0)
			throw new ParseException("Impossible to make a reference to the "+indice+"th column: a column index must be greater or equal 1 !");
		
		columnIndice = indice;
		columnName = null;
	}
	
	/**
	 * Creates a column reference with a name/alias of a selected column.
	 * 
	 * @param colName			A column name/alias.
	 * @throws ParseException 	If the given name is <i>null</i> or is an empty string.
	 */
	public ColumnReference(String colName) throws ParseException {
		if (colName == null || colName.trim().length() == 0)
			throw new ParseException("Impossible to make a reference: the given name is null or is an empty string !");
		
		columnName =  colName;
		columnIndice = -1;
	}

	/**
	 * Gets the index of the referenced column.
	 * 
	 * @return The index of the referenced column or <i>-1</i> if this column reference has been made with a column name/alias.
	 */
	public int getColumnIndice() {
		return columnIndice;
	}

	/**
	 * Gets the name/alias of the referenced column.
	 * 
	 * @return The referenced column's name/alias or <i>null</i> if this column reference has been made with a column index.
	 */
	public String getColumnName() {
		return columnName;
	}
	
	/**
	 * Gets the SQL expression of this column reference.
	 * 
	 * @return	Its SQL translation.
	 */
	public String toSQL(){
		return toString();
	}

	public String toSQL(SQLTranslator altTranslator) throws ParseException {
		return toSQL();
	}
	
	/**
	 * Gets the ADQL expression of this column reference.
	 * 
	 * @return 	Its ADQL expression.
	 */
	public String toString(){
		return (columnName == null)?(""+columnIndice):("\""+columnName+"\"");
	}

	public String getADQLName() {
		return "COLUMN_REFERENCE";
	}

	public ADQLObject getCopy() throws ParseException {
		return (columnName == null)?(new ColumnReference(columnIndice)):(new ColumnReference(columnName));
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
