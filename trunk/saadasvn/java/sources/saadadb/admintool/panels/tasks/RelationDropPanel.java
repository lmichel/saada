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
 * @author laurentmichel
 *
 */
public class RelationDropPanel extends TaskPanel {

	protected  RelationshipChooser configChooser;
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
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) {
			try {
				setSelectedResource("", null);
				configChooser.setDataTreePath(dataTreePath);
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	@Override
	protected Map<String, Object> getParamMap() {
		System.out.println(configChooser.getSelectedRelation() );
		if( configChooser.getSelectedRelation() != null ) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			System.out.println("coucuou");
			map.put("relation", configChooser.getSelectedRelation());
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
		configChooser = new RelationshipChooser(this, runButton);
		MyGBC c = new MyGBC(5,5,5,5);
		tPanel.add(configChooser, c);
		c.newRow();
		tPanel.add(getHelpLabel(HelpDesk.RELATION_SELECTOR), c);
		

		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}
}
