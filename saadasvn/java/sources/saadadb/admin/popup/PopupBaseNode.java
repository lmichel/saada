package saadadb.admin.popup;

import java.awt.Frame;

import javax.swing.JMenuItem;

import saadadb.admin.dmmapper.MapperDemo;


public class PopupBaseNode extends PopupNode {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupBaseNode(Frame frame, Object[] tree_path_components, String title) {
		super(frame, tree_path_components, title);
		JMenuItem item = new JMenuItem(CREATE_COLLECTION);
		item.addActionListener(this);
		this.add(item);
		if( frame instanceof MapperDemo ) {
			this.addSeparator();
			item = new JMenuItem(EDIT_DM);
			item.addActionListener(this);
			this.add(item);
		}
//		item = new JMenuItem(DUMMY);
//		item.addActionListener(this);
//		this.add(item);
	}

}
