package saadadb.exceptions;

import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

/**
 * @author michel
 * * @version $Id$

 */
public class AbortException extends FatalException {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param core_msg
	 * @param context
	 */
	public AbortException(String core_msg, String context) {
		super(core_msg, context);
	}

	
	/**
	 * Attempt to abort the current transaction
	 */
	public static void abort() {
		//(new Exception()).printStackTrace();
		try {
			SQLTable.abortTransaction();
		} catch (Exception e1) {
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.exceptions.IgnoreException#throwNewException(java.lang.String, java.lang.Exception)
	 */
	public static void throwNewException(String msg, Exception e) throws AbortException {
		Messenger.dbAccess();
		AbortException.abort();
		Messenger.procAccess();
		throw new AbortException(msg, SaadaException.getExceptionMessage(e));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.exceptions.IgnoreException#throwNewException(java.lang.String, java.lang.String)
	 */
	public static void throwNewException(String msg, String context) throws AbortException {
		AbortException.abort();
		throw new AbortException(msg, context);
	}

}
