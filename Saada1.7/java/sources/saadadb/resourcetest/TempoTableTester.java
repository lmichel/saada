package saadadb.resourcetest;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.sqltable.SQLTable;

/**
 * @author michel
 *
 */
public class TempoTableTester {

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		String tname = "tempo_GridQualByPos";
		Database.init(ap.getDBName());
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("CREATE TEMPORARY TABLE " + tname + "(oidprimary int8, oidsecondary int8, qual float8)");
		SQLTable.addQueryToTransaction("CREATE  INDEX CC  on " + tname + "(oidprimary)");
		System.out.println(Database.getWrapper().tableExist(tname));
		SQLTable.commitTransaction();
		System.out.println(Database.getWrapper().tableExist(tname));

		Database.close();
	}

}
