package saadadb.admin.dnd;

import javax.swing.JTextField;
import javax.swing.tree.TreePath;

public abstract class TreepathDropableTextField extends JTextField{
		protected TreePath treepath;

	public TreepathDropableTextField() {
		super();
	}
	
	public TreepathDropableTextField(int nb_col) {
		super(nb_col);
	}
	/**
	 * @param treepath
	 * @param num_node
	 */
	abstract public boolean setText(TreePath treepath) ;
	
	abstract protected boolean valid();

}
