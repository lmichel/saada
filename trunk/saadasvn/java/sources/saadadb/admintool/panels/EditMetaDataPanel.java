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

public class EditMetaDataPanel extends ChoicePanel {
	private static final long serialVersionUID = 1L;
	private ChoiceItem tagMetaData, manageExtendedAttributes;


	public EditMetaDataPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, ROOT_PANEL, null, ancestor);
	}

	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();

		tPanel = this.addSubPanel("Manage Meta Data");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		tagMetaData = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Meta Data", "icons/tagmeta.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(TAG_METADATA);}});
		tagMetaData.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.TAG_METADATA));

		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		manageExtendedAttributes = new ChoiceItem(rootFrame, tPanel, c
				, "Manage Extended Attributes", "icons/extenedAtt.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(MANAGE_EXTATTR);}});
		manageExtendedAttributes.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.MANAGE_EXTATTR));



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

	private void setActiveChoiceItem(TreePath treePath) 
	{
		DataTreePath dataTreePath;
		if (treePath!=null)
		{
			try
			{
				// Case : When a node is selected, it can be a collection, a category or a class
				dataTreePath = new DataTreePath(treePath);
				if (dataTreePath.isCategoryLevel()) // Collection selected
				{
					tagMetaData.active();
					manageExtendedAttributes.active();
				}
				else {
					tagMetaData.inactive();
				    manageExtendedAttributes.inactive();
				}
			}
			catch (QueryException e1) 
			{
				tagMetaData.inactive();
			    manageExtendedAttributes.inactive();
			}
		}
	}
}
