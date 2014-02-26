package saadadb.admintool.components.input;

import javax.swing.tree.TreePath;

import saadadb.admintool.dnd.TreepathDropableTextField;


/**
 * Textfield which could be set by DnD from the metaTree
 * @author michel
 * @version $Id$
 *
 */
public class UnitTextField extends TreepathDropableTextField {
	private static final long serialVersionUID = 1L;

	public UnitTextField(int size) {
		super(size);
	}

	/**
	 * @param treepath
	 * @param num_node
	 */
	public boolean setText(TreePath treepath) {
		if( valid() ) {
			this.setText(treepath.getLastPathComponent().toString());
			return true;
		} else {
			return false;
		}
	}

	protected boolean valid() {
		return true;
	}

}
