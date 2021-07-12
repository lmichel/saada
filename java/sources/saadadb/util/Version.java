package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "2.0";
	private static final String build = "49";
	public static final String date= "12/07/2021 17:00"; 
	
	public static String getVersion() {
		return version + ".build" + build;
	}
	
}
