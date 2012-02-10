package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.relationship.IndexBuilder;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadRelationPopulate extends ThreadRelationCreate {

	public ThreadRelationPopulate(Frame frame, String taskTitle) {
		super(frame, taskTitle);
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
			JCheckBox withEmpty = new JCheckBox("Do you want to first empty the relation?");
			JCheckBox withIndex = new JCheckBox("Do you want to index the relation after?");
			
		      JPanel myPanel = new JPanel();
		      myPanel.add(withEmpty);
		      myPanel.add(withIndex);

		      int result = JOptionPane.showConfirmDialog(frame, myPanel, 
		               "Do you want to proceed?", JOptionPane.OK_CANCEL_OPTION);
		      if (result == JOptionPane.OK_OPTION) {
		         System.out.println("x value: " + withEmpty.getText());
		         System.out.println("y value: " + withIndex.getText());
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
			AdminComponent.showSuccess(frame, "Relationship <" +config.getNameRelation() + "> created");		
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
		// TODO Auto-generated method stub
		return null;
	}
}
