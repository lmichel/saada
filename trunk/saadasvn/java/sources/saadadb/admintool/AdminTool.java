/**
 * 
 */
package saadadb.admintool;

import java.awt.BorderLayout;
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

import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadDeployWebApp;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.BaseFrame;
import saadadb.admintool.dialogs.AdminPassword;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.panels.ChoicePanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.panels.LoadDataPanel;
import saadadb.admintool.panels.ManageDataPanel;
import saadadb.admintool.panels.MetaDataPanel;
import saadadb.admintool.panels.ProcessPanel;
import saadadb.admintool.panels.RelationChoicePanel;
import saadadb.admintool.panels.RootChoicePanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.panels.VOPublishPanel;
import saadadb.admintool.panels.editors.DBInstallPanel;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.panels.editors.SAPServicePanel;
import saadadb.admintool.panels.editors.TAPServicePanel;
import saadadb.admintool.panels.editors.VOCuratorPanel;
import saadadb.admintool.panels.editors.WebInstallPanel;
import saadadb.admintool.panels.tasks.CategoryEmptyPanel;
import saadadb.admintool.panels.tasks.ClassCommentPanel;
import saadadb.admintool.panels.tasks.ClassDropPanel;
import saadadb.admintool.panels.tasks.ClassEmptyPanel;
import saadadb.admintool.panels.tasks.CollCommentPanel;
import saadadb.admintool.panels.tasks.CollCreatePanel;
import saadadb.admintool.panels.tasks.CollDropPanel;
import saadadb.admintool.panels.tasks.CollEmptyPanel;
import saadadb.admintool.panels.tasks.DataLoaderPanel;
import saadadb.admintool.panels.tasks.DataTableEditorPanel;
import saadadb.admintool.panels.tasks.MetaDataEditorPanel;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.panels.tasks.RelationCommentPanel;
import saadadb.admintool.panels.tasks.RelationCreatePanel;
import saadadb.admintool.panels.tasks.RelationDropPanel;
import saadadb.admintool.panels.tasks.RelationEmptyPanel;
import saadadb.admintool.panels.tasks.RelationIndexPanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.panels.tasks.SQLIndexPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.util.Version;

/**
 * Base frame of the administration tool
 * @author michel
 * @version $Id$
 *
 */
public class AdminTool extends BaseFrame {
	private static final long serialVersionUID = 1L;
	public static final int height = 700;
	private JSplitPane splitPane;
	public MetaDataPanel metaDataTree;
	private AdminPanel  activePanel;
	private ChoicePanel choicePanelRoot;
	private ChoicePanel manageDataPanel;
	private ChoicePanel loadDataPanel;
	private ChoicePanel voPublishPanel;
	private ChoicePanel relationPanel;

	private TaskPanel createCollPanel;
	private TaskPanel dropCollPanel;
	private TaskPanel emptyCollPanel;
	private TaskPanel commentCollPanel;

	private TaskPanel emptyCategoryPanel;
	private MetaDataEditorPanel metaDataPanel;
	private DataTableEditorPanel dataTablePanel;

	private TaskPanel dropClassPanel;
	private TaskPanel emptyClassPanel;
	private TaskPanel commentClassPanel;

	private TaskPanel sqlIndex;

	private TaskPanel dataLoaderPanel;;
	private TaskPanel createRelationPanel;
	private TaskPanel dropRelationPanel;
	private TaskPanel commentRelationPanel;
	private TaskPanel emptyRelationPanel;
	private TaskPanel populateRelationPanel;
	private TaskPanel indexRelationPanel;

	private EditPanel miscMapperPanel;;
	private EditPanel spectrumMapperPanel;;
	private EditPanel tableMapperPanel;;
	private EditPanel imageMapperPanel;;
	private EditPanel flatfileMapperPanel;

	private EditPanel dbInstallPanel;
	private EditPanel webInstallPanel;

	private EditPanel voCurator;;
	private EditPanel tapService;
	private EditPanel siaService;
	private EditPanel ssaService;
	private EditPanel csService;

	private TaskPanel obscoreMapperPanel;

	private final ProcessPanel processPanel = new ProcessPanel(this, AdminComponent.ROOT_PANEL);
	private CmdThread windowThread;		


	private  String logFile = null;


