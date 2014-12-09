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
import saadadb.admintool.panels.MetaDataPanel;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.ClassManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadDropClass extends CmdThread{
	protected String name;

	public ThreadDropClass(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.name = null;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		name = (String) params.get("name");
		resourceLabel = " Class " + name;

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		return (!withConfirm 
				||
				AdminComponent.showConfirmDialog(frame, "Do you really want to drop the class " + name));
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			saada_process = new ClassManager(name);
			
			// Associated class in the cases of ENTRY or TABLES remove action
			final String associatedClass = Database.getCachemeta().getClass(name).getAssociate_class();
			final String associatedCategory = (associatedClass!=null && associatedClass.length()>0?Database.getCachemeta().getClass(associatedClass).getCategory_name():"");
	
			SQLTable.beginTransaction();
			((ClassManager)saada_process).remove(new ArgsParser(new String[]{Messenger.getDebugParam()}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// Delete the removed class node in the Jtree
					MetaDataPanel metaDataTree = ((AdminTool)(frame)).metaDataTree;
					TreePath clickPath = metaDataTree.getClickedTreePath();
					metaDataTree.deleteClassNode(name, clickPath);
					
					// If this is an ENTRY or TABLE class, the associated class is also deleted
					if (associatedClass!=null && associatedClass.length()>0)
					{
						metaDataTree.deleteAssociatedClassNode(clickPath, associatedClass, associatedCategory);
						Messenger.printMsg(Messenger.DEBUG, "AssociatedClass <" + associatedClass + "> in " + associatedCategory + "." + associatedCategory + " removed");
					}
					
					JTree tree = metaDataTree.getTree();
					// When the collection is removed, the tree node selected is root
					tree.setSelectionPath(new TreePath((TreeNode)tree.getModel().getRoot()));
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Class <" + name + "> removed");		
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
		return AntDesk.getAntFile(AdminComponent.DROP_CLASS
				, taskTitle
				, new String[]{"-remove=" + name });
	}

	public String toString() {
		return "Drop class " + this.name;
	}



}
