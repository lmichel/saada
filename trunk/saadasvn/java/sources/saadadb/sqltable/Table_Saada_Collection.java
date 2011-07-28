package saadadb.sqltable;

import java.sql.ResultSet;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public abstract  class Table_Saada_Collection extends SQLTable {


	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_collection", "id int, name " + Database.getWrapper().getIndexableTextType() + ", description text", "name", false);
	}
	
	/**
	 * @param coll_name
	 * @param str_cat
	 * @throws FatalException 
	 */
	public static void addCollection(String coll_name,  String comment) throws FatalException {
		int max_key=0;
		try {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT max(id) from saada_collection");
			while( rs.next() ) {
				max_key = rs.getInt(1) +1;
			}
			squery.close();
			SQLTable.addQueryToTransaction("INSERT INTO saada_collection VALUES (" + max_key + ", '" + coll_name + "', '" + comment + "')"
					, "saada_collection" );
			Table_Saada_Metacoll.addCollection(coll_name, max_key);			
		} catch (SaadaException e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	
	/**
	 * @param coll_name
	 * @param str_cat
	 * @throws FatalException 
	 */
	public static void dropCollection(String coll_name) throws FatalException {
		try {
			Table_Saada_Metacoll.dropCollection(coll_name);
			SQLTable.addQueryToTransaction("DELETE FROM saada_collection WHERE  name = '" + coll_name + "'", "saada_collection");
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
		
}
