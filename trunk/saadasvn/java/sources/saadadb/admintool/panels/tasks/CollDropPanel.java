package saadadb.admintool.panels.tasks;

import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadDropCollection;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaCollection;

public class CollDropPanel extends CollCreatePanel {


	public CollDropPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, DROP_COLLECTION, new ThreadDropCollection(rootFrame, DROP_COLLECTION), ancestor);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected CollDropPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
	}

	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootLevel())
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
		if( this.isDataTreePathLocked() ){
			showInputError(rootFrame, "Can not change data treepath in this context");
		}else  if( dataTreePath != null ) {
			super.setDataTreePath(dataTreePath);
			treePathLabel.setText(dataTreePath.collection);
			MetaCollection mc;
			try {
				mc = Database.getCachemeta().getCollection(dataTreePath.collection);
				nameField.setText(mc.getName());
				commentField.setText(mc.getDescription());
			} catch (FatalException e) {
				showFatalError(rootFrame, e);
			}
		}
	}
	
	protected void setHelpKey() {
		help_key = HelpDesk.COLL_DROP;
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
		cmdThread = new ThreadDropCollection(rootFrame, DROP_COLLECTION);
	}



}
