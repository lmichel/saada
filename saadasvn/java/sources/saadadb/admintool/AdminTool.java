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

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.AdminPassword;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.BaseFrame;
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
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.panels.editors.VOCuratorPanel;
import saadadb.admintool.panels.tasks.CategoryEmptyPanel;
import saadadb.admintool.panels.tasks.CollCommentPanel;
import saadadb.admintool.panels.tasks.CollCreatePanel;
import saadadb.admintool.panels.tasks.CollDropPanel;
import saadadb.admintool.panels.tasks.CollEmptyPanel;
import saadadb.admintool.panels.tasks.DataLoaderPanel;
import saadadb.admintool.panels.tasks.MetaDataEditorPanel;
import saadadb.admintool.panels.tasks.RelationCreatePanel;
import saadadb.admintool.panels.tasks.RelationDropPanel;
import saadadb.admintool.panels.tasks.RelationEmptyPanel;
import saadadb.admintool.panels.tasks.RelationIndexPanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
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

	private TaskPanel dropClassPanel;
	private TaskPanel emptyClassPanel;
	private TaskPanel commentClassPanel;

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
	
	private EditPanel voCurator;;

	private final ProcessPanel processPanel = new ProcessPanel(this, AdminComponent.ROOT_PANEL);
	private CmdThread windowThread;		


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
				activePanel(AdminComponent.VO_CURATOR);
				//activeProcessPanel(new DummyTask(AdminTool.this));
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
		activePanel.setPreferredSize(new Dimension(600,  height));
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
	public void activePanel(String panelTitle)  {
		if( activePanel!= null && activePanel.hasChanged() && !panelTitle.equals(AdminComponent.PROCESS_PANEL) )  {
			if( !AdminComponent.showConfirmDialog(this, "Modifications not saved. Do you want to continue anyway?") ) {
				return;
			}
			else {
				activePanel.cancelChanges();
			}
		}
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
			 * Data loading task
			 */
		} else 	if( panelTitle.equals(AdminComponent.DATA_LOADER) ) {
			if( dataLoaderPanel == null ) {
				dataLoaderPanel = new DataLoaderPanel(this, AdminComponent.ROOT_PANEL);
			}
			if( activePanel instanceof MappingKWPanel) {
				System.out.println(activePanel.getSelectResourceLabel().getText());
			}
			activePanel = dataLoaderPanel;
			/*
			 * Data loadewr configuration
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
			/*
			 * VO publishing
			 */
		} else 	if( panelTitle.equals(AdminComponent.VO_CURATOR) ) {
			if( voCurator == null ) {
				voCurator = new VOCuratorPanel(this, AdminComponent.VO_PUBLISH);
			}
			activePanel = voCurator;
			
			/*
			 * Process pqnel used by all tasks
			 */
		} else 	if( panelTitle.equals(AdminComponent.PROCESS_PANEL) ) {
			processPanel.setAncestor(activePanel.getTitle());
			if( activePanel.getTreePathLabel() != null )
				processPanel.setDataTreePathLabel(activePanel.getTreePathLabel().getText());
			activePanel = processPanel;
		}
		else {
			System.err.println("Panel " + panelTitle + " not referenced");
		}
		activePanel.active();
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
			processPanel.setDataTreePathLabel(dataTreePath.toString());
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


}
