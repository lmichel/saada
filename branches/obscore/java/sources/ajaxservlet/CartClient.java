package ajaxservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class CartCLient
 */
public class CartClient extends SaadaServlet {
	private static final long serialVersionUID = 1L;


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		printAccess(request, true);
		getErrorPage( request,  response, "Unsupported GET method"); 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			printAccess(request, true);
			response.setContentType("text/plain");
		    PrintWriter out = response.getWriter();
			out.println("************************************************");
			out.println("    Content of userscripts/local.js file");
			out.println("************************************************");
			request.getRequestDispatcher("userscripts/local.js").include(request, response);
			out.println("\n************************************************");
			out.println("\n\nuserscripts/local.js file file must be edited by hand to point on your own cart client\n\n");
			out.println("This test URL received the following parameters\n");
			out.println("\tsaadadburl="+ URLDecoder.decode(request.getParameter("saadadburl"), "UTF8") );
			out.println("\tcartcontent=" + URLDecoder.decode(request.getParameter("cartcontent"), "UTF8") + "\n");

		} catch (Exception e) {
			getErrorPage( request,  response, e); 
		}
	}


}
