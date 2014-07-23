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
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;
import saadadb.util.zip.ZIPUtil;
import saadadb.util.zip.ZipEntryRef;
import saadadb.util.zip.ZipMap;
import saadadb.vo.VOLimits;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.request.formator.votable.OidsVotableFormator;
import saadadb.vo.request.formator.votable.SaadaqlVotableFormator;

/**
 * @author laurent
 * @version $Id$
 * 01/2014: add method zipInstance
 */
public class ZipFormator extends QueryResultFormator {
	private String responseDir;
	private ZipMap dataTree = new ZipMap();
	public final static int MAX_PRIMARY_SIZE = VOLimits.DOWNLOAD_MAXSIZE/1000000; //Mb
	public final String jobId;
	private String rootDir;

	/**
	 * @param jobId
	 */
	public ZipFormator(String jobId) {
		this.jobId = jobId;
		this.defaultSuffix  = QueryResultFormator.getFormatExtension("zip");
		this.limit = 100;
	}
	
	/**
	 * Build a zip ball  with instance and all the data linked through the "relations" list
	 * @param oid: saadaoid of the object
	 * @param dir: report directory
	 * @param relations: "any-relations" or cs list
	 * @param flatMode: put all data at the same level in the Zip ball
	 * @param cleanAfter: Remove all files from the output dir except the ZIPBALL itself
	 * @throws Exception
	 */
	public void zipInstance(long oid, String dir, String relations, boolean flatMode, boolean cleanAfter) throws Exception {
		ArrayList<Long> oids = new ArrayList<Long>();
		SaadaInstance si = Database.getCache().getObject(oid);			
		oids.add(oid);
		this.setResultSet(oids);
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("collection", si.getCollection().getName());
		params.put("category", Category.explain(si.getCategory()));
		params.put("relations", relations);
		this.setProtocolParams(params);
		String name = si.getFileName().split("\\.")[0];
		if( name == null ) {
			name = si.getObs_id().replaceAll("[^_a-zA-Z0-9\\-\\./]+", "_");
		}
		this.setResponseFilePath(dir, name);
		this.buildDataResponse(flatMode, cleanAfter);
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
		this.rootDir = this.protocolParams.get("collection") + "." +this.protocolParams.get("category");
		this.addPrimarySelection();
		for( String rel: relationsToInclude ) {
			this.addSecondarySelection(rel, false);
		}
		ZIPUtil.buildZipBall(dataTree, this.getResponseFilePath());
		WorkDirectory.emptyDirectory(new File(this.responseDir), (new File(this.getResponseFilePath()).getName()));
	}
	
	/**
	 * @param flatMode: put all data at the same level in the Zip ball
	 * @param cleanAfter: Remove all files from the output dir except the ZIPBALL itself
	 * @throws Exception
	 */
	public void buildDataResponse(boolean flatMode, boolean cleanAfter) throws Exception {
		this.rootDir = this.protocolParams.get("collection") + "." +this.protocolParams.get("category");
		this.addPrimarySelection();
		for( String rel: relationsToInclude ) {
			this.addSecondarySelection(rel, flatMode);
		}
		ZIPUtil.buildZipBall(dataTree, this.getResponseFilePath());
		if( cleanAfter )
			WorkDirectory.emptyDirectory(new File(this.responseDir), (new File(this.getResponseFilePath()).getName()));
	}

