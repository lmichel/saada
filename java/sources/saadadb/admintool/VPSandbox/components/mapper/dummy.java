package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import saadadb.admintool.utils.MyGBC;

public class dummy extends KwMapperPanel {

	private JTextField test1;
	private JButton test2;
	private JLabel test3;
	
	public dummy()
	{
		test1 = new JTextField();
		test2 = new JButton();
		test3 = new JLabel("Test dummy ");
		this.setLayout(new GridBagLayout());
		MyGBC gbc = new MyGBC(3,3,3,3);
		gbc.right(true);
		this.add(test3,gbc);
		gbc.next();
		gbc.left(false);
		this.add(test1,gbc);
		gbc.next();gbc.left(true);
		this.add(test2,gbc);
	}
}
