package saadadb.vo.request;

import saadadb.exceptions.QueryException;
import saadadb.vo.request.formator.fits.ConeSearchToFITSFormator;
import saadadb.vo.request.formator.votable.ConeSearchVotableFormator;
import saadadb.vo.request.query.ConeSearchQuery;

/**
 * Translate CS parameters in a SAADAQL query and run it
 * and build the response files
 * @author laurent
 * @version $Id: ConeSearchRequest.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 *
 */
public class ConeSearchRequest extends SaadaqlRequest {


	public ConeSearchRequest(String sessionID, String reportDir) throws QueryException {
		super(new ConeSearchQuery(), "CS", "1.0", sessionID, reportDir);
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addVotableFormator()
	 */
	protected void addVotableFormator() throws QueryException {
		this.formators.put("votable", new ConeSearchVotableFormator());
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addFitsFormator()
	 */
	protected void addFitsFormator() throws QueryException {
		this.formators.put("fits", new ConeSearchToFITSFormator());
	}
	
}
