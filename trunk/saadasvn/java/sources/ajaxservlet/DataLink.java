package ajaxservlet;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ajaxservlet.json.JsonUtils;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.meta.MetaRelation;

import netscape.javascript.JSObject;

/**
 * Servlet implementation class DataLink
 * This service is a prototype for the future VO datalink service
 * It returns a JSON message (see below) with all products (no ENTRY)
 * linked to the oid receieved as parameter
 * 
 * { oid: oid,
 *   columns: ["relation", "collection"  , "category", "url"],
 *   data [[relation, collection  , category, url],
 *         [relation, collection  , category, url],
 *          ....]}
 * 
 */
public class DataLink extends SaadaServlet implements Servlet {
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

	@SuppressWarnings("unchecked")
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String soid = request.getParameter("oid");
		ServletOutputStream out = response.getOutputStream();   
		printAccess(request, false);
		try {
			long oid = Long.parseLong(soid);
			JSONObject title;
			JSONObject retour = new JSONObject();
			JSONArray cols = new JSONArray();
			retour.put("oid", oid);
			retour.put("columns", cols);
			String[] colnames = {"url",  "category", "collection", "relation" };
			for( String colname: colnames) {
				title = new JSONObject(); 
				title.put("sTitle", colname);
				cols.add(0, title);
			}
			SaadaInstance si = Database.getCache().getObject(oid);
			String[] rels = si.getStartingRelationNames();
			JSONArray data = new JSONArray();
			for( String rel: rels ) {
				System.out.println(rel);
				MetaRelation mr = Database.getCachemeta().getRelation(rel);
				/*
				 * Links toward catalogues entries are out of the scope of the
				 * datalinks service
				 */
				if( mr.getSecondary_category() == Category.ENTRY) {
					continue;
				}
				System.out.println("OK");
				SaadaLink[] links = si.getStartingLinks(rel);
				String collection = mr.getSecondary_coll();
				String category = Category.explain(mr.getSecondary_category());
				/*
				 * Links are serialized: one row for each 
				 */
				for(SaadaLink sl: links) {
					System.out.println("OK2");
					long cpoid = sl.getEndindOID();
					JSONArray row = new JSONArray();
					row.add(0, rel);
					row.add(1, collection);
					row.add(2, category);
					if( SaadaServlet.secureDownlad ) {
						row.add(3, Database.getCache().getObject(cpoid).getSecureDownloadURL(true));	
					} else {
						row.add(3, Database.getCache().getObject(cpoid).getDownloadURL(true));	
					}
					data.add(row);
				}
			}
			retour.put("data", data);
			response.setContentType("application/json");
			JsonUtils.teePrint(out, retour.toJSONString());
			
			
		} catch (Exception e) {
			reportJsonError(request, response,  e);
			return;
		}

	}

}
