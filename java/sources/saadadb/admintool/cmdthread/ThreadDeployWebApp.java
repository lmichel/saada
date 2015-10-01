package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.admintool.components.AdminComponent;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.newdatabase.NewWebServer;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ThreadDeployWebApp extends CmdThread {

	public ThreadDeployWebApp(Frame frame, String taskTitle) {
			super(frame, taskTitle);
		}
	
	public void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			NewWebServer.buildDBNameFile(Database.getRoot_dir(), Database.getName(), Database.getUrl_root());
			NewWebServer.deployWebApp();
			frame.setCursor(cursor_org);
	
			AdminComponent.showSuccess(frame, "Web interface successfully deployed");		
		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			AdminComponent.showFatalError(frame, "Web Deployment failed (see console).");
		}
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {
	}

	@Override
	public String getAntTarget() {
		return null;
	}
	public String toString() {
		return "Deploy Web App " ;
	}

}
