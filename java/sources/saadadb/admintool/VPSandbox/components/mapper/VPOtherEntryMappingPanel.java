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
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.meta.AttributeHandler;
import saadadb.vocabulary.enums.DataMapLevel;

/**
 * This class inherit of OtherMappingPanel and represent the subpanel in the case where the category=TABLE
 * @author pertuy
 * @version $Id$
 */
public class VPOtherEntryMappingPanel extends VPOtherMappingPanel{

	private HashMap<String,JTextField> fields_entry;

	public VPOtherEntryMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel);

		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		//The separator
		JSeparator separator = new JSeparator();
		separator.setBackground(new Color(VPAxisPanel.SEPARATORCOLOR));
		separator.setForeground(new Color(VPAxisPanel.SEPARATORCOLOR));
		axisPanel.add(separator,
				gbc);

		gbc.newRow();
		gbc.fill=GridBagConstraints.NONE;
		
		JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELENTRY);
		subPanelTitle.setFont(VPAxisPanel.SUBPANELTITLEFONT);
		
		gbc.right(false);
		
		subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
		axisPanel.add(subPanelTitle,gbc);
		
		gbc.newRow();

		JTextField field;
		fields_entry=new HashMap<String,JTextField>();
		
		//We get the attributes in a map

		Map<String, AttributeHandler> attributes = Database.getCachemeta().getAtt_extend(Category.TABLE	/*mappingPanel.getCategory()*/);

		//For each attribute we create a line with a name and a field
		for( Entry<String, AttributeHandler> e: attributes.entrySet()){
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
				params.add("-entry.ukw");
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
	
	public void setParams(ArgsParser ap) {
		super.setParams(ap);
		for( Entry<String, JTextField> e: fields_entry.entrySet()){
				e.getValue().setText(ap.getUserKeyword(false, e.getKey()));
		}
	}

}
