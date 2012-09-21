package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.TaskPanel;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * Runs the thread saving the comment associated to one relationship
 * @author michel
 * @version $Id$
 *
 */
public class ThreadRelationComment extends CmdThread {
	protected String relation;
	protected String comment;
	protected TaskPanel taskPanel;

	public ThreadRelationComment(Frame frame, TaskPanel taskPanel,String taskTitle) {
		super(frame, taskTitle);
		this.taskPanel = taskPanel;
	}
	
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		relation = (String) params.get("relation");
		comment = (String) params.get("comment");
		resourceLabel = "Relation " + relation;

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( relation == null ) {
			return false;
		}
		else {
			return true;
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
			rm.comment(new ArgsParser(new String[]{"-comment=" +comment}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			taskPanel.cancelChanges();
			AdminComponent.showSuccess(frame, "Relationship <" + relation + "> commented");		
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
			} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Commenting relationship <" +relation + "> failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		return null;
	}
}

