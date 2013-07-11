package saadadb.admintool.components;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.Timer;

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.panels.TaskPanel;
import saadadb.util.Messenger;

public class RunPauseButton extends JBlinkingButton {
	private final TaskPanel taskPanel;
	private Timer commandChecker;
	private CmdThread cmdThread;
	private ImageIcon runIcon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Run.png"));
	private ImageIcon pauseIcon = new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Pause.png"));

	public RunPauseButton(TaskPanel taskPanel) {
		super(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Run.png")));
		this.taskPanel =taskPanel;
		this.setPreferredSize(new Dimension(60, 40));
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cmdThread = RunPauseButton.this.taskPanel.getCmdThread();

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
						AdminComponent.showInfo(RunPauseButton.this.taskPanel, "No running command: last command was " + cmdThread.getState());
					}
				}
				else {
					AdminComponent.showInfo(RunPauseButton.this.taskPanel, "No active command");				
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
			cmdThread = RunPauseButton.this.taskPanel.getCmdThread();
			this.setIcon(runIcon);
		}
	}
}
