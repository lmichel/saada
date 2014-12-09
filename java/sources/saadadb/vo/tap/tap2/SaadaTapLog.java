package saadadb.vo.tap.tap2;

import javax.servlet.http.HttpServletRequest;

import tap.TAPExecutionReport;
import tap.db.DBConnection;
import tap.log.TAPLog;
import tap.metadata.TAPMetadata;
import tap.metadata.TAPTable;
import uws.job.JobList;
import uws.job.UWSJob;
import uws.job.user.JobOwner;
import uws.service.UWS;

public class SaadaTapLog implements TAPLog {

	@Override
	public void debug(String msg) {
		System.out.println("logger debug message "+msg);

	}

	@Override
	public void debug(Throwable t) {
		System.out.println("Logger debug throwable "+ t.getMessage());

	}

	@Override
	public void debug(String msg, Throwable t) {
		System.out.println("logger debug message throwable MSG="+msg+" Throwable="+t.getMessage());

	}

	@Override
	public void info(String msg) {
		System.out.println("logger info : "+msg);

	}

	@Override
	public void warning(String msg) {
		System.out.println("logger warning :"+msg);

	}

	@Override
	public void error(String msg) {
		System.out.println("logger error message "+msg);

	}

	@Override
	public void error(Throwable t) {
		System.out.println("\n==========================");
		System.out.println("logger error throwable "+t.getMessage()+"  "+t.getCause());
		//new Exception().printStackTrace();
		System.out.println("=========================\n\n");

	}

	@Override
	public void error(String msg, Throwable t) {
		System.out.println("logger message throwable, MSG="+msg+" thro="+t.getMessage());

	}

	@Override
	public void uwsInitialized(UWS uws) {
		System.out.println("logger UWS "+uws.getName()+","+uws.getDescription()+" initialized");

	}

	@Override
	public void uwsRestored(UWS uws, int[] report) {
		System.out.println("logger UWS "+uws.getName()+","+uws.getDescription()+" restored");

	}

	@Override
	public void uwsSaved(UWS uws, int[] report) {
		System.out.println("logger UWS "+uws.getName()+","+uws.getDescription()+" saved");
	}

	@Override
	public void ownerJobsSaved(JobOwner owner, int[] report) {
		
System.out.println("logger ownerJobSaved : ID"+owner.getID()+" pseudo "+owner.getPseudo());
	}

	@Override
	public void jobCreated(UWSJob job) {
		System.out.println("logger job created "+job.getJobId());

	}

	@Override
	public void jobStarted(UWSJob job) {
		System.out.println("logger job started "+job.getJobId());

	}

	@Override
	public void jobFinished(UWSJob job) {
		System.out.println("job finished "+job.getJobId());

	}

	@Override
	public void jobDestroyed(UWSJob job, JobList jl) {
System.out.println("logger job destroyed "+job.getJobId()+ "list "+jl.getName());
	}

	@Override
	public void httpRequest(
			HttpServletRequest request,
			JobOwner user,
			String uwsAction,
			int responseStatusCode,
			String responseMsg,
			Throwable responseError) {
		System.out.println("Logger httprequest :"
				+ "\nRequest  "+request.getQueryString()
				+"\nOwner "+ user.getID()
				+ "\nresponse code "+ responseStatusCode
				+"\nresponse Msg "+responseMsg);
				//+" responseError "+responseError.getMessage());//(new Exception ().getStackTrace()));

	}

	@Override
	public void threadStarted(Thread t, String task) {
		System.out.println("logger thread started");

	}

	@Override
	public void threadInterrupted(Thread t, String task, Throwable error) {
	System.out.println("logger thread Interrupted, task "+task+" error "+error);

	}

	@Override
	public void threadFinished(Thread t, String task) {
		System.out.println("logger thread finished, task "+task);

	}

	@Override
	public void queryFinished(TAPExecutionReport report) {
		System.out.println("logger query finished");

	}

	@Override
	public void dbInfo(String message) {
		System.out.println("logger DB info "+message);

	}

	@Override
	public void dbError(String message, Throwable t) {
		System.out.println("logger DB error "+ message+" error "+t.getMessage());

	}

	@Override
	public void tapMetadataFetched(TAPMetadata metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tapMetadataLoaded(TAPMetadata metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void connectionOpened(DBConnection<?> connection, String dbName) {
		System.out.println("logger DB connection opened");

	}

	@Override
	public void connectionClosed(DBConnection<?> connection) {
		System.out.println("logger DB connection closed");

	}

	@Override
	public void transactionStarted(DBConnection<?> connection) {
System.out.println("logger transaction started");
	}

	@Override
	public void transactionCancelled(DBConnection<?> connection) {
System.out.println("logger transaction stopped");
	}

	@Override
	public void transactionEnded(DBConnection<?> connection) {
		System.out.println("loger transaction ended");

	}

	@Override
	public void schemaCreated(DBConnection<?> connection, String schema) {
		// TODO Auto-generated method stub

	}

	@Override
	public void schemaDropped(DBConnection<?> connection, String schema) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tableCreated(DBConnection<?> connection, TAPTable table) {
		// TODO Auto-generated method stub

	}

	@Override
	public void tableDropped(DBConnection<?> connection, TAPTable table) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rowsInserted(DBConnection<?> connection, TAPTable table, int nbInsertedRows) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sqlQueryExecuting(DBConnection<?> connection, String sql) {
		System.out.println("logger sql query executing: "+sql);

	}

	@Override
	public void sqlQueryError(DBConnection<?> connection, String sql, Throwable t) {
		System.out.println("logger sql query error: "+sql+" error "+t.getMessage());

	}

	@Override
	public void sqlQueryExecuted(DBConnection<?> connection, String sql) {
		System.out.println("sql query executed : "+sql);

	}

}
