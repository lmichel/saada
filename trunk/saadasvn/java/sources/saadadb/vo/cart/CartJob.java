/**
 * 
 */
package saadadb.vo.cart;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ajaxservlet.accounting.UserAccount;


import saadadb.database.Repository;
import saadadb.util.Messenger;
import saadadb.util.WorkDirectory;
import saadadb.util.zip.ZIPUtil;
import saadadb.util.zip.ZipMap;
import saadadb.vo.request.CartRequest;
import uws.UWSException;
import uws.UWSToolBox;
import uws.job.AbstractJob;
import uws.job.ErrorType;
import uws.job.Result;

/**
 * AbstractJob implementation to build ZIP archive in asynchronous mode
 * @author laurent
 * @version $Id$
 */
public class CartJob extends AbstractJob {
	private static final long serialVersionUID = 1L;
	private static int globalCounter = 0;
	private int localCounter; // counter used to avoid duplication of job ids or vo reports
	private String baseDir;
	private String reportDir;
	public static final String zipName = "CartContent";
	private String sessionId;
	private ZipMap zipMap;
	/*
	 * Params map stored seems to be filtered, I prefer keep it entire here
	 * Sorry Gregory :=)
	 */
	Map<String, String> lstParam;

	/**
	 * @param lstParam
	 * @throws UWSException
	 */
	public CartJob(Map<String, String> lstParam) throws UWSException {
		super(lstParam);
		System.out.println("@@@@@@@@@@@@@@ JOB");
		this.lstParam = lstParam;
		globalCounter++;
		this.sessionId = lstParam.get("owner");
		for (Entry<String, String> k: lstParam.entrySet()) {
			System.out.println(k.getKey() + " " + k.getValue());
		}
		this.baseDir = Repository.getUserReportsPath(sessionId)  ;
		this.reportDir = this.baseDir + File.separator + UserAccount.cartDirectory;
		try {
			WorkDirectory.validWorkingDirectory(baseDir);
			WorkDirectory.emptyDirectory(new File(this.reportDir));
			WorkDirectory.validWorkingDirectory(this.reportDir);
			CartDecoder decoder = new CartDecoder();
			decoder.decode(lstParam.get("cart"));
			zipMap = decoder.getZipMap();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, e);
		}
	}


	@Override
	protected void jobWork() throws UWSException, InterruptedException {
		try{
			CartRequest request = new CartRequest(sessionId, this.reportDir);
			request.addFormator("zip");
			request.setResponseFilePath("CartContent");
			request.processRequest(this.lstParam);
			this.addResult(new Result("Result", "cart/download" ));
		}catch(Exception ex){
			Messenger.printStackTrace(ex);
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex);
		}
		//		try{
		//			String resultPrefix = "ZippedSaadaql_" + this.localCounter;
		//			ZipRequest request = new ZipRequest(Integer.toString(this.localCounter), this.reportDir);
		//			request.addFormator("zip");
		//			request.setResponseFilePath(resultPrefix);
		//			request.processRequest(lstParam);
		//			addResult(new Result("Result", Database.getUrl_root()+"/getproduct?report=" + resultPrefix + ".zip"));
		//		}catch(Exception ex){
		//			Messenger.printStackTrace(ex);
		//			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex);
		//		}
	}

	/* (non-Javadoc)
	 * @see uws.job.AbstractJob#generateJobId()
	 */
	public String generateJobId(){	
		/*
		 * Counter update done here because this method is called by the super creator;
		 */
		this.localCounter = globalCounter;	
		return  "ZipArchive_" + this.localCounter ;
	}

	@Override
	public synchronized void error(UWSException ue) throws UWSException {
		if (ue.getHttpErrorCode() == UWSException.INTERNAL_SERVER_ERROR){
			String errorFileName = "UWSERROR_Job"+getJobId()+"_"+System.currentTimeMillis()+".txt";
			try{
				String errorURL = "/getproduct?report="+errorFileName;
				UWSToolBox.publishErrorSummary(this, ue, ErrorType.FATAL, errorURL, this.reportDir, errorFileName);
			}catch(IOException ioe){
				Messenger.printStackTrace(ioe);
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ioe, "Error while writing the error file for the job N°"+getJobId()+" !");
			}
		}else
			UWSToolBox.publishErrorSummary(this, ue.getMessage(), ue.getUWSErrorType());
	}
}
