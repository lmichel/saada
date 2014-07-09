package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldUnits;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.enums.DataMapLevel;

public class VPSpaceMappingPanel extends VPAxisPriorityPanel {

//	private VPKWAppendTextField positionField;
//	private VPKWAppendTextField errorField;
//	private VPKWAppendTextField systemField;
	
	//private JComboBox unitCombo;

	private VPKWNamedFieldUnits positionError;
	private VPKWNamedField positionField;
	private VPKWNamedField systemField;

	
	
	public VPSpaceMappingPanel(VPSTOEPanel mappingPanel,boolean forEntry) {
		super(mappingPanel, "Space Axis",HelpDesk.POSITION_MAPPING);

//		positionField = new VPKWAppendTextField(mappingPanel, DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		positionField.setColumns(AdminComponent.STRING_FIELD_NAME);
//
//		errorField = new VPKWAppendTextField(mappingPanel, DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		//errorField.setColumns(AdminComponent.STRING_FIELD_NAME);
//		
//		
//		//ATTENTION : C'est bien des Keyword qu'on veut ici ?
//		systemField = new VPKWAppendTextField(mappingPanel, DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		systemField.setColumns(AdminComponent.STRING_FIELD_NAME);
//
//		
//	
//		errorField.setColumns(15);
		
		//unitCombo  = new JComboBox(new String[]{"","deg", "arcsec", "arcmin", "mas"});
		positionField = new VPKWNamedField(this,"Position ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		positionError = new VPKWNamedFieldUnits(this,"Position error ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup),new String[]{"","deg", "arcsec", "arcmin", "mas"});
		systemField = new VPKWNamedField(this,"System ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		
		priority.selector.buildMapper(new JComponent[]{systemField.getField(),positionField.getField(),positionError.getField(),positionError.getComboBox()});
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		
		
		systemField.setComponents();
		positionField.setComponents();
		positionError.setComponents();
		
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("System "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(systemField,gbc);
//		gbc.newRow();
//		
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Position "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(positionField,gbc);
//		gbc.newRow();
//		
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel("Position error"), gbc);
//		gbc.next();
//		gbc.left(false);
//		panel.add(errorField,gbc);
//		gbc.next();
//		panel.add(AdminComponent.getPlainLabel("unity "), gbc);
//		gbc.next();
//		gbc.left(true);
//		panel.add(unitCombo,gbc);

		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{
			
			params.add("-posmapping="+priority.getMode());
			if(systemField.getText().length()>0)
				params.add("-system="+systemField.getText());
	
			if(positionField.getText().length()>0)
				params.add("-position="+positionField.getText());
	
			if(positionError.getText().length()>0)
				params.add("-poserror="+positionError.getText());

			if(positionError.getComboBox().getSelectedItem().toString().length()>0)
				params.add("-poserrorunit="+positionError.getComboBox().getSelectedItem().toString());
	
		}
//		if(getPriority().onlyBtn.isSelected() && emptyField)
//			mappingPanel.showInfo(mappingPanel.rootFrame,"You selected the \"Only\" priority for the Observation Axis but didn't fill each corresponding field");
//			

		return params;
	}

	@Override
	public String checkAxisParams() {
		// TODO Auto-generated method stub
		return null;
	}

}
