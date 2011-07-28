package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.relationship.IndexBuilder;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class RelationIndexationAll extends CmdThread {

	public RelationIndexationAll(Frame frame) {
		super(frame);
	}

	protected boolean getParam() {
		return SaadaDBAdmin.showConfirmDialog(frame, "Are you sure to index al relationships (can take a piece of time)?");
	}
	
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			openProgressDialog();
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			for(String relation: Database.getCachemeta().getRelation_names()) {
				IndexBuilder ib = new IndexBuilder(Database.getRoot_dir() + Database.getSepar()+ "indexation" + Database.getSepar(), relation);
				this.saada_process = ib;
				Messenger.printMsg(Messenger.TRACE, "Indexing the relation " + relation);
				ib.createIndexRelation();
			}
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Indexing of all relationships successfull\nDon't forget to deploy the Web again .");		
				}
			});
			frame.setCursor(cursor_org);
		} catch (AbortException ae) {
			frame.setCursor(cursor_org);
			Messenger.printStackTrace(ae);
			SaadaDBAdmin.showFatalError(frame, "Indexation of all relationships failed (see console).");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			Messenger.printStackTrace(e);
			SaadaDBAdmin.showFatalError(frame, "Indexation of all relationships failed (see console).");
		}
	}
}
