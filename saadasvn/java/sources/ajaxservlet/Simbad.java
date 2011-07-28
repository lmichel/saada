package ajaxservlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Simbad
 * Simple proxy for Simbad pages
 * 05/2011: use of sendRedirect
 */
public class Simbad extends SaadaServlet implements Servlet {
	private static final long serialVersionUID = 1L;

	/**

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, false);
		String coord = request.getParameter("coord");
		if( coord == null || coord.length() == 0 ) {
			getErrorPage(request, response, "ERROR: No coordinate received");
		}
		else {
			try {
				response.sendRedirect("http://simbad.u-strasbg.fr/simbad/sim-coo?Radius=1&Coord=" + URLEncoder.encode(coord, "iso-8859-1"));
			} catch (Exception e) {
				getErrorPage(request, response, e);
			}
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
