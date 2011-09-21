package saadadb.command;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class DeleteTableIndex {
	
	static public void main(String[] args) throws SaadaException {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		try {
			Database.getConnector().setAdminMode(ap.getPassword());
			if (args.length != 2) {
				Messenger.printMsg(Messenger.ERROR,
				"Usage: java command.DeleteTableIndex [table] [DBNAME]");
				System.exit(1);
			} else {
				SQLTable.dropTableIndex(args[0].toLowerCase(), null);
			}
			
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}
}
