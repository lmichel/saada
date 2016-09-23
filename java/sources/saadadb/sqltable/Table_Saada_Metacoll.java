package saadadb.sqltable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.configuration.CollectionAttributeExtend;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.vocabulary.DefineType;

public class Table_Saada_Metacoll extends SQLTable {
	/**
	 * @throws AbortException
	 * @throws CollectionException 
	 */
	public static  void createTables() throws  SaadaException {
		String[] categories = Category.NAMES;
		for( int i=1 ; i<categories.length ; i++ ) {
			String str_cat = Category.explain(categories[i]).toLowerCase();
			SQLTable.createTable("saada_metacoll_" + str_cat,   "pk int, level character, class_id int NULL, name_class text NULL, name_attr " 
					+ Database.getWrapper().getIndexableTextType() + ", type_attr text, name_origin text NULL, ucd text NULL, utype text, vo_datamodel text, ass_error int, queriable boolean, unit text NULL, comment text NULL, name_coll " 
					+ Database.getWrapper().getIndexableTextType() + ", id_collection int, format text NULL"
					, "name_attr, name_coll"
					, false);
		}
	}

	/**
	 * @param coll_name
	 * @throws FatalException 
	 */
	public static void addCollection(String coll_name, int num_coll) throws FatalException {
		for( int i=1 ; i<Category.NB_CAT ; i++ ) {
			addCollectionForCategory(coll_name, i);
		}
	}

