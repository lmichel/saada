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
import saadadb.exceptions.FatalException;

public class VPObservationMappingPanel extends VPAxisPriorityPanel {
	

	
	public VPObservationMappingPanel(VPSTOEPanel mappingPanel) throws FatalException{
		super(mappingPanel, "Observation Axis",HelpDesk.CLASS_MAPPING);
		//JPanel panel =  getContainer().getContentPane();

		//dum = new dummy(this);
		kwMapper = new VPKWName(mappingPanel,this);
		priority.selector.buildMapper(kwMapper);

		helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);
		

		kwMapper.setComponents();
	
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
