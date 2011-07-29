package saadadb.vo.request.query;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import adqlParser.SaadaADQLQuery;
import adqlParser.SaadaDBConsistency;
import adqlParser.SaadaQueryBuilderTools;
import adqlParser.parser.AdqlParser;

/**
 * @author laurent
 * @version $Id$

 */
public class AdqlQuery extends VOQuery {
	private static final int DEFAULTSIZE = 100000;
	private ADQLResultSet resultSet = null;
	private SaadaADQLQuery adqlQuery;
	private int limit;
	private String queryString;

	public AdqlQuery() {
		mandatoryDataParams = new String[]{"query"};
		mandatoryMetaParams = new String[]{};
	}

	@Override
	public ArrayList<Long> getOids() throws Exception {
		return null;
	}

	@Override
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return null;
	}

	public ADQLResultSet getResultSet() {
		return this.resultSet;
	}
	public SaadaADQLQuery getAdqlQuery() {
		return this.adqlQuery;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#isMetaQuery(java.util.Map)
	 */
	public boolean isMetaQuery(Map<String, String> params) {
		return false;
	}

	@Override
	public void close() throws QueryException {
		this.resultSet.close();
		this.resultSet = null;		
	}

	@Override
	public void buildQuery() throws Exception {
		queryString = queryParams.get("query");
		if( queryString == null || queryString.length() == 0 ) {
			QueryException.throwNewException("ERROR", "No ADQL query string given (param \"query\")");
		}
		String l = queryParams.get("limit");
		if( l == null ||l.length() == 0 ) {
			limit = DEFAULTSIZE;
		}
		else {
			try {
				limit = Integer.parseInt(l);
			} catch (Exception e) {
				QueryException.throwNewException("ERROR", "Limit parameter not valid " + l);
			}
		}
		protocolParams.put("query", queryString);
		protocolParams.put("limit", Integer.toString(limit));
	}

	@Override
	public void runQuery() throws Exception {		
		SaadaDBConsistency dbConsistency = new SaadaDBConsistency();
		AdqlParser parse = new AdqlParser(new ByteArrayInputStream(queryString.getBytes()), null, dbConsistency, new SaadaQueryBuilderTools(dbConsistency));
		adqlQuery = (SaadaADQLQuery)parse.Query();

		if (limit > -1)
			adqlQuery.setLimit(limit);
		if( limit <= 0 || limit > DEFAULTSIZE) {
			Messenger.printMsg(Messenger.WARNING, "ADQL result limited to " + DEFAULTSIZE);
			adqlQuery.setLimit(DEFAULTSIZE);
		}
		resultSet = new ADQLResultSet(adqlQuery.runQuery(), dbConsistency.getColumnsMeta());

		if(resultSet == null) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "No query result !");
		}

	}

}
