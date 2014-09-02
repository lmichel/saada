package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "1.8.0";
	private static final String build = "5";
	
	public static String getVersion() {
		return version + ".build" + build;
	}
	
}
