/**
 * 
 */
package saadadb.vo.request.formator.archive;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaInstanceResultSet;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;
import saadadb.util.zip.ZIPUtil;
import saadadb.util.zip.ZipEntryRef;
import saadadb.util.zip.ZipMap;
import saadadb.vo.cart.CartDecoder;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.request.formator.votable.OidsVotableFormator;
import saadadb.vo.request.formator.votable.SaadaqlVotableFormator;

/**
 * This formator transforms a JSON description of a shopping cart into a real ZIP ball
 * The Json string has the following structure:
 * {
 * nodename:{
 * 	"jobs":[entryrefs...]
 *    ,"urls":[entryrefs...]}
 * ,
 * nodename:{
 * 	"jobs":[entryrefs...]
 *    ,"urls":[entryrefs...]}
 * }
 * where entryrefs have the following form:
 * 
 * {"name":"...","uri":"....", "relations": [relations....]}
 * 
 * name: name of the entry within the archive
 * uri : entry identifier (oid or query)
 * relations: List of relation whose links must be added to the archive. "any-relations' means no filtering
 * 
 * The inner structure of the Zip ball is as follow
 * nodename -|
 *           | product files
 *           | ...
 *           | relation -|
 *                       | product file associated through that relation
 *                       
 * ...
 * If relations are added to the archive, product file names (no VOTables) are prepended with their OIDS if they are 
 * in the node name directory or with the primary OID if they are in a relation directory. That allows users
 * to easily reconstruct associations              
 * 
 * This formator has the same API as all VOI formators {@link saadadb.vo.request.VORequest VORequest}
 * @author laurent
 * @version $Id$
 * 
 * 01/2014: addLinkzedData: linked spectra are replaced with a zip ball containing their linked data too
 */
public class CartFormator  extends QueryResultFormator{
	/**
	 * Directory where the response is built
	 */
	private String responseDir;
	/**
	 * Archive map
	 */
	private ZipMap zipMap = new ZipMap();

