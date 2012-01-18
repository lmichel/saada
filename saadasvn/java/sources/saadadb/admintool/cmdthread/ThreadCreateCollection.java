package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class ThreadCreateCollection extends CmdThread {
	private String name;
	private boolean just_comment = false;
	private String comment;

	public ThreadCreateCollection(Frame frame) {
		super(frame);
		this.name = null;
	}
	
	public ThreadCreateCollection(Frame frame, Object[] tree_path_components) {
		super(frame);
		this.name = tree_path_components[1].toString();
		just_comment = true;
	}

	@Override
	public void setParams(Map<String, Object> params) throws SaadaException {		
    	name = (String) params.get("name");
    	comment = (String) params.get("comment");		
	}

	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	protected boolean checkParams() {
		if( name == null ) {
			AdminComponent.showFatalError(frame, "No collection name given");
			return false;
		}
		else if( !name.matches(RegExp.COLLNAME)) {
			AdminComponent.showFatalError(frame, "Wrong collection name");
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
		if( just_comment  ) {
			try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new CollectionManager(name);
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).create(new ArgsParser(new String[]{"-comment=" +comment}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);

			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AdminTool)(frame)).refreshTree();
					AdminComponent.showSuccess(frame, "Collection <" + name  + "> created");		
				}				
			});
			} catch (FatalException e) {
				frame.setCursor(cursor_org);
				Messenger.trapFatalException(e);
			}	
		}
		else {
			try {
				frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				saada_process = new CollectionManager(name);
				SQLTable.beginTransaction();
				((CollectionManager)saada_process).create(new ArgsParser(new String[]{"-comment=" +comment}));
				SQLTable.commitTransaction();
				Database.getCachemeta().reload(true);
				frame.setCursor(cursor_org);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						((AdminTool)(frame)).refreshTree();
						AdminComponent.showSuccess(frame, "Collection <" + name + "> created");		
					}				
				});
			} catch (AbortException e) {
				frame.setCursor(cursor_org);
				AdminComponent.showFatalError(frame, e);
			}catch (Exception e) {
				SQLTable.abortTransaction();
				frame.setCursor(cursor_org);
				AdminComponent.showFatalError(frame, e);
			}
		}
	}
	
	@Override
	public String getAntTarget() {
		return "ANT target for " + this.name;
	}

	public String toString() {
		return "Create collection " + this.name;
	}

}
