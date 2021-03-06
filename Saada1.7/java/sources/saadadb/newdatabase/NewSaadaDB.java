package saadadb.newdatabase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import saadadb.command.ArgsParser;
import saadadb.compat.Files;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.database.SaadaDBConnector;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassCollection;
import saadadb.generationclass.GenerationClassSaadaDB;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_SaadaDB;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Group;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.sqltable.Table_Saada_Metaclass;
import saadadb.sqltable.Table_Saada_Metacoll;
import saadadb.sqltable.Table_Saada_Qualifiers;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.sqltable.Table_Saada_VO_Resources;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;

/**
 * @author michel
 * * @version $Id$

 * 03/2010: Ignore exception when load SQL procedure (exception rose by PSQL when proc does not exists)
 */
public class NewSaadaDB {
	protected static String separ = System.getProperty("file.separator");

	static public String SAADA_HOME;
	protected String SAADA_DB_HOME;
	protected SaadaDBConnector connector;
	private String admin_passwd;


	/**
	 * @param saada_home
	 * @param admin_password
	 * @throws FatalException
	 */
	public NewSaadaDB(String saada_home, String admin_password) throws  FatalException {
		NewSaadaDB.SAADA_HOME = saada_home;
		this.connector = SaadaDBConnector.getConnector(null,  false);
		this.connector.ParserSaadaDBConfFile(SAADA_HOME + separ + "config" + separ + "saadadb.xml");
		this.SAADA_DB_HOME = this.connector.getRoot_dir();
		this.admin_passwd = admin_password;
	}


