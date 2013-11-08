package saadadb.dataloader;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;


public class SQLImporter extends SaadaProcess {
	/*
	 * COnfiguration used by  the current loading session
	 */
	private ConfigurationDefaultHandler configuration;
	private String tableToImport = null;	
	private ArgsParser tabArg;

	/**
	 * @param args
	 */
	public static void main(String[] args){
		
		try {
			ArgsParser ap = new ArgsParser(args);
			String config=null;
			/*
			 * All parameters are given in the command line
			 */
			if( (config = ap.getConfig()) == null || config.length() == 0) {
				new SQLImporter(args).importTable();
			}
			/*
			 * The command line contains a reference to a config file. 
			 * Parameters contained in this file are taken in addition 
			 * with the collection and the data file/dir toload
			 */
			else {		
				Database.init(ap.getDBName());
				FileInputStream fis = new FileInputStream(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + config);
				ObjectInputStream in = new ObjectInputStream(fis);
				ArgsParser read_ap = (ArgsParser)in.readObject();
				in.close();
				new SQLImporter(read_ap.completeArgs(ap.getCollection(), ap.getFilename(), null, ap.isNoindex(), ap.getDebugMode())).importTable();				
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			usage();
			System.exit(1);
		} 
	}
	
	/**
	 * Print out a short help and exit
	 */
	public static void usage() {
		System.out.println("USAGE: jave dataloader.SQLImporter [loader params] [db_name]");
		System.exit(1);		
	}
	
	/**
	 * Creator init the loader and the database
	 * @param args
	 * @throws ParsingException
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public SQLImporter(String[] args) throws Exception {
		super(0);
		boolean debug = false;
		if( Messenger.debug_mode ) {
			debug = true;
		}
		this.tabArg = new ArgsParser(args);
		Database.init(this.tabArg.getDBName());
		if( !Database.getConnector().isAdmin_mode() ) {
			Database.setAdminMode(this.tabArg.getPassword());
		}
		this.tableToImport = this.tabArg.getFilename();
		if( debug ) {
			this.tabArg.addDebugMode(true);
			Messenger.debug_mode = true;
		}
	}

	/**
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public void importTable() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Start to load data with these parameters " + this.tabArg);
		/*
		 * Build a configuration from paramaters or load a named configuration.
		 */
		this.setConfiguration();

		String typeMapping = configuration.getTypeMapping().getTypeMapping();
		/*
		 * Process vectors of product one by one
		 */
		boolean build_index = true;
		if( this.tabArg.isNoindex() ) {
			build_index = false;
		}
		if (typeMapping.equals("MAPPING_USER_SAADA")) {
			/*
			 * Load all products in one step
			 */
			if( configuration.getCategorySaada() != Category.FLATFILE &&
					configuration.getCategorySaada() != Category.TABLE	) {
	//ERROR			(new SchemaFusionMapper(this, file_to_load, this.configuration, build_index)).ingestProductSetByBurst();			

			} else {
				//ERROR				(new SchemaFusionMapper(this, file_to_load, this.configuration, build_index)).ingestProductSet();			
			}
		}else if (typeMapping.equals("MAPPING_1_1_SAADA")){
			/*
			 * Load products one by one 
			 */
		}else if (typeMapping.equals("MAPPING_CLASSIFIER_SAADA")){
			/*
			 * Load products one by one and 
			 */

			//ERROR			(new SchemaClassifierMapper(this, file_to_load, this.configuration, build_index)).ingestProductSet();
		}
	}

	private void setConfiguration() throws SaadaException {
			/*
			 * Just to rise an excpetion if the collection doesn't exist
			 */
			Database.getCachemeta().getCollection(this.tabArg.getCollection());
			this.configuration = tabArg.getConfiguration();
		}

}
