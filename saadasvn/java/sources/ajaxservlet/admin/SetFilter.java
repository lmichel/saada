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
import ajaxservlet.accounting.UserAccount;
import ajaxservlet.accounting.UserTrap;
import ajaxservlet.formator.StoredFilter;

/**
 * Servlet implementation class SetFilter
 * @version $Id$

 */
public class SetFilter extends HttpServlet {
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
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			UserAccount ua = UserTrap.getUserAccount(request);
			
			String jsondata = request.getParameter("filter");

			JSONParser jp = new JSONParser();
			Object json = jp.parse(jsondata);
			String filename = "df." + ua.getSessionID() + request.getParameter("name") + ".json";
			String dir  = Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "userfilters";
			File child = new File(dir);
			if (!child.exists()) (new File(dir)).mkdir();
			File filter = new File(dir + "/" + filename );
			FileWriter fw = new FileWriter(filter, false);
			fw.write(jsondata);
			fw.close();
			
			FileReader fr = new FileReader(filter);
			StoredFilter sf = new StoredFilter(fr);
			
			ua.addFilter(sf);
			
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