	/**
	 * @param jobId
	 */
	public CartFormator(String jobId) {
		this.defaultSuffix  = QueryResultFormator.getFormatExtension("zip");
		this.limit = 100;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.QueryResultFormator#setProtocolParams(java.util.Map)
	 */
	public void setProtocolParams(Map<String, String> fmtParams) throws Exception{
		this.protocolParams = fmtParams;
		if( fmtParams.get("cart") == null ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "No cart content  in formator parameters");
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.vo.request.formator.VOResultFormator#setResultSet(saadadb.query.result.SaadaInstanceResultSet)
	 */
	public void setResultSet(SaadaInstanceResultSet saadaInstanceResulmsgtSet) throws QueryException{			
		QueryException.throwNewException(SaadaException.UNSUPPORTED_MODE
				, "CartFormator does not support SaadaInstanceResultSet processing");
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
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Make non sense for Cart formator");
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
		CartDecoder decoder = new CartDecoder();
		decoder.decode(this.protocolParams.get("cart"));
		this.zipMap = decoder.getZipMap();
		this.prepareData();
		ZIPUtil.buildZipBall(zipMap, this.getResponseFilePath());
		WorkDirectory.emptyDirectory(new File(this.responseDir), (new File(this.getResponseFilePath()).getName()));
	}


	/**
	 * Build all data files to be zipped within the archive.
	 * Data files can either be simple files stored within the DB or VOTable 
	 * generated from source selection.
	 * @throws Exception
	 */
	private void prepareData() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Start to prepare data files");
		/*
		 * Iterate on a copy of the Zipmap key because the content of
		 * zipMap can be extended by the insertion of linked data
		 * That avoids a concurrent update exception
		 */
		String[] keys = this.zipMap.keySet().toArray(new String[0]);
		for( String node: keys) {
			this.prepareZipNodeData(node);
		}
	}

	/**
	 * Build all data files to be zipped within the archive into the node node.
	 * @param node First level directory name
	 * @throws Exception
	 */
	private void prepareZipNodeData(String node) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Prepare data files for node " + node);
		Set<ZipEntryRef> entrySet = this.zipMap.get(node);
		Set<ZipEntryRef> entrySetToAdd = new LinkedHashSet<ZipEntryRef>();
		Set<ZipEntryRef> entrySetToRemove = new LinkedHashSet<ZipEntryRef>();
		/*
		 * Single files and queries are processed separately. 
		 * Single files are just referenced with their real path in the ZipEntryRef
		 * whereas queries must be executed and their results stored with a VOTable
		 * which will referenced in the ZipEntryRef too
		 */
		for(ZipEntryRef zer:  entrySet) {
			if( zer.getType() == ZipEntryRef.SINGLE_FILE ) {
				long oid = Long.parseLong(zer.getUri());
				SaadaInstance si = Database.getCache().getObject(oid);
				zer.setUri(si.getRepository_location());
				if( zer.includeLinkedData() ) {
					/*
					 * If linked data are also taken, filenames are prepended with OIDa to
					 * allow users to reconstruct associations
					 */
					zer.setName(oid + "_" + zer.getName());
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Add linked data");
					this.addAllLinkedData(node, si);
				}
			} else {
				/*
				 * We use a VOFormator to build the VOTable. 
				 * No VO request can bee used here because they don't allow to read 
				 * twice the resultset as we need to get associated data
				 */
				Query q = new Query();
				OidsaadaResultSet ors = q.runBasicQuery(zer.getUri());
				/*
				 * These parameters are request to set the VOTable columns
				 */
				protocolParams.put("collection", Merger.getMergedArray(q.getSfiClause().getListColl()));
				protocolParams.put("class"     , Merger.getMergedArray(q.getSfiClause().getListClass()));
				protocolParams.put("category"  , Category.explain(q.getSfiClause().getCatego()));
				oids = new ArrayList<Long>();
				while( ors.next()) {
					oids.add(ors.getOId());
				}
				ors.close();
				ors = null;

				/*
				 * If the query is apply on catalogue entries,a VOTable is generated and set as ZipENtryRef URI
				 * Otherwise all data files returned by the query are appended to the ZIOP MAP
				 */
				if( q.getSfiClause().getCatego() == Category.ENTRY ) {
					prepareZipNodeVOTable(node, zer, entrySetToAdd);
				} else {
					extendZipNodeToProductList(node, entrySetToAdd,zer.getOptions());	
					// The query netry is actually replaced with the set of porducts resulting
					// from that query
					entrySetToRemove.add(zer);
				}
			}
		}
		entrySet.addAll(entrySetToAdd);
		entrySet.removeAll(entrySetToRemove);
		if( Messenger.debug_mode ) {
			for(ZipEntryRef zer:  entrySet) {
				Messenger.printMsg(Messenger.DEBUG, zer.toString());
			}
		}
	}
	/**
	 * Build a set of nodes containing data products references by OIDs
	 * @param node : Current node of the zipmap: used to add linked data
	 * @param entrySetToAdd : where new nodes are appended 
	 * @param options : Options of the initiator node
	 * @throws Exception
	 */
	private void extendZipNodeToProductList(String node, Set<ZipEntryRef> entrySetToAdd, int options) throws Exception {
		for( long oid: oids) {
			SaadaInstance si = Database.getCache().getObject(oid);

			ZipEntryRef zer = new ZipEntryRef(ZipEntryRef.SINGLE_FILE, Long.toString(oid) + "_" + si.getFileName(), si.getRepository_location(), options);
			if( zer.includeLinkedData() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add linked data");
				this.addAllLinkedData(node, si);
			}
			entrySetToAdd.add(zer);
		}

	}

