/**
 * 
 */
package saadadb.database.spooler;

import java.sql.Connection;
import java.util.ArrayList;

import saadadb.database.Database;

/**
 * Singleton pattern
 * @author laurentmichel
 *
 */
public class Spooler {
	private ArrayList<ConnectionReference> connctionsReferences = new ArrayList<ConnectionReference>();
	private int maxConnections = 2;
	private static Spooler instance;
	private int numConnection = 0;
	public static final int DELAY = 100;

	private Spooler(){
		for( int i=0 ; i<maxConnections ; i++ ){
			this.addConnectionReference();
		}
	}

	public static Spooler getSpooler() {
		if( instance == null ) {
			instance = new Spooler();
		}
		return instance;
	}

	synchronized public ConnectionReference getConnection() throws InterruptedException {
		ConnectionReference retour;
		while( (retour = this.getFirstFreeConnection()) == null ) {
			this.completeConnectionsReferences();
			Thread.sleep(DELAY);
		}
		this.completeConnectionsReferences();
		return retour;					
	}

	private void addConnectionReference(){
		connctionsReferences.add(new ConnectionReference(this.numConnection++));
	}
	
	public ConnectionReference getFirstFreeConnection() {
		for(ConnectionReference connectionReference: connctionsReferences){
			if(connectionReference.isFree()){
				System.out.println("Return connection " + connectionReference);
				return connectionReference;
			}
		}
		return null;
	}
	
	synchronized private void completeConnectionsReferences() {
		/*
		 * Remove the obsolete connections
		 */
		ArrayList<ConnectionReference> toRemove = new ArrayList<ConnectionReference>();
		for( ConnectionReference cr: connctionsReferences){
			if( cr.isObsolete() ){
				toRemove.add(cr);
			}
		}
		int nbToRemove = toRemove.size();
		for( ConnectionReference cr: toRemove){
			connctionsReferences.remove(cr);
		}
		/*
		 * Replace the obsolete connection with new ones + one while the max is not reached
		 */
		for( int i=0 ; i<=nbToRemove ; i++ ){
			if( connctionsReferences.size() < maxConnections) {
				this.addConnectionReference();
			}
		}
	}

	public void give(ConnectionReference connectionReference){
		if(connectionReference != null ) {
			connectionReference.give();
		}
	}

	class ClientEmulator implements Runnable {
		@Override
		public void run() {
			try {
				System.out.println("Start Thread");
				Spooler spooler = Spooler.getSpooler();
				ConnectionReference connectionReference = spooler.getConnection();
				if( connectionReference == null ) {
					System.out.println("No connection");
				} else {
					connectionReference.getConnection();
					Thread.sleep(1000);
					spooler.give(connectionReference);
				}	
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}

	public void emulate() {
		ClientEmulator clientEmulator = new ClientEmulator();
		(new Thread(clientEmulator)).start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Spooler spooler = Spooler.getSpooler();
		spooler.emulate();
		spooler.emulate();
		spooler.emulate();
		spooler.emulate();
		spooler.emulate();
	}
}
