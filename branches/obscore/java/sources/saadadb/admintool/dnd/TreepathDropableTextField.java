package saadadb.admintool.dnd;

import javax.swing.JTextField;
import javax.swing.tree.TreePath;

/**
 * Abstract Text field component enable to receive data tree path DnD
 * 
 * @author laurentmichel
 * * @version $Id$
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
