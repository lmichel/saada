package saadadb.admintool.VPSandbox.components.input;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;

public class VPKWNamedFieldUnits extends VPKWNamedField {

	private JComboBox comboBox ;
	
	public VPKWNamedFieldUnits(VPSTOEPanel mappingPanel,
			VPAxisPriorityPanel axisPanel, String name, boolean forEntry,
			ButtonGroup bg,String[] combo) {
		super(mappingPanel, axisPanel, name, forEntry, bg);
		
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
