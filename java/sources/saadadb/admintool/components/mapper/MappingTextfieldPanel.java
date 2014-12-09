/**
 * 
 */
package saadadb.admintool.components.mapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.vocabulary.enums.DataMapLevel;

/**
 * @author laurentmichel
 *
 */
public class MappingTextfieldPanel extends MappingPanel{
	public final AppendMappingTextField mappingTextField;
	private boolean forEntry;

	public MappingTextfieldPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		this.forEntry = forEntry;
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		mappingTextField = new AppendMappingTextField(this.mappingPanel, DataMapLevel.KEYWORD, forEntry, null);
		mappingTextField.setColumns(AdminComponent.STRING_FIELD_NAME);
		GridBagConstraints cae = new GridBagConstraints();
		cae.anchor = GridBagConstraints.WEST;
		cae.weightx = 0.0; cae.gridx = 0; cae.gridy = 0;
		panel.add(mappingTextField, cae);
		cae.weightx = 1.0; cae.gridx++;
		panel.add(helpLabel, cae);
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#getText()
	 */
	public  String getText() {
		return mappingTextField.getText();
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#setText(java.lang.String)
	 */
	public  void setText(String text) {
		if( text == null ) {
			mappingTextField.setText("");
		}
		else {
			mappingTextField.setText(text);
		}
	}
	
	/**
	 * @param parser
	 */
	public void setParams(String[] parserArgs) {
		setText(getMergedComponent(parserArgs));	
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#reset()
	 */
	public  void reset() {
		mappingTextField.setText("");
	}
}	

