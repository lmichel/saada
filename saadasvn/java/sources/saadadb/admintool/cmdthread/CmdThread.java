package saadadb.admintool.cmdthread;


import java.awt.Frame;

import saadadb.admin.SaadaDBAdmin;
import saadadb.command.SaadaProcess;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;

/**
 * @author laurent
 * @version @Id@
 */
public abstract class CmdThread extends Thread {
	protected Frame frame;
	/* * @version $Id$

	 * Command run by sub classes of saada_process can be paused/resumed
	 * by the progress dialog
	 */
	protected SaadaProcess saada_process;
	
	/**
	 * @param frame
	 */
	public CmdThread(Frame frame) {
		super();
		this.frame = frame;
	}		

	/**
	 * @return
	 */
	public SaadaProcess getProcess() {
		return saada_process;
	}


	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run(){		
		try {
			if( this.getParam() == true ) {
				this.runCommand();
			}
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
	protected boolean getParam() throws QueryException, FatalException, Exception {
		return  SaadaDBAdmin.showConfirmDialog(this.frame, "Are you sure you want to do that?");
	}
	
	/**
	 * 
	 */
	public abstract void runCommand() ;
}
