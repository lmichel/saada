package saadadb.vo.formator;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.query.executor.Query;
import saadadb.query.parser.SelectFromIn;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.SaadaQLExecutor;
import saadadb.vo.translator.VOTranslator;



/**
 * @author michel
 *@version $Id: VOResultFormator.java 555 2013-05-25 17:18:55Z laurent.mistahl $
 */
public abstract class VOResultFormator {
	protected static int incr = 1;
	protected QueryInfos queryInfos;	
	protected boolean hasExtensions = false, allowsExtensions = false;
	// this flag delay the selection of the DM if the requested DM is "full native".
	// The DM chosen is either 'native category' or 'class classname' according to the query coverage
	protected boolean full_native = false;	protected VOResource vo_resource;
	protected int limit = -1;	
	protected String votable_desc, vores_type, data_desc;
	protected String result_filename;
	protected LinkedHashMap<String, String> cgiParams = new LinkedHashMap<String, String>();

	/**
	 * Constructor.
	 * @throws SaadaException 
	 */
	public VOResultFormator(String voresource_name, String default_resource, String votable_desc,String vores_type, String data_desc) throws SaadaException {
		this(votable_desc, vores_type, data_desc);
		setVOResource(voresource_name, default_resource);
		this.setDefaultReportFilename();
	}

