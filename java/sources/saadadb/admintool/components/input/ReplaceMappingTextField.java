package saadadb.admintool.components.input;

import javax.swing.ButtonGroup;

import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.vocabulary.enums.DataMapLevel;


public class ReplaceMappingTextField extends MappingTextField {

	/**
	 *  * @version $Id$
v
	 */
	private static final long serialVersionUID = 1L;

	public ReplaceMappingTextField(MappingKWPanel form, DataMapLevel dataMapLevel, boolean for_entry, ButtonGroup priority_bg) {
		super(form, dataMapLevel, for_entry, priority_bg);
	}

}
