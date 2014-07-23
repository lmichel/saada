package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JLabel;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.enums.DataMapLevel;

public class VPIgnoredMappingPanel extends VPAxisPanel {
	
	private AppendMappingTextField ignoredKWField;
	
	/**
	 * Represent the Ignored Keywords ubpanel in the filter form
	 * @author pertuy
	 * @version $Id$
	 */
	public VPIgnoredMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Ignored Keywords");
		
		/*
		 * equivalent to "if(category==Table)"
		 */
		if(this instanceof VPIgnoredEntryMappingPanel)
		{
			//this label differentiate the entries fields from the normal ones.
			JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELHEADER);
			gbc.right(false);
			subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
			subPanelTitle.setFont(VPAxisPanel.SUBPANELTITLEFONT);

			axisPanel.add(subPanelTitle,gbc);
			gbc.newRow();
		}
		gbc.right(false);
		
		axisPanel.add(AdminComponent.getPlainLabel("Ignored Keywords"),gbc);
		
		gbc.next();
		gbc.left(false);
		
		ignoredKWField= new AppendMappingTextField(this.mappingPanel, DataMapLevel.KEYWORD, true, null);
		ignoredKWField.setColumns(AdminComponent.STRING_FIELD_NAME);
		
		axisPanel.add(ignoredKWField,gbc);
		
		gbc.next();
		gbc.right(true);
		
		axisPanel.add(setHelpLabel(HelpDesk.CLASS_MAPPING),gbc);
		
		gbc.newRow();
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
	@Override
	public void setParams(ArgsParser ap) {
		StringBuilder builder = new StringBuilder();
		for(String s : ap.getIgnoredAttributes(false)) {
		    builder.append(s);
		}
		ignoredKWField.setText(builder.toString());
	}


	
	

}
