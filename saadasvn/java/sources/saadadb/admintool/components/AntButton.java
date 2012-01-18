package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.AdminPanel;

public class AntButton extends JButton{
	private final AdminPanel adminPanel;

	public AntButton(AdminPanel adminPanel) {
		super(new ImageIcon("icons/Ant.png"));
		this.adminPanel =adminPanel;
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CmdThread ct =  AntButton.this.adminPanel.getCmdThread ();
				if( ct != null ) {
					AdminComponent.showInfo(AntButton.this.adminPanel.rootFrame, ct.getAntTarget());
				}
				else {
					AdminComponent.showInfo(AntButton.this.adminPanel.rootFrame, "There is no command attached to his panel");
				}
			}
		});

	}

}
