/**
 * 
 */
package saadadb.admintool.panels;

import java.awt.GridBagConstraints;

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

/**
 * @author laurentmichel
 *
 */
public class ManageDataPanel extends ChoicePanel {

	private static final long serialVersionUID = 1L;
	private ChoiceItem commentCollection, emptyCollection, emptyCategory, removeCollection,
	commentClass, emptyClass, removeClass, SQLIndex;
	
	public ManageDataPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, MANAGE_DATA, null, ancestor);
	}
	
	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
//		
//		tPanel = this.addSubPanel("Data Loader");
//		c.gridx = 0;
//		c.gridy = 0;	
//		c.weightx = 0.5;
//		c.weighty = 0.5;
//		new ChoiceItem(rootFrame, tPanel, c
//				, "Load Data", "icons/LoadData.png"
//				, new Runnable(){public void run(){
//					rootFrame.activePanel(LOAD_DATA);}});
//		c.gridx = 1;
//		c.gridy = 0;	
//		c.weightx = 0.5;
//		c.weighty = 0.5;
//		(new ChoiceItem(rootFrame, tPanel, c
//				, "Statistic", "icons/Statistics.png"
//				, new Runnable(){public void run(){
//					System.out.println("loaddata");}})).inactive();
		
		tPanel = this.addSubPanel("Collection Level");
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		commentCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Comment Collection", "icons/CommentCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_COLLECTION);}});
		commentCollection.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.COMMENT_COLLECTION));

		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Collection", "icons/EmptyCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_COLLECTION);}});
		emptyCollection.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.EMPTY_COLLECTION));

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		emptyCategory = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Category", "icons/EmptyCategory.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CATEGORY);}});
		emptyCategory.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.EMPTY_CATEGORY));

		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		removeCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Remove Collection", "icons/DropCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_COLLECTION);}});	
		removeCollection.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DROP_COLLECTION));

		tPanel = this.addSubPanel("Class Level");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		commentClass = new ChoiceItem(rootFrame, tPanel, c
				, "Comment Class", "icons/CommentClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_CLASS);}});
		commentClass.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.COMMENT_CLASS));

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyClass = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Class", "icons/EmptyClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CLASS);}});
		emptyClass.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.EMPTY_CLASS));

		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		removeClass = new ChoiceItem(rootFrame, tPanel, c
				, "Remove Class", "icons/DropClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_CLASS);}});		
		removeClass.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DROP_CLASS));
		
		tPanel = this.addSubPanel("Manage SQL Index");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		SQLIndex = new ChoiceItem(rootFrame, tPanel, c
				, "SQL Index", "icons/IndexTable.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(SQL_INDEX);}});
		SQLIndex.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.SQL_INDEX));

		// Necessary when the panel is first called, you must know what kind of node is it and directly updates the ChoiceItem.
		setActiveChoiceItem(rootFrame.metaDataTree.getClickedTreePath());
		
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
				//Messenger.printMsg(Messenger.DEBUG, "isCollection : " + dataTreePath.isCollectionLevel() + ", isCategory : " + dataTreePath.isCategoryLevel() + ", isClasse : " + dataTreePath.isClassLevel());
				if (dataTreePath.isCollectionLevel()) // Collection selected
				{
					updateStateChoiceItem(true, true, false, true, false, false, false, false);
				}
				else if (dataTreePath.isCategoryLevel()) // Category selected
				{
					updateStateChoiceItem(true, true, true, true, false, false, false, true);
				}
				else if (dataTreePath.isClassLevel()) // Class selected
				{
					updateStateChoiceItem(true, true, true, true, true, true, true, true);
				}
				else // Root node is selected
				{
					updateStateChoiceItem(false, false, false, false, false, false, false, false);
				}
			}
			catch (QueryException e1) 
			{
				updateStateChoiceItem(false, false, false, false, false, false, false, false);
			}
		}
	}

	private void updateStateChoiceItem(boolean commentCollectionActive, boolean emptyCollectionActive, 
			boolean emptyCategoryActive, boolean removeCollectionActive, boolean commentClassActive, 
			boolean emptyClassActive, boolean removeClassActive, boolean SQLIndexActive)
	{
		if (commentCollectionActive) { commentCollection.active(); } else { commentCollection.inactive(); }
		if (emptyCollectionActive) { emptyCollection.active(); } else { emptyCollection.inactive(); }
		if (emptyCategoryActive) { emptyCategory.active(); } else { emptyCategory.inactive(); }
		if (removeCollectionActive) { removeCollection.active(); } else { removeCollection.inactive(); }
		if (commentClassActive) { commentClass.active(); } else { commentClass.inactive(); }
		if (emptyClassActive) { emptyClass.active(); } else { emptyClass.inactive(); }
		if (removeClassActive) { removeClass.active(); } else { removeClass.inactive(); }
		if (SQLIndexActive) { SQLIndex.active(); } else { SQLIndex.inactive(); }
	}
}
