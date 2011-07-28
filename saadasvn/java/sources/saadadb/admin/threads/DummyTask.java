package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.command.SaadaProcess;
import saadadb.util.Messenger;

public class DummyTask extends CmdThread {
	
	public DummyTask(Frame frame) {
		super(frame);
	}
	
	protected boolean getParam() {
		return  true;
	}
		
	protected  void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new SaadaProcess();

			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					openProgressDialog();
				}
			});
			saada_process.faitTonBoulot();
			closeProgressDialog();
			SaadaDBAdmin.showSuccess(frame, "Operation successed");		
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "operation failed.");
		}
	}

}
