package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ExtMappingTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.enums.ClassifierMode;
import saadadb.enums.DataMapLevel;
import saadadb.exceptions.QueryException;
import saadadb.util.RegExp;


/**
 * This class is basically the "Axis Class-mapping" Box in the new form
 */
public class VPClassMappingPanel extends VPAxisPanel {
	NodeNameTextField classField;
	protected JRadioButton classifier_btn;
	protected JRadioButton fusion_btn ;
	protected JRadioButton noclass_btn ;
	private ExtMappingTextField mappingTextField;

	public VPClassMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel,"Class-Mapping Axis");
		JPanel panel =  getContainer().getContentPane();
		panel.setLayout(new GridBagLayout());
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
		classField = new NodeNameTextField(AdminComponent.STRING_FIELD_NAME, RegExp.CLASSNAME, null);

		gbc.right(false);

		panel.add(AdminComponent.getPlainLabel("Mapping Mode "), gbc);

		gbc.next();
		gbc.left(false);

		// We set the class-mapping selector
		VPMapperPrioritySelector selector = new VPMapperPrioritySelector(new JRadioButton[] {classifier_btn, fusion_btn, noclass_btn}
		, noclass_btn
		, new ButtonGroup()
		, panel
		, gbc);
		axisPriorityComponents =new ArrayList<JComponent>();
		axisPriorityComponents.add(classField);
		selector.buildMapper(axisPriorityComponents);

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

		panel.add(classField, gbc);

		gbc.newRow();
		gbc.right(false);

		// We set the "Extension to Load" selector
		panel.add(AdminComponent.getPlainLabel("Extension to Load "), gbc);		

		gbc.next();
		gbc.left(true);

		mappingTextField = new ExtMappingTextField(mappingPanel, DataMapLevel.EXTENSION, false, null);
		mappingTextField.setColumns(AdminComponent.STRING_FIELD_NAME);

		panel.add(mappingTextField, gbc);

	}

	public void setText(String text) {
		classField.setText((text == null)? "": text);
	}

	/**
	 * @param classifierMode
	 * @param text
	 */
	public void setText(ClassifierMode classifierMode, String text) {
		classifier_btn.setSelected(false);
		fusion_btn.setSelected(false);
		noclass_btn.setSelected(false);
		switch(classifierMode){
		case CLASS_FUSION: fusion_btn.setSelected(true);
			this.setText(text);
			break;
		case CLASSIFIER: classifier_btn.setSelected(true);
			this.setText(text);
			break;
		default: noclass_btn.setSelected(true);
		}
	}


	public boolean hasMapping() {
		return (classifier_btn.isSelected() || fusion_btn.isSelected()) ;
	}

	/**
	 * @throws QueryException 
	 * 
	 */
	@Override
	public ArrayList<String> getAxisParams(){

		ArrayList<String> params = new ArrayList<String>();

		/*
		 * the Class Mapping params
		 */
		if(hasMapping())
		{
			if( classifier_btn.isSelected()) {
				if ( classField.getText().length()>0)
					params.add("-classifier=" + classField.getText());		
			}
			else if( fusion_btn.isSelected()  ) {
				if ( classField.getText().length()>0)
					params.add("-classfusion=" + classField.getText());	
			}
			else {
				params.add("");
			}

		}
		/*
		 * The mappingTextField Param
		 */
		if(mappingTextField.getText().length()>0)
			params.add("-extension="+mappingTextField.getText());
		
		return params;
	}

	public String checkAxisParams()
	{
		if(hasMapping())
		{
			if(classifier_btn.isSelected() || fusion_btn.isSelected())
			{
				if(classField.getText().length()==0)
					return "<LI>Empty class name not allowed in this classification mode</LI>";

				if(!classField.getText().matches(RegExp.CLASSNAME))
					return "<LI>Bad class name</LI>";
			}
		}
		return "";
	}

	public void reset(boolean keep_ext)
	{
		noclass_btn.setSelected(true);
		classField.setText("");
		classField.setEnabled(false);
		if(keep_ext==false)
		{
			mappingTextField.setText("");
		}
	}



}

