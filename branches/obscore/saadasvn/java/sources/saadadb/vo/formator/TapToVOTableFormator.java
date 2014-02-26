package saadadb.vo.formator;

import java.io.File;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Iterator;

import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vo.ADQLExecutor;
import saadadb.vo.SaadaQLExecutor;
import adqlParser.parser.ParseException;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLQuery;
import cds.savot.model.FieldSet;
import cds.savot.model.InfoSet;
import cds.savot.model.SavotField;
import cds.savot.model.SavotInfo;
import cds.savot.model.SavotTR;
import cds.savot.model.SavotTable;
import cds.savot.model.TDSet;
import cds.savot.writer.SavotWriter;

/**
 * Formats the result of an ADQL query in a VOTable document.
 * @version $Id$
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 10/05/2010
 *
 */
public class TapToVOTableFormator extends VOTableFormator {
	
	protected boolean adqlQuery;
	protected String queryString;
	
	protected ADQLQuery query;
	
	@Override
	protected void initWriter(File file) {
		writer = new SavotWriter();
		writer.enableAttributeEntities(false);
		writer.enableElementEntities(false);
		writer.setStyleSheet(Database.getUrl_root()+"/styles/votable.xsl");
		writer.initStream(file.getPath());
	}

	/**
	 * @throws SaadaException
	 */
	public TapToVOTableFormator() throws SaadaException {
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
    		this.limit = limit;
    		adqlQuery = !saadaQL;
    		this.queryString = queryStr;
    		
    		if (adqlQuery){	    		
				// Execute the adql query:
				ADQLExecutor executor = new ADQLExecutor();
				SaadaQLResultSet result = executor.execute(queryStr, this.limit);
				this.query = executor.getQuery();
				
				// Fill only needed fields of queryInfos:
				queryInfos = new QueryInfos();
				//queryInfos.setSaadaqlQuery(queryStr);
				
				// Build the result file in the VOTable format:
				this.buildResultFile(result);
				result.close();
    		}else{
    			// Execute the SaadaQL query:
				SaadaQLExecutor executor = new SaadaQLExecutor();
//				long[] oids = executor.execute(queryStr);
				SaadaQLResultSet result = executor.executeInStreaming(queryStr);
				
				// Get all selected columns (thanks to ADQLParser):
//				select = executor.getQuery().getSfiClause();
	//			result = executor.getQuery_result();
				
				// Fill only needed fields of queryInfos:
				queryInfos = new QueryInfos();
//				queryInfos.setCategory(select.getCatego());
				queryInfos.setSaadaqlQuery(queryStr);
				
				// Build the result file in the VOTable format:
//				this.buildResultFile(oids);
				this.buildResultFile(result);
    		}
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
    	
    	// Write the result in the wanted file:
    	this.dumpResultFile(out);
    }
    
	@Override
	protected void writeProtocolParamDescription() {
	}
	
	@Override
	protected void writeDMData(SaadaInstance obj) throws Exception {
	}
	
	protected void createResultErrorFile(String cause) throws Exception{
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("ERROR");
		info.setName("QUERY_STATUS");
		info.setContent("<![CDATA[" + cause + "]]>");
		infoSet.addItem(info);
		
		SavotInfo infoLanguage = new SavotInfo();
		infoLanguage.setName("LANGUAGE");
		infoLanguage.setContent(adqlQuery?"ADQL":"SaadaQL");
		infoSet.addItem(infoLanguage);
		
		SavotInfo infoQuery = new SavotInfo();
		infoQuery.setName("QUERY");
		infoQuery.setContent("<![CDATA["+queryString+"]]>");
		infoSet.addItem(infoQuery);
		
		writer.writeInfo(infoSet);
		
//		ParamSet paramSet = new ParamSet();
//		SavotParam sp = new SavotParam();
//		sp.setId("Error");
//		sp.setName("ERROR");
//		sp.setValue(cause.replaceAll("[<>]", "'"));
//		paramSet.addItem(sp);	
//		writer.writeParam(paramSet);
		writer.writeResourceEnd();
		writer.writeDocumentEnd();	
	}

    
/* ********* */
/* ADQL CASE */
/* ********* */
	protected void createResultFile(SaadaQLResultSet rs) throws Exception{
		this.allowsExtensions(this.queryInfos.getCategory()) ;
		if( limit <= 0 ) {
			this.limit = SaadaConstant.INT;
		}
		writeBeginingVOTable();
		
		InfoSet infoSet = new InfoSet();
		SavotInfo info = new SavotInfo();
		info.setValue("OK");
		info.setName("QUERY_STATUS");
		infoSet.addItem(info);
		
		SavotInfo infoLanguage = new SavotInfo();
		infoLanguage.setName("LANGUAGE");
		infoLanguage.setContent(adqlQuery?"ADQL":"SaadaQL");
		infoSet.addItem(infoLanguage);
		
		SavotInfo infoQuery = new SavotInfo();
		infoQuery.setName("QUERY");
		infoQuery.setContent("<![CDATA["+queryString+"]]>");
		infoSet.addItem(infoQuery);
		
		writer.writeInfo(infoSet);
		
		table = new SavotTable();
		table.setName("Results");
		table.setDescription(data_desc);
		if( this.queryInfos != null ) {
			writer.writeComment("Query parameters:\n " + this.queryInfos.toString());
		}
		else {
			writer.writeComment("Counterpart selection");
		}
		
		writer.writeTableBegin(table);
		writeMetaData(rs);
		writeData(rs);
		writer.writeTableEnd();
		writer.writeResourceEnd();
		/*
		 * Add tables with linked data
		 */
		this.writeExtensions(rs, this.queryInfos.getCategory());
		writer.writeDocumentEnd();
	}
	
