package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.vo.tap.DmServiceManager;

public class ThreadDmViewPopulate extends CmdThread {
	private VOResource vor;
	private String className;

	public ThreadDmViewPopulate(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		vor = (VOResource) params.get("dm");
		className = (String) params.get("class");

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( vor == null || className == null ) {
			return false;
		}		
		try {
			if( Database.getCachemeta().getClass(className) == null ){
				return false;
			}
		} catch (FatalException e) {
			return false;
		}		
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			SQLTable.beginTransaction();
			DmServiceManager dsm = new DmServiceManager(vor);
			dsm.populate(new ArgsParser(new String[]{"-populate=" + className, Messenger.getDebugParam()}));
			SQLTable.beginTransaction();
			SQLTable.indexTable(vor.getName(), dsm);
			SQLTable.commitTransaction();
			AdminComponent.showSuccess(frame, "Class " + className + " added to the view of the DM " + vor.getName());		
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
		} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Pushing class " +  className + " in DM " + vor.getName() + " failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		return "NO ant task available";
	}
}

