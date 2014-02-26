package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

public interface ADQLObject {
	
	/**
	 * Gets the name of the ADQL object (query, table, column, etc...).
	 * 
	 * @return	The name of this ADQL object.
	 */
	public String getADQLName();
	
	/**
	 * Gets the ADQL expression of this object.
	 * 
	 * @return	The corresponding ADQL expression.
	 */
	public String toString();
	
	/**
	 * Gets the SQL translation of this ADQL object.
	 * 
	 * @return					Its SQL translation.
	 * @throws ParseException	If there is any error during the translation.
	 */
	public String toSQL() throws ParseException;
	
	/**
	 * Gets the SQL translation of this ADQL object by using the given SQLTranslator.
	 * 
	 * @param altTranslator		The SQL translator to use.
	 * @return					Its SQL translation.
	 * @throws ParseException	If there is any error during the translation.
	 */
	public String toSQL(SQLTranslator altTranslator) throws ParseException;
	
	/**
	 * Gets a copy of this ADQL object.
	 * 
	 * @return					The copy of this ADQL object.
	 * @throws ParseException 	If there is any error during the copy.
	 */
	public ADQLObject getCopy() throws ParseException;
	
	/**
	 * Searches for all ADQL objects which satisfy the condition implemented in the given SearchHandler instance.
	 * 
	 * @param searchCondition	The search condition that wanted ADQL objects must satisfy. 
	 * @return					All matched ADQL objects or if there is no matched objects returns <i>null</i> or an empty vector.
	 */
	public Vector<ADQLObject> getAll(SearchHandler searchCondition);
	
	/**
	 * Searches for ADQL objects which satisfy the condition implemented in the given SearchHandler instance.<br />
	 * ONLY THE FIRST ADQL OBJECT WHICH HAS MATCHED IS RETURNED ! 
	 * 
	 * @param searchCondition	The search condition that the wanted ADQL object must satisfy.
	 * @return					The first ADQL object which has matched.
	 */
	public ADQLObject getFirst(SearchHandler searchCondition);
	
	/**
	 * Replaces the first ADQL object which has matched by the given ADQL object.
	 * 
	 * @param searchCondition		The search condition that the ADQL object to replace must satisfy.
	 * @param replacementObject		The ADQL object which must replace the first ADQL object which has matched.
	 * @return						The replaced ADQL object or <i>null</i> if no object has been found or replaced.
	 * @throws ParseException		If there is any error during the replacement.
	 */
	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException;
	
	/**
	 * Removes the first ADQL object which has matched.
	 * 
	 * @param searchCondition	The search condition that the ADQL object to remove must satisfy.
	 * @return					The removed ADQL object or <i>null</i> if no object has been found or removed.
	 * @throws ParseException	If there is any error during the deletion.
	 */
	public ADQLObject remove(SearchHandler searchCondition) throws ParseException;
	
}
