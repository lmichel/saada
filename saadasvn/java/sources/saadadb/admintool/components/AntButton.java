package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.exceptions.SaadaException;

public class AntButton extends JButton{
	private final TaskPanel adminPanel;

	public AntButton(TaskPanel adminPanel) {
		super(new ImageIcon("icons/Ant.png"));
		this.adminPanel =adminPanel;
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("@@@@ " + e.getSource());
				CmdThread ct =  AntButton.this.adminPanel.getCmdThread ();
				System.out.println("@@@@ 2" );
				if( ct != null ) {
					try {
						System.out.println("@@@@ 3" );
						if( AntButton.this.adminPanel.setCmdParams() )  {
							System.out.println("@@@@ 4" );
							AdminComponent.showInfo(AntButton.this.adminPanel.rootFrame, ct.getAntTarget());
						}
					} catch (SaadaException e1) {
						AdminComponent.showFatalError(AntButton.this.adminPanel.getRootFrame(), e1);
					}
				}
				else {
					AdminComponent.showInfo(AntButton.this.adminPanel.rootFrame, "There is no command attached to his panel");
				}
			}
		});

	}

}
