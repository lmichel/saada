package saadadb.admintool.panels.editors;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.VOResourceChooser;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;

public class VOProtocolFieldsPanel extends EditPanel 
{
	protected VOResourceChooser resourceChooser;
	
	public VOProtocolFieldsPanel(AdminTool rootFrame, String ancestor)
	{
		super(rootFrame, AdminComponent.VO_PROTOCOL_FIELDS, null, ancestor);
		// TODO thread
	}

	private static final long serialVersionUID = 1L;
	
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
		JPanel tPanel = this.addSubPanel("List of protocols");
		resourceChooser = new VOResourceChooser(this, VOResourceChooser.VO_PROTOCOL_FIELDS);
		MyGBC c = new MyGBC(5,5,5,5);
		tPanel.add(resourceChooser, c);
	}

	@Override
	public void active() {}
}
