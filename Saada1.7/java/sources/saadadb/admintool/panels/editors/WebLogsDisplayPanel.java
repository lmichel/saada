package saadadb.admintool.panels.editors;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.LogsDisplayer;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;

public class WebLogsDisplayPanel extends EditPanel 
{
	private static final long serialVersionUID = 1L;

	public WebLogsDisplayPanel(AdminTool rootFrame, String ancestor) 
	{
		super(rootFrame, AdminComponent.LOGS_DISPLAY_WEB, null, ancestor);
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
		JPanel tPanel = this.addSubPanel("Recorded Web Application Logs", false);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		tPanel.add(new LogsDisplayer(AdminComponent.LOGS_DISPLAY_WEB), c);
	}

	@Override
	public void active() {}
}
