package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "beta2";
	private static final String build = "x";
	
	public static String getVersion() {
		return version + ".build" + build;
	}
	
}
