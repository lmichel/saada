package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import ajaxservlet.accounting.QueryContext;
import ajaxservlet.accounting.UserTrap;

/**
 * Servlet implementation class NextPage
 */
public class NextPage extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NextPage() {
		super();
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
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, false);
		try {
			QueryContext context = UserTrap.getUserAccount(request).getQueryContext();
			ServletOutputStream out = response.getOutputStream();   
			int displayStart  = Integer.parseInt(request.getParameter("iDisplayStart"));
			int displayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
			int echo          = Integer.parseInt(request.getParameter("sEcho"));
			
			JsonUtils.teePrint(out,"{");
			JsonUtils.teePrint(out,JsonUtils.getParam("sEcho", new Integer(echo) , "    ") + ",");
			JsonUtils.teePrint(out,JsonUtils.getParam("iTotalRecords", context.getResultSize(), "    ") + ",");
			JsonUtils.teePrint(out,JsonUtils.getParam("iTotalDisplayRecords", context.getResultSize(),  "    ") + ",");
			JsonUtils.teePrint(out,"\"aaData\": [");
			String comma = "";
			for( int rank=displayStart ; rank<(displayStart + displayLength) ; rank++ ) {
				if( context.endReached(rank) ) {
					break;
				}
				JsonUtils.teePrint(out,  comma);
				JsonUtils.teePrint(out, JsonUtils.getRow(context.getRow(rank)) );
				comma = ",";
			}
			JsonUtils.teePrint(out,"     ]");
			JsonUtils.teePrint(out,"}");
		} catch (Exception e) {
			reportJsonError(request, response, e);		}


	}
}
