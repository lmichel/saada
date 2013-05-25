package saadadb.admin.threads;


import java.awt.Cursor;
import java.awt.Frame;

import javax.swing.SwingUtilities;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DataLoaderDefaultRunner;
import saadadb.admin.dialogs.DataLoaderRunner;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.Loader;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class CmdLoadData extends CmdThread {

	private String collection;
	private int category;
	private String classe=null;
	private String datafile=null;
	private ArgsParser ap;
	private boolean with_conf=false;

	public CmdLoadData(Frame frame, Object[] tree_path_elements, ArgsParser argparser) {
		super(frame);
		collection = tree_path_elements[1].toString();
		try {
			category = Category.getCategory(tree_path_elements[2].toString());
		} catch (SaadaException e) {
			SaadaDBAdmin.showFatalError(frame, e);
		}
		with_conf = true;
	}

	public CmdLoadData(Frame frame, Object[] tree_path_elements) {
		super(frame);
		collection = tree_path_elements[1].toString();
		try {
			category = Category.getCategory(tree_path_elements[2].toString());
		} catch (SaadaException e) {
			SaadaDBAdmin.showFatalError(frame, e);
		}
		if( tree_path_elements.length == 4 ) {
			classe = tree_path_elements[3].toString();
		}
		ap = null;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admin.threads.CmdThread#getParam()
	 */
	protected boolean getParam() {
		/*
		 * Load in default mode (no config)
		 */
		if( !with_conf ) {
			try {
				DataLoaderDefaultRunner fcd = new DataLoaderDefaultRunner((SaadaDBAdmin) frame, "Load " + Category.explain(category) + " Data Without Configuration ", null, collection, category);
				datafile = fcd.getTyped_dir();
				if( datafile == null ) {
					return false;
				}
				else {
					try {
						String dm = "-debug=off";
						if(Messenger.debug_mode == true) dm = "-debug=on";
						if( classe == null ){
							saada_process = (new Loader(new String[]{"-collection=" + collection,
									"-category=" + Category.explain(category),
									"-filename=" + datafile,
									dm,
									Database.getDbname()}));
						}
						/*
						 * If the command is run from a class node on the meta data tree
						 * the loader is set in classfusion mode
						 */
						else {
							saada_process = (new Loader(new String[]{"-collection=" + collection,
									"-category=" + Category.explain(category),
									"-classfusion=" + classe,
									"-filename=" + datafile,
									dm,
									Database.getDbname()}));
						}
					} catch (Exception e) {
						SaadaDBAdmin.showFatalError(frame, e);
						return false;
					}
					((Loader)saada_process).setFile_to_load(fcd.getSelected_files());					
					return true;
				}
			} catch (SaadaException e) {
			}
		}
		/*
		 * Load in config mode
		 */
		else  {
			try {
				DataLoaderRunner fcd = new DataLoaderRunner((SaadaDBAdmin) frame, "Load " + Category.explain(category) + " Data", null, collection, category);
				datafile = fcd.getTyped_dir();
				
				if( datafile == null ) {
					return false;
				}
				else {
					saada_process = (new Loader(fcd.getLoaderArgs()));;
					((Loader)saada_process).setFile_to_load(fcd.getSelected_files());	
					if( fcd.getTyped_dir() == null ) {
						return false;
					}
					else {
						return true;
					}
				}
			} catch (Exception e) {
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see gui.CmdThread#runCommand()
	 */
	protected void runCommand() {
		Cursor cursor_org = frame.getCursor();
		try {
			frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if( ap != null ) {
					openProgressDialog();
					((Loader)saada_process).load();
			}
			else if( classe == null ){
				openProgressDialog();
				((Loader)saada_process).load();
			}
			/*
			 * If the command is run from a class node on the meta data tree
			 * the loader is set in classfusion mode
			 */
			else {
				String dm = "-debug=off";
				if(Messenger.debug_mode == true) dm = "-debug=on";
				saada_process = (new Loader(new String[]{"-collection=" + collection,
						"-category=" + category,
						"-classfusion=" + classe,
						"-filename=" + datafile,
						dm,
						Database.getDbname()}));
				openProgressDialog();
				((Loader)saada_process).load();
			}
			frame.setCursor(cursor_org);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						((SaadaDBAdmin)(frame)).refreshTree(collection, Category.explain(category));
					} catch (SaadaException e) {
						// Should be detectec in creator
					}
					closeProgressDialog();
					SaadaDBAdmin.showSuccess(frame, "Data Loading successfull");		
				}				
			});
		} catch (AbortException ae) {
			Messenger.printStackTrace(ae);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "<HTML>Data loading failed (see console)<BR>" + SaadaException.toHTMLString(ae));
		}catch (Exception e) {
			SQLTable.abortTransaction();
			Messenger.printStackTrace(e);
			frame.setCursor(cursor_org);
			closeProgressDialog();
			SaadaDBAdmin.showFatalError(frame, "<HTML>Data loading failed (see console)<BR>" + SaadaException.toHTMLString(e));
		}
	}
}
