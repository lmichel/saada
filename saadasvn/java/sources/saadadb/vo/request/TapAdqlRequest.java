package saadadb.vo.request;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.ADQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.Formator;
import saadadb.vo.request.formator.json.TapAdqlJsonFormator;
import saadadb.vo.request.formator.votable.TapAdqlVotableFormator;
import saadadb.vo.request.query.AdqlQuery;
import adqlParser.SaadaADQLQuery;

/**
 * @author laurent
 * * @version $Id$

 */
public class TapAdqlRequest extends VORequest {

	private ADQLResultSet adqlResultSet;
	private SaadaADQLQuery adqlQuery;

	public TapAdqlRequest(String sessionID, String reportDir) {
		super(new AdqlQuery(), "TAP", "1.0", sessionID, reportDir);
	}

	 /* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addVotableFormator()
	 */
	protected void addVotableFormator() throws QueryException {
		 this.formators.put("votable", new TapAdqlVotableFormator());
	 }

	/* (non-Javadoc)
	 * @see saadadb.vo.request.VORequest#addJsonFormator()
	 */
	protected void addJsonFormator() throws QueryException {
		 this.formators.put("json", new TapAdqlJsonFormator());
	 }
	
	@Override
	public void init(Map<String, String> params) throws Exception {
		 this.voQuery.setParameters(params);
		 this.voQuery.buildQuery();
	}

	@Override
	public void runQuery() throws Exception {
		 this.voQuery.runQuery();
		 this.adqlResultSet = ((AdqlQuery)(this.voQuery)).getResultSet();
		 this.adqlQuery = ((AdqlQuery)(this.voQuery)).getAdqlQuery();
	}

	/**
	 * Invoke each formator and put response file names in a map with the format as key
	 * @return
	 * @throws QueryException
	 */
	public Map<String, String> buildResponses() throws Exception {
		if( formators.size() == 0 ) {
			QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "VO request has no Response formator");						
		}
		Map<String, String> retour = new LinkedHashMap<String, String>();
		int cpt = 0;
		for( Entry<String, Formator> esf: formators.entrySet()) {
			/*
			 * Result set are currently in forwardonly, the query canot be rewind
			 */
			if( cpt > 0 ) {
				this.runQuery();
			}
			Formator fmter = esf.getValue();
			fmter.setProtocolParams(voQuery.getProtocolParams());
			fmter.setAdqlResultSet(adqlResultSet);
			fmter.setAdqlQuery(adqlQuery);
			/*
			 * Overflow can not be detected in resultset mode because large SQL queries do not support
			 * SROLLING mode, thus query size can not be detected
			 */
			if( oids != null && oids.size() > fmter.getLimit()) {
				Messenger.printMsg(Messenger.TRACE, "Build Error Response file " + fmter.getResponseFilePath() + " due to too large number of matching images");
				fmter.buildErrorResponse(new QueryException("OVERFLOW", "Number of matching images exceeds limit of " +fmter.getLimit() ));				
			} else {
				Messenger.printMsg(Messenger.TRACE, "Build Response file " + fmter.getResponseFilePath() );
				fmter.buildDataResponse();				
			}
			retour.put(esf.getKey(), fmter.getResponseFilePath());
			cpt++;
		}
		return retour;
	}
}
