package saadadb.vo.request.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;

/**
 * Implementation of VOQuery builds and run the query from parameters
 * given from outside @link VORequest
 * @author laurentmichel
 * @version $Id$

 */
public abstract class VOQuery {
	protected static ArrayList<String> formatAllowedValues ;
	protected static String[]          mandatoryDataParams ;
	protected static String[]          mandatoryMetaParams ;
	protected Map<String, String>      queryParams;
	protected Map<String, String>      protocolParams = new LinkedHashMap<String, String>();
	protected String                   queryString;
	protected boolean                  metaQuery = false;

	/**
	 * Checks that mandatory parameters are present without regards to their values
	 * @param params
	 * @throws QueryException when mandatory parameters are not present
	 */
	public void setParameters(Map<String, String> params) throws QueryException {
		if( params == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No parameter given to the Query");
		}
		for(String mpd: mandatoryDataParams) {
			if( params.get(mpd) == null && params.get(mpd.toUpperCase() ) == null ) {
				QueryException.throwNewException("ERROR", "No parameter " +  mpd.toUpperCase() + " given to the Query");
			}			
		}
		metaQuery = false;
		Messenger.printMsg(Messenger.TRACE, "Data query");

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
	}

	/**
	 * Return a map with all parameter possibly used by the formator.
	 * All parameters values are strings, keys are lower case.
	 * @return
	 */
	public Map<String, String> getProtocolParams() {
		return protocolParams ;
	}

	/**
	 * @return
	 */
	public boolean isMetaQuery(Map<String, String> params) {
		/*
		 * Metadata response must be returned if input parameters match mandatoryMetaParams (usually one parameter);
		 * If one of these param is missing we consider that the requestshould return data
		 */
		for(String mp: mandatoryMetaParams) {
			if( (params.get(mp.toLowerCase()) == null && params.get(mp.toUpperCase()) == null)
			|| (!"metadata".equalsIgnoreCase(params.get(mp.toLowerCase())) && "metadata".equalsIgnoreCase(params.get(mp.toLowerCase())))  ) {
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


	/**
	 * @return
	 * @throws Exception 
	 */
	abstract public ArrayList<Long> getOids() throws Exception;
	
	/**
	 * @return
	 */
	abstract public SaadaInstanceResultSet getSaadaInstanceResultSet();

	/**
	 * Closes the resultSet
	 * @throws QueryException 
	 */
	abstract public void close() throws QueryException ;

	/**
	 * @throws Exception 
	 * 
	 */
	abstract public void buildQuery() throws Exception;

	/**
	 * @throws QueryException 
	 * @throws Exception 
	 * 
	 */
	abstract public void runQuery() throws Exception;


}
