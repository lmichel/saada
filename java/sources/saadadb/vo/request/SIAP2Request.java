package saadadb.vo.request;

import saadadb.exceptions.QueryException;
import saadadb.vo.request.formator.votable.Siap2VotableFormator;
import saadadb.vo.request.query.SIAP2Query;

/**
 * Translate SIAP parameters in a SAADAQL query and run it
 * and build the response files
 * @author laurent
 *  * @version $Id: SIAPRequest.java 1062 2014-03-12 15:57:54Z laurent.mistahl $

 * 
 *
 */
public class SIAP2Request extends VOParameterRequest {
   
	public SIAP2Request(String sessionID, String reportDir) throws QueryException {
		super(new SIAP2Query(), "SIAP", "2.0", sessionID, reportDir);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addVotableFormator()
	 */
	protected void addVotableFormator() throws QueryException {
		this.formators.put("votable", new Siap2VotableFormator());
	}
}
