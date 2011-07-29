package saadadb.newdatabase;

/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @version SAADA 1.0
 * @author: NGUYEN Ngoc Hoan
 * E-Mail: nguyen@saadadb.u-strasbg.fr</p>
 */

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

 */
class NewWebServer extends NewSaadaDB {

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

		FileWriter fw = new FileWriter(rootDir  + separ + "web" + separ + "dbname.txt");
		fw.write("# This file can be put at the webapp root to indicate the saadadb name of the current application\n");
		fw.write("saadadbname=" + nameDB + "\n");
		fw.write("#urlroot=" + this.connector.getUrl_root() + "\n");
		fw.write("#saadadbroot=" + rootDir + "\n");
		fw.write("#debug=off	\n");
		fw.close();
		
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

