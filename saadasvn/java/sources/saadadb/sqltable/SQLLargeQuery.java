package saadadb.sqltable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import saadadb.database.Database;
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
	Connection large_connection;
	/**
	 * @throws QueryException
	 */
	public SQLLargeQuery() throws QueryException {
		try {
			nb_open++;
			large_connection = Database.getWrapper().openLargeQueryConnection();
			//			large_connection = DriverManager.getConnection(Database.getConnector().getJdbc_url(),Database.getConnector().getJdbc_reader(), Database.getConnector().getJdbc_reader_password());
			//			large_connection.setAutoCommit(false);
			//			large_connection = Database.getConnector().getJDBCConnection();
			_stmts =large_connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
			Database.getWrapper().setFetchSize(_stmts,5000);
		} catch(Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}
	/**
	 * @param query
	 * @throws QueryException
	 */
	public SQLLargeQuery(String query) throws QueryException {
		this();
		nb_open++;
		try {
			this.query = query;
		} catch(Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}	

	public ResultSet run() throws  QueryException {
		try {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Select large query: " + this.query);
			long start = System.currentTimeMillis();
			resultset = _stmts.executeQuery(query); 
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Done in " + ((System.currentTimeMillis()-start)/1000F) + " sec");
			return resultset;
		

		} catch (Exception e) {
			Messenger.printMsg(Messenger.ERROR, "Query: " + query);
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return null;
	}


	/**
	 * @throws QueryException
	 */
	@Override
	public void close() throws QueryException {
		super.close();
		try {
			_stmts.close();
			Database.getWrapper().closeLargeQueryConnection(large_connection);
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		} catch (SQLException e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

}
