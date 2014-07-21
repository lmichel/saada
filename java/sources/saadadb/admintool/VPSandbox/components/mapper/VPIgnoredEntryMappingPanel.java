package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.enums.DataMapLevel;

public class VPIgnoredEntryMappingPanel extends VPIgnoredMappingPanel{
	private AppendMappingTextField ignoredKWField_entry;

	public VPIgnoredEntryMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel);
		// TODO Auto-generated constructor stub
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


		gbc.right(false);
		axisPanel.add(AdminComponent.getPlainLabel("Ignored Keywords"),gbc);
		gbc.next();gbc.left(true);
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
}
