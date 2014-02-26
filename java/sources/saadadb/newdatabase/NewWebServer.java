package saadadb.newdatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id: NewWebServer.java 865 2013-12-04 15:25:26Z laurent.mistahl $
 * 04/2012: method buildDBNameFile isolated to be used from the admin tool

 */
public class NewWebServer extends NewSaadaDB {

	/**
	 * @param saada_home
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws FatalException 
	 */
	NewWebServer(String saada_home, String admin_password) throws  FatalException {
		super(saada_home, admin_password);
	}

	/**
	 * Build the web application within SAADA_DB_HOME/web from the template in SAADA_HOME
	 * @throws IOException 
	 * @throws FatalException 
	 */
	protected void BuildWebApp() throws Exception {
		String rootDir = this.connector.getRoot_dir();
		String nameDB  = this.connector.getDbname();
		/*
		 * generate files web.xml
		 */
		String[] old2 = new String[1];
		old2[0] = "SAADA_DB_NAME";
		String[] remplace2 = new String[1];
		remplace2[0] = nameDB;
		this.GenererFileConfXML(SAADA_HOME + separ + "dbtemplate"
				+ separ + "web" + separ + "WEB-INF" + separ, "web.xml",
				old2, remplace2, rootDir  + separ + "web"
				+ separ + "WEB-INF" + separ);
		buildDBNameFile(rootDir, nameDB, this.connector.getUrl_root());
		
		Project p = new Project();
		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		org.apache.tools.ant.DefaultLogger log = new org.apache.tools.ant.DefaultLogger();
		log.setErrorPrintStream(System.err);
		log.setOutputPrintStream(System.out);
		log.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(log);
		helper.parse(p, new File(rootDir + separ + "bin" + separ + "build.xml"));
		Messenger.printMsg(Messenger.TRACE, "Set X mode for executables");
		p.executeTarget("xmode.set");
		Messenger.printMsg(Messenger.TRACE, "New web application is packed");
	}
	
	/**
	 * Build a dbname.txt file in the root of the web directory
	 * This method doen use any Saada resource because it can be called from any context
	 * @param rootDir : DB root directory
	 * @param nameDB : SaadaDB name
	 * @param urlRoot : URL of the SaadaDB webapp
	 * @throws Exception
	 */
	public static void buildDBNameFile(String rootDir, String nameDB, String urlRoot) throws Exception {
		FileWriter fw = new FileWriter(rootDir  + separ + "web" + separ + "dbname.txt");
		fw.write("######################################################################\n");
		fw.write("# This file allows some WEB application set up to be achieved by hand\n");
		fw.write("# Require server restart\n");
		fw.write("#\n\n");
		fw.write("#########################\n");
		fw.write("# Can be used to make the web aplication working on another SaadaDB.\n");
		fw.write("# In this case, make sure the generated jar file is in WEB-INF/lib\n");
		fw.write("saadadbname=" + nameDB + "\n");
		fw.write("#########################\n");
		fw.write("# Web application base urls. Must be set when the appliaction is moved by hand from \n");
		fw.write("#     a server to another or when the tomcat is pushed behind a proxy.\n");
		fw.write("urlroot=" + urlRoot + "\n");
		fw.write("#########################\n");
		fw.write("# If true product download requires an authentication.\n");
		fw.write("# Authentication parameters must be set by hand in the tomcat config.\n");
		fw.write("# (refer to the tutorial).\n");
		fw.write("securedownlad=false\n");
		fw.write("#########################\n");
		fw.write("# Current SaadaDB root dir (no longer used)\n");
		fw.write("#saadadbroot=" + rootDir + "\n");
		fw.write("#########################\n");
		fw.write("# Set debug mode\n");
		fw.write("debug=off	\n");
		fw.write("#########################\n");
		fw.write("# Set the tomcat log file. Possible values are:\n");
		fw.write("#   none: Default Tomcat log file (TOMCAT_HOME/logs/catalina.out usually)\n");
		fw.write("#   default: The log file has the same name as the webapps (last field of urlroot or SaadaDB name)\n");
		fw.write("#            It is located in TOMCAT_HOME/logs/\n");
		fw.write("#   Other name: Full path of the log file if starts with " + File.separator + "\n");		
		fw.write("#               TOMCAT_HOME/logs//Othername.log otherwise\n");
		fw.write("#   (refer to the tutorial).\n");
		fw.write("logfile=default\n");
		fw.close();
		
	}
	

	/**
	 * similar to the main but invoked from NewSaadaDBTool (no exit)
	 * @param args
	 * @throws FatalException 
	 */
	public static void innerMain(String args[]) throws FatalException  {
		try {
			ArgsParser ap = new ArgsParser(args);
			NewWebServer web_app = new NewWebServer(args[args.length-1], ap.getPassword());
			web_app.BuildWebApp();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}
	}
	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String args[])   {
		try {
			ArgsParser ap = new ArgsParser(args);
			NewWebServer web_app = new NewWebServer(args[args.length-1], ap.getPassword());
			web_app.BuildWebApp();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1); 
		}
	}

}

