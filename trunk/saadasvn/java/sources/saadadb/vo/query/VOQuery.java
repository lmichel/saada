package saadadb.vo.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.vo.formator.VOResultFormator;

/**
 * Implementation of VOQueries build and run the query from parameters
 * given from outside @link VORequest
 * @author laurentmichel
 *@version $Id$
 */
public abstract class VOQuery {
	protected static ArrayList<String> formatAllowedValues ;
	protected static String[]          mandatoryParams ;
	protected Map<String, String>      params;
	protected String                   queryString;

	/**
	 * Checks that mandatory parameters are present without regards to their values
	 * @param params
	 * @throws QueryException when ma,datory parameters are not present
	 */
	public void setParameters(Map<String, String> params) throws QueryException {
		if( params == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No parameter given to the Query");
		}
		for(String mp: mandatoryParams) {
			if( params.get(mp) == null && params.get(mp.toUpperCase() ) == null ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No parameter " +  mp.toUpperCase() + " given to the Query");
			}			
		}
		this.params = new HashMap<String, String>();
		/*
		 * Force key to lower case to be sure to retreive them
		 */
		for( String k: params.keySet()) {
			params.put(k.toLowerCase(), params.get(k));
		}
	}

	/**
	 * @param dm
	 */
	abstract public void setDM(VOResource dm);

	/**
	 * @throws Exception 
	 * 
	 */
	abstract public void buildQuery() throws Exception;

	/**
	 * @throws QueryException 
	 * 
	 */
	abstract public void runQuery() throws QueryException;

	/**
	 * @param filename
	 */
	abstract public void initReport(String filename);

	/**
	 * @param comments
	 */
	abstract public void writeComments(Map<String, String> comments);

	/**
	 * 
	 */
	abstract public void writeSTC();

	/**
	 * 
	 */
	abstract public void writeInputs();

	/**
	 * 
	 */
	abstract public void writeFields();

	/**
	 * 
	 */
	abstract public void writeData();

	/**
	 * 
	 */
	abstract public void finish();
}
