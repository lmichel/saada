package saadadb.admin;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


class ColorCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		SQLJTable ct=(SQLJTable)table;
		if(ct.hasModifiedItem(row) ){
			this.setBackground(Color.cyan);
		}
		else {
			this.setBackground(Color.white);
		}
		return this;
	}
}