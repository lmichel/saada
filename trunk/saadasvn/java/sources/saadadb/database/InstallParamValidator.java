package saadadb.database;

import java.io.File;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.RegExp;

/**
 * @author michel
 * @version $Id$
 *
 * 04/2012: replace FatalException with QueryException
 */
public class InstallParamValidator {

	/** * @version $Id$

	 * @param name
	 * @return
	 * @throws FatalException
	 */
	public static void validName(String name) throws QueryException {
		if( !name.matches(RegExp.DBNAME) ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong SaadaDB Name");
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void validURL(String name) throws QueryException {
		if( !name.matches(RegExp.URL) ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> is not an URL");
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void canBeRepository(String name)throws QueryException {
		isDirectoryWritable(name);
		try {
			areSubdirWritable(name, new String[]{Repository.VOREPORTS, Repository.TMP, Repository.EMBEDDEDDB, Repository.LOGS, Repository.INDEXRELATIONS});
		}
		catch(QueryException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a repository:"  + e.getContext());				
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void canBeTomcatDir(String name) throws QueryException {
		//isDirectoryWritable(name);
		try {
			String webappsdir = name + Database.getSepar() + "webapps";
			File f = new File(webappsdir);
			if( f.exists() && f.isDirectory() ) {
				areSubdirWritable(webappsdir, new String[]{""});
			}
			else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat home directory:" );								
			}
		}
		catch(QueryException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat directory:"  + e.getContext());				
		}
	}
	public static void canBeTomcatWebappsDir(String name) throws QueryException {
		//isDirectoryWritable(name);
		try {
			File f = (new File(name)).getParentFile();
			areSubdirWritable(f.getAbsolutePath(), new String[]{"webapps"});
		}
		catch(QueryException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat directory:"  + e.getContext());				
		}
	}
	
	/**
	 * @param name
	 * @param subdirs
	 * @throws FatalException
	 */
	public static void areSubdirWritable(String name, String[] subdirs) throws QueryException {
		String msg = "";
		for( String subdir: subdirs) {
			try {
				isDirectoryWritable(name +  Database.getSepar() + subdir);
			} catch( QueryException e) {
				msg += subdir + " ";
			}
		}
		if( msg.length() > 0) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "These subdirectories <" + msg + "> are not writable or do not exist");							
		}
	}
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void isDirectoryWritable(String name) throws QueryException {
		if( name == null || name.length() == 0 ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Null or empty directory name");	
		}
		File f = new File(name);
		if( !f.exists()  ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Directory <" + name + "> does no exist");	
		}
		else if(  !f.isDirectory()  ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> is not a directory");	
		}
		if( !f.canWrite() ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not write in directory <" + name + "> ");	
		}
	}
	
}
