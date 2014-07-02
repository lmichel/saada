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

public class VPObservableMappingPanel extends VPAxisPriorityPanel {
	
	private JComboBox coosysCombo;
	private  JTextField coosysField;
	
	public VPObservableMappingPanel(VPSTOEPanel mappingPanel){
		super(mappingPanel, "Observation Axis");
		JPanel panel =  getContainer().getContentPane();
		coosysCombo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});
		coosysField = new JTextField("Ceci est un test");
//		priority = new VPPriorityPanel(mappingPanel,"plop");
		MyGBC gbc = new MyGBC(3,3,3,3);
//		gbc.left(true);
//		panel.add(priority,gbc);
		priority.selector.buildMapper(new JComponent[]{coosysCombo,coosysField});
		gbc.gridwidth=3;
		//On fait 2 next d'affilés pour que la Grid comporte trois "emplacements" sur l'axe x
		//et que l'aide soit sur le dernier
		gbc.next();
		gbc.next();
		gbc.right(true);
		
		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		panel.add(helpLabel,gbc);
		gbc.newRow();

		gbc.right(false);
		panel.add(AdminComponent.getPlainLabel("Test "), gbc);
		gbc.next();
		gbc.left(false);
	
		panel.add(coosysField,gbc);
		gbc.next();
		gbc.left(true);
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
