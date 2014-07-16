package saadadb.admintool.VPSandbox.components.input;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.MappingTextField;
import saadadb.admintool.utils.MyGBC;

public class VPKWNamedFieldnBox extends VPKWNamedField {

	private JComboBox comboBox ;
	
	public VPKWNamedFieldnBox(VPAxisPriorityPanel axisPanel, String name,JTextField field,String[] combo) {
		super(axisPanel, name, field);
		
		comboBox= new JComboBox(combo);
//		container = new JPanel(new GridLayout(0,2));
//		container.add(field);
		//container.add(AdminComponent.getPlainLabel("Units "));
		container.add(comboBox);
	
	
	}
	
	
	public JComboBox getComboBox()
	{
		return comboBox;
	}
	
	@Override
	public void reset()
	{
		super.reset();
		comboBox.setSelectedIndex(0);
	}
	


}
