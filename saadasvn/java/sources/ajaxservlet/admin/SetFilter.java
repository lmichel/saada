package ajaxservlet.admin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import saadadb.database.Database;
import ajaxservlet.SaadaServlet;
import ajaxservlet.accounting.UserAccount;
import ajaxservlet.accounting.UserTrap;
import ajaxservlet.formator.StoredFilter;

/**
 * Servlet implementation class SetFilter
 * @version $Id$

 */
public class SetFilter extends SaadaServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SetFilter() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.process(request, response);
	}
	
	
	/**
	 * save the filter given as a parameter (a JSONString
	 * formatted using the javascript function escape)
	 * The filter is saved in ../config/userfilters and 
	 * added to the userbase
	 * 
	 * @param request
	 * @param response
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response)  {
		try {
			UserAccount ua = UserTrap.getUserAccount(request);
			this.printAccess(request, true);			
			String jsondata = request.getParameter("filter");			
			ua.addFilter(new StoredFilter(jsondata));	
		} catch (Exception e) {
			this.reportJsonError(request, response, e);
		}
	}

}
