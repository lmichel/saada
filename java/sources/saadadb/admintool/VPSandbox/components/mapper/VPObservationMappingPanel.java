package saadadb.admintool.VPSandbox.components.mapper;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;

public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	
	private JComboBox coosysCombo;
	private  JTextField coosysField;
	
	public VPObservationMappingPanel(VPSTOEPanel mappingPanel){
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);
		//JPanel panel =  getContainer().getContentPane();
		coosysCombo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});
		coosysField = new JTextField("Ceci est un test");
		priority.selector.buildMapper(new JComponent[]{coosysCombo,coosysField});

		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		
		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Test "), gbc);
		gbc.next();
		gbc.left(false);
	
		panel.add(coosysField,gbc);
		gbc.next();
		gbc.left(true);
		panel.add(coosysCombo,gbc);
		gbc.newRow();
		dum = new dummy(this);
	
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
