
package ajaxservlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.vo.tap.TapServiceManager;

/**
 * @author laurent
 * @version $Id: TAPTables.java 245 2012-02-21 17:52:11Z laurent.mistahl $
 */
public class TAPTables extends SaadaServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)  {
		try {
		printAccess(req, true);
		res.setContentType("text/xml");
		PrintWriter pw = res.getWriter();

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""+Database.getUrl_root()+"/styles/tables.xsl\"?>");
		pw.println(TapServiceManager.getXMLTables());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
		doGet(req, res);
	}
}
