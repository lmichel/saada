package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;
import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import saadadb.admin.SaadaDBAdmin;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class CmdDeployWebApp extends CmdThread {

	public CmdDeployWebApp(Frame frame) {
		super(frame);
	}
	
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
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

}
