package saadadb.admintool.cmdthread;


import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.collection.ClassManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadCommentClass extends ThreadCreateCollection {

	public ThreadCommentClass(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.name = null;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		name = (String) params.get("name");
		comment = (String) params.get("comment");	
		resourceLabel = "Class " + name;

	}


	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		try {
			saada_process = new ClassManager(name);
			SQLTable.beginTransaction();
			((ClassManager)saada_process).comment(new ArgsParser(new String[]{"-comment=" +comment, Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Desciption set for class <" + name  + ">");		
				}				
			});
		} catch (SaadaException e) {
			Messenger.trapFatalException(e);
		}	

	}

	@Override
	public String getAntTarget() {
		Messenger.printMsg(Messenger.DEBUG, "No ANT task in CommentClass !");
		return null;
	}
	public String toString() {
		return "Comment class " + this.name;
	}

}
