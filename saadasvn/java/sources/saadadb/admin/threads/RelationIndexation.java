package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.relationship.IndexBuilder;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class RelationIndexation extends CmdThread {
	protected String relation;
	
	public RelationIndexation(Frame frame, String relation) {
		super(frame);
		this.relation =relation;
	}
	
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			openProgressDialog();
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			IndexBuilder ib = new IndexBuilder(Repository.INDEXRELATIONS_PATH  + Database.getSepar(), relation);
			this.saada_process = ib;
			Messenger.printMsg(Messenger.TRACE, "Indexing the relationr");
			SQLTable.beginTransaction();
			ib.createIndexRelation();
			SQLTable.commitTransaction();
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Indexing of Relationship <" +relation + "> successfull\nDon't forget to deploy the Web again .");		
				}
			});
			frame.setCursor(cursor_org);
		} catch (AbortException ae) {
			frame.setCursor(cursor_org);
			Messenger.printStackTrace(ae);
			SaadaDBAdmin.showFatalError(frame, "Indexation of Relationship <" +relation + "> failed (see console).");
		} catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			Messenger.printStackTrace(e);
			SaadaDBAdmin.showFatalError(frame, "Indexation of Relationship <" +relation + "> failed (see console).");
		}
	}

}
