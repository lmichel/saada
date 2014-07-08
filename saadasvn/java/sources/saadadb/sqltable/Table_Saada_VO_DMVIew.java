package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.meta.MetaClass;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;
import cds.astro.Coo;
import cds.astro.Qbox;

/**
 * This static class handles current operation on an SQL table representing a view on a DM.
 * Note we are not using views but a data copy. This point of view could change later.
 * The view populating rely on a mapping saved in an xml file in the config directory
 * 
 * @author michel
 * @version $Id$
 *
 */
public class Table_Saada_VO_DMVIew extends SQLTable {


	/**
	 * Create the table from the Utypes of the VOResource
	 * Columns oidsaada and sky_pîxel_csa are prepended to the table.
	 * oidsaada allows to retrieve the data into the SaadaDB 
	 * sky_pixel_csa allows to use the optimized positional searches.
	 * @param vor
	 * @throws Exception
	 */
	public static  void createTable(VOResource vor) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Create table " + vor.getName());
		/*
		 * Both columns are forced in order to keep the table well connected with Saada
		 */
		String createStmt = "oidsaada " + Database.getWrapper().getSQLTypeFromJava("long")
		+ ", sky_pixel_csa " + Database.getWrapper().getSQLTypeFromJava("long");
		for( UTypeHandler uth: vor.getUTypeHandlers()) {
			createStmt += ", " + uth.getNickname() + " " + Database.getWrapper().getSQLTypeFromJava(uth.getAttributeHandler().getType());
		}
		SQLTable.createTable(vor.getName()
				,createStmt
				, null
				, false);

	}

	/**
	 * Store the data of the class className into the table according to the mapping 
	 * read in the config file by the VOResource.
	 * @param vor
	 * @param className
	 * @throws Exception
	 */
	public static  void addClass(VOResource vor, String className) throws Exception {		
		Map<String, String> mapping = vor.readClassMapping(className);
		MetaClass mc = Database.getCachemeta().getClass(className);
		String query = "INSERT INTO " + vor.getName() + "(";
		String cn="oidsaada ", ca=className + ".oidsaada AS oidsaada ";
		switch(mc.getCategory()) {
		case Category.ENTRY:
		case Category.SPECTRUM:
		case Category.IMAGE: cn += ", sky_pixel_csa"; ca += ", sky_pixel_csa AS sky_pixel_csa";
		break;
		default: break;
		}
		for( Entry<String, String> entry: mapping.entrySet()) {
			cn += "\n    , " + entry.getKey();
			ca += "\n     , " + entry.getValue() + " AS " + entry.getKey();
		}
		String ct = Database.getCachemeta().getCollectionTableName(mc.getCollection_name(), mc.getCategory());
		query += cn + ")\nSELECT " + ca + " ";
		query += "\nFROM "+ className ;
		query += "\nINNER JOIN " + ct + " ON  " + ct + ".oidsaada = " + className + ".oidsaada";

		SQLTable.addQueryToTransaction(query);
	}

	/**
	 * @param vor
	 * @param className
	 * @param raCol
	 * @param devCol
	 * @throws Exception 
	 */
	public static void ComputeSkyPixels(VOResource vor, String className, String raCol, String decCol) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Compute sky pixels for class " + className + " in DM view "  + vor.getName());
		int mask = (int)(SaadaOID.getMaskForClass(className) >> 32);
		SQLQuery sqlquery = new SQLQuery();
		ResultSet rs = sqlquery.run("SELECT sky_pixel_csa, " +  raCol + ", "  + decCol 
				+ " FROM " + vor.getName() 
				+ " WHERE oidsaada >> 32 = " + mask  + " AND "
				+   raCol + " IS NULL AND  "  + decCol + " IS NOT NULL");
		while( rs.next() ) {
			rs.updateLong(1, (new Qbox(new Coo(rs.getDouble(2), rs.getDouble(3)))).box());
		}
		rs.close();	
	}

	/**
	 * Returns true id the table mapping the VO resource (DM) vor as at least one row with a record
	 * coming from data of class className
	 * @param vor
	 * @param className
	 * @return
	 * @throws Exception 
	 */
	public static boolean isClassReferenced(VOResource vor, String className) throws Exception {
		if( ! SQLTable.tableExist( vor.getName() )) {
			return false;
		}
		int mask = (int)(SaadaOID.getMaskForClass(className) >> 32);
		SQLQuery sqlquery = new SQLQuery();
		ResultSet rs = sqlquery.run("SELECT * " 
				+ " FROM " + vor.getName() 
				+ " WHERE oidsaada >> 32 = " + mask  + " LIMIT 1");
		while( rs.next() ) {
			rs.close();
			return true;
		}
		rs.close();
		return false;
	}
	
	/**
	 * @param vor
	 * @param className
	 * @return
	 * @throws Exception
	 */
	public static String getClassDataQuery(VOResource vor, String className) throws Exception {
		int mask = (int)(SaadaOID.getMaskForClass(className) >> 32);
		return "SELECT * " 
				+ " FROM " + vor.getName() 
				+ " WHERE oidsaada >> 32 = " + mask ;
		
	}

	/**
	 * Retirns a collection of all classes referenced by the DM view
	 * @param vor
	 * @return
	 * @throws Exception
	 */
	public static Collection<String> getReferencedClasses(VOResource vor) throws Exception {
		ArrayList<String> retour = new ArrayList<String>();
		if( ! SQLTable.tableExist( vor.getName() )) {
			return retour;
		}
		SQLQuery sqlquery = new SQLQuery();
		ResultSet rs = sqlquery.run("SELECT DISTINCT ((oidsaada >> 32) & 65535)" 
				+ " FROM " + vor.getName() );
		while( rs.next() ) {
			retour.add(Database.getCachemeta().getClass(rs.getInt(1)).getName());
		}
		sqlquery.close();
		return retour;

	}

	/**
	 * Remove all rows coming from th class className
	 * @param vor
	 * @param className
	 * @throws Exception
	 */
	public static void removeClass(VOResource vor, String className) throws Exception {
		int mask = (int)(SaadaOID.getMaskForClass(className) >> 32);
		if( SQLTable.tableExist( vor.getName() )) {
			SQLTable.addQueryToTransaction("DELETE FROM " + vor.getName() + " WHERE oidsaada >> 32 = " + mask);
		}
	}

	/**
	 * @param vor
	 * @throws AbortException
	 */
	public static void emptyTable(VOResource vor) throws AbortException {
		if( SQLTable.tableExist( vor.getName() )) {
			SQLTable.addQueryToTransaction("DELETE FROM " + vor.getName());
		}

	}
	/**
	 * @param vor
	 * @throws AbortException
	 */
	public static void dropTable(VOResource vor) throws AbortException {
		SQLTable.dropTable(vor.getName());
	}		

}