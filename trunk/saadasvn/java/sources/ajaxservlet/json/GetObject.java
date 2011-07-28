package ajaxservlet.json;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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

/**
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
		printAccess(request, true);
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
/*					
					
					JsonUtils.teePrint(out,"{");
					JsonUtils.teePrint(out,JsonUtils.getParam("title",colform.getTitle()) + ",");

					JsonUtils.teePrint(out,"  \"collectionlevel\" : {");
					List<String>  data = colform.getCollectionKWTable();
					JsonUtils.teePrint(out,JsonUtils.getParam("iTotalRecords", data.size(), "    ") + ",");
					JsonUtils.teePrint(out,JsonUtils.getParam("iTotalDisplayRecords", data.size(), "    ") + ",");
					JsonUtils.teePrint(out,"    \"aoColumns\": [");
					String comma = "";
					JsonUtils.teePrint(out,"    " + comma);
					JsonUtils.teePrint(out,"    {\"sTitle\": \"Keyword\"},");
					JsonUtils.teePrint(out,"    {\"sTitle\": \"Value\"},");
					JsonUtils.teePrint(out,"    {\"sTitle\": \"Value\"},");
					JsonUtils.teePrint(out,"    {\"sTitle\": \"Comment\"}");
					JsonUtils.teePrint(out,"    ],");
					JsonUtils.teePrint(out,"    \"aaData\": [");
					comma = "        ";
					for( String sr : data) {
						JsonUtils.teePrint(out, comma);
						JsonUtils.teePrint(out, "        " + sr);
						comma = "        ,";

					}
					JsonUtils.teePrint(out,"    ] },");

					JsonUtils.teePrint(out,"  \"classlevel\" : {");
					data = colform.getClassKWTable();
					JsonUtils.teePrint(out,JsonUtils.getParam("iTotalRecords", data.size(), "    ") + ",");
					JsonUtils.teePrint(out,JsonUtils.getParam("iTotalDisplayRecords", data.size(), "    ") + ",");
					JsonUtils.teePrint(out,"    \"aoColumns\": [");
					comma = "";
					JsonUtils.teePrint(out,"    " + comma);
					Set<String> cdd = colform.getDisplayedColumns();
					comma = "    ";
					for( String cd: cdd) {
						JsonUtils.teePrint(out, comma + "{\"sTitle\": \"" + cd + "\"}");
						comma = "        ,";
					}
					JsonUtils.teePrint(out,"    ],");
					JsonUtils.teePrint(out,"    \"aaData\": [");
					comma = "        ";
					for( String sr : data) {
						JsonUtils.teePrint(out, comma);
						JsonUtils.teePrint(out, "        " + sr);
						comma = "        ,";

					}
					JsonUtils.teePrint(out,"    ] },");

					JsonUtils.teePrint(out,"\"relations\": ");
					JsonUtils.teePrint(out
							,JsonUtils.getRow(Database.getCachemeta().getRelationNamesStartingFromColl(
									SaadaOID.getCollectionName(oid), SaadaOID.getCategoryNum(oid))));

					JsonUtils.teePrint(out,"     ,");
					JsonUtils.teePrint(out,"\"links\": ");
					JSONArray list = new JSONArray();
					for( String link: colform.getLinks() ) {
						list.add(link);
					}
					JsonUtils.teePrint(out,list.toString());

					JsonUtils.teePrint(out,"}");*/
				}
				else {
					DisplayFilter colform  = new LinkDisplayFilter(relation);
					colform.setOId(oid);
					SaadaInstance  si = Database.getCache().getObject(oid);
					SaadaLink[] sls   = si.getStartingLinks(relation);

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
						JSONArray list = new JSONArray();
						if( cpt++ > 100 ) {
							list.add("truncated");
						}
						else {
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


/*					JsonUtils.teePrint(out,"{");
					JsonUtils.teePrint(out,JsonUtils.getParam("relation", relation) + ",");
					JsonUtils.teePrint(out,JsonUtils.getParam("primary_oid", soid) + ",");
					JsonUtils.teePrint(out,"\"aoColumns\": [");
					String comma = "";
					for( String c: colform.getDisplayedColumns() ) {
						JsonUtils.teePrint(out, comma + "{\"sTitle\": \"" + c + "\"}");
						comma = ",";
					}
					JsonUtils.teePrint(out,"], ");

					JsonUtils.teePrint(out,"\"aaData\": [");
					int cpt=0;
					String coma = "";
					for( SaadaLink sl : sls ) {
						if( cpt++ > 100 ) {
							JsonUtils.teePrint(out,"[\"truncated\"]");					

						}
						List<String> lr = colform.getRow(sl);
						JSONArray list = new JSONArray();
						for( String r: lr) {
							list.add(r);
						}
						JsonUtils.teePrint(out, coma + list.toString() );
						coma = ",";
					}
					JsonUtils.teePrint(out,"]}");		
*/					out.close();
				}
			}
			else {
				if( relation == null ) {
					request.setAttribute("file", "objectdetail");
				}
				else {
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
