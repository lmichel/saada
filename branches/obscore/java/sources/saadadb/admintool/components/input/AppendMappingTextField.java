package saadadb.admintool.components.input;

import javax.swing.ButtonGroup;
import javax.swing.tree.TreePath;

import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.enums.DataMapLevel;


public class AppendMappingTextField extends MappingTextField {

	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AppendMappingTextField(MappingKWPanel form, DataMapLevel dataMapLevel, boolean for_entry, ButtonGroup bg) {
		super(form, dataMapLevel, for_entry, bg);
	}


	/**
	 * @param treepath
	 * @param num_node
	 */
	public boolean setText(TreePath treepath) {
		this.treepath = treepath;
		this.previous_value = this.getText().trim();
		this.setExtensionFields();
		if( valid() ) {
			String text = this.getText();
			if( text.length() == 0 || text.endsWith(",")) {
				this.setText(text + treepath.getPathComponent(getDataMapLevelNumber(dataMapLevel)).toString());
			} 
			else {
				this.setText(text + "," + treepath.getPathComponent(getDataMapLevelNumber(dataMapLevel)).toString());				
			}
			this.setPriority();
			return true;
		}	
		else {
			return false;
		}
	}
	
}
