package saadadb.vo.request.query;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.vo.tap.TAPToolBox;
import adqlParser.SaadaADQLQuery;
import adqlParser.SaadaDBConsistency;
import adqlParser.SaadaQueryBuilderTools;
import adqlParser.parser.AdqlParser;
import adqlParser.parser.QueryBuilderTools;

/**
 * @author laurent
 * @version 07/2011
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
		if( this.adqlQuery != null )this.adqlQuery.close();
		this.adqlQuery = null;		
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
		protocolParams.put("query", TAPToolBox.setBooleanInContain(queryString));
		protocolParams.put("limit", Integer.toString(limit));
	}

	@Override
	public void runQuery() throws Exception {		
//		try {
			SaadaDBConsistency dbConsistency = new SaadaDBConsistency();
			AdqlParser parse = new AdqlParser(new ByteArrayInputStream(TAPToolBox.setBooleanInContain(queryString).getBytes()), null, dbConsistency, new SaadaQueryBuilderTools(dbConsistency));
			if(Messenger.debug_mode) parse.enable_tracing();
			adqlQuery = (SaadaADQLQuery)parse.Query();
			
			System.out.println("@@@@@@@@@@@@@@@@@@ " + limit + " " + adqlQuery.getLimit());			
			(new Exception()).printStackTrace();			

			
//			if (adqlQuery.getLimit() > -1)
//				adqlQuery.setLimit(limit);
			if( adqlQuery.getLimit() <= 0 || limit > DEFAULTSIZE) {
				Messenger.printMsg(Messenger.WARNING, "ADQL result limited to " + DEFAULTSIZE);
				adqlQuery.setLimit(DEFAULTSIZE);
			}
			this.limit = adqlQuery.getLimit();
			resultSet = new ADQLResultSet(adqlQuery.runQuery(), dbConsistency.getColumnsMeta());

			if(resultSet == null) {
				QueryException.throwNewException(SaadaException.DB_ERROR, "No query result !");
			}
//		} catch( adqlParser.parser.ParseException e) {
//			DefaultDBConsistency dbConsistency = new DefaultDBConsistency();
//			AdqlParser parse = new AdqlParser(new ByteArrayInputStream(queryString.getBytes()), null, dbConsistency, QueryBuilderTools());
//			adqlQuery = (SaadaADQLQuery)parse.Query();
//
//			if (limit > -1)
//				adqlQuery.setLimit(limit);
//			if( limit <= 0 || limit > DEFAULTSIZE) {
//				Messenger.printMsg(Messenger.WARNING, "ADQL result limited to " + DEFAULTSIZE);
//				adqlQuery.setLimit(DEFAULTSIZE);
//			}
//			resultSet = new ADQLResultSet(adqlQuery.runQuery(), null);
//
//			if(resultSet == null) {
//				QueryException.throwNewException(SaadaException.DB_ERROR, "No query result !");
//			}
//
//		}

	}

	private QueryBuilderTools QueryBuilderTools() {
		// TODO Auto-generated method stub
		return null;
	}

}
