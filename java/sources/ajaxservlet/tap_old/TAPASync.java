package ajaxservlet.tap_old;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ajaxservlet.SaadaServlet;
import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.tap_old.SaadaJob;
import uws.UWSException;
import uws.job.JobList;
import uws.service.BasicUWS;
import uws.service.QueuedBasicUWS;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;

/**
 * @author laurent
 * @version $Id$
 *
 */
public class TAPASync extends SaadaServlet {
	private static final long serialVersionUID = 1L;
	
	protected BasicUWS<SaadaJob> uws = null;

	@Override
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		try{
			// 2 jobs tournent en meme temps
			uws = new QueuedBasicUWS<SaadaJob>(SaadaJob.class, 2, "/tap");
//			uws.setUserIdentifier(new UserIdentifier() {
//				private static final long serialVersionUID = 1L;
//
//				public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
//					return request.getSession().getId();
//				}
//			});
//			uws.setXsltURL(Database.getUrl_root()+"/styles/uws.xsl");
//			uws.addJobList(new JobList<SaadaJob>("async"));
		}catch(UWSException ex){
			throw new ServletException(ex);
		}
	} 

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			printAccess(req, false);
			boolean done = uws.executeRequest(req, res);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, " Done ? "+done+" ; Method: "+req.getMethod()+" ; Action: "+uws.getExecutedAction()+" ; NbJobs: "
					+uws.getJobList("async").getNbJobs()+" ; NbUsers: "+uws.getJobList("async").getNbUsers());
		}catch(UWSException ex){
			getErrorPage(req, res, ex);
		}
	}

}
