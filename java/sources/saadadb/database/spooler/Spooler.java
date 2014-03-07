/**
 * 
 */
package saadadb.database.spooler;

import java.sql.SQLException;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.resourcetest.SpoolerTester;
import saadadb.util.Messenger;

/**
 * Connection Spooler. 
 * Manage a list of connections {@link DatabaseConnection}.
 * The list s managed by a forever thread
 * The list grows gradually on demand until it reaches the max size. 
 * Closed connections are removed from the list and are replaced with fresh connections
 * The max size can be set by the constructor.
 * USAGE:
 				Spooler spooler = Spooler.getSpooler();
				DatabaseConnection connectionReference = spooler.getConnection();
				Statement stmt = connectionReference.getLargeStatement();
				stmt.execute(getQuery());
				.. some processing ............
				spooler.give(connectionReference);

 *
 * Tested by the class {@link SpoolerTest}
 * @author michel
 * @version $Id$
 *
 * 02/2014: Add the admin connection, with a lot of impact
 */
public class Spooler { 
	/**
	 * Default max number of connection: can be overridden by a constructor
	 */
	public static final int MAX_CONNEXION = 20;
	/**
	 * delay applied to loop waiting one connection to be free (ms)
	 */
	public static final int WAIT_DELAY = 100;
	/**
	 * Period of the main loop of the thread  controlling the consistency of the connection list (ms)
	 */
	public static final int CHECK_PERIOD = 60;
	/**
	 * Period of the spooler reporting (active in debug mode) (multiple of CHECK_PERIOD)
	 */
	public static final int REPORT_PERIOD = 10000;
	private ArrayList<DatabaseConnection> connectionsReferences = new ArrayList<DatabaseConnection>();
	/**
	 * DB connection the the admin privilege
	 */
	private DatabaseConnection adminConnection;
	/**
	 * Reference to the singleton instance
	 */
	private static Spooler instance;
	/**
	 * Incremental numer giev to the Database Connections (debug purpose)
	 */
	private int numConnection = 0;
	/**
	 * Actual max number of connections. Can be constrained by the server setup
	 */
	private int maxConnections;
	private boolean spoolerIsRunning=false;
	private boolean checkerIsRunning=false;

	/*************************************************************************
	 * Singleton pattern:
	 */
	/**
	 * Return the unique instance and build it at the first call
	 * The max number of connection is limited by {@link Spooler#MAX_CONNEXION}
	 * @return returns the instance
	 * @throws FatalException 
	 */
	synchronized public static Spooler getSpooler() throws Exception {
		if( instance == null ) {
			instance = new Spooler();
		}
		return instance;
	}

	/**
	 * Return the unique instance and build it at the first call
	 * The max number of connection is limited by maxConnections
	 * @param maxConnections max number of connections (wished)
	 * @return returns the instance
	 * @throws Exception
	 */
	synchronized public static Spooler getSpooler(int maxConnections) throws Exception {
		if( instance == null ) {
			instance = new Spooler(maxConnections);
		}
		return instance;
	}
	/**
	 * Destroy the instance.
	 * For test purpose {@link SpoolerTester}
	 */
	synchronized public static void reset()  {
		instance = null;
	}

	/*************************************************************************
	 * Spooler intialisation
	 */
	/**
	 * Constructor
	 * @throws FatalException 
	 */
	private Spooler() throws Exception{
		int m = Database.getWrapper().getHardReaderConnectionLimit();
		this.maxConnections = ( m != -1 )? m: MAX_CONNEXION;
		this.checkAvailableConnections();
		this.spoolerIsRunning = true;
		/*
		 * Make sure to start with at least one connection
		 */
		this.addConnectionReference();
		(new Thread(new ListChecker())).start();
		Messenger.printMsg(Messenger.TRACE, "Spooler ready");
	}
	/**
	 * Constructor
	 * @param maxConnections max number of connections (wished)
	 * @throws Exception
	 */
	private Spooler(int maxConnections) throws Exception{
		int m = Database.getWrapper().getHardReaderConnectionLimit();
		this.maxConnections = ( m != -1 )? m: maxConnections;
		this.checkAvailableConnections();
		this.spoolerIsRunning = true;
		/*
		 * Make sure to start with at least one connection
		 */
		this.addConnectionReference();
		(new Thread(new ListChecker())).start();
		Messenger.printMsg(Messenger.TRACE, "Spooler ready");
	}

	/**
	 * Check the max number of connections which can be open, and take this n umber to limit the buffer size.
	 * All connections are closed after
	 * @throws FatalException 
	 * 
	 */
	private void checkAvailableConnections() throws Exception {
		ArrayList<DatabaseConnection> crs = new ArrayList<DatabaseConnection>();
		int i;
		for( i=0 ; i<this.maxConnections ; i++) {
			DatabaseConnection cr = new DatabaseConnection(i);
			if( !cr.isFree() ) {
				break;
			} else {
				crs.add(cr);
			}
		}
		this.maxConnections = i;
		for( DatabaseConnection cr : crs){
			cr.close();
		}
		if( this.maxConnections < 1 ) {
			FatalException.throwNewException(SaadaException.DB_ERROR, "No available reader connections ");
		}
		Messenger.printMsg(Messenger.TRACE, "Spooler currently supports up to " + this.maxConnections + " connections");
	}

	/*************************************************************************
	 * Public interface
	 */

	/**
	 * Close all connections referenced by the Spool with a grace delay (3") for the running queries.
	 * Do not destroy the Spooler
	 * @throws SQLException
	 */
	public void close() throws Exception {
		this.spoolerIsRunning = false;
		int cpt = 0;
		while( this.checkerIsRunning && cpt < 15) {
			Thread.sleep(200);
			cpt++;
		}
		synchronized (this) {
			for( DatabaseConnection cr : this.connectionsReferences){
				cr.close();
			}	
			this.connectionsReferences = null;		
		}
		Messenger.printMsg(Messenger.TRACE, "Spooler stopped");
	}

