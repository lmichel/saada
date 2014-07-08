package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.input.VPKWAppendTextField;
import saadadb.admintool.VPSandbox.components.input.VPKWMapperPanel;
import saadadb.admintool.VPSandbox.obsolet.VPKWName;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.enums.DataMapLevel;
import saadadb.exceptions.FatalException;

public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	
	private VPKWAppendTextField obs_collection;
	private VPKWAppendTextField target_name;
	private VPKWAppendTextField facility_name;
	private VPKWAppendTextField instrument_name;

	
	public VPObservationMappingPanel(VPSTOEPanel mappingPanel) throws FatalException{
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);
		//JPanel panel =  getContainer().getContentPane();
		obs_collection = new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		obs_collection.setColumns(AdminComponent.STRING_FIELD_NAME);
		target_name =  new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		target_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		facility_name = new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		facility_name.setColumns(AdminComponent.STRING_FIELD_NAME);
		instrument_name =  new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, false, null);
		instrument_name.setColumns(AdminComponent.STRING_FIELD_NAME);

		//dum = new dummy(this);
		kwMapper = new VPKWName(mappingPanel,this);
		priority.selector.buildMapper(new VPKWMapperPanel[]{obs_collection,target_name,facility_name,instrument_name});
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		

		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Collection "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(obs_collection,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Target Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(target_name,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Facility Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(facility_name,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Instrument Name "), gbc);
		gbc.next();
		gbc.left(true);
		panel.add(instrument_name,gbc);
		
		
//		kwMapper.setComponents();
	
//		
		
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public ArrayList<String> getAxisParams() {
		// TODO Auto-generated method stub
		ArrayList<String> params = new ArrayList<String>();
		boolean emptyField=false;
		
		if(getPriority().onlyBtn.isSelected())
			params.add("-obsmapping=only");
		
		if(getPriority().firstBtn.isSelected())
			params.add("-obsmapping=first");
		
		if(getPriority().lastBtn.isSelected())
			params.add("-obsmapping=last");
		
		if (!getPriority().noBtn.isSelected())
		{
			if(obs_collection.getText().length()>0)
				params.add("-obscollection="+obs_collection.getText());
			else
				emptyField=true;
			if(target_name.getText().length()>0)
				params.add("-target="+target_name.getText());
			else
				emptyField=true;
			if(facility_name.getText().length()>0)
				params.add("-facility="+facility_name.getText());
			else
				emptyField=true;
			if(instrument_name.getText().length()>0)
				params.add("-instrument="+instrument_name.getText());
			else
				emptyField=true;
		}
		if(getPriority().onlyBtn.isSelected() && emptyField)
			mappingPanel.showInfo(mappingPanel.rootFrame,"You selected the \"Only\" priority for the Observation Axis but didn't fill each corresponding field");
			

		return params;
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
//
//	@Override
//	public void setText(String text) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void reset() {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public String checkAxisParams() {
		// TODO Auto-generated method stub
		return null;
	}


}
