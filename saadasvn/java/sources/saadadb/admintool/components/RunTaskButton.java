package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.Timer;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.exceptions.SaadaException;

public class RunTaskButton extends JButton {
	private final TaskPanel adminPanel;
	private CmdThread cmdThread;

	public RunTaskButton(TaskPanel adminPanel) {
		super(new ImageIcon("icons/Run.png"));
		this.adminPanel = adminPanel;
		this.setEnabled(false);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RunTaskButton.this.adminPanel.initCmdThread();
				RunTaskButton.this.cmdThread = RunTaskButton.this.adminPanel.getCmdThread();
				AdminTool rootFrame = RunTaskButton.this.adminPanel.rootFrame;
				try {
					((JButton)(e.getSource())).setEnabled(false);
					RunTaskButton.this.adminPanel.setCmdParams();
					rootFrame.activeProcessPanel(cmdThread);
				} catch (SaadaException e1) {
					AdminComponent.showFatalError(rootFrame, e1);
					rootFrame.activePanel(RunTaskButton.this.adminPanel.getTitle());
				}
			}
		});
	}
}
