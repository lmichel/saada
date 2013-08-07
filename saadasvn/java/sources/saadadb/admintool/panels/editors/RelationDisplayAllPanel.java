package saadadb.admintool.panels.editors;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;

public class RelationDisplayAllPanel extends EditPanel
{
	private JPanel tPanel;
	private static final long serialVersionUID = 1L;
	
	public RelationDisplayAllPanel(AdminTool rootFrame, String ancestor) 
	{
		super(rootFrame, DISPLAY_RELATION, null, ancestor);
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
		tPanel = this.addSubPanel("Display All Relationships", false);
	}

	@Override
	public void active() {}
}
