package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.utils.MyGBC;

public class VPAxisPriorityPanel extends VPAxisPanel {

	private VPPriorityPanel priority;
	dummy dum;

	public VPAxisPriorityPanel(VPSTOEPanel mappingPanel,String title) {
		// TODO Auto-generated constructor stub
		super(title);
		JPanel panel = new JPanel(new GridBagLayout());

		panel=this.getContainer().getContentPane();
		MyGBC gbc = new MyGBC(3,3,3,3);
//		gbc.right(true);
//		panel.add(AdminComponent.getPlainLabel("Priority "), gbc);
//		gbc.next();
//		gbc.left(false);

	
	
	}


	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

}
