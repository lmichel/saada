package saadadb.vo.tap;

import javax.servlet.http.HttpServletRequest;

import saadadb.util.Messenger;
import tap.TAPExecutionReport;
import tap.db.DBConnection;
import tap.log.TAPLog;
import tap.metadata.TAPMetadata;
import tap.metadata.TAPTable;
import uws.job.JobList;
import uws.job.UWSJob;
import uws.job.user.JobOwner;
import uws.service.UWS;

public class TapLog implements TAPLog {

	@Override
	public void debug(String msg) {
		Messenger.printMsg(Messenger.DEBUG, "TAP: "+msg);

	}

	@Override
	public void debug(Throwable t) {
		Messenger.printMsg(Messenger.DEBUG, "TAP: "+t.getMessage());

	}

	@Override
	public void debug(String msg, Throwable t) {
		Messenger.printMsg(Messenger.DEBUG, "TAP: "+msg+" :"+t.getMessage());

	}

	@Override
	public void info(String msg) {
		Messenger.printMsg(Messenger.INFO,"TAP: "+ msg);

	}

	@Override
	public void warning(String msg) {
		Messenger.printMsg(Messenger.WARNING,"TAP: "+ msg);

	}

	@Override
	public void error(String msg) {
		Messenger.printMsg(Messenger.ERROR,"TAP: "+ msg);

	}

	@Override
	public void error(Throwable t) {
		Messenger.printMsg(Messenger.ERROR,"TAP: "+t.getMessage());

	}

	@Override
	public void error(String msg, Throwable t) {
		Messenger.printMsg(Messenger.ERROR,"TAP: "+msg+": " +t.getMessage());
	}

	@Override
	public void uwsInitialized(UWS uws) {
		Messenger.printMsg(Messenger.TRACE, "UWS job "+uws.getName()+" started");

	}

	@Override
	public void uwsRestored(UWS uws, int[] report) {
		Messenger.printMsg(Messenger.TRACE, "UWS job "+uws.getName()+" restored");

	}

	@Override
	public void uwsSaved(UWS uws, int[] report) {
		Messenger.printMsg(Messenger.TRACE, "UWS job "+uws.getName()+" Saved");
	}

	@Override
	public void ownerJobsSaved(JobOwner owner, int[] report) {
		
		Messenger.printMsg(Messenger.TRACE, "JobOwner "+owner.getID()+" saved");
	}

	@Override
	public void jobCreated(UWSJob job) {
		Messenger.printMsg(Messenger.TRACE, "Job "+job.getJobId()+" created");

	}

	@Override
	public void jobStarted(UWSJob job) {
		Messenger.printMsg(Messenger.TRACE, "Job "+job.getJobId()+" started");

	}

	@Override
	public void jobFinished(UWSJob job) {
		Messenger.printMsg(Messenger.TRACE, "Job "+job.getJobId()+" finished");

	}

	@Override
	public void jobDestroyed(UWSJob job, JobList jl) {
		Messenger.printMsg(Messenger.TRACE, "Job "+job.getJobId()+" of list "+jl.getName()+" destroyed");
	}

	@Override
	public void httpRequest(
			HttpServletRequest request,
			JobOwner user,
			String uwsAction,
			int responseStatusCode,
			String responseMsg,
			Throwable responseError) {
//		System.out.println("Logger httprequest :"
//				+ "\nRequest  "+request.getQueryString()
//				+"\nOwner "+ user.getID()
//				+ "\nresponse code "+ responseStatusCode
//				+"\nresponse Msg "+responseMsg);
				//+" responseError "+responseError.getMessage());//(new Exception ().getStackTrace()));

	}

	@Override
	public void threadStarted(Thread t, String task) {
		Messenger.printMsg(Messenger.TRACE, "Thread "+t.getName()+", task : "+task+ " Started");

	}

	@Override
	public void threadInterrupted(Thread t, String task, Throwable error) {
		Messenger.printMsg(Messenger.TRACE, "Thread "+t.getName()+", task : "+task+ " interrupted : "+error.getMessage());

	}

	@Override
	public void threadFinished(Thread t, String task) {
		Messenger.printMsg(Messenger.TRACE, "Thread "+t.getName()+", task : "+task+ " finished");

	}

	@Override
	public void queryFinished(TAPExecutionReport report) {
		Messenger.printMsg(Messenger.TRACE, "Query finished in "+report.getTotalDuration()+" ms");

	}

	@Override
	public void dbInfo(String message) {
		Messenger.printMsg(Messenger.INFO, "DBInfo: "+message);

	}

	@Override
	public void dbError(String message, Throwable t) {
		Messenger.printMsg(Messenger.ERROR, "DBError: "+message+ " ,cause : "+t.getMessage());

	}

	@Override
	public void tapMetadataFetched(TAPMetadata metadata) {
		Messenger.printMsg(Messenger.TRACE, "TAPMetadata fetched");

	}

	@Override
	public void tapMetadataLoaded(TAPMetadata metadata) {
		Messenger.printMsg(Messenger.TRACE, "TAPMetadata loaded");

	}

	@Override
	public void connectionOpened(DBConnection<?> connection, String dbName) {
		Messenger.printMsg(Messenger.TRACE, "Connection to DB "+dbName+" opened" );

	}

	@Override
	public void connectionClosed(DBConnection<?> connection) {
		Messenger.printMsg(Messenger.TRACE, "Connection to DB closed" );

	}

	@Override
	public void transactionStarted(DBConnection<?> connection) {
	}

	@Override
	public void transactionCancelled(DBConnection<?> connection) {
	}

	@Override
	public void transactionEnded(DBConnection<?> connection) {
	}

	@Override
	public void schemaCreated(DBConnection<?> connection, String schema) {
	}

	@Override
	public void schemaDropped(DBConnection<?> connection, String schema) {
	}

	@Override
	public void tableCreated(DBConnection<?> connection, TAPTable table) {
	}

	@Override
	public void tableDropped(DBConnection<?> connection, TAPTable table) {
	}

	@Override
	public void rowsInserted(DBConnection<?> connection, TAPTable table, int nbInsertedRows) {
	}

	@Override
	public void sqlQueryExecuting(DBConnection<?> connection, String sql) {

	}

	@Override
	public void sqlQueryError(DBConnection<?> connection, String sql, Throwable t) {

	}

	@Override
	public void sqlQueryExecuted(DBConnection<?> connection, String sql) {

	}

}
