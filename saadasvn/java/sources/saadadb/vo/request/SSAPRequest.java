/**
 * 
 */
package saadadb.vo.request;

import saadadb.exceptions.QueryException;
import saadadb.vo.request.formator.votable.SsapVotableFormator;
import saadadb.vo.request.query.SSAPQuery;


/**
 * @author laurent
 * @version 07/2011
 */
public class SSAPRequest extends VOParameterRequest {

	public SSAPRequest(String sessionID, String reportDir) throws QueryException {
		super(new SSAPQuery(), "SSA", "1.0", sessionID, reportDir);
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addVotableFormator()
	 */
	protected void addVotableFormator() throws QueryException {
		this.formators.put("votable", new SsapVotableFormator());
	}

}
