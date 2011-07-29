package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.collection.ProductManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

/** * @version $Id$

 * @author laurentmichel
 *
 */
public class CmdDeleteProduct extends CmdThread {
	private JTable jtable;
	Object[] tree_path_components;
	private String collection;
	private String category;

	public CmdDeleteProduct(Frame frame, Object[] tree_path_components, JTable jtable) {
		super(frame);
		this.jtable = jtable;
		this.tree_path_components = tree_path_components;
		collection = tree_path_components[1].toString();
		category = tree_path_components[2].toString();
	}
	
	protected boolean getParam() {
		if( jtable.getSelectedRows().length == 0 ) {
			SaadaDBAdmin.showInputError(this.frame, "There is no selcted product!");
			return false;
		}
		return SaadaDBAdmin.showConfirmDialog(frame, "Do you reallly want to delete " + jtable.getSelectedRowCount() + " product(s) ?");
	}
	
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			int[] rows = jtable.getSelectedRows();
			long oids_to_remove[] = new long[rows.length];
			int cpt = 0;
			for( int i: rows ) {
				oids_to_remove[cpt] = Long.parseLong(jtable.getValueAt(i, 0).toString());
				cpt++;
			}
			openProgressDialog();
			saada_process = new ProductManager();
			SQLTable.beginTransaction();
			((ProductManager)saada_process).removeProducts(oids_to_remove);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			((SaadaDBAdmin)(frame)).showProduct(tree_path_components);
			SaadaDBAdmin.showSuccess(frame, rows.length + " products removed");	
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						((SaadaDBAdmin)(frame)).refreshTree(collection, Category.explain(category));
					} catch (SaadaException e) {
					}
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Data Loading successfull");		
				}				
			});

			closeProgressDialog();
		} catch (AbortException e) {
			frame.setCursor(cursor_org);
			closeProgressDialog();
			Messenger.trapAbortException(e);
		} catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "Failed to remove products (see console).");
		}

	}
}
