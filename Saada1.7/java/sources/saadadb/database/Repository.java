package saadadb.database;

import java.io.File;
import java.util.Date;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaOID;
import saadadb.compat.Files;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.CopyFile;
import saadadb.util.Messenger;

/** * @version $Id$

 * some utilities handling repository files.
 * @author michel
 * @version $Id$
 * 07/2011: method sweepReportDir + getUserReportsPath
 */
public class Repository {
	private static String separ = System.getProperty("file.separator");
	/*
	 * Repository subdirs
	 */
	public static final String VOREPORTS      = "voreports";
	public static final String TMP            = "tmp";
	public static final String LOGS           = "logs";
	public static final String INDEXRELATIONS = "indexrelations";
	public static final String EMBEDDEDDB     = "embeddeddb";
	public static final String DMS     	      = "dms";
	public static final String CONFIG     	  = "config";
	/*
	 * These fields are set into the accessors because the class can be used before the repository is created
	 * We supposed the accessors to be called after the repository is created 
	 */
	public static String VOREPORTS_PATH      = null;
	public static String TMP_PATH            = null;
	public static String LOGS_PATH           = null;
	public static String INDEXRELATIONS_PATH = null;
	public static String EMBEDDEDDB_PATH     = null;
	public static String DMS_PATH            = null;
	public static String CONFIG_PATH         = null;

	/**
	 * @return
	 */
	public static final String getVoreportsPath() {
		if( VOREPORTS_PATH == null ) 
			VOREPORTS_PATH =  Database.getRepository()
			+ separ
			+ VOREPORTS 
			+ separ;		
		return VOREPORTS_PATH;
	}
	
	/**
	 * @param sessionId User session
	 * @return          Returns the directory where data associated 
	 *                   with a user session are stored
	 */
	public static final String getUserReportsPath(String sessionId) {
		if( VOREPORTS_PATH == null ) 
			VOREPORTS_PATH =  Database.getRepository()
			+ separ
			+ VOREPORTS 
			+ separ;		
		return VOREPORTS_PATH  + sessionId + separ;
	}
	
	
	/**
	 * @return
	 */
	public static final String getTmpPath() {
		if( TMP_PATH == null ) 
			TMP_PATH =  Database.getRepository()
			+ separ
			+ TMP ;		
		return TMP_PATH;
	}
	/**
	 * @return
	 */
	public static final String getLogsPath() {
		if( LOGS_PATH == null ) 
			LOGS_PATH =  Database.getRepository()
			+ separ
			+ LOGS ;	
		return LOGS_PATH;
	}
	/**
	 * @return
	 */
	public static final String getIndexrelationsPath() {
		if( INDEXRELATIONS_PATH == null ) 
			INDEXRELATIONS_PATH =  Database.getRepository()
			+ separ
			+ INDEXRELATIONS ;	
		return INDEXRELATIONS_PATH;
	}
	/**
	 * @return
	 */
	public static final String getDmsPath() {
		if( DMS_PATH == null ) 
			DMS_PATH =  Database.getRepository()
			+ separ
			+ DMS ;		
		return DMS_PATH;
	}
	/**
	 * @return
	 */
	public static final String getEmbeddeddbPath() {
		if( EMBEDDEDDB_PATH == null ) 
			EMBEDDEDDB_PATH =  Database.getRepository()
			+ separ
			+ EMBEDDEDDB ;		
		return EMBEDDEDDB_PATH;
	}
	/**
	 * @return
	 */
	public static final String getConfigPath() {
		if( CONFIG_PATH == null ) 
			CONFIG_PATH =  Database.getRepository()
			+ separ
			+ CONFIG ;		
		return CONFIG_PATH;
	}
	/**
	 *  remove VO reports  older the 30000 sec
	 * @throws DatabaseException 
	 */
	public static void cleanUpReportDir() {
		File f = new File(getVoreportsPath());
		if( f.isDirectory() ) {
			String[] content = f.list();
			long t0 = (new Date()).getTime();
			for( int i=0 ; i<content.length ; i++ ) {
				File item = new File(getVoreportsPath() + Database.getSepar() + content[i]);
				if( !item.isDirectory() ) {
					long dt = (t0 - item.lastModified())/1000;
					/*
					 * Reports are removed after 30000 seconds
					 */
					if( dt > 300000) {
						Messenger.printMsg(Messenger.TRACE, "removing VO report " + content[i] + " (" + dt + "sec old)");
						item.delete();
					}
				}
			}
		}
	}
	