	/**
	 * Returns a prefix od filename. If there are more the one entry in the dataset to bestore, all
	 * files are prefixed with the primary oidsaada in order to let the users restore relation links
	 * @param oid
	 * @return
	 */
	private String getFileNamePrefix(long oid) {
		return (this.resultSize <= 1)? "": (oid + "_");
	}
	/**
	 * @throws Exception
	 */
	private void addPrimarySelection() throws Exception {

		TreeSet<ZipEntryRef> ts = new TreeSet<ZipEntryRef>();;
		/*
		 * Searches data are entry: we build a FITS table 
		 * should be configurable vs VOTable
		 */
		if( this.protocolParams.get("category").equalsIgnoreCase("entry")) {
			OidsVotableFormator primaryFormator = new OidsVotableFormator();
			primaryFormator.setProtocolParams(this.protocolParams);
			primaryFormator.setResultSet(oids);
			primaryFormator.setResponseFilePath(this.responseDir, "SourceSelection.vot");

			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Store primary response file " + primaryFormator.getResponseFilePath() );
			ts.add(new ZipEntryRef(ZipEntryRef.QUERY_RESULT, "SourceSelection.vot", primaryFormator.getResponseFilePath(), ZipEntryRef.WITH_REL));
			primaryFormator.buildDataResponse();			
		}
		/*
		 * Put all products in the primary selection otherwise
		 */
		else {
			long primary_size = 0;
			for( long oid: this.oids ) {
				SaadaInstance si = Database.getCache().getObject(oid);
				String path = Database.getCache().getObject(oid).getRepository_location();
				/*
				 * Add the oid to the name when there are relations in order to enable users to sort out individual links
				 */
				if( this.relationsToInclude.size() > 0 ) {
					ts.add(new ZipEntryRef(ZipEntryRef.SINGLE_FILE, this.getFileNamePrefix(oid) + si.getFileName(),si.getRepository_location(), ZipEntryRef.WITH_REL));
				}
				else {
					ts.add(new ZipEntryRef(ZipEntryRef.SINGLE_FILE, si.getFileName(),si.getRepository_location(), ZipEntryRef.WITH_REL));					
				}
				Messenger.printMsg(Messenger.TRACE, "Add primary file " + path );
				primary_size += (new File(path)).length()/1000000;
				if(  primary_size > MAX_PRIMARY_SIZE) {
					Messenger.printMsg(Messenger.WARNING, "Cumulated size of primary files exceed " + MAX_PRIMARY_SIZE + "Mb: primary collection truncated");
					break;
				}
			}
		}
		dataTree.put(this.rootDir, ts);
	}

	/**
	 * @param relationName
	 * @param flatMode: put all data at the same level in the Zip ball
	 * @throws Exception
	 */
	private void addSecondarySelection(String relationName, boolean flatMode) throws Exception {
		Set<ZipEntryRef> ts = new TreeSet<ZipEntryRef>();
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
				ArrayList<Long> loc_entry_cp_oids = new ArrayList<Long>();
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
					loc_entry_cp_oids.add(entry_cp_oid);
				}
				OidsVotableFormator secondaryFormator = new OidsVotableFormator();
				secondaryFormator.setProtocolParams(this.protocolParams);
				secondaryFormator.setResultSet(loc_entry_cp_oids);
				secondaryFormator.setResponseFilePath(this.responseDir,this.getFileNamePrefix(oid) + relationName + ".vot");
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
							+ secondaryFormator.getResponseFilePath()  + "(relation " + relationName + ")");
				ts.add(new ZipEntryRef(ZipEntryRef.QUERY_RESULT, this.getFileNamePrefix(oid) + relationName + ".vot", secondaryFormator.getResponseFilePath(), 0));
				secondaryFormator.buildDataResponse();			
			}
			/*
			 * Store the product file for others categories
			 */
			else {
				for(long cp: cps){
					si = Database.getCache().getObject(cp);
					ts.add(new ZipEntryRef(ZipEntryRef.SINGLE_FILE, this.getFileNamePrefix(oid) + si.getFileName(),si.getRepository_location(), 0));
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
								+ Database.getCache().getObject(cp).getRepository_location()  
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
			ts.add(new ZipEntryRef(ZipEntryRef.QUERY_RESULT, relationName + "_merged.vot", secondaryFormator.getResponseFilePath(), 0));
			secondaryFormator.buildDataResponse();			
		}
		if( flatMode ) {
			dataTree.get(this.rootDir).addAll(ts);
		} else {
			dataTree.put(this.rootDir + "/" + relationName , ts);
		}
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
