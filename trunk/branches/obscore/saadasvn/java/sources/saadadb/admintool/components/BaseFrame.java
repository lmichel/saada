/**
 * 
 */
package saadadb.admintool.components;

import javax.swing.JFrame;

import saadadb.admintool.utils.DataTreePath;

/**
 * @author laurentmichel
 *
 */
public abstract class BaseFrame extends JFrame {
	private static final long serialVersionUID = -4339733895067241846L;
	protected DataTreePath dataTreePath;
	
	public BaseFrame(String title) {
		super(title);
	}
	
	public abstract void setDataTreePath(DataTreePath dataTreePath) ;
}
