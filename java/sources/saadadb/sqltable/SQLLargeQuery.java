package saadadb.sqltable;

import java.sql.ResultSet;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * Run query on large collection with a JDBC cursor. No memroyHeapSpace error 
 * Cannot scroll back: Must be used to generate result files not to browse data
 * Possible issues with SaadaResultSet because the scrolling is off. 
 * The scrolling works on one fetch, so the apparent size of the result set id 1000 ( the fetch size)
 * This kind of query must use a "oneshot" connection. As there is no commit, the postmaster get fatter as long as new queries are done.
 * If we put query, PSQL complains about cursor out transaction blocks. 
 */
public class SQLLargeQuery extends SQLQuery {
	/**
	 * @throws QueryException
	 */
	public SQLLargeQuery() throws QueryException {
		super();
	}
	/**
	 * @param query
	 * @throws QueryException
	 */
	public SQLLargeQuery(String query) throws QueryException {
		super(query);
	}	

	public ResultSet run() throws  QueryException {
		Exception te=null;
		try {
			_stmts = databaseConnection.getLargeStatement();
			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Select large query: " + this.query);
			long start = System.currentTimeMillis();
			resultset = _stmts.executeQuery(query); 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Done in " + ((System.currentTimeMillis()-start)/1000F) + " sec");
			return resultset;

		} catch (SaadaException e) {
			te = e;
			this.close();
			QueryException.throwNewException(SaadaException.DB_ERROR, te);
		} catch (Exception e) {
			te = e;
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			this.close();
			QueryException.throwNewException(SaadaException.DB_ERROR, te);
		}
		return null;
	}
}
