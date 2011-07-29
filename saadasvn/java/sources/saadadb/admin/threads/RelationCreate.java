package saadadb.admin.threads;

import java.awt.Cursor;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.relation.RelationConfPanel;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class RelationCreate extends RelationIndexation {
	private RelationConf config;
	private RelationConfPanel config_panel;
	
	/** * @version $Id$

	 * @param frame
	 * @param conf
	 */
	public RelationCreate(RelationConfPanel config_panel, RelationConf conf) {
		super(config_panel.frame, conf.getNameRelation());
		this.config = conf;
		this.config_panel = config_panel;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			/*
			 * TODO No longer use relation.xml
			 */	
			//config.save();
			SQLTable.beginTransaction();
			RelationManager rm = new RelationManager(config);
			rm.create();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					RelationCreate.this.config_panel.mapping_panel.setEditable(false);
					RelationCreate.this.config_panel.mapping_panel.query_panel.setEditable(true);
					/*
					 * Load operation can disable KNN widgets: must be done after setEditable...
					 */
					try {
						RelationCreate.this.config_panel.mapping_panel.load(config);
					} catch (SaadaException e) {
						SaadaDBAdmin.showFatalError(frame, e);
					}
					RelationCreate.this.config_panel.create_button.setEnabled(false);
					RelationCreate.this.config_panel.populate_button.setEnabled(true);
					RelationCreate.this.config_panel.index_button.setEnabled(false);
					RelationCreate.this.config_panel.empty_button.setEnabled(false);
					RelationCreate.this.config_panel.remove_button.setEnabled(true);		
					RelationCreate.this.config_panel.mapping_panel.query_panel.setBackground(SaadaDBAdmin.beige_color);
					RelationCreate.this.config_panel.mapping_panel.query_panel.initMetaData();
				}
			});
			SaadaDBAdmin.showSuccess(frame, "Relation <" + relation + "> created\nDon't forget to deploy the Web again .");		
		} catch (AbortException e) {			
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Creating relationship <" +relation + "> failed (see console).");
		} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Creating relationship <" +relation + "> failed (see console).");
		}
	}

}

