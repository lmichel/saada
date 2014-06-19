/**
 * 
 */
package saadadb.products;

import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 */
public class PriorityMessage {
	public final static String FIRST = "Priority FIRST: Apply first the mapping and then infer the searched values";
	public final static String LAST  = "Priority LAST: Infer values first and then apply the mapping";
	public final static String ONLY  = "Priority ONLY: AJust apply the mapping";
	
	private final static void print (String radix, boolean trace, String level){
		if( trace ){
			Messenger.printMsg(Messenger.TRACE, radix + ": " + level);
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, radix + ": " + level);
		}		
	}
	/**
	 * @param radix
	 * @param trace print in TRACE mode if true
	 */
	public final static void first(String radix, boolean trace){
		print(radix, trace, FIRST);
	}
	public final static void first(String radix){
		print(radix, false, FIRST);
	}
	/**
	 * @param radix
	 * @param trace print in TRACE mode if true
	 */
	public final static void last(String radix, boolean trace){
		print(radix, trace, LAST);
	}
	public final static void last(String radix){
		print(radix, false, LAST);
	}
	/**
	 * @param radix
	 * @param trace print in TRACE mode if true
	 */
	public final static void only(String radix, boolean trace){
		print(radix, trace, ONLY);
	}
	public final static void only(String radix){
		print(radix, false, ONLY);
	}

}
