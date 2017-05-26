package saadadb.admintool.components.input;

import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

import saadadb.admintool.dnd.TreepathDropableTextField;


/**
 * Textfield which could be set by DnD from the metaTree
 * The UCD or Utype is extracted from text whereas the description is appended to the JTextComponent
 * @author michel
 * @version $Id$
 *
 */
public class UcdTextField extends TreepathDropableTextField {
	private static final long serialVersionUID = 1L;
	private JTextComponent helpArea = null;

	public UcdTextField(int size, JTextComponent helpArea) {
		super(size);
		this.helpArea = helpArea;
	}
	public UcdTextField(int size) {
		super(size);
	}

	/**
	 * @param treepath
	 * @param num_node
	 */
	public boolean setText(TreePath treepath) {
		if( valid() ) {
			setText(treepath.getLastPathComponent().toString());
			return true;
		} else {
			return false;
		}
	}
	/* (non-Javadoc)
	 * @see javax.swing.text.JTextComponent#setText(java.lang.String)
	 */
	public void setText(String text) {
		int pos = text.indexOf('(');
		String comment = "";
		if( pos > 0 ) {
			comment = text.substring(pos).trim();	
			text = text.substring(0, pos).trim();
		}
		super.setText(text);		

		if( helpArea != null ){
			helpArea.setText(helpArea.getText() + comment);
		}
	}

	protected boolean valid() {
		return true;
	}
}
