package saadadb.command;

import saadadb.api.SaadaCollection;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class DeleteCollectionIndex {
	
	/**
	 * Drop Index all class tables for the category in the collection and also index the collection table
	 * @param collection
	 * @param category
	 * @throws SaadaException
	 */
	public static void dropIndexCollectionCategory(String collection, String category) throws SaadaException {
		SaadaCollection sc = new SaadaCollection(collection);
		String[] classes = sc.getClassNames(Category.getCategory(category));
		for( int c=0 ; c<classes.length ; c++ ) {
			SQLTable.dropTableIndex(classes[c].toLowerCase(), null);
		}
		SQLTable.dropTableIndex(collection + "_" + category, null);
	}
	
	/**
	 * 
	 */
	private static void usage() {		
		Messenger.printMsg(Messenger.ERROR, "USAGE: java DeleteCollectionIndex -category=[category] -collection=[collection] [Saada_DB_Name]");
		System.exit(1);
	}

	/**
	 * @param args
	 * @throws SaadaException
	 */
	static public void main(String[] args) throws SaadaException {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		try {
			Database.getConnector().setAdminMode(ap.getPassword());
			String collection = ap.getCollection();
			if( collection == null || !Database.getCachemeta().collectionExists(collection) ) {
				Messenger.printMsg(Messenger.ERROR, "Collection <" + collection + "> does not exist");
				usage();
			}
			String category = ap.getCategory();
			if( !Category.isValid(category) ) {
				Messenger.printMsg(Messenger.ERROR, "Category <" + category + "> does not exist");
				usage();
			}
			dropIndexCollectionCategory(collection, category);
		} catch (Exception e1) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e1);
		}
		
	}

}
