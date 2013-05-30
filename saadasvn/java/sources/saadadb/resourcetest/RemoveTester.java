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
		DataGenerator dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "MiscGenerated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "MiscGenerated", "prd_" + i);
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
		DataGenerator dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "TableGenerated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "TableGenerated", "prd_" + i);
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
		DataGenerator dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "TableGenerated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", size, "Starting", "TABLE", "TableGenerated", "prd_" + i);
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
		/*
		 * Load starting collection
		 */
		DataGenerator dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "StartData", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "StartData", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int startsize = Database.getCachemeta().getCollection("Starting").getSize(Category.MISC);
		/*
		 * Load ending misc collection
		 */
		
		dg = new DataGenerator("0 +0", 0, "Ending", "MISC", "EndData", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Ending", "MISC", "EndData", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int endindmiscsize = Database.getCachemeta().getCollection("Ending").getSize(Category.MISC);
		/*
		 * Load ending tables
		 */
		dg = new DataGenerator("0 +0", 1, "Ending", "TABLE", "EndTable", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 1, "Ending", "TABLE", "EndTable", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		int endindentrysize = Database.getCachemeta().getCollection("Ending").getSize(Category.ENTRY);
		/*
		 * Relation MISC -> MISC
		 */
		RelationManager rm = new RelationManager("MiscToMisc");
		SQLTable.beginTransaction();
		rm.remove();
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		Database.getCachemeta().reload(true);
		rm.create(new ArgsParser(new String[]{
				"-from=Starting_misc",
				"-to=Ending_misc",
				"-query=INSERT INTO MiscToMisc (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
				+ "FROM Starting_misc AS p, Ending_misc AS s WHERE s.namesaada = p.namesaada"
				}));
		rm.populate(null);
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
		SQLTable.beginTransaction();
		rm.index(null);
		SQLTable.commitTransaction();
		int miscmiscSize = Database.getCachemeta().getRelation("MiscToMisc").getSize();
		/*
		 * relation MISC -> ENTRY
		 */
		rm = new RelationManager("MiscToEntry");
		SQLTable.beginTransaction();
		rm.remove();
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		Database.getCachemeta().reload(true);
		rm.create(new ArgsParser(new String[]{
				"-from=Starting_misc",
				"-to=Ending_ENTRY",
				"-query=INSERT INTO MiscToEntry (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
				+ "FROM Starting_misc AS p, Ending_entry AS s WHERE s.namesaada " + Database.getWrapper().getRegexpOp() + " (p.namesaada || '.*')"
				}));
		rm.populate(null);
		SQLTable.commitTransaction();
		Database.getCachemeta().reload(true);
		SQLTable.beginTransaction();
		rm.index(null);
		SQLTable.commitTransaction();
		int miscentrySize = Database.getCachemeta().getRelation("MiscToEntry").getSize();
		
		Database.getCachemeta().reload(true);
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{namesaada='prd_0'}", "-noindex", "-links=follow"}));
		SQLTable.commitTransaction();

		int fstartsize = Database.getCachemeta().getCollection("Starting").getSize(Category.MISC);
		int fendindmiscsize = Database.getCachemeta().getCollection("Ending").getSize(Category.MISC);
		int fendindentrysize = Database.getCachemeta().getCollection("Ending").getSize(Category.ENTRY);
		int fmiscmiscSize = Database.getCachemeta().getRelation("MiscToMisc").getSize();
		int fmiscentrySize = Database.getCachemeta().getRelation("MiscToEntry").getSize();
		if( (startsize - fstartsize) != 1 ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "Starting collection should be set from " + startsize 
					+ " to (" + startsize + " -1)(!=" + fstartsize + ")");
		}
		if( (endindmiscsize - fendindmiscsize) != 1 ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "Ending misc collection should be set from " + endindmiscsize 
					+ " to (" + endindmiscsize + " -1)(!=" + fendindmiscsize + ")");
		}
		if( endindentrysize != fendindentrysize ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "Ending entry collection should be set from " + endindentrysize 
					+ " to " + endindentrysize + " (!=" + fendindentrysize + ")");
		}
		if( (2*miscmiscSize) != (3*fmiscmiscSize) ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "Misc -> Misc RELATION SIZE should be set from " + miscmiscSize 
					+ " to " + (2*miscmiscSize)/3 + " (!=" + fmiscmiscSize + ")");
		}
		if( (2*miscentrySize) != (3*fmiscentrySize) ) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "Misc -> Entry RELATION SIZE should be set from " + miscentrySize 
					+ " to " + (2*miscentrySize)/3 + " (!=" + fmiscentrySize + ")");
		}

	}
	
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Database.getConnector().setAdminMode(ap.getPassword());
		String command = ap.getCommand();
		boolean all = ("all".equals(command));
		boolean cmdok = false;
		if( all || command.endsWith("simplemisc") ) {
			cmdok = true;
			basicTestOnMisc();
		} 
		if( all || command.endsWith("simpletable")) {
			cmdok = true;
			basicTestOnTable(1);
		}
		if( all || command.endsWith("simpleentry")) {
			cmdok = true;
			basicTestOnEntry(1);
		} 
		if( all || command.endsWith("relationmisc")) {
			cmdok = true;
			relationTestOnMisc();
		} 
		if( !cmdok ) {
			System.out.println("USAGE  RemoteTester -command=[all|simplemisc|simpletable|simpleentry|simplemisc]");
			System.exit(1);
		}
	}

}
