/**
 * 
 */
package saadadb.vo.request;

import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.archive.CartFormator;
import saadadb.vo.request.query.CartQuery;

/**
 * @author laurent
 * @version $Id$
 */
public class CartRequest extends VORequest{
	
	public CartRequest(String sessionID, String reportDir) {
		super(new CartQuery(), "Saada Native", "", sessionID, reportDir);
	}
	
	@Override
	public void init(Map<String, String> params) throws Exception {
		this.voQuery.setParameters(params);
		this.voQuery.buildQuery();
	}

	/**
	 * @throws QueryException
	 */
	protected void addZipFormator() throws QueryException {
		this.formators.put("cart", new CartFormator(this.jobId));
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addFormator(java.lang.String)
	 */
	public void addFormator(String format) throws QueryException {
		if( this.formators.size() == 0 ) {
			super.addFormator(format);
		}
		else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, "CART request support only one formator");
		}
	}

	@Override
	public void runQuery() throws Exception {

		/*
		 * This request run multiple and various queries which run by the formator because queries are
		 * considered as product URIs
		 */
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#processRequest(java.util.Map)
	 */
	public Map<String, String> processRequest(Map<String, String> pmap) {
		Map<String, String> retour;
		/*
		 * Ignore mete  data query
		 */
		try {
			this.init(pmap);
			this.runQuery();
			retour =  this.buildResponses();
			this.voQuery.close();
		} catch(Exception e) {
			retour =  this.buildErrorResponses(e);			
			Messenger.printStackTrace(e);
			try {
				this.voQuery.close();
			} catch (Exception e1) {
				Messenger.printStackTrace(e1);
			}
		}
		return retour;

	}

}
