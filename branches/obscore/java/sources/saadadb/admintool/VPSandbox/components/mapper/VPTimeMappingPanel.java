package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.enums.DataMapLevel;

public class VPTimeMappingPanel extends VPAxisPriorityPanel {

	private VPKWNamedField tmin;
	private VPKWNamedField tmax;
	private VPKWNamedField exptime;
	
	public VPTimeMappingPanel(VPSTOEPanel mappingPanel, boolean forEntry) {
		super(mappingPanel, "Time Axis",HelpDesk.CLASS_MAPPING);
		tmin = new VPKWNamedField(this,"tMin ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		tmax = new VPKWNamedField(this,"tMax ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		exptime = new VPKWNamedField(this,"Exposure time ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		priority.selector.buildMapper(new JComponent[]{tmin.getField(),tmax.getField(),exptime.getField()});
		
		tmin.setComponents();tmax.setComponents();exptime.setComponents();

	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{
			
			params.add("-timemapping="+priority.getMode());
			
			if(mappingPanel.getCategory()!=Category.ENTRY)
			{
				if(tmin.getText().length()>0)
					params.add("-tmin="+tmin.getText());
		
				if(tmax.getText().length()>0)
					params.add("-tmax="+tmax.getText());
		
				if(exptime.getText().length()>0)
					params.add("-exptime="+exptime.getText());
			}
			else
			{
				if(tmin.getText().length()>0)
					params.add("-entry.tmin="+tmin.getText());
		
				if(tmax.getText().length()>0)
					params.add("-entry.tmax="+tmax.getText());
		
				if(exptime.getText().length()>0)
					params.add("-entry.exptime="+exptime.getText());
			}

	
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
		tmin.reset();
		tmax.reset();
		exptime.reset();
		
	}

}
