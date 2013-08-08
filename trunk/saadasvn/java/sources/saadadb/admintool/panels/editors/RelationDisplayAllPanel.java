package saadadb.admintool.panels.editors;

import java.util.Map;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

public class RelationDisplayAllPanel extends TaskPanel
{
	private JPanel tPanel;
	private RelationshipChooser relationChooser;
	private static final long serialVersionUID = 1L;
	
	public RelationDisplayAllPanel(AdminTool rootFrame, String ancestor) 
	{
		super(rootFrame, AdminComponent.DISPLAY_RELATION, null, ancestor);
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

	@Override
	protected void setActivePanel() 
	{
		tPanel = this.addSubPanel("Display All Relationships", false);
		relationChooser = new RelationshipChooser(this, null, RelationshipChooser.ALL_RELATIONS);
		MyGBC c = new MyGBC(5,5,5,5);
		tPanel.add(relationChooser, c);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) 
	{
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null)
		{
			try 
			{
				relationChooser.setDataTreePath(dataTreePath);
			} 
			catch (FatalException e) 
			{
				Messenger.trapFatalException(e);
			}
		}
	}

	@Override
	public void initCmdThread() {}

	@Override
	protected Map<String, Object> getParamMap() { return null; }
}
