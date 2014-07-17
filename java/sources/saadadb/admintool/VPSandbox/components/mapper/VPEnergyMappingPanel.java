package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnBox;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.enums.DataMapLevel;

public class VPEnergyMappingPanel extends VPAxisPriorityPanel {

	// spectral range (=column)
	private VPKWNamedFieldnBox spcRange;
	private VPKWNamedField spcResPower;
	
	public VPEnergyMappingPanel(VPSTOEPanel mappingPanel, boolean forEntry) {
		super(mappingPanel, "Energy Axis",HelpDesk.DISPERSION_MAPPING);
		
		String[] specunit= new String[]{"","Angstrom","nm","um","m","mm","cm","km","nm","Hz","kHz","MHz","GHz","eV","keV","MeV","GeV","TeV"};
		
		spcResPower=new VPKWNamedField(this,"Resolution Power ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		spcRange = new VPKWNamedFieldnBox(this,"Spectral Range ",new ReplaceMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup), specunit);
		// Attention, ce ReplaceMappingTextField subit normalement un setcolumn(int) -> aucune idée de l'effet
		priority.selector.buildMapper(new JComponent[]{spcResPower.getField(),spcRange.getField(),spcRange.getComboBox()});
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
			
			if(mappingPanel.getCategory()!=Category.ENTRY)
			{
				if(spcRange.getText().length()>0)
					params.add("-spccolumn="+spcRange.getText());
				
				if(spcRange.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-spcunit="+spcRange.getComboBox().getSelectedItem().toString());
		
				if(spcResPower.getText().length()>0)
					params.add("-spcrespower="+spcResPower.getText());
			}
			else
			{
				if(spcRange.getText().length()>0)
					params.add("-entry.spccolumn="+spcRange.getText());
				
				if(spcRange.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-entry.spcunit="+spcRange.getComboBox().getSelectedItem().toString());
		
				if(spcResPower.getText().length()>0)
					params.add("-entry.spcrespower="+spcResPower.getText());
			}
			


	
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
		spcRange.reset();
		spcResPower.reset();
		
	}



}
