package ajaxservlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;

/**
 * Servlet implementation class GetVignette
 * @version $Id$
 */
public class GetVignette extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetVignette() {
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
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.printAccess(request, false);
		try {
			long oid = Long.parseLong(request.getParameter("oid"));
			SaadaInstance instance = Database.getCache().getObject(oid);
			String retour = instance.getVignettePath();
			if( retour == null ) {
				retour = base_dir + File.separator  + "images" + File.separator + "nondispo.jpeg";
			}
			downloadProduct(request, response, retour);
		} catch(Exception ee) {
			reportJsonError(request, response, ee);
		}
	}

}
