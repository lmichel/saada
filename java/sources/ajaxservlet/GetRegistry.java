package ajaxservlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.vo.BuildRegistry;

/**
 * Servlet implementation class GetRegistry
  * @version $Id: GetRegistry.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 * 
 * Return either a registry template or a glu mark for the resources described by the parameters
 */
public class GetRegistry extends SaadaServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. 
	 */
	public GetRegistry() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, true);
		String get   = request.getParameter("get");
		String type  = request.getParameter("type");
		String coll  = request.getParameter("coll");
		String dm    = request.getParameter("datamodel");
		String intersect  = request.getParameter("intersect");
		String mode       = request.getParameter("mode");
		String s_withrel  = request.getParameter("withrel");
		boolean withrel = false;
		/*
		 * Log hit: what URL from where and when
		 */
		String full_url = request.getRequestURL().toString();
		String queryString = request.getQueryString(); 

		if (queryString != null) {
			full_url += "?"+queryString;
		}		

		PrintWriter out = response.getWriter();

		if( s_withrel != null && s_withrel.equals("true") ) {
			withrel = true;
		}
		response.setContentType("text/plain");
		try {
			if (coll == null || coll.equals("")) {
				getErrorPage(request, response, "ERROR:No \"col\" parameter given");
			}
			else if (get == null || get.equals("")) {
				getErrorPage(request, response, "ERROR:No \"get\" parameter given");
			}
			else if (type == null || type.equals("")) {
				getErrorPage(request, response, "ERROR: No \"type\" parameter given (registry or glu)");
			}
			else if( get.equals("CS") || get.equals("cs") ){
				if( type.equalsIgnoreCase("registry") ) {
					out.print(BuildRegistry.buildConeSearchRegistry(coll, dm, withrel));
				}
				else {
					out.print(BuildRegistry.buildConeSearchGlumark(coll, dm, withrel));					
				}
			}
			else if( get.equals("SIA") || get.equals("sia") ){
				if( type.equalsIgnoreCase("registry") ) {
					out.print(BuildRegistry.buildSIARegistry(coll, dm, intersect, mode, withrel));
				}
				else {
					out.print(BuildRegistry.buildSIAGlumark(coll, dm, intersect, mode, withrel));					
				}
			}
			else if( get.equals("SSA") || get.equals("ssa") ){
				out.print(BuildRegistry.buildSSARegistry(coll, dm, withrel));
			}
			else if( get.equals("VOQL") || get.equals("voql") ){
				out.print(BuildRegistry.buildSkynodeRegistry(coll));
			}
			else {
				getErrorPage(request, response, "ERROR: \"get\" parameter \"" + get + "\" not understood");
			}
		} catch(Exception e) {
			getErrorPage(request, response,e);
		}
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
