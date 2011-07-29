package saadadb.sqltable;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

public abstract class Table_Saada_Metaclass extends SQLTable{
	
	/** * @version $Id$

	 * @throws AbortException
	 * @throws CollectionException 
	 */
	public static  void createTables() throws  SaadaException {
		String[] categories = Category.NAMES;
		for( int i=1 ; i<categories.length ; i++ ) {
			String str_cat = Category.explain(categories[i]).toLowerCase();
			SQLTable.createTable("saada_metaclass_" + str_cat,   "pk " + Database.getWrapper().getSerialToken() + " , level character, class_id int, name_class text, name_attr " 
					          + Database.getWrapper().getIndexableTextType() + ", type_attr text, name_origin text NULL, ucd " 
					          + Database.getWrapper().getIndexableTextType() + " NULL, utype text, vo_datamodel text, ass_error int, queriable boolean, unit text NULL, comment text NULL, name_coll text, id_collection int, format text NULL"
					          , "pk"
					          , false);
			SQLTable.addQueryToTransaction("create index saada_metaclass_" + str_cat.toLowerCase() + "_pk0 on saada_metaclass_" + str_cat + "(class_id)", "saada_metaclass_" + str_cat);
			SQLTable.addQueryToTransaction("create index saada_metaclass_" + str_cat.toLowerCase() + "_pk1 on saada_metaclass_" + str_cat + "(name_attr)", "saada_metaclass_" + str_cat);
			SQLTable.addQueryToTransaction("create index saada_metaclass_" + str_cat.toLowerCase() + "_pk2 on saada_metaclass_" + str_cat + "(ucd)", "saada_metaclass_" + str_cat);
		}
	}
	
	/**
	 * @param classe
	 * @throws FatalException
	 */
	public static void removeClass(String classe) throws FatalException {
		String table_name = "saada_metaclass_" + Database.getCachemeta().getClass(classe).getCategory_name().toLowerCase();	
		SQLTable.addQueryToTransaction("DELETE FROM " + table_name + " WHERE name_class ='" + classe + "'", table_name);
		
	}
}
