package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.collection.CollectionManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class CmdEmptyCollection extends CmdThread {
	private String name;

	/** * @version $Id$

	 * @param frame
	 * @param tree_path_elements
	 */
	public CmdEmptyCollection(Frame frame, Object[] tree_path_elements) {
		super(frame);
		name = tree_path_elements[tree_path_elements.length - 1].toString();
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	protected boolean getParam() throws Exception {
		String inst_msg = "";
		SQLQuery squery = new SQLQuery();

		ResultSet rs = squery.run("select count(*) from saada_loaded_file where collection = '" + name + "'");
		try {
			while( rs.next() ) {
				int nb;
				if( (nb = rs.getInt(1)) != 0 ) {
					squery.close();
					return SaadaDBAdmin.showConfirmDialog(frame, "The collection <" + name + "> contains " + nb + " product files:\n"
							+ inst_msg 
							+ "Do you want to clear it anyway ?");				
				}
				squery.close();
				return true;
			}
		} catch (SQLException e) {			
			Messenger.printStackTrace(e);
			squery.close();
			SaadaDBAdmin.showFatalError(null, e.toString());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new CollectionManager(name);
			openProgressDialog();
			SQLTable.beginTransaction(true);
			((CollectionManager)saada_process).empty(null);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					for( int i=1 ; i<Category.NB_CAT ; i++) {
						((SaadaDBAdmin)(frame)).refreshTree(name, Category.NAMES[i]);
					}
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Collection <" + name + "> emptied");
				}
				});
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "Collection Emptying failed (see console).");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "Collection Emptying failed (see console).");
		}
	}
}
