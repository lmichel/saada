package saadadb.command;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.Table_SaadaDB;

/**
 * @author michel
 *
 */
public class ManageDatabase {

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws FatalException {
		try {
			ArgsParser ap = new ArgsParser(args);
			Database.init(ap.getDBName());
			Database.setAdminMode(ap.getPassword());
			String param_value ;
			if( (param_value = ap.getUrlroot()) != null  ) {
				Table_SaadaDB.changeURL(param_value);
			}
			else if( (param_value = ap.getBasedir()) != null  ) {
				Table_SaadaDB.changeBasedir(param_value);
			}
			else if( (param_value = ap.getRepdir()) != null  ) {
				Table_SaadaDB.changeRepdir(param_value);
			}
			else if( (param_value = ap.getRename()) != null  ) {
				Table_SaadaDB.rename(param_value);
			}
			else {
				usage();				
			}
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}
	}

	/**
	 * 
	 */
	private static void usage() {
		System.out.println("USAGE: java ManageDatabase -[urlroot|basedir|repdir|rename]=value SaadaDB_Name");
		System.exit(1);						
	}

}
