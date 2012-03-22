package saadadb.sqltable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saadadb.database.Database;
import saadadb.database.SaadaDBConnector;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

public class SQLQuery {
	protected Statement _stmts;
	protected String  query;
	protected ResultSet resultset;
	static protected int nb_open = 0;
	
	public SQLQuery(String query) throws QueryException {
		this();
		nb_open++;
		try {
			this.query = query;
		} catch(Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}
	
	public String getQuery() {
		return query;
	}

	public SQLQuery() throws QueryException {
		try {
			nb_open++;
			Connection connector;
			
			if( Database.get_connection() == null ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Make a new DB connection");
//				_stmts = SaadaDBConnector.getConnector(null, false).getConnection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
				connector = SaadaDBConnector.getConnector(null, false).getJDBCConnection();
			}
			else {
//				_stmts = Database.get_connection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
				connector = Database.get_connection();
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "autocommit switched on " + connector.isReadOnly());
			connector.setAutoCommit(true);
			_stmts = connector.createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
		} catch(Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}
	
	public ResultSet run(String query) throws  QueryException {
		this.query = query;
		return this.run();
	}
	
	public ResultSet run(String sql, String[] tables_to_lock) throws  QueryException {
		if( tables_to_lock != null ) {
			try {
				SQLTable.lockTables(null, tables_to_lock);
				/*
				 * Abort exception is downraded in QueryException because we are here in read mode.
				 */
			} catch (AbortException e) {
				QueryException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		return this.run(sql);
	}

	/**
	 * @return
	 * @throws QueryException
	 */
	public ResultSet run() throws  QueryException {
		try {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Select query: " + this.query);
			_stmts.setMaxRows(5000000);
			long start = System.currentTimeMillis();
			try {
				resultset = _stmts.executeQuery(query); 
			} catch(Exception e) {
				_stmts.close();
				Messenger.printMsg(Messenger.ERROR, "Query: " + query);
				Messenger.printMsg(Messenger.WARNING, "Reopen DB connection due to " + e);
				Database.get_connection().close();
				SaadaDBConnector sc = SaadaDBConnector.getConnector(null, false);
				sc.reconnect();
				_stmts = sc.getJDBCConnection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
				resultset = _stmts.executeQuery(query); 
			} 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Done in " + ((System.currentTimeMillis()-start)/1000F) + " sec");
			return resultset;
		} catch (Exception e) {
			this.close();
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			//Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return null;
	}

	/**
	 * 
	 * Returns the size of the result set if possible
	 * @return
	 */
	public int getSize() {
		try {
			if( _stmts.getResultSetType() == ResultSet.TYPE_FORWARD_ONLY) {
				return -1;
			}
			else {
				
				resultset.last();
				int retour = resultset.getRow();
				resultset.beforeFirst();
				return retour;
			}
		} catch (SQLException e) {
			Messenger.printMsg(Messenger.ERROR, e.getLocalizedMessage());
			return -1;
		}
	}
	/**
	 * @throws QueryException
	 */
	public void close() throws QueryException {
		nb_open--;
		try {
		if( resultset != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Close result set");
			resultset.close();
		}
		if( _stmts != null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Close statement");
			_stmts.close();
		}
		} catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/*
	 * TODO The following must be removed because they are always executed before the normal transaction.
	 * That can alter the JDBC meta cache if a resource is removed thus make some queries generating sputious errors
	 */
	/**
	 * Execute query and ignore errors
	 * @param sql
	 */
	public static void forceUpdateQueryXX(String sql)  {
		if( sql == null || sql.length() == 0 ) {
			return;
		}
		try {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "FORCE UPDATE: " + sql);
			Database.get_connection().setAutoCommit(true);
			
			Statement stmt = Database.get_connection().createStatement();
			stmt.executeUpdate(sql);
			stmt.close();
	
		} catch(Exception e ) {
			Messenger.printMsg(Messenger.ERROR, "Query: " + sql + ": " + e.toString());
		}
	}

	/**
	 * The same as runQueryUpdateSQL but without throwing exceptions. 
	 * That allows to achieve operations on corrupted DBs. (e.g. collection removal) if an operation fails, the process can continue
	 * @param sql
	 * @param table_to_lock
	 * @return
	 * @throws FatalException 
	 */
	public static void forceUpdateQueryXX(String sql, String table_to_lock) throws FatalException  {
		try {
			if( table_to_lock != null && table_to_lock.length() > 0 ) {
				SQLTable.addQueryToTransaction(Database.getWrapper().lockTable(table_to_lock));
			}
		} catch(Exception e ) {
			Messenger.printMsg(Messenger.ERROR, "Query: " + sql + ": " + e.toString());
		}
		SQLTable.addQueryToTransaction(sql);
		SQLTable.addQueryToTransaction(Database.getWrapper().unlockTables());
	}
}
