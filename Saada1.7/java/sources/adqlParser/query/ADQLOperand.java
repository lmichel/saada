package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * This class is the generic definition of an operand which can be used as item of the clause SELECT or as "parameter" of a constraint.<br />
 * Since this operand may be a SELECT's item it may have an alias (see the functions {@link ADQLOperand#getAlias()} and {@link ADQLOperand#setAlias(String)}).
 * Indeed it may correspond to a numeric value and so it is possible to put a minus symbol in front of, to make negative the numeric value (example: -1.2, -_s_ra, ...).
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public interface ADQLOperand extends ADQLObject {
	
	/**
	 * Gets the label of this operand.
	 * 
	 * @return Its label.
	 */
	public String getAlias();
	
	/**
	 * Sets the label of this operand.
	 * 
	 * @param label	The alias to put to this operand.
	 */
	public void setAlias(String label);

	/**
	 * Makes negative this operand.
	 * 
	 * @param negative			<i>true</i> to add a minus symbol (-) in front of this operand, <i>false</i> else.
	 * @throws ParseException	If its impossible to "negativate" this operand.
	 */
	public void negativate(boolean negative) throws ParseException;
	
	/**
	 * Gets all column references contained into this operand.
	 * 
	 * @return	All column references or <i>null</i> or an empty vector if there is no column reference.
	 */
	public Vector<ADQLColumn> getAllImpliedColumns();
	
	/**
	 * Translates this operand in SQL.
	 * 
	 * @return					Its SQL expression.
	 * @throws ParseException	If any error during the translation of this operand.
	 * @see adqlParser.query.ADQLObject#toSQL()
	 */
	public String toSQL() throws ParseException;
	
	/**
	 * Translates this operand in SQL.
	 * @param translator		A tool to translate a this operand in the good SQL dialect.
	 * 
	 * @return					Its SQL expression.
	 * @throws ParseException	If any error during the translation of this operand.
	 * @see ADQLObject#toSQL(SQLTranslator)
	 */
	public String toSQL(SQLTranslator translator) throws ParseException;
	
	/**
	 * Gets the corresponding ADQL expression.
	 * 
	 * @return Its ADQL expression.
	 * @see ADQLObject#toString()
	 */
	public String toString();
}
