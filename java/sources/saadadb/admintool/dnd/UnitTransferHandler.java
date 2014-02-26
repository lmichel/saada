package saadadb.admintool.dnd;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import saadadb.admintool.components.ResultSetTableModel;
import saadadb.admintool.components.input.UnitTextField;



/**
 * @author michel
 * @version $Id$
 *
 */
public class UnitTransferHandler extends SaadaTransferHandler {
	private static final long serialVersionUID = 1L;
	private int[] rows = null;
	private int addIndex = -1; //Location where items were added
	private int addCount = 0;  //Number of items added.

	protected String exportString(JComponent c) {
		JTable table = (JTable)c;
		rows = table.getSelectedRows();
		int colCount = table.getColumnCount();

		StringBuffer buff = new StringBuffer();

		for (int i = 0; i < rows.length; i++) {
			for (int j = 0; j < colCount; j++) {
				Object val = table.getValueAt(rows[i], j);
				buff.append(val == null ? "" : val.toString());
				if (j != colCount - 1) {
					buff.append(",");
				}
			}
			if (i != rows.length - 1) {
				buff.append("\n");
			}
		}

		return buff.toString();
	}

	/* (non-Javadoc)
	 * @see gui.DnDTransferHandler#importString(javax.swing.JComponent, java.lang.String)
	 */
	protected void importString(JComponent c, String str) {
		int index=0;ResultSetTableModel model=null;JTable target=null;
		try {
			if( c instanceof JTable ) {
				target = (JTable)c;
				model = (ResultSetTableModel)target.getModel();
				index = target.getSelectedRow();
				model.setUnit(str, index);
				target.updateUI();
			} else if( c instanceof UnitTextField ) {
				((UnitTextField) c).setText(str);
			}
		} catch(Exception e) {}
	}

	protected void cleanup(JComponent c, boolean remove) {
		JTable source = (JTable)c;
		if (remove && rows != null) {
			DefaultTableModel model =
				(DefaultTableModel)source.getModel();

			//If we are moving items around in the same table, we
			//need to adjust the rows accordingly, since those
			//after the insertion point have moved.
			if (addCount > 0) {
				for (int i = 0; i < rows.length; i++) {
					if (rows[i] > addIndex) {
						rows[i] += addCount;
					}
				}
			}
			for (int i = rows.length - 1; i >= 0; i--) {
				model.removeRow(rows[i]);
			}
		}
		rows = null;
		addCount = 0;
		addIndex = -1;
	}
}
