package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "1.9";
	private static final String build = "6";
	
	public static String getVersion() {
		return version + ".build" + build;
	}	
}