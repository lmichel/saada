package saadadb.admin.popup;

import java.awt.Frame;

import javax.swing.JMenuItem;

public class PopupCollNode extends PopupNode {

	/**
	 *  * @version $Id$

	 */
	private static final long serialVersionUID = 1L;

	public PopupCollNode(Frame frame, Object[] tree_path_components, String title) {
		super(frame, tree_path_components, title);
		
		JMenuItem item ;
		item = new JMenuItem(COMMENT_COLLECTION);
		this.add(item);
		item.addActionListener(this);
		
		this.addSeparator();
		
		item = new JMenuItem(EMPTY_COLLECTION);
		this.add(item);
		item.addActionListener(this);

		item = new JMenuItem(DELETE_COLLECTION);
		this.add(item);
		item.addActionListener(this);
	}

}
