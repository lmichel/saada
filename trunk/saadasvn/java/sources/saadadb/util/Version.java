package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "1.7.0.build5";
	private static final String pack = "1";
	
	public static String getVersion() {
		return version + "-p" + pack;
	}
	
}
