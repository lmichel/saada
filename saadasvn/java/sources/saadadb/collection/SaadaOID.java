package saadadb.collection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.sqltable.SQLQuery;

/**
 * @author michel
 *
 */
public abstract class SaadaOID {
	/*
	 * This field avoids to run a query each time a new oid is asked. 
	 * That is also necessary because current OID values can not be taken from the DB as 
	 * the transaction is executed at the end if the loading, thus after OIDS are used
	 * Works only with one single dataloader
	 */
	static final HashMap<String, Long> last_oid_for_class = new HashMap<String, Long>();
//	static final long last_oid_for_class;
//	static final String last_class = "";
	//32 row_num
	//16 class
	//10 collection
	//4 category
	//2 unused

	/**
	 * Used to build SQL queries
	 * @param col_name
	 * @return
	 */
	public static final String getSQLClassFilter(String col_name) {
		return "((" + col_name + " >> 32) & 65535)";
	}

	/**
	 * Used to build SQL queries
	 * @param col_name
	 * @return
	 */
	public static final String getSQLCollectionilter(String col_name) {
		return "((" + col_name + " >> 48) & 1023)";
	}

	/**
	 * Used to build SQL queries
	 * @param col_name
	 * @return
	 */
	public static final String getSQLCategoryFilter(String col_name) {
		return "((" + col_name + " >> 58) & 15)";
	}

	/**
	 * Used to build SQL queries
	 * @param col_name
	 * @return
	 */
	public static final String getSQLrowFilter(String col_name) {
		return "(" + col_name + " & 4294967295)";
	}
	
	/**
	 * Return a String matching the tree path of the oid
	 * COLECTION.CAT.CLASS
	 * @param oid
	 * @return
	 */
	public static final String getTreePath(long oid){
		return getCollectionName(oid) + "." + getCategoryName(oid) + "." + getClassName(oid);
	}
	/**
	 * @param oid
	 * @return
	 */
	public static final int getClassNum(long oid) {
		return (int)((oid >> 32) & 0xffffL);
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final String getClassName(long oid) {
		try {
			return Database.getCachemeta().getClass((int)((oid >> 32) & 0xffffL)).getName();
		} catch (FatalException e) {
			return null;
		}
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final int getCollectionNum(long oid) {
		return (int)((oid >> 48) & 0x3ffL);
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final String getCollectionName(long oid) {
		try {
			return Database.getCachemeta().getCollection((int)((oid >> 48) & 0x3ffL)).getName();
		} catch (FatalException e) {
			return null;
		}
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final int getCategoryNum(long oid) {
		return (int)((oid >> 58) & 0xfL);
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final String getCategoryName(long oid) {
		try {
			return Category.explain((int)((oid >> 58) & 0xfL));
		} catch (FatalException e) {
			return null;
		}
	}

	/**
	 * @param oid
	 * @return
	 */
	public static final int getRowNum(long oid) {
		return (int)(oid  & 0xffffffffL);
	}

	/**
	 * Generator Oid
	 * @param nameClass
	 * @exception SaadaException
	 */
	public static final long newOid(String class_name)throws Exception, SaadaException{
		Long current_oid = last_oid_for_class.get(class_name);
		if( current_oid == null ) {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("select max(oidsaada) from "+class_name);
			if(rs.next() && (current_oid = rs.getLong(1)) != 0 ){
			}
			else {
				current_oid = getMaskForClass(class_name);
			}
			squery.close();
		}
		current_oid++;
		last_oid_for_class.put(class_name, current_oid);
		
		return current_oid;
	}
	
	/**
	 * Called when a class is removed: suppress the oid counter for taht class
	 * @param className
	 */
	public static final void  resetCounter(String className) {
		last_oid_for_class.remove(className);
	}
	
	/**
	 * Generator Oid
	 * @param nameClass
	 * @throws SaadaException 
	 * @throws SQLException 
	 * @exception SaadaException
	 */
	public static final long newFlatFileOid (String coll_name) throws Exception {
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select max(oidsaada) from "+ Database.getCachemeta().getCollectionTableName(coll_name, Category.FLATFILE));
		long val;
		if(rs.next() && (val = rs.getLong(1)) != 0 ){
			squery.close();
			return val + 1;
		}
		else {
			squery.close();
			MetaCollection mc = Database.getCachemeta().getCollection(coll_name);
			/*
			 * Class id = 1 is reserved for flatfiles
			 */
			return ((long)((1 & 0xffff) | ((mc.getId() & 0x3f)<< 16) | ((Category.FLATFILE& 0xf) << 26))) << 32;
		}   	
	}
	/**
	 * @param class_name
	 * @return
	 * @throws Exception
	 */
	public static final long getMaskForClass(String class_name) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(class_name);
		return ((long)((mc.getId() & 0xffff) | ((mc.getCollection_id() & 0x3f) << 16) | ((mc.getCategory() & 0xf) << 26))) << 32;
	}
}
