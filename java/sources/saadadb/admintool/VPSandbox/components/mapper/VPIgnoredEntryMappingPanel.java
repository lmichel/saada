package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.command.ArgsParser;
import saadadb.enums.DataMapLevel;

/**
 * This class inherit of IgnoredMappingPanel and represent the subpanel in the case where the category=TABLE
 * @author pertuy
 * @version $Id$
 */
public class VPIgnoredEntryMappingPanel extends VPIgnoredMappingPanel{
	private AppendMappingTextField ignoredKWField_entry;

	public VPIgnoredEntryMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel);

		/*
		 * We create a separator to separate the normal fields from the entries
		 */
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
		subPanelTitle.setFont(VPAxisPanel.SUBPANELTITLEFONT);
		
		gbc.right(false);
		
		subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
		axisPanel.add(subPanelTitle,gbc);
		
		gbc.newRow();
		gbc.right(false);
		
		axisPanel.add(AdminComponent.getPlainLabel("Ignored Keywords"),gbc);
		
		gbc.next();gbc.left(true);
		
		//We had the additional fields to the axis
		ignoredKWField_entry= new AppendMappingTextField(this.mappingPanel, DataMapLevel.KEYWORD, false, null);
		ignoredKWField_entry.setColumns(AdminComponent.STRING_FIELD_NAME);
		
		axisPanel.add(ignoredKWField_entry,gbc);
	}
	
	public ArrayList<String> getAxisParams(){
		ArrayList<String> params = super.getAxisParams();
		if(ignoredKWField_entry.getText().length()>0)
		{
			params.add("-entry.ignore="+ignoredKWField_entry.getText());	
		}
		return params;
	}
	
	public void reset()
	{
		super.reset();
		ignoredKWField_entry.setText("");
	}
	
	@Override
	public void setParams(ArgsParser ap) {
		super.setParams(ap);
		StringBuilder builder = new StringBuilder();
		for(String s : ap.getIgnoredAttributes(true)) {
		    builder.append(s);
		}
		ignoredKWField_entry.setText(builder.toString());
	}
}