	/**
	 * @param voresource_name
	 * @param default_resource
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @throws SaadaException
	 */
	public VOResultFormator(String voresource_name, String default_resource, String votable_desc,String vores_type, String data_desc, String result_filename) throws SaadaException {
		this(votable_desc, vores_type, data_desc);
		setVOResource(voresource_name, default_resource);
		if( result_filename != null ) {
			this.result_filename = result_filename;
		}
		else {
			vo_resource = Database.getCachemeta().getVOResource(voresource_name);
			if( vo_resource == null ) {
				Messenger.printMsg(Messenger.WARNING, "Default resource (" + default_resource + ") taken in replacement of <" + voresource_name + ">");
				vo_resource = Database.getCachemeta().getVOResource(default_resource);
				if( vo_resource == null) {
					QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "Default resource <" + default_resource + "> not found");
				}
			}	
		}
	}

	/**
	 * @param votable_desc
	 * @param vores_type
	 * @param data_desc
	 * @param result_filename
	 */
	public VOResultFormator(String votable_desc,String vores_type, String data_desc, String result_filename) {
		this.votable_desc  = votable_desc;
		this.vores_type = vores_type;
		this.data_desc = data_desc;
		if( result_filename != null ) {
			this.result_filename = result_filename;
		}
		else {
			this.setDefaultReportFilename();			
		}
	}

	/**
	 * 
	 */
	public VOResultFormator(String votable_desc,String vores_type, String data_desc) {
		this.votable_desc  = votable_desc;
		this.vores_type = vores_type;
		this.data_desc = data_desc;
		this.setDefaultReportFilename();
	}

	/**
	 * Stores a copy of the params map with key in lower case
	 * @param cgi_params
	 */
	public void setCGIParams(Map<String, String[]> cgi_params ){
		/*
		 * Make a Map copy because it could be altered 
		 */
		if( cgi_params != null ) {
			for( Entry<String, String[]> e: cgi_params.entrySet()) {
				String v;
				if( e.getValue().length == 0 ) {
					v = "";
				}
				else  {
					v = e.getValue()[0];
				}
				cgiParams.put(e.getKey().toLowerCase(),v);
			}
		}
	}
	
	/**
	 * Return the CGI parameter param if it exist or "not set" string
	 * @param param
	 * @return
	 */
	public String getCGIParam(String param) {
		String retour = cgiParams.get(param);
		if( param == null || retour == null) {
			return "not set";
		}
		else return retour;
	}

	/**
	 * @param voresource_name
	 * @param default_resource
	 * @throws SaadaException
	 */
	private void  setVOResource(String voresource_name, String default_resource) throws SaadaException {
		if( "full native".equalsIgnoreCase(voresource_name) ) {
			Messenger.printMsg(Messenger.DEBUG, "Full native mode: level of the model will be chosen at query time ");

			this.full_native = true;

		}
		else {
			vo_resource = Database.getCachemeta().getVOResource(voresource_name);
			if( vo_resource == null ) {
				Messenger.printMsg(Messenger.WARNING, "Default resource (" + default_resource + ") taken in replacement of <" + voresource_name + ">");
				vo_resource = Database.getCachemeta().getVOResource(default_resource);
				if( vo_resource == null) {
					QueryException.throwNewException(SaadaException.MISSING_RESOURCE, "Default resource <" + default_resource + "> not found");
				}
			}	
		}
	}
	/**
	 * @return
	 */
	public String getResult_filename() {
		return result_filename;
	}

	/**
	 * @param result_filename
	 */
	public void setResult_filename(String result_filename) {
		this.result_filename = result_filename;
	}
	/**
	 * 
	 */
	protected abstract void setDefaultReportFilename();

	/**
	 * @param qi
	 * @throws SaadaException 
	 */
	public void setQueryInfos(QueryInfos qi) throws SaadaException {
		this.queryInfos = qi;
		if( this.full_native) {
			if( queryInfos.getQueryTarget() == QueryInfos.ONE_COLL_ONE_CLASS ) {
				vo_resource = Database.getCachemeta().getVOResource("class " + this.queryInfos.getClassName());
			}
			else {
				vo_resource = Database.getCachemeta().getVOResource("native " + Category.explain(this.queryInfos.getCategory()));
			}
		}	
	}

	/**
	 * @return
	 */
	public QueryInfos getQueryInfos() {
		return this.queryInfos;
	}

	/**
	 * @param limit
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	/**
	 * @param oids
	 * @param out
	 * @throws Exception
	 */
	public abstract void dumpResultFile(OutputStream out) throws Exception ;


	/**
	 * Process the query encapsulated into the translator and return the path of the result file
	 * @param translator
	 * @return
	 * @throws Exception
	 */
	public String processVOQuery(VOTranslator translator) throws Exception {
		try {
			if( translator.isMetadataRequired() ) {
				this.setQueryInfos(translator.queryInfos);
				return this.buildMetadataFile();		
			}
			else {
				String saadaQuery = translator.translate();	
				this.setQueryInfos(translator.queryInfos);
				SaadaQLExecutor executor = new SaadaQLExecutor();
				return this.buildResultFile(executor.execute(saadaQuery));		
			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		

		}
	}


	/**
	 * Process the query encapsulated into the translator and print the result in the Stream
	 * @param translator
	 * @param out
	 * @throws Exception
	 */
	public void processVOQuery(VOTranslator translator, OutputStream out) throws Exception {
		try {
			if( translator.isMetadataRequired() ) {
				this.setQueryInfos(translator.queryInfos);
				this.buildMetadataFile();		
			}
			else {
				String saadaQuery = translator.translate();	
				this.setQueryInfos(translator.queryInfos);
				SaadaQLExecutor executor = new SaadaQLExecutor();
				this.buildResultFile(executor.execute(saadaQuery));		
			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			this.buildResultErrorFile(e.toString());		

		}
		this.dumpResultFile(out);
	}

	/**
	 * Process the query using the streaming mode of the query engine.
	 * So that the report can be build on the fly instead of building individual SaadaInstances
	 * one by one
	 * @param translator
	 * @return
	 * @throws Exception
	 */
	public void processVOQueryInStreaming(VOTranslator translator, OutputStream out) throws Exception {
		try {
			if( translator.isMetadataRequired() ) {
				this.setQueryInfos(translator.queryInfos);
				this.buildMetadataFile();		
			}
			else {
				String saadaQuery = translator.translate();	
				this.setQueryInfos(translator.queryInfos);
				SaadaQLExecutor executor = new SaadaQLExecutor();
				this.buildResultFile(executor.executeInStreaming(saadaQuery));		
			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			this.buildResultErrorFile(e.toString());		

		}
		this.dumpResultFile(out);
	}

	/**
	 * Process the query using the streaming mode of the query engine.
	 * So that the report can be build on the fly instead of building individual SaadaInstances
	 * one by one
	 * @param translator
	 * @param translator
	 * @return
	 * @throws Exception
	 */
	public String processVOQueryInStreaming(VOTranslator translator) throws Exception {
		try {
			if( translator.isMetadataRequired() ) {
				this.setQueryInfos(translator.queryInfos);
				return this.buildMetadataFile();		
			}
			else {
				String saadaQuery = translator.translate();	
				this.setQueryInfos(translator.queryInfos);
				SaadaQLExecutor executor = new SaadaQLExecutor();
				return this.buildResultFile(executor.executeInStreaming(saadaQuery));		
			}
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		

		}
	}

	/**
	 * @param query
	 * @param out
	 * @throws Exception
	 */
	public void processVOQueryInStreaming(String query, OutputStream out) throws Exception {
		try {
			QueryInfos qi = new QueryInfos();
			qi.setSaadaqlQuery(query);
			Query q = new Query();
			SaadaQLResultSet resultset;
			if( this.isInMappedDmMode() ) {
				q.setDM(vo_resource.getName());
				resultset = q.runQuery(query, true);
			}
			else {
				resultset = q.runQuery(query, true);
			}
			qi.setCategory(q.getSfiClause().getCatego());
			if( q.getSfiClause().getMode()  == SelectFromIn.ONE_COL_ONE_CLASS ) {
				qi.setQueryTarget(QueryInfos.ONE_COLL_ONE_CLASS);
				qi.setClassName(q.getSfiClause().getListClass()[0]);
			}
			/*
			 * If no DM has been selected by the creator, the transient one is selected. 
			 */
			if( vo_resource == null ) {
				this.vo_resource = VOResource.getNativeVOResource("Transient Data Model", resultset.getMeta());
			}
			this.setQueryInfos(qi);
			this.buildResultFile(resultset);		
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			this.buildResultErrorFile(e.toString());		

		}
		this.dumpResultFile(out);
	}
	/**
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public String processVOQueryInStreaming(String query) throws Exception {
		try {
			QueryInfos qi = new QueryInfos();
			qi.setSaadaqlQuery(query);
			Query q = new Query();
			SaadaQLResultSet resultset;
			if( this.isInMappedDmMode() ) {
				q.setDM(vo_resource.getName());
				resultset = q.runQuery(query, true);
			}
			else {
				resultset = q.runQuery(query, true);
			}
			qi.setCategory(q.getSfiClause().getCatego());
			if( q.getSfiClause().getMode()  == SelectFromIn.ONE_COL_ONE_CLASS ) {
				qi.setQueryTarget(QueryInfos.ONE_COLL_ONE_CLASS);
				qi.setClassName(q.getSfiClause().getListClass()[0]);
			}
			/*
			 * If no DM has been selected by the creator, the transient one is selected. 
			 */
			if( vo_resource == null ) {
				this.vo_resource = VOResource.getNativeVOResource("Transient Data Model", resultset.getMeta());
			}
			this.setQueryInfos(qi);
			return this.buildResultFile(resultset);		
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		
		}
	}
	/**
	 * @param primoid
	 * @param rel_name
	 * @return
	 * @throws Exception
	 */
	public String processVOQuery(long primoid, String rel_name) throws Exception {
		this.setQueryInfos(new QueryInfos());
		try {
			SaadaQLExecutor executor = new SaadaQLExecutor();
			return this.buildResultFile(executor.getCounterparts(primoid, rel_name));
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		
		}
	}


	/**
	 * @param primoid
	 * @param rel_name
	 * @param out
	 * @throws Exception
	 */
	public void processVOQuery(long primoid, String rel_name, OutputStream out) throws Exception {
		this.setQueryInfos(new QueryInfos());
		try {
			SaadaQLExecutor executor = new SaadaQLExecutor();
			this.buildResultFile(executor.getCounterparts(primoid, rel_name));
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			this.buildResultErrorFile(e.toString());		
		}
		this.dumpResultFile(out);
	}


	/**
	 * @param primoid
	 * @return
	 * @throws Exception
	 */
	public String processVOQuery(long[] primoids) throws Exception {
		this.setQueryInfos(new QueryInfos());
		try {
			return this.buildResultFile(primoids);
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			return this.buildResultErrorFile(e.toString());		
		}
	}

	/**
	 * @param primoid
	 * @param out
	 * @return
	 * @throws Exception
	 */
	public void processVOQuery(long[] primoids, OutputStream out) throws Exception {
		this.setQueryInfos(new QueryInfos());
		try {
			this.buildResultFile(primoids);
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			this.buildResultErrorFile(e.toString());		
		}
		this.dumpResultFile(out);
	}
	/**
	 * Returns true if the formator works with a data model mapped on real data
	 * @return
	 */
	public boolean isInMappedDmMode () {
		if( vo_resource != null ) {
			String lc_name = vo_resource.getName().toLowerCase();		
			if( !lc_name.startsWith("native") && !lc_name.startsWith("class") && !lc_name.endsWith("default")){
				return true;
			}
		}
		return false;
	}

	/**
	 * @param oids
	 * @return
	 * @throws Exception
	 */
	abstract public String buildResultFile(SaadaQLResultSet rs) throws Exception ;
	/**
	 * @param cause
	 * @return
	 * @throws Exception
	 */
	abstract  protected String buildResultErrorFile(String cause) throws Exception ;
	/**
	 * @param oids
	 * @return
	 * @throws Exception
	 */
	abstract public String buildResultFile(long[] oids) throws Exception ;
	/**
	 * @return
	 * @throws Exception 
	 */
	abstract public String buildMetadataFile() throws Exception;
	/**
	 * @param oids
	 * @param category
	 * @throws Exception
	 */
	protected abstract void createResultFile(SaadaQLResultSet rs) throws Exception;
	/**
	 * @param oids
	 * @param category
	 * @throws Exception
	 */
	protected abstract void createResultFile(long[] oids) throws Exception;

	/**
	 * @param oid
	 * @throws Exception
	 */
	protected abstract void writeDMData(SaadaInstance obj) throws Exception ;
	/**
	 * @param oid
	 * @param rs
	 * @throws Exception
	 */
	protected abstract void writeDMData(long oid, SaadaQLResultSet rs) throws Exception ;

	/**
	 * @param length
	 * @throws SaadaException 
	 */
	protected void writeMetaData(long[] oids) throws Exception {
		if( oids == null ) {
			this.writeDMFieldsAndGroups(0);
		}
		else {
			this.writeDMFieldsAndGroups(oids.length);			
		}
		this.writeHousekeepingFieldAndGroup();
		//this.writeMappedUtypeFieldAndGroup(oids);
		//this.writeAttExtendFieldsAndGroup(category);
		this.writeExtMetaReferences(this.queryInfos.getCategory());
	}
	/**
	 * @param length
	 * @throws SaadaException 
	 */
	protected void writeMetaData(SaadaQLResultSet rs) throws Exception {
		if( rs == null ) {
			this.writeDMFieldsAndGroups(0);
		}
		else {
			this.writeDMFieldsAndGroups(rs.getSize());			
		}
		this.writeHousekeepingFieldAndGroup();
		//this.writeMappedUtypeFieldAndGroup(oids);
		//this.writeAttExtendFieldsAndGroup(category);
		this.writeExtMetaReferences(this.queryInfos.getCategory());
	}

	/**
	 * @param rs
	 * @throws Exception
	 */
	protected abstract void writeData(SaadaQLResultSet rs) throws Exception ;
	/**
	 * @param oids
	 * @param category
	 * @throws Exception
	 */
	protected abstract void writeData(long[] oids) throws Exception ;
	/**
	 * @param oid
	 * @throws SaadaException
	 */
	protected abstract void writeHouskeepingData(SaadaInstance obj) throws SaadaException ;
	/**
	 * @param obj
	 * @throws Exception
	 */
	protected abstract void writeNativeValues(SaadaInstance obj) throws Exception ;
	/**
	 * @param oid
	 */
	protected abstract void writeExtReferences(SaadaInstance obj) ;
	/**
	 * 
	 */
	protected abstract void writeProtocolParamDescription();

	/**
	 * @param category
	 * @throws QueryException 
	 */
	protected  abstract void writeExtMetaReferences(int category) throws QueryException ;

	/**
	 * 
	 */
	protected abstract void writeHousekeepingFieldAndGroup() ;

	/**
	 * @param length
	 * @throws Exception 
	 */
	protected abstract void writeDMFieldsAndGroups(int length) throws Exception ;

	/**
	 * @param category
	 * @throws Exception
	 */
	protected abstract void createMetadataResultFile() throws Exception;

	/*
	 * The following methods are used to build dynamically a DM from the selected data
	 */


	public String getCollectionName() {
		int fin = this.queryInfos.getInputSaadaTable().indexOf("(");
		if (fin == -1) {
			fin = this.queryInfos.getInputSaadaTable().indexOf("]");
		}
		if (fin == -1) {
			fin = this.queryInfos.getInputSaadaTable().length() - 1;
		}
		if (this.queryInfos.getInputSaadaTable().startsWith("[")) {
			return this.queryInfos.getInputSaadaTable().substring(1, fin);
		} else {
			return this.queryInfos.getInputSaadaTable().substring(0, fin);
		}
	}

	/**
	 * @param category
	 * @return
	 */
	protected boolean allowsExtensions(int category) {
		this.hasExtensions = false;
		if( this.queryInfos != null && (this.queryInfos.getQueryTarget() == QueryInfos.ONE_COLL_ONE_CLASS || this.queryInfos.getQueryTarget() == QueryInfos.ONE_COLL_N_CLASS) 
				&& this.queryInfos.isExtensionAllowed() 
				&& Database.getCachemeta().getRelationNamesStartingFromColl(this.getCollectionName(), category).length > 0 ) {
			this.hasExtensions = true;
		}
		return this.hasExtensions;
	}

	/**
	 * @param category
	 * @return
	 */
	protected String[] getRelationNames() {
		return  Database.getCachemeta().getRelationNamesStartingFromColl(this.getCollectionName(), this.queryInfos.getCategory());

	}



}
