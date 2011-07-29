package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.collection.CollectionManager;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

public class CmdDeleteCollection extends CmdThread {
	private String name;

	/** * @version $Id$

	 * @param frame
	 * @param tree_path_elements
	 */
	public CmdDeleteCollection(Frame frame, Object[] tree_path_elements) {
		super(frame);
		name = tree_path_elements[tree_path_elements.length - 1].toString();
	}

	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	@Override
	protected boolean getParam() {
		String class_msg = "";
		String inst_msg = "";
		boolean class_found = false;
		int not_empty=0;
		for( int i=1 ; i<Category.NB_CAT ; i++ ) {
			String[] classes = Database.getCachemeta().getClassesOfCollection(name, i);
			try {
				class_msg += "- Category " + Category.explain(i) + " has classes\n";
				for( int c=0 ; c<classes.length ; c++ ) {
					not_empty++;
					class_found = true;
					MetaClass mc = Database.getCachemeta().getClass(classes[c]);
					if( mc.hasInstances() ) {
						inst_msg += "- Class <" + mc.getName() + "> has data\n";
					}
					/*
					 * Avoid a too large popup window.
					 */
					if( not_empty > 10 ) {
						inst_msg += "......\n";
						break;
					}
				}
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
				return false;
			}			
			if( not_empty > 10 ) {
				break;
			}
		}
		if( class_found ) {
			return SaadaDBAdmin.showConfirmDialog(frame, "The collection <" + name + "> is not empty:\n"
					+  class_msg 
					+ inst_msg 
					+ "Do you want to remove it (with data and classes) anyway ?");
		}
		else {
			return SaadaDBAdmin.showConfirmDialog(frame, "Are you sure to remove the collection <" + name + " > ?");
							
		}
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#runCommand()
	 */
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new CollectionManager(name);
			openProgressDialog();
			SQLTable.beginTransaction(true);
			((CollectionManager)saada_process).remove(null);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			((SaadaDBAdmin)(frame)).refreshTree();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((SaadaDBAdmin)(frame)).refreshTree();
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Collection <" + name + "> removed");		
				}				
			});
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Failed to remove collection <" + name + "> (see console).");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Failed to remove collection <" + name + "> (see console).");
		}
	}
}
