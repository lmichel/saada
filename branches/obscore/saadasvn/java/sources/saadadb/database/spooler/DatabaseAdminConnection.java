package saadadb.database.spooler;

import java.sql.SQLException;

import saadadb.database.Database;

/**
 * Subclass of {@link DatabaseConnection} designed to handle connection updating the database (except for SQLITE)
 * @author michel
 * @version $Id$
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
	DatabaseAdminConnection(int number, String password) throws SQLException {
		super(number);
		this.password = password;
		this.connect();

	}

	/**
	 * Connect the DB with the admin account
	 * @throws SQLException
	 */
	private void connect() throws SQLException{
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
}
