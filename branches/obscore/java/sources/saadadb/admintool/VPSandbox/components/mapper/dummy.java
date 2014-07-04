package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Component;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;

public class dummy extends VPKwMapperPanel {

	private JTextField test1;
	private JButton test2;
	private JLabel test3;
	private JComboBox coosysCombo;
	private  JTextField coosysField;
	
	public dummy(VPAxisPriorityPanel axisPanel)
	{
		super(axisPanel);
		test1 = new JTextField("Ceci est aussi un test !");
		test2 = new JButton("clic");
		test3 = new JLabel("Test dummy ");
		components = new JComponent[2];
		coosysCombo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});
		coosysField = new JTextField("Ceci est un test");
		components[0]=coosysCombo;
		components[1]=coosysField;

//		gbc.next();gbc.left(true);
//		this.add(test2,gbc);
	}
	
	public void setComponents()
	{
		gbc.left(false);
		panel.add(coosysField,gbc);
		gbc.next();
		gbc.left(true);
		panel.add(coosysCombo,gbc);
		gbc.newRow();
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Test Dummy "), gbc);
		gbc.next();
		gbc.left(false);
		panel.add(test1,gbc);
		gbc.next();
		gbc.left(true);
		panel.add(test2,gbc);
	}
}
