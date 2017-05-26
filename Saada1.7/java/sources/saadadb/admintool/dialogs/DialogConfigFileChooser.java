package saadadb.admintool.dialogs;


import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.ConfFileFilter;
import saadadb.api.SaadaDB;
import saadadb.database.Database;

public class DialogConfigFileChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;
	private int category;
	
	/**
	 * @param category
	 */
	public DialogConfigFileChooser(int category) {
		super(SaadaDB.getRoot_dir() 
					+ Database.getSepar() + "config");
		this.category = category;
		this.addChoosableFileFilter(new ConfFileFilter(category));
	}
	
	/**
	 * 
	 */
	public DialogConfigFileChooser() {
		super(SaadaDB.getRoot_dir() 
					+ Database.getSepar() + "config");
		this.addChoosableFileFilter(new ConfFileFilter());
	}
	
	/**
	 * @param frame
	 * @return
	 */
	public String open(Frame frame ) {
		this.setFileSelectionMode(JFileChooser.FILES_ONLY);
		File base_dir = new File( SaadaDB.getRoot_dir()  + Database.getSepar() + "config");
		ConfFileFilter dff = new ConfFileFilter(category);
		if( base_dir.isDirectory()) {
			for( File c: base_dir.listFiles() ) {
				dff.accept(c);
				if( dff.hasCandidate() ) {
					break;
				}
			}
		}
		if( !dff.hasCandidate() ) {
			AdminComponent.showInputError(frame, "No configuration file found, create first a new configuration.");
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
					AdminComponent.showInputError(frame, "File <" + selected_file + "> does not exists.");
					return "";
				}
			}
		}
		return "";
	}

}
