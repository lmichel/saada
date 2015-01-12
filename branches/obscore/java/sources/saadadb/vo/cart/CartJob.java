package saadadb.vo.cart;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;

import saadadb.database.Repository;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;
import saadadb.vo.request.CartRequest;
import uws.UWSException;
import uws.job.JobThread;
import uws.job.Result;
import uws.job.UWSJob;
import ajaxservlet.accounting.UserAccount;

public class CartJob extends JobThread {

	private static final long serialVersionUID = 1L;
	private static int globalCounter = 0;
	private int localCounter; // counter used to avoid duplication of job ids or vo reports
	private String baseDir;
	private String reportDir;
	public static final String zipName = "CartContent";
	private String sessionId;
	private UWSJob uwsJob = null;
	LinkedHashMap<String, String> lstParams;

	public CartJob(UWSJob j) throws UWSException {
		super(j);
		init(j);
	}

	public CartJob(UWSJob j, String task) throws UWSException {
		super(j, task);
		init(j);

	}

	protected void init(UWSJob j) throws UWSException {
		uwsJob = j;
		globalCounter++;
		Iterator<String> it = j.getAdditionalParameters().iterator();
		String currentParam;
		String currentValue;
		lstParams = new LinkedHashMap<String, String>();
		while (it.hasNext()) {
			currentParam = it.next();
			currentValue = (String) j.getAdditionalParameterValue(currentParam);
			lstParams.put(currentParam, currentValue);
		}
		this.sessionId = j.getOwner().getID();
		this.baseDir = Repository.getUserReportsPath(sessionId);
		this.reportDir = this.baseDir + File.separator + UserAccount.cartDirectory;
		try {
			WorkDirectory.validWorkingDirectory(baseDir);
			WorkDirectory.emptyDirectory(new File(this.reportDir));
			WorkDirectory.validWorkingDirectory(this.reportDir);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e);
		}
	}

	@Override
	protected void jobWork() throws UWSException, InterruptedException {
		try {

			CartRequest request = new CartRequest(sessionId, this.reportDir);
			request.addFormator("zip");
			request.setResponseFilePath("CartContent");
			request.processRequest(this.lstParams);
			Result result = new Result("result", "simple", "cart/download", true);
			this.getResultOutput(result);
			this.publishResult(result);
		} catch (Exception ex) {
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex);
		}
		// try{
		// String resultPrefix = "ZippedSaadaql_" + this.localCounter;
		// ZipRequest request = new ZipRequest(Integer.toString(this.localCounter), this.reportDir);
		// request.addFormator("zip");
		// request.setResponseFilePath(resultPrefix);
		// request.processRequest(lstParam);
		// addResult(new Result("Result", Database.getUrl_root()+"/getproduct?report=" + resultPrefix + ".zip"));
		// }catch(Exception ex){
		// Messenger.printStackTrace(ex);
		// throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex);
		// }

	}

}
