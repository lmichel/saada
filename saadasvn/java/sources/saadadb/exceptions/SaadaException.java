package saadadb.exceptions;



abstract public class SaadaException extends Exception{
	public static final String VOTABLE_FORMAT   = "VOTable Format Error";
	public static final String FITS_FORMAT      = "FITS Format Error";
	public static final String FILE_FORMAT      = "File Format Error";
	public static final String FILE_ACCESS      = "File Access Error";
	public static final String STRING_FORMAT    = "Wrong String Format";
	public static final String MISSING_RESOURCE = "Resource not Found";
	public static final String WRONG_RESOURCE   = "Resource has a Wrong Type";
	public static final String USER_ABORT       = "Abort On User Request";
	public static final String WRONG_DB_ROLE    = "DB Role Does Not Exist";
	public static final String CORRUPTED_DB     = "Wrong Result Returned by the DB";
	public static final String WRONG_PARAMETER  = "Wrong Parameter";
	public static final String MISSING_FILE     = "File Does not Exist";
	public static final String MAPPING_FAILURE  = "Mapping Failure";
	public static final String UNSUPPORTED_OPERATION = "Unsupported Operation";
	public static final String UNSUPPORTED_MODE = "Unsupported Mode";
	public static final String UNSUPPORTED_TYPE = "Unsupported Type";
	public static final String METADATA_ERROR   = "Metadata not Found";
	public static final String DB_ERROR         = "DBMS Error";
	public static final String WCS_ERROR        = "Wrong WCS Paramters";
	public static final String INTERNAL_ERROR   = "Internal Error";
	public static final String SYNTAX_ERROR     = "Syntax Error";
	public static final String NO_QUERIED_CLASS = "No Class to Query";
	
	/*
	 * 2nd message giving precision about the error but not taken in account for 
	 * tne "remember my decision" 
	 */
	protected String context;
	/**
	 * @param msg
	 */
	public SaadaException(String msg, String context) {
		super(msg);
		this.context = context;
	}

	/**
	 * @return
	 */
	public String getContext() {
		return context;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.getClass().getName().replace("saadadb.exceptions.", "") +  ": " + this.getMessage() + " - " + this.getContext();
	}

	/**
	 * @param msg
	 * @param e
	 * @throws Exception
	 */
	public static void throwNewException(String msg, Exception e) throws Exception {
		e.printStackTrace();
		throw new IgnoreException(msg, e.toString());
	}
	

	/**
	 * @param msg
	 * @param context
	 * @throws Exception
	 */
	public static void throwNewException(String msg, String context) throws Exception {
		throw new IgnoreException(msg, context);
	}
	
	/**
	 * Avoid the concatenation of message in case of relayed Saada Exceptions
	 * @param e
	 * @return
	 */
	public static String getExceptionMessage(Exception e) {
		if( e instanceof QueryException || e instanceof AbortException || e instanceof FatalException || e instanceof IgnoreException ) {
			return ((SaadaException)e).getContext();
		}
		else {
			return e.toString();
		}
	}
	
	
	public static String toString(Exception e) {
		if( e instanceof QueryException || e instanceof AbortException || e instanceof FatalException || e instanceof IgnoreException ) {
			return e.getClass().getName().replace("saadadb.exceptions.", "") +  ": " + e.getMessage() + " - " + getExceptionMessage(e);
		}
		else {
			return e.toString();			
		}
	}
	
	public static String toHTMLString(Exception e) {
		if( e instanceof QueryException || e instanceof AbortException || e instanceof FatalException || e instanceof IgnoreException ) {
			return "<HTML><B>" + e.getClass().getName().replace("saadadb.exceptions.", "") 
			     + ": </B>" + e.getMessage().replaceAll("<", "&lt;") .replaceAll(">", "&gt;")
			     + "<BR>" + getExceptionMessage(e).replaceAll("<", "&lt;") .replaceAll(">", "&gt;");
		}
		else {
			return e.toString();			
		}
	}
}

  
