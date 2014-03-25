package saadadb.sqltable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
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
	protected DatabaseConnection databaseConnection;

	public SQLQuery(String query) throws QueryException {
		this();
		try {
			this.query = query;
		} catch(Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	public String getQuery() {
		return query;
	}

//	public SQLQuery() throws QueryException {
//		try {
//			nb_open++;
//			Connection connector;
//
//			if( Database.get_connection() == null ) {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Make a new DB connection");
//				//				_stmts = SaadaDBConnector.getConnector(null, false).getConnection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
//				connector = SaadaDBConnector.getConnector(null, false).getJDBCConnection();
//			}
//			else {
//				//				_stmts = Database.get_connection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
//				connector = Database.get_connection();
//			}
//			if (Messenger.debug_mode)
//				Messenger.printMsg(Messenger.DEBUG, "autocommit switched on " + connector.isReadOnly());
//			connector.setAutoCommit(true);
//			_stmts = connector.createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
//		} catch(Exception e) {
//			QueryException.throwNewException(SaadaException.DB_ERROR, e);
//		}
//
//	}
	public SQLQuery() throws QueryException {
		try {
			nb_open++;
//			StackTraceElement [] se = (new Exception()).getStackTrace();
//			for( int i=0 ; i<3 ; i++ ) System.out.println(se[i]);
//			System.out.println("============= OPEN " +  Spooler.getSpooler());
			databaseConnection = Database.getConnection();
		} catch(Exception e) {
			Messenger.printStackTrace(e);
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
			Messenger.dbAccess();
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Select query: " + this.query);
			_stmts = databaseConnection.getStatement();
			_stmts.setMaxRows(5000000);
			long start = System.currentTimeMillis();
			try {
				resultset = _stmts.executeQuery(query); 
			} catch(Exception e) {
				Messenger.printMsg(Messenger.WARNING, "Takea new connection  due to the following error " + e);
				Messenger.printStackTrace(e);
				_stmts.close();
				Database.giveConnection(databaseConnection);
				databaseConnection = Database.getConnection();
				_stmts = databaseConnection.getStatement();
//
//				Messenger.printMsg(Messenger.ERROR, "Query: " + query);
//				Messenger.printMsg(Messenger.WARNING, "Reopen DB connection due to " + e);
//				Database.get_connection().close();
//				SaadaDBConnector sc = SaadaDBConnector.getConnector(null, false);
//				sc.reconnect();
//				_stmts = sc.getJDBCConnection().createStatement(Database.getWrapper().getDefaultScrollMode(),Database.getWrapper().getDefaultConcurentMode());
				resultset = _stmts.executeQuery(query); 
			} 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Done in " + ((System.currentTimeMillis()-start)/1000F) + " sec");
			Messenger.procAccess();
			return resultset;
		} catch (Exception e) {
			try {
				this.close();
			} catch (Exception e1) {}
			Messenger.procAccess();
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			//Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		Messenger.procAccess();
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
//	public void close() throws QueryException {
//		nb_open--;
//		try {
//			if( resultset != null ) {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Close result set");
//				resultset.close();
//			}
//			if( _stmts != null ) {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Close statement");
//				_stmts.close();
//			}
//		} catch (Exception e) {
//			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
//			Messenger.printStackTrace(e);
//			QueryException.throwNewException(SaadaException.DB_ERROR, e);
//		}
//	}
	public void close() throws QueryException {
		nb_open--;
		try {
			if( resultset != null ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Close result set " + nb_open);
				resultset.close();
			}
			//System.out.println("============= CLOSE1 " +  Spooler.getSpooler());
			Database.giveConnection(databaseConnection);
			System.out.println("============= CLOSE2 " +  Spooler.getSpooler());
		} catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
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
