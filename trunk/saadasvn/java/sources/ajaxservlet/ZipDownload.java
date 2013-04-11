package ajaxservlet;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.request.formator.archive.ZipFormator;


/**
 * @author michel
 * @version $Id:
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
			SaadaInstance si = Database.getCache().getObject(oid);			
			String sessionid = request.getSession().getId();
			ZipFormator formator = new ZipFormator(sessionid);
			ArrayList<Long> oids = new ArrayList<Long>();
			oids.add(oid);
			formator.setResultSet(oids);
			Map<String, String> params = new LinkedHashMap<String, String>();
			params.put("collection", si.getCollection().getName());
			params.put("category", Category.explain(si.getCategory()));
			params.put("relations", "any-relations");
			formator.setProtocolParams(params);
			String name = si.getFileName().split("\\.")[0];
			if( name == null ) {
				name = si.getNameSaada().replaceAll("[^_a-zA-Z0-9\\-\\./]+", "_");
			}
			formator.setResponseFilePath(Database.getVOreportDir() + File.separatorChar + sessionid, name);
			formator.buildDataResponse();
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