	/**
	 * @see saadadb.vo.formator.VOResultFormator#writeMetaData(saadadb.query.result.SaadaQLResultSet)
	 */
	@Override
	protected void writeMetaData(SaadaQLResultSet rs) throws Exception {		
		fieldSet_dm = new FieldSet();
		
		try{
			if (rs != null){
			// Create one field for each returned column:
				int cpt = 100, indCol = 0;
				Iterator<AttributeHandler> it = rs.getMeta().iterator();
				while(it.hasNext()){
					AttributeHandler meta = it.next();
					SavotField f = null;
					if (meta == null && query != null){
						f = new SavotField();
						if (query != null){
							ADQLOperand col = query.getColumn(indCol);
							if (col.getAlias() != null){
								f.setName(col.getAlias());
								if (col instanceof ADQLColumn)
									f.setDescription("Column '"+((ADQLColumn)col).getColumn()+"'");
							}else{
								if (col instanceof ADQLColumn)
									f.setName(((ADQLColumn)col).getColumn());
								else
									f.setName(col.toString());								
							}
							f.setRef(f.getName()+"_"+cpt);
						}else
							f.setName("???");
						f.setDataType("char");
						f.setArraySize("*");
					}else
						f = (new UTypeHandler(meta)).getSavotField(cpt);
					
					fieldSet_dm.addItem(f);
					indCol++;
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		// write all these fields:
		writer.writeField(fieldSet_dm);
		
		this.writeHousekeepingFieldAndGroup();
		//this.writeMappedUtypeFieldAndGroup(oids);
		//this.writeAttExtendFieldsAndGroup(category);
//		this.writeExtMetaReferences(this.queryInfos.getCategory());
	}
	
//	/**
//	 * Create fields used by Aladin to put an URL within the data pane.
//	 * @param writer
//	 * @param category
//	 */
//	protected void writeSaadaLinksMetaReferences(String key_field, FieldSet fieldset) {
//		SavotField field = new SavotField();
//		
//		SavotLink link = new SavotLink();
//		LinkSet links = new LinkSet();
//		link.setHref(Database.getUrl_root() + "/getinstance?oid=${" + key_field + "}");
//		links.addItem(link);
//		
//		field.setLinks(links);
//		fieldset.addItem(field);
//	}
	
	/**
	 * @see saadadb.vo.QueryResultFormator#writeData(saadadb.query.SQLLikeResultSet)
	 */
	protected void writeData(SaadaQLResultSet rs) throws Exception {
		writer.writeDataBegin();
		writer.writeTableDataBegin();
		if(rs != null) {
			int i = 0 ; 
			long start = System.currentTimeMillis() ;
			while(rs.next()) {
				if (this.limit > 0 && i >= this.limit){
					Messenger.printMsg(Messenger.TRACE, "result truncated to i");
					break;
				}
				/*
				 * SQLite (at least) becomes ery slow with queries returning large result set. 
				 * It is better to set a TO and to return a truncated result than to reach the browser TO
				 * The pb s that the user is not advertised of the situation
				 */
				long delta;
				long TIMEOUT = 30000;
				if(  (delta = (System.currentTimeMillis() - start)) > TIMEOUT) {
					Messenger.printMsg(Messenger.WARNING, "Query stopped on time out (" + (delta/1000) + "\") at " + i + " match ");
					break;				
				}	
				SavotTR savotTR = new SavotTR();					
				tdSet = new TDSet(); 
				writeDMData(rs);
//				this.writeHouskeepingData(si);
				//this.writeMappedUtypeData(oid);
				//this.writeAttExtendData(oid);
//				this.writeExtReferences(si);
				savotTR.setTDs(tdSet);
				writer.writeTR(savotTR);
				i++;
			}
		}
		writer.writeTableDataEnd();
		writer.writeDataEnd();
	}

	protected void writeDMData(SaadaQLResultSet rs) throws Exception {
		// Write a TD element for each selected field:
		for( Object f: fieldSet_dm.getItems()) {
			SavotField sf = (SavotField)f;
			
			String colname = sf.getName();
			try {
				// gets its value:
				Object val = rs.getObject(colname);
				// write the value in a TD element:
				if (val != null){
					if( sf.getDataType().equals("char"))
						addCDataTD(val.toString());
					else
						addTD(val.toString());
				}else
					addTD("");
			}catch(SQLException ex){ 
				addTD("");
			}catch(Exception e) {
				throw e;
			}
		}
	}


/* ************ */
/* SAADAQL CASE */
/* ************ */
//	protected void createResultFile(long[] oids) throws Exception{
//		if( this.queryInfos != null ) {
//			this.allowsExtensions(this.queryInfos.getCategory()) ;
//		}
//		if( limit <= 0 ) {
//			this.limit = SaadaConstant.INT;
//		}
//		writeBeginingVOTable();
//		
//	// Write info elements:
//		InfoSet infoSet = new InfoSet();
//		
//		SavotInfo info = new SavotInfo();
//		info.setValue("OK");
//		info.setName("QUERY_STATUS");
//		infoSet.addItem(info);
//		
//		SavotInfo infoLanguage = new SavotInfo();
//		infoLanguage.setName("LANGUAGE");
//		infoLanguage.setContent(adqlQuery?"ADQL":"SaadaQL");
//		infoSet.addItem(infoLanguage);
//		
//		SavotInfo infoQuery = new SavotInfo();
//		infoQuery.setName("QUERY");
//		infoQuery.setContent("<![CDATA["+queryString+"]]>");
//		infoSet.addItem(infoQuery);
//
//		writer.writeInfo(infoSet);
//		
//	// Write the table:
//		table = new SavotTable();
//		table.setName("Results");
//		table.setDescription(data_desc);
//		if( this.queryInfos != null ) {
//			writer.writeComment("Query parameters:\n " + this.queryInfos.toString());
//		}
//		else {
//			writer.writeComment("Counterpart selection");
//		}
//		
//		writer.writeTableBegin(table);
//		writeMetaData(oids);
//		writeData(oids);
//		writer.writeTableEnd();
//		writer.writeResourceEnd();
//		/*
//		 * Add tables with linked data
//		 */
//		this.writeExtensions(oids, this.queryInfos.getCategory());
//		writer.writeDocumentEnd();
//	}
//
//	/**
//	 * Writes the VOTABLE element corresponding to the given SaadaInstance object and 
//	 * in function of all columns displayed in the FIELD elements of this VOTable document.
//	 *  
//	 * @param obj	The SaadaInstance object to write as a row of the votable.
//	 * 
//	 * @see saadadb.vo.formator.VOTableFormator#writeDMData(saadadb.collection.SaadaInstance)
//	 */
//	@Override
//	protected void writeDMData(SaadaInstance obj) throws Exception {
//		// Write a TD element for each selected field:
//		for( Object f: fieldSet_dm.getItems()) {
//			SavotField sf = (SavotField)f;
//			
//			String colname = sf.getName();
//			try {
//				// gets its value:
//				Object val = obj.getFieldValue(colname);
//				// write the value in a TD element:
//				if (val != null){
//					if( sf.getDataType().equals("char"))
//						addCDataTD(val.toString());
//					else
//						addTD(val.toString());
//				}else
//					addTD("");
//			}catch(NoSuchFieldException ex){ 
//				addTD("");
//			}catch(Exception e) {
//				throw e;
//			}
//		}
//	}
//	
//	/**
//	 * Writes all FIELD elements corresponding to the columns selected in the SELECT clause of the ADQL query.
//	 * @param length	NOT USED !
//	 * 
//	 * @see saadadb.vo.formator.VOTableFormator#writeDMFieldsAndGroups(int)
//	 */
//	@Override
//	protected void writeDMFieldsAndGroups(int length) {
//		if (!adqlQuery){
//			super.writeDMFieldsAndGroups(length);
//			return;
//		}
//		
//		fieldSet_dm = new FieldSet();
//		
//		try{
//		// Create one field for each column selected in the SELECT clause:
//			int cpt = 100;
//			Iterator<ADQLColumn> it = query.getColumns();
//			while(it.hasNext()){
//				ADQLColumn col = it.next();
//				AttributeHandler meta = col.getMeta();
//				SavotField f = null;
//				if (meta == null){
//					f = new SavotField();
//					if (col.getAlias() != null){
//						f.setName(col.getAlias());
//						f.setDescription("Column '"+col.getColumn()+"'");
//					}else
//						f.setName(col.getColumn());
//					f.setDataType("char");
//					f.setArraySize("*");
//					f.setRef(col.getColumn()+"_"+cpt);
//				}else
//					f = (new UTypeHandler(meta)).getSavotField(cpt);
//				
//				fieldSet_dm.addItem(f);					
//			}
//			
//			// write all these fields:
//			writer.writeField(fieldSet_dm);
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
//	}

	public static void main(String[] args) {
		Database.init("XIDResult");
		TapToVOTableFormator f;
		Messenger.debug_mode = true;
		try {
			f = new TapToVOTableFormator();
			f.processVOQuery("SELECT * FROM WideFieldData_ENTRY;", System.out, 10, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
