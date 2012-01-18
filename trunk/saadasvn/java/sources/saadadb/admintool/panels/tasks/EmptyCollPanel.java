package saadadb.admintool.panels.tasks;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadDropCollection;
import saadadb.admintool.cmdthread.ThreadEmptyCollection;

public class EmptyCollPanel extends DropCollPanel {

	public EmptyCollPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, EMPTY_COLLECTION, new ThreadEmptyCollection(rootFrame), ancestor);
	}

	public void initCmdThread() {
		cmdThread = new ThreadEmptyCollection(rootFrame);
	}

}
