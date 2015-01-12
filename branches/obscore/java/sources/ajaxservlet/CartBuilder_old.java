//package ajaxservlet;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Iterator;
//
//import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import saadadb.database.Database;
//import saadadb.util.Messenger;
//import saadadb.vo.cart.CartJob_old;
//import uws.UWSException;
//import uws.job.JobList;
//import uws.job.UWSJob;
//import uws.job.parameters.UWSParameters;
//import uws.job.serializer.UWSSerializer;
//import uws.service.UWS;
//import uws.service.UWSFactory;
//import uws.service.UWSUrl;
//import uws.service.UserIdentifier;
//import uws.service.backup.UWSBackupManager;
//import uws.service.file.UWSFileManager;
//import uws.service.log.UWSLog;
//import ajaxservlet.accounting.UserTrap;
//
///**
// * UWS service managing cart service
// * Servlet implementation class ZipBuilder
// * @version $Id$
// */
//public class CartBuilder_old extends SaadaServlet {
//	
//	/* *************************************************************** */
//	/*  This class has been replaced by a newer version using uws 4.0  */
//	/*                         See CartBuilder.java                    */
//	/*  ************************************************************** */
////	private static final long serialVersionUID = 1L;
////	 protected BasicUWS<CartJob_old> cartUWS = null;
////	protected UWS uws;
////	protected JobList cartUWS;
////
////	/* (non-Javadoc)
////	 * @see ajaxservlet.SaadaServlet#init(javax.servlet.ServletConfig)
////	 */
////	public void init(ServletConfig conf) throws ServletException {
////		super.init(conf);
////
////		try {
////			
////			cartUWS = new JobList("zipper");
////			uws.addJobList(cartUWS);
////		} catch (UWSException e) {
////			throw new ServletException("JobList's name is null or empty", e);
////		}
//
//		// try {
//		// // cartUWS = new QueuedBasicUWS<CartJob>(CartJob.class, 2, "/cart");
//		// // cartUWS.setUserIdentifier(new UserIdentifier() {
//		// // private static final long serialVersionUID = 1L;
//		// // public String extractUserId(UWSUrl urlInterpreter, HttpServletRequest request) throws UWSException {
//		// // return request.getSession().getId();
//		// // }
//		// // });
//		// // cartUWS.addJobList(new JobList<CartJob>("zipper"));
//		// }catch(UWSException ex){
//		// throw new ServletException(ex);
//		// }
//	//}
//
//	@Override
//	public void service(HttpServletRequest req, HttpServletResponse res)
//			throws ServletException,
//			IOException {
//		Messenger.locateCode(req.getRequestURI());
//		try {
//			SaadaServlet.printAccess(req, false);
//			if (req.getRequestURI().endsWith("/download")) {
//				if (SaadaServlet.secureDownlad) {
//					res.sendRedirect(Database.getUrl_root() + "/securedownload?download=cart");
//					return;
//				}
//				this.downloadProduct(req, res, UserTrap.getUserAccount(req).getCartDir()
//						+ File.separator + CartJob_old.zipName + ".zip", CartJob_old.zipName + ".zip");
//			} else {
//				SaadaServlet.printAccess(req, false);
//		//		this.getFlatParameterMap(req);
//
//UWSJob job = new UWSJob(new UWSParameters(req));
////cartUWS.getExecutionManager().execute(job);
//				// cartUWS.executeRequest(req, res);
//				// Messenger.printMsg(Messenger.TRACE, req.getSession().getId() + ": " + cartUWS.getJobList("zipper").getNbJobs()+" jobs");
//			}
//		} catch (Exception ex) {
//			SaadaServlet.reportJsonError(req, res, ex);
//		}
//	}
//}
