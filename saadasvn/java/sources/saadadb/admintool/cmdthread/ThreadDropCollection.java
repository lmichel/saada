package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

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
					// When the collection is removed, the tree node selected is root
					JTree tree = ((AdminTool)(frame)).metaDataTree.getTree();
					tree.setSelectionPath(new TreePath((TreeNode)tree.getModel().getRoot()));
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
		return AntDesk.getAntFile(AdminComponent.DROP_COLLECTION
				, taskTitle
				, new String[]{"-remove=" + name });
	}

	public String toString() {
		return "Drop collection " + this.name;
	}



}
