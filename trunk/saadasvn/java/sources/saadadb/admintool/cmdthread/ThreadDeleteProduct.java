package saadadb.admintool.cmdthread;


import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.panels.tasks.DataTableEditorPanel;
import saadadb.admintool.utils.AntDesk;
import saadadb.collection.ProductManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/** * @version $Id: CmdDeleteProduct.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 * @author laurentmichel
 *
 */
public class ThreadDeleteProduct extends CmdThread {
	private Frame frame;
	private DataTableEditorPanel dataTableEditor;
	long oids_to_remove[] ;
	private boolean isLinksFollow;

	public ThreadDeleteProduct(Frame frame, String taskTitle) {
		super(frame, taskTitle);
		this.frame = frame;
	}
	
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException 
	{	
		dataTableEditor = (DataTableEditorPanel) params.get("datatable");
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		JCheckBox linksFollowCheckBox = new JCheckBox("Also remove all products targeted by links of the product");
		linksFollowCheckBox.setSelected(false);
		isLinksFollow = false;
		if( dataTableEditor == null ) {
			AdminComponent.showFatalError(frame, "No JTable where to read oids of products to remove (Inner error)");
			return  false;
		}
		else if( dataTableEditor.getProductTable().getSelectedColumnCount() == 0 ) {
			AdminComponent.showFatalError(frame, "There is no selected rows: no product to remove");
			return false;
		}
		else {
			int nbSelectedProducts = dataTableEditor.getProductTable().getSelectedRowCount();
			boolean tmp = (!withConfirm
					||
					AdminComponent.showConfirmDialog(frame, "Do you really want to remove these " + nbSelectedProducts + " product" + (nbSelectedProducts>1?"s":"") + " ?", new Component[] {new JLabel("Do you really want to remove these " + nbSelectedProducts + " product" + (nbSelectedProducts>1?"s":"") + " ?"),linksFollowCheckBox}));
			isLinksFollow = linksFollowCheckBox.isSelected();
			return tmp;
		}
	}

	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			final int[] rows = dataTableEditor.getProductTable().getSelectedRows();
			long oids_to_remove[] = new long[rows.length];
			int cpt = 0;
			for( int i: rows ) {
				oids_to_remove[cpt] = Long.parseLong(dataTableEditor.getProductTable().getValueAt(i, 1).toString());
				cpt++;
			}
			saada_process = new ProductManager();
			SQLTable.beginTransaction();
			
			String[] tab_args;
			if (isLinksFollow)
			{
				tab_args = new String[3];
				tab_args[2]= "-links=follow";
			}
			else
			{
				tab_args = new String[2];
			}
			tab_args[0] = "-remove=";
			for (int i=0 ; i<oids_to_remove.length ; i++)
			{
				tab_args[0] += (i==0?"":",") + oids_to_remove[i] + "";
			}
			tab_args[1]= "-noindex=true";
			
			ArgsParser args = new ArgsParser(tab_args);
			((ProductManager)saada_process).remove(args);
			
			SQLTable.commitTransaction();
			Database.getCachemeta().reloadGraphical(frame, true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dataTableEditor.refresh();
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
		return AntDesk.getAntFile(AdminComponent.REMOVE_PRODUCT
				, taskTitle
				, new String[]{"-remove=" + oids_to_remove });
	}

}
