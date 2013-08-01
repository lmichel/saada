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

public class VOPublishPanel extends ChoicePanel {
	
	private static final long serialVersionUID = 1L;
	private ChoiceItem publishSIA, publishSSA, publishSCS, 
	publishTAPService, publishObscoreTable, publishUserDefinedDM, VOServicesSummary;
	

	public VOPublishPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, VO_PUBLISH, null, ancestor);
	}


	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		GridBagConstraints c = new GridBagConstraints();
		
		tPanel = this.addSubPanel("Simple Protocols");
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		publishSIA = new ChoiceItem(rootFrame, tPanel, c
				, "Publish SIA", "icons/SIA.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(SIA_PUBLISH);}});
		publishSIA.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.SIA_PUBLISH));
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		publishSSA = new ChoiceItem(rootFrame, tPanel, c
				, "Publish SSA", "icons/SSA.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(SSA_PUBLISH);}});
		publishSSA.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.SSA_PUBLISH));
		
		c.gridx = 2;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		publishSCS = new ChoiceItem(rootFrame, tPanel, c
				, "Publish SCS", "icons/ConeSearch.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(CONESEARCH_PUBLISH);}});
		publishSCS.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.CONESEARCH_PUBLISH));

		tPanel = this.addSubPanel("TAP Service");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		publishTAPService = new ChoiceItem(rootFrame, tPanel, c
				, "Publish TAP service", "icons/TAP.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(TAP_PUBLISH);}});
		publishTAPService.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.TAP_PUBLISH));
		
		c.gridx = 1;
		c.gridy = 0;	
		c.weightx = 0.5;
		c.weighty = 0.5;
		publishObscoreTable = new ChoiceItem(rootFrame, tPanel, c
				, "Publish ObsCore Table", "icons/ObsCore.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(OBSCORE_MAPPER);}});
		publishObscoreTable.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.OBSCORE_MAPPER));
		publishObscoreTable.inactive();
		
		c.gridx = 2;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;	
		publishUserDefinedDM = new ChoiceItem(rootFrame, tPanel, c
				, "Publish User Defined DM", "icons/UserModel.png"
				, new Runnable(){public void run(){
					System.out.println("user DM");}});
		publishUserDefinedDM.inactive();
		publishUserDefinedDM.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.USER_DEFINED_DM));
		
		tPanel = this.addSubPanel("Summary");		
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		VOServicesSummary = new ChoiceItem(rootFrame, tPanel, c
				, "VO Registry", "icons/VOServices.png"
				, new Runnable(){public void run(){
					rootFrame.activePanel(VO_REGISTRY);}});
		VOServicesSummary.setToolTipText(ToolTipTextDesk.get(ToolTipTextDesk.VO_REGISTRY));
		
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
				if (dataTreePath.isCollectionLevel() || dataTreePath.isCategoryLevel()) // Collection selected
				{
					publishObscoreTable.inactive();
				}
				else if (dataTreePath.isClassLevel()) // Category or class selected
				{
					publishObscoreTable.active();
				}
				else // Root node is selected
				{
					publishObscoreTable.inactive();
				}
			}
			catch (QueryException e1) 
			{
				publishObscoreTable.inactive();
			}
		}
	}

}
