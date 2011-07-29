package saadadb.admin.dialogs;


import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import saadadb.admin.SaadaDBAdmin;
import saadadb.api.SaadaDB;
import saadadb.database.Database;

public class DialogDMFileChooser extends JFileChooser {
	
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean candidate_found = false;
	
	/**
	 * @param category
	 */
	public DialogDMFileChooser() {
		super(SaadaDB.getRoot_dir() 
					+ Database.getSepar() + "config");
		this.addChoosableFileFilter(new DataFileFilter());
	}
	
	/**
	 * @param frame
	 * @return
	 */
	public String open(Frame frame ) {
		this.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File base_dir = new File( SaadaDB.getRoot_dir()  + Database.getSepar() + "config");
		if( base_dir.isDirectory()) {
			DataFileFilter dff = new DataFileFilter();
			for( File c: base_dir.listFiles() ) {
				dff.accept(c);
				if( candidate_found ) {
					break;
				}
			}
		}
		if( !candidate_found ) {
			SaadaDBAdmin.showInputError(frame, "No configuration file found, create first a new configuration.");
			return "";        		
		}
		else {
			this.setLocation(frame.getLocation());
			int retour = this.showOpenDialog(frame);
			if (retour == JFileChooser.APPROVE_OPTION) {
				File selected_file = this.getSelectedFile();
				if( selected_file.exists() ) {
					return selected_file.getAbsolutePath();
				}
				else {
					SaadaDBAdmin.showInputError(frame, "File <" + selected_file + "> does not exists.");
					return "";
				}
			}
		}
		return "";
	}
	
	/**
	 * @author michel
	 *
	 */
	public class DataFileFilter extends FileFilter {
		private String category  = null;

		public boolean accept(File f) {
			if (f.isDirectory()) {
				return false;
			}

			String filename = f.getName();
			if( filename.matches("vodm\\..*\\.xml") ) {
				candidate_found = true;					
				return true;
			} else {
				return false;
			}
		}
		
		//The description of this filter
		public String getDescription() {
			return "Data models description files";				
		}
	}
}
