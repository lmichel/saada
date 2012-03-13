package saadadb.admin.dialogs;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.MalformedURLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JWindow;

import saadadb.admin.SaadaDBAdmin;

/**
 * Open a splash window while the metadata cache intit 
 * @author michel
 * * @version $Id$

 */
public class StartDialog extends JWindow  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final JTextArea task_output = new JTextArea(5, 48);
	/**
	 * @param aFrame
	 * @param title
	 * @param thread
	 * @param monitor_mode
	 * @throws MalformedURLException 
	 */
	public StartDialog() throws MalformedURLException {
		JLabel l = new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("images/splash.png")));
		task_output.setBackground(SaadaDBAdmin.beige_color);
		
		Container cont = getContentPane();
		cont.setLayout(new BorderLayout());
		cont.add(l, BorderLayout.NORTH);
		cont.add(task_output, BorderLayout.SOUTH);
		
		this.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = this.getPreferredSize();
		setLocation(screenSize.width/2 - (labelSize.width/2),
				    screenSize.height/2 - (labelSize.height/2));
		this.setVisible(true);	
	}

	/**
	 * @return
	 */
	public JTextArea getConsole() {
		return task_output;
	}
	
	public static void main(String[] args) throws MalformedURLException {
		new StartDialog();
	}

}
