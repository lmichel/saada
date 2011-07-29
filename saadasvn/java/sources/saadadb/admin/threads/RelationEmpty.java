package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.relationship.RelationManager;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class RelationEmpty extends RelationIndexation {
	
	public RelationEmpty(Frame frame, String relation) {
		super(frame, relation);
	}
	
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			RelationManager rm = new RelationManager(relation);
			SQLTable.beginTransaction();
			rm.empty();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showSuccess(frame, "Relation <" + relation + "> empty\nDon't forget to deploy the Web again .");		
		} catch (AbortException ae) {
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Emptying Relationship <" +relation + "> failed (see console).");
			SQLTable.abortTransaction();
		} catch (Exception e) {
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Emptying Relationship <" +relation + "> failed (see console).");
		}
	}

}
