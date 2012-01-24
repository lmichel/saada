/**
 * 
 */
package saadadb.admintool.utils;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import saadadb.collection.Category;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;

public class ConfFileFilter extends FileFilter {
	private String category  = null;
	private boolean candidate_found = false;
	
	public ConfFileFilter(int category) {
		try {
			this.category = Category.explain(category);
		} catch (SaadaException e) {
			this.category = null;
		}
	}

	public ConfFileFilter(String category) throws FatalException {
		this(Category.getCategory(category));
	}

	public ConfFileFilter() {
		this.category = null;
	}
	
	
	public boolean hasCandidate() {
		return candidate_found;
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
