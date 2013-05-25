package saadadb.resourcetest;

import saadadb.collection.ProductManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.sqltable.SQLTable;
import saadadb.util.DataGenerator;

public class RemoveTester {

	public static void basicTestOnMisc() throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "Generated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 0, "Starting", "MISC", "Generated", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select MISC From * In Starting", "-noindex"}));
		SQLTable.commitTransaction();
	}
	public static void basicTestOnTable() throws Exception {
		DataGenerator dg = new DataGenerator("0 +0", 1, "Starting", "TABLE", "Generated", "prd_0");
		dg.ingestVOTable(true);
		for( int i=1 ; i<3 ; i++ ){
		    dg = new DataGenerator("0 +0", 1, "Starting", "TABLE", "Generated", "prd_" + i);
			dg.ingestVOTable(false);			
		}
		ProductManager pm = new ProductManager();
		SQLTable.beginTransaction();
		pm.remove(new ArgsParser(new String[]{"-remove=Select TABLE From * In Starting", "-noindex"}));
		SQLTable.commitTransaction();
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
			basicTestOnTable();
		} else {
			System.out.println("Command " + command + " not undestood");
			System.exit(1);			
		}
	}

}
