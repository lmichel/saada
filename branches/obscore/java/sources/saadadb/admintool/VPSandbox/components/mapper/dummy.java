package saadadb.admintool.VPSandbox.components.mapper;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;

public class dummy extends VPKwMapperPanel {

	private JTextField test1;
	private JButton test2;
	private JLabel test3;
	
	
	public dummy(VPAxisPriorityPanel axisPanel)
	{
		super(axisPanel);
		test1 = new JTextField("Ceci est aussi un test !");
		test2 = new JButton("clic");
		test3 = new JLabel("Test dummy ");
		components[0]=test1;
		components[1]=test2;
		components[2]=test3;
		MyGBC gbc = axisPanel.gbc;
		JPanel panel = axisPanel.panel;
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Test Dummy "), gbc);
		gbc.next();
		gbc.left(false);
		panel.add(test1,gbc);
		gbc.next();
		gbc.left(true);
		panel.add(test2,gbc);
//		gbc.next();gbc.left(true);
//		this.add(test2,gbc);
	}
}
