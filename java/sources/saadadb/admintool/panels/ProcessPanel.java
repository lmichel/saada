package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.JBlinkingButton;
import saadadb.admintool.components.RunPauseButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.util.Messenger;


/**
 * Panel showing the current action progress.
 * It is activated after the action is started
 * 
 * @author laurent
 * @version $Id: ProcessPanel.java 749 2013-08-07 09:58:32Z rybak.jonathan $
 *
 */
public  class ProcessPanel extends TaskPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea outputArea;
	private JLabel procLight;
	private JLabel diskLight ;
	private JLabel dbLight;
	private RunPauseButton runPauseButton ;
	private JBlinkingButton abortButton ;
	private Color NO_HARDWARE_ACCESS_COLOR;
	private Timer threadChecker;
	private JLabel statusLabel;


	public ProcessPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, PROCESS_PANEL, null, ancestor);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
	}

	/**
	 * Tree path can only be set by the calling panel
	 * @param dataTreePath
	 */
	/*public void setDataTreePathLabel(String dataTreePathLabel) {
		treePathLabel.setText(dataTreePathLabel);
	}*/

	/**
	 * 
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.initCurrentTaskLabel();
		this.add(new ToolBarPanel(this, true, true, true));
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel = this.addSubPanel("Console");
		outputArea = new JTextArea();
		outputArea.setBackground(IVORY);
		JScrollPane jcp = new JScrollPane(outputArea);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		tPanel.add(jcp, c);	

		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;	
		c.weightx = 0; c.weighty = 0;	
		c.anchor = GridBagConstraints.PAGE_END;


		runPauseButton = new RunPauseButton(this);

		abortButton = new JBlinkingButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Abort.png")));
		abortButton.setPreferredSize(new Dimension(60,40));
		abortButton.setToolTipText("Abort");
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( cmdThread != null ) {
					if( cmdThread.isRunning() ||cmdThread.isWaiting())
					{
						Messenger.requestResume();
						cmdThread.wakeUp();
						Messenger.requestAbort();
						abortButton.startBlinking();
						ProcessPanel.this.rootFrame.currentProcess.setActiveProcess(false);
					}
					else {
						AdminComponent.showInfo(ProcessPanel.this, "No running command: last command was " + cmdThread.getState());
						ProcessPanel.this.rootFrame.currentProcess.setActiveProcess(false);
					}
				}
				else {
					AdminComponent.showInfo(ProcessPanel.this, "No active command");
					ProcessPanel.this.rootFrame.currentProcess.setActiveProcess(false);
				}
			}
		});

//		JButton jb = new AntButton(rootFrame.getActivePanel());
//		jb.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				cmdThread = new ThreadCreateCollection(rootFrame, CREATE_COLLECTION);
//				AdminComponent.showInfo(rootFrame,cmdThread.getAntTarget());
//			}
//		});
		procLight = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Processor.png")));
		diskLight = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Disk.png")));
		dbLight   = new JLabel(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/Database.png")));
		statusLabel = new JLabel("task status");
		this.setProcessControlBar(new Component[] {runPauseButton, abortButton, debugButton}
		, statusLabel
		, new JLabel[]{procLight, diskLight, dbLight});

		NO_HARDWARE_ACCESS_COLOR = tPanel.getBackground();
		this.noMoreHarwareAccess();


	}

	public void diskAccess() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dbLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				diskLight.setBackground(Color.GREEN);
				procLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
			}
		});
	}
	public void procAccess() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dbLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				diskLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				procLight.setBackground(Color.GREEN);
			}
		});
	}
	public void dbAccess() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dbLight.setBackground(Color.GREEN);
				diskLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				procLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
			}
		});
	}
	public void noMoreHarwareAccess() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dbLight.setOpaque(true);
				dbLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				diskLight.setOpaque(true);
				diskLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
				procLight.setOpaque(true);
				procLight.setBackground(NO_HARDWARE_ACCESS_COLOR);
			}
		});
	}

	public JTextArea getOutputArea() {
		return outputArea;
	}

	public boolean hasARunningThread() {
		return !(this.cmdThread  == null || this.cmdThread.isCompleted());
	}

	public void setCmdThread(CmdThread cmdThread) {
		if( this.cmdThread  == null || this.cmdThread.isCompleted()) {
			this.cmdThread = cmdThread;
			this.noMoreHarwareAccess();
			Messenger.setGui_area_output(outputArea); //TODO understand !
			Messenger.resetUserRequests();
			this.cmdThread.start();
			this.outputArea.setText("");
			this.currentTaskLabel.setText(this.cmdThread.taskTitle);
			this.selectResourceLabel.setText(this.cmdThread.getResourceLabel());
			
			ProcessPanel.this.outputArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			ProcessPanel.this.rootFrame.currentProcess.setActiveProcess(true);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			threadChecker  = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ProcessPanel.this.runPauseButton.updateIcon();
					ProcessPanel.this.statusLabel.setText(ProcessPanel.this.cmdThread.getState().toString());
					if( ProcessPanel.this.cmdThread.isCompleted()) {
						ProcessPanel.this.setCursor(Cursor.getDefaultCursor());
						ProcessPanel.this.rootFrame.currentProcess.setActiveProcess(false);
						ProcessPanel.this.outputArea.setCursor(Cursor.getDefaultCursor());
						threadChecker.stop();
						runPauseButton.stopBlinking(false);
						abortButton.stopBlinking(false);
					}
				}
			});
			threadChecker.start();
		}
		else {
			AdminComponent.showInfo(rootFrame, "Another thread is running");
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#getParamMap()
	 */
	protected Map<String, Object> getParamMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#initCmdThread()
	 */
	@Override
	public void initCmdThread() {		
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#active()
	 */
	public void active() {
		debugButton.active();
	}
}
