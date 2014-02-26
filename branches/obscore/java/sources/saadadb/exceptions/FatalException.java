package saadadb.exceptions;


/**
 * Excecption thrown on fatal errors: halts the process
 * @author michel
 *
 */
public class FatalException extends SaadaException{
	/**
	 *  * @version $Id: FatalException.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param msg
	 */
	public FatalException(String msg, String context) {
		super(msg, context);
	}

	/* (non-Javadoc)
	 * @see saadadb.exceptions.SaadaException#throwNewException(java.lang.String, java.lang.Exception)
	 */
	public static void throwNewException(String msg, Exception e) throws FatalException {
		throw new FatalException(msg, SaadaException.getExceptionMessage(e));
	}
	
	/*	
	 * @param msg
	 * @param context
	 * @throws IgnoreException
	 */
	public static void throwNewException(String msg, String context) throws FatalException {
		throw new FatalException(msg, context);
	}

}
