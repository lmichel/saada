package saadadb.admintool.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ComponentTitledBorder;
import saadadb.admintool.components.JBlinkingButton;
import saadadb.util.Messenger;


/**
 * @author laurent
 * @version $Id$
 *
 */
public  class ProcessPanel extends AdminPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected CmdThread cmdThread;
	private JTextArea outputArea;
	private JLabel procLight;
	private JLabel diskLight ;
	private JLabel dbLight;
	private JBlinkingButton runPauseButton ;
	private JBlinkingButton abortButton ;
	private Color NO_HARDWARE_ACCESS_COLOR;
	private Timer threadChecker;

	public ProcessPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, PROCESS_PANEL, null, ancestor);
	}

	/**
	 * 
	 */
	protected void setToolBar() {
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setPreferredSize(new Dimension(1000,64));
		tPanel.setMaximumSize(new Dimension(1000,64));
		tPanel.setBackground(LIGHTBACKGROUND);
		JLabel tl = getTitleLabel(" " + title + " ");
		tl.setOpaque(true);
		tl.setBorder(BorderFactory.createLineBorder(Color.black));
		this.setBorder(new ComponentTitledBorder(tl, this, BorderFactory.createLineBorder(Color.black)));
		//tPanel.setBackground(Color.RED);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 7, 1, 1);
		JButton jb;
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1; c.gridheight = 3;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTH;

		if( ! title.equals(ROOT_PANEL)) {

			jb = new JButton(new ImageIcon("icons/back.png"));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rootFrame.activePanel(ancestor);				
				}});
			tPanel.add(jb, c);
			c.gridx++;
		}

		if( c != null || ! ancestor.equals(ROOT_PANEL)) {
			jb = new JButton(new ImageIcon("icons/maison.png"));
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rootFrame.activePanel(ROOT_PANEL);				
				}});

			tPanel.add(jb, c);
		}


		c.gridx = 2; c.gridy = 0;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.8; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		if( treePathLabel == null ) treePathLabel = getSubTitleLabel("TreePath");
		tPanel.add(treePathLabel, c);

		c.gridx = 2; c.gridy = 1;
		c.gridwidth = 1; c.gridheight = 1;
		c.weightx = 0; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		tPanel.add(new JLabel(new ImageIcon("icons/question.png")), c);

		c.gridx = 3; c.gridy = 1;
		c.weightx = 0.75; c.weighty = 1;
		c.anchor = GridBagConstraints.LINE_START;
		if( selectResourceLabel == null ) selectResourceLabel = getSubTitleLabel("No selected resource");
		tPanel.add(selectResourceLabel, c);

		c.gridx = 2; c.gridy = 2;
		c.gridwidth = 2; c.gridheight = 1;
		c.weightx = 0.8; c.weighty = 1;
		if( currentTaskLabel == null ) currentTaskLabel = getSubTitleLabel("No task");
		tPanel.add(currentTaskLabel, c);

		this.add(tPanel);
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


		tPanel = this.addSubPanel("Process Control");
		//tPanel.setPreferredSize(new Dimension(1000, 36));
		tPanel.setMaximumSize(new Dimension(1000, 36));
		runPauseButton = new JBlinkingButton(new ImageIcon("icons/Run.png"));
		runPauseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( cmdThread != null ) {
					if( cmdThread.isRunning()	){
						Messenger.requestPause();
						runPauseButton.startBlinking();
						threadChecker  = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
							if( cmdThread.isWaiting() ){
									threadChecker.stop();
									runPauseButton.stopBlinking(true);
								}
							}
						});
						threadChecker.start();
					}
					else if( cmdThread.isWaiting()) {
						Messenger.requestResume();
						cmdThread.wakeUp();
						threadChecker  = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
							if( cmdThread.isRunning() ){
									threadChecker.stop();
									runPauseButton.stopBlinking(false);
								}
							}
						});
						threadChecker.start();			
					}
					else {
						AdminComponent.showInfo(ProcessPanel.this, "No running command: last command was " + cmdThread.getState());
					}
				}
				else {
					AdminComponent.showInfo(ProcessPanel.this, "No active command");				
				}
			}
		});
		tPanel.add(runPauseButton, c);
		c.gridx++;
		abortButton = new JBlinkingButton(new ImageIcon("icons/Abort.png"));
		abortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( cmdThread != null ) {
					if( cmdThread.isRunning() ||cmdThread.isWaiting()	){
						Messenger.requestResume();
						cmdThread.wakeUp();
						Messenger.requestAbort();
						abortButton.startBlinking();
						threadChecker  = new Timer(1000, new ActionListener() {
							public void actionPerformed(ActionEvent e) {
							if( cmdThread.isCompleted()){
									threadChecker.stop();
									runPauseButton.stopBlinking(false);
									abortButton.stopBlinking(false);
								}
							}
						});
						threadChecker.start();
					}
					else {
						AdminComponent.showInfo(ProcessPanel.this, "No running command: last command was " + cmdThread.getState());
					}
				}
				else {
					AdminComponent.showInfo(ProcessPanel.this, "No active command");				
				}
			}
		});
		tPanel.add(abortButton, c);
		c.gridx++;
		JButton jb = new JButton(new ImageIcon("icons/DebugMode.png"));
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Messenger.debug_mode = !Messenger.debug_mode;
			}
		});
		tPanel.add(jb, c);
		c.gridx++;
		jb = new JButton(new ImageIcon("icons/Ant.png"));
		tPanel.add(jb, c);
		c.gridx++;

		c.weightx = 1; c.weighty = 0;	
		c.anchor= GridBagConstraints.SOUTHEAST;
		JPanel stsPanel = new JPanel();
		Border empty = new EmptyBorder(new Insets(2,2,2,2));

		procLight = new JLabel(new ImageIcon("icons/Processor.png"));
		stsPanel.setLayout(new BoxLayout(stsPanel, BoxLayout.LINE_AXIS));
		procLight.setBorder(empty);
		stsPanel.add(procLight);

		diskLight = new JLabel(new ImageIcon("icons/Disk.png"));
		diskLight.setBorder(empty);	
		stsPanel.add(diskLight);

		dbLight   = new JLabel(new ImageIcon("icons/Database.png"));
		dbLight.setBorder(empty);	
		stsPanel.add(dbLight);

		stsPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		tPanel.add(stsPanel, c);	
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

	public void setCmdThread(CmdThread cmdThread) {
		this.cmdThread = cmdThread;
		this.noMoreHarwareAccess();
		Messenger.setGui_area_output(outputArea);
		Messenger.resetUserRequests();
		this.cmdThread.start();
		this.outputArea.setText("");
	}

}
