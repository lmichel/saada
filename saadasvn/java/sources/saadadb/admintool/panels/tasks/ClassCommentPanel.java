package saadadb.admintool.panels.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadCommentClass;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;

public class ClassCommentPanel extends ClassDropPanel {


	public ClassCommentPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, COMMENT_CLASS, new ThreadCommentClass(rootFrame, COMMENT_CLASS), ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected ClassCommentPanel(AdminTool rootFrame, String title,
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
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));
	}

	protected void setHelpKey() {
		help_key = HelpDesk.CLASS_COMMENT;
	}

	protected void setActivePanel() {
		super.setActivePanel();
		nameField.setEditable(false);
		commentField.setEditable(true);
	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", this.nameField.getText().trim());
		map.put("comment", this.commentField.getText().trim());
		return map;
	}

	public void initCmdThread() {
		cmdThread = new ThreadCommentClass(rootFrame, COMMENT_CLASS);
	}



}
