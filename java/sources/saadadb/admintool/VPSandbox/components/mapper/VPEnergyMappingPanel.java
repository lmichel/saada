package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnBox;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.enums.DataMapLevel;

/**
 * Represent the Energy axis in the panel
 * @author pertuy
 * @version $Id$
 */
public class VPEnergyMappingPanel extends VPAxisPriorityPanel {

	// spectral range (=column)
	/*
	 * Each attribute represent a line
	 */
	private VPKWNamedFieldnBox spcRange;
	private VPKWNamedField spcResPower;
	
	public VPEnergyMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Energy Axis",HelpDesk.DISPERSION_MAPPING);
		axisPriorityComponents=new ArrayList<JComponent>();
		String[] specunit= new String[]{"","Angstrom","nm","um","m","mm","cm","km","nm","Hz","kHz","MHz","GHz","eV","keV","MeV","GeV","TeV"};
		
		spcResPower=new VPKWNamedField(this,"Resolution Power ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		spcRange = new VPKWNamedFieldnBox(this,"Spectral Range ",new ReplaceMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup), specunit);
		// Attention, ce ReplaceMappingTextField subit normalement un setcolumn(int) -> aucune idée de l'effet
		axisPriorityComponents = new ArrayList<JComponent>();

		
		axisPriorityComponents.add(spcResPower.getField());
		axisPriorityComponents.add(spcRange.getField());
		axisPriorityComponents.add(spcRange.getComboBox());
		priority.selector.buildMapper(axisPriorityComponents);
		//helpLabel=setHelpLabel(HelpDesk.DISPERSION_MAPPING);
		spcRange.setComponents();
		spcResPower.setComponents();

	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{
			
			params.add("-spcmapping="+priority.getMode());
//			
//			if(mappingPanel.getCategory()!=Category.ENTRY)
//			{
				if(spcRange.getText().length()>0)
					params.add("-spccolumn="+spcRange.getText());
				
				if(spcRange.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-spcunit="+spcRange.getComboBox().getSelectedItem().toString());
		
				if(spcResPower.getText().length()>0)
					params.add("-spcrespower="+spcResPower.getText());
//			}
//			else
//			{
//				if(spcRange.getText().length()>0)
//					params.add("-entry.spccolumn="+spcRange.getText());
//				
//				if(spcRange.getComboBox().getSelectedItem().toString().length()>0)
//					params.add("-entry.spcunit="+spcRange.getComboBox().getSelectedItem().toString());
//		
//				if(spcResPower.getText().length()>0)
//					params.add("-entry.spcrespower="+spcResPower.getText());
//			}
			


	
		}

		return params;
	}

	@Override
	public String checkAxisParams() {
		// TODO Auto-generated method stub
		String error ="";
		if(priority.isOnly())
		{
			if(spcResPower.getText().length()==0)
				error+= "<LI>Energy Axis : Priority \"Only\" selected but no Spectral power specified</LI>";
			if(spcRange.getText().length()==0)
				error+= "<LI>Energy Axis : Priority \"Only\" selected but no range specified</LI>";
			if(spcRange.getComboBox().getSelectedItem().toString().length()==0)
				error+= "<LI>Energy Axis : Priority \"Only\" selected but no range unit specified</LI>";

		}

		return error;
	}

	@Override
	public void reset() {
		super.reset();
		spcRange.reset();
		spcResPower.reset();
		
	}



}
