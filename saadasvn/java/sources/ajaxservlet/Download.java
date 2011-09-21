package ajaxservlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.collection.ImageSaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.database.Repository;
import ajaxservlet.accounting.UserTrap;

/**
 *  
 * @version $Id$
 * Servlet implementation class Download
 */
public class Download extends SaadaServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Download() {
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
		int category ;
		String product_path;
		long oid;
		String separ= Database.getSepar();
		printAccess(request, true);
		try {

			String soid = request.getParameter("oid");

			if( soid != null ) {
				oid = Long.parseLong(soid);
			}
			String ext    = request.getParameter("ext");
			String report = request.getParameter("report");

			if( soid != null  ) {
				oid = Long.parseLong(soid);
				SaadaInstance si = Database.getCache().getObject(oid);
				category = SaadaOID.getCategoryNum(oid);
				if( category == Category.ENTRY) {
					getErrorPage(request, response, "There are no product files associated with individual catalogue entries");
					return;						
				}
				if( "vignette".equalsIgnoreCase(ext) ) {					
					if( category != Category.IMAGE) {
						getErrorPage(request, response, "Vignettes are available for image only.");
						return;						
					}
					product_path = Repository.getVignettePath((ImageSaada)si);
				}
				else if( "url".equalsIgnoreCase(ext) ) {		
					PrintWriter out = response.getWriter();
					response.setContentType("text/plain");
					out.print(Database.getUrl_root() + "/getproduct?&oid=" + oid);
					return;
				}
				else {
					product_path = si.getRepositoryPath();
					if( report != null ) {
						product_path +=  separ + report;
					}

					if( ext != null ) {
						product_path += "." + ext;
					}
				}
					downloadProduct(request, response, product_path);
				return;
			}
			else if( report != null ) { 
				downloadProduct(request, response, UserTrap.getUserAccount(request).getReportDir() +report);
				Repository.cleanUpReportDir();
				//Messenger.printMsg(Messenger.WARNING, "penser a remettre le nettoyage (Servlet Download)");
				return;
			}
			else {
				getErrorPage(request, response, "Unconsistant parameters");					
				return;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			getErrorPage(request, response, e);					
			return;
		}
	}

}
