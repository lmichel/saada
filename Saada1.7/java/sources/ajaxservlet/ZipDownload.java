package ajaxservlet;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.archive.ZipFormator;


/**
 * @author michel
 * @version $Id:
 * 01/2014 ZIP ball construction moved to {@link ZipFormator#zipInstance(long, String, String)}
 */
public class ZipDownload extends SaadaServlet {
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
		printAccess(request, false);
		try {
			String soid = request.getParameter("oid");
			long oid = Long.parseLong(soid);
			String sessionid = request.getSession().getId();
			ZipFormator formator = new ZipFormator(sessionid);
			formator.zipInstance(oid, Database.getVOreportDir() + File.separatorChar + sessionid, "any-relations", true, true);
			downloadProduct(request, response, formator.getResponseFilePath());
			// No stack trace when the client interrupt the download
		} catch( SocketException e) {
			Messenger.printMsg(Messenger.WARNING, e.getMessage());
			getErrorPage(request, response, e);					
			return;				
		} catch(Exception e) {
			e.printStackTrace();
			getErrorPage(request, response, e);					
			return;
		}
	}
}
