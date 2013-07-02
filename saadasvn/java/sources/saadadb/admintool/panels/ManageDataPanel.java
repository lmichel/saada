/**
 * 
 */
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
		commentCollection.setToolTipText("Comment Collection", "You can add a description of the selected collection.");
		commentCollection.inactive();
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Collection", "icons/EmptyCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_COLLECTION);}});
		emptyCollection.setToolTipText("Empty Collection", "You can empty the selected collection.");
		emptyCollection.inactive();
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		emptyCategory = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Category", "icons/EmptyCategory.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CATEGORY);}});
		emptyCategory.setToolTipText("Empty Category", "You can empty the selected category.");
		emptyCategory.inactive();
		
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		removeCollection = new ChoiceItem(rootFrame, tPanel, c
				, "Remove Collection", "icons/DropCollection.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_COLLECTION);}});	
		removeCollection.setToolTipText("Remove Collection", "You can remove the selected collection.");
		removeCollection.inactive();
		
		tPanel = this.addSubPanel("Class Level");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		commentClass = new ChoiceItem(rootFrame, tPanel, c
				, "Comment Class", "icons/CommentClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_CLASS);}});
		commentClass.setToolTipText("Comment Class", "You can comment the selected class.");
		commentClass.inactive();
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyClass = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Class", "icons/EmptyClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_CLASS);}});
		emptyClass.setToolTipText("Empty Class", "You can empty the selected class.");
		emptyClass.inactive();
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		removeClass = new ChoiceItem(rootFrame, tPanel, c
				, "Remove Class", "icons/DropClass.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_CLASS);}});		
		removeClass.setToolTipText("Remove Class", "You can remove the selected class.");
		removeClass.inactive();
		
		tPanel = this.addSubPanel("Manage SQL Index");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		SQLIndex = new ChoiceItem(rootFrame, tPanel, c
				, "SQL Index", "icons/IndexTable.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(SQL_INDEX);}});
		SQLIndex.setToolTipText("SQLIndex", "You can add a SQL Index in the selected category or table.");
		SQLIndex.inactive();
		
		// Necessary when the panel is first called, you must know what kind of node is it and directly updates the ChoiceItem.
		setActiveChoiceItem();
		
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
					// Collection level
					commentCollection.active();
					emptyCollection.active();
					emptyCategory.inactive();
					removeCollection.active();
					// Class level
					commentClass.inactive();
					emptyClass.inactive();
					removeClass.inactive();
					// SQLIndex
					SQLIndex.inactive();
				}
				else if (dataTreePath.isCategoryLevel()) // Category selected
				{
					// Collection level
					commentCollection.active();
					emptyCollection.active();
					emptyCategory.active();
					removeCollection.active();
					// Class level
					commentClass.inactive();
					emptyClass.inactive();
					removeClass.inactive();
					// SQLIndex
					SQLIndex.active();
				}
				else if (dataTreePath.isClassLevel()) // Class selected
				{
					// Collection level
					commentCollection.active();
					emptyCollection.active();
					emptyCategory.active();
					removeCollection.active();
					// Class level
					commentClass.active();
					emptyClass.active();
					removeClass.active();
					// SQLIndex
					SQLIndex.active();
				}
				else // Root node is selected
				{
					// Collection level
					commentCollection.inactive();
					emptyCollection.inactive();
					emptyCategory.inactive();
					removeCollection.inactive();
					// Class level
					commentClass.inactive();
					emptyClass.inactive();
					removeClass.inactive();
					// SQLIndex
					SQLIndex.inactive();
					Messenger.printMsg(Messenger.INFO, "Root node selected");
				}
			}
			catch (QueryException e1) 
			{
				// Collection level
				commentCollection.inactive();
				emptyCollection.inactive();
				emptyCategory.inactive();
				removeCollection.inactive();
				// Class level
				commentClass.inactive();
				emptyClass.inactive();
				removeClass.inactive();
				// SQLIndex
				SQLIndex.inactive();
			}
		}
	}


}
