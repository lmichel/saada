package saadadb.admintool.popups;

import java.awt.Container;
import java.awt.Frame;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import saadadb.admintool.AdminTool;

public class PopupClassEdit extends PopupNode {

	/** * @version $Id: PopupClassEdit.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PopupClassEdit(AdminTool rootFrame, Object[] tree_path_components,
			String title) {
		super(rootFrame, tree_path_components, title);
	}

	public PopupClassEdit(AdminTool rootFrame, Object[] tree_path_components,
			JTable jtable, String title) {
		super(rootFrame, tree_path_components, jtable, title);
		JMenuItem item ;
		item = new JMenuItem(MAP_META);
		this.add(item);
		item.addActionListener(this);	
				
		this.addSeparator();
		
		item = new JMenuItem(SAVE_MAPPING);
		this.add(item);
		item.addActionListener(this);
		
		item = new JMenuItem(CANCEL_MAPPING);
		this.add(item);
		item.addActionListener(this);	
		
		this.addSeparator();
//		item = new JMenuItem(BUILD_INDEX);
//		item.addActionListener(this);
//		this.add(item);

	}

}
