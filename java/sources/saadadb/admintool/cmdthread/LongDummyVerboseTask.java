package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.SaadaProcess;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @version $Id$
 */
public class LongDummyVerboseTask extends CmdThread {
	
	public LongDummyVerboseTask(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}
	
	public boolean checkParams() {
		return  true;
	}
		
	public  void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new SaadaProcess();

			saada_process.faitTonLongBoulotVerbeux();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					AdminComponent.showSuccess(frame, "Operation successed");		
				}
			});
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "operation failed.");
		}
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAntTarget() {
		return null;
	}

}
