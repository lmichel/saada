/**
 * 
 */
package saadadb.admintool.components.input;

import java.awt.Container;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author laurentmichel
 *
 */
public class FreeTextField extends JTextArea {
	private JScrollPane jsc;

	/**
	 * @param rows
	 * @param columns
	 */
	public FreeTextField(int rows, int columns) {
		super(rows, columns);
		jsc = new JScrollPane(this);
	}

	/**
	 * @return
	 */
	public Container getPanel() {
		return this.jsc;
	}
}
