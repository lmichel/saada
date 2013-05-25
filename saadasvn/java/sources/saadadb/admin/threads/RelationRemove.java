package saadadb.admin.threads;

import java.awt.Cursor;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.relation.RelationConfPanel;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;

/** * @version $Id$

 * @author laurentmichel
 *
 */
public class RelationRemove extends RelationIndexation {
	private RelationConfPanel config_panel;

	public RelationRemove(RelationConfPanel config_panel, String relation) {
		super(config_panel.frame, relation);
		this.config_panel = config_panel;
	}

	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			RelationManager rm = new RelationManager(relation);
			SQLTable.beginTransaction(true);
			rm.remove();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					RelationRemove.this.config_panel.mapping_panel.setEditable(false);
					RelationRemove.this.config_panel.mapping_panel.reset();
					RelationRemove.this.config_panel.mapping_panel.paintInGray();
					RelationRemove.this.config_panel.mapping_panel.query_panel.setEditable(false);
					RelationRemove.this.config_panel.create_button.setEnabled(false);
					RelationRemove.this.config_panel.populate_button.setEnabled(false);
					RelationRemove.this.config_panel.index_button.setEnabled(false);
					RelationRemove.this.config_panel.empty_button.setEnabled(false);
					RelationRemove.this.config_panel.remove_button.setEnabled(false);		
					RelationRemove.this.config_panel.mapping_panel.query_panel.setBackground(SaadaDBAdmin.gray_color);
				}
			});
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showSuccess(frame, "Relation <" + relation + "> removed\nDon't forget to deploy the Web again .");		
		} catch (AbortException ae) {
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, ae);
		} catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, e);
		}
	}

}
