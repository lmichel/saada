/**
 * 
 */
package saadadb.database.spooler;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saadadb.database.Database;
import saadadb.database.DbmsWrapper;
import saadadb.util.Messenger;

/*
 * Wrapper for JDBC connection {@link Spooler}
 * @author michel
 * @version $Id$
 * 02/2014: Add methods to handle update transactions: Due to the use of the spooler in admin mode
 */
public class DatabaseConnection {
	/**
	 * Identification number (debug purpose)
	 */
	protected final int number;
	protected int status;
	/**
	 * Normal or large query
	 */
	protected int mode;
	/*
	 * Status states
	 */
	public static final int LARGE_QUERY = 1;
	public static final int NOTREADY = 0;
	public static final int FREE = 1;
	public static final int CLOSED = 2;
	public static final int WORKING = 3;
	public static final int OBSOLETE = 4;
	/**
	 * JDBC connection
	 */
	protected Connection connection = null;	
	/**
	 * JDBC statement
	 */
	protected Statement statement;

	/**
	 * The connection is open at creation time whereas the statement is open later according to the mode
	 * @param number ID number (debug purose)
	 * @throws SQLException
	 */
	DatabaseConnection(int number) throws Exception{
		this.number = number;
		this.status = NOTREADY;
		this.connect();
	}
	/**
	 * Connect to the database
	 * @throws SQLException
	 */
	private void connect() throws Exception{
		this.connection = Database.getConnector().getNewConnection();
		this.status = FREE;
	}
	/*
	 * Methods supposed to be called by the Spooler.
	 * Using them outside would lead to synchro failures
	 */
	/**
	 * Close the database connection.
	 * @throws SQLException
	 */
	protected void close() throws SQLException{
		if( this.connection != null ) this.connection.close();
		this.connection = null;
		this.status = OBSOLETE;	
	}
	/**
	 * Close and reopen the connection: used by SQLITE which can alter the schema with a connection
	 * which has not the write_lock locking level
	 * @throws SQLException
	 */
	protected void reconnect() throws Exception{
		this.close();
		this.connect();
	}
	/**
	 * Make the connection ready again.
	 * Close the connection in large query mode. Does nothing but changing the status otherwise
	 * @throws SQLException
	 */
	protected void give() throws SQLException {
		if( statement != null ) {		
			this.statement.close();
		}
		if( this.mode == LARGE_QUERY)  {
			this.close();
		} else {
			this.status = FREE;
		}
	}

	/**
	 * @return Return the SQL statement (readonly) ready to be executed by the client
	 * @throws Exception
	 */
	public  Statement getStatement() throws Exception{
		this.status = WORKING;
		this.connection.setAutoCommit(true);
		this.statement = connection.createStatement(Database.getWrapper().getDefaultScrollMode()
				,Database.getWrapper().getDefaultConcurentMode());
		return this.statement;
	}
	public  Statement getUpdatableStatement() throws Exception{
		this.status = WORKING;
		this.connection.setAutoCommit(true);
		this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
		return this.statement;
	}
	
	/**
	 * Returns a prepared statement
	 * Set autocommit to false since the prepared statement 
	 * is supposed to executed within a transaction
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(String query) throws SQLException {
		this.status = WORKING;
		this.connection.setAutoCommit(false);
		this.statement =  connection.prepareStatement(query);
		return (PreparedStatement) this.statement;
	}

	/**
	 * Returns a pointer to the DB metadata attached to the connection
	 * @return
	 * @throws SQLException
	 */
	public DatabaseMetaData getMetaData() throws SQLException{
		if( this.connection != null ){
			return this.connection.getMetaData();
		} else {
			return null;
		}
	}

	/**
	 * @return Return an SQL statement (readonly) ready to be executed by the client.
	 * The statement is setup for queries with a large result set: mode FORWARDONLY => no bufferisation but 
	 * @throws Exception
	 */
	public  Statement getLargeStatement() throws Exception {
		this.status = WORKING;
		this.mode = LARGE_QUERY;		
		this.connection.setAutoCommit(false);
		this.statement = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		Database.getWrapper().setFetchSize(this.statement,5000);
		return this.statement;
	}
	/**
	 * connection should be encaplusated, but we need it at DB creation time when we work without the spooler
	 * used by {@link DbmsWrapper#checkAdminPrivileges(String, boolean)} to load a small TSV with SQLLIte
	 * @return
	 */
	public Connection getConnection() {
		return this.connection;
	}
	/**
	 * AutoCommit is managed internally
	 * @param autoCommit
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException{}
	
	/**
	 * Commit the current transaction
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		if( !this.connection.getAutoCommit() ) this.connection.commit();
	}
	/**
	 * Rollback the current transaction
	 * @throws SQLException
	 */
	public void rollBack() throws SQLException {
		if( !this.connection.getAutoCommit() ) this.connection.rollback();
	}
	/*
	 * Status setter/getter
	 */
	public void startWorking() {
		this.status = WORKING;
	}
	public boolean isFree() {
		return (this.status == FREE)? true: false;
	}
	public boolean isCLosed() {
		return (this.status == CLOSED)? true: false;
	}
	public boolean isWorking() {
		return (this.status == WORKING)? true: false;
	}
	public boolean isObsolete() {
		return (this.status == OBSOLETE)? true: false;
	}
	/*
	 * Mode getters
	 */
	public boolean isLargeQuery() {
		return (this.mode == LARGE_QUERY)? true: false;
	}

	/*
	 * reporting
	 */
	public String toString(){
		return "ConnectionReference #" + this.number + " " 
		+ ((this.status == NOTREADY)? "NOTREADY":
			(this.status == CLOSED)? "CLOSED":
				(this.status == FREE)? "FREE":
					(this.status == OBSOLETE)? "OBSOLETE":
						(this.status == WORKING)? "WORKING": 
		"UNKNOWN STATUS") 
		+ " " + ((this.mode == LARGE_QUERY)? "LARGE_QUERY": "");
	}
	/**
	 * @return a one letter message reflecting the status of the connection
	 */
	public String toShortString(){
		return  ((this.status == NOTREADY)? "N":
			(this.status == CLOSED)? "C":
				(this.status == FREE)? "F":
					(this.status == OBSOLETE)? "O":
						(this.status == WORKING)? "W": 
		"X") 
		+ ((this.mode == LARGE_QUERY)? "-l": "");
	}
	
}
