package saadadb.dataloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

/**
 * @author michel
 * * @version $Id$

 */
public class Loader extends SaadaProcess {
	
	/*
	 * COnfiguration used by  the current loading session
	 */
	private ConfigurationDefaultHandler configuration;
	private ArrayList<File> file_to_load = null;
	
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
				new Loader(args).load();
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
				new Loader(read_ap.completeArgs(ap.getCollection(), ap.getFilename(), ap.getRepository(), ap.isNoindex(), ap.getDebugMode())).load();				
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
		System.out.println("USAGE: jave dataloader.Loader [loader params] [db_name]");
		System.exit(1);		
	}
	
	/**
	 * Creator init the loader and the database
	 * @param args
	 * @throws ParsingException
	 * @throws Exception 
	 * @throws SQLException 
	 */
	public Loader(String[] args) throws Exception {
		super(0);
		boolean debug = false;
		if( Messenger.debug_mode ) {
			debug = true;
		}
		this.tabArg = new ArgsParser(args);
		this.setTabArg();
		Database.init(this.tabArg.getDBName());
		if( !Database.getConnector().isAdmin_mode() ) {
			Database.setAdminMode(this.tabArg.getPassword());
		}
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
	public void load() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Start to load data with these parameters " + this.tabArg);
		/*
		 * Build a configuration from paramaters or load a named configuration.
		 */
		this.setConfiguration();
		/*
		 * Build the list of product files possibly loaded
		 */
		/*
		 * The file to load list can be set by the GUI via the setFile_to_load method.
		 * In that case we don't set this list again
		 */
		if( this.file_to_load == null ) {
			setCandidateFileList();
		}
		if( file_to_load.size() == 0 ) {
			return;
		}
		String typeMapping = configuration.getTypeMapping().getTypeMapping();
		/*
		 * Process vectors of product one by one
		 */
		boolean build_index = true;
		if( this.tabArg.isNoindex() ) {
			build_index = false;
		}
		/*
		 * Flatfiles have no class, they can be loaded by burst
		 */
		if( configuration.getCategorySaada() == Category.FLATFILE ) {
			(new FlatFileMapper(this, file_to_load, this.configuration, build_index)).ingestProductSet();						
		} else if (typeMapping.equals("MAPPING_USER_SAADA")) {
			/*
			 * Load all products in one step
			 */
			if( configuration.getCategorySaada() != Category.FLATFILE &&
					configuration.getCategorySaada() != Category.TABLE	) {
				(new SchemaFusionMapper(this, file_to_load, this.configuration, build_index)).ingestProductSetByBurst();			

			} else {
				(new SchemaFusionMapper(this, file_to_load, this.configuration, build_index)).ingestProductSet();			
			}
		}else if (typeMapping.equals("MAPPING_1_1_SAADA")){
			/*
			 * Load products one by one 
			 */
		}else if (typeMapping.equals("MAPPING_CLASSIFIER_SAADA")){
			/*
			 * Load products one by one and 
			 */
			(new SchemaClassifierMapper(this, file_to_load, this.configuration, build_index)).ingestProductSet();
		}
	}
	
	/**
	 * @return
	 * @throws AbortException 
	 */
	public void setCandidateFileList() throws AbortException {
		this.file_to_load = new ArrayList<File>();
		String filename = this.tabArg.getFilename();
		String filter = this.tabArg.getFilter();
		if( filename ==  null || filename.equals("")) {
			AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "A file (or dir) name must be specified");
		}
		File requested_file = new File(filename);
		
		if( !requested_file.exists()  ) {
			try {
				/*
				 * Just to see if the file is a double symbolic link
				 */
				if( !(new File(requested_file.getCanonicalPath())).exists() ) {
					AbortException.throwNewException(SaadaException.MISSING_FILE, "file or directory <" + requested_file.getAbsolutePath() + "> doesn't exist");	
				}
				else {
					return;
				}
			} catch (Exception e) {
			}
			AbortException.throwNewException(SaadaException.MISSING_FILE, "file or directory <" + requested_file.getAbsolutePath() + "> doesn't exist");							
		}
		/*
		 * "filename" is a directory 
		 */ 
		else if( requested_file.isDirectory() ) {
			String[] dir_content = requested_file.list();
			if( dir_content.length == 0 ) {
				Messenger.printMsg(Messenger.TRACE, "Directory <" + requested_file.getAbsolutePath() + "> is empty");		
				return;
			}
			Messenger.printMsg(Messenger.TRACE, "Reading directory <" + requested_file.getAbsolutePath() + ">");							
			int cpt = 0;
			for( int i=0 ; i<dir_content.length ; i++ ) {	
				File candidate_file = new File(requested_file.getAbsolutePath() + System.getProperty("file.separator") + dir_content[i]);
				/*
				 * Do not proceed recursively: just files are taken
				 */
				if( !candidate_file.isDirectory() && this.validCandidateFile(dir_content[i], filter)) {
					this.file_to_load.add(candidate_file);
					cpt++;
				}
			}
			Messenger.printMsg(Messenger.TRACE, cpt + " candidate files found in directory <" + requested_file.getAbsolutePath() + ">");
		}
		/*
		 *  "filename" is a single file to load
		 */
		else {
			this.file_to_load.add(requested_file);
			Messenger.printMsg(Messenger.TRACE, "One unique file to process <" + requested_file.getAbsolutePath() + ">");							
		}
		
	}
	
	/**
	 * Check if filename is a possible datafile. It should match filter if not null, otherwise
	 * it should be either a VOTable (except for images) or a FITS file.
	 * @param filename
	 * @param filter
	 * @return
	 */
	private boolean validCandidateFile(String filename, String filter) {
		if( filter != null ) {
			return filename.matches(filter);
		}
		else {
			switch(configuration.getCategorySaada()) {
			case Category.FLATFILE: return true;
			case Category.IMAGE: return filename.matches(RegExp.FITS_FILE);
			default: if( !filename.matches(RegExp.FITS_FILE) ) {
				return filename.matches(RegExp.VOTABLE_FILE);
			}
			else {
				return true;
			}
			}
		}
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	private void setConfiguration() throws SaadaException {
		/*
		 * Just to rise an excpetion if the collection doesn't exist
		 */
		Database.getCachemeta().getCollection(this.tabArg.getCollection());
		this.configuration = tabArg.getConfiguration();
	}
	
	/**
	 * This methode return the option corresponding to the argument.
	 * @param argument -- The argument we want to get the option. For example -uniqueinstance=
	 * @return A string wich represent the option.
	 */
	public static String getOption(String argument, Vector<String> tabArg){
		
		String res = "";
		for(int i=0; i<tabArg.size(); i++){
			String arg = tabArg.get(i).trim();
			if( arg.startsWith(argument) )
				res = arg.substring( argument.length() ).trim();
		}
		return res;
	}
	
	
	
	/**
	 * Apply from args global parameters (debug...)
	 * @param tabArg
	 */
	public void setTabArg() {
		this.tabArg.setDebugMode();
	}
	
	/**
	 * @param file_to_load The file_to_load to set.
	 * @throws AbortException 
	 */
	public void setFile_to_load(ArrayList<String> file_to_load) throws AbortException {
		String base_dir = this.tabArg.getFilename();
		this.file_to_load = new ArrayList<File>();
		for( String f: file_to_load) {
			File cf = new File(base_dir + Database.getSepar() + f);
			if( cf.exists() && !cf.isDirectory() ) {
				this.file_to_load.add(cf);
			}
			else {
				AbortException.throwNewException(SaadaException.MISSING_FILE, "<" + cf.getAbsolutePath() + "> does not exist or is not a file");
			}
		}
	}
}
