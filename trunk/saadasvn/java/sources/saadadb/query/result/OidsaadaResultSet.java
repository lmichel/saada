package saadadb.query.result;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.KeyIndex;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;

/** * @version $Id$

 * This class run an SQL query and merge it with a pattern result set.
 * The SQL query is supposed to have an oidsaada in the first column. There is no check.
 * Both query and merge are done at the first access to the result.
 * Query result is truncated to the @see limit parameter
 * 
 * @author laurentmichel
 * 03/2011
 *
 */
public class OidsaadaResultSet extends SaadaInstanceResultSet{

	private ArrayList<Long> oids = new ArrayList<Long>();
	private LinkedHashMap<String, ArrayList<Object>> resultmap = new LinkedHashMap<String, ArrayList<Object>>() ;
	private boolean withComputedColumns = false;
	private static final long TIMEOUT = 30000;

	/**
	 * Nothing else than initialize fields
	 * @param sqlQuery
	 * @param patternKeySet
	 * @param limit
	 * @throws QueryException 
	 */
	public OidsaadaResultSet(String sqlQuery, KeyIndex patternKeySet, int limit, boolean with_computed_column) throws QueryException {
		super(null, sqlQuery, patternKeySet, limit, SaadaConstant.INT);
		withComputedColumns = with_computed_column ;
	}

	/**
	 * @return
	 */
	public int getLimit() {
		return limit;
	}

	private void initResultmap(ResultSet rs) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();   		
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			if( !"oidsaada".equals(rsmd.getColumnName(i))) {
				resultmap.put(rsmd.getColumnName(i),  new ArrayList<Object>() );
			}
		}		
	}
	/**
	 * run the sqlquery, match its resultset with the pattern result set and store the result within an array list.
	 * If SQLquery has no WHERE statement and the pattern result set is not null, its result is taken directly
	 * @throws Exception throws any error to the calling method
	 */
	private void init() throws Exception {
		Set<String> keys = resultmap.keySet();
		if( patternKeySet != null && whereDetector.matcher(this.sqlQuery.getQuery()).find() ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Execute SQL query: " + this.sqlQuery.getQuery());
			ResultSet rs = sqlQuery.run();
			initResultmap(rs);
			/*
			 * Cross match oids of both result set
			 */
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Match result with pattern result set (limit: " + limit + ") "
					+ ((patternKeySet == null)?"no matchPattern": (patternKeySet.selectedKeysSize() + " oids selected by matchPatterns")));
			int cpt = 1;
			int cpt2 = 1;
			Database.gc();

			Set<Long> hs = patternKeySet.getSELECTEDKeySet();
			patternKeySet = null;
			long start = System.currentTimeMillis();

			while( rs.next() ) {
				long oid = rs.getLong(1);
				//if( patternKeySet.hasDichotoKey(oid, true) != -1 ) {
				if( hs.contains(oid)  ) {
					oids.add(oid);
					hs.remove(oid);
					if( withComputedColumns ) {
						for( String k: keys) {
							resultmap.get(k).add(rs.getObject(k));
						}
					}
					if( cpt >= limit ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Result truncated to " + limit); 
						break;
					}
					cpt++;
				}
				/*
				 * SQLite (at least) becomes ery slow with queries returning large result set. 
				 * It is better to set a TO and to return a truncated result than to reach the browser TO
				 * The pb s that the user is not advertised of the situation
				 */
				long delta;
				if( cpt2> 1000 && (cpt2 % 1000) == 0 && (delta = (System.currentTimeMillis() - start)) > TIMEOUT) {
					Messenger.printMsg(Messenger.WARNING, "Query stopped on time out (" + (delta/1000) + "\") at " + cpt + " match (" + cpt2 + " scan)");
					break;				
				}	

//				if(  (cpt2 % 10000) == 0 ) {
//					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, cpt + " match /" + cpt2 
//							+ " checked " + hs.size());
//				}				
				cpt2++;
			}
			sqlQuery.close();
		}
		else if( patternKeySet != null ){
			int cpt = 1;
			Set<Long> sks=  patternKeySet.getSELECTEDKeySet();
			for( Long oid: sks) {
				oids.add(oid);
				if( cpt >= limit ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Result truncated to " + limit); 
					break;
				}
				cpt++;
			}
		}
		else {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Execute SQL query: " + this.sqlQuery.getQuery());
			ResultSet rs = sqlQuery.run();
			initResultmap(rs);
			int cpt = 1;
			long start = System.currentTimeMillis();
			while( rs.next() ) {
				long oid = rs.getLong(1);
				if( withComputedColumns ) {
					for( String k: keys) {
						resultmap.get(k).add(rs.getObject(k));
					}
				}
				oids.add(oid);
				/*
				 * SQLite (at least) becomes ery slow with queries returning large result set. 
				 * It is better to set a TO and to return a truncated result than to reach the browser TO
				 * The pb s that the user is not advertised of the situation
				 */
				long delta;
				if( cpt> 1000 && (cpt % 1000) == 0 && (delta = (System.currentTimeMillis() - start)) > TIMEOUT) {
					Messenger.printMsg(Messenger.WARNING, "Query stopped on time out (" + (delta/1000) + "\") at " + cpt + " match");
					break;				
				}				
				if( cpt >= limit ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Result truncated to " + limit); 
					break;
				}
				cpt++;
			}
			//sqlQuery.close();
		}

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, oids.size() + "oids selected");
		/*
		 * Don't need it anymore, save memory
		 */
		patternKeySet = null;
		initDone = true;
		currentPtr = -1;;
	}


	/* (non-Javadoc)
	 * @see saadadb.query.result.SaadaInstanceResultSet#next()
	 */
	public boolean next() throws QueryException {
		try {
			if( !initDone ) {
				this.init();
			}
			currentPtr++;
			if( currentPtr >= oids.size() ) {
				return false;
			}
			else {
				return true;
			}

		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return false;
	}


	/* (non-Javadoc)
	 * @see saadadb.query.result.SaadaInstanceResultSet#getOId()
	 */
	public long getOId() throws QueryException {
		try {
			return oids.get(currentPtr);
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		return SaadaConstant.LONG;
	}

	public Object getObject(String key) throws Exception {
		if( "oidsaada".equals(key)) {
			return getOId();
		}
		else {
			return this.resultmap.get(key).get(currentPtr);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.query.result.SaadaInstanceResultSet#getOId(int)
	 */
	public long getOId(int rank) throws QueryException  {
		try {
			if( !initDone ) {
				this.init();
			}
			return oids.get(rank);
		} catch (Exception e) {
			return SaadaConstant.LONG;
		}
	}

	/**
	 * @param rank
	 * @param key
	 * @return
	 * @throws QueryException
	 */
	public Object getObject(int rank, String key) throws QueryException {
		if( "oidsaada".equals(key)) {
			return getOId(rank);
		}
		else {
			return this.resultmap.get(key).get(rank);
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.query.result.SaadaInstanceResultSet#getPage(int, int)
	 */
	public List<Long> getPage(int start, int length) throws QueryException {
		int end = start + length;
		int s = oids.size();
		if( end > s) {
			end = s;
		}
		return oids.subList(start, end);
	}


	/* (non-Javadoc)
	 * @see saadadb.query.result.SaadaInstanceResultSet#size()
	 */
	public int size( ) throws QueryException{
		if( !initDone ) {
			try {
				this.init();
			} catch (Exception e) {
				Messenger.printStackTrace(e);
				QueryException.throwNewException(SaadaException.DB_ERROR, e);
			}
		}
		return oids.size();
	}
}
