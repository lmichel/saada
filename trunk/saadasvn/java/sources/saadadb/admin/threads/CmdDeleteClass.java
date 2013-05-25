package saadadb.admin.threads;

import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.collection.ClassManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/** * @version $Id$

 * @author laurentmichel
 *
 */
public class CmdDeleteClass extends CmdThread {
	private String name;
	private String collection;
	private String category;

	public CmdDeleteClass(Frame frame, Object[] tree_path_elements) {
		super(frame);
		name = tree_path_elements[tree_path_elements.length - 1].toString();
		collection = tree_path_elements[1].toString();
		category = tree_path_elements[2].toString();
	}

	@Override
	protected boolean getParam() {
		String inst_msg = "";
		boolean data_found = false;
		try {
			MetaClass mc = Database.getCachemeta().getClass(name);
			if( mc.hasInstances() ) {
				data_found = true;
				inst_msg += "- Class <" + mc.getName() + "> has data\n";
			}

		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			return false;
		}			

		if( data_found ) {
			return SaadaDBAdmin.showConfirmDialog(frame, "The class <" + name + "> is not empty:\n"
					+ inst_msg 
					+ "Do you want to remove it (with data) anyway ?");
		}
		else {
			return SaadaDBAdmin.showConfirmDialog(frame, "Are you sure to remove the class <" + name + " > ?");

		}
	}
	
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new ClassManager(name);
			openProgressDialog();
			SQLTable.beginTransaction(true);			
			((ClassManager)saada_process).remove((ArgsParser)null);
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						((SaadaDBAdmin)(frame)).refreshTree(collection, Category.explain(category));
					} catch (SaadaException e) {
					}
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Class <" + name  + "> removal successfull");		
				}				
			});
		} catch (AbortException ae) {
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Class removal failed (see console).");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			closeProgressDialog();
			frame.setCursor(cursor_org);
			SaadaDBAdmin.showFatalError(frame, "Class removal failed (see console).");
		}
	}
}