	public AdminTool(boolean nolog, Point p) throws Exception {
		super("Saada " + Version.version + " - Admintool for the " + Database.getDbname() + " database");
		connectMessaging(nolog);
		/*
		 * Exit after confirmation when click on the window close button
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				if (AdminComponent.showConfirmDialog(AdminTool.this, "Do you really want to exit?")) 
					System.exit(1);
			}
		});

		this.setResizable(true);
		this.setLayout(new GridBagLayout());		
		/*Display the console panel connected on the current asynchronous process
		 * Make sure to close and rename the log file when exit
		 */
		Date date = new Date();
		String sdate = DateFormat.getDateInstance(DateFormat.SHORT,Locale.FRANCE).format(date);
		sdate += date.toString().substring(10,19);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("icons/saada_transp_square.png")));


		GridBagConstraints c = new GridBagConstraints();
		/*
		 * meta_data tree is always visible on the left pbutton_panelart of the panel
		 */
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridBagLayout());
		//leftPanel.setPreferredSize(new Dimension(500,  height));
		metaDataTree = new MetaDataPanel(this, 250, 100);;
		c.gridx = 0;
		c.gridy = 0;	
		c.fill  = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		leftPanel.add(metaDataTree,c);
		JButton b = new JButton("Look at Current Process");
		b.setToolTipText("Display the console panel connected on the current asynchronous process");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int x = (int)(Math.random() * 10);
				activePanel(AdminComponent.PROCESS_PANEL);
				activePanel.setDataTreePath(new DataTreePath("collection" + x, "CATEGORY", "classe" + x));
				activePanel.setCurrentTask("Au Boulot");
			}
		});
		//		c.gridx = 0;
		//		c.gridy = 1;	
		//		c.weightx = 0;
		//		c.weighty = 0;
		//		leftPanel.add(b, c);
		//		b = new JButton("Start Process");
		//		b.addActionListener(new ActionListener() {
		//			public void actionPerformed(ActionEvent arg0) {
		//				activePanel(AdminComponent.FILTER_SELECTOR);
		//				//activeProcessPanel(new DummyTask(AdminTool.this));
		//			}
		//		});
		c.gridx = 0;
		c.gridy++;	
		c.weightx = 0;
		c.weighty = 0;
		leftPanel.add(b, c);

		b = new JButton("Deploy Web application");
		b.setToolTipText("Deploy the web application in the Tomcat instance");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				CmdThread ct = new ThreadDeployWebApp(AdminTool.this, "Deploy Webapp");
				ct.run();
			}
		});
		c.gridy++;	
		c.weightx = 0;
		c.weighty = 0;
		leftPanel.add(b, c);
		/*
		 * Build the right tab panel
		 */
		choicePanelRoot = new RootChoicePanel(this, "Root Panel");
		activePanel = choicePanelRoot;
		activePanel.setPreferredSize(new Dimension(700,  height));
		c.gridx = 0;
		c.gridy = 0;		

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, activePanel);	
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(200);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(splitPane, BorderLayout.CENTER);

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
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("icons/saada_transp_square.png")));

		/*
		 * Ask for the administrator password
		 */
		if( Database.getWrapper().supportAccount() ) {
			AdminPassword	ap = new AdminPassword(this);
			if( ap.getTyped_name() == null ) {
				System.exit(1);
			}
			/*
			 * If SQLITE is used, there is no need to switch the user but the schema update needs to be run anyway
			 */
		} else {
			Database.updatSchema();
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

	/**
	 * @param panelTitle
	 */
	/**
	 * @param panelTitle
	 */
	/**
	 * @param panelTitle
	 */
	/**
	 * @param panelTitle
	 */
	public void activePanel(String panelTitle)  {
		if( activePanel!= null && activePanel.hasChanged() && !panelTitle.equals(AdminComponent.PROCESS_PANEL) )  {
			if( !AdminComponent.showConfirmDialog(this, "Modifications not saved. Do you want to continue anyway?") ) {
				return;
			} else {
				activePanel.cancelChanges();
			}
		}
		AdminPanel previousPanel = activePanel;
		/*
		 * Choice panels
		 */
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
				loadDataPanel = new LoadDataPanel(this, AdminComponent.ROOT_PANEL);
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
			/*
			 * Meta data management
			 */
		} else 	if( panelTitle.equals(AdminComponent.MANAGE_METADATA) ) {
			if( metaDataPanel == null ) {
				metaDataPanel = new MetaDataEditorPanel(this,  AdminComponent.ROOT_PANEL);
			}
			activePanel = metaDataPanel;
			/*
			 * Collection mqnagement tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.CREATE_COLLECTION) ) {
			if( createCollPanel == null ) {
				createCollPanel = new CollCreatePanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = createCollPanel;
		} else 	if( panelTitle.equals(AdminComponent.DROP_COLLECTION) ) {
			if( dropCollPanel == null ) {
				dropCollPanel = new CollDropPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = dropCollPanel;
		} else 	if( panelTitle.equals(AdminComponent.EMPTY_COLLECTION) ) {
			if( emptyCollPanel == null ) {
				emptyCollPanel = new CollEmptyPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = emptyCollPanel;
		} else 	if( panelTitle.equals(AdminComponent.COMMENT_COLLECTION) ) {
			if( commentCollPanel == null ) {
				commentCollPanel = new CollCommentPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = commentCollPanel;
			/*
			 * Category management tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.EMPTY_CATEGORY) ) {
			if( emptyCategoryPanel == null ) {
				emptyCategoryPanel = new CategoryEmptyPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = emptyCategoryPanel;
			/*
			 * Index category management tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.SQL_INDEX) ) {
			if( sqlIndex == null ) {
				sqlIndex = new SQLIndexPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = sqlIndex;
			/*
			 * Class level management tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.DROP_CLASS) ) {
			if( dropClassPanel == null ) {
				dropClassPanel = new ClassDropPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = dropClassPanel;
		} else 	if( panelTitle.equals(AdminComponent.EMPTY_CLASS) ) {
			if( emptyClassPanel == null ) {
				emptyClassPanel = new ClassEmptyPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = emptyClassPanel;
		} else 	if( panelTitle.equals(AdminComponent.COMMENT_CLASS) ) {
			if( commentClassPanel == null ) {
				commentClassPanel = new ClassCommentPanel(this, AdminComponent.MANAGE_DATA);
			}
			activePanel = commentClassPanel;

			/*
			 * Data loading task
			 */
		} else 	if( panelTitle.equals(AdminComponent.DATA_LOADER) ) {
			if( dataLoaderPanel == null ) {
				dataLoaderPanel = new DataLoaderPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = dataLoaderPanel;
		} else 	if( panelTitle.equals(AdminComponent.EXPLORE_DATA) ) {
			if( dataTablePanel == null ) {
				dataTablePanel = new DataTableEditorPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = dataTablePanel;
			/*
			 * Data loader configuration
			 */
		} else 	if( panelTitle.equals(AdminComponent.MISC_MAPPER) ) {
			if( miscMapperPanel == null ) {
				miscMapperPanel = new MappingKWPanel(this, AdminComponent.MISC_MAPPER, Category.MISC, AdminComponent.DATA_LOADER);
			}
			activePanel = miscMapperPanel;
		} else 	if( panelTitle.equals(AdminComponent.SPECTRUM_MAPPER) ) {
			if( spectrumMapperPanel == null ) {
				spectrumMapperPanel = new MappingKWPanel(this, AdminComponent.SPECTRUM_MAPPER, Category.SPECTRUM, AdminComponent.DATA_LOADER);
			}
			activePanel = spectrumMapperPanel;
		} else 	if( panelTitle.equals(AdminComponent.TABLE_MAPPER) ) {
			if( tableMapperPanel == null ) {
				tableMapperPanel = new MappingKWPanel(this, AdminComponent.TABLE_MAPPER, Category.TABLE, AdminComponent.DATA_LOADER);
			}
			activePanel = tableMapperPanel;
		} else 	if( panelTitle.equals(AdminComponent.IMAGE_MAPPER) ) {
			if( imageMapperPanel == null ) {
				imageMapperPanel = new MappingKWPanel(this,AdminComponent.IMAGE_MAPPER , Category.IMAGE, AdminComponent.DATA_LOADER);
			}
			activePanel = imageMapperPanel;
		} else 	if( panelTitle.equals(AdminComponent.FLATFILE_MAPPER) ) {
			if( flatfileMapperPanel == null ) {
				flatfileMapperPanel = new MappingKWPanel(this, AdminComponent.FLATFILE_MAPPER, Category.FLATFILE,  AdminComponent.DATA_LOADER);
			}
			activePanel = flatfileMapperPanel;
			/*
			 * Relationship management
			 */
		} else 	if( panelTitle.equals(AdminComponent.CREATE_RELATION) ) {
			if( createRelationPanel == null ) {
				createRelationPanel = new RelationCreatePanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = createRelationPanel;
		} else 	if( panelTitle.equals(AdminComponent.DROP_RELATION) ) {
			if( dropRelationPanel == null ) {
				dropRelationPanel = new RelationDropPanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = dropRelationPanel;
		} else 	if( panelTitle.equals(AdminComponent.INDEX_RELATION) ) {
			if( indexRelationPanel == null ) {
				indexRelationPanel = new RelationIndexPanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = indexRelationPanel;
		} else 	if( panelTitle.equals(AdminComponent.EMPTY_RELATION) ) {
			if( emptyRelationPanel == null ) {
				emptyRelationPanel = new RelationEmptyPanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = emptyRelationPanel;
		} else 	if( panelTitle.equals(AdminComponent.POPULATE_RELATION) ) {
			if( populateRelationPanel == null ) {
				populateRelationPanel = new RelationPopulatePanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = populateRelationPanel;
		} else 	if( panelTitle.equals(AdminComponent.COMMENT_RELATION) ) {
			if( commentRelationPanel == null ) {
				commentRelationPanel = new RelationCommentPanel(this, AdminComponent.MANAGE_RELATIONS);
			}
			activePanel = commentRelationPanel;
			/*
			 * VO publishing
			 */
		} else 	if( panelTitle.equals(AdminComponent.VO_CURATOR) ) {
			if( voCurator == null ) {
				voCurator = new VOCuratorPanel(this, AdminComponent.VO_PUBLISH);
			}
			activePanel = voCurator;
		} else 	if( panelTitle.equals(AdminComponent.TAP_PUBLISH) ) {
			if( tapService == null ) {
				tapService = new TAPServicePanel(this, AdminComponent.VO_PUBLISH);
			}
			activePanel = tapService;
		} else 	if( panelTitle.equals(AdminComponent.SIA_PUBLISH) ) {
			if( siaService == null ) {
				siaService = new SAPServicePanel(this, AdminComponent.VO_PUBLISH, AdminComponent.SIA_PUBLISH);
			}
			activePanel = siaService;
		} else 	if( panelTitle.equals(AdminComponent.SSA_PUBLISH) ) {
			if( ssaService == null ) {
				ssaService = new SAPServicePanel(this, AdminComponent.VO_PUBLISH, AdminComponent.SSA_PUBLISH);
			}
			activePanel = ssaService;
		} else 	if( panelTitle.equals(AdminComponent.CONESEARCH_PUBLISH) ) {
			if( csService == null ) {
				csService = new SAPServicePanel(this, AdminComponent.VO_PUBLISH, AdminComponent.CONESEARCH_PUBLISH);
			}
			activePanel = csService;
		} else 	if( panelTitle.equals(AdminComponent.OBSCORE_MAPPER) ) {
			if( obscoreMapperPanel== null ) {
				obscoreMapperPanel = new ObscoreMapperPanel(this, AdminComponent.VO_PUBLISH);
			}
			activePanel = obscoreMapperPanel;
			/*
			 * Installation panels
			 */
		} else 	if( panelTitle.equals(AdminComponent.DB_INSTALL) ) {
			if( dbInstallPanel== null ) {
				dbInstallPanel = new DBInstallPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = dbInstallPanel;
		} else 	if( panelTitle.equals(AdminComponent.WEB_INSTALL) ) {
			if( webInstallPanel== null ) {
				webInstallPanel = new WebInstallPanel(this, AdminComponent.ROOT_PANEL);
			}
			activePanel = webInstallPanel;
			/*
			 * Process panel used by all tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.PROCESS_PANEL) ) {
			processPanel.setAncestor(activePanel.getTitle());
			/*if( activePanel.getTreePathLabel() != null )
				processPanel.setDataTreePathLabel(activePanel.getTreePathLabel().getText());*/
			if( activePanel.getTreePathPanel() != null )
				try 
				{
					processPanel.setTextTreePathPanel(new DataTreePath(metaDataTree.getClickedTreePath()));
					//processPanel.setDataTreePathPanel(new DataTreePath(this.metaDataTree.getClickedTreePath()));
				} 
				catch (QueryException e) 
				{
					e.printStackTrace();
				}
			activePanel = processPanel;
		}
		else {
			System.err.println("Panel " + panelTitle + " not referenced");
		}
		/*
		 * The panel refuses to be open when the datatreepath does not match what it need 
		 */
		if( !activePanel.acceptTreePath(this.dataTreePath) ) {
			activePanel = previousPanel;
			return;
		}
		/*
		 * Data treepath must be locked later  by the ancestor
		 */
		activePanel.unlockDataTreePath();

		this.activePanel.setDataTreePath(this.dataTreePath);

		/*
		 * Make the active panel visible
		 */
		int dl = splitPane.getDividerLocation();
		splitPane.setRightComponent(activePanel);
		splitPane.setDividerLocation(dl);

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				activePanel.updateUI();
			}
		});
	}

	/**
	 * Allow to programmaticaly connect a resource on a panel
	 * @param label
	 * @param explanation
	 */
	public void setSelectedResource(String label, String explanation) {	
		if( activePanel != null ) {
			activePanel.setSelectedResource(label, explanation);
		}
	}

	public AdminPanel getActivePanel() {
		return activePanel;
	}
	public void activeProcessPanel(CmdThread cmdThread) {
		if( processPanel.hasARunningThread() || (windowThread != null &&  !windowThread.isCompleted() )) {
			AdminComponent.showInfo(this, "Another thread is running, please wait...");
		}
		else {
			this.activePanel(AdminComponent.PROCESS_PANEL);
			processPanel.setCmdThread(cmdThread);
		}
	}

	/**
	 * @param cmdThread
	 */
	public void activeWindowProcess(CmdThread cmdThread, DataTreePath dataTreePath) {
		if( processPanel.hasARunningThread()  || (windowThread != null &&  !windowThread.isCompleted() ) ){
			AdminComponent.showInfo(this, "Another thread is running, please wait...");
		}
		else {
			windowThread = cmdThread;
			this.activePanel(AdminComponent.PROCESS_PANEL);
			processPanel.setAncestor(AdminComponent.ROOT_PANEL);
			processPanel.setDataTreePath(dataTreePath);
			//processPanel.setDataTreePathLabel(dataTreePath.toString());
			try 
			{
				processPanel.setTextTreePathPanel(new DataTreePath(metaDataTree.getClickedTreePath()));
				//processPanel.setDataTreePathPanel(new DataTreePath(metaDataTree.getClickedTreePath()));
			} 
			catch (QueryException e) 
			{

				e.printStackTrace();
			}
			processPanel.setCmdThread(cmdThread);
		}	
	}

	/**
	 * Synchronize the collection nodes of the tree with real meta_data
	 */
	public void refreshTree() {
		try {
			metaDataTree.synchronizeCollectionNodes();
		} catch (SaadaException e) {
			AdminComponent.showFatalError(this, "An internal error occured (see console)");
		}
	}

	/**
	 * Synchronize the category leaf of the collection nodes of the tree with real meta_data
	 * @param collection
	 * @param category
	 */
	public void refreshTree(String collection, String category) {
		try {
			metaDataTree.synchronizeCategoryNodes(collection, category);
			if( category.equalsIgnoreCase("TABLE")) {
				metaDataTree.synchronizeCategoryNodes(collection, "ENTRY");			
			}
		} catch (SaadaException e) {
			AdminComponent.showFatalError(this, "An internal error occured (see console)");
		}
	}

	/**
	 * Synchronize all categories leaf of the collection nodes of the tree with real meta_data
	 * @param collection
	 */
	public void refreshTree(String collection) {
		for( int i=1 ; i<Category.NB_CAT ; i++) {
			this.refreshTree(collection, Category.NAMES[i]);
		}
	}

	public boolean acceptTreePath(DataTreePath dataTreePath) {
		if( this.activePanel != null ) {
			return this.activePanel.acceptTreePath(dataTreePath);
		} else {
			return true;
		}
	}

	@Override
	public void setDataTreePath(DataTreePath dataTreePath) {
		this.dataTreePath = dataTreePath;
		if( this.activePanel != null ) {
			/*
			 * Mapper form must change when the category is changed
			 */
			if( this.activePanel instanceof MappingKWPanel ) {
				if( this.activePanel.isDataTreePathLocked() )  {
					AdminComponent.showInfo(this, "This action has no effect while the data loader configuration");
				}
				if( dataTreePath.category != null ) {
					String category = dataTreePath.category;
					try {
						if( ((MappingKWPanel)(this.activePanel)).getCategory() != Category.getCategory(category) ) {
							if( "FLATFILE".equalsIgnoreCase(category)) {
								activePanel(AdminComponent.FLATFILE_MAPPER);
							}
							else if( "IMAGE".equalsIgnoreCase(category)) {
								activePanel(AdminComponent.IMAGE_MAPPER);
							}
							else if( "MISC".equalsIgnoreCase(category)) {
								activePanel(AdminComponent.MISC_MAPPER);
							}
							else if( "SPECTRUM".equalsIgnoreCase(category)) {
								activePanel(AdminComponent.SPECTRUM_MAPPER);
							}
							else if( "TABLE".equalsIgnoreCase(category) || "ENTRY".equalsIgnoreCase(category)) {
								activePanel(AdminComponent.TABLE_MAPPER);
							}	
						}
					} catch (FatalException e) {
						Messenger.trapFatalException(e);
					}
				}
			}
			else {
				this.activePanel.setDataTreePath(dataTreePath);		
			}
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
					} catch (Exception e) {
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


}
