package saadadb.admintool.VPSandbox.components.input;

import javax.swing.ButtonGroup;
import javax.swing.tree.TreePath;

import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.enums.DataMapLevel;

public class VPKWAppendTextField extends VPKWMappingTextField{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public VPKWAppendTextField(MappingKWPanel form, DataMapLevel dataMapLevel,
			boolean for_entry, ButtonGroup priority_bg) {
		super(form, dataMapLevel, for_entry, priority_bg);
		// TODO Auto-generated constructor stub
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
