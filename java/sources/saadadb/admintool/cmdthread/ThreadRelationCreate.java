package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.tasks.RelationCreatePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.util.Merger;
import saadadb.util.Messenger;

public class ThreadRelationCreate extends CmdThread {
	protected RelationConf config;
	RelationCreatePanel panel; 
	
	public ThreadRelationCreate(Frame frame, RelationCreatePanel panel, String taskTitle) {
		super(frame, taskTitle);
		this.panel = panel;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		config = (RelationConf) params.get("config");
		if( config != null ){
			resourceLabel = "Relation " + config.getNameRelation();;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( config == null ) {
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
			SQLTable.beginTransaction();
			RelationManager rm = new RelationManager(config);
			rm.create();
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					int userChoice = AdminComponent.showSuccessQuestion(frame, "Relationship <" +config.getNameRelation() + "> created", "Do you want to populate this relation now ?");		
					panel.setSelectedResource(config.getNameRelation(), null);
					if (userChoice == JOptionPane.YES_OPTION)
					{
						((AdminTool)frame).activePanel(AdminComponent.POPULATE_RELATION);
						RelationPopulatePanel panel = (RelationPopulatePanel) (((AdminTool)frame).getActivePanel());
						panel.selectRelation(config.getNameRelation());
					}
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
		String retour = "";
		try {
			retour =  AntDesk.getAntFile(AdminComponent.EMPTY_RELATION
					, taskTitle
					, new String[]{"-create=" + config.getNameRelation() 
					, "-from=" +  config.getColPrimary_name() + "." + Category.explain(config.getColPrimary_type()) 
					, "-to=" +  config.getColSecondary_name() + "." + Category.explain(config.getColSecondary_type()) 
					, "-comment="+ config.getDescription().replaceAll("\"", "") 
					, "-qualifiers=" +  Merger.getMergedArray(config.getQualifier().keySet().toArray(new String[0]))
			});
		} catch (Exception e) {}
		return retour;
	}
}

