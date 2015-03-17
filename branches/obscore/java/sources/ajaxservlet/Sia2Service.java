package ajaxservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.vo.request.SIAPRequest;

public class Sia2Service extends SaadaServlet {
	private static final long serialVersionUID = 2L; //original : 1L
	private static int count = 0;
      

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process( request,  response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process( request,  response);
	}

	private void process(HttpServletRequest req, HttpServletResponse res)  {
		try {
			printAccess(req, true);

			res.setContentType("text/xml");
			String reportename = "SIAP2_" + req.getSession().getId() + "_" + (count++);
			SIAPRequest request = new SIAPRequest(req.getSession().getId(), Database.getVOreportDir());
			request.addFormator("votable");
			request.setResponseFilePath(reportename);
			request.processRequest(getFlatParameterMap(req));
			this.dumpXmlFile(Database.getVOreportDir() + Database.getSepar() + reportename  + ".xml"
					, res
					, Database.getDbname() + "_SIA2.xml");
//			res.setContentType("text/xml");
//			String reportename = "SIAP_" + req.getSession().getId() + "_" + (count++);
//			SIAPRequest request = new SIAPRequest(req.getSession().getId(), Database.getVOreportDir());
//			request.addFormator("votable");
//			request.setResponseFilePath(reportename);
//			request.processRequest(getFlatParameterMap(req));
//			this.dumpXmlFile(Database.getVOreportDir() + Database.getSepar() + reportename  + ".xml"
//					, res
//					, Database.getDbname() + "_SIA.xml");

		} catch (Exception e) {
			this.getErrorPage(req, res, e);
		}
	}

}