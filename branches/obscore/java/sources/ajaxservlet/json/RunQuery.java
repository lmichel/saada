package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ajaxservlet.SaadaServlet;
import ajaxservlet.accounting.QueryContext;
import ajaxservlet.accounting.UserAccount;
import ajaxservlet.accounting.UserTrap;
import ajaxservlet.formator.DisplayFilter;
import ajaxservlet.formator.DisplayFilterFactory;

/** * @version $Id: RunQuery.java 941 2014-02-19 08:39:01Z laurent.mistahl $

 * Servlet implementation class RunQuery
 */
public class RunQuery extends SaadaServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	@SuppressWarnings("unchecked")
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, true);
		String query = request.getParameter("query");
		String treepath = "";
		if( query == null ) {
			query = (String) request.getAttribute("query");
			treepath = (String) request.getAttribute("treepath");
		}
		try {			
			response.setContentType("application/json");
			String[] qs = query.split("\\s", -1) ;
			if( qs.length >= 6 ) {
				UserAccount ac = UserTrap.getUserAccount(request);
//			    QueryContext qc = ac.getQueryContext();
//			    if( qc != null ) qc.closeQuery();;
				DisplayFilter colfmtor = DisplayFilterFactory.getFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */,ac);
				ac.setQueryContext(new QueryContext(query, colfmtor, ac.getSessionID()));
				JSONObject jo = new JSONObject();
				jo.put("query", query);
				jo.put("treepath", treepath);
				JSONArray jsa = new JSONArray();
				for( String col: colfmtor.getDisplayedColumns()) {
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);
				}
				jo.put("attributes",jsa);
				JsonUtils.teePrint(response,jo.toJSONString());
			} else {
				reportJsonError(request, response, "Query badly formed");
				return;
			}
		}catch (Exception e) {
			reportJsonError(request, response, e);
		}
	}
	
}

