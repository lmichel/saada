package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.ClassManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadEmptyClass extends ThreadDropCollection{

	public ThreadEmptyClass(Frame frame, String taskTitle) {
		super(frame,taskTitle);
		this.name = null;
	}

		/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		return (!withConfirm
				||
				AdminComponent.showConfirmDialog(frame, "Do you really want to empty the class " + name));
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new ClassManager(name);
			SQLTable.beginTransaction();
			((ClassManager)saada_process).empty(new ArgsParser(new String[]{Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree(name);
					AdminComponent.showSuccess(frame, "Class <" + name + "> emptied");		
				}				
			});
		} catch (AbortException e) {
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}
	}


	@Override
	public String getAntTarget() {
		return AntDesk.getAntFile(AdminComponent.EMPTY_CLASS
				, taskTitle
				, new String[]{"-empty=" + name });
	}

	public String toString() {
		return "Empty class " + this.name;
	}



}
