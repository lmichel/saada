package saadadb.admintool.cmdthread;


import java.awt.Cursor;
import java.awt.Frame;
import java.util.Map;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DialogCollNameComment;
import saadadb.admintool.AdminTool;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class CmdCreateCollection extends CmdThread {
	private String name;
	private boolean just_comment = false;
	private String comment;
	/** * @version $Id: CmdCreateCollection.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * @param frame
	 */
	public CmdCreateCollection(Frame frame) {
		super(frame);
		this.name = null;
	}
	
	public CmdCreateCollection(Frame frame, Object[] tree_path_components) {
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
	protected boolean getParam() {
		DialogCollNameComment cd = new DialogCollNameComment(frame, "Create a Collection", name);
		cd.pack();
		cd.setLocationRelativeTo(frame);
        cd.setVisible(true);
        if( cd.getTyped_name() != null ) {
        	name = cd.getTyped_name();
           	comment = cd.getTyped_comment();
        	return true;
        }
        else {
        	return false;
        }
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
					SaadaDBAdmin.showSuccess(frame, "Collection <" + name  + "> created");		
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
						((SaadaDBAdmin)(frame)).refreshTree();
						((SaadaDBAdmin)(frame)).activateOnglets();
						SaadaDBAdmin.showSuccess(frame, "Collection <" + name + "> created");		
					}				
				});
			} catch (AbortException e) {
				frame.setCursor(cursor_org);
				SaadaDBAdmin.showFatalError(frame, e);
			}catch (Exception e) {
				SQLTable.abortTransaction();
				frame.setCursor(cursor_org);
				SaadaDBAdmin.showFatalError(frame, e);
			}
		}
	}

}