	/**
	 * Do not return while a free connection has not been found out
	 * @return the first free connection.
	 * @throws Exception
	 */
	public DatabaseConnection getConnection() throws Exception {
		if( !this.spoolerIsRunning ) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, this + "Attempt to get a connection from a spooler which is not running");
		}
		DatabaseConnection retour;
		while( (retour = this.getFirstFreeConnection()) == null ) {
			Thread.sleep(WAIT_DELAY);
		}
		return retour;		
	}
	/**
	 * @return
	 * @throws Exception
	 */
	public DatabaseConnection getAdminConnection() throws Exception {
		if( !this.spoolerIsRunning ) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, this + "Attempt to get a connection from a spooler which is not running");
		} else if( this.adminConnection == null) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, this + "Attempt to get an admin connection while the admin mode han=sn't been set.");
		} else {
			while( !this.adminConnection.isFree()  ) {
				Thread.sleep(WAIT_DELAY);
			}
			/*
			 * With SQLITE it is always better to close and reopen the connection in order to allow it to get the "write_lock" locking level, 
			 * which is necessary to modify the DB schema.
			 */
			if( !Database.getWrapper().supportDropTableInTransaction() ) {
				this.adminConnection.reconnect();
			}
			return this.adminConnection;		
		}
		return null;	
	}

	/**
	 * Must be invoked after the connection has been used.
	 * @param connectionReference
	 * @throws SQLException
	 */
	public void give(DatabaseConnection connectionReference) throws SQLException{
		if(connectionReference != null ) {
			connectionReference.give();
		}
	}
	/**
	 * Give back the admin connection
	 * @throws SQLException
	 */
	public void giveAdmin() throws SQLException{
		this.adminConnection.give();
	}

	/**
	 * @param password
	 * @throws Exception
	 */
	public void openAdminConnection(String password) throws Exception{
		if( this.adminConnection == null ) {
			if( Database.getWrapper().supportConcurrentAccess() ) {
				this.adminConnection = new DatabaseAdminConnection(-1, password);
			} else {
				this.adminConnection = this.connectionsReferences.get(0);
			}
		}		
	}

	/**
	 * @return
	 */
	public boolean isRunning() {
		return this.spoolerIsRunning;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s="Spooler: " +connectionsReferences.size() + "/" + this.maxConnections + " ";
		for( DatabaseConnection cr: connectionsReferences) s += cr.toShortString();
		s += " [" + ((adminConnection == null)? "-": adminConnection.toShortString()) + "]";
		return s;

	}

	/****************************************************************************
	 * Internal management methods
	 */
	/**
	 * Appends a new connection to the list
	 * @throws SQLException
	 */
	private void addConnectionReference() throws Exception{
		connectionsReferences.add(new DatabaseConnection(this.numConnection++));
	}
	/**
	 * Returns the first free connection if there is, returns null otherwise
	 * @throws Exception
	 * @return
	 */
	private DatabaseConnection getFirstFreeConnection() throws Exception{
		synchronized (this) {
			for(DatabaseConnection connectionReference: connectionsReferences){
				if(connectionReference.isFree()){
					/*
					 * Working flag must be set within the synchronized code to make sure another thread won't take it to
					 */
					connectionReference.startWorking();
					return connectionReference;
				}
			}
			return null;
		}
	}

	/**
	 * Check the connection list. remove the closed connection and complete the list in order to make sure 
	 * there at least one available connection while the max size is not reached
	 * 
	 * @throws SQLException
	 */
	synchronized private void completeConnectionsReferences() throws Exception {
		/*
		 * Remove the obsolete connections
		 */
		ArrayList<DatabaseConnection> toRemove = new ArrayList<DatabaseConnection>();
		for( DatabaseConnection cr: connectionsReferences){
			if( cr.isObsolete() ){
				toRemove.add(cr);
			}
		}
		int nbToRemove = toRemove.size();
		if (nbToRemove > 0 ) {
			for( DatabaseConnection cr: toRemove){
				connectionsReferences.remove(cr);
			}
			/*
			 * Replace the obsolete connection with new ones + one while the max is not reached
			 */
			for( int i=0 ; i<nbToRemove ; i++ ){
				if( connectionsReferences.size() < maxConnections) {
					this.addConnectionReference();
					break;
				}
			}
		}
		boolean hasFree = false;
		for( DatabaseConnection cr: connectionsReferences){
			if( cr.isFree() ){
				hasFree = true;
				break;
			}
		}
		if( !hasFree && connectionsReferences.size() < maxConnections) {
			this.addConnectionReference();
		}
	}

	/**
	 * Inner class checking in background the availability of connections in background
	 * @author michel
	 * @version $Id$
	 *
	 */
	class ListChecker implements Runnable {

		@Override
		public void run() {
			checkerIsRunning = true;
			if (Messenger.debug_mode) {
				Messenger.printMsg(Messenger.DEBUG, "ListChecker starting");
			}
			int cpt = 0;
			while( spoolerIsRunning ) {
				try {
					Thread.sleep(CHECK_PERIOD);	
					completeConnectionsReferences();
					if (Messenger.debug_mode) {
						if( cpt > REPORT_PERIOD ){
							Messenger.printMsg(Messenger.DEBUG, Spooler.this.toString());
							cpt = 0;
						}
						cpt++;
					}
				} catch (Exception e) {
					checkerIsRunning = false;	
					spoolerIsRunning = false;
					Messenger.printStackTrace(e);
					break;
				}
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "ListChecker stopping");
			checkerIsRunning = false;			
		}
	}
}
