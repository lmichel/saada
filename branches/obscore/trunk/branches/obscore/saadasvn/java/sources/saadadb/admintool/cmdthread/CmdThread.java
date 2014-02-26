package saadadb.admintool.cmdthread;


import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.SaadaProcess;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;

/**
 * @author laurent
 * @version @Id@
 */
public abstract class CmdThread extends Thread {
	protected Frame frame;
	public final String taskTitle;
	protected String resourceLabel = "No specific resource";
	/** Command run by sub classes of saada_process can be paused/resumed
	 * by the progress dialog
	 */
	protected SaadaProcess saada_process;
	
	/**
	 * @param frame
	 */
	public CmdThread(Frame frame, String taskTitle) {
		super();
		this.frame = frame;
		this.taskTitle = taskTitle;
	}		

	public String getResourceLabel() {
		return resourceLabel;
	}
	/**
	 * @return
	 */
	public SaadaProcess getProcess() {
		return saada_process;
	}

	/**
	 * Take params reveived from the task panel
	 * @param params
	 * @throws SaadaException
	 */
	public abstract void setParams(Map<String, Object> params) throws SaadaException;
	
	/**
	 * Returns the ANT target doing the task in script mode
	 * @return
	 */
	public abstract String getAntTarget() ;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run(){		
		try {
			//if( this.checkParams() == true ) {
				this.runCommand();
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
	 * @throws QueryException 
	 * @throws FatalException 
	 * @throws Exception 
	 */
	public boolean checkParams(boolean withConfirm) throws QueryException, FatalException, Exception {
		return  (!withConfirm || AdminComponent.showConfirmDialog(this.frame, "Are you sure you want to do that?"));
	}
	
	public void wakeUp() {
		saada_process.wakeUp();
	}
		
	public boolean isRunning() {
		return (this.getState().equals(Thread.State.RUNNABLE)  ||
				this.getState().equals(Thread.State.TIMED_WAITING) );
	}
	
	public boolean isWaiting() {
		return (this.getState().equals(Thread.State.WAITING) );
	}
	
	public boolean isCompleted() {
		return (this.getState().equals(Thread.State.TERMINATED) );
	}

/**
	 * 
	 */
	public abstract void runCommand() ;
}
