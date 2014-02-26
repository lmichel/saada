package saadadb.vo.formator;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.vo.ADQLExecutor;
import saadadb.vo.SaadaQLExecutor;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLQuery;
import ajaxservlet.formator.DefaultPreviews;

/**
 * Formats the result of an ADQL query in a VOTable document.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 10/05/2010
 *@version $Id: TapToJsonFormator.java 555 2013-05-25 17:18:55Z laurent.mistahl $
 */
public class TapToJsonFormator extends  VOResultFormator {

	protected boolean adqlQuery;
	protected String queryString;

	protected ADQLQuery query;
	private int nbCol = 0;
	private PrintWriter writer;
	private SaadaQLResultSet result;
	private int oidsaada=-1;
	private int oidtable=-1;
	private int product_url_csa=-1;


	/**
	 * @throws SaadaException
	 */
	public TapToJsonFormator() throws SaadaException {
		super(null, "native MISC", "Saada TAP service", "dal:SimpleQueryResponse", "TAP search result on SaadaDB " + Database.getDbname());
	}
	
	/**
	 * Processes the given ADQL query and prints the result in the given stream.
	 * 
	 * @param queryStr		The ADQL query to process.
	 * @param out			The output stream.
	 * @param limit			The maximum number of returned records. 
	 * @param saadaQL		Indicates whether the given query is expressed in SaadaQL (true) or in ADQL (false).
	 * @throws Exception	If there is an error during the execution of the query or the building of the result file.
	 */
	public void processVOQuery(String queryStr, OutputStream out, int limit, boolean saadaQL) throws Exception {
		try {
			if( limit <= 0 || limit > 10000 ) {
				Messenger.printMsg(Messenger.WARNING, "Limit limited to 10000 for display");
				this.limit = 10000;
			}
			else {
				this.limit = limit;				
			}
			adqlQuery = !saadaQL;
			this.queryString = queryStr;
			this.writer = new PrintWriter(out, true);;
			if (adqlQuery){	    		
				// Execute the adql query:
				ADQLExecutor executor = new ADQLExecutor();
				result = executor.execute(queryStr, this.limit);
				this.query = executor.getQuery();

				// Fill only needed fields of queryInfos:
				queryInfos = new QueryInfos();
				//queryInfos.setSaadaqlQuery(queryStr);
			}else{
				// Execute the SaadaQL query:
				SaadaQLExecutor executor = new SaadaQLExecutor();
				result = executor.executeInStreaming(queryStr);

				// Fill only needed fields of queryInfos:
				queryInfos = new QueryInfos();
				queryInfos.setSaadaqlQuery(queryStr);
			}
			
			this.buildResultFile(result);
			result.close();
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			if (e instanceof ParseException)
				this.buildResultErrorFile("[ADQL PARSING] "+e.getMessage());
			else
				this.buildResultErrorFile(e.toString());
		} catch(Error e) {
			e.printStackTrace();
			this.buildResultErrorFile(e.toString());
		}
	}
	

	/* (non-Javadoc)
	 * @see saadadb.vo.formator.VOResultFormator#setDefaultReportFilename()
	 */
	public void setDefaultReportFilename() {
		this.result_filename = Repository.getVoreportsPath()
		+  Database.getSepar() 
		+ "SaadaQL_Result" + System.currentTimeMillis()
		+ ".json";	
	}

	@Override
	protected void writeProtocolParamDescription() {
	}

	@Override
	protected void writeDMData(SaadaInstance obj) throws Exception {
	}

	@SuppressWarnings("unchecked")
	protected void createResultErrorFile(String cause) throws Exception{
		JSONObject jsobject = new JSONObject();
		jsobject.put("errormsg", cause);
		writer.println(jsobject.toJSONString());
	}


	/* ********* */
	/* ADQL CASE */
	/* ********* */
	protected void createResultFile(SaadaQLResultSet rs) throws Exception{
		writeMetaData(rs);
		writeData(rs);
	}

