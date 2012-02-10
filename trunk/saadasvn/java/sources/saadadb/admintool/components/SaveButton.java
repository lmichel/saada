package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.TaskPanel;
import saadadb.util.Messenger;

public class SaveButton extends JButton{
	private final TaskPanel adminPanel;

	public SaveButton(TaskPanel adminPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Save.png")));
		this.adminPanel =adminPanel;
		this.setEnabled(false);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					SaveButton.this.adminPanel.save ();
				} catch (Exception e1) {
					Messenger.printStackTrace(e1);
					AdminComponent.showFatalError(SaveButton.this.adminPanel.rootFrame, e1);
				}
			}
		});
	}
}
