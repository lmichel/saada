package saadadb.admintool.cmdthread;


import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.voresources.VOServiceItemSelector;
import saadadb.admintool.panels.AdminPanel;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;

/**
 * SAve a list of SAPcapabilities: Use of a thread because this operation can take time to set DataLinks
 * @author michel
 * @version $Id$
 *
 */
public class ThreadSaveSAPCapabilities extends ThreadSaveTAPCapabilities {

	public ThreadSaveSAPCapabilities(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}

	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	public void runCommand() {
		try {
			SQLTable.beginTransaction();
			Table_Saada_VO_Capabilities.emptyTable(itemSelector.getProtocol());
			itemSelector.saveCapabilities();
			SQLTable.commitTransaction();
			itemSelector.loadCapabilities();
			AdminPanel.showSuccess(frame, itemSelector.getProtocol() + "capabilities saved");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "VO capabilities saved");		
				}				
			});
		} catch (Exception e) {
			SQLTable.abortTransaction();
			AdminPanel.showFatalError(frame, e);
		}				
	}
}
