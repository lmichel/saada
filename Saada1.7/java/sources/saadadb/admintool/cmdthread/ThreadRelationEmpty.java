package saadadb.admintool.cmdthread;

import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadRelationEmpty extends ThreadRelationDrop {

	public ThreadRelationEmpty(Frame frame, String taskTitle) {
		super(frame, taskTitle);
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
			return (!withConfirm 
					||
					AdminComponent.showConfirmDialog(frame, "Do you really want to empty the relation <" + relation+ ">"));
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		try {
			SQLTable.beginTransaction();
			RelationManager rm = new RelationManager(relation);
			rm.empty(new ArgsParser(new String[]{ Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					AdminComponent.showSuccess(frame, "Relationship <" + relation + "> emptied");	
				}
			});
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
		} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			AdminComponent.showFatalError(frame, "Emptying relationship <" +relation + "> failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		return AntDesk.getAntFile(AdminComponent.EMPTY_RELATION
				, taskTitle
				, new String[]{"-empty=" + relation });
	}
}

