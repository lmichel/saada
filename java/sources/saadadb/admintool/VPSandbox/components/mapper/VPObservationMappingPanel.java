package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.enums.DataMapLevel;

/**
 * Represent the Observation axis/subpanel in the filter form
 * @author pertuy
 * @version $Id$
 */
public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	
	private VPKWNamedField obs_collection;
	private VPKWNamedField target_name;
	private VPKWNamedField facility_name;
	private VPKWNamedField instrument_name;

	public VPObservationMappingPanel(VPSTOEPanel mappingPanel){
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);

		//We set the field we always display
		obs_collection = new VPKWNamedField(this,"Collection ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		target_name = new VPKWNamedField(this,"Target name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		facility_name = new VPKWNamedField(this,"Facility name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		instrument_name = new VPKWNamedField(this,"Instrument name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		
		//We link the priority mapper and the fields
		axisPriorityComponents = new ArrayList<JComponent>();
		axisPriorityComponents.add(obs_collection.getField());
		axisPriorityComponents.add(target_name.getField());
		axisPriorityComponents.add(facility_name.getField());
		axisPriorityComponents.add(instrument_name.getField());

		
		// = If Category=Entry or Category=Table
		if(this instanceof VPObservationEntryMappingPanel)
		{
			gbc.fill=GridBagConstraints.HORIZONTAL;
			gbc.gridwidth=GridBagConstraints.REMAINDER;
			
			//We separate the priority mapper from the fields
			JSeparator separator = new JSeparator();
			separator.setBackground(new Color(VPAxisPanel.SEPARATORCOLOR));
			separator.setForeground(new Color(VPAxisPanel.SEPARATORCOLOR));
			axisPanel.add(separator,
					gbc);

			gbc.newRow();
			gbc.fill=GridBagConstraints.NONE;
			
			JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELHEADER);
			
			gbc.right(false);
			
			subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
			axisPanel.add(subPanelTitle,gbc);
			
			gbc.newRow();
		}
		else
		{	
			priority.selector.buildMapper(axisPriorityComponents);		
		}
		
		obs_collection.setComponents();
		target_name.setComponents();
		facility_name.setComponents();
		instrument_name.setComponents();
		
	}
	
	@Override
	public ArrayList<String> getAxisParams() {
		// TODO Auto-generated method stub
		ArrayList<String> params = new ArrayList<String>();

		if (!getPriority().noBtn.isSelected())
		{

			params.add("-obsmapping="+priority.getMode());


			if(obs_collection.getText().length()>0)
				params.add("-obscollection="+obs_collection.getText());

			if(target_name.getText().length()>0)
				params.add("-target="+target_name.getText());

			if(facility_name.getText().length()>0)
				params.add("-facility="+facility_name.getText());

			if(instrument_name.getText().length()>0)
				params.add("-instrument="+instrument_name.getText());
		}
		return params;
	}

	@Override
	public String checkAxisParams() {
		String error ="";
		if(priority.isOnly())
		{
			if(obs_collection.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no collection specified</LI>";
			if(target_name.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no target specified</LI>";
			if(facility_name.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no facility specified</LI>";			
			if(instrument_name.getText().length()==0)
				error+= "<LI>Observation Axis : Priority \"Only\" selected but no instrument specified</LI>";

		}
		return error;
	}

	@Override
	public void reset() {
		super.reset();
		obs_collection.reset();
		target_name.reset();
		facility_name.reset();
		instrument_name.reset();
	}
}
