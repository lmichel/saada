package ajaxservlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
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
import saadadb.util.Messenger;
import ajaxservlet.accounting.UserTrap;

/**
 *  
 * @version $Id$
 * Servlet implementation class Download
 */
public class Download extends SaadaServlet {
	private static final long serialVersionUID = 1L;



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

	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		int category ;
		String product_path;
		long oid;
		String separ= Database.getSepar();
		printAccess(request, false);

		try {

			String soid = request.getParameter("oid");

			if( soid != null ) {
				oid = Long.parseLong(soid);
			}
			String ext    = request.getParameter("ext");
			String report = request.getParameter("report");
System.out.println(secureDownlad);
			if( soid != null  ) {
				/*
				 * If the query is set to download a data product, not a preview and the app is in secire more
				 * the service is dispatched to secure download
				 */
				if( ext == null && secureDownlad == true ) {
					response.sendRedirect(Database.getUrl_root() + "/securedownload?oid=" + soid);
					return;						
				}
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
