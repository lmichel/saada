package saadadb.admintool.components.mapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.AppendMappingTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.command.ArgsParser;
import saadadb.vocabulary.enums.DataMapLevel;

public class CoordSysMapperPanel extends PriorityPanel {
	private JComboBox coosysCombo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});
	private  AppendMappingTextField coosysField;


	public CoordSysMapperPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		GridBagConstraints ccs = new GridBagConstraints();

		coosysField = new AppendMappingTextField(mappingPanel, DataMapLevel.KEYWORD, false, buttonGroup);
		coosysCombo = new JComboBox(new String[]{"", "ICRS", "FK5,J2000", "Galactic", "Ecliptic"});

		ccs.gridx = 0; ccs.gridy = 0; ccs.weightx = 0;ccs.gridwidth = 2;
		new MapperPrioritySelector(new JRadioButton[] {onlyBtn, firstBtn, lastBtn, noBtn}, noBtn, buttonGroup
				, new JComponent[]{coosysField, coosysCombo}
		, panel, ccs);

		ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START; ccs.gridwidth = 1;
		panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);

		ccs.gridx = 0; ccs.gridy = 1; ccs.weightx = 0;
		coosysCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( e.getSource() == coosysCombo) {
					coosysField.setText("'" + coosysCombo.getSelectedItem() + "'");
					if( noBtn.isSelected() ) {
						firstBtn.setSelected(true) ;
					}
				}
			}
		});

		panel.add(coosysCombo, ccs);

		ccs.gridx = 1; ccs.gridy = 1; ccs.weightx = 0;ccs.fill = GridBagConstraints.HORIZONTAL;
		coosysField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( noBtn.isSelected() ) {
					firstBtn.setSelected(true) ;
				}
			}	
		});
		panel.add(coosysField, ccs);
		ccs.weightx = 1.0; ccs.gridx++;
		panel.add(helpLabel, ccs);

		this.setHelpLabel(HelpDesk.DISPERSION_MAPPING);

	}
	
	@Override
	public String getText() {
		return coosysField.getText();
	}

	@Override
	public void setText(String text) {
		if( text == null ) {
			coosysField.setText("");	
		}
		else {
			coosysField.setText(text);	
		}
	}

	@Override
	public void reset() {
		coosysField.setText("");
		setMode(null);
	}

	/**
	 * @param unit
	 */
	public void setSystem(String unit){
		for( int i=0 ; i<coosysCombo.getItemCount() ; i++ ) {
			if( coosysCombo.getItemAt(i).toString().equalsIgnoreCase(unit) ) {
				coosysCombo.setSelectedIndex(i);
			}
		}
	}

	/**
	 * @param parser
	 */
	public void setParams(ArgsParser parser) {
		setMode(parser.getSysMappingPriority());
		setText(getMergedComponent(parser.getCoordinateSystem()));	
		setSystem(parser.getSpectralUnit());	
	}

	/**
	 * @return
	 */
	public ArrayList<String> getParams() {
		ArrayList<String> retour = new ArrayList<String>();
		if( !this.isNo() ) {
			retour.add("-sysmapping=" + this.getMode());						
			if( this.getText().length() > 0 ) {
				retour.add("-system=" + coosysField.getText());			
			}
		}
		return retour;
	}
}
