package saadadb.sqltable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import saadadb.command.ArgsParser;
import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 * 07/2009: SQL queries limited at 5000000 rows by JDBC
 *          Creation of method runLargeQuerySQL for very large resultSet
 * 03/2010: Method forceQueryUpdateSQL(String[]/...) ignore exception 
 *
 */
public abstract class SQLTable {
	static TransactionMaker transaction_maker=null;

	/**
	 * @throws FatalException
	 * @throws SQLException
	 */
	public static void disconnect() throws FatalException, SQLException {
		if( Database.get_connection() != null ) {
			Database.get_connection().close();
		}

	}

	/**
	 * Add query to the current transactionmaker if it exist:
	 * @param query
	 * @throws AbortException when no transaction maker does exist
	 */
	public static  void addQueryToTransaction(String query) throws AbortException {
		if( transaction_maker == null ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt add a query to a transaction which has not been initiated");
		}
		else if( !transaction_maker.isFree()) {
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt add a query to a transaction which is busy");
		}
		else {
			transaction_maker.addQuery(query);
		}
	}
	/**
	 * Add query to the current transaction maker if it exist:
	 * @param query
	 * @param params  : params for the prepared statement
	 * @throws AbortException
	 */
	public static  void addQueryToTransaction(String query, Object[] params) throws AbortException {
		if( transaction_maker == null ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt add a query to a transaction which has not been initiated");
		}
		else if( !transaction_maker.isFree()) {
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt add a query to a transaction which is busy");
		}
		else {
			transaction_maker.addQuery(query, params);
		}
	}

	/**
	 * @param table_name
	 * @param format
	 * @param unique_key: Coumn used as primary key
	 * @param overwrite: remove the table if it already exists
	 * @throws SaadaException
	 */
	public static void createTable(String table_name, String format, String unique_key, boolean overwrite) throws SaadaException {
		String constraint = "";
		if( overwrite) {
			dropTable(table_name);
		}
		if( unique_key != null && unique_key.length() > 0 ) {
			/*
			 * In some cases, te
			 */
			constraint = ", CONSTRAINT " + table_name.replaceAll("[\"'`]", "") + "_pksd PRIMARY KEY (" + unique_key + ")";
		}
		addQueryToTransaction("CREATE TABLE " + table_name + "(" + format + constraint + ")", (String)null );	
		lockTables(Database.getWrapper().getUserTables(), null);
		addQueryToTransaction(Database.getWrapper().grantSelectToPublic(table_name));	
	}

	/**
	 * @param table_name
	 * @param format
	 * @param unique_key
	 * @param overwrite
	 * @throws SaadaException
	 */
	public static void createTemporaryTable(String table_name, String format, String unique_key, boolean overwrite) throws SaadaException {
		String constraint = "";
		if( overwrite) {
			dropTable(Database.getWrapper().getTempoTableName(table_name));
		}
		if( unique_key != null && unique_key.length() > 0 ) {
			constraint = ", CONSTRAINT " + table_name + "_pksd PRIMARY KEY (" + unique_key + ")";
		}
		//@@ a supprimer/corriger
		addQueryToTransaction(Database.getWrapper().getCreateTempoTable( table_name, "(" + format + constraint + ")"), (String)null );	
		/*
		 * tmp tables can be made by reader user who has as few privileges as possible
		 */
		//lockTables(Database.getWrapper().getUserTables(), null);
		//runQueryUpdateSQL("GRANT select ON TABLE " + table_name + " TO PUBLIC", false);	
	}
	/**
	 * @execute queryupdate sql
	 * @param sql
	 *            query
	 * @throws FatalException 
	 * @exception SaadaException
	 */
	public static void addQueryToTransaction (String sql, String table_to_lock) throws AbortException {
		if( table_to_lock == null ) {
			addQueryToTransaction(sql);
		}
		else {
			lockTable(table_to_lock);
			addQueryToTransaction(sql);
		}
	}
	/**
	 * Execute a query sequence. Abort is thrown if one query fails
	 * @param sql
	 * @param transaction
	 * @param table_to_lock
	 * @return
	 * @throws AbortException
	 */
	public static int addQueryToTransaction(String[] sql, String table_to_lock) throws AbortException {
		if( table_to_lock == null ) {
			for( String q: sql) {
				addQueryToTransaction(q) ;
			}
		}
		else {
			lockTable(table_to_lock);
			for( String q: sql) {
				addQueryToTransaction(q) ;
			}
		}
		return 0;
	}


