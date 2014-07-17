package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnBox;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.enums.DataMapLevel;

public class VPSpaceMappingPanel extends VPAxisPriorityPanel {

	//	private VPKWAppendTextField positionField;
	//	private VPKWAppendTextField errorField;
	//	private VPKWAppendTextField systemField;

	//private JComboBox unitCombo;

	private VPKWNamedFieldnBox positionError;
	private VPKWNamedField positionField;
	private VPKWNamedFieldnBox system;



	public VPSpaceMappingPanel(VPSTOEPanel mappingPanel,boolean forEntry) {
		super(mappingPanel, "Space Axis",HelpDesk.POSITION_MAPPING);
		positionField = new VPKWNamedField(this,"Position ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup));
		positionError = new VPKWNamedFieldnBox(this,"Position error ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup),new String[]{"","deg", "arcsec", "arcmin", "mas"});
		system = new VPKWNamedFieldnBox(this,"System ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, forEntry,priority.buttonGroup),new String[]{"", "ICRS", "FK5,J2000", "Galactic", "Ecliptic"});

		priority.selector.buildMapper(new JComponent[]{system.getField(),positionField.getField(),positionError.getField(),positionError.getComboBox(),system.getComboBox()});
		//helpLabel=setHelpLabel(HelpDesk.CLASS_MAPPING);


		positionField.setComponents();
		positionError.setComponents();
		system.setComponents();


		/*
		 * We had the action listener allowing to link the system box to the system field
		 */
		system.getComboBox().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox temp =system.getComboBox();
				if( e.getSource() == temp) {
					//We don't want to display the quotes if the blank option is selected
					if(temp.getSelectedIndex()!=0)
						system.getField().setText("'" + temp.getSelectedItem() + "'");
					else
						system.getField().setText("");
					
					if( priority.noBtn.isSelected() ) {
						priority.firstBtn.setSelected(true) ;
					}
				}
			}
		});


	}

	@Override
	public ArrayList<String> getAxisParams() {
		ArrayList<String> params = new ArrayList<String>();
		if (!getPriority().noBtn.isSelected())
		{

			params.add("-posmapping="+priority.getMode());
			if(system.getText().length()>0)
				params.add("-system="+system.getText());

			if(mappingPanel.getCategory()!=Category.ENTRY)
			{
				if(positionField.getText().length()>0)
					params.add("-position="+positionField.getText());
	
				if(positionError.getText().length()>0)
					params.add("-poserror="+positionError.getText());
	
				if(positionError.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-poserrorunit="+positionError.getComboBox().getSelectedItem().toString());
			}
			else
			{
				if(positionField.getText().length()>0)
					params.add("-entry.position="+positionField.getText());
	
				if(positionError.getText().length()>0)
					params.add("-entry.poserror="+positionError.getText());
	
				if(positionError.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-entry.poserrorunit="+positionError.getComboBox().getSelectedItem().toString());
				
			}

		}


		return params;
	}

	@Override
	public String checkAxisParams() {
		String error ="";
		if(priority.isOnly())
		{
			if(system.getText().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no system specified</LI>";
			if(positionField.getText().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position specified</LI>";
			if(positionError.getText().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position error specified</LI>";			
			if(positionError.getComboBox().getSelectedItem().toString().length()==0)
				error+= "<LI>Space Axis : Priority \"Only\" selected but no position error unit specified</LI>";

		}

		return error;
	}

	@Override
	public void reset() {
		system.reset();
		positionField.reset();
		positionError.reset();
		
		
	}

}
