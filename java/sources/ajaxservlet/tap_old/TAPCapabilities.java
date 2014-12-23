
package ajaxservlet.tap_old;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import saadadb.database.Database;

/**
 * @author laurent
 * @version $Id$
 *
 */
public class TAPCapabilities extends SaadaServlet {

	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String url = new String(req.getRequestURL());
		String resourceName, paramValue;

		res.setContentType("text/xml");
		
		PrintWriter pw = res.getWriter();

		Enumeration<?> enumParam = this.getInitParameterNames();
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""+Database.getUrl_root()+"/styles/capabilities.xsl\"?>");
		pw.println("<vosi:capabilities xmlns:vosi=\"http://www.ivoa.net/xml/VOSICapabilities/v1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">");
        while (enumParam.hasMoreElements()){
            resourceName = enumParam.nextElement().toString();
            paramValue = this.getInitParameter(resourceName);
    		pw.println("<capability standardID=\""+paramValue+"\">");
    		pw.println("<interface xsi:type=\"vod:ParamHTTP\">");
    		pw.println("<accessURL use=\"full\">"+url.substring(0, url.lastIndexOf("/")+((resourceName == null || resourceName.trim().length() == 0)?0:1))+resourceName+"</accessURL>");
			pw.println("</interface>");
    		pw.println("</capability>");
        }
		pw.println("</vosi:capabilities>");
	}

	
}
