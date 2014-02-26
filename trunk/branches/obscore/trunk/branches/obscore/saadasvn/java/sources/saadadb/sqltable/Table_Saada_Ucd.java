package saadadb.sqltable;

import saadadb.collection.Category;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;

public abstract class Table_Saada_Ucd extends SQLTable{
	
	/**
	 * @throws AbortException
	 * @throws CollectionException 
	 */
	public static  void createTable(int category) throws  SaadaException {
		String str_cat = Category.explain(category).toLowerCase();
		SQLTable.createTable("saada_ucd_" + str_cat,   "pk int, class_id int, name_class text, name_attr text, type_attr text, name_origin text NULL, name_ucd text NULL, name_utype text, vo_datamodel text, ass_error int, queriable boolean, type text, unit text NULL, comment text NULL, name_coll text, id_collection int"
				, "pk"
				, false);
		String table_name = "saada_ucd_" + str_cat;
		SQLTable.addQueryToTransaction("create index " + table_name.toLowerCase() + "_pk0 on " + table_name + "(class_id)", table_name);
		SQLTable.addQueryToTransaction("create index " + table_name.toLowerCase() + "_pk1 on " + table_name + "(name_attr)", table_name);
		SQLTable.addQueryToTransaction("create index " + table_name.toLowerCase() + "_pk2 on " + table_name + "(name_ucd)", table_name);
	}
	
}
