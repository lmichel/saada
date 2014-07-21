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

public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	
	
	private VPKWNamedField obs_collection;
	private VPKWNamedField target_name;
	private VPKWNamedField facility_name;
	private VPKWNamedField instrument_name;
	
	//protected ArrayList<JComponent> priorityComponents;
//	private JPanel panel;
//	private MyGBC obsgbc;
	
	public VPObservationMappingPanel(VPSTOEPanel mappingPanel){
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);
//		panel = new JPanel(new GridBagLayout());
//		panel.setBorder(BorderFactory.createLineBorder(Color.black));
//		obsgbc = new MyGBC(3,3,3,3);


		//We set the field we always display
		obs_collection = new VPKWNamedField(this,"Collection ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		target_name = new VPKWNamedField(this,"Target name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		facility_name = new VPKWNamedField(this,"Facility name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		instrument_name = new VPKWNamedField(this,"Instrument name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
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
		
//		gbc.fill=GridBagConstraints.HORIZONTAL;
//		gbc.gridwidth=GridBagConstraints.REMAINDER;
//		axisPanel.add(panel,gbc);
//		gbc.newRow();
//		

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

//		if(getPriority().onlyBtn.isSelected() && emptyField)
//			mappingPanel.showInfo(mappingPanel.rootFrame,"You selected the \"Only\" priority for the Observation Axis but didn't fill each corresponding field");
//			

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

//	public JPanel getPanel() {
//		return panel;
//	}
//
//	public MyGBC getGbc() {
//		return obsgbc;
//	}


}
