package ajaxservlet.json;

import java.io.IOException;
import java.util.Set;

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
import ajaxservlet.formator.DynamicClassDisplayFilter;
import ajaxservlet.formator.FilterBase;
import ajaxservlet.formator.StoredFilter;

/** * @version $Id$

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
				//DisplayFilter colfmtor = DisplayFilterFactory.getFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */,ac);
				FilterBase.init(false);
				
				
				JSONObject jo = new JSONObject();
				jo.put("query", query);
				jo.put("treepath", treepath);
				
				StoredFilter sf = FilterBase.getColumnFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */);				
				DisplayFilter colfmtor = new DynamicClassDisplayFilter(sf, qs[5], qs[3]);
				/*
				 * Add the constrained field to the filter
				 */
				ac.setQueryContext(new QueryContext(query, colfmtor, ac.getSessionID()));
				Set<String>constCols = colfmtor.getConstrainedColumns();
				JSONArray jsa = new JSONArray();
				for( String col: colfmtor.getVisibleColumns()) {
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);
				}
				jo.put("columns",jsa);
				
				sf = FilterBase.getVisibleColumnsFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */);				
				colfmtor = new DynamicClassDisplayFilter(sf, qs[5], qs[3]);

				jsa = new JSONArray();
				for( String col: colfmtor.getVisibleColumns()) {
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);
				}
				jo.put("visibles",jsa);
				jsa = new JSONArray();
				for( String col: constCols){
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);					
				}
				jo.put("constrained",jsa);
				
				
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

