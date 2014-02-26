package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.TaskPanel;

public class RunTaskButton extends JButton {
	private final TaskPanel adminPanel;
	private CmdThread cmdThread;

	public RunTaskButton(TaskPanel adminPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Run.png")));

		this.adminPanel = adminPanel;
		this.setToolTipText("Run task");
		this.setEnabled(false);
		this.setPreferredSize(new Dimension(60, 40));
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( RunTaskButton.this.adminPanel != null ) {
					RunTaskButton.this.adminPanel.initCmdThread();
					RunTaskButton.this.cmdThread = RunTaskButton.this.adminPanel.getCmdThread();
					AdminTool rootFrame = RunTaskButton.this.adminPanel.rootFrame;
					try {
						((JButton)(e.getSource())).setEnabled(false);
						if( RunTaskButton.this.adminPanel.setCmdParams(true)  == true ) {
							rootFrame.activeProcessPanel(cmdThread);
						}
						else
						{
							((JButton)(e.getSource())).setEnabled(true);
						}
					} catch (Exception e1) {
						AdminComponent.showFatalError(rootFrame, e1);
						rootFrame.activePanel(RunTaskButton.this.adminPanel.getTitle());
					}
				}
			}
		});
	}

	public void activate() {
		this.setEnabled(true);
	}
	public void inactivate() {
		this.setEnabled(false);
	}

}
