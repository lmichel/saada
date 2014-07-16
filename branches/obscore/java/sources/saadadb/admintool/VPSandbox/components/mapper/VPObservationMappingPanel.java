package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.enums.DataMapLevel;

public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	
//	private VPKWAppendTextField obs_collection;
//	private VPKWAppendTextField target_name;
//	private VPKWAppendTextField facility_name;
//	private VPKWAppendTextField instrument_name;
	
	private VPKWNamedField obs_collection;
	private VPKWNamedField target_name;
	private VPKWNamedField facility_name;
	private VPKWNamedField instrument_name;

	
	public VPObservationMappingPanel(VPSTOEPanel mappingPanel, boolean forEntry){
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);
		//JPanel panel =  getContainer().getContentPane();
		
		
		
//		obs_collection = new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		obs_collection.setColumns(AdminComponent.STRING_FIELD_NAME);
//		target_name =  new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		target_name.setColumns(AdminComponent.STRING_FIELD_NAME);
//		facility_name = new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		facility_name.setColumns(AdminComponent.STRING_FIELD_NAME);
//		instrument_name =  new VPKWAppendTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry, priority.buttonGroup);
//		instrument_name.setColumns(AdminComponent.STRING_FIELD_NAME);

		obs_collection = new VPKWNamedField(this,"Collection ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		target_name = new VPKWNamedField(this,"Target name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		facility_name = new VPKWNamedField(this,"Facility name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		instrument_name = new VPKWNamedField(this,"Instrument name ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		
		priority.selector.buildMapper(new JComponent[]{obs_collection.getField(),target_name.getField(),facility_name.getField(),instrument_name.getField()});		
		
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

			if(mappingPanel.getCategory()!=Category.ENTRY)
			{
				if(obs_collection.getText().length()>0)
					params.add("-obscollection="+obs_collection.getText());

				if(target_name.getText().length()>0)
					params.add("-target="+target_name.getText());

				if(facility_name.getText().length()>0)
					params.add("-facility="+facility_name.getText());

				if(instrument_name.getText().length()>0)
					params.add("-instrument="+instrument_name.getText());
			}
			else
			{
				if(obs_collection.getText().length()>0)
					params.add("-entry.obscollection="+obs_collection.getText());

				if(target_name.getText().length()>0)
					params.add("-entry.target="+target_name.getText());

				if(facility_name.getText().length()>0)
					params.add("-entry.facility="+facility_name.getText());

				if(instrument_name.getText().length()>0)
					params.add("-entry.instrument="+instrument_name.getText());
			}

		}
		
//		if(getPriority().onlyBtn.isSelected() && emptyField)
//			mappingPanel.showInfo(mappingPanel.rootFrame,"You selected the \"Only\" priority for the Observation Axis but didn't fill each corresponding field");
//			

		return params;
	}
//
//	@Override
//	public String getText() {
//		// TODO Auto-generated method stub
//		return null;
//	}
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
		obs_collection.reset();
		target_name.reset();
		facility_name.reset();
		instrument_name.reset();
	}


}
