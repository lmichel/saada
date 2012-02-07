package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ThreadDropCollection extends CmdThread{
	protected String name;

	public ThreadDropCollection(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.name = null;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		name = (String) params.get("name");
		resourceLabel = "Collection " + name;

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		return (!withConfirm 
				||
				AdminComponent.showConfirmDialog(frame, "Do you really want to drop the content of the collection " + name));
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new CollectionManager(name);
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).remove(new ArgsParser(new String[]{Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Collection <" + name + "> removed");		
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
		return "ANT target for " + this.name;
	}

	public String toString() {
		return "Drop collection " + this.name;
	}



}
