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
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
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
 */
public class CartFormator  extends QueryResultFormator{
	/**
	 * Directory where the response is built
	 */
	private String responseDir;
	/**
	 * Archive map
	 */
	private ZipMap dataTree = new ZipMap();

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
		this.dataTree = decoder.getZipMap();
		this.prepareData();
		ZIPUtil.buildZipBall(dataTree, this.getResponseFilePath());
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
		for( String node: this.dataTree.keySet()) {
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
		Set<ZipEntryRef> entrySet = this.dataTree.get(node);
		Set<ZipEntryRef> entrySetToAdd = new LinkedHashSet<ZipEntryRef>();
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
				zer.setUri(si.getRepositoryPath());
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
			}
			else {
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
				 * Stores the query in a text file
				 */
				String reportName = zer.getName()+ ".query.txt";
				File f = new File(this.responseDir + File.separator +  reportName);
				FileWriter fw = new FileWriter(f);
				fw.write("Query of job " + zer.getName() + "\n");
				fw.write(zer.getUri() + "\n");
				fw.close();
				entrySetToAdd.add(new ZipEntryRef(ZipEntryRef.SINGLE_FILE, reportName, f.getAbsolutePath()));
				/*
				 * Stores the query result
				 */
				OidsVotableFormator formator = new OidsVotableFormator();
				formator.setProtocolParams(this.protocolParams);
				formator.setResultSet(oids);
				formator.setResponseFilePath(this.responseDir, zer.getName());
				formator.buildDataResponse();			
				String name = zer.getName();
				if( !name.endsWith(".vot") && !name.endsWith(".xml")) {
					zer.setName(name + ".xml");
				}
				zer.setUri(this.responseDir + File.separator + zer.getName());
				if( zer.includeLinkedData() ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Add linked data");
					for( long oid: oids ){
						this.addAllLinkedData(node, Database.getCache().getObject(oid));					
					}
				}
			}
		}
		entrySet.addAll(entrySetToAdd);
	}

	/**
	 * Prepare all data files associated with the SaadaInstance. 
	 * @param node  First level directory name
	 * @param si    Primary SaadaInstance
	 * @throws Exception
	 */
	private void addAllLinkedData(String node, SaadaInstance si)  throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Add linked data for oid " + si.getNameSaada());
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
			if( Database.getCachemeta().getRelation(relation).getSecondary_category() != Category.ENTRY ) {
				for( long cpoid: cpoids) {
					SaadaInstance cpi = Database.getCache().getObject(cpoid);
					ZipEntryRef zer = new ZipEntryRef(ZipEntryRef.SINGLE_FILE, si.getOid() + "_" + cpi.getFileName(), cpi.getRepositoryPath());
					this.dataTree.add(root, zer);
				}
			}
			else {
				String resultFilename = si.getOid() + "_LinkedSources.vot";
				ArrayList<Long> loc_entry_cp_oids = new ArrayList<Long>();
				for( long entry_cp_oid: cpoids ){
					loc_entry_cp_oids.add(entry_cp_oid);
				}
				OidsVotableFormator secondaryFormator = new OidsVotableFormator();
				secondaryFormator.setProtocolParams(this.protocolParams);
				secondaryFormator.setResultSet(loc_entry_cp_oids);
				secondaryFormator.setResponseFilePath(this.responseDir, resultFilename);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Store secondary response file " 
							+ secondaryFormator.getResponseFilePath()  + "(relation " + relation + ")");
				this.dataTree.add(root, new ZipEntryRef(ZipEntryRef.QUERY_RESULT,resultFilename, secondaryFormator.getResponseFilePath(), 0));
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
