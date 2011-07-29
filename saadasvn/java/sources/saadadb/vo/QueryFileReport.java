package saadadb.vo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.executor.Query;
import saadadb.query.parser.SelectFromIn;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.vo.formator.ConeSearchToFITSFormator;
import saadadb.vo.formator.ConeSearchToVOTableFormator;
import saadadb.vo.formator.OtherAPToFITSFormator;
import saadadb.vo.formator.OtherAPToVoTableFormator;
import saadadb.vo.formator.OtherAPToZIPFormator;
import saadadb.vo.formator.SiaToVOTableFormator;
import saadadb.vo.formator.SsaToVOTableFormator;
import saadadb.vo.formator.VOResultFormator;
import saadadb.vo.translator.ConeSearchTranslator;
import saadadb.vo.translator.SiaTranslator;
import saadadb.vo.translator.SsaTranslator;
import saadadb.vo.translator.VOTranslator;

/** * @version $Id$

 * @author michel
 *
 */
public class QueryFileReport {
	private HttpServletRequest http_req= null;
	private LinkedHashMap<String, String> query_params= null;
	private VOTranslator translator= null;
	private VOResultFormator formator= null;
	private String mime_type= null;
	private String dl_default_fn= null;
	int searched_category;
	public static final int NO_PROTOCOL  = -1; // native data
	public static final int CS  = 0;
	public static final int SIA = 1;
	public static final int SSA = 2;
	public static final int AUTO = 3; // Select on of the previous protocols according to the Select clause
	
	/**
	 * Init the object from an HttpServletRequest
	 * @param access_protocol
	 * @param data_model Only used for VO protocols
	 * @param http_req
	 * @param format fits votable or metadata
	 * @throws Exception
	 */
	public QueryFileReport(int access_protocol, String data_model, HttpServletRequest http_req, String format) throws Exception {
		this.http_req = http_req;
		this.setSearchedCategory(access_protocol);
		this.setExecutorAndFormator(access_protocol, data_model, format );
	}
	
	public QueryFileReport(int access_protocol, String data_model, LinkedHashMap<String, String> query_params, String format) throws Exception {
		this.query_params = query_params;
		this.setSearchedCategory(access_protocol);
		this.setExecutorAndFormator(access_protocol, data_model, format );
	}
	
	public QueryFileReport(int access_protocol, String data_model, String query, String format) throws Exception {
		this.buildQueryParams(access_protocol, data_model, query);
		this.setExecutorAndFormator(access_protocol, data_model, format );
	}
	
	/**
	 * searched_category is used by the AUTO format to detect which protocol must be applied
	 * It has no effect for categories not handled by implemented VO protocols
	 * @param access_protocol
	 */
	private void setSearchedCategory(int access_protocol) {
		switch (access_protocol) {
		case QueryFileReport.SIA:
			searched_category = Category.IMAGE;
			break;
		case QueryFileReport.SSA:
			searched_category = Category.SPECTRUM;
			break;
		case QueryFileReport.CS:
			searched_category = Category.ENTRY;
			break;
		default:
			searched_category = -1;
			break;
		}
	}
	/**
	 * @param access_protocol
	 * @param query
	 * @throws SaadaException
	 */
	private void buildQueryParams(int access_protocol, String data_model, String query) throws Exception {
		Query query_proc = new Query(query);	
		if( data_model != null && data_model.length() > 0 ) {
			query_proc.setDM(data_model);
		}
		query_proc.parse();
		SelectFromIn sfi_clause = query_proc.getSfiClause();
		query_params = new LinkedHashMap<String, String>();
		query_params.put("query", query);
		query_params.put("collection", sfi_clause.getVOResourceName());
		searched_category = sfi_clause.getCatego();
		if( access_protocol == QueryFileReport.SIA && sfi_clause.getCatego() != Category.IMAGE ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not use this SIAP protocol with " + Category.explain( sfi_clause.getCatego()));
		}
		else if( access_protocol == QueryFileReport.SSA && sfi_clause.getCatego() != Category.SPECTRUM ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not use SSAP protocol with " + Category.explain( sfi_clause.getCatego()));
		}
		else if( access_protocol == QueryFileReport.CS && sfi_clause.getCatego() != Category.ENTRY ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Can not use CS protocol with " + Category.explain( sfi_clause.getCatego()));
		}
	}
	
