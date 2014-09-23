/**
 * 
 */
package saadadb.vocabulary.enums;

/**
 * Priority level of yhe mapping rules
 * @author michel
 * @version $Id$
 *
 */
public enum PriorityMode {
	/**
	 * use first the mapping rule and the automated mode in case of failure
	 */
	FIRST ,
	/**
	 * use first  the automated mode and then the mapping rule  in case of failure
	 */
	LAST,
	/**
	 * use first the mapping rule and the automated mode in case of failure
	 */
	ONLY 
}
