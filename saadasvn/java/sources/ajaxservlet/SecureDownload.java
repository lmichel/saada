package ajaxservlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.vo.cart.CartJob;
import ajaxservlet.accounting.UserTrap;

/**
 * Servlet implementation class SecureDownload
 * identical to download. The existence of this servlet is to restrict the access to some DB resources thanks to
 * a password set in Tomcat config.
 * In the exemple below, we assume that the account name is reader ( to be set in server.xml)
 * 	<security-constraint>
		<web-resource-collection>
			<web-resource-name>securedownload requires authentication</web-resource-name>
			<url-pattern>/securedownload</url-pattern>
			<http-method>GET</http-method>
			<http-method>POST</http-method>
		</web-resource-collection>
		<auth-constraint>
			<role-name>reader</role-name>
		</auth-constraint>

		<user-data-constraint>
			<!-- transport-guarantee can be CONFIDENTIAL, INTEGRAL, or NONE -->
			<transport-guarantee>NONE</transport-guarantee>
		</user-data-constraint>
	</security-constraint>

	<login-config>
		<auth-method>BASIC</auth-method>
	</login-config>

 * @author michel
 * @version $Id$
 *
 */
public class SecureDownload extends Download implements Servlet {
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see ajaxservlet.Download#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
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
			String download    = request.getParameter("download");

			if( soid != null  ) {
				oid = Long.parseLong(soid);
				SaadaInstance si = Database.getCache().getObject(oid);
				category = SaadaOID.getCategoryNum(oid);
				if( category == Category.ENTRY) {
					getErrorPage(request, response, "There are no product files associated with individual catalogue entries");
					return;						
				}
				product_path = si.getRepositoryPath();
				if( report != null ) {
					product_path +=  separ + report;
				}

				if( ext != null ) {
					product_path += "." + ext;
				}
				downloadProduct(request, response, product_path);
				return;
			} else if( "cart".equalsIgnoreCase(download) ) {
				this.downloadProduct(request
						, response
						, UserTrap.getUserAccount(request).getCartDir() + File.separator + CartJob.zipName + ".zip"
						, CartJob.zipName + ".zip");	
				return ;
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
