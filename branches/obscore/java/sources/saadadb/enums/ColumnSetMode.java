package saadadb.enums;

/**
 * Indicate the way an attributeHandler value has been set;
 * @author michel
 * @version $Id$
 */
public enum ColumnSetMode {
	/**
	 * Set with a constant value 
	 */
	BY_VALUE,
	/**
	 * Set with a keyword value
	 */
	BY_KEYWORD,
	/**
	 * Set by scanning one column of a data table
	 */
	BY_TABLE_COLUMN,
	/**
	 * Inferred from the WCS matrix
	 */
	BY_WCS,
	/**
	 * Inferred from the pixel matrix
	 */
	BY_PIXELS,
	/**
	 * Computed internally
	 */
	BY_SAADA,
	/**
	 * Not set at all 
	 */
	NOT_SET;
}
