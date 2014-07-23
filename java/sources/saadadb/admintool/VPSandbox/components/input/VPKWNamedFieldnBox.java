package saadadb.admintool.VPSandbox.components.input;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;

/**
 * Represent a line in an Axis panel composed of a name, an AppendTextField (KeyWord mapped field) and a comboBox
 * @author pertuy
 * @version $Id$
 */
public class VPKWNamedFieldnBox extends VPKWNamedField {

	private JComboBox comboBox ;

	public VPKWNamedFieldnBox(VPAxisPriorityPanel axisPanel, String name,JTextField field,String[] combo) {
		super(axisPanel, name, field);
		comboBox= new JComboBox(combo);
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
		comboBox.setEnabled(false);
	}
	
	public void setText(String text,String boxText) {
		super.setText(text);
		comboBox.setSelectedItem(boxText);
	}
	
	@Override
	public void setEnable(boolean enable) {
		super.setEnable(enable);
		comboBox.setEnabled(enable);		
	}



}
