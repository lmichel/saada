package saadadb.database.spooler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import saadadb.database.Database;

/**
 * Subclass of {@link DatabaseConnection} designed to handle connection updating the database (except for SQLITE)
 * @author michel
 * @version $Id: DatabaseAdminConnection.java 1053 2014-03-11 16:45:28Z laurent.mistahl $
 *
 */
public class DatabaseAdminConnection extends DatabaseConnection {
	/**
	 * admin password
	 */
	private String password; 

	/**
	 * @param number
	 * @param password
	 * @throws SQLException
	 */
	DatabaseAdminConnection(int number, String password) throws Exception {
		super(number);
		this.password = password;
		this.connect();

	}

	/**
	 * Connect the DB with the admin account
	 * @throws SQLException
	 */
	private void connect() throws Exception{
		this.connection = Database.getConnector().getNewAdminConnection(password);
		this.status = FREE;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.spooler.DatabaseConnection#give()
	 */
	protected void give() throws SQLException {
		this.statement.close();
		this.status = FREE;		
	}

	/* (non-Javadoc)
	 * @see saadadb.database.spooler.DatabaseConnection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException{
		this.connection.setAutoCommit(autoCommit);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.spooler.DatabaseConnection#getStatement()
	 */
	public  Statement getStatement() throws Exception{
		this.status = WORKING;
		this.connection.setAutoCommit(false);
		this.statement = connection.createStatement(Database.getWrapper().getDefaultScrollMode()
				,Database.getWrapper().getDefaultConcurentMode());
		return this.statement;
	}
	/* (non-Javadoc)
	 * @see saadadb.database.spooler.DatabaseConnection#getUpdatableStatement()
	 */
	public  Statement getUpdatableStatement() throws Exception{
		this.status = WORKING;
		this.connection.setAutoCommit(false);
		this.statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
		return this.statement;
	}

}
