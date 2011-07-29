/**
 * 
 */
package saadadb.vo.request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.Formator;
import saadadb.vo.request.query.VOQuery;

/** * @version $Id$

 * Request and subclasses handle totality  of the process, from the params to the
 * report delivery
 * At servlet level, we sould have something like
 * <code>
 * voRequest = new VORequest();
 * voRequest.addFormator("votable");
 * voRequest.setResponseFilePath("directory", "prefix");
 * voRequest.processRequest(parameters);
 * </code>
 * @author laurentmichel
 * @version 07/1022
 */
public abstract class VORequest {
	final protected VOQuery voQuery;
	protected Map<String, Formator> formators = new LinkedHashMap<String, Formator>();
	protected ArrayList<Long>   	oids;
	protected SaadaInstanceResultSet   	saadaInstanceResultSet;

	final protected String protocolName;
	final protected String protocolVersion;
	final protected String jobId;
	final protected String reportDir;
	/*
	 * This map stores all parameters the formator needs
	 */
	protected LinkedHashMap<String, String> fmtParams = new LinkedHashMap<String, String>();

	/**
	 * @param voQuery
	 * @param protocolName
	 * @param protocolVersion
	 * @param sessionID
	 * @param reportDir
	 */
	public VORequest(VOQuery voQuery, String protocolName, String protocolVersion,
			String jobId, String reportDir) {
		super();
		this.voQuery = voQuery;
		this.protocolName = protocolName;
		this.protocolVersion = protocolVersion;
		this.jobId = jobId;
		this.reportDir = reportDir;
	}

	/**
	 * Add a formator for the requested format to the request. By default no formator is supported.
	 * Supported formator are declared by overloaded add*Formator methods in subclasses
	 * @param format : can be fits, fir, xml, vot. votable text, txt, cs or json
	 * @throws QueryException thrown if the format is not recognized or if it is not supported
	 */
	public void addFormator(String format) throws QueryException {
		if( "fits".equalsIgnoreCase(format) || "fit".equalsIgnoreCase(format) ) {
			this.addFitsFormator();
		}
		else if( "xml".equalsIgnoreCase(format) || "vot".equalsIgnoreCase(format) || "votable".equalsIgnoreCase(format)) {
			this.addVotableFormator();
		}
		else if( "json".equalsIgnoreCase(format) ) {
			this.addJsonFormator();
		}
		else if( "text".equalsIgnoreCase(format) || "txt".equalsIgnoreCase(format) || "csv".equalsIgnoreCase(format)) {
			this.addTextFormator();
		}
		else if( "zip".equalsIgnoreCase(format) || "zipball".equalsIgnoreCase(format)) {
			this.addZipFormator();
		}
		else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Unrecognized format: " + format );		
		}
	}

	/**
	 * @throws QueryException
	 */
	protected  void addTextFormator() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, this.getClass() + " does not support TEXT reponse");
	}
	/**
	 * @throws QueryException
	 */
	protected void addJsonFormator() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, this.getClass() + " does not support JSON reponse");
	}
	/**
	 * @throws QueryException
	 */
	protected void addVotableFormator() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, this.getClass() + " does not support VOTable reponse");
	}
	/**
	 * @throws QueryException
	 */
	protected void addFitsFormator() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, this.getClass() + " does not support FITS reponse");
	}
	/**
	 * @throws QueryException
	 */
	protected void addZipFormator() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, this.getClass() + " does not support Zipped reponse");
	}

	/**
	 * Set the directory and the prefix of names of the response files.
	 * The suffix is added the the formator
	 * @param responseDir
	 * @param prefix
	 * @throws Exception
	 */
	public void setResponseFilePath(String prefix) throws Exception {
		if( formators.size() == 0 ) {
			QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "VO request has no response formator");			
		}
		else {
			for(Formator formator: formators.values()) {
				formator.setResponseFilePath(this.reportDir, prefix);
			}
		}
	}

	/**
	 * Tell to formators to include relationships in responses. 
	 * There is no validation at this level
	 * @param relationName
	 * @throws QueryException: thrown by some formator or whether there is no formator
	 */
	public void includeRelationInResponse(String relationName) throws QueryException {
		if( formators.size() == 0 ) {
			QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "VO request has no Response formator");			
		}
		else {
			for(Formator formator: formators.values()) {
				formator.includeRelationInResponse(relationName);
			}
		}
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
		for( Entry<String, Formator> esf: formators.entrySet()) {
			Formator fmter = esf.getValue();
			fmter.setProtocolParams(voQuery.getProtocolParams());
			fmter.setResultSet(oids);
			/*
			 * Overflow can not be detected in resultset mode because large SQL queries do not support
			 * SROLLING mode, thus query size can not be detected
			 */
			if( oids != null && oids.size() > fmter.getLimit()) {
				Messenger.printMsg(Messenger.WARNING, "Result (" +  oids.size() + "elements) trucanted to formator limit (" + fmter.getLimit() + " )");
			} 
			Messenger.printMsg(Messenger.TRACE, "Build Response file " + fmter.getResponseFilePath() );
			fmter.buildDataResponse();

			retour.put(esf.getKey(), fmter.getResponseFilePath());
		}
		return retour;
	}

	/**
	 * Invoke meta data response for each formator and put response file names in a map with the format as key
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> buildMetaResponses() throws Exception {
		if( formators.size() == 0 ) {
			QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "VO request has no Response formator");						
		}
		Map<String, String> retour = new LinkedHashMap<String, String>();
		for( Entry<String, Formator> esf: formators.entrySet()) {
			Formator fmter = esf.getValue();
			fmter.setProtocolMetaParams(voQuery.getProtocolParams());
			fmter.setResultSet(new ArrayList<Long>());
			Messenger.printMsg(Messenger.TRACE, "Build Response file " + fmter.getResponseFilePath() );
			fmter.buildMetaResponse();
			retour.put(esf.getKey(), fmter.getResponseFilePath());
		}
		return retour;
	}

	/**
	 * Build Error Response file
	 * @param e Exception originaly of then error
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> buildErrorResponses(Exception e)  {
		Map<String, String> retour = new LinkedHashMap<String, String>();
		for( Entry<String, Formator> esf: formators.entrySet()) {
			Formator fmter = esf.getValue();
			Messenger.printMsg(Messenger.TRACE, "Build Error Response file " + fmter.getResponseFilePath() + " due to " + e);
			try {
				fmter.setProtocolParams(voQuery.getProtocolParams());
				fmter.buildErrorResponse(e);
			} catch(Exception e2) {
				try {
					fmter.buildErrorResponse(e2);
				} catch (Exception e1) {
					Messenger.printStackTrace(e2);
				}
			}
			retour.put(esf.getKey(), fmter.getResponseFilePath());
		}
		return retour;
	}

	/**
	 * Init the request with the parameters map
	 * @param params Parameter map
	 * @throws Exception any error in any subclass
	 */
	public abstract void init(Map<String, String> params) throws Exception;

	/**
	 * Invoke the VOQuery to run the query
	 * @throws QueryException
	 * @throws Exception 
	 */
	public abstract void runQuery() throws Exception;

	/**
	 * Wrap the request processing
	 * Generate an error response in case of query failure
	 * @param pmap
	 * @return 
	 */
	public Map<String, String> processRequest(Map<String, String> pmap) {
		Map<String, String> retour;
		try {
			if( this.voQuery.isMetaQuery(pmap) ) {
				return this.buildMetaResponses();				
			}
			else {
				this.init(pmap);
				this.runQuery();
				retour =  this.buildResponses();
				this.voQuery.close();
			}
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
