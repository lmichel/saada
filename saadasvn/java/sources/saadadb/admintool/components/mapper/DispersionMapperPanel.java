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
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

public class DispersionMapperPanel extends PriorityPanel {
	protected ReplaceMappingTextField spec_field;
	protected JComboBox specunit_combo;

	public DispersionMapperPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		GridBagConstraints ccs = new GridBagConstraints();

		specunit_combo = new JComboBox();
		specunit_combo.addItem("");
		specunit_combo.addItem("Angstrom");
		specunit_combo.addItem("nm"       );
		specunit_combo.addItem("um"       ); 
		specunit_combo.addItem("m"        );
		specunit_combo.addItem("mm"       );
		specunit_combo.addItem("cm"       );
		specunit_combo.addItem("km"       );
		specunit_combo.addItem("nm"       );

		specunit_combo.addItem("Hz" );
		specunit_combo.addItem("kHz");
		specunit_combo.addItem("MHz");
		specunit_combo.addItem("GHz");

		specunit_combo.addItem("eV"  );
		specunit_combo.addItem("keV" );
		specunit_combo.addItem("MeV" );
		specunit_combo.addItem("GeV" );
		specunit_combo.addItem("TeV" );
		spec_field = new ReplaceMappingTextField(mappingPanel, 2, false, buttonGroup);
		spec_field.setColumns(15);


		ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 2;
		new MapperPrioritySelector(new JRadioButton[] {onlyBtn, firstBtn, lastBtn, noBtn}
		, noBtn
		, buttonGroup
		, new JComponent[]{specunit_combo, spec_field}
		, panel, ccs);

		ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 1;
		panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);

		ccs.gridx = 0; ccs.gridy = 1;ccs.weightx = 0;
		spec_field.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( noBtn.isSelected() ) {
					firstBtn.setSelected(true) ;
				}
			}	
		});
		panel.add(spec_field, ccs);

		ccs.gridx = 1; ccs.gridy = 1;ccs.weightx = 0;
		panel.add(specunit_combo, ccs);			

		ccs.gridx = 2; ccs.gridy = 1;ccs.weightx = 1;
		panel.add(helpLabel, ccs);
		setHelpLabel(HelpDesk.DISPERSION_MAPPING);
	}

	public String getUnit(){
		return specunit_combo.getSelectedItem().toString().trim();
	}
	public void setUnit(String unit){
		for( int i=0 ; i<specunit_combo.getItemCount() ; i++ ) {
			if( specunit_combo.getItemAt(i).toString().equalsIgnoreCase(unit) ) {
				specunit_combo.setSelectedIndex(i);
			}
		}
	}
	@Override
	public String getText() {
		return spec_field.getText();
	}

	@Override
	public void setText(String text) {
		if( text == null ) {
			spec_field.setText("");	
		}
		else {
			spec_field.setText(text);	
		}
	}

	@Override
	public void reset() {
		spec_field.setText("");
		setMode("no");
	}

	/**
	 * @param parser
	 */
	public void setParams(ArgsParser parser) {
		try {
			setMode(parser.getSpectralMappingPriority());
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
		setText(parser.getSpectralColumn());	
		setUnit(parser.getSpectralUnit());	
	}

	/**
	 * @return
	 */
	public ArrayList<String> getParams() {
		ArrayList<String> retour = new ArrayList<String>();
		if( !this.isNo() ) {
			retour.add("-spcmapping=" + this.getMode());						

			if(  this.getText().length() > 0 ) {
				retour.add("-spccolumn=" + this.getText());			
			}
			String param = this.getUnit();
			if( param.length() > 0 ) {
				retour.add("-spcunit=" + param);							
			}
		}
		return retour;
	}


}
