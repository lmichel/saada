package saadadb.collection;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

/**
 * Handle repositoy operations
 * @author michel
 *
 */
public class RepositoryManager {

	/**
	 * @param filepath
	 */
	private static final void removeFile(String filepath) {
		/*
		 * Continue even if removing fails
		 */
		try {
			File f = new File(filepath);
			if( f.exists() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Deleting <" + filepath + "> from the repository");
				f.delete();
			}
			else {
				Messenger.printMsg(Messenger.WARNING, "File <" + filepath + "> does not exist in the repository");
			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
		}
	}
	/**
	 * @param filename
	 * @param collection
	 * @param category
	 * @throws FatalException
	 */
	protected static final void removeFile(String filename, String collection, int category) throws FatalException {
		if( filename.indexOf(Database.getSepar()) != -1 ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Removing a file <" + filename + "> not stored within the repository is not allowed");
		}
		else if( category == Category.ENTRY ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "There is never file in the repository mathing the category ENTRY");
		}
		else {
			String filepath = Database.getRepository() 
			+ Database.getSepar() + collection
			+ Database.getSepar() + Category.explain(category)
			+ Database.getSepar() ;
			removeFile(filepath + filename);
			if( category == Category.IMAGE ) {
				int pos;
				if( (pos = filepath.lastIndexOf(".")) != -1  ) {
					filepath = filepath + "JPEG" + Database.getSepar() + filepath.substring(0, pos) + ".jpg";
					removeFile(filepath);
				}
			}
		}
	}

	/**
	 * @param collection
	 * @param category
	 * @throws FatalException
	 */
	protected static final void emptyCategory(String collection, int category) throws FatalException {
		if( category == Category.ENTRY ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "There is never file in the repository mathing the category ENTRY");
		}
		else {
			String path = Database.getRepository() 
			+ Database.getSepar() + collection
			+ Database.getSepar() + Category.explain(category)
			+ Database.getSepar() ;
			String[] fns = ((new File(path)).list());
			if( fns != null ) {
				for( String fn: fns ){
					removeFile(fn, collection, category);
				}
			}

		}
	}

	/**
	 * @param dir
	 * @return
	 */
	protected static void removeCollection(String collection) {
		File dir = new File(Database.getRepository() 
				+ Database.getSepar() + collection
				+ Database.getSepar());
		if( dir.exists() ) {
			deleteDir(dir);
		}
		else {
			Messenger.printMsg(Messenger.WARNING, "Directory <" + dir.getName() + "> does not exist");
		}
	}

	/**
	 * @param dir
	 * @return
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	/**
	 * @param classe
	 * @throws FatalException
	 * @throws SQLException
	 */
	protected static final void emptyClass(String classe) throws Exception {
		try {
			int category  = Database.getCachemeta().getClass(classe).getCategory();
			String coll   = Database.getCachemeta().getClass(classe).getCollection_name();
			if( category == Category.ENTRY ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "There is never file in the repository mathing the category ENTRY");
			}
			else {

				SQLQuery squery = new SQLQuery();
				ResultSet rs = squery.run("SELECT repositoryname FROM saada_loaded_file WHERE classname = '" + classe + "'", new String[]{"saada_loaded_file"});
				while( rs.next() ) {
					String fn = rs.getString(1);
					RepositoryManager.removeFile(fn, coll, category);
				}
				squery.close();
			}		
		}catch(Exception e) {
			Messenger.printMsg(Messenger.ERROR, e.getMessage());
		}
	}
}
