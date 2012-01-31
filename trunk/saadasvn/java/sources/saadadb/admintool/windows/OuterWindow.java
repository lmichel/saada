/**
 * 
 */
package saadadb.admintool.windows;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;


/**
 * @author laurentmichel
 *
 */
public abstract class OuterWindow extends JFrame {
	private static final Dimension defaultDim = new Dimension (500, 500);
	protected Dimension dimension;
	public static AdminTool rootFrame;
	protected JPanel panel ;
	protected String title;
	
	OuterWindow(AdminTool rootFrame, String title) {
		this(rootFrame);
		this.setTitle(title);
	}
	OuterWindow(AdminTool rootFrame) {
		super("Data View");
		dimension = new Dimension(defaultDim.width, defaultDim.height);
		OuterWindow.rootFrame = rootFrame;
		this.panel = new JPanel();
		this.setPreferredSize(dimension);
		this.getContentPane().setBackground(AdminComponent.LIGHTBACKGROUND);

	}
	
	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
		super.setTitle(title);		
	}
	/**
	 * 
	 */
	public void open(){
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		try {
			this.setContent();
		} catch (Exception e) {
			AdminComponent.showFatalError(this, e);
			return;
		}
		this.pack();
		this.setLocationRelativeTo(rootFrame);
		this.setVisible(true);
		this.setCursor(Cursor.getDefaultCursor());
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	protected abstract void setContent() throws Exception ;
	

}
