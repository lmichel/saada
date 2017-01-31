package saadadb.util;

/**
 * @author laurentmichel
 * * @version $Id$

 */
final public class Version {
	private static final String version = "1.9";
<<<<<<< HEAD
	private static final String build = "6";
=======
	private static final String build = "3";
>>>>>>> branch 'master' of https://github.com/lmichel/saada.git
	
	public static String getVersion() {
		return version + ".build" + build;
	}	
}