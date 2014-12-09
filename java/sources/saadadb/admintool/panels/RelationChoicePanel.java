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

public class RelationChoicePanel extends ChoicePanel {
	
	private static final long serialVersionUID = 1L;
	private ChoiceItem newRelationship, commentRelationship, dropRelationship,
	populateRelationship, indexRelationship, emptyRelationship, displayAllRelationships;

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
		newRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.CREATE_RELATION));

		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		commentRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Comment Relationship", "icons/CommentRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(COMMENT_RELATION);}});
		commentRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.COMMENT_RELATION));

		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		dropRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Drop Relationship", "icons/DropRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DROP_RELATION);}});
		dropRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DROP_RELATION));
		
		tPanel = this.addSubPanel("Link Management");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		populateRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Populate relationship", "icons/PopulateRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(POPULATE_RELATION);}});
		populateRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.POPULATE_RELATION));

		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		indexRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Index Relationship", "icons/IndexRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(INDEX_RELATION);}});
		indexRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.INDEX_RELATION));

		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		emptyRelationship = new ChoiceItem(rootFrame, tPanel, c
				, "Empty Relationship", "icons/EmptyRel.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(EMPTY_RELATION);}});
		emptyRelationship.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.EMPTY_RELATION));
		
		tPanel = this.addSubPanel("Explore Relationships");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		displayAllRelationships = new ChoiceItem(rootFrame, tPanel, c
				, "Display All Relationships", "icons/DisplayRelations.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(DISPLAY_RELATION);}});
		displayAllRelationships.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.DISPLAY_RELATION));

		updateStateChoiceItem(false, false, false, false, false, false, true);
		
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
				if (dataTreePath.isCollectionLevel()) // Collection selected
				{
					updateStateChoiceItem(false, false, false, false, false, false, true);
				}
				else if (dataTreePath.isCategorieOrClassLevel()) // Category or class selected
				{
					updateStateChoiceItem(true, true, true, true, true, true, true);
				}
				else // Root node is selected
				{
					updateStateChoiceItem(false, false, false, false, false, false, true);
				}
			}
			catch (QueryException e1) 
			{
				updateStateChoiceItem(false, false, false, false, false, false, true);
			}
		}
	}
	
	private void updateStateChoiceItem(boolean newRelationshipActive, boolean commentRelationshipActive, boolean dropRelationshipActive,
			boolean populateRelationshipActive, boolean indexRelationshipActive, boolean emptyRelationshipActive, boolean displayAllRelationshipsActive)
	{
		if (newRelationshipActive) { newRelationship.active(); } else { newRelationship.inactive(); }
		if (commentRelationshipActive) { commentRelationship.active(); } else { commentRelationship.inactive(); }
		if (dropRelationshipActive) { dropRelationship.active(); } else { dropRelationship.inactive(); }
		if (populateRelationshipActive) { populateRelationship.active(); } else { populateRelationship.inactive(); }
		if (indexRelationshipActive) { indexRelationship.active(); } else { indexRelationship.inactive(); }
		if (emptyRelationshipActive) { emptyRelationship.active(); } else { emptyRelationship.inactive(); }
		if (displayAllRelationshipsActive) { displayAllRelationships.active(); } else { displayAllRelationships.inactive(); }
	}

}
