package saadadb.dataloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.products.datafile.DataResourcePointer;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.ClassifierMode;

/**
 * Entry point for loading data files. Get the list of files, build the appropriate {@link SchemaMapper} and run it
 * @author michel
 * @version $Id$
 */
public class Loader extends SaadaProcess {

	/*
	 * COnfiguration used by  the current loading session
	 */
	private ProductMapping productMapping;
	private ArrayList<String> filesToBeLoaded = null;

	private ArgsParser tabArg;
	
	/**
	 * Creator: init the loader and the database
	 * @param args array of parameters
	 * @throws Exception 
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
			Messenger.switchDebugOn();
		}
	}
	
	/**
	 * Creator init the loader and the database
	 * @param args parser object
	 * @throws Exception 
	 */
	public Loader(ArgsParser args) throws Exception {
		super(0);
		boolean debug = false;
		if( Messenger.debug_mode ) {
			debug = true;
		}
		this.tabArg = args;
		this.setTabArg();
		Database.init(this.tabArg.getDBName());
		if( !Database.getConnector().isAdmin_mode() ) {
			Database.setAdminMode(this.tabArg.getPassword());
		}

		if( debug ) {
			this.tabArg.addDebugMode(true);
			Messenger.switchDebugOn();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		Messenger.debug_mode =true;
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
				FileInputStream fis = new FileInputStream(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + config);
				ObjectInputStream in = new ObjectInputStream(fis);
				ArgsParser read_ap = (ArgsParser)in.readObject();
				in.close();
				new Loader(read_ap.completeArgs(ap.getCollection(), ap.getFilename(), ap.getRepository(), ap.isNoindex(), ap.getDebugMode())).load();				
			}
		} catch (Exception e) {
			Database.close();
			Messenger.printStackTrace(e);
			usage();
			System.exit(1);
		} 
		Database.close();
	}

	/**
	 * Print out a short help and exit
	 */
	public static void usage() {
		System.out.println("USAGE: jave dataloader.Loader [loader params] [db_name]");
		System.exit(1);		
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
		if( this.filesToBeLoaded == null ) {
			setCandidateFileList();
		}
		if( filesToBeLoaded.size() == 0 ) {
			return;
		}
		ClassifierMode classifierMode = productMapping.getClassifier();
		/*
		 * Process vectors of product one by one
		 */
		/*
		 * Flatfiles have no class, they can be loaded by burst
		 */
		if( productMapping.getCategory() == Category.FLATFILE ) {
			(new FlatFileMapper(this, filesToBeLoaded, this.productMapping)).ingestProductSet();						
		} else if (classifierMode == ClassifierMode.CLASS_FUSION) {
			/*
			 * Load all products in one step
			 */
			if( productMapping.getCategory() != Category.FLATFILE &&
					productMapping.getCategory() != Category.TABLE	) {
				(new SchemaFusionMapper(this, this.filesToBeLoaded, this.productMapping)).ingestProductSetByBurst();			

			} else {
				(new SchemaFusionMapper(this, this.filesToBeLoaded, this.productMapping)).ingestProductSet();			
			}
		} else if (classifierMode == ClassifierMode.CLASSIFIER){
			/*
			 * Load products one by one and 
			 */
			(new SchemaClassifierMapper(this, this.filesToBeLoaded, this.productMapping)).ingestProductSet();
		}
	}

	/**
	 * @return
	 * @throws AbortException 
	 */
	public void setCandidateFileList() throws Exception {
		this.filesToBeLoaded = new ArrayList<String>();
		String filename = this.tabArg.getFilename();
		String filter = this.tabArg.getFilter();
		if( filename ==  null || filename.equals("")) {
			AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "A file (or dir) name must be specified");
		}
		DataResourcePointer requested_file = null;
		try {
			requested_file = new DataResourcePointer(filename);
		} catch(Exception e){
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.FILE_ACCESS, e);
		}
		if( requested_file.isURL) {
			this.filesToBeLoaded.add(requested_file.getAbsolutePath());
			Messenger.printMsg(Messenger.TRACE, "One unique URL to process <" + requested_file.inputFileName + ">");							
			
		} else 	if( !requested_file.file.exists()  ) {
			try {
				/*
				 * Just to see if the file is a double symbolic link
				 */
				if( !(new File(requested_file.file.getCanonicalPath())).exists() ) {
					AbortException.throwNewException(SaadaException.MISSING_FILE, "file or directory <" + requested_file.file.getAbsolutePath() + "> doesn't exist");	
				} else {
					// Something missing here?
					return;
				}
			} catch (Exception e) {
			}
			AbortException.throwNewException(SaadaException.MISSING_FILE, "file or directory <" + requested_file.file.getAbsolutePath() + "> doesn't exist");							
		}
		/*
		 * "filename" is a directory 
		 */ 
		else if( requested_file.file.isDirectory() ) {
			String[] dir_content = requested_file.file.list();
			this.filesToBeLoaded = new ArrayList<String>(dir_content.length);
			if( dir_content.length == 0 ) {
				Messenger.printMsg(Messenger.TRACE, "Directory <" + requested_file.file.getAbsolutePath() + "> is empty");		
				return;
			}
			Messenger.printMsg(Messenger.TRACE, "Reading directory <" + requested_file.file.getAbsolutePath() + ">");							
			int cpt = 0;
			for( int i=0 ; i<dir_content.length ; i++ ) {	
				DataResourcePointer candidate_file=null;
				try {
				candidate_file = new DataResourcePointer(requested_file.file.getAbsolutePath() + System.getProperty("file.separator") + dir_content[i]);
				} catch(Exception e){
					AbortException.throwNewException(SaadaException.FILE_ACCESS, e);
				}
				/*
				 * Takes all URLs.  If it does not work, an exception is risen
				 */
				if( candidate_file.isURL ){
					this.filesToBeLoaded.add(candidate_file.getAbsolutePath());
					cpt++;				
				}
				/*
				 * Do not proceed recursively: just files are taken
				 */
				else if( !candidate_file.file.isDirectory() && this.validCandidateFile(dir_content[i], filter)) {
					this.filesToBeLoaded.add(candidate_file.getAbsolutePath());
					cpt++;
				}
				if( (cpt% 5000) == 0 ){
					Messenger.printMsg(Messenger.TRACE, cpt + "/" + dir_content.length + " file validated (" + (i+1-cpt) + " rejected)");
				}
			}
			Messenger.printMsg(Messenger.TRACE, cpt + " candidate files found in directory <" + requested_file.inputFileName + ">");
		}
		/*
		 *  "filename" is a single file to load
		 */
		else {
			this.filesToBeLoaded.add(requested_file.getAbsolutePath());
			Messenger.printMsg(Messenger.TRACE, "One unique file to process <" + requested_file.inputFileName + ">");							
		}
	}

	/**
	 * Create an appropriate DataFile from the file name and push it in the lits of files to be loaded
	 * @param fullPath
	 * @throws Exception 
	 */
	private void addDataFile(String fullPath) throws Exception {
		if( fullPath.matches(RegExp.FITS_FILE) ) {
			this.filesToBeLoaded.add(fullPath);
		} else if( fullPath.matches(RegExp.VOTABLE_FILE) ) {
			this.filesToBeLoaded.add(fullPath);
		} else {
			this.filesToBeLoaded.add(fullPath);
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
		} else {
			switch(productMapping.getCategory()) {
			case Category.FLATFILE: return true;
			case Category.IMAGE: return filename.matches(RegExp.FITS_FILE);
			default: if( !filename.matches(RegExp.FITS_FILE) ) {
				return filename.matches(RegExp.VOTABLE_FILE);
			} else {
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
		this.productMapping = tabArg.getProductMapping();
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
	public void setFileToLoad(ArrayList<String> file_to_load) throws Exception {
		String base_dir = this.tabArg.getFilename();
		this.filesToBeLoaded = new ArrayList<String>();
		for( String f: file_to_load) {
			File cf = new File(base_dir + Database.getSepar() + f);
			if( cf.exists() && !cf.isDirectory() ) {
				if( this.tabArg.getCategory() == Category.explain(Category.FLATFILE )||
				f.matches(RegExp.FITS_FILE) || f.matches(RegExp.VOTABLE_FILE) || f.matches(RegExp.JSON_FILE)) {
					this.addDataFile(base_dir + Database.getSepar() + f);
				} else {
					Messenger.printMsg(Messenger.TRACE, f + ": cannot be loaded because it does not look like a datafile (FITS or VOTable or JSON)");
				}
			} else {
				AbortException.throwNewException(SaadaException.MISSING_FILE, "<" + cf.getAbsolutePath() + "> does not exist or is not a file");
			}
		}
	}
	

	/**
	 * returns a map of the data files clusters Each cluster contain all files with the same format. 
	 * The key map is the format signature. The cluster (map value) is contained in a {@linkplain DataFileCluster} 
	 * @return the cluster map
	 * @throws Exception
	 */
	public Map<String, DataFileCluster> getProductClusters() throws Exception{
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
		if( this.filesToBeLoaded == null ) {
			setCandidateFileList();
		}
		if( filesToBeLoaded.size() == 0 ) {
			return null;
		}
		return (new SchemaClassifierMapper(this, this.filesToBeLoaded, this.productMapping)).getProductClusters();
	}

}
