package saadadb.admintool.panels.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadCommentCollection;
import saadadb.admintool.components.ToolBarPanel;

public class CollCommentPanel extends CollDropPanel {

	
	public CollCommentPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, COMMENT_COLLECTION, new ThreadCommentCollection(rootFrame), ancestor);
	}
	
	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected CollCommentPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
	}

	
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
	
	protected void setActivePanel() {
		super.setActivePanel();
		nameField.setEditable(false);
		commentField.setEditable(true);
	}
	
	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("name", this.nameField.getText());
		map.put("comment", this.commentField.getText());
		return map;
	}
	
	public void initCmdThread() {
		cmdThread = new ThreadCommentCollection(rootFrame);
	}



}
