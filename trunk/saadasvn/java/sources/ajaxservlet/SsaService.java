package ajaxservlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.vo.request.SSAPRequest;

/**
 * Endpoint of SSA services
 * @author michel
 * @version $Id$
 *
 * 07/2011 switch to the new SSA request processing
 */
public class SsaService extends SaadaServlet {
	private static int count = 0;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/* (non-Javadoc)
	 * @see servlet.SaadaServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)  {
		try {
			printAccess(req, true);
			res.setContentType("text/xml");
			String reportename = "SSAP_" + req.getSession().getId() + "_" + (count++);
			SSAPRequest request = new SSAPRequest(req.getSession(true).getId(), Database.getVOreportDir());
			request.addFormator("votable");
			request.setResponseFilePath(reportename);
			request.processRequest(getFlatParameterMap(req));
			this.dumpXmlFile(Database.getVOreportDir() + Database.getSepar() + reportename  + ".xml", res, Database.getDbname() + "_SSA.xml" );

		} catch (Exception e) {
			this.getErrorPage(req, res, e);
		}
	}

	
}
