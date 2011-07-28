package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DialogCollNameComment;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class CmdCommentCollection extends CmdThread {
	private String name;
	private boolean just_comment = false;
	private String comment;
	/**
	 * @param frame
	 */
	public CmdCommentCollection(Frame frame) {
		super(frame);
		this.name = null;
	}
	
	public CmdCommentCollection(Frame frame, Object[] tree_path_components) {
		super(frame);
		this.name = tree_path_components[1].toString();
		just_comment = true;
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
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		if( just_comment  ) {
			try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new CollectionManager(name);
			openProgressDialog();
			SQLTable.beginTransaction();
			((CollectionManager)saada_process).comment(new ArgsParser(new String[]{"-comment=" + comment}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			((SaadaDBAdmin)(frame)).refreshTree();
			closeProgressDialog();
			SaadaDBAdmin.showSuccess(frame, "Description of Collection <" + name + "> stored");
			} catch (SaadaException e) {
				closeProgressDialog();
				frame.setCursor(cursor_org);
				Messenger.trapFatalException(e);
			}	
		}
		else {
			try {
				frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				saada_process = new CollectionManager(name);
				openProgressDialog();
				((CollectionManager)saada_process).create(new ArgsParser(new String[]{"-comment=" + comment}));
				frame.setCursor(cursor_org);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						((SaadaDBAdmin)(frame)).refreshTree();
						closeProgressDialog();
						SaadaDBAdmin.showSuccess(frame, "Collection <" + name + "> created");		
					}				
				});
			} catch (AbortException ae) {
				closeProgressDialog();
				frame.setCursor(cursor_org);
				SaadaDBAdmin.showFatalError(frame, "Collection Creation failed (see console).");
			}catch (Exception e) {
				SQLTable.abortTransaction();
				closeProgressDialog();
				frame.setCursor(cursor_org);
				SaadaDBAdmin.showFatalError(frame, "Collection Creation failed (see console).");
			}
		}
	}
}
