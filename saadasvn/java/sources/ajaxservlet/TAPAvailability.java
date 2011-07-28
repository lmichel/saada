
package ajaxservlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;

public class TAPAvailability extends SaadaServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("text/xml");
		
		PrintWriter pw = res.getWriter();

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""+Database.getUrl_root()+"/styles/uws.xsl\"?>");
		pw.println("<vosi:availability xmlns:vosi=\"http://www.ivoa.net/xml/Availability/v0.4\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
        try {
        	if (Database.getDbname() != null && Database.getDbname().trim().length() > 0)
        		if (!Database.get_connection().isClosed())
        			pw.println("\t<vosi:available>true</vosi:available>\n\t<vosi:note>Service is accepting queries</vosi:note>\n");
        		else
        			pw.println("\t<vosi:available>false</vosi:available>\n\t<vosi:note>The database \""+Database.getDbname()+"\" is not accessible</vosi:note>\n");
        	else
    			pw.println("\t<vosi:available>false</vosi:available>\n\t<vosi:note>No database access because no database name is specified</vosi:note>\n");
		} catch (Exception e) {
			pw.println("\t<vosi:available>false</vosi:available>\n\t<vosi:note>"+e.getClass().getName()+": "+e.getMessage()+"</vosi:note>\n");
		}
		pw.println("</vosi:availability>");
	}

	
}
