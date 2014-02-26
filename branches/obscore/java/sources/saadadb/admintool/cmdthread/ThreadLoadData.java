package saadadb.admintool.cmdthread;


import java.awt.Frame;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.AntDesk;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.old.Loader;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class ThreadLoadData extends CmdThread {
	private ArgsParser ap;
	private ArrayList<String> fileList;
	
	private boolean with_conf=false;

	public ThreadLoadData(Frame frame, String taskTitle) {
		super(frame, taskTitle);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
		ap = (ArgsParser)(params.get("params"));
		fileList = (ArrayList<String>)(params.get("filelist"));
		resourceLabel = "Collection " + ap.getCollection() + "." + ap.getCategory();

	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	public boolean checkParams(boolean withConfirm) {
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
			Database.getCachemeta().reloadGraphical(frame, true);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						((AdminTool)(frame)).refreshTree(ap.getCollection(), ap.getCategory().toUpperCase());
						
					} catch (FatalException e) {
						Messenger.trapFatalException(e);
					}
					int userChoice = AdminComponent.showSuccessQuestion(frame, "Data Loading successfull", "Do you want to index the table of loaded data ?");		
					if (userChoice == JOptionPane.YES_OPTION)
					{
						((AdminTool)frame).activePanel(AdminComponent.SQL_INDEX);
					}	
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
		return AntDesk.getAntFile(AdminComponent.LOAD_DATA, taskTitle, ap.toXML());
	}
}