	/**
	 * @param sql
	 * @param tables_to_lock
	 * @return
	 * @throws AbortException
	 */
	public static void addQueryToTransaction(String sql, String wtables_to_lock, String[] rtables_to_lock) throws AbortException {
		if( wtables_to_lock != null )
			lockTables(new String[]{wtables_to_lock},  rtables_to_lock);
		else
			lockTables(null,  rtables_to_lock);

		addQueryToTransaction(sql);
	}


	/**
	 * @throws AbortException 
	 * 
	 */
	public static void beginTransaction() throws AbortException  {
		if( transaction_maker != null && (!transaction_maker.isFree() || transaction_maker.queries.size() != 0)) {
			Messenger.printMsg(Messenger.ERROR, "Attempt to open a transaction within a transaction");
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt to open a transaction within a transaction");
		}
		transaction_maker = new TransactionMaker(false);			
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Start to record a new transaction");

	}

	/**
	 * @param forced_mode: ask the transaction manager to continue on errors
	 * @throws AbortException
	 */
	public static void beginTransaction(boolean forced_mode) throws AbortException  {
		if( transaction_maker != null && (!transaction_maker.isFree() || transaction_maker.queries.size() != 0)) {
			Messenger.printMsg(Messenger.ERROR, "Attempt to open a transaction within a transaction");
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt to open a transaction within a transaction");
		}
		transaction_maker = new TransactionMaker(forced_mode);			
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Start to record a new transaction in forced mode " + forced_mode);
	}

	public static void abortTransaction() {
		if( transaction_maker != null ) {
			transaction_maker.abortTransaction();
		}
	}

	/**
	 * @throws AbortException 
	 * 
	 */
	public static void commitTransaction() throws AbortException  {
		if( transaction_maker == null ) {
			AbortException.throwNewException(SaadaException.DB_ERROR, "Attempt to commit a transaction which has not been initiated");
		}
		else {
			transaction_maker.makeTransaction();
		}
		transaction_maker = null;			
	}

	/**
	 * The transaction status is used by the dataloader to know if it must commit a transaction before to update the schema.
	 * @return
	 */
	public static boolean isTransactionOpen() {
		if( transaction_maker == null || (transaction_maker.isFree() && transaction_maker.queries.size() == 0) ) {
			return false;
		}
		else {
			return true;
		}

	}
	/**
	 * @param table
	 * @return
	 * @throws FatalException 
	 */
	public static void lockTable(String table) throws AbortException {
		try {
			addQueryToTransaction(Database.getWrapper().lockTable(table));

		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}
	}


	/**
	 * @param w_tables
	 * @param r_tables
	 * @throws AbortException
	 */
	public static void lockTables(String[] w_tables, String[] r_tables) throws AbortException {
		try {
			addQueryToTransaction(Database.getWrapper().lockTables(w_tables, r_tables));
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}	
	}

	/**
	 * @throws AbortException
	 */
	public static void unlockTables() throws AbortException {
		try {
			addQueryToTransaction(Database.getWrapper().unlockTables());
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}	
	}

	/**
	 * @param table
	 * @throws AbortException
	 */
	public static void dropTable(String table) throws AbortException {
		try {
			if( Database.getWrapper().tableExist(table)) {
				addQueryToTransaction(Database.getWrapper().dropTable(table));				
			}
		} catch (Exception e) {
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}
	}

