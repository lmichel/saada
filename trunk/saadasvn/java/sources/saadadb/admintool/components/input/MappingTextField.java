package saadadb.admintool.components.input;


import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.tree.TreePath;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.dnd.TreepathDropableTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;

/**
 * @author michel
 * * @version $Id: MappingTextField.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class MappingTextField extends TreepathDropableTextField {
	private static final long serialVersionUID = 1L;
	protected MappingKWPanel form;
	protected int num_node;
	protected boolean for_entry;
	protected String previous_value="";
	protected String ext_num;
	protected String ext_name;
	protected String ext_type;
	protected ButtonGroup  priority_bg;
	
	/**
	 * @param form
	 */
	MappingTextField(MappingKWPanel form, int num_node, boolean for_entry, ButtonGroup priority_bg) {
		this.form = form;
		this.for_entry = for_entry;
		this.num_node = num_node;
		this.priority_bg = priority_bg;
		/*
		 * Takes the second node, without extension checking
		 */
		this.setTransferHandler(new ProductTreePathTransferHandler(num_node));			
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
			this.setText(treepath.getPathComponent(num_node).toString());
			return true;
		}	
		else {
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.text.JTextComponent#setText(java.lang.String)
	 */
	public void setText(String text) {
		this.previous_value = this.getText();
		if( text != null ) {
			super.setText(text.trim());
		}
		this.setPriority();
	}

	/**
	 * @return
	 */
	protected boolean setExtensionFields() {
		String extension_desc = treepath.getPathComponent(1).toString();
		String[] ext_comp = extension_desc.split(" ");
		switch( ext_comp.length ) {
		case 3: ext_num = ext_comp[0];ext_name = ext_comp[1]; ext_type = ext_comp[2];
		return true;
		case 4: ext_num = ext_comp[0];ext_name = ext_comp[1]; ext_type = ext_comp[2] + " " + ext_comp[3];
		return true;
		default: AdminComponent.showFatalError(this.getParent(), "Extension description<" + extension_desc  + "> badly formed");
		return false;
		}
	}

	/**
	 * @return
	 */
	protected boolean valid() {
		String current_extension = "";
		current_extension = this.form.getExtension();
		
		if( !setExtensionFields () ) {
			return false;
		}
		if( !checkPathCount() ) {
			return false;
		}
		if( for_entry ) {
			if( ext_type.endsWith("COLUMNS)") &&
				(ext_num.equals(current_extension) || ext_name.equals(current_extension) || ext_num.equals("#0")) ) {
				return true;
			}
			else {
				AdminComponent.showFatalError(this.getParent(), "KW must be taken from the COLUMNS  of the extension <" + current_extension + ">");
				return false;	
			}
		}
		else {
			if( ext_type.endsWith("COLUMNS)") ) {
				AdminComponent.showFatalError(this.getParent(), "Cannot use column names except for an entry table configuration");
				return false;			
			}
			else if( ext_num.equals(current_extension) || ext_name.equals(current_extension) || ext_num.equals("#0")) {
				return true;
			}
			else {
				AdminComponent.showFatalError(this.getParent(), "Only attributes from extension <" + current_extension + "> or from primary header can be choosen");
				return false;
			}
		}
	}
	
	/*
	 * Check if the tree path contains enough nodes to get the requested value 
	 */
	protected boolean checkPathCount() {
		if( treepath.getPathCount() != (num_node+1) ) {
			switch( num_node) {
			case 1: AdminComponent.showFatalError(this.getParent(), "You must select an extension"); break;
			case 2: AdminComponent.showFatalError(this.getParent(), "You must select an attribute"); break;
			default: AdminComponent.showFatalError(this.getParent(), "You must select an higher level node"); break;
			}
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * 
	 */
	public void setForEntry() {
		for_entry = true;
	}

	public void setPrevious_value(String previous_value) {
		this.previous_value = previous_value;
	}
	
	/**
	 * Button Group BG is supposed to be a priority setter with 4 buttons (only, first, last and no priority)
	 * if a text is set, the priority musn't be on "no mapping". Thus it is set to "first".
	 */
	protected void setPriority() {
		if( priority_bg != null && this.getText().trim().length() > 0 ) {
			Enumeration<AbstractButton> en = priority_bg.getElements();
			int index = 0;
			AbstractButton to_select = null;
			while( en.hasMoreElements() ) {
				AbstractButton ab = en.nextElement();
				if( index == 1 ) {
					to_select = ab;
				}
				else if( index == 3 && ab.isSelected()) {
					to_select.setSelected(true);
				}
				index ++;
				
			}
			
		}
	}

}
