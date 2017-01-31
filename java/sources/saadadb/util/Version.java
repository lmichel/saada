package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "2.0";
	private static final String build = "30";
	public static final String date= "31/01/17 10:00"; 
	
	public static String getVersion() {
		return version + ".build" + build;
	}
	
}
