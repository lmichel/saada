package ajaxservlet.json;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import saadadb.database.Database;

import ajaxservlet.SaadaServlet;
import ajaxservlet.accounting.QueryContext;
import ajaxservlet.accounting.UserTrap;
import ajaxservlet.formator.*;

/** * @version $Id$

 * Servlet implementation class RunQuery
 */
public class RunQuery extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RunQuery() {
		super();
		// TODO Auto-generated constructor stub
	}

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
			new FilterBase(Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar(), false);
			
			String[] qs = query.split("\\s", -1) ;
			
			ServletOutputStream out = response.getOutputStream();
			if( qs.length >= 6 ) {
				DisplayFilter colfmtor = DisplayFilterFactory.getFilter(qs[5], qs[1], request);
				UserTrap.getUserAccount(request).setQueryContext(new QueryContext(query, colfmtor));
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
				JsonUtils.teePrint(out,jo.toJSONString());
			}
			else {
				reportJsonError(request, response, "Query badly formed");
				return;
			}
		}catch (Exception e) {
			reportJsonError(request, response, e);
		}
	}
	
}

