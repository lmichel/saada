/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadRelationDrop;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;



/**
 * @author michel
 * @version $Id$
 *
 */
public class RelationDropPanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	protected  RelationshipChooser relationChooser;
	protected RunTaskButton runButton;


	public RelationDropPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, DROP_RELATION, null, ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected RelationDropPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.cmdThread = cmdThread;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a collection, a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) {
			try {
				setSelectedResource("", null);
				relationChooser.setDataTreePath(dataTreePath);
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	@Override
	protected Map<String, Object> getParamMap() {
		if( relationChooser.getSelectedRelation() != null ) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("relation", relationChooser.getSelectedRelation());
			return map;
		}
		else {
			return null;	
		}
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationDrop(rootFrame, DROP_RELATION);
	}

	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);

		JPanel tPanel = this.addSubPanel("Relationship Selector");
		relationChooser = new RelationshipChooser(this, runButton);
		MyGBC c = new MyGBC(5,5,5,5);
		tPanel.add(relationChooser, c);
		

		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}
}
