package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.enums.DataMapLevel;
import saadadb.meta.AttributeHandler;

/**
 * Represent the Extended attributes axis/subpanel in the filter form
 * @author pertuy
 * @version $Id$
 */
public class VPOtherMappingPanel extends VPAxisPanel {

	//Contains the name of the extended attribute + its field
	private HashMap<String,JTextField> fields;

	public VPOtherMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Extended attributes");

		if(this instanceof VPOtherEntryMappingPanel)
		{
			JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELHEADER);
			gbc.right(false);
			subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
			axisPanel.add(subPanelTitle,gbc);
			gbc.newRow();
		}

		int cpt =0;
		JTextField field;
		fields=new HashMap<String,JTextField>();

		//We get the attributes in a map
		Map<String, AttributeHandler> attributes = Database.getCachemeta().getAtt_extend(Category.TABLE	/*mappingPanel.getCategory()*/);

		//For each attribute we create a line with a name and a field
		for( Entry<String, AttributeHandler> e: attributes.entrySet()){
			gbc.right(false);
			axisPanel.add(AdminComponent.getPlainLabel(e.getKey()), gbc);
			gbc.next();gbc.left(true);

			//Here we can add a type check to choose the Field we need
			field=new ReplaceMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,null);
			field.setColumns(AdminComponent.STRING_FIELD_NAME);
			fields.put(e.getKey(), field);
			axisPanel.add(field,gbc);

			//We set the helpLabel next the the first field
			if(cpt==0)
			{
				gbc.next();
				gbc.right(true);
				axisPanel.add(setHelpLabel(HelpDesk.CLASS_MAPPING),gbc);
			}
			gbc.newRow();
			cpt++;
		}
	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		for(Entry<String, JTextField> e: fields.entrySet())
		{
			if(e.getValue().getText().length()>0)
			{
				params.add("-ukw");
				params.add(e.getKey().trim()+"="+e.getValue().getText());
			}
		}
		return params;
	}

	@Override
	public String checkAxisParams() {
		return "";
	}

	public void reset() {
		for(Entry<String, JTextField> e: fields.entrySet())
		{
			e.getValue().setText("");
		}
	}

}
