package saadadb.admin.popup;

import java.awt.Frame;

import javax.swing.JMenuItem;
import javax.swing.JTable;

public class PopupCollEdit extends PopupNode {

	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupCollEdit(Frame frame, Object[] tree_path_components,
			String title) {
		super(frame, tree_path_components, title);
	}

	public PopupCollEdit(Frame frame, Object[] tree_path_components,
			JTable jtable, String title) {
		super(frame, tree_path_components, jtable, title);
		JMenuItem item ;
		item = new JMenuItem(SAVE_MAPPING);
		this.add(item);
		item.addActionListener(this);
		
		item = new JMenuItem(CANCEL_MAPPING);
		this.add(item);
		item.addActionListener(this);	
		
	}

}
