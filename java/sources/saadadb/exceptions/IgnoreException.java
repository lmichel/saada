package saadadb.exceptions;

import java.util.TreeSet;

import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class IgnoreException extends SaadaException {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Message of exception to be ignored (set when "remember my decision" is OK)
	 */
	private static TreeSet<String> to_ignore = new TreeSet<String>();
	
	/**
	 * @param msg
	 */
	public IgnoreException(String core_msg, String context) {
		super(core_msg, context);
	}

	
	/**
	 * @param msg
	 * @return returns true if msg must be ignored
	 */
	public static boolean isIgnored(String msg){
		return to_ignore.contains(msg);
	}
	
	/**
	 * @param msg
	 * @return returns true if exception msg must be ignored
	 */
	public static boolean isIgnored(Exception e){
		return to_ignore.contains(e.getMessage());
	}
	
	/**
	 * From now, all exception with msg as message will no longer interrupt the process
	 * @param msg
	 * @return 
	 */
	public static void mustIgnore(String msg) {
		to_ignore.add(msg);
	}
	
	/**
	 * From now, all exception with e.getMessage will no longer interrupt the process
	 * @param msg
	 * @return 
	 */
	public static void mustIgnore(Exception e) {
		to_ignore.add(e.getMessage());
	}
	
	/**
	 * From now, all exceptions with msg as message will no longer be ignored
	 * @param msg
	 * @return 
	 */
	public static void mustNotIgnore(String msg) {
		to_ignore.remove(msg);
	}
	
	/**
	 * From now, all exceptions with e.getMessage() as message will no longer be ignored
	 * @param msg
	 * @return 
	 */
	public static void mustNotIgnore(Exception e) {
		to_ignore.remove(e.getMessage());
	}
	
	/**
	 * From know, no exception wil be ignored
	 */
	public static void ignoreAll() {
		to_ignore = new TreeSet<String>();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String ign = "";
		if( isIgnored(this.getMessage())) {
			ign = "(ignored)";
		}
		return "IgnoreException " + ign + ": " + this.getMessage() + ", Context: " + this.getContext();
	}
	
	/**
	 * Relay the Exception the with an IgnoreException. Msg is also used to check if the 
	 * relayed IgnoredException can be ignored. The stack trace of e is printed out.
	 * @param msg
	 * @param e
	 * @throws IgnoreException
	 */
	public static void throwNewException(String msg, Exception e) throws IgnoreException {
		throw new IgnoreException(msg, SaadaException.getExceptionMessage(e));
	}
	
	/**
	 * Throw a Exception with msg used to check is it can be ignored
	 * @param msg
	 * @param context
	 * @throws IgnoreException
	 */
	public static void throwNewException(String msg, String context) throws IgnoreException {
		throw new IgnoreException(msg, context);
	}
	/**
	 * @throws IgnoreException 
	 * 
	 */
	public static void m1() throws IgnoreException {
		String  x = null;
		String[] y = new String[3];
		try {
			System.out.println(y[12]);
			System.out.println(x.length());
		}
		catch(Exception e) {
			IgnoreException.throwNewException("Message", e);
		}
	}
	/**
	 * @throws IgnoreException 
	 * 
	 */
	public static void m2() throws IgnoreException {
		m1();
	}
	/**
	 * 
	 */
	public static void m3() {
		try {
			m2();
		}
		catch(IgnoreException e) {
			if( Messenger.trapIgnoreException(e) == Messenger.ABORT)  {
				System.exit(1);
			}
		}
	}
	
	public static void main (String[] args) {
		while( true ) {
			IgnoreException.m3();
		}
		
	}
}
