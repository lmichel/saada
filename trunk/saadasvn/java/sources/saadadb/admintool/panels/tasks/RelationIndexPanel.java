/**
 * 
 */
package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadRelationIndex;
import saadadb.admintool.utils.DataTreePath;


/**
 * @author laurentmichel
 *
 */
public class RelationIndexPanel extends RelationDropPanel {

	public RelationIndexPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, INDEX_RELATION, null, ancestor);
		cmdThread = new ThreadRelationIndex(rootFrame, INDEX_RELATION);
	}

	/**
	 * Used by subclasses
	 * @param rootFrame
	 * @param title
	 * @param cmdThread
	 * @param ancestor
	 */
	protected RelationIndexPanel(AdminTool rootFrame, String title,
			CmdThread cmdThread, String ancestor) {
		super(rootFrame, title, cmdThread, ancestor);
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationIndex(rootFrame, INDEX_RELATION);
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
	
}
