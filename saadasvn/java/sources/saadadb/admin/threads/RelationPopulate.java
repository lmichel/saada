package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.relation.RelationConfPanel;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.relationship.IndexBuilder;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class RelationPopulate extends CmdThread {
	protected String rel_name;
	protected String query;
	private RelationConfPanel config_panel;
	
	/** * @version $Id$

	 * @param frame
	 * @param rel_conf
	 */
	public RelationPopulate(Frame frame, String rel_name, String query, RelationConfPanel config_panel) {
		super(frame);
		this.rel_name = rel_name;
		this.query    = query;
		this.config_panel = config_panel;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	protected void runCommand() {
		Messenger.setMaxProgress(10);
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			openProgressDialog();
			RelationManager rm = new RelationManager(rel_name);
			this.saada_process = rm;
			rm.getRelation_conf().setQuery(query);
			rm.getRelation_conf().save();
			Messenger.printMsg(Messenger.TRACE, "Running the correlator");
			SQLTable.beginTransaction();
			rm.populateWithQuery();
			IndexBuilder ib = new IndexBuilder(Repository.getIndexrelationsPath() + Database.getSepar(), rel_name);
			ib.createIndexRelation();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					RelationPopulate.this.config_panel.index_button.setEnabled(true);
					RelationPopulate.this.config_panel.empty_button.setEnabled(true);
					RelationPopulate.this.config_panel.remove_button.setEnabled(true);
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Populating of Relationship <" +rel_name + "> successfull\nDon't forget to deploy the Web again .");		
				}
			});
			frame.setCursor(cursor_org);
		} catch(AbortException ae ) {
			frame.setCursor(cursor_org);
			closeProgressDialog();
			Messenger.printStackTrace(ae);
			SaadaDBAdmin.showFatalError(frame, "Populating of Relationship <" +rel_name + "> failed: " + ae.getMessage() + " (see console).");
			
		} catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			closeProgressDialog();
			Messenger.printStackTrace(e);
			SaadaDBAdmin.showFatalError(frame, "Populating of Relationship <" +rel_name + "> failed: " + e.getMessage() + " (see console).");
		}
	}

}
