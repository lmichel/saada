package saadadb.admintool.panels;

import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;
import saadadb.admintool.utils.DataTreePath;
import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;

public class RootChoicePanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;

	public RootChoicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, ROOT_PANEL, null, ancestor);
	}
	
	private ChoiceItem createCollection, loadData, editFilter, 
	manageData, manageMetaData, manageRelationships,
	databaseInstallation, webPublishing, VOPublishing;

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		
		tPanel = this.addSubPanel("Data Management");
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		createCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Create Collection", "icons/CreateColl.png"
				, new Runnable(){public void run() {
					try {
						rootFrame.activePanel(CREATE_COLLECTION);
					} catch (Exception e) {
						e.printStackTrace();
					}
					}});
		createCollection.setToolTipText("Create a collection", "A collection contains your data with differents classes and categories.");
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		loadData = new ChoiceItem(rootFrame, tPanel, c
				, "Load Data", "icons/LoadData.png"
				, new Runnable(){public void run(){rootFrame.activePanel(DATA_LOADER);}});
		loadData.inactive();
		loadData.setToolTipText("Load Data", "You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to load data.");
		
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		editFilter = new ChoiceItem(rootFrame, tPanel, c
				, "Edit Filter", "icons/Filter.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(FILTER_SELECTOR);}});
		editFilter.inactive();
		editFilter.setToolTipText("Edit Filter", "You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to edit filter.");
		
		c.gridx = 0;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageData = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Data", "icons/ManageData.png"
				, new Runnable(){public void run(){
					try {
						rootFrame.activePanel(MANAGE_DATA);
					} catch (Exception e) {
						e.printStackTrace();
					}
					}});
		manageData.inactive();
		manageData.setToolTipText("Manage Data", "You can manage your collections and your classes.");
		
		c.gridx = 1;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageMetaData = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Meta Data", "icons/MetaData.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_METADATA);}});
		manageMetaData.inactive();
		manageMetaData.setToolTipText("Manage Meta Data", "You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to manage meta data.");

		c.gridx = 2;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageRelationships = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Relationships", "icons/Relation.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_RELATIONS);}});
		manageRelationships.inactive();
		manageRelationships.setToolTipText("Manage Relationships", "You must select either a category (IMAGE, SPECTRUM, ...) or a class in order to manage relationships");

		tPanel = this.addSubPanel("Data publication");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		databaseInstallation = new ChoiceItem(rootFrame, tPanel, c
				, "Database Installation", "icons/Tool.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DB_INSTALL);}});
		databaseInstallation.setToolTipText("Database Installation", "You can manage the database installation configuration.");
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		webPublishing = new ChoiceItem(rootFrame, tPanel, c
				, "Web Publishing", "icons/Web.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(WEB_INSTALL);}});
		webPublishing.setToolTipText("Web Publishing", "You can manage the web publishing configuration.");
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		VOPublishing = new ChoiceItem(rootFrame, tPanel, c
				, "VO Publishing", "icons/ivoa.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(VO_PUBLISH);}});
		VOPublishing.setToolTipText("VO Publishing", "You can manage your VO and publish data.");

		// Add an event on a left panel tree in order to update the active ChoiceItem depending on the type of node
		JTree tree = rootFrame.metaDataTree.getTree();
		tree.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mouseReleased(MouseEvent e) 
			{
				setActiveChoiceItem();
			}
		});
	}
	
	// Procedure that updates the selected ChoiceItem depending on the type of node (root, collection, category or class node)
	private void setActiveChoiceItem()
	{
		TreePath treePath = rootFrame.metaDataTree.getClickedTreePath();
		DataTreePath dataTreePath;
		if (treePath!=null)
		{
			try
			{
				// Case : When a node is selected, it can be a collection, a category or a class
				dataTreePath = new DataTreePath(treePath);
				//Messenger.printMsg(Messenger.DEBUG, "isCollection : " + dataTreePath.isCollectionLevel() + ", isCategory : " + dataTreePath.isCategoryLevel() + ", isClasse : " + dataTreePath.isClassLevel());
				if (dataTreePath.isCollectionLevel()) // Collection selected
				{
					createCollection.active();
					loadData.inactive();
					editFilter.inactive();
					manageData.active();
					manageMetaData.inactive();
					manageRelationships.inactive();
				}
				else if (dataTreePath.isCategoryLevel()) // Category selected
				{
					createCollection.active();
					loadData.active();
					editFilter.active();
					manageData.active();
					manageMetaData.active();
					manageRelationships.active();
				}
				else if (dataTreePath.isClassLevel()) // Class selected
				{
					createCollection.active();
					loadData.active();
					editFilter.active();
					manageData.active();
					manageMetaData.active();
					manageRelationships.active();
				}
				else // Root node is selected
				{
					createCollection.active();
					loadData.inactive();
					editFilter.inactive();
					manageData.inactive();
					manageMetaData.inactive();
					manageRelationships.inactive();
					Messenger.printMsg(Messenger.INFO, "Root node selected");
				}
			}
			catch (QueryException e1) 
			{
				createCollection.active();
				loadData.inactive();
				editFilter.inactive();
				manageData.inactive();
				manageMetaData.inactive();
				manageRelationships.inactive();
			}
		}
	}

}
