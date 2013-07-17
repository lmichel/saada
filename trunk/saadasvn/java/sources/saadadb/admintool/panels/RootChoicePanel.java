package saadadb.admintool.panels;

import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ChoiceItem;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.ToolTipTextDesk;
import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;

public class RootChoicePanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;

	public RootChoicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, ROOT_PANEL, null, ancestor);
	}
	
	private ChoiceItem createCollection, loadData, exploreData, 
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
		createCollection.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.CREATE_COLLECTION));
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		loadData = new ChoiceItem(rootFrame, tPanel, c
				, "Load Data", "icons/LoadData.png"
				, new Runnable(){public void run(){rootFrame.activePanel(DATA_LOADER);}});
		loadData.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DATA_LOADER));
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		exploreData = new ChoiceItem(rootFrame, tPanel, c
				, "Explore Data", "icons/ExploreData.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EXPLORE_DATA);}});
		exploreData.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.EXPLORE_DATA));
		
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
		manageData.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.MANAGE_DATA));
		
		c.gridx = 1;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageMetaData = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Meta Data", "icons/MetaData.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_METADATA);}});
		manageMetaData.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.MANAGE_METADATA));

		c.gridx = 2;
		c.gridy = 1;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageRelationships = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Relationships", "icons/Relation.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_RELATIONS);}});
		manageRelationships.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.MANAGE_RELATIONS));

		tPanel = this.addSubPanel("Data publication");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		databaseInstallation = new ChoiceItem(rootFrame, tPanel, c
				, "Database Installation", "icons/Tool.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DB_INSTALL);}});
		databaseInstallation.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DB_INSTALL));

		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		webPublishing = new ChoiceItem(rootFrame, tPanel, c
				, "Web Publishing", "icons/Web.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(WEB_INSTALL);}});
		webPublishing.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.WEB_INSTALL));

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		VOPublishing = new ChoiceItem(rootFrame, tPanel, c
				, "VO Publishing", "icons/ivoa.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(VO_PUBLISH);}});
		VOPublishing.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.VO_PUBLISH));

		// Enable only "CreateCollection" when the application is launched
		updateStateChoiceItem(true, false, false, false, false, false);

		// Add an event on a left panel tree in order to update the active ChoiceItem depending on the type of node
		JTree tree = rootFrame.metaDataTree.getTree();
		tree.addTreeSelectionListener(new TreeSelectionListener() 
		{
			@Override
		    public void valueChanged(TreeSelectionEvent e) 
		    {
				setActiveChoiceItem(e.getPath());
		    }
		});
	}
	
	// Procedure that updates the selected ChoiceItem depending on the type of node (root, collection, category or class node)
	private void setActiveChoiceItem(TreePath treePath)
	{
		DataTreePath dataTreePath;
		if (treePath!=null)
		{
			try
			{
				// Case : When a node is selected, it can be a collection, a category or a class
				dataTreePath = new DataTreePath(treePath);
				if (dataTreePath.isCollectionLevel()) // Collection selected
				{
					updateStateChoiceItem(true, false, false, true, false, false);
				}
				else if (dataTreePath.isCategoryLevel()) // Category selected
				{
					updateStateChoiceItem(true, true, true, true, true, true);
				}
				else if (dataTreePath.isClassLevel()) // Class selected
				{
					updateStateChoiceItem(true, true, true, true, true, true);
				}
				else // Root node is selected
				{
					updateStateChoiceItem(true, false, false, false, false, false);
				}
			}
			catch (QueryException e1) 
			{
				updateStateChoiceItem(true, false, false, false, false, false);
			}
		}
	}
	
	private void updateStateChoiceItem(boolean createCollectionActive, boolean loadDataActive, boolean exploreDataActive,
			boolean manageDataActive, boolean manageMetaDataActive, boolean manageRelationshipsActive)
	{
		if (createCollectionActive) { createCollection.active(); } else { createCollection.inactive(); }
		if (loadDataActive) { loadData.active(); } else { loadData.inactive(); }
		if (exploreDataActive) { exploreData.active(); } else { exploreData.inactive(); }
		if (manageDataActive) { manageData.active(); } else { manageData.inactive(); }
		if (manageMetaDataActive) { manageMetaData.active(); } else { manageMetaData.inactive(); }
		if (manageRelationshipsActive) { manageRelationships.active(); } else { manageRelationships.inactive(); }
	}

}
