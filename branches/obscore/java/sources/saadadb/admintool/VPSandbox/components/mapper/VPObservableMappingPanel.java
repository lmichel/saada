package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.MappingTextField;
import saadadb.admintool.components.mapper.PriorityPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;

public class VPObservableMappingPanel extends VPAxisPriorityPanel {
	private VPPriorityPanel priority;
	private JComboBox coosysCombo;
	private  JTextField coosysField;
	
	public VPObservableMappingPanel(VPSTOEPanel mappingPanel){
		super(mappingPanel, "Observation Axis");
		JPanel panel =  getContainer().getContentPane();
		coosysCombo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});
		coosysField = new JTextField();
		priority = new VPPriorityPanel(mappingPanel,"plop",new JComponent[]{coosysCombo,coosysField});
		MyGBC gbc = new MyGBC(3,3,3,3);
		gbc.left(true);
		panel.add(priority,gbc);
		gbc.next();
		gbc.right(true);
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		panel.add(helpLabel,gbc);
		gbc.newRow();
		gbc.left(false);
		panel.add(coosysField,gbc);
		gbc.next();
		gbc.left(false);
		panel.add(coosysCombo,gbc);
		gbc.newRow();
		gbc.left(false);
		dum = new dummy();
		panel.add(dum,gbc);
//		
	


		

		
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}
//
//	@Override
//	public void setText(String text) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void reset() {
//		// TODO Auto-generated method stub
//		
//	}


}
