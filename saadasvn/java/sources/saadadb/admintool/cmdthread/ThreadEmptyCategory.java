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
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadEmptyCategory extends ThreadDropCollection{
	protected String collection;
	protected String category;

	public ThreadEmptyCategory(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.collection = null;
		this.category = null;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		collection = (String) params.get("collection");
		category = (String) params.get("category");
		resourceLabel = "Collection " + collection + "." + category;;

	}


	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( collection == null ||category == null ) {
			AdminComponent.showInputError(frame, "Both collection and category mustbe given");
			return false;
		}
		else {
			return (!withConfirm 
					||
					AdminComponent.showConfirmDialog(frame
					, "Do you really want to empty the content of the category " + collection + "." + category));
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new CollectionManager(collection);
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).empty(new ArgsParser(new String[]{"-category=" + category, Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree(collection, category);
					AdminComponent.showSuccess(frame, "Collection <" + collection + "." + category + "> emptied");		
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
		return "Empty collection " + this.name;
	}



}
