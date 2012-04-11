package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.sqltable.Table_Saada_VO_DMVIew;
import saadadb.sqltable.Table_Tap_Schema_Tables;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capability;
import saadadb.vo.tap.DmServiceManager;
import saadadb.vo.tap.TapServiceManager;

public class ThreadDmViewPopulate extends CmdThread {
	private VOResource vor;
	private String className;

	public ThreadDmViewPopulate(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
		vor = (VOResource) params.get("dm");
		className = (String) params.get("class");

	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
		if( vor == null || className == null ) {
			return false;
		}		
		try {
			if( Database.getCachemeta().getClass(className) == null ){
				return false;
			}
			if( Table_Saada_VO_DMVIew.isClassReferenced(vor, className) ) {
				AdminComponent.showInfo(frame, "Class " + className + " is already referenced in table " + vor.getName());
				return false;
			}
		} catch (Exception e) {
			return false;
		} 	
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			/*
			 * Add class data to the DM table
			 */
			SQLTable.beginTransaction();
			DmServiceManager dsm = new DmServiceManager(vor);
			dsm.populate(new ArgsParser(new String[]{"-populate=" + className, Messenger.getDebugParam()}));
			SQLTable.beginTransaction();
			SQLTable.indexTable(vor.getName(), dsm);
			SQLTable.commitTransaction();			
			AdminComponent.showSuccess(frame, "Class " + className + " added to the view of the DM " + vor.getName());		
			/*
			 * Make TAP service really exist
			 */
			TapServiceManager tsm = new TapServiceManager();
			if( TapServiceManager.serviceExists() == 0 ) {
				if( AdminComponent.showConfirmDialog(frame, "No TAP service detected. Do you want to create it (needed to register the ObsCore table)?") ) {
						SQLTable.beginTransaction();
						Messenger.printMsg(Messenger.TRACE, "Create TAP service");
						tsm.create(null);
						SQLTable.commitTransaction();
				}	else {
					AdminComponent.showInfo(frame, "ObsCore table registration canceled");
					return;
				}
			}
			/*
			 * Make sure the DM is recorded to the TAP service
			 */
			if( !Table_Tap_Schema_Tables.knowsTable(vor.getName())) {
				ArgsParser ap = new ArgsParser(new String[]{"-populate=" + vor.getName(), Messenger.getDebugParam()});
				try {
					SQLTable.beginTransaction();
					tsm.populate(ap);
					Capability cpb = new Capability();
					cpb.setDataTreePath("ivoa." + vor.getName());
					cpb.setProtocol(Capability.TAP);
					cpb.setDescription("Table of data maiing the DM " + vor.getName());
					Table_Saada_VO_Capabilities.addCapability(cpb);
					SQLTable.commitTransaction();
					AdminComponent.showSuccess(frame, "ObsCore table added to the TAP service");
				} catch (SaadaException e1) {
					SQLTable.abortTransaction();
					if( e1.getMessage().equals(SaadaException.MISSING_RESOURCE)) {
						if( AdminComponent.showConfirmDialog(frame, "No TAP service detected. Do you want to create it?") ) {
							try {
								SQLTable.beginTransaction();
								tsm.create(null);
								tsm.populate(ap);
								Capability cpb = new Capability();
								cpb.setDataTreePath("ivoa." + vor.getName());
								cpb.setProtocol(Capability.TAP);
								cpb.setDescription("Table of data maiing the DM " + vor.getName());
								Table_Saada_VO_Capabilities.addCapability(cpb);
								SQLTable.commitTransaction();
								AdminComponent.showSuccess(frame, "ObsCore table added to the TAP service");
							} catch (Exception e) {
								throw new Exception(e.getMessage());
							}
						}
					}	
				}
			}
		} catch (AbortException e) {			
			Messenger.trapAbortException(e);
		} catch (Exception ae) {			
			SQLTable.abortTransaction();
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Pushing class " +  className + " in DM " + vor.getName() + " failed (see console).");
		}
	}

	@Override
	public String getAntTarget() {
		return null;
	}
}

