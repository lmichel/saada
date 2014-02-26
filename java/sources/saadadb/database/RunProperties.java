/**
 * 
 */
package saadadb.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class RunProperties {
	public static final String JVM_TARGET;
	public static final String JVM_XMS;
	public static final String JVM_XMX;

	static {
		String ltarget = "1.6";
		String lxms= "64m";
		String lxmx="1024m";	
		try {
			InputStream is = RunProperties.class.getClassLoader().getResourceAsStream("javarun.properties");
			if( is != null ) {
				is = new FileInputStream(Database.getRoot_dir() + "/ ");
				Properties prop = new Properties();	
				prop.load(is);
				ltarget = (prop.getProperty("javac.target") == null)?"1.6" : prop.getProperty("javac.target");
				lxms = (prop.getProperty("java.xms") == null)?"64m" : prop.getProperty("javac.target");
				lxmx = (prop.getProperty("java.xmx") == null)?"1024m" : prop.getProperty("javac.target");
			}

		} catch (IOException e) {
			Messenger.printStackTrace(e);
		}
		JVM_TARGET= ltarget;
		JVM_XMS = lxms;
		JVM_XMX = lxmx;
		Messenger.printMsg(Messenger.TRACE, "JVM options: target=" + JVM_TARGET + " xms=" + JVM_XMS + " xmx=" + JVM_XMX);
	}

}
