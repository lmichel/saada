package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadDropCollection;
import saadadb.admintool.cmdthread.ThreadEmptyCollection;

public class CollEmptyPanel extends CollDropPanel {

	public CollEmptyPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_COLLECTION, new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION), ancestor);
	}

	public void initCmdThread() {
		cmdThread = new ThreadEmptyCollection(rootFrame, EMPTY_COLLECTION);
	}

}
