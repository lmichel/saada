/**
 * 
 */
package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.regex.Pattern;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.query.executor.Query;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.util.Messenger;

/**
 * Run a SaadaQL in the VO request context which is used iether by VO services by the download service
 * @author laurent
 * @version 07.2011
 */
public class SaadaqlQuery extends VOQuery {
	public static final Pattern limitPattern = Pattern.compile(".*Limit\\s+[0-9]+.*", Pattern.DOTALL);	
	private static final int DEFAULTSIZE = 100000;
	private SaadaInstanceResultSet resultSet = null;
	private Query saadaqlQuery;
	private int limit;
	private String queryString;
	private SaadaInstance instance;
	public SaadaqlQuery() {
		mandatoryDataParams = new String[]{"query"};
		mandatoryMetaParams = new String[]{"format"};
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getOids()
	 */
	@Override
	public ArrayList<Long> getOids() throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getSaadaInstanceResultSet()
	 */
	@Override
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#close()
	 */
	@Override
	public void close() throws QueryException {
		if( this.resultSet != null ) {
			this.resultSet.close();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#buildQuery()
	 */
	@Override
	public void buildQuery() throws Exception {
		queryString = queryParams.get("query");
		if( queryString == null || queryString.length() == 0 ) {
			QueryException.throwNewException("ERROR", "No SaadaQL query string given (param \"query\")");
		}
		String l = queryParams.get("limit");
		if( l == null ||l.length() == 0 ) {
			Messenger.printMsg(Messenger.WARNING, "SAADAQL result limited to " + DEFAULTSIZE);
			limit = DEFAULTSIZE;
		}
		else {
			try {
				limit = Integer.parseInt(l);
			} catch (Exception e) {
				QueryException.throwNewException("ERROR", "Limit parameter not valid " + l);
			}
		}
		if( limit <= 0 || limit > DEFAULTSIZE) {
			Messenger.printMsg(Messenger.WARNING, "SAADAQL result limited to " + DEFAULTSIZE);
			limit = DEFAULTSIZE;
		}
		
		if( limitPattern.matcher(queryString).matches() ) {
			queryString.replaceAll(".*Limit\\s+[0-9]+.*", ("Limit " + limit));
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Set limit to " + limit + " in SaadaQL string");
		}
		else {
			System.out.println(queryString);
			queryString += ("\nLimit " + limit);
			Messenger.printMsg(Messenger.DEBUG, "Append limit " + limit + " in SaadaQL string");
			System.out.println(queryString);
		}
		protocolParams.put("query", queryString);
		protocolParams.put("limit", Integer.toString(limit));
		saadaqlQuery = new Query(queryString);
		saadaqlQuery.parse();
		protocolParams.put("collection", Merger.getMergedArray(saadaqlQuery.getSfiClause().getListColl()));
		protocolParams.put("class"     , Merger.getMergedArray(saadaqlQuery.getSfiClause().getListClass()));
		protocolParams.put("category"  , Category.explain(saadaqlQuery.getSfiClause().getCatego()));
		
		String[] classes = saadaqlQuery.getSfiClause().getListClass();
		if( classes.length == 1 && !"*".equals(classes[0]) ) {
			instance = (SaadaInstance)  SaadaClassReloader.forGeneratedName(classes[0]).newInstance();
		}
		else {
			instance = (SaadaInstance) Class.forName("generated." + Database.getName() + "." + Category.explain(saadaqlQuery.getSfiClause().getCatego()) + "UserColl").newInstance();			
		}

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#runQuery()
	 */
	@Override
	public void runQuery() throws Exception {

		resultSet = saadaqlQuery.runAllColumnsQuery(instance, queryString);

		if(resultSet == null) {
			QueryException.throwNewException(SaadaException.DB_ERROR, "No query result !");
		}

	}

}
