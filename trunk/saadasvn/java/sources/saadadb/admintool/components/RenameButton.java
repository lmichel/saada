package saadadb.admintool.components;

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
public class RenameButton extends JButton{
	private static final long serialVersionUID = 1L;
	private final AdminPanel adminPanel;

	public RenameButton(AdminPanel adminPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Rename.png")));
		this.adminPanel =adminPanel;
		this.setToolTipText("Rename and Save");
		this.setEnabled(false);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( RenameButton.this.adminPanel != null ) {
					try {
						RenameButton.this.adminPanel.rename ();
					} catch (Exception e1) {
						Messenger.printStackTrace(e1);
						AdminComponent.showFatalError(RenameButton.this.adminPanel.rootFrame, e1);
					}
				}
			}
		});
	}
}
