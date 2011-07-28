package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import saadadb.admin.SQLJTable;
import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class CmdSaveClass extends CmdThread {
	private SQLJTable sql_table;
	private Object[] tree_path_components;

	public CmdSaveClass(Frame frame, Object[] tree_path_elements, SQLJTable sql_table) {
		super(frame);
		this.sql_table = sql_table;
		tree_path_components = tree_path_elements;
	}
	
	/* (non-Javadoc)
	 * @see gui.CmdThread#getParam()
	 */
	protected boolean getParam() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));	
			SQLTable.beginTransaction();
			sql_table.saveModifiedRows();
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			((SaadaDBAdmin)(frame)).showClass(tree_path_components);		
			SaadaDBAdmin.showSuccess(frame, "Metadata successfully updated");		
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Metadata update failed (see console)");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Metadata update failed (see console)");
		}
	}

}
