package ajaxservlet;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.accounting.UserTrap;

import saadadb.vo.QueryFileReport;
import saadadb.vo.request.SaadaqlRequest;

/**
 * Servlet implementation class GetQueryReport
 * @version $Id$
 */
public class GetQueryReport extends SaadaServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. 
	 */
	public GetQueryReport() {
	}

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

	private void process(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		printAccess(req, true);
		try {
			String query = req.getParameter("query");
			String format = req.getParameter("format");
			String slimit = req.getParameter("limit");
			String model = req.getParameter("model");

			int limit  = 10000;

			if( query == null || query.length() == 0 ) {
				this.getErrorPage(req, res, " Missing query parameter");
			}
			else if( format == null || format.length() == 0 ) {
				this.getErrorPage(req, res, " Missing format parameter");
			} else {
				try {
					limit = Integer.parseInt(slimit);
				} catch( Exception e) {	
					limit = 10000;
				}
			}

			Map<String, String> pmap = new LinkedHashMap<String, String>();
			pmap.put("query", query);
			pmap.put("limit", Integer.toString(limit));
			if( model != null ){
				pmap.put("model", model);
			}
			String dir = UserTrap.getUserAccount(req).getReportDir();
			String fn = "Saadaql";
			SaadaqlRequest request = new SaadaqlRequest( UserTrap.getUserAccount(req).getSessionID(), dir);
			request.addFormator((format != null)? format: "votable");
			request.setResponseFilePath(fn);
			request.processRequest(pmap);		
			System.out.println("============================");
			res.setHeader("Access-Control-Allow-Origin", "*");

			downloadProduct(req, res, request.getResponseFilePath()[0]);

		} catch (Exception e) {
			this.getErrorPage(req, res, e);
		}
	}
}
