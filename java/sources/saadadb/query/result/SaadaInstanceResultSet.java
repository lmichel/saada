package saadadb.query.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.relationship.KeyIndex;
import saadadb.sqltable.SQLLargeQuery;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/**
 * This class run an SQL query and store a pattern result set.
 * This kind of result set must be used to write query report in one shoot. The result set 
 * remains open while the close method is not called.
 * Not calling @see close could lock the DB in case of SQLite use.
 * Access is only in sequential mode
 * The current row is returned as a SaadaInstance supposed to instanciate the right class.
 * No check is done. 
 * Method requiring random access are implemented but throw exceptions
 *  * @version $Id$

 * @author laurentmichel
 * 03/2011
 *
 */
public class SaadaInstanceResultSet  {
	/**
	 * Instance modeling one result row. Supposed to have he right class
	 */
	private SaadaInstance instance;

	/**
	 * SQL result set, set by @see sq 
	 */
	protected ResultSet resultSet;
	protected Set<String> colNames = new TreeSet<String>();
	/**
	 * SQL query executor. Store as a field to keep available for closing
	 */
	protected SQLLargeQuery sqlQuery;
	protected KeyIndex patternKeySet;
	protected boolean initDone = false;
	protected int limit = SaadaConstant.INT;
	protected int currentPtr = SaadaConstant.INT;
	protected int size = SaadaConstant.INT;
	protected static final Pattern whereDetector = Pattern.compile(".*\\s+(where|order)\\s+.*", Pattern.CASE_INSENSITIVE);

	
	/**
	 * Dummy constructor allowing to override the class without executing the query at creation time
	 */
	public SaadaInstanceResultSet() {
		
	}

	/**
	 * Nothing else than init params
	 * @param instance
	 * @param sqlQuery
	 * @param patternKeySet
	 * @param limit
	 * @param siwe the result size must be given for JDBC driver running in forard mode
	 * @throws QueryException 
	 */
	public SaadaInstanceResultSet(SaadaInstance instance, String sqlQuery, KeyIndex patternKeySet, int limit, int size) throws QueryException {
		this.instance = instance;
		this.sqlQuery = new SQLLargeQuery(sqlQuery);
		this.patternKeySet = patternKeySet;
		this.limit = limit;
		if( size != SaadaConstant.INT) {
			this.size = size;
		}

	}

	/**
	 * Run the SQl query and store both result set and query executor. This method is called 
	 * by accessors
	 * @throws Exception any Exception is thrown to the calling method
	 */
	private void init() throws Exception {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Execute SQL query: " + this.sqlQuery);
		this.resultSet = sqlQuery.run();
		this.initDone = true;
		this.currentPtr = 0;			
		ResultSetMetaData rsmd = this.resultSet.getMetaData();
		for( int i=1 ; i<=rsmd.getColumnCount() ; i++ ) {
			colNames.add(rsmd.getColumnName(i));
		}

//		if( !Database.getWrapper().forwardOnly) {
//			this.resultSet.afterLast();
//			this.size = this.resultSet.getRow();
//			this.resultSet.beforeFirst();
//		}
	}

	/**
	 * Call the @see OidsaadaResultSet.init method and increment the result pointer.
	 * @return true if the end is not reached
	 * @throws QueryException any exception is forwarded as @see QueryException
	 */
	public boolean next() throws QueryException {
		try {
			if( !initDone ) {
				this.init();
			}
			if( currentPtr >= limit ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Result truncated to " + limit); 
				return false;
			}

			if( patternKeySet == null ) {
				currentPtr++;
				return resultSet.next();
			} else {
				while( resultSet.next() ) {
					if( patternKeySet.hasKey(resultSet.getLong("oidsaada"), true) != -1 )  {
						currentPtr++;
						return true;
					}
				}
				return false;
			}

		} catch(Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return false;
	}
	
	/**
	 * Return the oid from the current index
	 * @return the oid or @see util.SaadaConstant.LONG if any error occurs
	 * @throws QueryException any exception is forwarded as @see QueryException
	 */
	public long getOId() throws QueryException {
		try {
			return resultSet.getLong("oidsaada");
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return SaadaConstant.LONG;
	}
	
	/**
	 * Random access the the result list. @see OidsaadaResultSet.init the result set if needed
	 * @param rank of the requested OID
	 * @return the oid or @see util.SaadaConstant.LONG if any error occurs such as an out of bounds error
	 * @throws QueryException 
	 */
	public long getOId(int rank) throws QueryException  {
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Scrollable mode not supported by SaadaInstanceResultSet");
		return SaadaConstant.LONG;

	}
	

	/**
	 * Returns a sublist of oids.
	 * @param start beginning of the list
	 * @param length length of the list
	 * @return
	 * @throws QueryException 
	 */	
	 public List<Long> getPage(int start, int length) throws QueryException {
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Scrollable mode not supported by SaadaInstanceResultSet");
		return null;	
		}

	
	/**
	 * Close the query executor.
	 * @throws QueryException
	 */
	public void close() throws QueryException {
		if( sqlQuery!= null) {
			try {
				sqlQuery.close();
			} catch (QueryException e) {
				QueryException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
	}

	/**
	 * Return the size of the result list. @see OidsaadaResultSet.init the result set if needed
	 * @return size of the result list
	 * @throws QueryException any exception is forwarded as @see QueryException
	 */
	public int size( ) throws QueryException{
		QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Scrollable mode not supported by SaadaInstanceResultSet");
		return this.size;
	}

	/**
	 * Set the SaadaInstance with the content of the current row.
	 * @return
	 * @throws QueryException any Exception is forwarded as a QueryException
	 */
	public SaadaInstance getInstance() throws QueryException {
		try {
			long oidsaada = resultSet.getLong("oidsaada");
			String classOrg = this.instance.getClass().getName();
			String realClass = SaadaOID.getClassName(oidsaada);
			if( !classOrg.endsWith("." + realClass)) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Change class in query result " + classOrg + " " + realClass);
				this.instance = (SaadaInstance)  SaadaClassReloader.forGeneratedName(realClass).newInstance();

			}
			this.instance.init(resultSet, colNames);
			return this.instance;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}

}
