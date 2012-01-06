/**
 * 
 */
package saadadb.admintool;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.AdminPassword;
import saadadb.admintool.cmdthread.DummyTask;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.BaseFrame;
import saadadb.admintool.panels.ChoicePanel;
import saadadb.admintool.panels.RootChoicePanel;
import saadadb.admintool.panels.LoadDataPanel;
import saadadb.admintool.panels.ManageDataPanel;
import saadadb.admintool.panels.MetaDataPanel;
import saadadb.admintool.panels.ProcessPanel;
import saadadb.admintool.panels.RelationChoicePanel;
import saadadb.admintool.panels.VOPublishPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.command.ArgsParser;
import saadadb.command.SaadaProcess;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;




/**
 * @author laurent
 *
 */
public class AdminTool extends BaseFrame {
	public static final int height = 500;
	private JSplitPane splitPane;
	public MetaDataPanel metaDataTree;
	private AdminComponent activePanel;
	private ChoicePanel choicePanelRoot;
	private ChoicePanel manageDataPanel;
	private ChoicePanel loadDataPanel;
	private ChoicePanel voPublishPanel;
	private ChoicePanel relationPanel;
	private final ProcessPanel processPanel = new ProcessPanel(this, AdminComponent.ROOT_PANEL);



	private  String logFile = null;


	public AdminTool(boolean nolog, Point p) throws SaadaException {
		super("Saada  Admintool");
		connectMessaging(nolog);
		/*
		 * Exit after confirmation when click on the window close button
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				if( SaadaDBAdmin.showConfirmDialog(AdminTool.this, "Do you really want to exit?") ==  true ) {
					System.exit(1);
				}
			}
		});

		this.setResizable(true);
		this.setLayout(new GridBagLayout());		
		/*
		 * Make sure to close and rename the log file when exit
		 */
		Date date = new Date();
		String sdate = DateFormat.getDateInstance(DateFormat.SHORT,Locale.FRANCE).format(date);
		sdate += date.toString().substring(10,19);

		/*
		 * Exit after confirmation when click on the window close button
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				//if( SaadaDBAdmin.showConfirmDialog(SaadaDBAdmin.this, "Do you really want to exit?") ==  true ) {
				System.exit(1);
				//}
			}
		});


		GridBagConstraints c = new GridBagConstraints();
		/*
		 * meta_data tree is always visible on the left pbutton_panelart of the panel
		 */
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		leftPanel.setPreferredSize(new Dimension(500,  height));
		metaDataTree = new MetaDataPanel(this, 250, 100);;
		c.gridx = 0;
		c.gridy = 0;	
		c.fill  = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		leftPanel.add(metaDataTree,c);
		JButton b = new JButton("Look at Current Process");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int x = (int)(Math.random() * 10);
				activePanel(AdminComponent.PROCESS_PANEL);
				activePanel.setDataTreePath(new DataTreePath("collection" + x, "CATEGORY", "classe" + x));
				activePanel.setCurrentTask("Au Boulot");
			}
		});
		c.gridx = 0;
		c.gridy = 1;	
		c.weightx = 0;
		c.weighty = 0;
		leftPanel.add(b, c);
		b = new JButton("Start Process");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				activePanel(AdminComponent.PROCESS_PANEL);
				processPanel.setCmdThread(new DummyTask(AdminTool.this));
			}
		});
		c.gridx = 0;
		c.gridy = 2;	
		c.weightx = 0;
		c.weighty = 0;
		leftPanel.add(b, c);
		
		/*
		 * Build the right tab panel
		 */
		choicePanelRoot = new RootChoicePanel(this, "Root Panel");
		activePanel = choicePanelRoot;
		activePanel.setPreferredSize(new Dimension(250,  height));
		c.gridx = 0;
		c.gridy = 0;		
		//this.add(onglets, c);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, activePanel);	
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(250);
		this.add(splitPane, c);

		/*
		 * and show
		 */
		Dimension screenSize =
			Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = this.getPreferredSize();
		setLocation(screenSize.width/2 - (labelSize.width/2),
				screenSize.height/2 - (labelSize.height/2));
		this.pack();
		Messenger.hideSplash();		
		this.setVisible(true);	
		/*
		 * Ask for the administrator password
		 */
		if( Database.getWrapper().supportAccount() ) {
			AdminPassword	ap = new AdminPassword(this);
			if( ap.getTyped_name() == null ) {
				System.exit(1);
			}
		}


	}
	
	public ProcessPanel getProcessPanel() {
		return processPanel;
	}


	/**
	 * @param nolog
	 */
	private void connectMessaging(boolean nolog) {
		/*
		 * Make sure to close and rename the log file when exit
		 */
		Date date = new Date();
		String sdate = DateFormat.getDateInstance(DateFormat.SHORT,Locale.FRANCE).format(date);
		sdate += date.toString().substring(10,19);
		logFile  = Repository.getLogsPath() 
		+  Database.getSepar() + "SaadaAdmin." + sdate.replaceAll("[ :\\/]", "_") + ".log";
		Messenger.setGraphicMode(this);
		if( !nolog ) {
			Messenger.openLog(logFile);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					Messenger.closeLog();
					System.out.println("Log saved in " + logFile);
				}
			});
		}

	}
	
	public void activePanel(String panelTitle)  {
		if( panelTitle.equals(AdminComponent.ROOT_PANEL) ) {
			if( choicePanelRoot == null ) {
				choicePanelRoot = new RootChoicePanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = choicePanelRoot;
		} else 	if( panelTitle.equals(AdminComponent.MANAGE_DATA) ) {
			if( manageDataPanel == null ) {
				manageDataPanel = new ManageDataPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = manageDataPanel;
		} else 	if( panelTitle.equals(AdminComponent.LOAD_DATA) ) {
			if( loadDataPanel == null ) {
				loadDataPanel = new LoadDataPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = loadDataPanel;
		} else 	if( panelTitle.equals(AdminComponent.VO_PUBLISH) ) {
			if( voPublishPanel == null ) {
				voPublishPanel = new VOPublishPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = voPublishPanel;
		} else 	if( panelTitle.equals(AdminComponent.MANAGE_RELATIONS) ) {
			if( relationPanel == null ) {
				relationPanel = new RelationChoicePanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = relationPanel;
		} else 	if( panelTitle.equals(AdminComponent.PROCESS_PANEL) ) {
			activePanel = processPanel;
		}

		else {
			System.err.println("Panel " + panelTitle + " not referenced");
		}
		int dl = splitPane.getDividerLocation();
		splitPane.setRightComponent(activePanel);
		splitPane.setDividerLocation(dl);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args){
		try {		
			Messenger.showSplash();
			final ArgsParser ap = new  ArgsParser(args);
			ap.setDebugMode();
			Database.init(ap.getDBName());
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					try {
						new AdminTool(ap.isNolog(), null);
					} catch (SaadaException e) {
						Messenger.printStackTrace(e);
						System.exit(1);
					}
				}

			});
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}

	@Override
	public void setDataTreePath(DataTreePath dataTreePath) {
		this.dataTreePath = dataTreePath;
		if( this.activePanel != null ) {
			this.activePanel.setDataTreePath(dataTreePath);
		}	
	}
	
	public void diskAccess() {
		this.processPanel.diskAccess();
	}
	public void procAccess() {
		this.processPanel.procAccess();
	}
	public void dbAccess() {
		this.processPanel.dbAccess();
	}
	public void noMoreAccess() {
		this.processPanel.noMoreHarwareAccess();
	}


}