	/**
	 * Remove the whole content of the report dir
	 */
	public static final void sweepReportDir() {
		Messenger.printMsg(Messenger.TRACE, "Sweeping report dir");
		String[] content = (new File(getVoreportsPath())).list();
		for( String c: content) {
			Files.deleteFile(getVoreportsPath() + File.separator + c);
		}

	}
	

//	/**
//	 * Return the full path of the vignette file associated with the image si.
//	 * @param si
//	 * @return
//	 * @throws SaadaException 
//	 */
//	public static String getVignettePath(ImageSaada si) throws SaadaException {
//		return Database.getRepository()
//		+ separ
//		+ SaadaOID.getCollectionName(si.getOid())
//		+ separ
//		+ "IMAGE" 
//		+ separ
//		+ "JPEG"
//		+ separ
//		+ si.getVignetteName();
//
//	}			

	/**
	 * @param org_file : Full path of the origin file
	 * @param dest_file : name of the destination file
	 * @param collection : collection where to store the file
	 * @param category : category where to store the file
	 * @throws Exception
	 */
	public static void storeDataFile(String org_file,String dest_file, String collection, int category) throws Exception {
		String reportFile = Database.getRepository() 
		+ separ + collection
		+ separ + Category.explain(category) 
		+ separ;
		CopyFile.copy(org_file, reportFile + dest_file);
	}

	/**
	 * @param org_file : Full path of the origin file
	 * @param dest_file : name of the destination file
	 * @param oidsaada
	 * @throws Exception
	 */
	public static void storeDataFile(String org_file,String dest_file, long oidsaada) throws Exception {
		String reportFile = Database.getRepository() 
		+ separ + SaadaOID.getCollectionName(oidsaada) 
		+ separ + SaadaOID.getCategoryName(oidsaada) 
		+ separ;
		CopyFile.copy(org_file, reportFile + dest_file);
	}

	/**
	 * Remove from the repository the directory tree of the collection coll
	 * @param coll: collection to remove
	 */
	public static void removeCollectionDir(String coll) {
		String dirname = Database.getRepository() + separ + coll;
		File dir = new File(dirname);
		if( dir.exists() ) {
			if( !Files.deleteFile(dir) ) {
				Messenger.printMsg(Messenger.WARNING, "Could not remove directory " + dirname);
			}
		}		
	}
	/**
	 * Create in the repository the directory tree for the collection coll
	 * @param coll
	 * @throws FatalException 
	 */
	public static void createSubdirsForCollection(String coll) throws FatalException {
		removeCollectionDir(coll);
		String dirname = Database.getRepository() + separ + coll;
		File dir = new File(dirname);
		if( !dir.mkdirs() ) {
			FatalException.throwNewException(SaadaException.FILE_ACCESS, "Could not create directory " + dirname);				
		}
		if( !dir.exists() || !dir.isDirectory() || !dir.canWrite() ) {
			FatalException.throwNewException(SaadaException.FILE_ACCESS, "Directory " + dirname + " has nnot been created or has no wrtite permission");				

		}
		for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
			/*
			 * update the repository
			 */
			if( ! (new File(dirname + separ + Category.explain(cat))).mkdirs() ){
				FatalException.throwNewException(SaadaException.FILE_ACCESS, "Could not create directory " + dirname + separ + Category.explain(cat));				
			}
			if( cat == Category.IMAGE || cat == Category.SPECTRUM ) {
				if( ! (new File(dirname + separ + Category.explain(cat) + separ + "JPEG")).mkdirs()		){
					FatalException.throwNewException(SaadaException.FILE_ACCESS, "Could not create directory " + dirname + separ + Category.explain(cat)+ separ + "JPEG");				
				}			
			}
		}
	}
}
