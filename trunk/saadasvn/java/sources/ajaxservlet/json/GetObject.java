package ajaxservlet.json;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.api.SaadaLink;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import ajaxservlet.SaadaServlet;
import ajaxservlet.formator.DisplayFilter;
import ajaxservlet.formator.InstanceDisplayFilter;
import ajaxservlet.formator.LinkDisplayFilter;

/** * @version $Id$

 * Return the description of the instance having oid = oid
 * Or the counterpart if there is a relation parameter
 * Protocol
 */
public class GetObject extends SaadaServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}
	/**
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String soid = request.getParameter("oid");
		String relation = request.getParameter("relation");
		String target = request.getParameter("target");
		ServletOutputStream out = response.getOutputStream();   
		printAccess(request, false);
		try {
			long oid = Long.parseLong(soid);

			if( !JSON_FILE_MODE ) {
				if( "sources".equals(target) ){
					String collection = SaadaOID.getCollectionName(oid);
					String classe = Database.getCachemeta().getClass(SaadaOID.getClassName(oid)).getAssociate_class();
					RequestDispatcher rd = request.getRequestDispatcher("runquery");
					request.setAttribute("query", "Select ENTRY From " + classe + " In " 
							+ collection + " WhereAttributeSaada{ oidtable = " + oid + "}");
					request.setAttribute("treepath", collection + ".ENTRY." + classe);
					rd.forward(request, response); 
				}
				else if( relation == null || relation.length() == 0 ) {
					DisplayFilter colform  = new InstanceDisplayFilter(null);
					colform.setOId(oid);
					
					JSONObject jo = new JSONObject();
					jo.put("title",colform.getTitle());
					/*
					 * Collection level data
					 */					
					JSONArray  data = colform.getCollectionKWTable();
					JSONObject jolevel = new JSONObject();
					jolevel.put("iTotalRecords", data.size());
					jolevel.put("iTotalDisplayRecords", data.size());
					JSONArray colarray = new JSONArray();
					JSONObject jsloc = new JSONObject();
					jsloc.put("sTitle", "Keyword");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Value");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Unit");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Comment");
					colarray.add(jsloc);
					jolevel.put("aoColumns", colarray);
					jolevel.put("aaData", colform.getCollectionKWTable());
					jo.put("collectionlevel", jolevel);
					/*
					 * Class level data
					 */										
					data = colform.getClassKWTable();
					jolevel = new JSONObject();
					jolevel.put("iTotalRecords", data.size());
					jolevel.put("iTotalDisplayRecords", data.size());
					colarray = new JSONArray();
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Keyword");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Value");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Unit");
					colarray.add(jsloc);
					jsloc = new JSONObject();
					jsloc.put("sTitle", "Comment");
					colarray.add(jsloc);
					jolevel.put("aoColumns", colarray);
					jolevel.put("aaData", colform.getClassKWTable());
					jo.put("classlevel", jolevel);	
					/*
					 * Relationship references
					 */															
					colarray = new JSONArray();
					for(String r: Database.getCachemeta().getRelationNamesStartingFromColl(
									SaadaOID.getCollectionName(oid), SaadaOID.getCategoryNum(oid))){
						colarray.add(r)	;			
					}
					jo.put("relations", colarray);	
					/*
					 * Useful links
					 */															
					colarray = new JSONArray();					
					for( String link: colform.getLinks() ) {
						colarray.add(link);
					}
					jo.put("links", colarray);	
					JsonUtils.teePrint(out, jo.toJSONString());
					out.close();
				} else {
					DisplayFilter colform = new LinkDisplayFilter(relation, request);
					colform.setOId(oid);
					SaadaInstance  si = Database.getCache().getObject(oid);
					SaadaLink[] sls   = si.getStartingRelation(relation).getCounterparts(si.getOid(), true);

					JSONObject jo = new JSONObject();
					jo.put("relation", relation);
					jo.put("primary_oid", soid);

					JSONArray ja = new JSONArray();
					for( String c: colform.getDisplayedColumns() ) {
						JSONObject jl = new JSONObject();
						jl.put("sTitle", c);
						ja.add(jl);
					}
					jo.put("aoColumns", ja);

					ja = new JSONArray();
					int cpt=0;
					for( SaadaLink sl : sls ) {
						colform.setOId(sl.getEndindOID());
						JSONArray list = new JSONArray();
						if( cpt++ > 100 ) {
							list.add("truncated");
						} else {
							List<String> lr = colform.getRow(sl, -1);
							for( String r: lr) {
								list.add(r);
							}
						}
						ja.add(list);
					}
					jo.put("aaData",ja);					
					JsonUtils.teePrint(out,jo.toJSONString());		
					out.close();
				}
			}
			else {
				if( relation == null ) {
					request.setAttribute("file", "objectdetail");
				} else {
					request.setAttribute("file", "counterparts");				
				}
				RequestDispatcher rd;
				rd = request.getRequestDispatcher("getjsonarray");
				rd.forward(request, response);
			}
		} catch(Exception e) {
			reportJsonError(request, response,  e);
			return;
		}
	}
}
