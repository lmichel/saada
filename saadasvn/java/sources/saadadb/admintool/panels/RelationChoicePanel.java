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

public class RelationChoicePanel extends ChoicePanel {
	
	private static final long serialVersionUID = 1L;
	private ChoiceItem newRelationship, commentRelationship, dropRelationship,
	populateRelationship, indexRelationship, emptyRelationship;

	public RelationChoicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, MANAGE_RELATIONS, null, ancestor);
	}
	
	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		
		tPanel = this.addSubPanel("Relationship Management");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		newRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "New Relationship", "icons/CreateRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(CREATE_RELATION);}});
		newRelationship.setToolTipText("New Relationship", "Create a new relationship from the selected table or category.");
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		commentRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Comment Relationship", "icons/CommentRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_RELATION);}});
		commentRelationship.setToolTipText("Comment Relationship", "Comment a relationship from the selected table or category.");
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		dropRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Drop Relationship", "icons/DropRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_RELATION);}});
		dropRelationship.setToolTipText("Drop Relationship", "Drop a relationship from the selected table or category.");

		tPanel = this.addSubPanel("Link Management");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		populateRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Populate relationship", "icons/PopulateRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(POPULATE_RELATION);}});
		populateRelationship.setToolTipText("Populate Relationship", "Populate a relationship from the selected table or category.");
		
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		indexRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Index Relationship", "icons/IndexRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(INDEX_RELATION);}});
		indexRelationship.setToolTipText("Index Relationship", "Create a new index on a relationship.");
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Relationship", "icons/EmptyRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_RELATION);}});
		emptyRelationship.setToolTipText("Empty Relationship", "Empty a relationship from the selected table or category.");
		
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
				if (dataTreePath.isCollectionLevel()) // Collection selected
				{
					updateStateChoiceItem(false, false, false, false, false, false);
				}
				else if (dataTreePath.isCategorieOrClassLevel()) // Category or class selected
				{
					updateStateChoiceItem(true, true, true, true, true, true);
				}
				else // Root node is selected
				{
					updateStateChoiceItem(false, false, false, false, false, false);
					Messenger.printMsg(Messenger.INFO, "Root node selected");
				}
			}
			catch (QueryException e1) 
			{
				updateStateChoiceItem(false, false, false, false, false, false);
			}
		}
	}
	
	private void updateStateChoiceItem(boolean newRelationshipActive, boolean commentRelationshipActive, boolean dropRelationshipActive,
			boolean populateRelationshipActive, boolean indexRelationshipActive, boolean emptyRelationshipActive)
	{
		if (newRelationshipActive) { newRelationship.active(); } else { newRelationship.inactive(); }
		if (commentRelationshipActive) { commentRelationship.active(); } else { commentRelationship.inactive(); }
		if (dropRelationshipActive) { dropRelationship.active(); } else { dropRelationship.inactive(); }
		if (populateRelationshipActive) { populateRelationship.active(); } else { populateRelationship.inactive(); }
		if (indexRelationshipActive) { indexRelationship.active(); } else { indexRelationship.inactive(); }
		if (emptyRelationshipActive) { emptyRelationship.active(); } else { emptyRelationship.inactive(); }
	}

}
