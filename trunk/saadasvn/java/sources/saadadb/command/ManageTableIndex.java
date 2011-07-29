package saadadb.command;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ManageTableIndex extends SaadaProcess {
	private String table_name;

	static public void main(String[] args) throws SaadaException {
		try {
			ArgsParser ap = new ArgsParser(args);
			Messenger.printMsg(Messenger.TRACE, ap.toString());
			Database.init(ap.getDBName());
			Database.getConnector().setAdminMode(ap.getPassword());
			String table_name ;
			if( (table_name = ap.getCreate()) != null && table_name.length() > 0 ) {
				(new ManageTableIndex(table_name)).indexTable();				
			}
			else if( (table_name = ap.getRemove()) != null && table_name.length() > 0 ) {
				(new ManageTableIndex(table_name)).dropTableIndex();				
			}
			else {
				usage();				
			}
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}

	}
	
	public ManageTableIndex(String table_name) {
		this.table_name = table_name;
	}
	/**
	 * 
	 */
	private static void usage() {
		System.out.println("USAGE: java ManageTableIndex -[remove|create]=table_name SaadaDB_Name");
		System.exit(1);						
	}

	/**
	 * @param class_name
	 * @throws SaadaException 
	 */
	public void indexTable() throws SaadaException {
		SQLTable.indexTable(this.table_name, this);
	}
	
	/**
	 * @param col_name
	 * @throws SaadaException
	 */
	public void indexTableColumn( String col_name) throws SaadaException {
		SQLTable.indexColumnOfTable(this.table_name, col_name, this);
	}
	
	/**
	 * @throws AbortException
	 */
	public void dropTableIndex() throws AbortException {
		SQLTable.dropTableIndex(this.table_name, this);
		
	}
	/**
	 * @param col_name
	 * @throws AbortException
	 */
	public void dropTableColumnIndex(String col_name) throws AbortException {
		SQLTable.dropTableColumnIndex(this.table_name,col_name,  this);
		
	}

}
