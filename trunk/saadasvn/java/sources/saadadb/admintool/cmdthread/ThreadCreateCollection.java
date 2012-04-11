package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ThreadCreateCollection extends CmdThread {
	protected String name;
	protected String comment;

	public ThreadCreateCollection(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.name = null;
	}


	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		name = (String) params.get("name");
		comment = (String) params.get("comment");		
		resourceLabel = "Collection " + name;
	}


	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( name == null ) {
			AdminComponent.showFatalError(frame, "No collection name given");
			return false;
		}
		else if( !name.matches(RegExp.COLLNAME)) {
			AdminComponent.showFatalError(frame, "Wrong collection name");
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
			saada_process = new CollectionManager(name);
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).create(new ArgsParser(new String[]{"-comment=" +comment, Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Collection <" + name + "> created");		
					((AdminTool)(frame)).setDataTreePath(new DataTreePath(name, null, null));
				}				
			});
		} catch (AbortException e) {
			Messenger.trapFatalException(e);
		}catch (Exception e) {
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}
	}

	@Override
	public String getAntTarget() {
		return AntDesk.getAntFile(AdminComponent.CREATE_COLLECTION
				, taskTitle
				, new String[]{"-create=" + name + "", "-comment="+ comment.replaceAll("\"", "") + ""});
	}

	public String toString() {
		return "Create collection " + this.name;
	}

}
