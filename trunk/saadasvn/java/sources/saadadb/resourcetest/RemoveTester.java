package saadadb.resourcetest;

import saadadb.collection.Category;
import saadadb.collection.ProductManager;
import saadadb.command.ArgsParser;
import saadadb.command.ManageRelation;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.DataGenerator;
import saadadb.util.Messenger;

public class RemoveTester {

	/**
	 * @throws Exception
	 */
	public static void basicTestOnMisc() throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "Generated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "Generated", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int t0 = Database.getCachemeta().getCollection("Starting").getSize(Category.MISC);
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{namesaada='prd_0'}", "-noindex"}));
		SQLTable.commitTransaction();
		int t1 = Database.getCachemeta().getCollection("Starting").getSize(Category.MISC);
		if( 3*t1 != 2*t0 ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "simplemisc" + t1 + "misc!= 2/3 of " + t0 + "misc");
		}
		System.out.println("simplemisc OK " +  t1 + "misc == 2/3 of " + t0 + "misc");
	}
	
	/**
	 * @param size
	 * @throws Exception
	 */
	public static void basicTestOnTable(int size) throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "Generated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "Generated", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int t0 = Database.getCachemeta().getCollection("Starting").getSize(Category.TABLE);
		int e0 = Database.getCachemeta().getCollection("Starting").getSize(Category.ENTRY);
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select TABLE From * In Starting WhereAttributeSaada{namesaada='prd_0'}"
				, "-noindex"}));
		SQLTable.commitTransaction();
		
		int t1 = Database.getCachemeta().getCollection("Starting").getSize(Category.TABLE);
		int e1 = Database.getCachemeta().getCollection("Starting").getSize(Category.ENTRY);
		if( 3*t1 != 2*t0 || 3*e1 != 2*e0) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "simpletable: " + t1 + "tables/" + e1 + " entries != 2/3 of " + t0 + "tables/" + e0 + " entries");
		}
		System.out.println("simpletable: OK " + t1 + "tables/" + e1 + " entries = 2/3 of " + t0 + "tables/" + e0 + " entries");
	}
	/**
	 * @param size
	 * @throws Exception
	 */
	public static void basicTestOnEntry(int size) throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "Generated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "Generated", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int e0 = Database.getCachemeta().getCollection("Starting").getSize(Category.ENTRY);
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select ENTRY From * In Starting WhereAttributeSaada{namesaada LIKE 'prd_% 1 %'}"
				, "-noindex"}));
		SQLTable.commitTransaction();
		
		int e1 = Database.getCachemeta().getCollection("Starting").getSize(Category.ENTRY);
		if( e1 != (e0 - 3) ){
			QueryException.throwNewException(SaadaException.DB_ERROR, "simpleentry: " + e1 + " entries != of " + e0 + " - 3 entries");
		}
		System.out.println("simpleentry: OK " + e1 + " entries = "  + e0 + "-3 entries");
	}

	public static void relationTestOnMisc() throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "StartData", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "StartData", "prd_" + i);
			dg.ingestVOTable(false);			
		}

		dg = new DataGenerator("0 +0", 0, "Ending", "MISC", "EndData", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Ending", "MISC", "EndData", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		RelationManager rm = new RelationManager("MiscToMisc");
		SQLTable.beginTransaction();
		rm.remove();
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		Database.getCachemeta().reload(true);
		rm.create(new ArgsParser(new String[]{
				"-from=Starting_MISC",
				"-to=Ending_MISC",
				"-query=INSERT INTO MiscToMisc (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
				+ "FROM Starting_MISC AS p, Ending_MISC AS s WHERE s.namesaada = p.namesaada"
				}));
		rm.populate(null);
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
		SQLTable.beginTransaction();
		rm.index(null);
		SQLTable.commitTransaction();
		
		System.out.println(Database.getCachemeta().getCollection("Starting").getSize(Category.MISC) + "starting");;
		System.out.println(Database.getCachemeta().getCollection("Ending").getSize(Category.MISC) + "ending");;
		
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{namesaada='prd_0'}", "-noindex", "-links=follow"}));
		SQLTable.commitTransaction();
		System.out.println(Database.getCachemeta().getCollection("Starting").getSize(Category.MISC) + "starting");;
		System.out.println(Database.getCachemeta().getCollection("Ending").getSize(Category.MISC) + "ending");;
		
	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Database.getConnector().setAdminMode(ap.getPassword());
		String command = ap.getComment();
		if( command.equals("simplemisc")) {
			basicTestOnMisc();
		} else if( command.equals("simpletable")) {
			basicTestOnTable(1);
		} else if( command.equals("simpleentry")) {
			basicTestOnEntry(1);
		} else if( command.equals("relationmisc")) {
			relationTestOnMisc();
		} else {
			System.out.println("Command " + command + " not undestood");
			System.exit(1);			
		}
	}

}