	/**
	 * Build a VOtable with the set of oids
	 * @param node: Current node of the zipmap: used to add linked data
	 * @param zipEntryRef
	 * @param entrySetToAdd
	 * @throws Exception
	 */
	private void prepareZipNodeVOTable(String node, ZipEntryRef zipEntryRef, Set<ZipEntryRef> entrySetToAdd) throws Exception {
		/*
		 * Stores the query in a text file
		 */
		String reportName = zipEntryRef.getName()+ ".query.txt";
		File f = new File(this.responseDir + File.separator +  reportName);
		FileWriter fw = new FileWriter(f);
		fw.write("Query of job " + zipEntryRef.getName() + "\n");
		fw.write(zipEntryRef.getUri() + "\n");
		fw.close();
		entrySetToAdd.add(new ZipEntryRef(ZipEntryRef.SINGLE_FILE, reportName, f.getAbsolutePath()));
		/*
		 * Stores the query result
		 */
		OidsVotableFormator formator = new OidsVotableFormator();
		formator.setProtocolParams(this.protocolParams);
		formator.setResultSet(oids);
		formator.setResponseFilePath(this.responseDir, zipEntryRef.getName());
		formator.buildDataResponse();			
		String name = zipEntryRef.getName();
		if( !name.endsWith(".vot") && !name.endsWith(".xml")) {
			zipEntryRef.setName(name + ".xml");
		}
		zipEntryRef.setUri(this.responseDir + File.separator + zipEntryRef.getName());
		if( zipEntryRef.includeLinkedData() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Add linked data");
			for( long oid: oids ){
				this.addAllLinkedData(node, Database.getCache().getObject(oid));					
			}
		}		
	}

	/**
	 * Prepare all data files associated with the SaadaInstance. 
	 * @param node  First level directory name
	 * @param si    Primary SaadaInstance
	 * @throws Exception
	 */
	private void addAllLinkedData(String node, SaadaInstance si)  throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Add linked data for oid " + si.obs_id);
		String[] relations = si.getSaadaClass().getStartingRelationNames();
		for( String  rel: relations) {
			this.addLinkedData(node, si, rel);
		}
	}


	/**
	 * Prepare all data files associated with the SaadaInstance through the relation. 
	 * If the counterpart is a simple file, its reference is just added to the ZipMap.
	 * If it is a source selection, a VOTable is built which is also added to the ZipMap.
	 * The names of the zipped files are prepended with the primary oid in order to allow users
	 * to reconstruct association.
	 * @param node     First level directory name
	 * @param si       Primary SaadaInstance
	 * @param relation Relation to be considered
	 * @throws Exception
	 */
	private void addLinkedData(String node, SaadaInstance si, String relation)  throws Exception{
		long[] cpoids = si.getCounterparts(relation);
		if( cpoids.length > 0 ) {
			String root = node + "/" + relation;
			MetaRelation mr = Database.getCachemeta().getRelation(relation);
			/*
			 * Linked spectra are packed with their attached data which are usually calibration data
			 */
			if( mr.getSecondary_category() == Category.SPECTRUM ) {
				ZipFormator formator = new ZipFormator(null);
				for( long cpoid: cpoids) {
					formator.zipInstance(cpoid, this.responseDir, "any-relations", true, false);
					String zpn = formator.getResponseFilePath();
					ZipEntryRef zer = new ZipEntryRef(ZipEntryRef.SINGLE_FILE, si.oidsaada + "_" + (new File(zpn)).getName(), zpn);
					this.zipMap.add(root, zer);
				}
			} else if( mr.getSecondary_category() != Category.ENTRY ) {
				for( long cpoid: cpoids) {
					SaadaInstance cpi = Database.getCache().getObject(cpoid);
					ZipEntryRef zer = new ZipEntryRef(ZipEntryRef.SINGLE_FILE, si.oidsaada + "_" + cpi.getFileName(), cpi.getRepository_location());
					this.zipMap.add(root, zer);
				}
			} else {
				String resultFilename = si.oidsaada + "_LinkedSources.vot";
				ArrayList<Long> loc_entry_cp_oids = new ArrayList<Long>();
				for( long entry_cp_oid: cpoids ){
					loc_entry_cp_oids.add(entry_cp_oid);
				}
				OidsVotableFormator secondaryFormator = new OidsVotableFormator();
				/*
				 * Notify the formator that VO table contain catalogue Entries
				 */
				this.protocolParams.put("category", Category.explain(mr.getSecondary_category()));
				secondaryFormator.setProtocolParams(this.protocolParams);
				secondaryFormator.setResultSet(loc_entry_cp_oids);
				secondaryFormator.setResponseFilePath(this.responseDir, resultFilename);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
							+ secondaryFormator.getResponseFilePath()  + "(relation " + relation + ")");
				this.zipMap.add(root, new ZipEntryRef(ZipEntryRef.QUERY_RESULT,resultFilename, secondaryFormator.getResponseFilePath(), 0));
				secondaryFormator.buildDataResponse();			
			}
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
