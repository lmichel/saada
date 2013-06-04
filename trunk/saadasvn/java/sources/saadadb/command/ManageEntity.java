package saadadb.command;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public abstract class ManageEntity  {
	EntityManager manager;

	protected abstract void usage();

	public void processCommand(ArgsParser ap) throws AbortException {
		try {
			String name;
			SQLTable.beginTransaction(ap.getForce());
			if( (name = ap.getCreate()) != null && name.length() > 0 ) {
				manager.setName(name);
				manager.create(ap);				
			}
			else if( (name = ap.getEmpty()) != null && name.length() > 0  ) {
				manager.setName(name);
				manager.empty(ap);	
			}
			else if( (name = ap.getRemove()) != null && name.length() > 0  ) {
				manager.setName(name);
				manager.remove(ap);				
			}
			else if( (name = ap.getPopulate()) != null && name.length() > 0  ) {
				manager.setName(name);
				manager.populate(ap);
			}
			else if( (name = ap.getIndex()) != null && name.length() > 0  ) {
				manager.setName(name);
				manager.index(ap);
			}
			else {
				usage();				
			}
			SQLTable.unlockTables();
			SQLTable.commitTransaction();

		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
			SQLTable.dropTmpTables();
		}	
	}

	protected static void init(ArgsParser ap) throws Exception {
		Database.init(ap.getDBName());
		Database.setAdminMode(ap.getPassword());
	}

	public static void processCommand(ManageEntity me, String[] args) {
		try {
			ArgsParser ap = new ArgsParser(args);
			init(ap);
			me.processCommand(ap);
		} catch (AbortException e) {
			me.usage();
			System.exit(1);
		}
		catch (Exception e2) {
			Messenger.printStackTrace(e2);
			AbortException.abort();
			System.exit(1);
		}
	}

}
