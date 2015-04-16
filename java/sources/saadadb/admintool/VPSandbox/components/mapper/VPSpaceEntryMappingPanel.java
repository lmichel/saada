package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnBox;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.vocabulary.enums.DataMapLevel;

/**
 * This class inherit of SpaceMappingPanel and represent the subpanel in the case where the category=TABLE
 * @author pertuy
 * @version $Id$
 */
public class VPSpaceEntryMappingPanel extends VPSpaceMappingPanel{

	private VPKWNamedFieldnBox positionError_entry;
	private VPKWNamedField positionField_entry;
	
	/*
	 * see ObservationEntryMappingPanel for functionals precisions
	 */
	public VPSpaceEntryMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel);
		
		//The separator
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		JSeparator separator = new JSeparator();
		separator.setBackground(new Color(VPAxisPanel.SEPARATORCOLOR));
		separator.setForeground(new Color(VPAxisPanel.SEPARATORCOLOR));
		axisPanel.add(separator,
				gbc);

		gbc.newRow();
		gbc.fill=GridBagConstraints.NONE;
		JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELENTRY);
		gbc.right(false);
		subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
		subPanelTitle.setFont(VPAxisPanel.SUBPANELTITLEFONT);

		axisPanel.add(subPanelTitle,gbc);
		gbc.newRow();
		
		positionField_entry = new VPKWNamedField(this,"Position ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		positionError_entry = new VPKWNamedFieldnBox(this,"Position error ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup),new String[]{"","deg", "arcsec", "arcmin", "mas"});

		axisPriorityComponents.add(positionField_entry.getField());
		axisPriorityComponents.add(positionError_entry.getField());
		axisPriorityComponents.add(positionError_entry.getComboBox());
		priority.selector.buildMapper(axisPriorityComponents);		

		positionField_entry.setComponents();
		positionError_entry.setComponents();

	}
	
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = super.getAxisParams();

		if (!getPriority().noBtn.isSelected())
		{
			if(positionField_entry.getText().length()>0)
				params.add("-entry.position="+positionField_entry.getText());

			if(positionError_entry.getText().length()>0)
				params.add("-entry.poserror="+positionError_entry.getText());

			if(positionError_entry.getComboBox().getSelectedItem()!=null)
			{
				if(positionError_entry.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-entry.poserrorunit="+positionError_entry.getComboBox().getSelectedItem().toString());
			}
		}

		return params;
	}
	
	public String checkAxisParams() {
		String error =super.checkAxisParams();
		if(priority.isOnly())
		{
			if(positionField_entry.getText().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position specified</LI>";
			if(positionError_entry.getText().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position error specified</LI>";			
			if(positionError_entry.getComboBox().getSelectedItem().toString().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position error unit specified</LI>";		

		}

		return error;
	}

	public void reset() {
		super.reset();
		positionField_entry.reset();
		positionError_entry.reset();
	}
	
	public void setParams(ArgsParser ap) throws FatalException {
		super.setParams(ap);
		StringBuilder builder = new StringBuilder();
		for(String s : ap.getPositionMapping(true)) {
		    builder.append(s);
		}
		if(!priority.noBtn.isSelected())
		{
			positionError_entry.setEnable(true);
			positionField_entry.setEnable(true);
		}
		positionField_entry.setText(builder.toString());
		//positionError_entry.setText(ap.getPoserrorMapping(true), ap.getPoserrorUnit(true));
		
	}
	@Override
	public boolean fieldsEmpty(ArgsParser ap) {
		boolean empty=true;
		for(String s : ap.getPositionMapping(true)) {
		    empty=false;
		}
		return super.fieldsEmpty(ap) && empty  &&	ap.getPoserrorMapping(true)==null ;
//				 &&ap.getPoserrorUnit(true)==null;

	}
	
}
