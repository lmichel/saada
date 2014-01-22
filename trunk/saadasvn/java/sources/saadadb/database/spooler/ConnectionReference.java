/**
 * 
 */
package saadadb.database.spooler;

import java.sql.Connection;

/**
 * @author laurentmichel
 *
 */
public class ConnectionReference {
	private final int number;
	private int status;
	public static final int NOTREADY = 0;
	public static final int FREE = 1;
	public static final int CLOSED = 2;
	public static final int WORKING = 3;
	public static final int OBSOLETE = 3;
	private Connection connection = null;

	ConnectionReference(int number){
		this.number = number;
		this.status = NOTREADY;
		this.connect();
	}
	private void connect(){
		System.out.println("Connect");
		this.status = FREE;
	}
	private void close(){
		System.out.println("Close");
		this.connection = null;
		this.status = CLOSED;		
	}
	protected  Connection getConnection() {
		this.status = WORKING;
		return connection;
	}
	protected void give() {
		this.close();
		this.status = FREE;
	}
	/*
	 * Status getter
	 */
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
		return (this.status == WORKING)? true: false;
	}
	public String toString(){
		return "ConnectionReference #" + this.number + " " 
		+ ((this.status == NOTREADY)? "NOTREADY":
			(this.status == CLOSED)? "CLOSED":
				(this.status == FREE)? "FREE":
					(this.status == OBSOLETE)? "OBSOLETE":
						(this.status == WORKING)? "WORKING": 
		"UNKNOWN STATUS");
	}
}
