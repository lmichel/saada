package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.enums.DataMapLevel;

public class VPObservationEntryMappingPanel extends VPObservationMappingPanel{

	private VPKWNamedField target_name_entry;
	private VPKWNamedField instrument_name_entry;
	
	public VPObservationEntryMappingPanel(VPSTOEPanel mappingPanel) {
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
		axisPanel.add(subPanelTitle,gbc);
		gbc.newRow();

		//We create the new Fields
		instrument_name_entry = new VPKWNamedField(this,"Entry_Instrument ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, true,priority.buttonGroup));
		target_name_entry = new VPKWNamedField(this,"Entry_Target",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, true,priority.buttonGroup));

		//We had them to the priority
		axisPriorityComponents.add(instrument_name_entry.getField());
		axisPriorityComponents.add(target_name_entry.getField());

		priority.selector.buildMapper(axisPriorityComponents);		

		instrument_name_entry.setComponents();
		target_name_entry.setComponents();
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> getAxisParams() {
		// TODO Auto-generated method stub
		ArrayList<String> params = super.getAxisParams();

		if (!getPriority().noBtn.isSelected())
		{
			if(target_name_entry.getText().length()>0)
				params.add("-entry.target="+target_name_entry.getText());

			if(instrument_name_entry.getText().length()>0)
				params.add("-entry.instrument="+instrument_name_entry.getText());
		}

		return params;
	}
	
	
	
	
	public String checkAxisParams() {
		String error =super.checkAxisParams();
		if(priority.isOnly())
		{

			if(target_name_entry.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no target entry specified</LI>";
	
			if(instrument_name_entry.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no instrument entry specified</LI>";

		}

		return error;
	}

	
	public void reset() {
		super.reset();
		target_name_entry.reset();
		instrument_name_entry.reset();
	}
}
