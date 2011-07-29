package saadadb.vo.request.formator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import adqlParser.SaadaADQLQuery;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.result.ADQLResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;



/** * @version $Id$

 * @author michel
 * @version 07/2011
 */
public abstract class QueryResultFormator implements Formator{
	protected  Set<String>        relationsToInclude = new LinkedHashSet<String>();
	protected boolean hasExtensions = false; // short cut to avoid to chacke at any time the size of relationsToInclude
	protected Map<String, String> protocolParams;
	protected String 			  protocolName;	
	protected String              defaultSuffix;
	protected ArrayList<Long>     oids;
	protected int                 resultSize;

	protected SaadaInstanceResultSet    saadaInstanceResultSet;
	protected String              responseFilePath = ".";
	protected VOResource		  dataModel;
	protected int 				  limit = -1;	

	protected LinkedHashMap<String, String> infoMap = new LinkedHashMap<String, String>();

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#includeRelationInResponse(java.lang.String)
	 */
	public void includeRelationInResponse(String relationName) throws QueryException {
		if( this.supportResponseInRelation() ) {

		}
		else {
			QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, this.getClass() + " can not include linked data in its response");
		}
	}	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#supportResponseInRelation()
	 */
	public boolean supportResponseInRelation() {
		return false;
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setResultSet(saadadb.query.result.OidsaadaResultSet)
	 */
	public void setResultSet(ArrayList<Long> oids) throws QueryException {
		this.oids = oids;
		this.resultSize = oids.size();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResultSet) throws QueryException{			
		QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE, this.getClass() + " can not use SaadaInstanceResultSet to build a response");
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		String rel, coll, cat;
		if( (coll = fmtParams.get("collection")) == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No collection  in formator parameters");
		}
		if( (cat = fmtParams.get("category")) == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No category in formator parameters");
		}
		/*
		 * Check relations to be include. They must exist in the collection/category
		 */
		if( (rel = fmtParams.get("relations")) != null ) {
			if( ! this.supportResponseInRelation() ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, this.getClass() + " Does not support relations in relations");				
			}
			// Extract relation list
			String[] rls = rel.split("(,|;|:| )");
			boolean all = false;
			// Look for the any flag
			for( String r: rls) {
				if( r.toLowerCase().startsWith("any-") ) {
					all = true;
					break;
				}
			}
			// Get all candidate relations
			String[] drls = Database.getCachemeta().getRelationNamesStartingFromColl(coll, Category.getCategory(cat));
			// Take all if flag "any" is set
			if( all ) {
				for( String dr: drls ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Add relation \"" + dr + "\" in response");
					this.relationsToInclude.add(dr);							
					this.hasExtensions = true;
				}
			}
			// Match existing relations with requested relations
			else {
				for( String r: rls ) {
					boolean found = false;
					for( String dr: drls ) {
						if( dr.equals(r)) {
							found = true;
							break;
						}
					}
					if( !found ) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Relation " + r + " does not existe in " + coll + "_" + cat);					
					} else {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Add relation " + r + " in response");
						this.relationsToInclude.add(r);
						this.hasExtensions = true;
					}
				}

			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setProtocolMetaParams(java.util.Map)
	 */
	public void setProtocolMetaParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setResponseDir(java.lang.String)
	 */
	public void setResponseFilePath(String responseFilePath) throws Exception {
		ResponseFilePath rp = (new ResponseFilePath(responseFilePath));
		this.responseFilePath = rp.getResponseFilePath();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setResponseFilePath(java.lang.String, java.lang.String)
	 */
	public void setResponseFilePath(String responseDir, String prefix) throws Exception {
		ResponseFilePath rp = (new ResponseFilePath(responseDir, buildResponseNameFromPrefix(prefix)));
		this.responseFilePath = rp.getResponseFilePath();
	}


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#buildResponse()
	 */
	abstract public void buildDataResponse( ) throws Exception;


	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#getResponseFilePath()
	 */
	public String getResponseFilePath() {
		return responseFilePath;
	}

	public int getLimit() {
		return limit;
	}
	/**
	 * @param prefix
	 * @return
	 */
	protected String buildResponseNameFromPrefix(String prefix){
		return prefix + this.defaultSuffix;

	}

	/**
	 * return a query parameter value. If the value is not set, an exception is rise
	 * because that means the query is not compliant with the protocol
	 * @param param
	 * @return
	 * @throws QueryException
	 */
	protected String getProtocolParam(String param) throws QueryException {
		String retour = this.protocolParams.get(param);
		if( retour == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Query paramterer <" + param + "> not set" );
		}
		return retour;
	}
	/**
	 * Load the request data model.
	 * @param dataModelName
	 * @throws QueryException if the DM is not found
	 */
	protected void setDataModel(String dataModelName) throws QueryException{
		try {
			VOResource vor = Database.getCachemeta().getVOResource(dataModelName);
			if( vor == null ) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Unkown resource "  + dataModelName);
			}
			else {
				this.dataModel = vor;
			}
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}
	}

	/** @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */




	/**
	 * Write the column description
	 * @throws Exception 
	 */
	protected void writeMetaData() throws Exception {
		this.writeDMFieldsAndGroups();			
		this.writeHousekeepingFieldAndGroup();
		this.writeExtMetaReferences();
	}

	/**
	 * @param oid
	 * @throws SaadaException
	 */
	protected abstract void writeHouskeepingData(SaadaInstance obj) throws SaadaException ;
	/**
	 * @param obj
	 * @throws QueryException 
	 * @throws Exception
	 */
	protected abstract void writeExtReferences(SaadaInstance obj) throws QueryException ;
	/**
	 * @throws Exception 
	 * 
	 */
	protected abstract void writeProtocolParamDescription() throws Exception;

	/**
	 * @param category
	 * @throws QueryException 
	 */
	protected  abstract void writeExtMetaReferences() throws QueryException ;

	/**
	 * @throws QueryException 
	 * 
	 */
	protected abstract void writeHousekeepingFieldAndGroup() throws QueryException ;

	/**
	 * @param length
	 * @throws Exception 
	 */
	protected abstract void writeDMFieldsAndGroups() throws Exception ;
	
	/**
	 * Only used for ADQL formators, does nothing orherwise
	 * @param adqlResultSet
	 */
	public void setAdqlResultSet(ADQLResultSet adqlResultSet){
	}

	/**
	 * Only used for ADQL formators, does nothing orherwise
	 * @param adqlQuery
	 */
	public void setAdqlQuery(SaadaADQLQuery adqlQuery) {
	}


	/**
	 * Gets the corresponding content type (for {@HttpServletResponse#setContentType(String)}).
	 * 
	 * @param format	MIME type or shortcut (for instance: votable is the shortcut of text/xml or application/x-votable+xml).
	 * @return			The corresponding content type string if supported by this TAP service, <i>null</i> else.
	 */
	public static final String getContentType(String format){
		if (format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("text/xml") || format.equalsIgnoreCase("application/x-votable+xml"))
			return "text/xml";
		else if (format.equalsIgnoreCase("html") || format.equalsIgnoreCase("text/html"))
			return "text/html";
		else if (format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("text/csv"))
			return "text/csv";
		else if (format.equalsIgnoreCase("tsv") || format.equalsIgnoreCase("text/tab-separated-values"))
			return "text/tsv";
		else if (format.equalsIgnoreCase("json") || format.equalsIgnoreCase("text/json")|| format.equalsIgnoreCase("application/json"))
			return "application/json";
		else if (format.equalsIgnoreCase("fits") || format.equalsIgnoreCase("application/fits"))
			return "application/";
		else
			return null;
	}

	/**
	 * Gets the file extension corresponding to the given format.
	 * 
	 * @param format	MIME type or shortcut (for instance: votable is the shortcut of text/xml or application/x-votable+xml).
	 * @return			The corresponding file extension if supported by this TAP service, <i>null</i> else.
	 */
	public static final String getFormatExtension(String format){
		if (format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("text/xml") || format.equalsIgnoreCase("application/x-votable+xml"))
			return ".xml";
		else if (format.equalsIgnoreCase("html") || format.equalsIgnoreCase("text/html"))
			return ".html";
		else if (format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("text/csv"))
			return ".csv";
		else if (format.equalsIgnoreCase("tsv") || format.equalsIgnoreCase("text/tab-separated-values"))
			return ".tsv";
		else if (format.equalsIgnoreCase("json") || format.equalsIgnoreCase("text/json")|| format.equalsIgnoreCase("application/json"))
			return ".json";
		else if (format.equalsIgnoreCase("fits") || format.equalsIgnoreCase("application/fits"))
			return ".fits";
		else if (format.equalsIgnoreCase("zip") || format.equalsIgnoreCase("application/zip"))
			return ".zip";
		else
			return null;
	}







}
