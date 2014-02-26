package ajaxservlet.admin;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import ajaxservlet.accounting.UserAccount;
import ajaxservlet.accounting.UserTrap;

/** * @version $Id: ResetFilter.java 878 2013-12-17 13:33:34Z laurent.mistahl $

 * Servlet implementation class ResetFilter
 * @version $Id: ResetFilter.java 878 2013-12-17 13:33:34Z laurent.mistahl $
 */
public class ResetFilter extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/** * @version $Id: ResetFilter.java 878 2013-12-17 13:33:34Z laurent.mistahl $

	 * @see HttpServlet#HttpServlet()
	 */
	public ResetFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/** * @version $Id: ResetFilter.java 878 2013-12-17 13:33:34Z laurent.mistahl $

	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 * * @version $Id: ResetFilter.java 878 2013-12-17 13:33:34Z laurent.mistahl $
/
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		process(request, response);
	}


	/**
	 * given the category and the collection as
	 * parameters, resets the user's filterbase
	 * 
	 * - will delete a single filter if both the cat 
	 * and coll are properly provided
	 * - will reset all the filters for the given category
	 * if the param coll is set to "all"
	 *  - will reset all the filters if both cat
	 * and coll are set to "all"
	 * 
	 * @param request
	 * @param response
	 */
	private void process(HttpServletRequest request, HttpServletResponse response)  {
		try {
			this.printAccess(request, true);
			String cat = request.getParameter("cat");
			String coll = request.getParameter("coll");
			UserAccount ua = UserTrap.getUserAccount(request);
			if (cat.compareTo("all") == 0) {
				ua.resetAll();			
			} else if (coll.compareTo("all") == 0) {
				ua.resetCat(cat);
			} else {
				ua.resetFilter(coll, cat);
			}
		} catch (Exception e) {
			this.reportJsonError(request, response, e);
		}
	}

}
