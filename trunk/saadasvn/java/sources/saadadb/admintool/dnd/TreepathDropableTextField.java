package saadadb.admintool.dnd;

import javax.swing.JTextField;
import javax.swing.tree.TreePath;

/**
 * @author laurentmichel
 * * @version $Id: TreepathDropableTextField.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
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
