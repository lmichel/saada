package saadadb.admintool.dialogs;


import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import saadadb.admintool.components.AdminComponent;
import saadadb.util.RegExp;

public class DialogFileChooser extends JFileChooser {
	
	/** * @version $Id: DialogFileChooser.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String current_dir;
	
	/**
	 * 
	 */
	public DialogFileChooser() {
		super();
		this.addChoosableFileFilter(new DataFileFilter());
	}
	
	/**
	 * 
	 */
	public DialogFileChooser(String filter, String description) {
		super();
		this.addChoosableFileFilter(new DataFileFilter());
		this.addChoosableFileFilter(new DataFileFilter(filter,description));
	}

	/**
	 * @param frame
	 * @param file_only
	 * @return
	 */
	public String open(Frame frame, boolean file_only ) {
		if( file_only ) {
			this.setFileSelectionMode(JFileChooser.FILES_ONLY);			
		}
		else {
			this.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		int retour = this.showOpenDialog(frame);
		if (retour == JFileChooser.APPROVE_OPTION) {
			File selected_file = this.getSelectedFile();
			if( selected_file.exists() ) {
				if( selected_file.isDirectory() ) {
					current_dir = selected_file.getAbsolutePath();
				}
				else {
					current_dir = selected_file.getParent();       			
				}
				//AdminComponent.current_dir = current_dir;
				return selected_file.getAbsolutePath();
			}
			else {
				AdminComponent.showInputError(frame, "File <" + selected_file + "> does not exists.");
				return "";
			}
		}
		return "";
	}
	/**
	 * @author michel
	 *
	 */
	public class DataFileFilter extends FileFilter {
		private String description=null;
		private String filter=null;;
		
		DataFileFilter(String filter, String description) {
			this.filter = filter;
			this.description = description;
		}
		
		DataFileFilter() {
			this.filter = null;
			this.description = null;
		}

		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			
			String filename = f.getName();
			try {
				if( filter == null &&
						(filename.matches(RegExp.FITS_FILE) || filename.matches(RegExp.VOTABLE_FILE)) ) {
					return true;
				} 
				else if( filter != null && filename.matches(filter) ) {
					return true;
				}
				else {
					return false;
				}
			}catch(Exception e) {
				filter = null;
				AdminComponent.showFatalError(null, "Wrong filename filter: " + e.getMessage() + " CLose the window and fix your filter (regexp)");
				return false;
			}
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.filechooser.FileFilter#getDescription()
		 */
		public String getDescription() {
			if( filter == null ) {
				return "FITS files and VOTables";
			}
			else {
				return description;
			}
		}
	}
	/**
	 * @return Returns the current_dir.
	 */
	public static String getCurrent_dir() {
		return current_dir;
	}
}
