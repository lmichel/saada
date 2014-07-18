package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.enums.DataMapLevel;
import saadadb.meta.AttributeHandler;

public class VPOtherEntryMappingPanel extends VPOtherMappingPanel{

	private HashMap<String,JTextField> fields_entry;

	public VPOtherEntryMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel);


		//The separator
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		axisPanel.add(new JSeparator(),
				gbc);

		gbc.newRow();
		gbc.fill=GridBagConstraints.NONE;
		JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELENTRY);
		gbc.right(false);
		subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELCOLOR));
		axisPanel.add(subPanelTitle,gbc);
		gbc.newRow();

		JTextField field;
		//		lines= new ArrayList<VPKWNamedField>();
		fields_entry=new HashMap<String,JTextField>();
		//We get the attributes in a map

		Map<String, AttributeHandler> attributes = Database.getCachemeta().getAtt_extend(Category.TABLE	/*mappingPanel.getCategory()*/);

		//For each attribute we create a line with a name and a field
		for( Entry<String, AttributeHandler> e: attributes.entrySet()){
			//lines.add(new VPKWNamedField(this, e.getKey(),new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,null)));
			gbc.right(false);
			axisPanel.add(AdminComponent.getPlainLabel(e.getKey()), gbc);
			gbc.next();gbc.left(true);
			//Here we can add a type check to choose the Field we need

			field=new ReplaceMappingTextField(mappingPanel,DataMapLevel.KEYWORD, true,null);
			field.setColumns(AdminComponent.STRING_FIELD_NAME);
			fields_entry.put(e.getKey(), field);
			axisPanel.add(field,gbc);


			gbc.newRow();
		}
		
		
		
	}
	
	
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = super.getAxisParams();
		for(Entry<String, JTextField> e: fields_entry.entrySet())
		{
			if(e.getValue().getText().length()>0)
			{

				params.add("-eukw");
				params.add(e.getKey().trim()+"="+e.getValue().getText());

			}
		}
		return params;
	}


	public void reset() {
		super.reset();
		for(Entry<String, JTextField> e: fields_entry.entrySet())
		{
			e.getValue().setText("");
		}
	}

}
