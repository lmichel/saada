package saadadb.admintool.panels.editors;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;

public class LogsDisplayPanel extends EditPanel 
{
	private static final long serialVersionUID = 1L;

	public LogsDisplayPanel(AdminTool rootFrame, String ancestor) 
	{
		super(rootFrame, AdminComponent.LOGS_DISPLAY_ADMINTOOL, null, ancestor);
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
		JPanel tPanel = this.addSubPanel("List of application logs", false);
	}

	@Override
	public void active() {}
}
