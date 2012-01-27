package saadadb.admintool.popups;

import java.awt.Container;
import java.awt.Frame;
import java.util.LinkedHashMap;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadDeleteProduct;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.windows.DataTableWindow;
import saadadb.exceptions.SaadaException;


public class PopupProduct extends PopupNode {
	/** * @version $Id: PopupProduct.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupProduct(DataTableWindow jtable, String title) throws SaadaException {
		super(jtable.rootFrame, jtable.getDataTreePath(), title);
		JMenuItem item ;
		item = new JMenuItem(DELETE_PRODUCTS);
		if( jtable.getProductTable().getSelectedRowCount() == 0 ) {
			item.setEnabled(false);
		}
		this.add(item);
		params = new LinkedHashMap<String, Object>();
		params.put("datatable", jtable);
		item.addActionListener(this);		
	}

}
