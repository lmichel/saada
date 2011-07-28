
package ajaxservlet;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;

/**
 * @author laurent
 * @version $Id$
 */
public class TAPTables extends SaadaServlet {

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest req, HttpServletResponse res)  {
		try {
		printAccess(req, true);
		res.setContentType("text/xml");
System.out.println("1");
		PrintWriter pw = res.getWriter();
		System.out.println("1");

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<?xml-stylesheet type=\"text/xsl\" href=\""+Database.getUrl_root()+"/styles/tables.xsl\"?>");
		System.out.println("1");

		pw.println(Database.getCachemeta().getTables());
		System.out.println("1");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
		doGet(req, res);
	}
}
