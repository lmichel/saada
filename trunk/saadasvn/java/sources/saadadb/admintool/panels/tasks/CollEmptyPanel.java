package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadEmptyCollection;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;

public class CollEmptyPanel extends CollDropPanel {

	public CollEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_COLLECTION, new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION), ancestor);
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

	public void initCmdThread() {
		cmdThread = new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION);
	}

	protected void setHelpKey() {
		help_key = HelpDesk.COLL_EMPTY;
	}

}
