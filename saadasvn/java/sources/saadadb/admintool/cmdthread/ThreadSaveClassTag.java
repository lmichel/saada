package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.utils.DataTreePath;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadSaveClassTag extends CmdThread {
	private SQLJTable sqlTable;

	public ThreadSaveClassTag(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		this.sqlTable = (SQLJTable) params.get("datatable");
		if( this.sqlTable != null ) {
			resourceLabel = this.sqlTable.getDataTreePath().toString();
		}

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( sqlTable == null ) {
			AdminComponent.showFatalError(frame, "No meta data table to save (Inner error)");
			return  false;
		}
		else {
			return (!withConfirm 
					||
					AdminComponent.showConfirmDialog(frame, "Do you really want to save meta dat tag"));
		}
	}


	/* (non-Javadoc)
	 * @see gui.CmdThread#getParam()
	 */
	protected boolean getParam() {
		return true;
	}

	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	public void runCommand() {
		try {
			SQLTable.beginTransaction();
			sqlTable.saveModifiedRows();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Meta data saved");		
				}				
			});
		} catch (AbortException e) {
			Messenger.trapFatalException(e);
		}catch (Exception e) {
			AdminComponent.showFatalError(frame, e);
		}
	}

	@Override
	public String getAntTarget() {
		return "No Ant task available to save meta data.\nmust be done from the admintool";
	}
}
