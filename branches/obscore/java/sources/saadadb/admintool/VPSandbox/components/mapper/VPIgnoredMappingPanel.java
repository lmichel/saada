package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.enums.DataMapLevel;

public class VPIgnoredMappingPanel extends VPAxisPanel {
	private AppendMappingTextField ignoredKWField;
	public VPIgnoredMappingPanel(VPSTOEPanel mappingPanel, boolean forEntry) {
		super(mappingPanel, "Ignored Keywords");
//		ignoredKWMapper = new VPKWNamedField(this,"Keywords",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,null));
//		ignoredKWMapper.setComponents();
//		gbc.next();gbc.right(true);
//		axisPanel.add(this.setHelpLabel(HelpDesk.CLASS_MAPPING),gbc);
		
		gbc.right(false);
		axisPanel.add(AdminComponent.getPlainLabel("Ignored Keywords"),gbc);
		gbc.next();gbc.left(true);
		ignoredKWField= new AppendMappingTextField(this.mappingPanel, DataMapLevel.KEYWORD, false, null);
		ignoredKWField.setColumns(AdminComponent.STRING_FIELD_NAME);
		axisPanel.add(ignoredKWField,gbc);
		gbc.next();
		axisPanel.add(setHelpLabel(HelpDesk.CLASS_MAPPING));
	}
	@Override
	public ArrayList<String> getAxisParams(){
		ArrayList<String> params = new ArrayList<String>();
		if(ignoredKWField.getText().length()>0)
		{
			if(mappingPanel.getCategory()!=Category.ENTRY)
			{
				params.add("-ignore="+ignoredKWField.getText());
			}
			else
			{
				params.add("-entry.ignore="+ignoredKWField.getText());
			}
		}
		return params;
	}
	@Override
	public String checkAxisParams() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void reset()
	{
		ignoredKWField.setText("");
	}


	
	

}
