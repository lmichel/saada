package saadadb.admintool.VPSandbox.components.mapper;

import java.util.ArrayList;

import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.command.ArgsParser;
import saadadb.vocabulary.enums.DataMapLevel;

/**
 * Represent the Time axis/subpanel in the filter form
 * @author pertuy
 * @version $Id$
 */
public class VPTimeMappingPanel extends VPAxisPriorityPanel {

	private VPKWNamedField tmin;
	private VPKWNamedField tmax;
	private VPKWNamedField exptime;

	/*
	 * See ObservationPanel for functional explanations
	 */
	public VPTimeMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Time Axis",HelpDesk.CLASS_MAPPING);
		tmin = new VPKWNamedField(this,"tMin ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		tmax = new VPKWNamedField(this,"tMax ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		exptime = new VPKWNamedField(this,"Exposure time ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));

		axisPriorityComponents = new ArrayList<JComponent>();

		//Fields dependent of the priority
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
			if(tmin.getText().length()>0)
				params.add("-tmin="+tmin.getText());

			if(tmax.getText().length()>0)
				params.add("-tmax="+tmax.getText());

			if(exptime.getText().length()>0)
				params.add("-exptime="+exptime.getText());
		}
		return params;	
	}

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

	@Override
	public void setParams(ArgsParser ap) {
		priority.noBtn.setSelected(false);
		switch(ap.getTimeMappingPriority()){
		case FIRST:priority.firstBtn.setSelected(true);
			break;
		case ONLY: priority.onlyBtn.setSelected(true);
			break;
		case LAST: priority.lastBtn.setSelected(true);
			break;
		default: priority.noBtn.setSelected(true);
		}
		if(fieldsEmpty(ap))
			priority.noBtn.setSelected(true);
		if(!priority.noBtn.isSelected())
		{
			tmin.setEnable(true);
			tmax.setEnable(true);
			exptime.setEnable(true);
		}
		tmin.setText(ap.getTmin(false));
		tmax.setText(ap.getTmax(false));
		exptime.setText(ap.getExpTime(false));
		
	}

	@Override
	public boolean fieldsEmpty(ArgsParser ap) {
		return ap.getTmin(false)==null && ap.getTmax(false)==null && ap.getExpTime(false)==null;
	}
	
}
