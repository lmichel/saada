package saadadb.database;

import java.io.File;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.RegExp;

public class InstallParamValidator {

	/** * @version $Id$

	 * @param name
	 * @return
	 * @throws FatalException
	 */
	public static void validName(String name) throws FatalException {
		if( !name.matches(RegExp.DBNAME) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Wrong SaadaDB Name");
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void validURL(String name) throws FatalException {
		if( !name.matches(RegExp.URL) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> is not an URL");
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void canBeRepository(String name)throws FatalException {
		isDirectoryWritable(name);
		try {
			areSubdirWritable(name, new String[]{Repository.VOREPORTS, Repository.TMP, Repository.EMBEDDEDDB, Repository.LOGS, Repository.INDEXRELATIONS});
		}
		catch(FatalException e) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a repository:"  + e.getContext());				
		}
	}
	
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void canBeTomcatDir(String name) throws FatalException {
		//isDirectoryWritable(name);
		try {
			String webappsdir = name + Database.getSepar() + "webapps";
			File f = new File(webappsdir);
			if( f.exists() && f.isDirectory() ) {
				areSubdirWritable(webappsdir, new String[]{""});
			}
			else {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat home directory:" );								
			}
		}
		catch(FatalException e) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat directory:"  + e.getContext());				
		}
	}
	public static void canBeTomcatWebappsDir(String name) throws FatalException {
		//isDirectoryWritable(name);
		try {
			areSubdirWritable(name, new String[]{""});
		}
		catch(FatalException e) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> does no look like a Tomcat directory:"  + e.getContext());				
		}
	}
	
	/**
	 * @param name
	 * @param subdirs
	 * @throws FatalException
	 */
	public static void areSubdirWritable(String name, String[] subdirs) throws FatalException {
		String msg = "";
		for( String subdir: subdirs) {
			try {
				isDirectoryWritable(name +  Database.getSepar() + subdir);
			} catch( FatalException e) {
				msg += subdir + " ";
			}
		}
		if( msg.length() > 0) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "These subdirectories <" + msg + "> are not writable or do not exist");							
		}
	}
	/**
	 * @param name
	 * @throws FatalException
	 */
	public static void isDirectoryWritable(String name) throws FatalException {
		if( name == null || name.length() == 0 ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Null or empty directory name");	
		}
		File f = new File(name);
		if( !f.exists()  ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Directory <" + name + "> does no exist");	
		}
		else if(  !f.isDirectory()  ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "<" + name + "> is not a directory");	
		}
		if( !f.canWrite() ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not write in directory <" + name + "> ");	
		}
	}
	
}
