package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;

import saadadb.admintool.AdminTool;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedComboBox;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnButton;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.input.UcdTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.windows.VoTreeFrame;

/**
 * Represent the Observable axis/subpanel in the filter form
 * @author pertuy
 * @version $Id$
 */
public class VPObservableMappingPanel extends VPAxisPriorityPanel {

	private VPKWNamedFieldnButton oucd;
	private VPKWNamedComboBox ocalibstatus;

	//Just here to allow the use of the MetaDataPanel
	private SQLJTable productTable;

	public VPObservableMappingPanel(VPSTOEPanel mappingPanel, final AdminTool rootFrame) {
		super(mappingPanel, "Observable Axis",HelpDesk.CLASS_MAPPING);

		//We instantiate the objects representing the lines of the panel
		oucd = new VPKWNamedFieldnButton(this,"UCD ",new UcdTextField(50),new JButton("Open Meta Data panel"));
		ocalibstatus = new VPKWNamedComboBox(this,"Calibration Status",new String[]{"","0","1","2","3","4"});
		
		//We link the fields and the priority mapper
		axisPriorityComponents = new ArrayList<JComponent>();
		axisPriorityComponents.add(oucd.getField());
		axisPriorityComponents.add(oucd.getButton());
		axisPriorityComponents.add(ocalibstatus.getComboBox());
		priority.selector.buildMapper(axisPriorityComponents);		
		
		//We build the fields
		oucd.setComponents();ocalibstatus.setComponents();	

		/*
		 * Action Listener allowing the button to open the Metadata Panel
		 */
		oucd.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VoTreeFrame vtf = VoTreeFrame.getInstance(rootFrame, productTable);
				vtf.open(SQLJTable.META_PANEL);
			}
		});
	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{
			params.add("-observablemapping="+priority.getMode());
			if(oucd.getText().length()>0)
				params.add("-oucd="+oucd.getText());

			if(ocalibstatus.getComboBox().getSelectedItem().toString().length()>0)
				params.add("-ocalibstatus="+ocalibstatus.getComboBox().getSelectedItem().toString());
		}
		return params;
	}

	@Override
	public String checkAxisParams() {
		String error ="";
		if(priority.isOnly())
		{
			if(oucd.getText().length()==0)
				error+= "<LI>Observable Axis : Priority \"Only\" selected but no oucd specified</LI>";

			if(ocalibstatus.getComboBox().getSelectedItem().toString().length()==0)
				error+= "<LI>Observable Axis : Priority \"Only\" selected but no calibration specified</LI>";		
		}
		return error;
	}

	@Override
	public void reset() {
		super.reset();
		oucd.reset();
		ocalibstatus.reset();
	}
}
