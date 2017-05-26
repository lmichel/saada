package ajaxservlet;

import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.meta.MetaRelation;
import saadadb.query.result.OidsaadaResultSet;
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
			//if( si.getVignetteName() != null )
			response.setContentType("text/xml");
			Writer w = response.getWriter();
			w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");			
			w.write("<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\">\n");
			w.write("  <DESCRIPTION>\n");
			w.write("  SaadaDB:\n");		
			w.write("   name : " + Database.getDbname()+ "\n");		
			w.write("   url  : " + Database.getUrl_root() + "\n");		
			w.write("   date : " + (new Date()) + "\n");		
			w.write("   Query: " + Database.getUrl_root() + "/datalink?oid= " + oid +"\n");
			w.write("  </DESCRIPTION>\n");
			w.write("  <COOSYS ID=\"" + Database.getCoord_sys() + "\" system=\"" + Database.getCoord_sys() + "\"/>\n");
			w.write("  <RESOURCE type=\"results\"><DESCRIPTION>Datalink 0.0</DESCRIPTION>\n");
			w.write("    <INFO name=\"QUERY_STATUS\" value=\"OK\"/>\n");
			w.write("    <INFO name=\"LANGUAGE\" value=\"DataLink\"/>\n");
			w.write("    <INFO name=\"QUERY\">datalink?oid= " + oid +"</INFO>\n");
			w.write("    <TABLE name=\"Results\">\n");
			w.write("      <FIELD name=\"uri\" datatype=\"char\" ucd=\"meta.id\" utype=\"datalink:Datalink.uri\" xtype=\"w3c:URI\" arraysize=\"*\" />");
			w.write("      <FIELD name=\"accessURL\" datatype=\"char\" utype=\"datalink:Datalink.accessURL\" xtype=\"w3c:URL\" arraysize=\"*\" />");
			w.write("      <FIELD name=\"semantics\" datatype=\"char\" utype=\"datalink:Datalink.semantics\" xtype=\"w3c:URI\" arraysize=\"*\" />");
			w.write("      <FIELD name=\"productType\" datatype=\"char\" utype=\"caom:Artifact.productType\" arraysize=\"*\" />");
			w.write("      <FIELD name=\"contentType\" datatype=\"char\" utype=\"caom:Artifact.contentType\" arraysize=\"*\" />");
			w.write("      <FIELD name=\"contentLength\" datatype=\"long\" unit=\"byte\" utype=\"caom:Artifact.contentLength\" />");
			w.write("      <FIELD name=\"errorMessage\" datatype=\"char\" utype=\"datalink:Datalink.error\" arraysize=\"*\" />");

//			w.write("      <PARAM name=\"oidsaada\" datatype=\"long\"  value=\"" + oid + "\"/>\n");
//			w.write("      <FIELD  name=\"url\" datatype=\"char\"  arraysize=\"*\"/>\n");
//			w.write("      <FIELD  name=\"category\" datatype=\"char\"  arraysize=\"*\"/>\n");
//			w.write("      <FIELD  name=\"collection\" datatype=\"char\"  arraysize=\"*\"/>\n");
//			w.write("      <FIELD  name=\"relation\" datatype=\"char\"  arraysize=\"*\"/>\n");
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
				for(SaadaLink sl: links) {
					w.write("          <TR>");

					long cpoid = sl.getEndindOID();
					SaadaInstance cpsi = Database.getCache().getObject(cpoid);
					w.write("            <TD>" + cpoid+ "</TD>");
					String lnk;
					if( SaadaServlet.secureDownlad ) {
						lnk =  cpsi.getSecureDownloadURL(true);	
					} else {
						lnk =cpsi.getDownloadURL(true);	
					}
					w.write("            <TD>" + lnk + "</TD>");
					w.write("            <TD>Data Linked by the relationship " + rel+ "</TD>");
					w.write("            <TD>"  + SaadaOID.getCategoryName(cpoid) + "</TD>");
					String contentLength = "";
					String contentType = "";
					try {
						URLConnection conn = (new URL(cpsi.getDownloadURL(true))).openConnection(); 
						Map<String, List<String>>  map = conn.getHeaderFields();
						for( Entry<String, List<String>> s: map.entrySet()) {
							String k = s.getKey();
							k = (k != null)?k.replaceAll("-", ""): "nokey";
							String value = s.getValue().get(0);
							if( k.equalsIgnoreCase("contentType")) {
								contentType = value;
							} else if( k.equalsIgnoreCase("contentLength")) {
								contentLength = value;
							}
						}
					}
					catch (Exception e) {}

					w.write("            <TD>" + contentType + "</TD>");
					w.write("            <TD>" + contentLength + "</TD>");
					w.write("            <TD>Product name: " + cpsi.getNameSaada() + "</TD>");
					w.write("          </TR>\n");
				}
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
