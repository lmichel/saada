package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.windows.TextSaver;

public class AntButton extends JButton{
	private final TaskPanel adminPanel;

	public AntButton(TaskPanel adminPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Ant.png")));
		this.adminPanel =adminPanel;
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CmdThread ct =  AntButton.this.adminPanel.getCmdThread ();
				if( ct != null ) {
					try {
						if( AntButton.this.adminPanel.setCmdParams(false) )  {
							(new TextSaver(AntButton.this.adminPanel.rootFrame
									, " Ant file for " + AntButton.this.adminPanel.getTitle()
									, AntButton.this.adminPanel.getTitle().replaceAll(" " , "_") + ".xml"
									, ct.getAntTarget())).open();
						}
					} catch (Exception e1) {
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
