package saadadb.exceptions;

/**
 * @author michel
 * @version $Id$
 *
 */
public class QueryException extends SaadaException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public QueryException(String msg, String context) {
		super(msg, context);
	}

	/* (non-Javadoc)
	 * @see saadadb.exceptions.SaadaException#throwNewException(java.lang.String, java.lang.Exception)
	 */
	public static void throwNewException(String msg, Exception e) throws QueryException {
 		throw new QueryException(msg, SaadaException.getExceptionMessage(e));
	}
	
	/**
	 * @param msg
	 * @param context
	 * @throws QueryException
	 */
	public static void throwNewException(String msg, String context) throws QueryException {
		throw new QueryException(msg, context);
	}
}
