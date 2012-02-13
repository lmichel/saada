package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadCommentCollection extends ThreadCreateCollection {

	public ThreadCommentCollection(Frame frame, String taskTitle) {
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
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new CollectionManager(name);
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).comment(new ArgsParser(new String[]{"-comment=" +comment, Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Desciption set for collection <" + name  + ">");		
				}				
			});
		} catch (SaadaException e) {
			Messenger.trapFatalException(e);
		}	

	}

	@Override
	public String getAntTarget() {
		return "No Ant task available to comment any entity.\nmust be done either at creagtion time or from the admintool";
	}
	public String toString() {
		return "Comment collection " + this.name;
	}

}
