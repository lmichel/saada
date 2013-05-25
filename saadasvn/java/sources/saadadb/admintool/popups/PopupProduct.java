package saadadb.admintool.popups;

import java.util.LinkedHashMap;

import javax.swing.JMenuItem;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.utils.DataTreePath;
import saadadb.exceptions.SaadaException;


public class PopupProduct extends PopupNode {
	/** * @version $Id: PopupProduct.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupProduct(AdminTool rootFrame, DataTreePath dataTreePath, SQLJTable sqlTable , String title) throws SaadaException {
		super(rootFrame, dataTreePath, title);
		JMenuItem item ;
		item = new JMenuItem(DELETE_PRODUCTS);
		if( sqlTable.getSelectedRowCount() == 0 ) {
			item.setEnabled(false);
		}
		this.add(item);
		params = new LinkedHashMap<String, Object>();
		params.put("datatable", sqlTable.getCmdThreadParam());
		item.addActionListener(this);		
	}

}
