/**
 * 
 */
package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadRelationIndex;


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

}
