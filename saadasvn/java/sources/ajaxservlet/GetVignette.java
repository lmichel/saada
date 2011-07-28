package ajaxservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.ImageSaada;
import saadadb.database.Database;

/**
 * Servlet implementation class GetVignette
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
			String separ = Database.getSepar();
			long oid = Long.parseLong(request.getParameter("oid"));
			ImageSaada instance = (ImageSaada) Database.getCache().getObject(oid);
			String filename =  Database.getRepository() 
			+ separ + instance.getCollection().getName() 
			+ separ + "IMAGE" 
			+ separ + "JPEG"
			+separ +instance.getVignetteName();
			downloadProduct(request, response, filename);

		} catch(Exception ee) {
			reportJsonError(request, response, ee);
		}


	}

}
