package ajaxservlet;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.vo.request.ConeSearchRequest;

/**
 * Endpoint of SIA services
 * @author michel
 * @version $Id$
 *
 * 07/2011 switch to the new SIA request processing
 */
public class ConeSearch extends SaadaServlet {
	private static final long serialVersionUID = 1L;
	private static int count = 0;
		
	public void doGet(HttpServletRequest req, HttpServletResponse res)  {
		try {
			printAccess(req, true);
			String reportename = "CS_" + req.getSession().getId() + "_" + (count++);
			ConeSearchRequest request = new ConeSearchRequest(req.getSession().getId(), Database.getVOreportDir());
			request.addFormator("votable");
			request.setResponseFilePath(reportename);
			request.processRequest(getFlatParameterMap(req));
			this.dumpXmlFile(Database.getVOreportDir() + Database.getSepar() + reportename  + ".xml"
					, res
					, Database.getDbname() + "_CS.xml");

			

//			
//			String cp_primoid  = req.getParameter("primoid");
//			String cp_relname  = req.getParameter("relation");
//			/*
//			 * Log hit: what URL from where and when
//			 */
//	        String full_url = req.getRequestURL().toString();
//	        String queryString = req.getQueryString(); 
//	        
//	        if (queryString != null) {
//	            full_url += "?"+queryString;
//	        }		
//			Messenger.printMsg(Messenger.TRACE, req.getRemoteAddr() + ": " + full_url );
//			res.setContentType("text/xml");
//			if( cp_primoid != null && cp_relname != null ) {
//				(new ConeSearchToVOTableFormator(req.getParameter("datamodel"))).processVOQuery(Long.parseLong(cp_primoid), cp_relname, res.getOutputStream());
//			}
//			else {
//				(new ConeSearchToVOTableFormator(req.getParameter("datamodel"))).processVOQuery(new ConeSearchTranslator(req), res.getOutputStream());
//			}
		} catch (Exception e) {
			this.getErrorPage(req, res, e); 
		}
	}
	public void doPost(HttpServletRequest req, HttpServletResponse res)  {
		doGet(req, res);
	}

	
}
