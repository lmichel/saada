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
import saadadb.util.AntTarget;
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
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{obs_id='prd_0'}", "-noindex"}));
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
		pm.remove(new ArgsParser(new String[]{"-remove=Select TABLE From * In Starting WhereAttributeSaada{obs_id='prd_0'}"
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
		pm.remove(new ArgsParser(new String[]{"-remove=Select ENTRY From * In Starting WhereAttributeSaada{obs_id LIKE 'prd_% 1 %'}"
				, "-noindex"}));
		SQLTable.commitTransaction();
		
		int e1 = Database.getCachemeta().getCollection("Starting").getSize(Category.ENTRY);
		if( e1 != (e0 - 3) ){
			QueryException.throwNewException(SaadaException.DB_ERROR, "simpleentry: " + e1 + " entries != of " + e0 + " - 3 entries");
		}
		System.out.println("simpleentry: OK " + e1 + " entries = "  + e0 + "-3 entries");
	}

	/**
	 * @throws Exception
	 */
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
				+ "FROM Starting_misc AS p, Ending_misc AS s WHERE s.obs_id = p.obs_id"
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
				+ "FROM Starting_misc AS p, Ending_entry AS s WHERE s.obs_id " 
				+ Database.getWrapper().getRegexpOp() +  " (" + Database.getWrapper().getStrcatOp("p.obs_id","'.*'") + ")"
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
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{obs_id='prd_0'}", "-noindex", "-links=follow"}));
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
		System.out.println("relationTestOnMisc: OK ");

	}
	/**
	 * @throws Exception
	 */
	public static void relationAntTestOnMisc() throws Exception {
		Database.getCachemeta().reload(true);
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
		Database.getCachemeta().reload(true);
		/*
		 * Relation MISC -> MISC
		 */
		AntTarget at = new AntTarget("relation.remove", "Remove relation MiscToMisc");
		at.setProperty("name", "MiscToMisc");
		at.execute(true);
		
		at = new AntTarget("relation.create", "Create relation MiscToMisc");
		at.setProperty("name", "MiscToMisc");
		at.setProperty("from", "Starting_misc");
		at.setProperty("to", "Ending_misc");
		at.setProperty("query", "INSERT INTO MiscToMisc (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
			                  	+ "FROM Starting_misc AS p, Ending_misc AS s WHERE s.obs_id = p.obs_id");
		at.execute(true);
		at = new AntTarget("relation.populate", "Populate relation MiscToMisc");
		at.setProperty("name", "MiscToMisc");
		at.execute(true);
		
		at = new AntTarget("relation.index", "Index relation MiscToMisc");
		at.setProperty("name", "MiscToMisc");
		at.execute(true);		
		int miscmiscSize = Database.getCachemeta().getRelation("MiscToMisc").getSize();
		/*
		 * relation MISC -> ENTRY
		 */
		at = new AntTarget("relation.remove", "Remove relation MiscToMisc");
		at.setProperty("name", "MiscToEntry");
		at.execute(true);
		
		at = new AntTarget("relation.create", "Create relation MiscToMisc");
		at.setProperty("name", "MiscToEntry");
		at.setProperty("from", "Starting_misc");
		at.setProperty("to", "Ending_ENTRY");
		at.setProperty("query", "INSERT INTO MiscToEntry (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
				+ "FROM Starting_misc AS p, Ending_entry AS s WHERE s.obs_id "
				+ Database.getWrapper().getRegexpOp() +  " (" + Database.getWrapper().getStrcatOp("p.obs_id","'.*'") + ")");
		at.execute(true);
		at = new AntTarget("relation.populate", "Populate relation MiscToEntry");
		at.setProperty("name", "MiscToEntry");
		at.execute(true);
		
		at = new AntTarget("relation.index", "Index relation MiscToEntry");
		at.setProperty("name", "MiscToEntry");
		at.execute(true);
		int miscentrySize = Database.getCachemeta().getRelation("MiscToEntry").getSize();
		/*
		 * remove products
		 */
		at = new AntTarget("product.remove", "Remove products");
		at.setProperty("remove", "Select MISC From * In Starting WhereAttributeSaada{obs_id='prd_0'}");
		at.setProperty("debug", "on");
		at.setProperty("noindex", "true");
		at.setProperty("links", "follow");
		at.execute(true);
//		RelationManager rm = new RelationManager("MiscToEntry");
//		SQLTable.beginTransaction();
//		rm.remove();
//		SQLTable.commitTransaction();
//		SQLTable.beginTransaction();
//		Database.getCachemeta().reload(true);
//		rm.create(new ArgsParser(new String[]{
//				"-from=Starting_misc",
//				"-to=Ending_ENTRY",
//				"-query=INSERT INTO MiscToEntry (oidprimary, oidsecondary) SELECT p.oidsaada, s.oidsaada "
//				+ "FROM Starting_misc AS p, Ending_entry AS s WHERE s.obs_id " 
//				+ Database.getWrapper().getRegexpOp() +  " (" + Database.getWrapper().getStrcatOp("p.obs_id","'.*'") + ")"
//				}));
//		rm.populate(null);
//		SQLTable.commitTransaction();
//		Database.getCachemeta().reload(true);
//		SQLTable.beginTransaction();
//		rm.index(null);
//		SQLTable.commitTransaction();
//		int miscentrySize = Database.getCachemeta().getRelation("MiscToEntry").getSize();
		
//		Database.getCachemeta().reload(true);
//		ProductManager pm = new ProductManager();
//		SQLTable.beginTransaction();
//		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting WhereAttributeSaada{obs_id='prd_0'}", "-noindex", "-links=follow"}));
//		SQLTable.commitTransaction();

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
		System.out.println("relationTestOnMisc: OK ");

	}

	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Database.setAdminMode(ap.getPassword());

		String command = ap.getCommand();
		boolean all = ("all".equals(command));
		boolean cmdok = false;
		if( all || command.equalsIgnoreCase("simplemisc") ) {
			cmdok = true;
			basicTestOnMisc();
		} 
		if( all || command.equalsIgnoreCase("simpletable")) {
			cmdok = true;
			basicTestOnTable(1);
		}
		if( all || command.equalsIgnoreCase("simpleentry")) {
			cmdok = true;
			basicTestOnEntry(1);
		} 
		if( all || command.equalsIgnoreCase("relationmisc")) {
			cmdok = true;
			relationTestOnMisc();
		} 
		if( all || command.equalsIgnoreCase("antrelationmisc")) {
			cmdok = true;
			relationAntTestOnMisc();
		} 
		if( !cmdok ) {
			System.out.println("USAGE  RemoteTester -command=[all|simplemisc|simpletable|simpleentry|simplemisc]");
			System.exit(1);
		}
		Database.close();
	}

}
