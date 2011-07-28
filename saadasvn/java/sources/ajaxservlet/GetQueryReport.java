package ajaxservlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.vo.QueryFileReport;

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
			String sprotoc = req.getParameter("protocol");
			String datamodel = req.getParameter("datamodel");

			int protoc = QueryFileReport.NO_PROTOCOL;
			if( sprotoc != null ) {
				if( "sia".equalsIgnoreCase(sprotoc)) {
					protoc = QueryFileReport.SIA;
				}
				else if( "ssa".equalsIgnoreCase(sprotoc)) {
					protoc = QueryFileReport.SSA;
				}
				else if( "cs".equalsIgnoreCase(sprotoc) || "cone search".equalsIgnoreCase(sprotoc) || "conesearch".equalsIgnoreCase(sprotoc)) {
					protoc = QueryFileReport.CS;
				}
				else if( "auto".equalsIgnoreCase(sprotoc)) {
					protoc = QueryFileReport.AUTO;
				}
				else if( "noprotocol".equalsIgnoreCase(sprotoc)) {
					protoc = QueryFileReport.NO_PROTOCOL;
				}

			}
			if( query == null || query.length() == 0 ) {
				this.getErrorPage(req, res, " Missing query parameter");
			}
			else if( format == null || format.length() == 0 ) {
				this.getErrorPage(req, res, " Missing format parameter");
			}
			else {
				int limit ;
				try {
					limit = Integer.parseInt(slimit);
				} catch( Exception e) {	
					limit = 10000;
				}
				/*
				 * QueryFileReport executes the query again. That can be avoided by looking in the user session for 
				 * the oids matching that query.
				 */
				QueryFileReport qfr = new QueryFileReport(protoc, datamodel, query, format);
				qfr.getQueryReport(res, limit);
			}
		} catch (Exception e) {
			this.getErrorPage(req, res, e);
		}
}
}
