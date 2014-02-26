package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id: Version.java 873 2013-12-17 13:22:51Z laurent.mistahl $

 */
final public class Version {
	private static final String version = "1.7.0";
	private static final String build = "9";
	
	public static String getVersion() {
		return version + ".build" + build;
	}
	
}
