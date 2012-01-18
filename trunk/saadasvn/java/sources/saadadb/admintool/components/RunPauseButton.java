package saadadb.admintool.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.Timer;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.AdminPanel;
import saadadb.util.Messenger;

public class RunPauseButton extends JBlinkingButton {
	private final AdminPanel adminPanel;
	private Timer commandChecker;
	private CmdThread cmdThread;
	private ImageIcon runIcon = new ImageIcon("icons/Run.png");
	private ImageIcon pauseIcon = new ImageIcon("icons/Pause.png");

	public RunPauseButton(AdminPanel adminPanel) {
		super(new ImageIcon("icons/Run.png"));
		this.adminPanel =adminPanel;

		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cmdThread = RunPauseButton.this.adminPanel.getCmdThread();

				if( cmdThread != null ) {
					if( cmdThread.isRunning()	){
						Messenger.requestPause();
						RunPauseButton.this.startBlinking();
						RunPauseButton.this.updateIcon();

						RunPauseButton.this.commandChecker  = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								RunPauseButton.this.updateIcon();
								if( cmdThread.isWaiting() ){
									RunPauseButton.this.commandChecker.stop();
									RunPauseButton.this.stopBlinking(true);
								}
							}
						});
						RunPauseButton.this.commandChecker.start();
					}
					else if( cmdThread.isWaiting()) {
						Messenger.requestResume();
						RunPauseButton.this.cmdThread.wakeUp();
						RunPauseButton.this.commandChecker  = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								RunPauseButton.this.updateIcon();
								if( cmdThread.isRunning() ){
									RunPauseButton.this.commandChecker.stop();
									RunPauseButton.this.stopBlinking(false);
								}
							}
						});
						RunPauseButton.this.commandChecker.start();			
					}
					else {
						AdminComponent.showInfo(RunPauseButton.this.adminPanel, "No running command: last command was " + cmdThread.getState());
					}
				}
				else {
					AdminComponent.showInfo(RunPauseButton.this.adminPanel, "No active command");				
				}
			}
		});
	}

	/**
	 * Updates the icon according to the thread status
	 */
	public void updateIcon() {
		if( cmdThread != null ) {
			if( cmdThread.isRunning() && this.getIcon() != pauseIcon	){
				this.setIcon(pauseIcon);
			}
			else if( cmdThread.isWaiting() && this.getIcon() != runIcon	){
				this.setIcon(runIcon);
			}
		}
		else {
			cmdThread = RunPauseButton.this.adminPanel.getCmdThread();
			this.setIcon(runIcon);
		}
	}
}