	/**
	 * Create the datatable category level for that collection.
	 * Populate the metacoll table if needed
	 * @param coll_name
	 * @param str_cat
	 * @throws FatalException 
	 */
	private static void addCollectionForCategory(String coll_name,  int cat) throws FatalException {
		try {
			String str_cat = Category.NAMES[cat].toLowerCase();
			//  data table for that collection/category
			String data_table_name = Database.getWrapper().getCollectionTableName(coll_name, cat);;
			// Meta datatable for that category
			String meta_table_name = "saada_metacoll_" + str_cat;
			String dumpfile = Repository.getTmpPath() + Database.getSepar()  + meta_table_name + ".psql";
			String sql = buildCollectionDump(cat, dumpfile);
			/*
			 * Populate the metacoll table if it is empty
			 */
			storeCollectionDump(meta_table_name, dumpfile);
			/*
			 * Create the data table. done here because we got the format here
			 */
			SQLTable.createTable(data_table_name, sql, "oidsaada", false);
		} catch(SaadaException e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}  catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	public static void addAttributeForCategory(int cat, AttributeHandler attributeHandler) throws Exception {
		/*
		 * Populate the metacoll table if needed; That can occurs if the new extended attribute is 
		 * added before the first collection is created
		 */
		String str_cat = Category.explain(cat).toLowerCase();
		String meta_table_name = "saada_metacoll_" + str_cat;
		String dumpfile = Repository.getTmpPath() + Database.getSepar()  + meta_table_name + ".psql";
		buildCollectionDump(cat, dumpfile);
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("SELECT name_attr from saada_metacoll_" + str_cat + " where name_attr = '" + attributeHandler.getNameattr() + "'");
		while( rs.next() ) {
			squery.close();
			QueryException.throwNewException(SaadaException.DB_ERROR, "Attribute " + attributeHandler.getNameattr() + " is already referenced in table " + meta_table_name );
		}
		squery.close();
		/*
		 * Compute the key (likely no longer useful)
		 */
		squery = new SQLQuery();
		rs = squery.run("SELECT max(pk) from " + meta_table_name);
		int max_key=0;
		while( rs.next() ) {
			max_key = rs.getInt(1) + 1;
		}
		/*
		 * Add the new attribute to the metacoll table
		 */
		squery.close();
		attributeHandler.setLevel('E');		
		SQLTable.addQueryToTransaction(
				"INSERT INTO " + meta_table_name + " VALUES ("
				+ max_key + ", '"
				+ attributeHandler.getLevel() + "', "
				+ "null , '"
				+ str_cat + "UserColl" + "', '"
				+ attributeHandler.getNameattr() + "', '"
				+ attributeHandler.getType() + "', '" 
				+ attributeHandler.getNameorg() + "', '"
				+ attributeHandler.getUcd() + "', '"
				+ attributeHandler.getUtype() + "', '"
				+ attributeHandler.getVo_dm() + "', "
				+ "null,"
				+ Database.getWrapper().getBooleanAsString(true) + ", '"
				+ attributeHandler.getUnit()+ "', '"
				+  Database.getWrapper().getEscapeQuote(attributeHandler.getComment()) + " ', '"
				+ "Generic', "
				+ "-1,"
				+ "null)");
	}

	/**
	 * remove the attribute from the metacoll table
	 * @param cat
	 * @param attributeHandler
	 * @throws Exception
	 */
	public static void removeAttributeForCategory(int cat, AttributeHandler attributeHandler) throws Exception {
		/*
		 * Populate the metacoll table if needed; That can occurs if the new extended attribute is 
		 * added before the first collection is created
		 */
		String str_cat = Category.explain(cat).toLowerCase();
		String meta_table_name = "saada_metacoll_" + str_cat;
		String dumpfile = Repository.getTmpPath() + Database.getSepar()  + meta_table_name + ".psql";
		buildCollectionDump(cat, dumpfile);
		/*
		 * Remove the  attribute from the metacoll table
		 */
		attributeHandler.setLevel('E');		
		SQLTable.addQueryToTransaction(
				"DELETE FROM saada_metacoll_" + str_cat 
				+ " WHERE name_attr = '" + attributeHandler.getNameattr() + "'");
	}
	/**
	 * remove the attribute from the metacoll table
	 * @param cat
	 * @param attributeHandler
	 * @throws Exception
	 */
	public static void renameAttributeForCategory(int cat, AttributeHandler oldAattributeHandler, AttributeHandler newAattributeHandler) throws Exception {
		/*
		 * Populate the metacoll table if needed; That can occurs if the new extended attribute is 
		 * added before the first collection is created
		 */
		String str_cat = Category.explain(cat).toLowerCase();
		String meta_table_name = "saada_metacoll_" + str_cat;
		String dumpfile = Repository.getTmpPath() + Database.getSepar()  + meta_table_name + ".psql";
		buildCollectionDump(cat, dumpfile);
		/*
		 * Rename the  attribute from the metacoll table
		 */
		SQLTable.addQueryToTransaction(
				"UPDATE saada_metacoll_" + str_cat 
				+ " SET name_attr='" + newAattributeHandler.getNameattr() + "',"
				+ " name_origin='" + newAattributeHandler.getNameattr() + "',"
				+ " comment='" + Database.getWrapper().getEscapeQuote(newAattributeHandler.getComment()) + "',"
				+ " ucd='" + newAattributeHandler.getUcd() + "',"
				+ " utype='" + newAattributeHandler.getType() + "',"
				+ " unit='" + newAattributeHandler.getUnit() + "'"
				+ " WHERE name_attr = '" + oldAattributeHandler.getNameattr() + "'");
	}

	/**
	 * just change comment, unit, ucd and utype on the metacoll table
	 * @param cat
	 * @param oldAattributeHandler
	 * @throws Exception
	 */
	public static void modifyAttributeForCategory(int cat, AttributeHandler attributeHandler) throws Exception {
		/*
		 * Populate the metacoll table if needed; That can occurs if the new extended attribute is 
		 * added before the first collection is created
		 */
		String str_cat = Category.explain(cat).toLowerCase();
		String meta_table_name = "saada_metacoll_" + str_cat;
		String dumpfile = Repository.getTmpPath() + Database.getSepar()  + meta_table_name + ".psql";
		buildCollectionDump(cat, dumpfile);
		/*
		 * modify the  attribute from the metacoll table
		 */
		SQLTable.addQueryToTransaction(
				"UPDATE saada_metacoll_" + str_cat 
				+ " SET comment='" + Database.getWrapper().getEscapeQuote(attributeHandler.getComment()) + "',"
				+ " ucd='" + attributeHandler.getUcd() + "',"
				+ " utype='" + attributeHandler.getUtype() + "',"
				+ " unit='" + attributeHandler.getUnit() + "'"
				+ " WHERE name_attr = '" + attributeHandler.getNameattr() + "'");
	}


	/**
	 * @param coll_name
	 * @param str_cat
	 * @throws FatalException 
	 */
	private static void storeCollectionDump(String meta_table_name, String dumpfile) throws Exception {
		/*
		 * Collection meta data are the same for all collections and are defined in the Saada DM
		 * or by user collection attr set at creation time. So they must be stored once for all collections
		 */
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select count(*) from " + meta_table_name.toLowerCase() );
		while( rs.next()) {
			if( rs.getInt(1) == 0) {
				SQLTable.addQueryToTransaction(Database.getWrapper().lockTables(new String[]{meta_table_name}, new String[]{meta_table_name + " as a"}));
				SQLTable.addQueryToTransaction("LOADTSVTABLE " + meta_table_name + " -1 " + dumpfile.replaceAll("\\\\", "\\\\\\\\")) ;
				//"copy " + meta_table_name  + " from '"+ dumpfile.replaceAll("\\\\", "\\\\\\\\") + "'", false, meta_table_name);
				/*
				 * Associate errors on position with position
				 * These queries change the row order in the table, then the attribute order must
				 * be restored with an order by pk
				 * Does nothing with SQLITE
				 */
				SQLTable.lockTables(new String[]{meta_table_name}, new String[]{meta_table_name + " as a"});
				SQLTable.addQueryToTransaction(Database.getWrapper().getUpdateWithJoin(meta_table_name
						, meta_table_name + " as a"
						, "a.name_coll = " + meta_table_name  + ".name_coll" 
						, meta_table_name
						, new String[]{"ass_error"}
				, new String[]{meta_table_name + ".pk"}
				, "a.name_attr = 'error_ra_csa' " + " AND " + meta_table_name + ".name_attr = 's_ra'") );
				SQLTable.addQueryToTransaction(Database.getWrapper().getUpdateWithJoin(meta_table_name
						, meta_table_name + " as a"
						, "a.name_coll = " + meta_table_name  + ".name_coll" 
						, meta_table_name
						, new String[]{"ass_error"}
				, new String[]{meta_table_name + ".pk"}
				, "a.name_attr = 'error_dec_csa' " + " AND " + meta_table_name + ".name_attr = 's_dec'") );
			}
			break;
		}
		squery.close();
	}

	/**
	 * @param cat number of the category
	 * @param dumpfile full path of the file where the content of the dump of the metacoll table is stored
	 * @return an SQL description for the colunms of the data table for that category
	 * @throws FatalException
	 */
	@SuppressWarnings("rawtypes")
	private static String buildCollectionDump(int cat, String dumpfile) throws Exception {
		Map<String, AttributeHandler> ahs = (new CollectionAttributeExtend(Database.getRoot_dir())).getAttrSaada(Category.explain(cat));
		String str_cat = Category.explain(cat).toLowerCase();
		/*
		 * Mysql requires all table accessed during a transaction to be locked
		 */
		SQLTable.lockTable("saada_metacoll_" + str_cat);
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("SELECT max(pk) from saada_metacoll_" + str_cat);
		int max_key=0;
		while( rs.next() ) {
			max_key = rs.getInt(1) + 1;
		}
		squery.close();
		BufferedWriter bustmpfile = new BufferedWriter(new FileWriter(dumpfile));
		String sql = "";
		Class cls = Class.forName("generated." + Database.getDbname() + "." + Category.NAMES[cat] + DefineType.TYPE_EXTEND_COLL);
		List<Field> lf = ((SaadaInstance) cls.newInstance()).getAllPersistentFields();
		for( Field f: lf) {
			String fname = f.getName();
			String ftype = f.getType().getName().replace("java.lang.", "");
			if( ftype.equals("saadadb.meta.DMInterface") || ftype.equals("saadadb.collection.obscoremin.VignetteFile") ) {
				continue;
			}
			/*
			 * Create one attribute handler per row
			 */
			AttributeHandler  ah;
			/*
			 * Extended attributes are tagged E
			 */
			if( (ah = ahs.get(fname)) != null ){
				ah.setLevel('E');							
				/*
				 * Build-in attributes are tagged N
				 */
			} else {
				ah = new AttributeHandler();
				ah.setNameorg(DefineType.getCollection_name_org().get(fname));
				if( ah.getNameorg().length() == 0 ) {
					ah.setNameorg(fname);
				}
				ah.setNameattr(fname);
				ah.setType(ftype);
				ah.setQueriable(true);
				ah.setCollname("Generic");
				ah.setUcd(DefineType.getCollection_ucds().get(fname));
				ah.setUnit(DefineType.getCollection_units().get(fname));
				ah.setComment("Attribute managed by Saada");
				ah.setUtype(DefineType.getColl_sdm_utypes().get(fname));
				ah.setLevel('N');						
			} 
			/*
			 * Add specific utype for spectra, the only category supporting a model yet.
			 */
			//TODO must be extend to other categories
			if( cat == Category.SPECTRUM ) {
				ah.setVo_dm(DefineType.VO_SDM);
				ah.setUtype(DefineType.getColl_sdm_utypes().get(fname));
			}

			String dumpline = max_key + "\t"
			+ ah.getLevel() + "\t"
			+ Database.getWrapper().getAsciiNull() + "\t"
			///						+ "\\N\t" 
			+ str_cat + "UserColl" + "\t"
			+ ah.getNameattr() + "\t"
			+ ah.getType() + "\t" 
			+ ah.getNameorg() + "\t"
			+ ah.getUcd() + "\t"
			+ ah.getUtype() + "\t"
			+ ah.getVo_dm() + "\t"
			+ Database.getWrapper().getAsciiNull() + "\t"
			///						+ "\\N\t"
			+ Database.getWrapper().getBooleanAsString(true) + "\t"
			+ ah.getUnit()+ "\t"
			+ ah.getComment() + " \t"
			//						+ coll_name + "\t"
			//						+ num_coll + "\t"
			+ "Generic\t"
			+ "-1\t"
			+Database.getWrapper().getAsciiNull();
			///						+ "\\N";
			bustmpfile.write(dumpline + "\n");
			max_key++;
			if( sql.length() > 0  ) {
				sql += ", ";
			}
			sql += ah.getNameattr() + " " + Database.getWrapper().getSQLTypeFromJava(ftype);
		}
		bustmpfile.close();
		return sql;
	}

	/**
	 * Drop collection attribute from the saada_metacoll_* table and drop collection tables
	 * Class table are supposed to be already removed
	 * @param coll_name
	 * @throws FatalException 
	 */
	public static void dropCollection(String coll_name) throws FatalException {
		try {
			for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
				String str_cat = Category.NAMES[cat].toLowerCase();
				SQLTable.addQueryToTransaction("DELETE FROM saada_metacoll_" + str_cat + " WHERE  name_coll = '" + coll_name + "'", "saada_metacoll_" + str_cat);
			}
		} catch(Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	public static void main(String[] args ) throws Exception{
//		Database.init("Obscore");
//		SQLTable.beginTransaction();
//		buildCollectionDump(Category.TABLE, "/dev/null");
		Messenger.debug_mode =true;
		Database.init("saadaObscore");
		Database.setAdminMode(null);
		SQLTable.beginTransaction();
		//dropTable("VizierData_IMAGE");
		dropTable("VizierData_SPECTRUM");
		SQLTable.commitTransaction();
		Messenger.printMsg(Messenger.DEBUG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		SQLTable.beginTransaction();
		
		//SQLTable.addQueryToTransaction("DELETE FROM saada_metacoll_image");
		SQLTable.addQueryToTransaction("DELETE FROM saada_metacoll_spectrum");
		SQLTable.commitTransaction();
		Messenger.printMsg(Messenger.DEBUG, "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		
		SQLTable.beginTransaction();
		addCollectionForCategory("VizierData", Category.SPECTRUM);
		SQLTable.commitTransaction();
		Database.close();
	}
}
