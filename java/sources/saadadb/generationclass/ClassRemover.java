package saadadb.generationclass;

import java.io.File;

import saadadb.database.Database;
import saadadb.util.Messenger;

public class ClassRemover {
	public static String separ = System.getProperty("file.separator");
	private static String mapping_classpath =  Database.getRoot_dir() + separ + "class_mapping";

	/** * @version $Id: ClassRemover.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * Remove both java and class files from the class_mapping directory
	 * @param class_name
	 */
	public static void remove(String class_name) {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Remove Java file");
		(new File(mapping_classpath + separ + class_name + ".java")).delete();
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Remove class file");
		(new File(mapping_classpath + separ + "generated" + separ +  Database.getDbname() + separ + class_name + ".java")).delete();
		Messenger.printMsg(Messenger.TRACE, "Java class <" + class_name + "> removed from the class_mapping directory. Don't forget to re-build generated.jar and to redeploy your web app");
	}

}
