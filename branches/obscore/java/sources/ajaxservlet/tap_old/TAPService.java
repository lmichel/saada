package ajaxservlet.tap_old;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import saadadb.database.Database;

/**
 * Redirect TAP service to Taphandle
 * @author michel
 * @version $Id$
 *
 */
public class TAPService extends SaadaServlet{


	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		process(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
		process(req, res);
	}

	private void process(HttpServletRequest req, HttpServletResponse response) {
		try {
			response.setStatus(301);
			response.setHeader("Location", "http://saada.unistra.fr/taphandle?url=" + java.net.URLEncoder.encode(Database.getUrl_root() + "/tap", "ISO-8859-1"));
			response.setHeader("Connection", "close");
		} catch(Exception e){
			this.getErrorPage(req, response, e);
		}
	}
}
