package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.panels.AdminPanel;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class SaveButton extends JButton{
	private static final long serialVersionUID = 1L;
	private final AdminPanel adminPanel;

	public SaveButton(AdminPanel adminPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Save.png")));
		this.adminPanel =adminPanel;
		this.setToolTipText("Save current Work");
		this.setEnabled(false);
		this.setPreferredSize(new Dimension(60, 40));
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( SaveButton.this.adminPanel != null ) {
					try {
						SaveButton.this.adminPanel.save ();
					} catch (Exception e1) {
						Messenger.printStackTrace(e1);
						AdminComponent.showFatalError(SaveButton.this.adminPanel.rootFrame, e1);
					}
				}
			}
		});
	}
}
