package saadadb.vo.tap;
 
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.util.RegExp;
import saadadb.vo.request.formator.QueryResultFormator;
import saadadb.vo.tap.TAPToolBox.TAPParameters;
import uws.UWSException;
import uws.UWSToolBox;
import uws.job.AbstractJob;
import uws.job.ErrorSummary;
import uws.job.ErrorType;
import uws.job.Result;

/**
 * @author Gregory
 * @version $Id$
 * 07/2011 Switch on new VO request framework by LM
 * 07/2011 change report name by LM
 */
public class SaadaJob extends AbstractJob {
	private static final long serialVersionUID = 1L;
	private final String owner;
	private  String id;
	private static int globalCounter = 0;
	private int localCounter; // counter used to avoid duplication of job ids or vo reports
	private final String reportNameRoot;
	private static final Pattern ROOTJOB_PATTERN = Pattern.compile("FROM\\s+(" + RegExp.CLASSNAME + ")[\\s;]?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	/**
	 * @param lstParam
	 * @throws UWSException
	 */
	public SaadaJob(Map<String, String> lstParam) throws UWSException {
		super(lstParam);
		this.owner = lstParam.get("owner");
		System.out.println("owner" + this.owner);

		globalCounter++;
		reportNameRoot ="TAPResult" + "_"+getJobId();
	}

	/**
	 * @param ownerID
	 * @param lstParam
	 * @throws UWSException
	 */
	public SaadaJob(String ownerID, Map<String, String> lstParam) throws UWSException {
		super(ownerID, lstParam);
		this.owner = lstParam.get("owner");
		System.out.println("owner" + this.owner);
		globalCounter++;
		reportNameRoot ="TAPResult" + "_"+getJobId();
	}

	/**
	 * @param jobName
	 * @param userId
	 * @param maxDuration
	 * @param destructTime
	 * @param lstParam
	 * @throws UWSException
	 */
	public SaadaJob(String jobName, String userId, long maxDuration, Date destructTime, Map<String, String> lstParam) throws UWSException {
		super(jobName, userId, maxDuration, destructTime, lstParam);
		this.owner = lstParam.get("owner");
		System.out.println("owner" + this.owner);
		globalCounter++;
		reportNameRoot ="TAPResult" + "_"+getJobId();
	}


	/* (non-Javadoc)
	 * @see uws.job.AbstractJob#generateJobId()
	 */
	public String generateJobId(){
		System.out.println("couco");
		try{
		String rootJobName = "saadajob";
		String query = additionalParameters.get("query");
		/*
		 * Counter update done here because this method is called by the super creator;
		 */
		this.localCounter = globalCounter;	
		if( query == null ) {
			query = additionalParameters.get("QUERY");
		}
		if( query != null ) {
			Matcher m = ROOTJOB_PATTERN.matcher(query);
			if( m.find()  ) {
				rootJobName = m.group(1).trim();
				if( rootJobName.length() > 32 ) {
					rootJobName = rootJobName.substring(0, 32);
				}
			}
		}
		this.id = (this.localCounter) + "_" + rootJobName	;	
		System.out.println(this.id);
		return  this.id;
		}catch (Exception e) {
           e.printStackTrace();		}
		return null;
	}
	
	@Override
	public void clearResources() {
		super.clearResources();
		
		File f;
		// Delete all results files:
		Iterator<Result> it = getResults();
		while(it.hasNext()){
			try{
				Result res = it.next();
				f = new File(Repository.getUserReportsPath(this.owner), res.getHref().toString().substring(res.getHref().toString().indexOf('=')+1));
				if (f.exists()) f.delete();
			}catch(Exception ex){;}
		}
		results.clear();
		
		// Delete the error file:
		ErrorSummary error = getErrorSummary();
		if (error != null && error.hasDetail()){
			try{
				f = new File(Repository.getUserReportsPath(this.owner), error.getDetails().toString().substring(error.getDetails().toString().indexOf('=')+1));
				if (f.exists()) f.delete();
			}catch(Exception ex){;}
		}
		errorSummary = null;
	}

	@Override
	public synchronized void error(UWSException ue) throws UWSException {
		if (ue.getHttpErrorCode() == UWSException.INTERNAL_SERVER_ERROR){
			String errorFileName = "UWSERROR_Job"+getJobId()+"_"+System.currentTimeMillis()+".txt";
			try{
				String errorURL = Database.getUrl_root()+"/getproduct?jobreport="+errorFileName;
				UWSToolBox.publishErrorSummary(this, ue, ErrorType.FATAL, errorURL,Repository.getUserReportsPath(this.owner), errorFileName);
			}catch(IOException ioe){
				throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ioe, "Error while writing the error file for the job N°"+getJobId()+" !");
			}
		}else
			UWSToolBox.publishErrorSummary(this, ue.getMessage(), ue.getUWSErrorType());
	}

	@Override
	protected void jobWork() throws UWSException, InterruptedException {
		try{
			// Check and get TAP parameters:
			TAPParameters params = new TAPParameters(this);
			
			// Check the REQUEST value (only doQuery is supported):
			if (!params.request.equalsIgnoreCase("doQuery"))
				throw new UWSException("The only value accepted for the REQUEST parameter in an asynchronous request is: \"doQuery\" (now: REQUEST=\""+params.request+"\") !", ErrorType.TRANSIENT);
			
			// TODO For now the language version is ignored:
			if (params.lang.lastIndexOf('-') > -1)
				params.lang = params.lang.substring(0, params.lang.lastIndexOf('-'));
			
			// Check if the given language is supported:
			if (!params.lang.equals("SaadaQL") && !params.lang.equals("ADQL"))
				throw new UWSException("The query language \""+params.lang+"\" is not supported ! Supported formats are: \"ADQL\" and \"SaadaQL\".", ErrorType.TRANSIENT);
						
			if (thread.isInterrupted())
				throw new InterruptedException();
			
			// Execute the query and return a formatted result:
			TAPToolBox.executeTAPQuery(params.query, params.lang.equals("SaadaQL"), params.format, params.maxrec,  Repository.getUserReportsPath(owner), reportNameRoot);

			if (thread.isInterrupted())
				throw new InterruptedException();
			
			// Update the job description and status:
			addResult(new Result("Result", Database.getUrl_root()+"/getproduct?jobreport="+reportNameRoot + QueryResultFormator.getFormatExtension(params.format)));
			
			if(params.format.equalsIgnoreCase("json")) {
				TAPToolBox.executeTAPQuery(params.query, params.lang.equals("SaadaQL"), "votable", params.maxrec, Repository.getUserReportsPath(owner), reportNameRoot);
				if (thread.isInterrupted())
					throw new InterruptedException();
				addResult(new Result("Result", Database.getUrl_root()+"/getproduct?jobreport="+reportNameRoot + QueryResultFormator.getFormatExtension("votable")));
			}
		}catch(UWSException ue){
			throw ue;
		}catch(InterruptedException ie){
			throw ie;
		}catch(Exception ex){
			throw new UWSException(UWSException.INTERNAL_SERVER_ERROR, ex);
		}
	}

	public String toString() {
		return (this.owner + " " + this.id);
	}
}
