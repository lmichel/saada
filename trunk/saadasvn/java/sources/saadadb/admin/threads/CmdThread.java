package saadadb.admin.threads;


import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.ProgressDialog;
import saadadb.command.SaadaProcess;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;

public abstract class CmdThread extends Thread {
	protected Frame frame;
	/* * @version $Id$

	 * Command run by sub classes of saada_process can be paused/resumed
	 * by the progress dialog
	 */
	protected SaadaProcess saada_process;
	private ProgressDialog progress_dialog;

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

	/**
	 * 
	 */
	public void openProgressDialog() {
		progress_dialog = new ProgressDialog(frame, "Progress", CmdThread.this);				
	}

	/**
	 * 
	 */
	protected void closeProgressDialog() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if( progress_dialog != null ) {
					progress_dialog.finish();
				}
			}
		});
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
	protected abstract void runCommand() ;
}


