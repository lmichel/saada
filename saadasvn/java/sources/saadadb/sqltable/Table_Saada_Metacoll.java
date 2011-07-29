package saadadb.sqltable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Vector;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.DefineType;

public class Table_Saada_Metacoll extends SQLTable {
	/** * @version $Id$

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
			addCollectionForCategory(coll_name, num_coll, i);
		}
	}

	/**
	 * @param coll_name
	 * @param str_cat
	 * @throws FatalException 
	 */
	private static void addCollectionForCategory(String coll_name, int num_coll,  int cat) throws FatalException {
		try {
			String str_cat = Category.NAMES[cat].toLowerCase();
			String data_table_name = Database.getWrapper().getCollectionTableName(coll_name, cat);;
			String meta_table_name = "saada_metacoll_" + str_cat;
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
			Class cls = Class.forName("generated." + Database.getDbname() + "." + Category.NAMES[cat] + DefineType.TYPE_EXTEND_COLL);
			String sql = "";
			Class _class = cls;
			Vector<Class> vt_class = new Vector<Class>();
			while ( !_class.getName().equals("java.lang.Object")  ) {
				vt_class.add(_class);
				_class = _class.getSuperclass();
			}
			String dumpfile = Repository.getTmpPath() + Database.getSepar()  + data_table_name + ".psql";
			BufferedWriter bustmpfile = new BufferedWriter(new FileWriter(dumpfile));
			for (int k = vt_class.size() - 1; k >= 0; k--) {
				Field fl[] = (vt_class.get(k)).getDeclaredFields();
				if (fl.length > 0) {
					for (int i = 0; i < fl.length; i++) {
						String fname = fl[i].getName();
						String ftype = fl[i].getType().getName().replace("java.lang.", "");
						if( ftype.equals("saadadb.meta.DMInterface")) {
							continue;
						}
						//System.out.println(vt_class.get(k).getName() + " FIELD " + fl[i].getName() + " " + ftype);
						AttributeHandler ah = new AttributeHandler();
						ah.setNameorg(DefineType.getCollection_name_org().get(fname));
						if( ah.getNameorg().length() == 0 ) {
							ah.setNameorg(fname);
						}
						ah.setNameattr(fname);
						ah.setType(ftype);
						ah.setQueriable(true);
						ah.setCollname(coll_name);
						ah.setUcd(DefineType.getCollection_ucds().get(fname));
						ah.setUnit(DefineType.getCollection_units().get(fname));
						if( cat == Category.SPECTRUM ) {
							ah.setVo_dm(DefineType.VO_SDM);
							ah.setUtype(DefineType.getColl_sdm_utypes().get(fname));
						}
						/*
						 * Extented attributes
						 */
						if( vt_class.get(k).getName().startsWith("generated") ) {
							ah.setLevel('E');
						}
						/*
						 * Native attributes
						 */
						else {
							ah.setLevel('N');						
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
						+ "Attribute managed by Saada\t"
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
				}
			}
			bustmpfile.close();
			/*
			 * Collection meta data are the same for all collections and are defined in the Saada DM
			 * or by user collection attr set at creation time. So they must be stored once for all collections
			 */
			squery = new SQLQuery();
			rs = squery.run("select count(*) from " + meta_table_name.toLowerCase() );
			while( rs.next()) {
				if( rs.getInt(1) == 0) {
					SQLTable.addQueryToTransaction(Database.getWrapper().lockTables(new String[]{meta_table_name}, new String[]{meta_table_name + " as a"}));
					SQLTable.addQueryToTransaction("LOADTSVTABLE " + meta_table_name + " -1 " + dumpfile.replaceAll("\\\\", "\\\\\\\\")) ;
							//"copy " + meta_table_name  + " from '"+ dumpfile.replaceAll("\\\\", "\\\\\\\\") + "'", false, meta_table_name);
					/*
					 * Associate errors on position with position
					 * These queries change the row order in the table, then the attribute order must
					 * be restored with an order by pk
					 */
					SQLTable.lockTables(new String[]{meta_table_name}, new String[]{meta_table_name + " as a"});
					SQLTable.addQueryToTransaction(Database.getWrapper().getUpdateWithJoin(meta_table_name
							, meta_table_name + " as a"
							, "a.name_coll = " + meta_table_name  + ".name_coll" 
							, meta_table_name
							, new String[]{"ass_error"}
							, new String[]{meta_table_name + ".pk"}
							, "a.name_attr = 'error_ra_csa' " + " AND " + meta_table_name + ".name_attr = 'pos_ra_csa'") );
					SQLTable.addQueryToTransaction(Database.getWrapper().getUpdateWithJoin(meta_table_name
							, meta_table_name + " as a"
							, "a.name_coll = " + meta_table_name  + ".name_coll" 
							, meta_table_name
							, new String[]{"ass_error"}
							, new String[]{meta_table_name + ".pk"}
							, "a.name_attr = 'error_dec_csa' " + " AND " + meta_table_name + ".name_attr = 'pos_dec_csa'") );
//					SQLTable.runQueryUpdateSQL("UPDATE " + meta_table_name  
//							+ "   SET ass_error = pk "
//							+ "  FROM " + meta_table_name + " a "
//							+ " WHERE a.name_coll = " + meta_table_name  + ".name_coll AND a.name_attr = 'error_ra_csa' "
//							+ "   AND " + meta_table_name + ".name_attr = 'pos_ra_csa'"
//							, false
//							, null );
//					SQLTable.runQueryUpdateSQL(" UPDATE " + meta_table_name  
//							+ "   SET ass_error = a.pk "
//							+ "  FROM " + meta_table_name + " a "
//							+ " WHERE a.name_coll =" + meta_table_name  + ".name_coll AND a.name_attr = 'error_dec_csa' "
//							+ "   AND " + meta_table_name + ".name_attr = 'pos_dec_csa'"
//							, false
//							, null);
				}
				break;
			}
			squery.close();
			SQLTable.createTable(data_table_name, sql, "oidsaada", false);

		} catch(Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
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
}
