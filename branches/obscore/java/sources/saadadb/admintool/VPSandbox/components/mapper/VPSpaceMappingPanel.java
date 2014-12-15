package saadadb.admintool.VPSandbox.components.mapper;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import saadadb.admintool.VPSandbox.components.input.VPKWNamedField;
import saadadb.admintool.VPSandbox.components.input.VPKWNamedFieldnBox;
import saadadb.admintool.VPSandbox.panels.editors.VPSTOEPanel;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.utils.HelpDesk;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.vocabulary.enums.DataMapLevel;

/**
 * Represent the Space axis/subpanel in the filter form
 * @author pertuy
 * @version $Id$
 */
public class VPSpaceMappingPanel extends VPAxisPriorityPanel {
	private VPKWNamedFieldnBox positionError;
	private VPKWNamedField positionField;
	private VPKWNamedFieldnBox system;

	/*
	 * See ObservationMapping for functional explanations
	 */
	public VPSpaceMappingPanel(VPSTOEPanel mappingPanel) {
		super(mappingPanel, "Space Axis",HelpDesk.POSITION_MAPPING);
		positionField = new VPKWNamedField(this,"Position ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup));
		positionError = new VPKWNamedFieldnBox(this,"Position error ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup),new String[]{"","deg", "arcsec", "arcmin", "mas"});
		system = new VPKWNamedFieldnBox(this,"System ",new AppendMappingTextField(mappingPanel,DataMapLevel.KEYWORD, false,priority.buttonGroup),new String[]{"", "ICRS", "FK5,J2000", "Galactic", "Ecliptic"});

		axisPriorityComponents = new ArrayList<JComponent>();
		//Field which are dependant of the priority
		axisPriorityComponents.add(positionField.getField());
		axisPriorityComponents.add(positionError.getField());
		axisPriorityComponents.add(positionError.getComboBox());
		axisPriorityComponents.add(system.getField());
		axisPriorityComponents.add(system.getComboBox());

		// = If Category=Entry or Category=Table
		if(this instanceof VPSpaceEntryMappingPanel)
		{
			gbc.fill=GridBagConstraints.HORIZONTAL;
			gbc.gridwidth=GridBagConstraints.REMAINDER;
			JSeparator separator = new JSeparator();
			separator.setBackground(new Color(VPAxisPanel.SEPARATORCOLOR));
			separator.setForeground(new Color(VPAxisPanel.SEPARATORCOLOR));
			axisPanel.add(separator,
					gbc);


			gbc.newRow();
			gbc.fill=GridBagConstraints.NONE;
			JLabel subPanelTitle = new JLabel(VPAxisPanel.SUBPANELHEADER);
			gbc.right(false);
			subPanelTitle.setForeground(new Color(VPAxisPanel.SUBPANELTITLECOLOR));
			subPanelTitle.setFont(VPAxisPanel.SUBPANELTITLEFONT);

			axisPanel.add(subPanelTitle,gbc);
			gbc.newRow();

		}
		else
		{	
			priority.selector.buildMapper(axisPriorityComponents);		
		}

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

			if(positionField.getText().length()>0)
				params.add("-position="+positionField.getText());

			if(positionError.getText().length()>0)
				params.add("-poserror="+positionError.getText());

			if(positionError.getComboBox().getSelectedItem()!=null)
			{
				if(positionError.getComboBox().getSelectedItem().toString().length()>0)
					params.add("-poserrorunit="+positionError.getComboBox().getSelectedItem().toString());
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
		super.reset();
		system.reset();
		positionField.reset();
		positionError.reset();
	}

	@Override
	public void setParams(ArgsParser ap) throws FatalException {
		priority.noBtn.setSelected(false);
		switch(ap.getPositionMappingPriority()){
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
			system.setEnable(true);
			positionError.setEnable(true);
			positionField.setEnable(true);
		}
		
		//We use a StringBuilder to transform the String[] into a simple String
		StringBuilder builder = new StringBuilder();
//		for(String s : ap.getCoordinateSystem()) {
//		    builder.append(s);
//		}
		//We delete the ' characters for the comboBox to correspond with the String it contains
		system.setText(builder.toString(), builder.toString().replace("'", ""));
		
		builder = new StringBuilder();
		for(String s : ap.getPositionMapping(false)) {
		    builder.append(s);
		}
		positionField.setText(builder.toString());
		positionError.setText(ap.getPoserrorMapping(false), ap.getPoserrorUnit(false));
		
	}

	@Override
	public boolean fieldsEmpty(ArgsParser ap) {
		boolean empty=true;
//		for(String s : ap.getCoordinateSystem()) {
//			empty=false;
//		}
		for(String s : ap.getPositionMapping(false)) {
			empty=false;		
			}
		return empty &&	ap.getPoserrorMapping(false)==null && ap.getPoserrorUnit(false)==null;

	}
}
