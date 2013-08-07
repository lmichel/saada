package saadadb.admintool.cmdthread;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.newdatabase.NewWebServer;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id: CmdDeployWebApp.java 118 2012-01-06 14:33:51Z laurent.mistahl $

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
			Project p = new Project();
			p.init();
			ProjectHelper helper = ProjectHelper.getProjectHelper();
			helper.parse(p, new File(Database.getRoot_dir() 
					+ Database.getSepar() + "bin" 
					+ Database.getSepar() + "build.xml" ));
			p.executeTarget("tomcat.deploy");
			frame.setCursor(cursor_org);
	
			SaadaDBAdmin.showSuccess(frame, "Web interface successfully deployed");		
		} catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Web Deployment failed (see console).");
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
