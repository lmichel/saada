package ajaxservlet.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.*;

import saadadb.database.Database;
import saadadb.util.Messenger;

import ajaxservlet.SaadaServlet;
import ajaxservlet.formator.*;
import ajaxservlet.json.JsonUtils;

/**
 * Servlet implementation class GetFilter
 * @version $Id$
 */
public class GetFilter extends SaadaServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetFilter() {
        super();
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
	
	
	/**
	 * given the category and the collection as parameters
	 * will search for the right filter.
	 * First asking the userbase, if nothing's found, will 
	 * search for the default filter (the one chosen by the admin)
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			this.printAccess(request, true);
			String cat = request.getParameter("cat");
			String coll = request.getParameter("coll");
			ServletOutputStream out = response.getOutputStream();  

			StoredFilter filter;
			filter = DisplayFilterFactory.getStoredFilter(coll, cat, request);
			String jsonfilter = null;
			if (filter != null) {
				jsonfilter = filter.getRawJSON();
			} else {
				Messenger.printMsg(Messenger.TRACE, "No stored filter found");
				jsonfilter = DisplayFilterFactory.getDefaultJSON(cat);
			}
			JsonUtils.teePrint(out, jsonfilter);
		} catch (Exception e) {
			this.reportJsonError(request, response, e);
		}
	}

}
