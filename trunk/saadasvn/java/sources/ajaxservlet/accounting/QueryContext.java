package ajaxservlet.accounting;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.util.Messenger;
import ajaxservlet.formator.DisplayFilter;

/**
 * @author laurentmichel
  * @version $Id$
*
 */
public class QueryContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String queryString;
	private Query query;
	private int resultSize;
	transient OidsaadaResultSet resultSet;
	transient private DisplayFilter colfmtor;

	public QueryContext( String queryString, DisplayFilter colfmtor, String sessionId) throws Exception {
		this.queryString = queryString;
		this.colfmtor = colfmtor;
		Pattern p = Pattern.compile(".*Limit\\s+[0-9]+\\s*", Pattern.DOTALL);
		if(! p.matcher(this.queryString).matches() ) {
			Messenger.printMsg(Messenger.WARNING, "Query truncated to 10000");
			this.queryString += " Limit 10000";
		}
		long session;
		try {
			session = Long.parseLong(sessionId);
		} catch(NumberFormatException e) {
			session = sessionId.hashCode();
		}
		colfmtor.setSessionId(session);
		this.executeQuery(colfmtor);
	}

	/**
	 * @throws Exception
	 */
	private void executeQuery (DisplayFilter colfmtor) throws Exception {
		query = new Query();
		resultSet = query.runBasicQuery(queryString);
		colfmtor.setResultSet(resultSet);
		this.resultSize = resultSet.size();
		for( AttributeHandler ah: query.getUCDColumns()) {
			colfmtor.addUCDColumn(ah); 
		}
		colfmtor.addConstrainedColumns(query.buildListAttrHandPrinc());
	}
	
	public String getQuery() {
		return queryString;
	}
	public int getResultSize() throws QueryException {
		return resultSize;
	}
	public List<Long> getPage(int start, int length) throws QueryException {
		return resultSet.getPage(start, length);
	}	
	public Set<String> getColumns() {
		return colfmtor.getDisplayedColumns();
	}
	public Set<String> getConstrainedColumns() {
		return colfmtor.getConstrainedColumns();
	}
	public List<String> getRow(int rank) throws Exception {
		colfmtor.setOId(resultSet.getOId(rank));
		return colfmtor.getRow(null, rank);		
	}
	public long getOid(int rank) throws Exception {
		return resultSet.getOId(rank);		
	}

	public boolean endReached(int rank) {
		if( rank >= this.resultSize) {
			return true;
		}
		return false;
	}
}
