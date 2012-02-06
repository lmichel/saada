package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DataLoaderDefaultRunner;
import saadadb.admin.dialogs.DataLoaderRunner;
import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ThreadLoadData extends CmdThread {
	private ArgsParser ap;
	private ArrayList<String> fileList;
	
	private boolean with_conf=false;

	public ThreadLoadData(Frame frame) {
		super(frame);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		ap = (ArgsParser)(params.get("params"));
		fileList = (ArrayList<String>)(params.get("filelist"));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams() {
		if( ap == null ) {
			AdminComponent.showFatalError(frame, "No loader parameter given");
			return false;
		}
		return true;
	}



	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	public void runCommand() {
		try {
			Loader loader = new Loader(ap.getArgs());
			if( fileList != null && fileList.size() > 0 ) {
				loader.setFile_to_load(fileList);
			}
			loader.load();
			Database.getCachemeta().reload(true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						((AdminTool)(frame)).refreshTree(ap.getCollection(), ap.getCategory().toUpperCase());
					} catch (FatalException e) {
						Messenger.trapFatalException(e);
					}
					SaadaDBAdmin.showSuccess(frame, "Data Loading successfull");		
				}				
			});
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			AdminComponent.showFatalError(frame, "<HTML>Data loading failed (see console)<BR>" + SaadaException.toHTMLString(ae));
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			AdminComponent.showFatalError(frame, "<HTML>Data loading failed (see console)<BR>" + SaadaException.toHTMLString(e));
		}
	}


	@Override
	public String getAntTarget() {
		return "Load data with " + ap.toString();
	}
}
