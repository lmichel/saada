package saadadb.admintool.panels.editors;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.VOResourceChooser;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;

public class VOPublishedResourcesPanel extends EditPanel
{
	private static final long serialVersionUID = 1L;
	private VOResourceChooser resourceChooser;
	
	public VOPublishedResourcesPanel(AdminTool rootFrame, String ancestor)
	{
		super(rootFrame, AdminComponent.VO_PUBLISHED_RESOURCES, null, ancestor);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() 
	{
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) 
	{
		// Nothing for the moment
	}

	@Override
	protected void setActivePanel() 
	{
		JPanel tPanel = this.addSubPanel("List of published VO Resources", false);
		resourceChooser = new VOResourceChooser(this, VOResourceChooser.VO_PUBLISHED_RESOURCES);
		MyGBC c = new MyGBC(5,5,5,5);
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH; c.gridwidth = 2;
		tPanel.add(resourceChooser, c);

	}

	@Override
	public void active() {}
}
