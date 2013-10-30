package saadadb.admintool.cmdthread;


import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.voresources.VOServiceItemSelector;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capability;
import saadadb.vo.tap.TapServiceManager;

/**
 * SAve a list of capabilities: Use of a thread because this operation can take time to set DataLinks
 * @author michel
 * @version $Id$
 *
 */
public class ThreadSaveTAPCapabilities extends CmdThread {
	protected VOServiceItemSelector itemSelector;

	public ThreadSaveTAPCapabilities(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		this.itemSelector = (VOServiceItemSelector) params.get("itemSelector");
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( itemSelector == null ) {
			AdminComponent.showFatalError(frame, "No list of VO capabilties to save (Inner error)");
			return  false;
		} else {
			return (!withConfirm 
					||
					AdminComponent.showConfirmDialog(frame, "Do you really want to save the >VO capabilities"));
		}
	}


	/* (non-Javadoc)
	 * @see gui.CmdThread#getParam()
	 */
	protected boolean getParam() {
		return true;
	}

	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	public void runCommand() {
		TapServiceManager tsm = new TapServiceManager();
		try {
			SQLTable.beginTransaction();
			Table_Saada_VO_Capabilities.emptyTable(Capability.TAP);
			itemSelector.saveCapabilities();
			SQLTable.commitTransaction();
			
			SQLTable.beginTransaction();
			tsm.removeAllTables();
			SQLTable.commitTransaction();
			
			tsm.synchronizeWithGlobalCapabilities();
			
			Messenger.printMsg(Messenger.TRACE, "Add selected tables to TAP service");
			itemSelector.loadCapabilities();
			AdminComponent.showSuccess(frame, "Exposed tables saved");
		} catch (SaadaException e1) {
			SQLTable.abortTransaction();
			if( e1.getMessage().equals(SaadaException.MISSING_RESOURCE)) {
				if( AdminComponent.showConfirmDialog(frame, "No TAP service detected. Do you want to create it?") ) {
					try {
						SQLTable.beginTransaction();
						Messenger.printMsg(Messenger.TRACE, "Create TAP service");
						tsm.create(null);
						SQLTable.commitTransaction();
						
						Messenger.printMsg(Messenger.TRACE, "Add selected tables to TAP service");
						tsm .synchronizeWithGlobalCapabilities();
						
						itemSelector.loadCapabilities();
						AdminComponent.showSuccess(frame, "Exposed tables saved");
					} catch (Exception e) {
						SQLTable.abortTransaction();
						AdminComponent.showFatalError(frame, e);
					}
				}

			} else {
				AdminComponent.showFatalError(frame, e1);
				return;
			}			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "VO capabilities saved");		
				}				
			});

		} catch (Exception e) {
			AdminComponent.showFatalError(frame, e);
		}
	}

	@Override
	public String getAntTarget() {
		return null;
	}
}
