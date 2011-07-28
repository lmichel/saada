package saadadb.admin.dialogs;


import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import saadadb.admin.SaadaDBAdmin;
import saadadb.api.SaadaDB;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;

public class DialogConfigFileChooser extends JFileChooser {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean candidate_found = false;
	private int category;
	
	/**
	 * @param category
	 */
	public DialogConfigFileChooser(int category) {
		super(SaadaDB.getRoot_dir() 
					+ Database.getSepar() + "config");
		this.category = category;
		this.addChoosableFileFilter(new DataFileFilter(category));
	}
	
	/**
	 * 
	 */
	public DialogConfigFileChooser() {
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
			DataFileFilter dff = new DataFileFilter(category);
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
		
		public DataFileFilter(int category) {
			try {
				this.category = Category.explain(category);
			} catch (SaadaException e) {
				this.category = null;
			}
		}
		
		public DataFileFilter() {
			this.category = null;
		}
		
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			
			String filename = f.getName();
			if( category == null ) {
				if( filename.endsWith(".config") ) {
					candidate_found = true;					
					return true;
				} else {
					return false;
				}
			}
			else {
				if( filename.matches(this.category + ".*\\.config") ) {
					candidate_found = true;					
					return true;	
				} else {
					return false;
				}				
			}
		}
		
		//The description of this filter
		public String getDescription() {
			if( category == null ) {
				return "Dataloader config files in any categories";
			}
			else {
				return "Dataloader config files for category " + category;				
			}
		}
	}
}
