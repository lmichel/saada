package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.exceptions.QueryException;
import saadadb.query.parser.PositionParser;
import ajaxservlet.SaadaServlet;
import ajaxservlet.formator.DefaultFormats;

/** * @version $Id$

 * Servlet implementation class Sesame
 */
public class Sesame extends SaadaServlet  {
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor. 
	 */
	public Sesame() {
		// TODO Auto-generated constructor stub
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

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String object = request.getParameter("object");
		ServletOutputStream out = response.getOutputStream();
		response.setContentType("application/json");
		if( object == null || object.trim().length() == 0 ) {
			JsonUtils.teePrint(out , "{");
			JsonUtils.teePrint(out , JsonUtils.getParam("alpha", "") + ",");
			JsonUtils.teePrint(out, JsonUtils.getParam("delta", ""));
			JsonUtils.teePrint(out , "}");
		}
		else {
			try {
				PositionParser pp = new PositionParser(object);
				String alpha = DefaultFormats.getDecimalCoordString(pp.getRa());
				String delta;
				if( pp.getDec() > 0 ) {
					delta = "+" + DefaultFormats.getDecimalCoordString(pp.getDec());
				}
				else {
					delta = DefaultFormats.getDecimalCoordString(pp.getDec());	
				}
				JsonUtils.teePrint(out , "{");
				JsonUtils.teePrint(out , JsonUtils.getParam("alpha", alpha) + ",");
				JsonUtils.teePrint(out, JsonUtils.getParam("delta", delta));
				JsonUtils.teePrint(out , "}");

			} catch (QueryException e) {
				reportJsonError( request,response, e);
			}
		}
	}
}
