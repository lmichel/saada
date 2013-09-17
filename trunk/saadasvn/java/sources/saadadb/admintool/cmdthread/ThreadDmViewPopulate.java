package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.util.Map;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.tasks.ObscoreMapperPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
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
	private ObscoreMapperPanel parent;
	public ThreadDmViewPopulate(ObscoreMapperPanel parent, String taskTitle) {
		super(parent.rootFrame, taskTitle);
		this.parent = parent;
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
				return AdminComponent.showConfirmDialog(frame, "Class " + className + " is already referenced in table " 
						+ vor.getName() 
						+ ". \nDo you want to check the TAP service?");
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
		String jobDone="";
		try {
			/*
			 * Make TAP service really exists
			 */
			TapServiceManager tsm = new TapServiceManager();
			if( TapServiceManager.serviceExists() == 0 ) {
				if( AdminComponent.showConfirmDialog(frame, "No TAP service detected. Do you want to create it (needed to register the ObsCore table)?") ) {
					SQLTable.beginTransaction();
					Messenger.printMsg(Messenger.TRACE, "Create TAP service");
					tsm.create(null);
					SQLTable.commitTransaction();
					jobDone += "TAP service created\n";
				} else {
					AdminComponent.showInfo(frame, vor.getName() + "  table registration canceled");
					return;
				}
			}
			ArgsParser ap = new ArgsParser(new String[]{"-populate=" + vor.getName(), Messenger.getDebugParam()});
			SQLTable.beginTransaction();
			tsm.populate(ap);
			SQLTable.commitTransaction();
			jobDone += vor.getName() + " table added to the TAP service\n";				

			/*
			 * Register the capability
			 */
			Capability cpb = new Capability();
			cpb.setDataTreePath(new DataTreePath("ivoa", vor.getName(), null));
			cpb.setProtocol(Capability.TAP);
			cpb.setDescription("Table of data matching  the DM " + vor.getName());
			if( !Table_Saada_VO_Capabilities.hasCapability(cpb) ) {
				SQLTable.beginTransaction();
				Table_Saada_VO_Capabilities.addCapability(cpb);
				SQLTable.commitTransaction();
				jobDone += vor.getName() + " added to the capability table";							
			}
			/*
			 * Add class data to the DM table
			 */
			if( !Table_Saada_VO_DMVIew.isClassReferenced(vor, className) ) {
				SQLTable.beginTransaction();
				DmServiceManager dsm = new DmServiceManager(vor);
				dsm.populate(new ArgsParser(new String[]{"-populate=" + className, Messenger.getDebugParam()}));
				SQLTable.beginTransaction();
				SQLTable.indexTable(vor.getName(), dsm);
				SQLTable.commitTransaction();			
				jobDone +=  className + " added to the view of the DM " + vor.getName();	
			}
			AdminComponent.showSuccess(frame, jobDone);
			parent.updateComponents();
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

