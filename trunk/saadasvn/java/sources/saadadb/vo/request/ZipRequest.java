package saadadb.vo.request;

import java.util.ArrayList;
import java.util.Map;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.archive.ZipFormator;
import saadadb.vo.request.query.SaadaqlQuery;


/**
 * @author laurent
 * @version 07/2011
 */
public class ZipRequest extends VORequest {

	public ZipRequest(String sessionID, String reportDir) {
		super(new SaadaqlQuery(), "Saada Native", "", sessionID, reportDir);
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
		this.formators.put("zip", new ZipFormator(this.jobId));
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addFormator(java.lang.String)
	 */
	public void addFormator(String format) throws QueryException {
		if( this.formators.size() == 0 ) {
			super.addFormator(format);
		}
		else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, "SaadaQL request support only one formator");
		}
	}

	@Override
	public void runQuery() throws Exception {
		this.voQuery.runQuery();
		SaadaInstanceResultSet sirs = this.voQuery.getSaadaInstanceResultSet();
		/*
		 * Zip request need to navigate into result set. The requires to work with an oid list.
		 * No matter with the size because such request are very limited in size
		 */
		this.oids = new ArrayList<Long>();
		while( sirs.next() ) {
			oids.add(sirs.getOId());
		}
		Messenger.printMsg(Messenger.TRACE, "Query done (" + this.oids.size() + " returned)");
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
