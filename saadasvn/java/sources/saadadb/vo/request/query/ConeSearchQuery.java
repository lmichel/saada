package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.query.executor.Query;
import saadadb.query.parser.PositionParser;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.vo.PseudoTableParser;

/**
 * Translate CS parameters in a SAADAQL query and run it
 * @author laurent
 * 
 * @version 06/2011
 *
 */
public class ConeSearchQuery extends VOQuery {
	private static final int DEFAULTSIZE = 100000;
	private SaadaInstanceResultSet resultSet;
	private String queryString;

	public ConeSearchQuery() {
		mandatoryDataParams = new String[]{"RA", "SR"};
		mandatoryMetaParams = new String[]{"SR"};
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getOids()
	 */
	@Override
	public ArrayList<Long> getOids() throws Exception {
		ArrayList<Long> retour = new ArrayList<Long>();
		while( this.resultSet.next()) {
			retour.add(this.resultSet.getOId());
		}
		return retour;
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#isMetaQuery(java.util.Map)
	 */
	public boolean isMetaQuery(Map<String, String> params) {
		for(String mp: mandatoryMetaParams) {
			/*
			 * CS: getmeta if SR=0
			 */
			if( (params.get(mp) == null || !"0".equals(params.get(mp)) ) &&
					(params.get(mp.toUpperCase()) == null || !"0".equals(params.get(mp.toUpperCase()))) )	 {
				return false;
			}
		}
		this.queryParams = new HashMap<String, String>();
		/*
		 * Force key to lower case to be sure to retrieve them
		 */
		for( String k: params.keySet()) {
			this.queryParams.put(k.toLowerCase(), params.get(k));
			/*
			 * All params are copied in protocol params (for the formator) Those refomated
			 * will be override in buildQuery()
			 */
			this.protocolParams.put(k.toLowerCase(), params.get(k));
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#getSaadaInstanceResultSet()
	 */
	@Override
	public SaadaInstanceResultSet getSaadaInstanceResultSet() {
		return this.resultSet;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#close()
	 */
	@Override
	public void close() throws QueryException {
		this.resultSet.close();
		this.resultSet = null;
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#buildQuery()
	 */
	@Override
	public void buildQuery() throws Exception {
		double posRa = 1000.0, posDec = 1000.0;
		double size = 1.0;
		String value;
		int limit ;

		/*
		 * Mandatory params
		 */
		PseudoTableParser ptp = new PseudoTableParser(queryParams.get("collection"));
		queryString = "Select ENTRY From " + Merger.getMergedArray(ptp.getclasses()) 
		+ " In "  + Merger.getMergedArray(ptp.getCollections()) ;
		protocolParams.put("category", "IMAGE");
		protocolParams.put("class", Merger.getMergedArray(ptp.getclasses()));
		protocolParams.put("collection", Merger.getMergedArray(ptp.getCollections()));

		if (protocolParams.size() == 0) {
			QueryException.throwNewException("ERROR","No parameters given");
		}
		// position parameter
		String pos="";
		String pos1 = this.queryParams.get("ra");
		String pos2 = this.queryParams.get("dec");
		if( pos1 == null && pos2 == null ) {
			QueryException.throwNewException("ERROR","No position parameters given");			
		}
		if( pos1 != null ) pos += pos1;
		if( pos2 != null ) pos += " " + pos2;
		try {
			PositionParser pp = new PositionParser(pos);
			posRa  = pp.getRa(); 
			posDec = pp.getDec();
			protocolParams.put("ra", Double.toString(posRa));
			protocolParams.put("dec", Double.toString(posDec));
		} catch (Exception e) {
			e.printStackTrace();
			QueryException.throwNewException("ERROR",   "Unrecognized format for position parameter :" + pos);
		}
		/*
		 * Cone size parameter
		 */
		value = queryParams.get("sr");
		try {
			size =  Double.parseDouble(value);
		} catch (Exception e) {
			QueryException.throwNewException("ERROR",   "Unrecognized format for SIZE parameter :" + value);
		}
		if ( size < 0.0) {
			QueryException.throwNewException("ERROR","SIZE must be greater than 0.0 ");
		} else	if( size > 10 ){
			QueryException.throwNewException("ERROR","SIZE is limited to 10 deg");
		}
		protocolParams.put("size", value);
		size *= 60;
		queryString  += "\nWherePosition{isInCircle(\"" + pos + "\"," + size + ",J2000,FK5)}";
		/*
		 * Query limit parameter
		 */
		value = queryParams.get("limit");
		limit = DEFAULTSIZE;
		if( value != null ) {
			try {
				limit =  Integer.parseInt(value);
			} catch (Exception e) {
				QueryException.throwNewException("ERROR",   "Unrecognized format for LIMIT parameter :" + value);
			}
		}
		if( limit <= 0 ) {
			QueryException.throwNewException("ERROR",   "LIMIT parameter must be greater than 0");
		}

		if( limit > DEFAULTSIZE) {
			Messenger.printMsg(Messenger.WARNING, "Query limit limited to " + DEFAULTSIZE);;
			limit = DEFAULTSIZE;
		}
		queryString  += "\nLimit " + limit;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.query.VOQuery#runQuery()
	 */
	@Override
	public void runQuery() throws Exception {	
		Query q = new Query (queryString);
		q.parse();
		String dataclass = Category.explain(q.getSfiClause().getCatego()) + "UserColl";
		if( q.getSfiClause().getListClass().length == 1 && !"*".equals(q.getSfiClause().getListClass()[0])) {
			dataclass = q.getSfiClause().getListClass()[0];
		}
		SaadaInstance si = (SaadaInstance) Class.forName(
				"generated." + Database.getDbname() + "." + dataclass)
				.newInstance();
		resultSet = q.runAllColumnsQuery(si, queryString);
	}

}
