package saadadb.vo.tap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.util.Messenger;
import tap.db.DBException;

/**
 * Manages tap's queries to the DB to be compliant with Saada.
 * It handles temporary table creation, inserts and drops with the same connection.
 * @author hahn
 *
 */
public class TapToDBTransaction {
	/*
	 * If tap needs multiple tables to be created, an Insert statement is stored for each of these tables
	 */
	private ArrayList<PreparedStatement> insertStatements;
	/*
	 * the current Preparedstatement for the curent Table
	 */
	private PreparedStatement currentStatement;
	/*
	 * After every #createTable, a new PreparedStatement need to be created
	 */
	private boolean needNewStatement = true;
	/*
	 * Needed to determine if the tap query has already been executed. If not, #endTransaction won't close the connection 
	 */
	private boolean tapQueryExecuted = false;
	/*
	 * The Resultset of the TapQuery
	 */
	private ResultSet resultset;
	/*
	 * The connection used to query the DB
	 */
	private Connection connection;
	/*
	 * The query that is excuted after all Create Table and Insert have been done
	 */
	private String tapQuery;

	public TapToDBTransaction() {
		insertStatements = new ArrayList<PreparedStatement>();
	}

	/**
	 * Gets an open connection and opens Transaction
	 * @throws DBException 
	 */
	public void beginTransaction() throws DBException {
		try {	
			connection = Database.getConnector().getNewConnection();
			connection.setAutoCommit(false);
		} catch (Exception e) {
			throw new DBException("Can't begin a DB transaction", e);
		}
	}

	/**
	 * Performs an executeUpdate, Adds the current PreparedStatement in the InsertStatement list and raise the needNewStatement flag.
	 * @param sql
	 */
	public void createTable(String sql) throws DBException {

		if (currentStatement != null) {
			insertStatements.add(currentStatement);
			needNewStatement = true;
		}
		Messenger.locateCode("CRATE TABLE\n"+sql);
		executeUpdate(sql);
	}

	/**
	 * Execute an ExecuteUpdate if the sql query is not empty
	 * @param sql
	 */
	public void executeUpdate(String sql) throws DBException{
		if (!sql.isEmpty() && sql != null) {
			try {
				//System.out.println("Execute Update : " + sql);
				if(connection == null || connection.isClosed()) {
					beginTransaction();
				}
					
				connection.createStatement().executeUpdate(sql);
				
			} catch (SQLException e) {
			throw new DBException("Failed to execute the Query:"+e.getMessage());
			}
		} else {
			Messenger.locateCode("Can't execute Update, sql string is Null or empty");
		}
	}

	/**
	 * Returns the currentStatement or gets a new one if needNewStatement is true
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		if (needNewStatement) {
			//System.out.println("Requesting new PreparedStatement to the connection\n"+sql);
			currentStatement = connection.prepareStatement(sql);
			needNewStatement = false;
		}
		return currentStatement;
	}

	/**
	 * Execute the  Insert statements and then the Tap Query
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
	//	System.out.println("execute Insert Statements");
		if (currentStatement != null) {
			insertStatements.add(currentStatement);
		}
		for (PreparedStatement p : insertStatements) {
			p.executeBatch();
		}
	//	System.out.println("execute TapQuery " + tapQuery);
		if (tapQuery != null && !tapQuery.isEmpty()) {
			resultset = connection.createStatement().executeQuery(tapQuery);
		}
		//System.out.println("Commit");
		connection.commit();
		tapQueryExecuted = true;
	}

	/**
	 * 
	 * @return the result of TapQuery
	 */
	public ResultSet getResultset() {
		return resultset;
	}

	/**
	 * close the connection if the Tap query has been executed, do nothing otherwise
	 * @throws Exception
	 */
	public void endTransaction() throws Exception {
		if (tapQueryExecuted) {
			if (connection != null && !connection.isClosed())
				connection.close();
		} 
		//else
//			System.out.println("Not closing, there are still queries to process");
	}

	/**
	 * Sts the TapQuery
	 * @param sql
	 */
	public void setTapQuery(String sql) {
		tapQuery = sql;
	}

	/**
	 * Forces the connectino to close
	 * @throws Exception
	 */
	public void forceCloseTransaction() throws Exception {

		// Spooler.getSpooler().give(connection);
		if (connection != null && !connection.isClosed())
			connection.close();
	}
	
	/**
	 * Rollback the current DB transaction
	 * @throws Exception
	 */
	public void rollback() throws Exception {
		if (connection != null && !connection.isClosed()) {
			connection.rollback();
		}
	}
}
