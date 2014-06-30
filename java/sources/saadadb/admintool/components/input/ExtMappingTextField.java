package saadadb.admintool.components.input;

import javax.swing.ButtonGroup;
import javax.swing.tree.TreePath;

import saadadb.admintool.VPSandbox.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.collection.Category;


public class ExtMappingTextField extends MappingTextField {


	/** * @version $Id$

	 * Modifié par valentin pertuy
	 */
	private static final long serialVersionUID = 1L;

	public ExtMappingTextField(MappingKWPanel form, int num_node, boolean for_entry, ButtonGroup priority_bg) {
		super(form, num_node, for_entry, priority_bg);
	}
	
	/*
	 * Ajouté par valentin Pertuy
	 */
//	public ExtMappingTextField(VPSTOEPanel form, int num_node, boolean for_entry, ButtonGroup priority_bg) {
//		super(form, num_node, for_entry, priority_bg);
//	}

	/**
	 * @param treepath
	 * @param num_node
	 */
	public boolean setText(TreePath treepath) {
		this.treepath = treepath;
		this.previous_value = this.getText().trim();
		this.setExtensionFields();
		if( valid() ) {
			/*
			 * unamed extension in FITS files are shown as "primary"
			 * Which is not a real name but just an alias. In this case, 
			 * we use the extension to identify the good extension
			 */
			if( ext_name.equalsIgnoreCase("primary") ) {
				this.setText(ext_num);
			}
			else {
				this.setText(ext_name);
			}
			this.setPriority();
			return true;
		}	
		else {
			return false;
		}
	}

	protected boolean valid() {
		if( !setExtensionFields () ) {
			return false;
		}
		if( !checkPathCount() ) {
			return false;
		}
		String extension_desc = treepath.getPathComponent(1).toString();
		String[] ext_comp = extension_desc.split(" ");
		if( ext_comp.length != 3 ) {
			AdminComponent.showFatalError(this.getParent(), "Extension description<" + extension_desc  + "> badly formed");
			return false;
		}
//		else if( ext_comp[0].startsWith("#0") ) {
//			SaadaDBAdmin.showFatalError(this.getParent(), "Primary header cannot be selected as extension to be load");
//			return false;						
//		}
		else if( (this.form.getCategory() == Category.TABLE /*|| this.form.spc_btn.isSelected() */) 
				&& !ext_comp[2].endsWith("TABLE)") ){
			AdminComponent.showFatalError(this.getParent(), "You must select a (BIN)TABLE extension");
			return false;	
		}
		else if( this.form.getCategory() == Category.IMAGE && !ext_comp[2].endsWith("IMAGE)") ){
			AdminComponent.showFatalError(this.getParent(), "You must select a IMAGE extension");
			return false;	
		}
		else {	
			return this.checkChange(null);
		}
	}

	/*
	 * can be invoked by DnD: new_val is taken from the dropped values
	 * or by hand editing : new_bval is read in thetext field
	 * Reste the form if the extension number has been changed
	 */
	public boolean checkChange(String new_val) {
		if( new_val != null ) {
			ext_name = new_val;
		}
		if( previous_value.length() > 0 && !previous_value.equals(new_val)) {
			if( AdminComponent.showConfirmDialog(this.getParent(), "Changing the extension will reset the current form!") == true ) {
				this.form.reset(true);
				this.setPrevious_value(new_val);
				return true;
			}
			else {
				this.setText(previous_value);
				return false;
			}
		}
		else {
			return true;
		}
	}

}
