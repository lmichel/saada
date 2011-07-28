package saadadb.admin.popup;

import java.awt.Frame;

import javax.swing.JMenuItem;
import javax.swing.JTable;

public class PopupProduct extends PopupNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupProduct(Frame frame, Object[] tree_path_components, JTable jtable, String title) {
		super(frame, tree_path_components, jtable, title);
		JMenuItem item ;
		item = new JMenuItem(DELETE_PRODUCTS);
		if( jtable.getSelectedRowCount() == 0 ) {
			item.setEnabled(false);
		}
		this.add(item);
		item.addActionListener(this);		
	}

}
