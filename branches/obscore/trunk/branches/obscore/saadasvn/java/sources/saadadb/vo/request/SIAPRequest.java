package saadadb.vo.request;

import saadadb.exceptions.QueryException;
import saadadb.vo.request.formator.votable.SiapVotableFormator;
import saadadb.vo.request.query.SIAPQuery;

/**
 * Translate SIAP parameters in a SAADAQL query and run it
 * and build the response files
 * @author laurent
 *  * @version $Id$

 * 
 *
 */
public class SIAPRequest extends VOParameterRequest {
   
	public SIAPRequest(String sessionID, String reportDir) throws QueryException {
		super(new SIAPQuery(), "SIAP", "1.0", sessionID, reportDir);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addVotableFormator()
	 */
	protected void addVotableFormator() throws QueryException {
		this.formators.put("votable", new SiapVotableFormator());
	}
}
