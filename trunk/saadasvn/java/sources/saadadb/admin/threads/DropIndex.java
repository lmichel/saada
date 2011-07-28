package saadadb.admin.threads;


import java.awt.Cursor;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.SQLIndexPanel;
import saadadb.command.ManageTableIndex;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class DropIndex extends CmdThread {
	private String table;
	String[] columns = null;
	
	public DropIndex(SQLIndexPanel frame, String table, String[] columns) {
		super(frame);
		this.table = table;
		this.columns = columns;
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
		openProgressDialog();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process  = new ManageTableIndex(table);
			SQLTable.beginTransaction(true);
			if( columns == null ) {
				((ManageTableIndex)saada_process).dropTableIndex();
			}
			else {
				for(String c: columns) {
					((ManageTableIndex)saada_process).dropTableColumnIndex(c);
				}
			}
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {					
				public void run() {
					try {
						((SQLIndexPanel)frame).update();
					} catch (Exception e) {
						SaadaDBAdmin.showFatalError(frame, e);
					}
				}
			});
			closeProgressDialog();
			SaadaDBAdmin.showSuccess(frame, "Indexes of table <" +  table + "> successfully dropped");		
			frame.setCursor(cursor_org);
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Index drop failed for table <" + table + "> (see console)");
		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Index drop failed for table <" + table + "> (see console)");
		}
	}

}
