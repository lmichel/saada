package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.windows.DataTableWindow;
import saadadb.collection.ProductManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;

/** * @version $Id: CmdDeleteProduct.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @author laurentmichel
 *
 */
public class ThreadDeleteProduct extends CmdThread {
	private DataTableWindow dataTable;

	public ThreadDeleteProduct(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}
	
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		dataTable = (DataTableWindow) params.get("datatable");
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( dataTable == null ) {
			AdminComponent.showFatalError(frame, "No JTable where to read oids of products to remove (Inner error)");
			return  false;
		}
		else if( dataTable.getProductTable().getSelectedColumnCount() == 0 ) {
			AdminComponent.showFatalError(frame, "There is no selected rows: no product to remove");
			return false;
		}
		else {
			return (!withConfirm
					||
					AdminComponent.showConfirmDialog(frame, "Do you really want to remove these " + dataTable.getProductTable().getSelectedColumnCount() + " products"));
		}
	}


	
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			final int[] rows = dataTable.getProductTable().getSelectedRows();
			long oids_to_remove[] = new long[rows.length];
			int cpt = 0;
			for( int i: rows ) {
				oids_to_remove[cpt] = Long.parseLong(dataTable.getProductTable().getValueAt(i, 0).toString());
				cpt++;
			}
			saada_process = new ProductManager();
			SQLTable.beginTransaction();
			((ProductManager)saada_process).removeProducts(oids_to_remove);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dataTable.refresh();
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, rows.length + " products removed");		
				}				
			});

		} catch (AbortException e) {
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}catch (Exception e) {
			SQLTable.abortTransaction();
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, e);
		}


	}

	@Override
	public String getAntTarget() {
		return "product remove";
	}
}
