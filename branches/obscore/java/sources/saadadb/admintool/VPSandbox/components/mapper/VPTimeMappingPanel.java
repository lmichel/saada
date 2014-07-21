package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.enums.DataMapLevel;

public class VPTimeMappingPanel extends VPAxisPriorityPanel {

	private VPKWNamedField tmin;
	private VPKWNamedField tmax;
	private VPKWNamedField exptime;
	
	public VPTimeMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Time Axis",HelpDesk.CLASS_MAPPING);
		tmin = new VPKWNamedField(this,"tMin ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		tmax = new VPKWNamedField(this,"tMax ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		exptime = new VPKWNamedField(this,"Exposure time ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		
		axisPriorityComponents = new ArrayList<JComponent>();

		//Fields dependant of the priority
		axisPriorityComponents.add(tmin.getField());
		axisPriorityComponents.add(tmax.getField());
		axisPriorityComponents.add(exptime.getField());
		priority.selector.buildMapper(axisPriorityComponents);
				
		tmin.setComponents();tmax.setComponents();exptime.setComponents();

	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{
			
			params.add("-timemapping="+priority.getMode());
			
//			if(mappingPanel.getCategory()!=Category.ENTRY)
//			{
				if(tmin.getText().length()>0)
					params.add("-tmin="+tmin.getText());
		
				if(tmax.getText().length()>0)
					params.add("-tmax="+tmax.getText());
		
				if(exptime.getText().length()>0)
					params.add("-exptime="+exptime.getText());
//			}
//			else
//			{
//				if(tmin.getText().length()>0)
//					params.add("-entry.tmin="+tmin.getText());
//		
//				if(tmax.getText().length()>0)
//					params.add("-entry.tmax="+tmax.getText());
//		
//				if(exptime.getText().length()>0)
//					params.add("-entry.exptime="+exptime.getText());
//			}

	
		}
		

		return params;	}

	@Override
	public String checkAxisParams() {
		String error ="";
		int nbFieldEmpty=0;
		if(priority.isOnly())
		{
			if(tmin.getText().length()==0)
				nbFieldEmpty++;
			if(tmax.getText().length()==0)
				nbFieldEmpty++;
			if(exptime.getText().length()==0)
				nbFieldEmpty++;

		}
		if(nbFieldEmpty>=2)
			error+="<LI>Time Axis : At least two fields must be specified with the priority \"Only\"</LI>";
		
		return error;
	}

	@Override
	public void reset() {
		super.reset();
		tmin.reset();
		tmax.reset();
		exptime.reset();
		
	}

}
