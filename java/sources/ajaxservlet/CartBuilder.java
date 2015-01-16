package ajaxservlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.vo.cart.CartJob;
import uws.UWSException;
import uws.job.JobList;
import uws.job.JobThread;
import uws.job.UWSJob;
import uws.job.user.DefaultJobOwner;
import uws.job.user.JobOwner;
import uws.service.UWSUrl;
import uws.service.actions.UWSAction;
import ajaxservlet.accounting.UserTrap;

public class CartBuilder extends SaadaUWSServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5006758650708814415L;

	@Override
	public JobThread createJobThread(UWSJob jobDescription) throws UWSException {

		JobThread newJob = new CartJob(jobDescription);
		return newJob;
	}

	@Override
	public void initUWS() throws UWSException {
		Messenger.debug_mode = true;
		addJobList(new JobList("zipper"));
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException,
			IOException {

		SaadaServlet.printAccess(req, false);
		if (req.getRequestURI().endsWith("/download")) {

			if (SaadaServlet.secureDownlad) {
				resp.sendRedirect(Database.getUrl_root() + "/securedownload?download=cart");
				return;
			}
			try {
				this.downloadProduct(req, resp, UserTrap.getUserAccount(req).getCartDir()
						+ File.separator + CartJob.zipName + ".zip", CartJob.zipName + ".zip");
			} catch (Exception e) {
				throw new ServletException("Can't download product", e);
			}
		} else {

			String uwsAction = null;
			JobOwner user = null;

			try {
				String method = req.getMethod();

				// Create a URL interpreter if needed:
				if (getUrlInterpreter() == null)
					setUrlInterpreter(new UWSUrl(req));

				// Update the UWS URL interpreter:
				UWSUrl requestUrl = new UWSUrl(this.getUrlInterpreter());
				requestUrl.load(req);
				
				/* Original UserID identification */
				/*Removed because Saada has its own way to extract userID*/
				// Identify the user:
				// user = (userIdentifier == null) ? null : userIdentifier.extractUserId(
				// requestUrl,
				// req);
				String userId = UserTrap.getUserAccount(req).getSessionID();
				user = new DefaultJobOwner(userId, userId);

				// METHOD GET:
				if (method.equals("GET")) {

					// HOME PAGE:
					if (!requestUrl.hasJobList()) {
						uwsAction = UWSAction.HOME_PAGE;
						writeHomePage(requestUrl, req, resp, user);

					}// LIST JOBS:
					else if (requestUrl.hasJobList() && !requestUrl.hasJob()) {
						uwsAction = UWSAction.LIST_JOBS;
						doListJob(requestUrl, req, resp, user);

					}// JOB SUMMARY:
					else if (requestUrl.hasJobList() && requestUrl.hasJob()
							&& !requestUrl.hasAttribute()) {

						uwsAction = UWSAction.JOB_SUMMARY;
						doJobSummary(requestUrl, req, resp, user);

					}// GET JOB PARAMETER:
					else if (requestUrl.hasJobList() && requestUrl.hasJobList()
							&& requestUrl.hasAttribute()) {

						uwsAction = UWSAction.GET_JOB_PARAM;
						doGetJobParam(requestUrl, req, resp, user);

					} else {

						logger.httpRequest(req, user, null, 0, null, null);
						super.service(req, resp);
						return;
					}

				}// METHOD POST:
				else if (method.equals("POST")) {

					// HOME PAGE:
					if (!requestUrl.hasJobList()) {

						uwsAction = UWSAction.HOME_PAGE;
						writeHomePage(requestUrl, req, resp, user);

					}// ADD JOB:
					else if (requestUrl.hasJobList() && !requestUrl.hasJob()) {

						uwsAction = UWSAction.ADD_JOB;
						doAddJob(requestUrl, req, resp, user);
					}// SET JOB PARAMETER:
					else if (requestUrl.hasJobList()
							&& requestUrl.hasJob()
							&& (!requestUrl.hasAttribute() || requestUrl.getAttributes().length == 1)
							&& req.getParameterMap().size() > 0) {

						uwsAction = UWSAction.SET_JOB_PARAM;
						doSetJobParam(requestUrl, req, resp, user);

					}// DESTROY JOB:
					else if (requestUrl.hasJobList()
							&& requestUrl.hasJob()
							&& req.getParameter(UWSJob.PARAM_ACTION) != null
							&& req.getParameter(UWSJob.PARAM_ACTION).equalsIgnoreCase(
									UWSJob.ACTION_DELETE)) {

						uwsAction = UWSAction.DESTROY_JOB;
						doDestroyJob(requestUrl, req, resp, user);

					} else {
						logger.httpRequest(req, user, null, 0, null, null);
						super.service(req, resp);
						return;
					}

				}// METHOD PUT:
				else if (method.equals("PUT")) {
					// SET JOB PARAMETER:
					if (requestUrl.hasJobList()
							&& requestUrl.hasJob()
							&& req.getMethod().equalsIgnoreCase("put")
							&& requestUrl.getAttributes().length >= 2
							&& requestUrl.getAttributes()[0]
									.equalsIgnoreCase(UWSJob.PARAM_PARAMETERS)
							&& req.getParameter(requestUrl.getAttributes()[1]) != null) {
						uwsAction = UWSAction.SET_JOB_PARAM;
						doSetJobParam(requestUrl, req, resp, user);

					} else {
						logger.httpRequest(req, user, null, 0, null, null);
						super.service(req, resp);
						return;
					}

				}// METHOD DELETE:
				else if (method.equals("DELETE")) {
					// DESTROY JOB:
					if (requestUrl.hasJobList() && requestUrl.hasJob()
							&& req.getMethod().equalsIgnoreCase("delete")) {
						uwsAction = UWSAction.DESTROY_JOB;
						doDestroyJob(requestUrl, req, resp, user);

					} else {
						logger.httpRequest(req, user, null, 0, null, null);
						super.service(req, resp);
						return;
					}

				}// ELSE => DEFAULT BEHAVIOR:
				else {

					logger.httpRequest(req, user, null, 0, null, null);
					super.service(req, resp);
					return;
				}

				resp.flushBuffer();
				logger.httpRequest(req, user, uwsAction, HttpServletResponse.SC_OK, "[OK]", null);

			} catch (UWSException ex) {
				sendError(ex, req, user, uwsAction, resp);
			} catch (Exception cae) {
				logger.info("Request aborted by the user !");
				logger.httpRequest(
						req,
						user,
						uwsAction,
						HttpServletResponse.SC_OK,
						"[Client abort => ClientAbortException]",
						null);
			} catch (Throwable t) {
				logger.error("Request unexpectedly aborted !", t);
				logger.httpRequest(
						req,
						user,
						uwsAction,
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						t.getMessage(),
						t);
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
			}
		}
	}
}
