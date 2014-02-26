package saadadb.vo.formator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.query.executor.Query;
import saadadb.query.result.OidsaadaResultSet;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.QueryFileReport;

/**
 * @author laurentmichel
 *@version $Id: OtherAPToZIPFormator.java 555 2013-05-25 17:18:55Z laurent.mistahl $
 */
public class OtherAPToZIPFormator extends VOResultFormator{

	public OtherAPToZIPFormator(String votable_desc, String vores_type,
			String data_desc) {
		super(votable_desc, vores_type, data_desc);
	}

	public OtherAPToZIPFormator(int category, String resource_name) throws SaadaException {
		super(resource_name, "native " + Category.explain(category),"Saada Result service", "web:SimpleQueryResponse", "Query Result on SaadaDB " + Database.getName());
	}

	@Override
	public String buildMetadataFile() throws Exception {
		return null;
	}

	@Override
	protected String buildResultErrorFile(String cause) throws Exception {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#processVOQueryInStreaming(java.lang.String)
	 */
	public String processVOQueryInStreaming(String query) throws Exception {
		try {
			/*
			 * datatree: ZIP directories with files contained in
			 */
			Map<String, Set<String>> data_tree = new HashMap<String, Set<String>>();
			this.queryInfos = new QueryInfos();
			this.queryInfos.setSaadaqlQuery(query);
			/*
			 * Build the selection result
			 */
			TreeSet<String> ts ;
			/*
			 * Searches data are entry: we build a FITS table 
			 * should be configurable vs VOTable
			 */
			if( this.queryInfos.getCategory() == Category.ENTRY) {
				ts = new TreeSet<String>();
				QueryFileReport qfr = new QueryFileReport(QueryFileReport.NO_PROTOCOL, vo_resource.getName(),query, "votable");
				String select_file = Repository.getVoreportsPath() +  Database.getSepar() + "primary_selection.votable";
				qfr.getQueryReport(select_file, this.limit);
				ts.add(select_file);
			}
			/*
			 * Put all products in the primary selection otherwise
			 */
			else {
				ts = new TreeSet<String>();
				OidsaadaResultSet ors = (new Query()).runBasicQuery(query);
				long primary_size=0;
				while( ors.next() ) {
					String path = Database.getCache().getObject(ors.getOId()).getRepositoryPath();
					ts.add(path);
					primary_size += (new File(path)).length()/1000000;
					if(  primary_size > 200) {
						Messenger.printMsg(Messenger.WARNING, "The size of primary file exceed 200Mb: primary collection truncated");
						break;
					}
				}
			}
			data_tree.put("primary_selection" , ts);
			/*
			 * Now we run the query again to get the counterparts
			 */
			if( this.queryInfos.getQueryTarget() != QueryInfos.N_COLL) {
				Query q = new Query();
				SaadaQLResultSet resultset;
				if( this.isInMappedDmMode() ) {
					resultset = q.runQuery(query, vo_resource.getName(), true);
				}
				else {
					resultset = q.runQuery(query, true);
				}
				String relations[] = Database.getCachemeta().getRelationNamesStartingFromColl(q.getSfiClause().getListColl()[0], q.getSfiClause().getCatego());
				for(String rel: relations) {
					MetaRelation mr = Database.getCachemeta().getRelation(rel);
					ts = new TreeSet<String>();
					resultset.rewind();
					int cpt = 0;
					ArrayList<Long> entry_cp_oids = new ArrayList<Long>();
					String cp_class = "";
					boolean multiclass = false;
					while( resultset.next()) {
						SaadaInstance si = Database.getCache().getObject(resultset.getOid());
						long[] cps = si.getCounterparts(rel) ;
						/*
						 * Build a table with all counterparts for catalogue entries
						 */
						if( mr.getSecondary_category() == Category.ENTRY ) {
							for( long entry_cp_oid: cps ){
								if( !multiclass ) {
									if( cp_class.length() == 0 ) {
										cp_class = SaadaOID.getClassName(entry_cp_oid);
									}
									else if( !cp_class.equals(SaadaOID.getClassName(entry_cp_oid))) {
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
							}
						}
						if( cpt > this.limit) {
							break;
						}
						cpt++;
					}	
					/*
					 * Build a FITS table with entry counterparts
					 */
					if( mr.getSecondary_category() == Category.ENTRY ) {
						String cp_file = Repository.getVoreportsPath() +  Database.getSepar() + rel + "_counterpart.xml";
						VOTableFormator ff;
						if( !multiclass ) {
							ff = new OtherAPToVoTableFormator(Category.ENTRY, "class " + cp_class, cp_file);
						}
						else {
							ff = new OtherAPToVoTableFormator(Category.ENTRY, "", cp_file);
						}
						long[] cp_table = new long[entry_cp_oids.size()];
						for( int i=0 ; i<cp_table.length ; i++ ) {
							cp_table[i] = entry_cp_oids.get(i);
						}		
						ff.queryInfos = new QueryInfos();
						ff.queryInfos.setCategory(Category.ENTRY);
						ff.queryInfos.setQueryTarget(QueryInfos.ONE_COLL_ONE_CLASS);
						ff.buildResultFile(cp_table);
						ts.add(cp_file);
					}

					data_tree.put(rel , ts);

				}
			}
			//return ZIPUtil.buildZipBall(data_tree, this.result_filename);
			return null;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		
		}
	}	

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#processVOQueryInStreaming(java.lang.String, java.io.OutputStream)
	 */
	public void processVOQueryInStreaming(String query, OutputStream out) throws Exception {
		processVOQueryInStreaming(query);
		this.dumpResultFile(out);
	}
	@Override
	public String buildResultFile(SaadaQLResultSet rs) throws Exception {
		return null;
	}

	@Override
	public String buildResultFile(long[] oids) throws Exception {
		return null;
	}

	@Override
	protected void createMetadataResultFile() throws Exception {

	}

	@Override
	protected void createResultFile(SaadaQLResultSet rs) throws Exception {

	}

	@Override
	protected void createResultFile(long[] oids) throws Exception {

	}

	/* (non-Javadoc)
	 * @see saadadb.vo.VOResultFormator#dumpResultFile(long[], java.io.Writer)
	 */
	public void dumpResultFile(OutputStream out) throws Exception {
		BufferedInputStream br = new BufferedInputStream(new FileInputStream(this.result_filename));
		int lg;
		byte[] target = new byte[1024];
		while( (lg = br.read(target)) > 0  ) {
			out.write(target, 0, lg);
		}
		out.flush();
		br.close();
	}

	@Override
	protected void setDefaultReportFilename() {

	}

	@Override
	protected void writeDMData(SaadaInstance obj) throws Exception {

	}

	@Override
	protected void writeDMData(long oid, SaadaQLResultSet rs) throws Exception {

	}

	@Override
	protected void writeDMFieldsAndGroups(int length) throws Exception {

	}

	@Override
	protected void writeData(SaadaQLResultSet rs) throws Exception {

	}

	@Override
	protected void writeData(long[] oids) throws Exception {

	}

	@Override
	protected void writeExtMetaReferences(int category) throws QueryException {

	}

	@Override
	protected void writeExtReferences(SaadaInstance obj) {

	}

	@Override
	protected void writeHousekeepingFieldAndGroup() {

	}

	@Override
	protected void writeHouskeepingData(SaadaInstance obj)
	throws SaadaException {

	}

	@Override
	protected void writeNativeValues(SaadaInstance obj) throws Exception {

	}

	@Override
	protected void writeProtocolParamDescription() {

	}

}
