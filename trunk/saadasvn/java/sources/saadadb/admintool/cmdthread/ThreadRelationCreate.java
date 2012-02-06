package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadRelationCreate extends CmdThread {
	private RelationConf config;
	
	public ThreadRelationCreate(Frame frame) {
		super(frame);
	}
	
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		config = (RelationConf) params.get("config");
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams() {
		System.out.println("config" + config);
		if( config == null ) {
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
			RelationManager rm = new RelationManager(config);
			rm.create();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			AdminComponent.showSuccess(frame, "Relationship <" +config.getNameRelation() + "> created");		
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
			} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Creating relationship <" +config.getNameRelation() + "> failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		// TODO Auto-generated method stub
		return null;
	}
}

