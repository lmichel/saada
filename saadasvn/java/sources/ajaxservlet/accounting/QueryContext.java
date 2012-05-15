package ajaxservlet.accounting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import saadadb.exceptions.QueryException;
import saadadb.meta.AttributeHandler;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import ajaxservlet.formator.DisplayFilter;

/**
 * @author laurentmichel
  * @version $Id$
*
 */
public class QueryContext implements Serializable {
	private static final long serialVersionUID = 1L;
	private String query;
	private int resultSize;
	transient OidsaadaResultSet resultSet;
	transient private DisplayFilter colfmtor;

	public QueryContext( String query, DisplayFilter colfmtor) throws Exception {
		this.query = query;
		this.colfmtor = colfmtor;
		Pattern p = Pattern.compile(".*Limit\\s+[0-9]+\\s*", Pattern.DOTALL);
		if(! p.matcher(query).matches() ) {
			Messenger.printMsg(Messenger.WARNING, "Query truncated to 10000");
			this.query += " Limit 10000";
		}
		this.executeQuery();
	}

	/**
	 * @throws Exception
	 */
	private void executeQuery () throws Exception {
		Query q = new Query();
		resultSet = q.runBasicQuery(query);
		colfmtor.setResultSet(resultSet);
		this.resultSize = resultSet.size();
		for( AttributeHandler ah: q.getUCDColumns()) {
			colfmtor.addUCDColumn(ah); 
		}
	}
	
	public String getQuery() {
		return query;
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
	public List<String> getRow(int rank) throws Exception {
		colfmtor.setOId(resultSet.getOId(rank));
		return colfmtor.getRow(null, rank);		
	}

	public boolean endReached(int rank) {
		if( rank >= this.resultSize) {
			return true;
		}
		return false;
	}

}
