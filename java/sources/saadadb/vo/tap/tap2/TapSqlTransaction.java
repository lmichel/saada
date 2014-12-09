package saadadb.vo.tap.tap2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.QueryException;

public class TapSqlTransaction {

	public class Query {
		String query;
		boolean resultExpected;

		public Query(String query, boolean resultExpected) {
			this.query = query;
			this.resultExpected = resultExpected;
		}

		public String getQuery() {
			return query;
		}

		public boolean isResultExpected() {
			return resultExpected;
		}
	}

	protected ArrayList<Query> queries;
	protected ResultSet resultSet;
	protected Statement stmt;
	protected DatabaseConnection connection;
	protected boolean isOpen = false;

	public TapSqlTransaction() {
		queries = null;
		queries = new ArrayList<TapSqlTransaction.Query>();
	}

	// public void beginTransaction() {
	// this.isOpen=true;
	// }

	public void addQueryToTransaction(String query, boolean resultExpected) throws QueryException {
//		if (queries == null )//|| !this.isOpen)
//			throw new QueryException());

		queries.add(new Query(query, resultExpected));
		System.out.println("Query #" + queries.size() + " added to transaction");
	}

	/**
	 * Commit all added queries by using addQueryToTransaction
	 * @throws Exception
	 */
	public void commitTransaction() throws Exception {
		if (queries == null | queries.size() == 0)// || !isOpen)
			throw new QueryException("Can't commit transaction : No query to commit", "");
		if (connection == null) {
			connection = Spooler.getSpooler().getConnection();
			connection.getConnection().setAutoCommit(false);
			stmt = connection.getStatement();
		}
		Iterator<Query> it = queries.iterator();
		Query query;

		while (it.hasNext()) {
			query = it.next();
			System.out.println("Execute query : " + query.getQuery());
			if (query.resultExpected) {
				resultSet = stmt.executeQuery(query.getQuery());
			} else {
				stmt.executeUpdate(query.getQuery());
			}
		}
		queries.clear();
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void endTransaction() throws Exception {
		if (stmt != null && queries.size() == 0) {
			stmt.close();
		Spooler.getSpooler().give(connection);
		}else System.out.println("Not closing, there are still queries to process");
		// isOpen =false;
	}
	
	public void forceCloseTransaction() throws Exception {
		if(stmt != null) {
			stmt.close();
		}
		Spooler.getSpooler().give(connection);
	}
}
