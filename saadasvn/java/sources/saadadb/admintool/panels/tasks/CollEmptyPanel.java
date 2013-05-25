package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadEmptyCollection;
import saadadb.admintool.utils.HelpDesk;

public class CollEmptyPanel extends CollDropPanel {

	public CollEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_COLLECTION, new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION), ancestor);
	}

	public void initCmdThread() {
		cmdThread = new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION);
	}

	protected void setHelpKey() {
		help_key = HelpDesk.COLL_EMPTY;
	}

}