	/**
	 * @see saadadb.vo.formator.VOResultFormator#writeMetaData(saadadb.query.result.SaadaQLResultSet)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void writeMetaData(SaadaQLResultSet rs) throws Exception {		
		if (rs != null){
			JSONArray ja = new JSONArray();
			// Create one field for each returned column:
			nbCol = 0;
			Iterator<AttributeHandler> it = rs.getMeta().iterator();
			String colname;
			while(it.hasNext()){
				JSONObject jso = new JSONObject();
				AttributeHandler meta = it.next();
				if (meta == null && query != null){
					if (query != null){
						ADQLOperand col = query.getColumn(nbCol);
						if (col.getAlias() != null){
							colname = col.getAlias();
						}else{
							if (col instanceof ADQLColumn)
								colname =  ((ADQLColumn)col).getColumn();
							else
								colname =  col.toString();
						}
					}else
						colname =  "???";
				}else {
					colname =  (new UTypeHandler(meta)).getNickname();
				}	
				if( "oidsaada".equals(colname)) {
					oidsaada = nbCol;
				}
				else if( "oidtable".equals(colname)) {
					oidtable = nbCol;
				}
				else if( "product_url_csa".equals(colname)) {
					product_url_csa = nbCol;
				}
				jso.put("sTitle", colname);				
				ja.add(jso);
				nbCol++;
			}
			writer.println("\"aoColumns\": ");
			writer.println(ja.toJSONString());
		}
	}


	/**
	 * @see saadadb.vo.QueryResultFormator#writeData(saadadb.query.SQLLikeResultSet)
	 */
	@SuppressWarnings("unchecked")
	protected void writeData(SaadaQLResultSet rs) throws Exception {
		JSONArray jrow = new JSONArray();

		writer.println("\"aaData\": [");
		if(rs != null) {
			int i = 0 ; 
			long start = System.currentTimeMillis() ;
			while(rs.next()) {
				jrow = new JSONArray();
				if (this.limit > 0 && i >= this.limit){
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
				/*
				 * SQLite (at least) becomes very slow with queries returning large result set. 
				 * It is better to set a TO and to return a truncated result than to reach the browser TO
				 * The pb s that the user is not advertised of the situation
				 */
				long delta;
				long TIMEOUT = 30000;
				if(  (delta = (System.currentTimeMillis() - start)) > TIMEOUT) {
					Messenger.printMsg(Messenger.WARNING, "Query stopped on time out (" + (delta/1000) + "\") at " + i + " match ");
					break;				
				}	

				for( int col=1 ; col<=nbCol ; col++) {
					Object val ;
					if( col == (oidsaada + 1) ) {
						val = DefaultPreviews.getDetailLink(rs.getLong(col), null);
					}
					else if( col == (oidtable + 1) ) {
						val = DefaultPreviews.getHeaderLink(rs.getLong(col));						
					}
					else if( col == (product_url_csa + 1) ) {
						val = DefaultPreviews.getDLLink(rs.getLong(oidsaada + 1), false);
					}
					else {
						val = rs.getObject(col);					
					}
					jrow.add(val);
				}
				if( i > 0 )
					writer.print(",");
				writer.println(jrow.toJSONString());
				i++;
			}
			writer.println("]");
		}
	}



	@Override
	public void dumpResultFile(OutputStream out) throws Exception {
		}

	@Override
	public String buildResultFile(SaadaQLResultSet rs) throws Exception {
		writer.println("{");
		writeMetaData(rs);
		writer.println(",");
		writeData(rs);
		writer.println("}");
		return null;
	}

	@Override
	protected String buildResultErrorFile(String cause) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String buildResultFile(long[] oids) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String buildMetadataFile() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createResultFile(long[] oids) throws Exception {
		// TODO Auto-generated method stub

	}



	@Override
	protected void writeDMData(long oid, SaadaQLResultSet rs) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeData(long[] oids) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeHouskeepingData(SaadaInstance obj)
	throws SaadaException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeNativeValues(SaadaInstance obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeExtReferences(SaadaInstance obj) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeExtMetaReferences(int category) throws QueryException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeHousekeepingFieldAndGroup() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeDMFieldsAndGroups(int length) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createMetadataResultFile() throws Exception {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		Database.init("XIDResult");
		TapToJsonFormator f;
		Messenger.debug_mode = true;
		try {
			f = new TapToJsonFormator();
			f.processVOQuery("SELECT * FROM WideFieldData_ENTRY;", System.out, 10, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
