package saadadb.vocabulary.enums;

/**
 * Indicate the way thye data extension (or resource for VOtable) has been chosen.
 * @author michel
 * @version $Id$
 */
public enum ExtensionSetMode {
	/**
	 * Automatically detected 
	 */
	DETECTED,
	/**
	 * Set with thye dataloader param
	 */
	GIVEN,
	
	NOT_SET;
}
