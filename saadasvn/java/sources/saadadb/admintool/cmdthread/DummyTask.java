package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.SaadaProcess;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * @version $Id$
 */
public class DummyTask extends CmdThread {
	
	public DummyTask(Frame frame) {
		super(frame);
	}
	
	protected boolean getParam() {
		return  true;
	}
		
	public  void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new SaadaProcess();

			saada_process.faitTonBoulot();
			AdminComponent.showSuccess(frame, "Operation successed");		
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

}
