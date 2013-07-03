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
					// When a collection is created, the new collection node is selected and expanded
					JTree tree = ((AdminTool)(frame)).metaDataTree.getTree();
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Collection <" + name + "> created");		
					((AdminTool)(frame)).setDataTreePath(new DataTreePath(name, null, null));
					Object[] tab_path; // Contains the path from the node Root to the new collection node
					TreeNode rootNode = (TreeNode) tree.getModel().getRoot();
					TreeNode newCollectionNode = null;
					// Need to find the index of the new collection to select and expand it
					int i;
					for(i=0 ; i<tree.getModel().getChildCount(rootNode) ; i++)
					{
						TreeNode tmp = (TreeNode) tree.getModel().getChild(rootNode, i);
						if (tmp.toString().compareTo(name)==0)
						{
							newCollectionNode = tmp;
						}
					}
					if (newCollectionNode != null) // Only if the new collection was found, if not root node is selected
					{
						tab_path = new Object[2];
						tab_path[1] = newCollectionNode;
					}
					else
					{
						tab_path = new Object[1];
					}
					tab_path[0] = rootNode;
					TreePath path = new TreePath(tab_path);
					tree.setSelectionPath(path);
					tree.expandPath(path);
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
