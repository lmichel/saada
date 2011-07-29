package saadadb.vo;

import java.io.ByteArrayInputStream;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import adqlParser.SaadaADQLQuery;
import adqlParser.SaadaDBConsistency;
import adqlParser.SaadaQueryBuilderTools;
import adqlParser.parser.AdqlParser;

/**
 * As the {@link SaadaQLExecutor} class the ADQLExecutor class executes a query but written in ADQL.
 *   * @version $Id$

 * @author Gregory Mantelet
 */
public class ADQLExecutor {
	
	/** The last executed query. */
	protected SaadaADQLQuery query = null;

	/** The result of the last executed query. */
	protected SaadaQLResultSet query_result = null;
	
	public SaadaADQLQuery getQuery() {
		return query;
	}

	public SaadaQLResultSet getQuery_result() {
		return query_result;
	}

	/**
	 * Executes an ADQL query and returns a list of oids.
	 * @param 	queryStr	The ADQL query to execute.
	 * @return 	The oids of the matched data.
	 */
	public SaadaQLResultSet execute(String queryStr) throws Exception {
		return execute(queryStr, -1);
	}

	/**
	 * Executes an ADQL query and returns a list of oids.
	 * @param 	queryStr	The ADQL query to execute.
	 * @return 	The oids of the matched data.
	 */
	public SaadaQLResultSet execute(String queryStr, int limit) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "ADQL executor : ADQL query received :\n" + queryStr);
		
		// Execute the given query:
		SaadaDBConsistency dbConsistency = new SaadaDBConsistency();
		AdqlParser parse = new AdqlParser(new ByteArrayInputStream(queryStr.getBytes()), null, dbConsistency, new SaadaQueryBuilderTools(dbConsistency));
//		parse.setDebug(true);
		query = (SaadaADQLQuery)parse.Query();
		
		if (limit > -1)
			query.setLimit(limit);
		if( limit <= 0 || limit > 50000) {
			Messenger.printMsg(Messenger.WARNING, "ADQL result limited to 50000");
			query.setLimit(50000);
		}
		query_result = new ADQLResultSet(query.runQuery(), dbConsistency.getColumnsMeta());
		
		
		// If no result throw an exception:
		if(query_result == null)
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No query result !"/*query.getErrorReport()*/);
		
		return query_result;
	}
	
	public void close() throws QueryException {
		if( query != null) {
			query.close();
		}
	}
	
}
