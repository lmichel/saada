package ajaxservlet;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.api.SaadaLink;
import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.meta.MetaRelation;
import ajaxservlet.json.JsonUtils;


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
		printAccess(request, false);
		try {
			long oid = Long.parseLong(soid);
			SaadaInstance si = Database.getCache().getObject(oid);
			if( si.getVignetteName() != null )
			response.setContentType("text/xml");
			Writer w = response.getWriter();
			w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");			
			w.write("<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\">\n");
			w.write("  <DESCRIPTION>\n");
			w.write("  SaadaDB:\n");		
			w.write("   name : " + Database.getDbname()+ "\n");		
			w.write("   url  : " + Database.getUrl_root() + "\n");		
			w.write("   date : " + (new Date()) + "\n");		
			w.write(  "Query: datalink?oid= " + oid +"\n");
			w.write("  </DESCRIPTION>\n");
			w.write("  <COOSYS ID=\"" + Database.getCoord_sys() + "\" system=\"" + Database.getCoord_sys() + "\"/>\n");
			w.write("  <RESOURCE type=\"results\"><DESCRIPTION>Datalink 0.0</DESCRIPTION>\n");
			w.write("    <INFO name=\"QUERY_STATUS\" value=\"OK\"/>\n");
			w.write("    <INFO name=\"LANGUAGE\" value=\"DataLink\"/>\n");
			w.write("    <INFO name=\"QUERY\">datalink?oid= " + oid +"</INFO>\n");
			w.write("    <TABLE name=\"Results\">\n");
			w.write("      <PARAM name=\"oidsaada\" datatype=\"long\"  value=\"" + oid + "\"/>\n");
			w.write("      <FIELD  name=\"url\" datatype=\"char\"  arraysize=\"*\"/>\n");
			w.write("      <FIELD  name=\"category\" datatype=\"char\"  arraysize=\"*\"/>\n");
			w.write("      <FIELD  name=\"collection\" datatype=\"char\"  arraysize=\"*\"/>\n");
			w.write("      <FIELD  name=\"relation\" datatype=\"char\"  arraysize=\"*\"/>\n");
			w.write("      <DATA>\n");
			w.write("        <TABLEDATA>\n");
			String[] rels = si.getStartingRelationNames();
			for( String rel: rels ) {
				MetaRelation mr = Database.getCachemeta().getRelation(rel);
				/*
				 * Links toward catalogues entries are out of the scope of the
				 * datalinks service
				 */
				if( mr.getSecondary_category() == Category.ENTRY) {
					continue;
				}
				SaadaLink[] links = si.getStartingLinks(rel);
				String collection = mr.getSecondary_coll();
				String category = Category.explain(mr.getSecondary_category());
				/*
				 * Links are serialized: one row for each 
				 */
				w.write("          <TR>");
				for(SaadaLink sl: links) {
					long cpoid = sl.getEndindOID();
					JSONArray row = new JSONArray();
					w.write("<TD>" + rel+ "</TD>");
					w.write("<TD>" + collection+ "</TD>");
					w.write("<TD>" + category+ "</TD>");
					row.add(0, rel);
					row.add(1, collection);
					row.add(2, category);
					String lnk;
					if( SaadaServlet.secureDownlad ) {
						lnk =  Database.getCache().getObject(cpoid).getSecureDownloadURL(true);	
					} else {
						lnk = Database.getCache().getObject(cpoid).getDownloadURL(true);	
					}
					w.write("<TD>" + lnk+ "</TD>");
				}
				w.write("          </TR>\n");
			}			
			w.write("        </TABLEDATA>\n");
			w.write("      </DATA>\n");
			w.write("    </TABLE>\n");
			w.write("  </RESOURCE>\n");
			w.write("</VOTABLE>\n");

			
		} catch (Exception e) {
			reportJsonError(request, response,  e);
			return;
		}

	}

}
