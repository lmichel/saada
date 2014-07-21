package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.AdminTool;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedComboBox;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnButton;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.input.UcdTextField;
import saadadb.admintool.windows.VoTreeFrame;

public class VPObservableEntryMappingPanel extends VPObservableMappingPanel{

	private VPKWNamedFieldnButton oucd_entry;
	private VPKWNamedComboBox ocalibstatus_entry;
	
	//Just here to allow the use of the MetaDataPanel
	private SQLJTable productTable;

	public VPObservableEntryMappingPanel(VPSTOEPanel mappingPanel, final AdminTool rootFrame) {
		super(mappingPanel,rootFrame);
		
		//The separator
		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.gridwidth=GridBagConstraints.REMAINDER;
		JSeparator sep = new JSeparator();
		sep.setBackground(new Color(VPAxisPanel.SEPARATORCOLOR));
		axisPanel.add(sep,
				gbc);

		gbc.newRow();
		gbc.fill=GridBagConstraints.NONE;
		JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELENTRY);
		gbc.right(false);
		subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELCOLOR));
		axisPanel.add(subPanelTitle,gbc);
		gbc.newRow();
		
		
		oucd_entry = new VPKWNamedFieldnButton(this,"Entry_UCD ",new UcdTextField(50),new JButton("Open Meta Data panel"));
		ocalibstatus_entry = new VPKWNamedComboBox(this,"Entry_Calibration Status",new String[]{"","0","1","2","3","4"});
		axisPriorityComponents.add(oucd_entry.getField());
		axisPriorityComponents.add(oucd_entry.getButton());
		axisPriorityComponents.add(ocalibstatus_entry.getComboBox());
		priority.selector.buildMapper(axisPriorityComponents);		

		oucd_entry.setComponents();ocalibstatus_entry.setComponents();	

		/*
		 * Action Listener allowing the button to open the Metadata Panel
		 */
		oucd_entry.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				VoTreeFrame vtf = VoTreeFrame.getInstance(rootFrame, productTable);
				vtf.open(SQLJTable.META_PANEL);
			}
		});

	}
	
	
	public ArrayList<String> getAxisParams() {
		// TODO Auto-generated method stub
		ArrayList<String> params = super.getAxisParams();

		if (!getPriority().noBtn.isSelected())
		{
			if(oucd_entry.getText().length()>0)
				params.add("-entry.oucd="+oucd_entry.getText());

			if(ocalibstatus_entry.getComboBox().getSelectedItem().toString().length()>0)
				params.add("-entry.ocalibstatus="+ocalibstatus_entry.getComboBox().getSelectedItem().toString());
		}

		return params;
	}
	
	
	
	
	public String checkAxisParams() {
		String error =super.checkAxisParams();
		if(priority.isOnly())
		{

			if(oucd_entry.getText().length()==0)
				error+= "<LI>Observable Axis : Priority \"Only\" selected but no oucd specified</LI>";

			if(ocalibstatus_entry.getComboBox().getSelectedItem().toString().length()==0)
				error+= "<LI>Observable Axis : Priority \"Only\" selected but no calibration specified</LI>";			


		}

		return error;
	}

	
	public void reset() {
		super.reset();
		oucd_entry.reset();
		ocalibstatus_entry.reset();
	}
	
}
