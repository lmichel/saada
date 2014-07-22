package saadadb.admintool.VPSandbox.components.input;

import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import saadadb.admintool.VPSandbox.components.mapper.VPAxisPriorityPanel;

public class VPKWNamedComboBox extends VPKWMapper{
	
	private JComboBox comboBox;
	
	public VPKWNamedComboBox(VPAxisPriorityPanel axisPanel, String title, String[] choices) {
		super(axisPanel, title);
		comboBox = new JComboBox(choices);
		container=new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
		container.add(comboBox);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return comboBox.getSelectedItem().toString();
	}
	
	public JComboBox getComboBox()
	{
		return comboBox;
	}
	
	/**
	 * put the comboBox to its first index
	 */
	public void reset()
	{
		comboBox.setSelectedIndex(0);
	}
	

//	@Override
//	public void setComponents() {
//		// TODO Auto-generated method stub
//		panel = axisPanel.getPanel();
//		MyGBC gbc = axisPanel.getGbc();
//		gbc.right(false);
//		panel.add(AdminComponent.getPlainLabel(nameBox), gbc);
//		gbc.next();gbc.left(true);
//		panel.add(container,gbc);
//		gbc.newRow();
//		
//	}

}