	/**
	 * @return
	 * @throws Exception
	 */
	public boolean buildSaadaDB() throws Exception {
		String nameDB = this.connector.getDbname();
		String rootDir = this.connector.getRoot_dir();

		// creer un r�pertoire pour saada_db
		File db_root = new File(rootDir);
		Messenger.printMsg(Messenger.TRACE,
				"Creating the SaadaDB base dir in "
				+ db_root.getAbsolutePath() );
		WorkDirectory.emptyDirectory(db_root);
		WorkDirectory.validWorkingDirectory(db_root.getAbsolutePath() );

		// creer un r�pertoire pour les products (les collections des
		// produits seront h�berg� ici)
		File db_rep = new File(this.connector.getRepository());
		Messenger.printMsg(Messenger.TRACE, "Creating the repository in " + db_rep.getAbsolutePath());
		WorkDirectory.emptyDirectory(db_rep);
		WorkDirectory.validWorkingDirectory(db_rep.getAbsolutePath());
		/*
		 * Directories to store flatfiles and query results files
		 * accessed by /getproduct?..
		 */
		File reposi_root = new File(this.connector.getRepository() + separ + Repository.VOREPORTS);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.TMP);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.LOGS);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.INDEXRELATIONS);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.EMBEDDEDDB);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.DMS);
		reposi_root.mkdirs();
		reposi_root = new File(this.connector.getRepository() + separ + Repository.CONFIG);
		reposi_root.mkdirs();

		for( String f: (new File(SAADA_HOME + separ + "dbtemplate")).list()) {
			Files.copy(SAADA_HOME + separ + "dbtemplate" + separ + f
					, SAADA_DB_HOME + separ + f);
		}
		(new File(SAADA_DB_HOME + separ + "indexation")).mkdir();			
		(new File(SAADA_DB_HOME + separ + "class_mapping")).mkdir();
		(new File(SAADA_DB_HOME + separ + "web" + separ + "WEB-INF" + separ + "lib")).mkdir();
		//				// Create directory tree
		//			Messenger.printMsg(Messenger.TRACE,
		//					"Creating the directory tree of the SaadaDB");
		//			String dirs[] = { "lib", "java", "bin", "doc", "algo_correlation", "class_mapping", "config", "sqlprocs" };
		//			for (int i = 0; i < dirs.length; i++) {
		//				File dirCreator2 = new File(db_root.getAbsolutePath()
		//						+ separ + dirs[i]);
		//				dirCreator2.mkdirs();
		//				Files.copy(SAADA_HOME + separ + "dbtemplate" + separ + dirs[i],
		//						SAADA_DB_HOME + separ + dirs[i]);
		//			}
		Files.copy(SAADA_HOME + separ + "jtools" + separ, SAADA_DB_HOME+ separ + "jtools");
		//
		//			String wdirs[] = { "applets", "images", "javascript", "jsimport", "jsonsample",
		//					           "styles", "userforms", "WEB-INF", "saadaqleditorGeneration" };
		//			for (int i = 0; i < wdirs.length; i++) {
		//				File dirCreator2 = new File(db_root.getAbsolutePath()
		//						+ separ + "web" + separ + wdirs[i]);
		//				dirCreator2.mkdirs();
		//				Files.copy(SAADA_HOME + separ + "dbtemplate" + separ + "web"
		//						+ separ + wdirs[i], SAADA_DB_HOME + separ + "web"
		//						+ separ + wdirs[i]);
		//			}
		//			File dirCreator2 = new File(db_root.getAbsolutePath() + separ
		//					+ "web" + separ + "WEB-INF" + separ + "classes");
		//			dirCreator2.mkdirs();
		//			dirCreator2 = new File(db_root.getAbsolutePath() + separ + "web"
		//					+ separ + "WEB-INF" + separ + "lib");
		//			dirCreator2.mkdirs();
		//			dirCreator2 = new File(db_root.getAbsolutePath() + separ + "web"
		//					+ separ + "sources");
		//			dirCreator2.mkdirs();
		//			dirCreator2 = new File(db_root.getAbsolutePath() + separ + "web"
		//					+ separ + nameDB + "servlet" + separ + nameDB + "servlet");
		//			dirCreator2.mkdirs();
		//			dirCreator2 = new File(db_root.getAbsolutePath() + separ + "web"
		//					+ separ + nameDB + "servlet" + separ + "lib");
		//			dirCreator2.mkdirs();
		//			dirCreator2 = new File(db_root.getAbsolutePath() + separ + "web"
		//					+ separ + "META-INF");
		//			dirCreator2.mkdirs();
		//			
		Messenger.printMsg(Messenger.TRACE, "Generating saadadb.properties");
		String[] old_start = new String[3];
		old_start[0] = "DIRECTORY_ROOT_SAADA_DB";
		old_start[1] = "SAADA_DB_GENERIC_NAME";
		old_start[2] = "DIRECTORY_ROOT_TOMCAT";
		String[] remplace_start = new String[3];
		/*
		 * For Windows compatibility
		 */
		remplace_start[0] = SAADA_DB_HOME.replaceAll("\\\\", "\\\\"+"\\\\").replaceAll(" ", "\\ ");
		remplace_start[1] = nameDB;
		remplace_start[2] = this.connector.getWebapp_home().replaceAll("\\\\", "\\\\"+"\\\\").replaceAll(" ", "\\ ");
		this.GenererFileConfXML(SAADA_HOME + separ + "bin" + separ,
				"saadadb.properties"
				, old_start, remplace_start
				, rootDir + separ + "bin" + separ);
		this.GenererFileConfXML(SAADA_HOME + separ + "config" + separ,
				"collection_attr.xml"
				, null, null
				, rootDir + separ + "config" + separ);
		this.GenererFileConfXML(SAADA_HOME + separ + "config" + separ,
				"saadadb.xml"
				, null, null
				, rootDir + separ + "config" + separ);

		Messenger.printMsg(Messenger.TRACE, "Generating SaadaDB class");
		GenerationClassSaadaDB.Generation(connector);
		/*
		 * Create SQL tables
		 */	
		Database.initConnector(this.connector.getDbname(), false);
		Spooler.getSpooler(1).openAdminConnection(this.admin_passwd);
		Database.getWrapper().loadSQLProcedures();
		SQLTable.beginTransaction();
		Table_SaadaDB.createTable();   	
		Table_Saada_Class.createTable();
		Table_Saada_Collection.createTable();
		Table_Saada_Group.createTable();
		Table_Saada_Relation.createTable();
		Table_Saada_Qualifiers.createTable();
		Table_Saada_Loaded_File.createTable();
		Table_Saada_Metacoll.createTables();
		Table_Saada_Metaclass.createTables();
		Table_Saada_VO_Resources.createTable(connector);
		/*
		 * Reload SQL procedures
		 */
		SQLTable.commitTransaction();
		/*
		 * From now we are connected to the new SaadaDB
		 */
		Database.init(this.connector.getDbname());
		/*
		 * Create class for collection level
		 */
		GenerationClassCollection.Generation(this.connector);
		Messenger.printMsg(Messenger.TRACE, "Database : " + nameDB
				+ " successfully created ");
		Runtime.getRuntime().gc();
		Spooler.getSpooler().close();
		return true;
	}


	/**
	 * Copy nameFile from saada_dir to saadadb_dir with remplacing old strings with remplace strings
	 * @param saada_dir
	 * @param nameFile
	 * @param old
	 * @param remplace
	 * @param saadadb_dir
	 * @throws FatalException 
	 */
	protected void GenererFileConfXML(String saada_dir,String nameFile,String[] old, String[] remplace, String saadadb_dir) throws FatalException {
		try {
			Messenger.printMsg(Messenger.TRACE, "Generating " + nameFile + " config file");
			FileWriter writer = new FileWriter(saadadb_dir+nameFile);

			FileReader in =new FileReader(saada_dir+nameFile);
			BufferedReader reader =new BufferedReader(in);
			String str="";
			boolean[] changes = null;
			if( old != null ) {
				changes = new boolean[old.length];
			}
			while ((str=reader.readLine())!=null) {
				if ( old != null && remplace != null ) {
					for (int i=0;i<old.length;i++) {
						if ( old != null && remplace != null && str.indexOf(old[i]) > 0) {
							String s1 = str.substring(0, str.indexOf(old[i]));
							String s2 = str.substring(str.indexOf(old[i])+old[i].length());
							str = s1 + remplace[i] + s2;	
							changes[i] = true;
						} 
					}
				}
				writer.write(str + "\n");
			}
			writer.close();
			reader.close();	
			if( old != null ) {
				String msg ="";
				for( int i=0 ; i< changes.length; i++ ) {
					if( !changes[i]) msg += " " + old[i];
				} 
				if( msg.length() > 0 ) {
					Messenger.printMsg(Messenger.WARNING, "String(s) " + msg + " not found in template " + saada_dir+nameFile);
					//FatalException.throwNewException(SaadaException.WRONG_RESOURCE, "String(s) " + msg + " not found in template " + saada_dir+nameFile);
				}
			}
		}
		catch (IOException e)  {        	
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.FILE_ACCESS, e);	
		}
	}

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String args[])   {
		int retour = 1;
		try {
			ArgsParser ap = new ArgsParser(args);
			NewSaadaDB newdb = new NewSaadaDB(args[args.length-1], ap.getPassword());
			Messenger.printMsg(Messenger.TRACE, "Build a new SaadaDB from " + args[args.length-1]);
			newdb.buildSaadaDB();
			NewWebServer.main(args);
			retour = 0;
		} catch (Exception e3) {
			Messenger.printStackTrace(e3);
		} finally {
			System.exit(retour);			
		}
	}

}
