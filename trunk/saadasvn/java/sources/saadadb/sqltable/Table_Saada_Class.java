package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.ArrayList;

import saadadb.classmapping.TypeMapping;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

public abstract  class Table_Saada_Class extends SQLTable {


	public static  void createTable() throws SaadaException {
		SQLTable.createTable("saada_class", "class_id int, name " + Database.getWrapper().getIndexableTextType() + ", category text, configuration_name text, collection_id int, mapping_type text, signature text, associate_class text, description text", "name", false);
//		SQLTable.addQueryToTransaction("create unique index saada_class1 on saada_class(class_id)", "saada_class");
//		SQLTable.addQueryToTransaction("create  index saada_class2 on saada_class(name)", "saada_class");
	}
	/**
	 * add class into table saada_class
	 * @throws SaadaException 
	 * @throws AbortException 
	 */
	public static int  addClass(String name_class, String name_collection,
			int category, String configurationName, int mapping_type, String signature, String description) throws Exception {

		if( Database.getCachemeta().classExists(name_class) ) {
			AbortException.throwNewException(SaadaException.METADATA_ERROR,"Class <" + name_class + "> already exist");
		}
		int id = 0;
		int collection_id = Database.getCachemeta().getCollection(name_collection).getId();
		SQLQuery squery = new SQLQuery();
		ResultSet rs2 = squery.run("Select max(class_id) From saada_class ");
		try {
			if (rs2.next()) {
				id = rs2.getInt(1) ;
				/*
				 * class id  = 1 is reserved fr flatfile which have no class
				 */
				if( id == 0 ) {
					id = 2;
				}
				else {
					id += 1;
				}
			}
			squery.close();
		} catch(Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
		SQLTable.addQueryToTransaction("insert into saada_class values (" + id
				+ ",'" + name_class + "','" 
				+ Category.explain(category) + "','"
				+ configurationName + "'," 
				+ collection_id + ",'"
				+ TypeMapping.explain(mapping_type) + "','"
				+ signature + "','"
				+ "', '"
				+ Database.getWrapper().getEscapeQuote(description)
				+ "')");
		return id;
	}

	/**
	 * @param classe
	 * @throws AbortException 
	 */
	public static void removeClass(String classe) throws AbortException {
		SQLTable.addQueryToTransaction("DELETE FROM saada_class WHERE name ='" + classe + "'", "saada_class");		
	}

	/**
	 * @param name_class
	 * @param collection_id
	 * @param category
	 * @param associate_class
	 * @throws AbortException
	 * @throws SaadaException 
	 */
	public static void setAssociateClass(String name_class, String collection,
			int category, String associate_class) throws AbortException, SaadaException {
		int collection_id = Database.getCachemeta().getCollection(collection).getId();

		SQLTable.addQueryToTransaction("UPDATE saada_class SET associate_class='" + associate_class + "' "
				+ "WHERE name = '" + name_class + "' AND collection_id = " + collection_id + " AND category = '" + Category.explain(category) + "'"
				, "saada_class");
	}

	/**
	 * Collection manager uses this kind of queries because it can not always rely on the cache meta (altered DB)
	 * @return
	 * @throws FatalException 
	 * @throws QueryException 
	 */
	public static String[] getClassNamesForCollection(String collection, int category) throws Exception {
		SQLQuery sq = new SQLQuery("SELECT name FROM saada_class WHERE collection_id = " 
				+ Database.getCachemeta().getCollection(collection).getId() 
				+ " AND category = '" + Category.explain(category) + "'");
		ResultSet rs = sq.run();
		ArrayList<String> retour = new ArrayList<String>();
		while( rs.next() ) {
			retour.add(rs.getString(1)); 
		}
		return retour.toArray(new String[0]);

	}
	/**
	 * Collection manager uses this kind of queries because it can not always rely on the cache meta (altered DB)
	 * @return
	 * @throws FatalException 
	 * @throws QueryException 
	 */
	public static String[] getClassNamesForCollection(String collection) throws Exception {
		SQLQuery sq = new SQLQuery("SELECT name FROM saada_class WHERE collection_id = " 
				+ Database.getCachemeta().getCollection(collection).getId());
		ResultSet rs = sq.run();
		ArrayList<String> retour = new ArrayList<String>();
		while( rs.next() ) {
			retour.add(rs.getString(1)); 
		}
		sq.close();
		return retour.toArray(new String[0]);

	}
	
	/**
	 * Add a stat column the the table
	 * @throws Exception
	 */
	public static final void addStatColumn() throws Exception {
		SQLTable.addStatColumn("saada_class");
	}

}
