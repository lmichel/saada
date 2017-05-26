package saadadb.admintool.panels.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadDropClass;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaClass;

@SuppressWarnings("serial")
public class ClassDropPanel extends CollCreatePanel {


	public ClassDropPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, DROP_CLASS, new ThreadDropClass(rootFrame, DROP_CLASS), ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected ClassDropPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
	}

	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (!dataTreePath.isClassLevel())
		{
			showInputError(rootFrame, "You must select a class.");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( this.isDataTreePathLocked() ){
			showInputError(rootFrame, "Can not change data treepath in this context");
		}else  if( dataTreePath != null ) {
			if( !dataTreePath.isClassLevel() ) {
				showInputError(rootFrame, "Data tree node must be selected at class level (leaf)");
				return;
			}

			super.setDataTreePath(dataTreePath);
			/*treePathLabel.setText(dataTreePath.collection + "." + dataTreePath.category + "." + dataTreePath.classe);
			treePathLabel.setText(dataTreePath.collection);*/
			this.setTextTreePathPanel(dataTreePath);
			MetaClass mc;
			try {
				mc = Database.getCachemeta().getClass(dataTreePath.classe);
				nameField.setText(mc.getName());
				commentField.setText(mc.getDescription());
			} catch (FatalException e) {
				showFatalError(rootFrame, e);
			}
		}
	}
	
	protected void setHelpKey() {
		help_key = HelpDesk.CLASS_DROP;
	}
	protected void setNodeLabel() {
		nodeLabel = getPlainLabel("Class Name");
	}

	protected void setActivePanel() {
		super.setActivePanel();
		nameField.setEditable(false);
		commentField.setEditable(false);

	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", this.nameField.getText());
		return map;
	}

	public void initCmdThread() {
		cmdThread = new ThreadDropClass(rootFrame, DROP_CLASS);
	}
}
