package saadadb.admintool.VPSandbox.components.input;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.MappingTextField;
import saadadb.admintool.utils.MyGBC;

public class VPKWNamedFieldUnits extends VPKWNamedField {

	private JComboBox comboBox ;
	
	public VPKWNamedFieldUnits(VPAxisPriorityPanel axisPanel, String name,MappingTextField field,String[] combo) {
		super(axisPanel, name, field);
		
		comboBox= new JComboBox(combo);
//		container = new JPanel(new GridLayout(0,2));
//		container.add(field);
		container.add(AdminComponent.getPlainLabel("Units "));
		container.add(comboBox);
	
	
	}
	
	
	public JComboBox getComboBox()
	{
		return comboBox;
	}
	


}
