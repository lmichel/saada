package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ExtMappingTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.components.mapper.MapperPrioritySelector;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.util.RegExp;

public class VPClassMappingPanel extends VPAxisPanel {
	NodeNameTextField classField;
	protected JRadioButton classifier_btn;
	protected JRadioButton fusion_btn ;
	protected JRadioButton noclass_btn ;
	private ExtMappingTextField mappingTextField;
	
	
	/**
	 * This class is basically the "Axis Class-mapping" Box in the new form
	 * @param mappingPanel
	 */
	
	
	/*
	 * 
	 */
	public VPClassMappingPanel(VPSTOEPanel mappingPanel) {
		super("Axe Class-Mapping");
		JPanel panel =  getContainer().getContentPane();
		panel.setLayout(new GridBagLayout());
		//panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		// this custom GridBagConstraint manage the positions of our elements
		MyGBC gbc = new MyGBC(3,3,3,3);
		
		//Creation of the class-mapping mode choice 

		classifier_btn = new JRadioButton("Automatic Classifier");
		classifier_btn.setBackground(AdminComponent.LIGHTBACKGROUND);
		classifier_btn.setToolTipText("One class (with a name derived from this you give) created for each group of identical product files");
		fusion_btn     = new JRadioButton("Class Fusion");
		fusion_btn.setBackground(AdminComponent.LIGHTBACKGROUND);
		fusion_btn.setToolTipText("One class merging all product files will be created");
		noclass_btn    = new JRadioButton("Default");
		noclass_btn.setBackground(AdminComponent.LIGHTBACKGROUND);
		fusion_btn.setToolTipText("One class (with a default name) created for each group of identical product files");
		
		// Class name field
		classField    = new NodeNameTextField(AdminComponent.STRING_FIELD_NAME, RegExp.CLASSNAME, null);
		
		gbc.right(false);

		
		panel.add(AdminComponent.getPlainLabel("Mapping Mode "), gbc);

		gbc.next();
		gbc.left(false);
		// We set the class-mapping selector
		new VPMapperPrioritySelector(new JRadioButton[] {classifier_btn, fusion_btn, noclass_btn}
		, noclass_btn
		, new ButtonGroup()
		, new JComponent[] {classField}
		, panel
		, gbc);
		
		// We set the "help" button
		gbc.next();
		gbc.right(true);
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		panel.add(helpLabel,gbc);
		
		
		gbc.newRow();
		gbc.right(false);

		panel.add(AdminComponent.getPlainLabel("Class Name "), gbc);
		classField.setColumns(10);
		classField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( noclass_btn.isSelected() ) {
					fusion_btn.setSelected(true) ;
				}
			}	
		});
		gbc.next();
		gbc.left(true);

//		ccs.gridx = 1; ccs.gridy = 1;
//		ccs.anchor = GridBagConstraints.LINE_START;
		panel.add(classField, gbc);
		
		gbc.newRow();
		gbc.right(false);

		// We set the "Extension to Load" selector
		panel.add(AdminComponent.getPlainLabel("Extension to Load "), gbc);		
		
		gbc.next();
		gbc.left(true);
		
		//panel.add(ext,ccs);
		//Partie pour l'extension (Attention, pas repris de ExxAttMapperPanel mais de ExtensionTextFieldPanel)
		// Booleen "ForEntry", mis à faux temporairement
		mappingTextField = new ExtMappingTextField(mappingPanel, 1, false, null);
		mappingTextField.setColumns(AdminComponent.STRING_FIELD_NAME);
		//GridBagConstraints cae = new GridBagConstraints();
		panel.add(mappingTextField, gbc);
		
//		gbc.newRow();
//		gbc.left(true);
//		
//		
//		panel.add(new VPPriorityPanel(mappingPanel,"plop"),gbc);
//		
		
		
		

//		panel.add(helpLabel, ccs);
//		setHelpLabel(HelpDesk.CLASS_MAPPING);
	}
//	@Override
//	public String getText() {
//		return classField.getText();
//	}
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
			return "";
		}
	}
	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public void setText(String text) {
//		classField.setText((text == null)? "": text);
//	}
//
//	/**
//	 * @param classifierMode
//	 * @param text
//	 */
//	public void setText(ClassifierMode classifierMode, String text) {
//		classifier_btn.setSelected(false);
//		fusion_btn.setSelected(false);
//		noclass_btn.setSelected(false);
//		switch(classifierMode){
//		case CLASS_FUSION: fusion_btn.setSelected(true);
//		this.setText(text);
//		break;
//		case CLASSIFIER: classifier_btn.setSelected(true);
//		this.setText(text);
//		break;
//		default: noclass_btn.setSelected(true);
//		}
//	}
//	@Override
//			public void reset() {
//		classField.setText("");
//	}
//
//	public boolean hasMapping() {
//		return (classifier_btn.isSelected() || fusion_btn.isSelected()) ;
//	}

}

