package saadadb.admintool.components.mapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.util.DefineType;
import saadadb.util.RegExp;

public class ClassMapperPanel extends MappingPanel {
	NodeNameTextField classField;
	protected JRadioButton classifier_btn;
	protected JRadioButton fusion_btn ;
	protected JRadioButton noclass_btn ;

	
	public ClassMapperPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		GridBagConstraints ccs = new GridBagConstraints();
		
		classifier_btn = new JRadioButton("Automatic Classifier");
		classifier_btn.setToolTipText("One class (with a name derived from this you give) created for each group of identical product files");
		fusion_btn     = new JRadioButton("Class Fusion");
		fusion_btn.setToolTipText("One class merging all product files will be created");
		noclass_btn    = new JRadioButton("Default");
		fusion_btn.setToolTipText("One class (with a default name) created for each group of identical product files");
		try {
			classField    = new NodeNameTextField(AdminComponent.STRING_FIELD_NAME, RegExp.CLASSNAME, null);
		} catch (ParseException e1) {
			AdminComponent.showFatalError(mappingPanel.rootFrame, e1);
		}

		ccs.gridx = 0; ccs.gridy = 0;
		ccs.weightx = 0.0;                       //reset to default
		ccs.anchor = GridBagConstraints.LINE_END;
		panel.add(AdminComponent.getPlainLabel("Mapping Mode "), ccs);

		ccs.anchor = GridBagConstraints.LINE_START;
		ccs.gridx = 1; ccs.gridy = 0; ccs.gridwidth = 2;
		new MapperPrioritySelector(new JRadioButton[] {classifier_btn, fusion_btn, noclass_btn}
			, noclass_btn
			, new ButtonGroup()
			, new JComponent[] {classField}
		    , panel
			, ccs);

		ccs.gridx = 0; ccs.gridy = 1;ccs.anchor = GridBagConstraints.LINE_END;ccs.gridwidth = 1;
		panel.add(AdminComponent.getPlainLabel("Class Name"), ccs);
		classField.setColumns(10);
		classField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( noclass_btn.isSelected() ) {
					fusion_btn.setSelected(true) ;
				}
			}	
		});
		ccs.gridx = 1; ccs.gridy = 1;
		ccs.anchor = GridBagConstraints.LINE_START;
		panel.add(classField, ccs);

		ccs.gridx = 2; ccs.gridy = 1; 
		ccs.weightx = 1.0;  
		setHelpLabel(new String[] {"The class name must only contain letters, numbers or undescores."
						, "It can not starts with a number"});


	}
	@Override
	public String getText() {
		return classField.getText();
	}
	/**
	 * @return
	 */
	public String getParam() {
		if( classifier_btn.isSelected()  ) {
			return "-classifier=" + classField.getText();							
		}
		else if( fusion_btn.isSelected()  ) {
			return "-classfusion=" + classField.getText();											
		}
		else {
			return null;
		}
	}

	@Override
	public void setText(String text) {
		classField.setText((text == null)? "": text);
	}

	/**
	 * @param mappingType
	 * @param text
	 */
	public void setText(int mappingType, String text) {
		classifier_btn.setSelected(false);
		fusion_btn.setSelected(false);
		noclass_btn.setSelected(false);
		if( mappingType == DefineType.TYPE_MAPPING_USER  ) {
			fusion_btn.setSelected(true);
			this.setText(text);
		}
		else if( mappingType == DefineType.TYPE_MAPPING_CLASSIFIER  ) {
			classifier_btn.setSelected(true);
			this.setText(text);
		}
		else {
			noclass_btn.setSelected(true);
		}
	}
	@Override
	public void reset() {
		classField.setText("");
	}
	
	public boolean hasMapping() {
		return (classifier_btn.isSelected() || fusion_btn.isSelected()) ;
	}

}
