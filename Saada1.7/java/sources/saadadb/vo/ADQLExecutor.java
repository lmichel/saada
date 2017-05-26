package saadadb.vo;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.tap.TAPToolBox;
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
	
	/** flag used to quotes tapschema table names which are also reserved keywords fo mysql **/
	public static final boolean MySQLMode ;

	static {
		boolean msql = false;
		try {
			msql= ( Database.getWrapper() instanceof saadadb.database.MysqlWrapper ) ;
		} catch (FatalException e) {}
		MySQLMode = msql;
	}
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
		AdqlParser parse;
		SaadaDBConsistency dbConsistency;
			dbConsistency = new SaadaDBConsistency();
			parse = new AdqlParser(new ByteArrayInputStream(TAPToolBox.setBooleanInContain(queryStr).getBytes()), null, dbConsistency, new SaadaQueryBuilderTools((SaadaDBConsistency)dbConsistency));
			query = (SaadaADQLQuery)parse.Query();
			query.setMySQLMode(MySQLMode);
//		parse.setDebug(true);
//System.out.println("@@@@@@@@@@@@@@@@@@ " + limit + " " + query.getLimit());			
//(new Exception()).printStackTrace();			
		if (limit > -1)
			query.setLimit(limit);
		if( limit <= 0 || limit > VoProperties.TAP_hardLimit) {
			Messenger.printMsg(Messenger.WARNING, "ADQL result limited to " + VoProperties.TAP_hardLimit);
			query.setLimit(VoProperties.TAP_hardLimit);
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
