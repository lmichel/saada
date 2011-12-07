package ajaxservlet;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.util.Messenger;
import saadadb.vo.request.formator.archive.ZipperJob;
import uws.UWSException;
import uws.job.JobList;
import uws.service.BasicUWS;
import uws.service.QueuedBasicUWS;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;

/** 
 * Servlet implementation class datapack
 * @version $Id: datapack.java 295 2011-07-26 12:29:22Z saada $
 */
public class ZipAsync extends SaadaServlet  {
	private static final long serialVersionUID = 1L;

	protected BasicUWS<ZipperJob> zipUWS = null;

	/* (non-Javadoc)
	 * @see ajaxservlet.SaadaServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		try {zipUWS = new QueuedBasicUWS<ZipperJob>(ZipperJob.class, 2, "/datapack");
		zipUWS.setUserIdentifier(new UserIdentifier() {
			private static final long serialVersionUID = 1L;
			public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
				return request.getSession().getId();
			}
		});
		zipUWS.addJobList(new JobList<ZipperJob>("zipper")); 
		}catch(UWSException ex){
			throw new ServletException(ex);
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			printAccess(req, false);
			zipUWS.executeRequest(req, res);
			Messenger.printMsg(Messenger.TRACE, req.getSession().getId() + ": " + zipUWS.getJobList("zipper").getNbJobs()+" jobs");
		}catch(Exception ex){
			getErrorPage(req, res, ex);
		}
	}}
