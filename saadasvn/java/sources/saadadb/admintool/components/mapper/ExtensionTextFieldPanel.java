package saadadb.admintool.components.mapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ExtMappingTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;

public class ExtensionTextFieldPanel extends MappingPanel {

	public final  ExtMappingTextField mappingTextField;
	
	public ExtensionTextFieldPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		mappingTextField = new ExtMappingTextField(this.mappingPanel, 1, forEntry, null);
		mappingTextField.setColumns(AdminComponent.STRING_FIELD_NAME);
		GridBagConstraints cae = new GridBagConstraints();
		cae.anchor = GridBagConstraints.WEST;
		cae.weightx = 0.0; cae.gridx = 0; cae.gridy = 0;
		panel.add(mappingTextField, cae);
		cae.weightx = 1.0; cae.gridx++;
		panel.add(helpLabel, cae);
		setHelpLabel(new String[]{"Drop an extension from the Data Sample window" 
							, "or put a number prefixed with a #"
							, "Keywords of the first extension are loaded by default"});
	}

	public boolean checkChange() {
		return mappingTextField.checkChange(mappingTextField.getText());
	}
	
	public void setPreviousValue() {
		mappingTextField.setPrevious_value(mappingTextField.getText());
	}
	@Override
	public String getText() {
		return mappingTextField.getText();
	}

	@Override
	public void setText(String text) {
		mappingTextField.setText((text == null)? "": text);		
	}

	@Override
	public void reset() {
		mappingTextField.setText("");		
	}
	
}