	/**
	 * @param tableName
	 * @return
	 */
	public static boolean tableExist(String tableName) {
		try {
			return (Database.getWrapper().tableExist(tableName) || Database.getWrapper().tableExist(tableName.toLowerCase()));
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			return false;
		}
	}
	/**
	 * @param table
	 * @param sp used for progress bar
	 * @throws AbortException
	 */
	public static void indexColumnOfTable(String table, String col_name, SaadaProcess sp) throws AbortException {
		try {			
			if( Database.getWrapper().tableExist(table)) {
				lockTable(table);
				SQLQuery squery = new SQLQuery("select count(*) from (select * from " + table + " Limit 1001) as js");
				ResultSet rs = squery.run();
				int v=0;
				if( rs.next() && (v = rs.getInt(1)) < 1000 ) {
					Messenger.printMsg(Messenger.TRACE, "Table <" + table + "> has less than 1000 rows: not indexed");
					unlockTables();
					squery.close();
					return;
				} else {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Table <" + table + "> has  more than 1000 rows: to be indexed");
				}
				squery.close();
				if( sp != null ) {
					sp.processUserRequest();
					Messenger.incrementeProgress();
				}
				ResultSet rsColumns = Database.getWrapper().getTableColumns(table);
				Map<String, String> existing_index = Database.getWrapper().getExistingIndex(table);
				while (rsColumns.next()) {
					String columnName = rsColumns.getString("COLUMN_NAME");
					if( columnName.equals(col_name)) {
						String columnDataType = rsColumns.getString("TYPE_NAME");
						String index_name;
						String[] sc = table.split("\\.");
						if( sc.length == 1 ) {
							index_name = sc[0] + "_" + columnName;
						}
						else {
							index_name = sc[1] + "_" + columnName;						
						}
						index_name = index_name.toLowerCase();
						if( sp != null ) {
							sp.processUserRequest();
							Messenger.incrementeProgress();
						}
						if( existing_index.get(index_name) == null ) {
							if (columnName.equals("oidsaada") ) {
								String sql1 = "CREATE UNIQUE INDEX "
									+ index_name + " ON "
									+ table + " ( " + columnName + ")";
								addQueryToTransaction(sql1);
							} else if (!columnDataType.equals("bool")) {
								String sql1 = "CREATE INDEX " 
									+ index_name + " ON "
									+ table + " ( " + columnName + ")";
								addQueryToTransaction(sql1);
							}
						}
						else {
							if (Messenger.debug_mode)
								Messenger.printMsg(Messenger.DEBUG, "Index <" + index_name + "> already exists");
						}
					}
				}	
				rsColumns.close();		
				unlockTables();
			}
			else  {
				Messenger.printMsg(Messenger.WARNING, "table <" + table + "> not found");
			}
		} catch (Exception e) {
			unlockTables();
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}
		unlockTables();
	}
	/**
	 * @param table
	 * @param sp used for progress bar
	 * @throws AbortException
	 */
	public static void indexTable(String table, SaadaProcess sp) throws AbortException {
		/*
		 * Can genetrate error because this fucntiojn relies on JDBC meta cache which is not updated inside a transaction
		 * dropTableIndex(table, sp);
		 */
		try {			
			if( Database.getWrapper().tableExist(table)) {
				lockTable(table);
				SQLQuery squery = new SQLQuery();
				ResultSet rs = squery.run("select count(*) from (select * from " + table + " Limit 1001) as js");
				int v=0;
				if( rs.next() && (v = rs.getInt(1)) < 1000 ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Table <" + table + "> has less than 1000 rows : not indexed");
					unlockTables();
					squery.close();
					return;
				}
				else {
					Messenger.printMsg(Messenger.TRACE, "Table <" + table + "> has  more than 1000 rows to be indexed");
				}
				squery.close();
				if( sp != null ) {
					sp.processUserRequest();
					Messenger.incrementeProgress();
				}
				ResultSet rsColumns = Database.getWrapper().getTableColumns(table);
				Map<String, String> existing_index = Database.getWrapper().getExistingIndex(table);
				while (rsColumns.next()) {
					String columnName = rsColumns.getString("COLUMN_NAME");
					String columnDataType = rsColumns.getString("TYPE_NAME");
					String index_name;
					String[] sc = table.split("\\.");
					if( sc.length == 1 ) {
						index_name = sc[0] + "_" + columnName;
					}
					else {
						index_name = sc[1] + "_" + columnName;						
					}
					index_name = index_name.toLowerCase();
					if( sp != null ) {
						sp.processUserRequest();
						Messenger.incrementeProgress();
					}
					if( existing_index.get(index_name.toLowerCase()) == null ) {
						if (columnName.equals("oidsaada") ) {
							String sql1 = "CREATE UNIQUE INDEX "
								+ index_name + " ON "
								+ table + " ( " + columnName + ")";
							lockTable(table);
							addQueryToTransaction(sql1);
						} else if (!columnDataType.equals("bool")) {
							String sql1 = "CREATE INDEX " 
								+ index_name + " ON "
								+ table + " ( " + columnName + ")";
							lockTable(table);
							addQueryToTransaction(sql1);
						}
					}
					else {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Index <" + index_name + "> already exists");
					}
				}	
				rsColumns.close();			
			}
			else  {
				Messenger.printMsg(Messenger.WARNING, "Index creation: table <" + table + "> not found");
			}
		} catch (Exception e) {
			unlockTables();
			AbortException.throwNewException(SaadaException.DB_ERROR,e);
		}
		unlockTables();
	}

