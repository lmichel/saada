package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.collection.Category;
import saadadb.collection.CollectionManager;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.TransactionMaker;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class CmdEmptyCategory extends CmdThread {

	private String collection;
	private int category;

	public CmdEmptyCategory(Frame frame, Object[] tree_path_elements) {
		super(frame);
		collection = tree_path_elements[tree_path_elements.length - 2].toString();
		try {
			category = Category.getCategory(tree_path_elements[tree_path_elements.length - 1].toString());
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
		}
	}

	@Override
	protected boolean getParam() {
		String inst_msg = "";
		String[] classes = Database.getCachemeta().getClassesOfCollection(collection, category);
		try {
			for( int c=0 ; c<classes.length ; c++ ) {
				MetaClass mc = Database.getCachemeta().getClass(classes[c]);
				if( mc.hasInstances() ) {
					inst_msg += "- Class <" + mc.getName() + "> has data\n";
				}
			}
			if( inst_msg.length() > 0 ) {
				return SaadaDBAdmin.showConfirmDialog(frame, "The category <" + Category.explain(category) + "> of the collection <" + collection + "> is not empty:\n"
						+ inst_msg 
						+ "Do you want to remove these data anyway ?");
			}
			else {
				return SaadaDBAdmin.showConfirmDialog(frame, "Are you sure to remove classes of the category <" + Category.explain(category) 
						+"> in the collection <" + collection + " > ?");
				
			}
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
			return false;
		}			
	}
	
	@Override
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			saada_process = new CollectionManager(collection);
			openProgressDialog();
			SQLTable.beginTransaction(true);
			((CollectionManager)saada_process).empty(new ArgsParser(new String[]{"-category=" + Category.explain(category)}));
			SQLTable.commitTransaction();
			Database.getCachemeta().reload(true);
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
					((SaadaDBAdmin)(frame)).refreshTree(collection, Category.explain(category));
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Category <" + Category.explain(category) + "> of the collection <" + collection + "> emptied");		
					} catch (SaadaException e) {
					}
				}				
			});
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "Category Emptying failed (see console).");
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "Category Emptying failed (see console).");
		}
	}

}
