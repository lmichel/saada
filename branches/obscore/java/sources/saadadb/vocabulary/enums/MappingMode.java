/**
 * 
 */
package saadadb.vocabulary.enums;

/**
 * Indicate the way to interpret the value returned by a column mapping
 * @author michel
 * @version $Id$
 *
 */
public enum MappingMode {
	/**
	 * The mapping rule returns a constant value
	 */
	VALUE, 
	/**
	 * The mapping rule returns an attribute name from which the value must be taken
	 */
	KEYWORD,
	/**
	 * The mapping rule returns a SQL statement (not used yet)
	 */
	SQL,
	/**
	 * There is no mapping
	 */
	NOMAPPING,
	/**
	 * the mapping rule returns an expression which must be calculate
	 */
	EXPRESSION

}
