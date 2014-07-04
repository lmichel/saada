package saadadb.util;

import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.vo.ADQLExecutor;

public class SpoolerVsLargeQuery {

	/**
	 * @param args
	 * @throws FatalException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		Messenger.debug_mode = true;
		
		while( 1 == 1) {
			ADQLExecutor q = new ADQLExecutor();
			q.execute("SELECT * FROM simbad", 1000);
			q.close();
		}

	}

}
