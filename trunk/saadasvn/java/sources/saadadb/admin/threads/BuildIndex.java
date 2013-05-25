package saadadb.admin.threads;


import java.awt.Cursor;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.SQLIndexPanel;
import saadadb.command.ManageTableIndex;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class BuildIndex extends CmdThread {
	private String table;
	String[] columns = null;

	
	public BuildIndex(SQLIndexPanel frame, String table, String[] columns) {
		super(frame);
		this.table = table;
		this.columns = columns;
	}
	/* (non-Javadoc)
	 * @see gui.CmdThread#getParam()
	 */
	@Override
	protected boolean getParam() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		openProgressDialog();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));	
			saada_process  = new ManageTableIndex(table);
			SQLTable.beginTransaction();
			if( columns == null ) {
				((ManageTableIndex)saada_process).indexTable();
			}
			else {
				for(String c: columns) {
					((ManageTableIndex)saada_process).indexTableColumn(c);
				}
			}
			SQLTable.commitTransaction();
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
			SaadaDBAdmin.showSuccess(frame, "Table <" +  table + "> successfully indexed");		
			frame.setCursor(cursor_org);
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "<HTML>Index creation failed for table " + table + "<BR>" + SaadaException.toHTMLString(ae));
		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "<HTML>Index creation failed for table " + table + "<BR>" + SaadaException.toHTMLString(e));
		}
	}

}