	/**
	 * @param table
	 * @param sp used for progress bar
	 * @throws AbortException
	 */
	public static void dropTableIndex(String table, SaadaProcess sp) throws AbortException {
		try {
			Messenger.printMsg(Messenger.TRACE, "Drop indexes of table <" + table + ">");			
			Map<String, String> existing_index = Database.getWrapper().getExistingIndex(table);
			if( existing_index != null ) {
				for( String iname: existing_index.keySet()) {
					if( sp != null ) {
						sp.processUserRequest();
						Messenger.incrementeProgress();
					}
					/*
					 * Do not remove indexes on primary keys
					 */
					if( Database.getWrapper().isIndexDroppable(iname)) {
						if ( !iname.toString().endsWith("pkoid") && !iname.toString().endsWith("pksd") && !iname.equalsIgnoreCase("PRIMARY")) {
							addQueryToTransaction(Database.getWrapper().getDropIndexStatement(table, iname), table);
						}
					}
				}
			}
			else {
				Messenger.printMsg(Messenger.TRACE, "No index found");			
			}
		} catch (SaadaException e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**
	 * @param table
	 * @param col_name
	 * @param sp
	 * @throws AbortException
	 */
	public static void dropTableColumnIndex(String table, String col_name, SaadaProcess sp) throws AbortException {
		try {
			Map<String, String> existing_index = Database.getWrapper().getExistingIndex(table);
			if( existing_index != null ) {
				for( String iname: existing_index.keySet()) {
					if( existing_index.get(iname).equals(col_name)) {
						if( sp != null ) {
							sp.processUserRequest();
							Messenger.incrementeProgress();
						}
						/*
						 * Do not remove indexes on primary keys
						 */
						if( Database.getWrapper().isIndexDroppable(iname)) {
							if ( !iname.toString().endsWith("pkoid")&& !iname.toString().endsWith("pksd") && !iname.equalsIgnoreCase("PRIMARY")) {
								Messenger.printMsg(Messenger.TRACE, "Drop index <" + iname + "> on table <" + table + ">");
								addQueryToTransaction(Database.getWrapper().getDropIndexStatement(table, iname), table);
							}
						}
					}
				}
			}
		} catch (SaadaException e) {
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	/**
	 * Drop all temp tables (MySQL) recored by the wrapper
	 * @throws FatalException
	 */
	public static void dropTmpTables() {
		try {
			for( String tbl: Database.getWrapper().getReferencedTempTable() ) { 
				dropTable( Database.getWrapper().getDropTempoTable(tbl));

			}
		} catch(Exception e) {
			Messenger.printMsg(Messenger.WARNING, "Dropping tmp tables" + e.getMessage());
		}	
	}


	/**
	 * Return all table columns except those of rejected_columns array
	 * @param table
	 * @param rejected_columns
	 * @return
	 * @throws SQLException
	 * @throws FatalException 
	 */
	static public String[] getColumnsExceptsThose(String table, String [] rejected_columns) throws FatalException {
		try {
			ResultSet rsColumns = null;

			rsColumns = Database.getWrapper().getTableColumns(table); 
			ArrayList<String> al = new ArrayList<String>();
			while (rsColumns.next()) {
				boolean found = false;
				String cn = rsColumns.getString("COLUMN_NAME");
				if( rejected_columns != null ) {
					for( String rc: rejected_columns) {
						if( cn.equalsIgnoreCase(rc) ) {
							found = true;
							break;
						}
					}
				}
				if( !found ) {
					al.add(cn);
				}
			}
			rsColumns.close();
			return al.toArray(new String[0]);
		} catch(Exception e) {
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	} 


	/*
	 * Methods used to debug interlock between JDBC and SWING while index creation
	 */
	static class Indexeur extends Thread {
		@Override
		public void run() {
			try {
				indexTable("GrosFITSEntry", null);
			} catch (AbortException e) {
				Messenger.printStackTrace(e);
			}
		}
	}

	static class Compteur extends Thread {
		@Override
		public void run() {
			int x = 1;
			int cpt = 0;
			while( x == 1 ) {
				for( int i=0 ; i<1000000 ; i++ );
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Messenger.printStackTrace(e);
				}
				System.out.println(cpt++);
			}
		}

	}
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws AbortException 
	 */
	public static void main(String[] args) throws Exception {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		(new Thread(new Runnable() {
			public void run() {
				try {
					SQLLargeQuery squery = new SQLLargeQuery();
					ResultSet rs2;
					rs2  = squery.run("SELECT oidprimary,oidsecondary FROM XMMCounterparts ORDER BY oidprimary,oidsecondary limit 100");
					//rs2 = runLargeQuerySQL("SELECT oidprimary,oidsecondary FROM XMMCounterparts ");
					int cpt2=0;
					while(rs2.next()) {
						cpt2++;
					}
					squery.close();
					rs2 = null;
					//closeLargeQueryConnection();
					System.out.println(cpt2);
					//abortTransaction();
					rs2  = squery.run("SELECT oidprimary,oidsecondary FROM XMMCounterparts ORDER BY oidprimary,oidsecondary");
					//rs2 = runLargeQuerySQL("SELECT oidprimary,oidsecondary FROM XMMCounterparts ");
					cpt2=0;
					while(rs2.next()) {
						cpt2++;
					}
					System.out.println(cpt2);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		})).start();
		if( true) 
			while( true ) {
				System.out.println(Runtime.getRuntime().freeMemory() + " " + Runtime.getRuntime().maxMemory());
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
			}
		Messenger.debug_mode = true;
		Database.getWrapper().loadSQLProcedures();
		System.exit(1);
		indexColumnOfTable("DEVBENCH1_5_1_tempo.tempo_GridQualByPos", "oidprimary", null);
		System.exit(1);
		//		dropTableIndex("XID_TABLE", null);
		//		indexTable("XID_TABLE", null);
		//		indexTable("XID_TABLE", null);
		//		Compteur c = new Compteur();
		//		Indexeur i = new Indexeur();
		//		c.start();
		//		i.start();

		Messenger.printMsg(Messenger.TRACE, "start");
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("Select * From GrosFITSEntry where oidsaada > 0");
		Messenger.printMsg(Messenger.TRACE, "start2");
		int cpt=0;
		while( rs.next() ) {
			cpt++;
		}
		Messenger.printMsg(Messenger.TRACE, "stop1 " + cpt);
		rs.beforeFirst();
		while( rs.next() ) {
			for( int i=1 ; i<20 ; i++ )
				rs.getObject(i);
		}
		squery.close();
		Messenger.printMsg(Messenger.TRACE, "stop2 " + cpt);
		System.gc();
		Messenger.printMsg(Messenger.TRACE, "start");
		squery = new SQLQuery();
		rs = squery.run("Select oidsaada From GrosFITSEntry where oidsaada > 0");
		Messenger.printMsg(Messenger.TRACE, "start2");
		cpt=0;
		while( rs.next() ) {
			cpt++;
			if( cpt == 1000 ) break;
		}
		squery.close();
		Messenger.printMsg(Messenger.TRACE, "stop1 " + cpt);
		//		while( rs.next() ) {
		//			System.out.println("rs: " + rs.getLong(1));
		//		}
		//		rs.beforeFirst();
		//		while( rs.next() ) {
		//			System.out.println("rs: " + rs.getLong(1));
		//		}

	}


	/**
	 * Add a stat column to the tableName if it does not exist.
	 * The stat columns contains an integer value relevant for the content of the data referenced by that table
	 * This method is invoked to update automatically and smoothly the meta data model
	 * This method must be invoked out of a transaction
	 * @param tableName
	 * @throws Exception
	 */
	public static final void addStatColumn(String tableName) throws Exception {
		System.out.println("bbbbbbbbbbbbbbbbbbb");
		if( Database.getWrapper() != null ) { 
			System.out.println("C");
			ResultSet cols = Database.getWrapper().getTableColumns(tableName);
			if( cols != null ) {
				System.out.println("D");
				while( cols.next() ){
					System.out.println(cols.getString("COLUMN_NAME"));
					if( cols.getString("COLUMN_NAME").equalsIgnoreCase("stat")) {
						cols.close();
						return;
					}
				}
				cols.close();
				Messenger.printMsg(Messenger.TRACE, "Add column to the " + tableName + " table");
				SQLTable.beginTransaction();
				for( String q : Database.getWrapper().addColumn(tableName, "stat", Database.getWrapper().getSQLTypeFromJava("int"))){
					SQLTable.addQueryToTransaction(q);
				}
				SQLTable.commitTransaction();
			}
		}
	}

}
