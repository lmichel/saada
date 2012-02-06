package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.ArgsParser;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadRelationDrop extends CmdThread {
	private String relation;
	
	public ThreadRelationDrop(Frame frame) {
		super(frame);
	}
	
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		relation = (String) params.get("relation");
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams() {
		if( relation == null ) {
			return false;
		}
		else {
			return AdminComponent.showConfirmDialog(frame, "Do you really want to drop the relation <" + relation+ ">");
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			SQLTable.beginTransaction();
			RelationManager rm = new RelationManager(relation);
			rm.remove(new ArgsParser(new String[]{ Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			AdminComponent.showSuccess(frame, "Relationship <" + relation + "> removed");		
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
			} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Creating relationship <" +relation + "> failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		// TODO Auto-generated method stub
		return null;
	}
}