	/**
	 * @param access_protocol
	 * @param data_model
	 * @param format
	 * @throws Exception
	 */
	private void setExecutorAndFormator(int access_protocol, String data_model, String format ) throws Exception{
		String dm = data_model;
		long time_stamp = (new Date()).getTime();
		if( format == null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No format specifier, take votable");
			format = "votable";
		}
		if( access_protocol == QueryFileReport.SIA || (access_protocol == QueryFileReport.AUTO && searched_category == Category.IMAGE)) {
			if( "native".equalsIgnoreCase(dm)) dm = "native image";
			if( http_req != null ) {
				this.translator = new SiaTranslator(http_req);
			}
			else {
				this.translator = new SiaTranslator(query_params);
			}
			if( format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("xml") || format.equalsIgnoreCase("metadata")) {
				this.formator = new SiaToVOTableFormator(dm);
				formator.setQueryInfos(translator.queryInfos);
				//mime_type = "application/xml-external-parsed-entity";
				mime_type = "text/xml";
				dl_default_fn = Database.getName() + "_siapselection_" + time_stamp + ".xml";
			}
			else {
				IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Image queries ca not return " + format + " files");
			}
		}
		else if( access_protocol == QueryFileReport.SSA || (access_protocol == QueryFileReport.AUTO && searched_category == Category.SPECTRUM)) {
			if( "native".equalsIgnoreCase(dm)) dm = "native spectrum";
			if( http_req != null ) {
				this.translator = new SsaTranslator(http_req);
			}
			else {
				this.translator = new SsaTranslator(query_params);
			}
			if( format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("xml") || format.equalsIgnoreCase("metadata")) {
				this.formator = new SsaToVOTableFormator(dm);
				formator.setQueryInfos(translator.queryInfos);
				//mime_type = "application/xml-external-parsed-entity";
				mime_type = "text/xml";
				dl_default_fn = Database.getName() + "_ssapselection_" + time_stamp + ".xml";
			}
			else {
				IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Spectrum queries ca not return " + format + " files");
			}
		}
		else if( access_protocol == QueryFileReport.CS || (access_protocol == QueryFileReport.AUTO && searched_category == Category.ENTRY)) {
			if( "native".equalsIgnoreCase(dm)) dm = "native entry";
			if( http_req != null ) {
				this.translator = new ConeSearchTranslator(http_req);
			}
			else {
				this.translator = new ConeSearchTranslator(query_params);
			}
			if( format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("xml") || format.equalsIgnoreCase("metadata")) {
				this.formator = new ConeSearchToVOTableFormator(dm);
				formator.setQueryInfos(translator.queryInfos);
				//mime_type = "application/xml-external-parsed-entity";
				mime_type = "text/xml";
				dl_default_fn = Database.getName() + "_csselection_" + time_stamp + ".xml";
			}
			else if( format.equalsIgnoreCase("fits") ) {
				this.formator = new ConeSearchToFITSFormator(dm);
				mime_type = "application/fits";
				dl_default_fn = Database.getName() + "_csselection_" + time_stamp + ".fits";
			}
			else {
				IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, Category.explain(searched_category) + " queries can not return " + format + " files");
			}
		}
		else {
			this.translator = null;
			if( format.equalsIgnoreCase("votable") || format.equalsIgnoreCase("xml") ) {
				this.formator = new OtherAPToVoTableFormator(searched_category, dm);
				//mime_type = "application/xml-external-parsed-entity";
				mime_type = "text/xml";
				dl_default_fn = Database.getName() + "_selection_" + time_stamp + ".xml";
			}
			else if( format.equalsIgnoreCase("fits")  ) {
				this.formator = new OtherAPToFITSFormator(searched_category, dm);
				mime_type = "application/fits";
				dl_default_fn = Database.getName() + "_selection_" + time_stamp + ".fits";
			}
			else if( format.equalsIgnoreCase("zipball")  ) {
				this.formator = new OtherAPToZIPFormator(searched_category, dm);
				mime_type = "application/zip";
				dl_default_fn = Database.getName() + "_selection_" + time_stamp + ".zip";
			}
			else {
				IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Queries on native data can not return " + format + " files");
			}
		}
		if(format.equalsIgnoreCase("metadata") && translator != null ) {
			translator.setMetadataRequired(true);
		}
	}

	/**
	 * @param writer
	 * @param limit
	 * @throws Exception
	 */
	public void getQueryReport(OutputStream writer, int limit) throws Exception{ 
		formator.setLimit(limit);
		if( translator != null ) {
			formator.processVOQueryInStreaming(translator, writer);
		}
		else {
			formator.processVOQueryInStreaming(query_params.get("query") , writer);			
		}
		SQLTable.dropTmpTables();
	}
	
	/**
	 * @param http_response
	 * @param limit
	 * @throws Exception
	 */
	public void getQueryReport(HttpServletResponse http_response, int limit) throws Exception{ 
		formator.setLimit(limit);
		formator.setResult_filename(Repository.getVoreportsPath() + Database.getSepar() + dl_default_fn);
		http_response.setContentType(this.mime_type); 	
		http_response.setHeader("Content-Disposition", "attachment; filename=\"" + dl_default_fn + "\"");

		if( translator != null ) {
			//Streaming mode might run joins between class and collection
			//formator.processVOQueryInStreaming(translator, http_response.getOutputStream());
			formator.processVOQuery(translator, http_response.getOutputStream());
		}
		else {
			formator.processVOQueryInStreaming(query_params.get("query") , http_response.getOutputStream());			
		}
		SQLTable.dropTmpTables();
	}
	
	/**
	 * @param filename
	 * @param limit
	 * @throws Exception
	 */
	public void getQueryReport(String filename, int limit) throws Exception{ 
		formator.setLimit(limit);
		formator.setResult_filename(filename);
		if( translator != null ) {
			//Streaming mode might run joins between class and collection
			//formator.processVOQueryInStreaming(translator);
			formator.processVOQuery(translator);
		}
		else {
			formator.processVOQueryInStreaming(query_params.get("query"));			
		}
		SQLTable.dropTmpTables();
	}
	
	public static void main(String[] args) {
		try {
//			ArgsParser ap = new ArgsParser(args);
			//Messenger.debug_mode = false;
			Database.init("Napoli");
			// http://saada:8888/XIDResult/getqueryreport?query=Select+ENTRY+From+*+In+SpectroscopicSample%0AWhereAttributeSaada+%7B%28E_MAG++is+not+null%29%7D&format=zipball&datamodel=XIDSrcModel&limit=100
				
				
				String query = "Select ENTRY  From CatalogueEntry In CATALOGUE WhereAttributeSaada{ _sum_flag = 0				} WhereRelation{   matchPattern{CatSrcToArchSrc,      Cardinality = 0,    AssObjClass{arch_0467TEntry}}  matchPattern{ObjClass,  Qualifier{proba_star > 0.9 },   Qualifier{sample = 1 }}}" ;
			    query = "Select ENTRY  From CatalogueEntry In CATALOGUE Limit 10";
//			String dm    = "XIDSrcModel";
//			String fmt   = "fits";
//			QueryFileReport qfr = new QueryFileReport(QueryFileReport.NO_PROTOCOL, dm, query,fmt);
////			qfr.getQueryReport("/tmpx/xcs.vot", 10);
			BufferedReader br;// = new BufferedReader(new FileReader("/tmp/xcs.vot"));
//			String str;
//			while( (str = br.readLine()) != null  ) {
//				System.out.println(str.replaceAll("<TD", "\n<TD"));
//			}
//			br.close();
//			LinkedHashMap<String, String> query_params = new LinkedHashMap<String, String>();
//			query_params.put("pos", "M33");
//			query_params.put("size", "0.7");
//			query_params.put("collection", "*");
//			QueryFileReport qfr = new QueryFileReport(QueryFileReport.SIA, null, query_params, "votable");
//			qfr.getQueryReport(new PrintWriter(System.out, true), 2);
//			QueryFileReport qfr = new QueryFileReport(QueryFileReport.NO_PROTOCOL, "LittleCharac", "Select IMAGE From * In GOODS", "votable");
//			qfr.getQueryReport("/tmp/xcs.vot", 212);
//			BufferedReader br = new BufferedReader(new FileReader("/tmp/xcs.vot"));
			String str;
//			while( (str = br.readLine()) != null  ) {
//				System.out.println(str.replaceAll("<TD", "\n<TD"));
//			}
//			br.close();
			query = "Select ENTRY From * In XMMNewton Limit 10";
			QueryFileReport qfr = new QueryFileReport(QueryFileReport.CS, null, query, "votable");
			qfr.getQueryReport("/Users/laurentmichel/Desktop/xcs.vot", 212);
			br = new BufferedReader(new FileReader("/Users/laurentmichel/Desktop/xcs.vot"));
			while( (str = br.readLine()) != null  ) {
				System.out.println(str.replaceAll("<TD", "\n<TD"));
			}
			br.close();
				
		} catch(Exception e) {
			Messenger.printStackTrace(e);
		}
	}
}
