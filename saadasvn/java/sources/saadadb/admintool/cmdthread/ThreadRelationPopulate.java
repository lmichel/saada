package saadadb.admintool.cmdthread;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.relationship.IndexBuilder;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadRelationPopulate extends ThreadRelationCreate {
	private static boolean emptyFirst = false;
	private static  boolean indexAfter = true;
	
	public ThreadRelationPopulate(Frame frame, String taskTitle) {
		super(frame, null, taskTitle);
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( config == null ) {
			return false;
		}
		else {
			JCheckBox withEmpty = new JCheckBox("Do you want to first empty the relation " + config.getNameRelation() + " ?");
			withEmpty.setSelected(emptyFirst);
			JCheckBox withIndex = new JCheckBox("Do you want to index the relation " + config.getNameRelation() + " after?");
			withIndex.setSelected(indexAfter);
			boolean retour = AdminComponent.showConfirmDialog(frame, "Do you want to proceed?", new Component[] {withEmpty, withIndex});
			
			if( retour ) {
				emptyFirst = withEmpty.isSelected();
				indexAfter = withIndex.isSelected();
				return true;
		      }
		
		      return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			RelationManager rm = new RelationManager(config);
			SQLTable.beginTransaction();
			rm.populateWithQuery();
			IndexBuilder ib = new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), config.getNameRelation());
			ib.createIndexRelation();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					AdminComponent.showSuccess(frame, "Relationship <" +config.getNameRelation() + "> populated");
				}
			});
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
			} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Creating relationship <" +config.getNameRelation() + "> failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		return AntDesk.getAntFile(AdminComponent.EMPTY_RELATION
				, taskTitle
				, new String[]{"-populate=" + config.getNameRelation() 
				, "-query=" + config.getQuery().replaceAll("\"", "\\\"")});
	}
}
