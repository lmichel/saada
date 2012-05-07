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
 * * @version $Id$
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

		Messenger.printMsg(Messenger.TRACE, "New web application ready to be deployed");
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
		fw.write("# This file can be put at the webapp root to indicate the saadadb name of the current application\n");
		fw.write("saadadbname=" + nameDB + "\n");
		fw.write("urlroot=" + urlRoot + "\n");
		fw.write("securedownlad=false\n");
		fw.write("#saadadbroot=" + rootDir + "\n");
		fw.write("#debug=off	\n");
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

