package saadadb.admintool.components.input;

import javax.swing.ButtonGroup;

import saadadb.admintool.panels.editors.MappingKWPanel;


public class ReplaceMappingTextField extends MappingTextField {

	/**
	 *  * @version $Id: ReplaceMappingTextField.java 118 2012-01-06 14:33:51Z laurent.mistahl $
v
	 */
	private static final long serialVersionUID = 1L;

	public ReplaceMappingTextField(MappingKWPanel form, int num_node, boolean for_entry, ButtonGroup priority_bg) {
		super(form, num_node, for_entry, priority_bg);
	}

}
