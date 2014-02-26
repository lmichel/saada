package saadadb.command;

import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * Command invoked by ant script to manage indexes on SQL table.
 * parameter values
 * -create=tableName: index creation
 * -remove=tableName: index removing
 * tableName can be a simple table name or it can be like tableName(col1,...) giving so the list of columns
 * concerned by the command 
 * @author michel
 * @version $Id: ManageTableIndex.java 915 2014-01-29 16:59:00Z laurent.mistahl $
 *
 */
public class ManageTableIndex extends SaadaProcess {
	private String table_name;

	/**
	 * @param args
	 * @throws SaadaException
	 */
	static public void main(String[] args) throws SaadaException {
		try {
			ArgsParser ap = new ArgsParser(args);
			Messenger.printMsg(Messenger.TRACE, "ManageTableIndex " + ap.toString());
			Database.init(ap.getDBName());
			Database.setAdminMode(ap.getPassword());
			String table_name ;
			Collection<String> columns = new TreeSet<String>();
			if( (table_name = ap.getCreate()) != null && table_name.length() > 0 ) {
				table_name = getTableName(table_name, columns);
				SQLTable.beginTransaction();
				if( columns.size() == 0 ) {
					(new ManageTableIndex(table_name)).indexTable();		
				} else {
					for( String c : columns) {
						(new ManageTableIndex(table_name)).indexTableColumn(c);		
					}
				}
				SQLTable.commitTransaction();
			} else if( (table_name = ap.getRemove()) != null && table_name.length() > 0 ) {
				table_name = getTableName(table_name, columns);
				SQLTable.beginTransaction();
				if( columns.size() == 0 ) {
					(new ManageTableIndex(table_name)).dropTableIndex();	
				} else {
					for( String c : columns) {
						(new ManageTableIndex(table_name)).dropTableColumnIndex(c);		
					}
				}
				SQLTable.commitTransaction();
			}
			else {
				usage();				
			}
			Database.close();
		} catch (Exception e) {
			Database.close();
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}

	}

	/**
	 * Parse the table name which can he the following form:
	 * tableName: just take it as it is
	 * tableName(col1,col2...): column names are stored in the columns collection
	 * @param tableParam
	 * @param columns
	 * @return
	 */
	static public final String getTableName(String tableParam, Collection<String> columns) {

		Pattern p = Pattern.compile("(.*)\\((.*)\\)");
		Matcher m = p.matcher(tableParam);
		if( m.find() && m.groupCount() == 2 ) {
			String[] cols = m.group(2).split("[,;\\s]");
			if( columns != null) {
				for( String c: cols ) columns.add(c);
			}
			return m.group(1);
		} else {
			return tableParam;
		}
	}


	/**
	 * @param table_name
	 */
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
