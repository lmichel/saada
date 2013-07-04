package saadadb.admintool.panels.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadEmptyClass;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;

public class ClassEmptyPanel extends ClassDropPanel {
	
	public ClassEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_CLASS, new ThreadEmptyClass(rootFrame, EMPTY_CLASS), ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected ClassEmptyPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
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

	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}

	protected void setHelpKey() {
		help_key = HelpDesk.CLASS_EMPTY;
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
		map.put("comment", this.commentField.getText());
		return map;
	}

	public void initCmdThread() {
		cmdThread = new ThreadEmptyClass(rootFrame, EMPTY_CLASS);
	}



}
