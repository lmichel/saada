package ajaxservlet.json;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

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

/**
 * Servlet implementation class RunQuery
 * 1- Run the query given buy the query parameter.
 * 2- manage the display filters to be used fo that query
 * Queried data are not returned here but just the definition of the concerned by the query:
 * The response looks like this:
 *  {
	"columns": [ {name: col_name},....],
	"query": query_string,
	"treepath": unused,
	"visibles": [ {name: col_name},....]
	,
	"constrained": [ {name: col_name},....]
	}
 * column: list of columns the user can display. This lost is used by the client to build the column selector
 * visible: list of columns actually displayed. This filter is only used to setup the column selector 
 *          the first time the data collection is displayed. 
 *          After it is overridden by filters set on client side
 * Both filters are extended with constrained columns. All constrained columns are added the visible filter
 * The visible filter is always set as a sub part of the column filter. 

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
				FilterBase.init(true);


				JSONObject jo = new JSONObject();
				jo.put("query", query);
				jo.put("treepath", treepath);
				/*
				 * Build the display filters. 
				 */
				StoredFilter sf = FilterBase.getColumnFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */);				
				DisplayFilter colfmtor = new DynamicClassDisplayFilter(sf, qs[5], qs[3]);
				/*
				 * run the query. Constrained fields are appended to the filter the the QueryContext
				 */
				ac.setQueryContext(new QueryContext(query, colfmtor, ac.getSessionID()));
				/*
				 * Keep the constrained columns for the visible filters
				 */
				Set<String>constCols = colfmtor.getConstrainedColumns();
				JSONArray jsa = new JSONArray();
				Set<String> vsSet = new TreeSet<String>();
				for( String col: colfmtor.getVisibleColumns()) {
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);
					vsSet.add(col);
				}
				jo.put("columns",jsa);
				/*
				 * Get the default visible filter
				 */
				sf = FilterBase.getVisibleColumnsFilter(qs[5] /* col */, qs[1]/* cat */, qs[3]/* class */);		
				colfmtor = new DynamicClassDisplayFilter(sf, qs[5], qs[3]);
				jsa = new JSONArray();
				for( String col: colfmtor.getVisibleColumns()) {
					/*
					 * Column must be into the column filter
					 */
					if( vsSet.contains(col)){
						JSONObject jo2 = new JSONObject();
						jo2.put("name", col);
						jsa.add(jo2);
					}
				}
				jo.put("visibles",jsa);
				/*
				 * Add constrained columns to the visible filter
				 */
				jsa = new JSONArray();
				for( String col: constCols){
					JSONObject jo2 = new JSONObject();
					jo2.put("name", col);
					jsa.add(jo2);					
				}
				jo.put("constrained",jsa);
				/*
				 * Send the result
				 */
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

