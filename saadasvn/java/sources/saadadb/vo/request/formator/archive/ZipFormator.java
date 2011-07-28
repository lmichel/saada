/**
 * 
 */
package saadadb.vo.request.formator.archive;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.util.ZIPUtil;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.request.formator.votable.OidsVotableFormator;
import saadadb.vo.request.formator.votable.SaadaqlVotableFormator;


/**
 * @author laurent
 * @version $Id$
 */
public class ZipFormator extends QueryResultFormator {
	private String responseDir;
	private Map<String, Set<String>> dataTree = new LinkedHashMap<String, Set<String>>();
	public final static int MAX_PRIMARY_SIZE = 200; //Mb
	public final String jobId;
	
	/**
	 * @param jobId
	 */
	public ZipFormator(String jobId) {
		this.jobId = jobId;
		this.defaultSuffix  = QueryResultFormator.getFormatExtension("zip");
		this.limit = 100;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResulmsgtSet) throws QueryException{			
		QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE
				, "ZipFormator does not support SaadaInstanceResultSet processing");
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.QueryResultFormator#supportResponseInRelation()
	 */
	public boolean supportResponseInRelation() {
		return true;
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.QueryResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	@Override
	public void setResultSet(ArrayList<Long> oids) {
		this.oids = oids;
		this.resultSize = oids.size();
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#setResponseFilePath(java.lang.String, java.lang.String)
	 */
	public void setResponseFilePath(String responseDir, String prefix) throws Exception {
		super.setResponseFilePath(responseDir, prefix);
		this.responseDir = responseDir;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#buildMetaResponse()
	 */
	public void buildMetaResponse() throws Exception {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.Formator#buildErrorResponse(java.lang.Exception)
	 */
	public void buildErrorResponse(Exception e) throws Exception {
		SaadaqlVotableFormator primaryFormator = new SaadaqlVotableFormator();
		Messenger.printMsg(Messenger.TRACE, "Build error response file " + primaryFormator.getResponseFilePath() );
		primaryFormator.setProtocolParams(this.protocolParams);
		primaryFormator.setResponseFilePath(this.responseDir, "PrimarySelection");
		primaryFormator.buildErrorResponse(e);
	}

	@Override
	public void buildDataResponse() throws Exception {
		this.addPrimarySelection();
		for( String rel: relationsToInclude ) {
			this.addSecondarySelection(rel);
		}
		ZIPUtil.buildZipBall(dataTree, this.getResponseFilePath());
	}

	/**
	 * @throws Exception
	 */
	private void addPrimarySelection() throws Exception {
		TreeSet<String> ts = new TreeSet<String>();;
		/*
		 * Searches data are entry: we build a FITS table 
		 * should be configurable vs VOTable
		 */
		if( this.protocolParams.get("category").equalsIgnoreCase("entry")) {
			OidsVotableFormator primaryFormator = new OidsVotableFormator();
			primaryFormator.setProtocolParams(this.protocolParams);
			primaryFormator.setResultSet(oids);
			primaryFormator.setResponseFilePath(this.responseDir, "PrimarySelection_" + this.jobId);

			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Store primary response file " + primaryFormator.getResponseFilePath() );
			ts.add(primaryFormator.getResponseFilePath());
			primaryFormator.buildDataResponse();			
		}
		/*
		 * Put all products in the primary selection otherwise
		 */
		else {
			long primary_size = 0;
			for( long oid: this.oids ) {
				String path = Database.getCache().getObject(oid).getRepositoryPath();
				ts.add(path);
				Messenger.printMsg(Messenger.TRACE, "Add primary file " + path );
				primary_size += (new File(path)).length()/1000000;
				if(  primary_size > MAX_PRIMARY_SIZE) {
					Messenger.printMsg(Messenger.WARNING, "Cumulated size of primary files exceed " + MAX_PRIMARY_SIZE + "Mb: primary collection truncated");
					break;
				}
			}
		}
		dataTree.put("primary_selection" , ts);
	}

	/**
	 * @param relationName
	 */
	private void addSecondarySelection(String relationName) throws Exception {
		Set<String> ts = new TreeSet<String>();
		MetaRelation mr = Database.getCachemeta().getRelation(relationName);
		String cp_class = "";
		boolean multiclass = false;
		ArrayList<Long> entry_cp_oids = new ArrayList<Long>();
		int cpt = 0;
		Map<String, String> secondaryParams = new LinkedHashMap<String, String>();
		secondaryParams.put("collection", mr.getSecondary_coll());
		secondaryParams.put("category", Category.explain(mr.getSecondary_category()));
		for( long oid: this.oids ) {
			SaadaInstance si = Database.getCache().getObject(oid);
			long[] cps = si.getCounterparts(relationName) ;
			/*
			 * Build a table with all counterparts for catalogue entries
			 */
			if( mr.getSecondary_category() == Category.ENTRY ) {
				for( long entry_cp_oid: cps ){
					if( !multiclass ) {
						if( cp_class.length() == 0 ) {
							cp_class = SaadaOID.getClassName(entry_cp_oid);
							secondaryParams.put("class", Category.explain(mr.getSecondary_category()));
						}
						else if( !cp_class.equals(SaadaOID.getClassName(entry_cp_oid))) {
							secondaryParams.remove("class");
							multiclass = true;
						}
					}
					entry_cp_oids.add(entry_cp_oid);
				}
			}
			/*
			 * Store the product file for others categories
			 */
			else {
				for(long cp: cps){
					ts.add(Database.getCache().getObject(cp).getRepositoryPath());
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
							+ Database.getCache().getObject(cp).getRepositoryPath()  
							+ "(relation " + relationName + ")");
				}
			}
			if( cpt > this.limit) {
				break;
			}
			cpt++;
		}

		/*
		 * Build a FITS/VO table with entry counterparts
		 */
		if( mr.getSecondary_category() == Category.ENTRY ) {
			OidsVotableFormator secondaryFormator = new OidsVotableFormator();
			secondaryFormator.setProtocolParams(this.protocolParams);
			secondaryFormator.setResultSet(entry_cp_oids);
			secondaryFormator.setResponseFilePath(this.responseDir, "SecondarySelection" + this.jobId);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
					+ secondaryFormator.getResponseFilePath()  + "(relation " + relationName + ")");
			ts.add(secondaryFormator.getResponseFilePath());
			secondaryFormator.buildDataResponse();			
		}
		dataTree.put(relationName , ts);
	}
	@Override
	protected void writeHouskeepingData(SaadaInstance obj)
	throws SaadaException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	@Override
	protected void writeExtReferences(SaadaInstance obj) throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	@Override
	protected void writeProtocolParamDescription() throws Exception {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	@Override
	protected void writeExtMetaReferences() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	@Override
	protected void writeHousekeepingFieldAndGroup() throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

	@Override
	protected void writeDMFieldsAndGroups() throws Exception {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for ZIP formator");
	}

}
