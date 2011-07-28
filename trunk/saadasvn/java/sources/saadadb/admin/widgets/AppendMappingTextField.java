package saadadb.admin.widgets;

import javax.swing.ButtonGroup;
import javax.swing.tree.TreePath;

import saadadb.admin.mapping.MappingKWPanel;

public class AppendMappingTextField extends MappingTextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AppendMappingTextField(MappingKWPanel form, int num_node, boolean for_entry, ButtonGroup bg) {
		super(form, num_node, for_entry, bg);
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
				this.setText(text + treepath.getPathComponent(num_node).toString());
			} 
			else {
				this.setText(text + "," + treepath.getPathComponent(num_node).toString());				
			}
			this.setPriority();
			return true;
		}	
		else {
			return false;
		}
	}
	
}
