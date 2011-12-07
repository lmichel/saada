package ajaxservlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.util.Messenger;
import saadadb.vo.cart.CartJob;
import uws.UWSException;
import uws.job.JobList;
import uws.service.BasicUWS;
import uws.service.QueuedBasicUWS;
import uws.service.UWSUrl;
import uws.service.UserIdentifier;
import ajaxservlet.accounting.UserTrap;

/**
 * UWS service managing cart service
 * Servlet implementation class ZipBuilder
 * @version $Id$
 */
public class CartBuilder extends SaadaServlet {
	private static final long serialVersionUID = 1L;
	protected BasicUWS<CartJob> cartUWS = null;

	/* (non-Javadoc)
	 * @see ajaxservlet.SaadaServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		try {
			cartUWS = new QueuedBasicUWS<CartJob>(CartJob.class, 2, "/cart");
			cartUWS.setUserIdentifier(new UserIdentifier() {
				private static final long serialVersionUID = 1L;
			public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
					return request.getSession().getId();
				}
			});
			cartUWS.addJobList(new JobList<CartJob>("zipper")); 
		}catch(UWSException ex){
			throw new ServletException(ex);
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try{
			this.printAccess(req, false);
			if( req.getRequestURI().endsWith("/download")) {
				this.downloadProduct(req
						, res
						, UserTrap.getUserAccount(req).getCartDir() + File.separator + CartJob.zipName + ".zip"
						, CartJob.zipName + ".zip");
			}
			else {
				this.printAccess(req, false);
				cartUWS.executeRequest(req, res);
				Messenger.printMsg(Messenger.TRACE, req.getSession().getId() + ": " + cartUWS.getJobList("zipper").getNbJobs()+" jobs");
			}
		}catch(Exception ex){
			this.reportJsonError(req, res, ex);
		}
	}}
