/**
 * 
 */
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
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class PositionErrorMapperPanel extends PriorityPanel {
	protected AppendMappingTextField errorfield;
	protected JComboBox unitCombo;
	private boolean forEntry;

	public PositionErrorMapperPanel(MappingKWPanel mappingPanel, String title, boolean forEntry) {
		super(mappingPanel, title);
		this.forEntry = forEntry;
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		GridBagConstraints ccs = new GridBagConstraints();
		errorfield = new AppendMappingTextField(mappingPanel, 2, forEntry, buttonGroup);
		unitCombo  = new JComboBox(new String[]{"deg", "arcsec", "arcmin", "mas"});
		errorfield.setColumns(15);

		ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 2;
		new MapperPrioritySelector(new JRadioButton[] {onlyBtn, firstBtn, lastBtn, noBtn},noBtn, buttonGroup
				, new JComponent[]{unitCombo, errorfield}
		, panel, ccs);

		ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 1;
		panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);

		ccs.gridx = 0; ccs.gridy = 1;ccs.weightx = 0;
		errorfield.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( noBtn.isSelected() ) {
					firstBtn.setSelected(true) ;
				}
			}	
		});
		panel.add( errorfield, ccs);

		ccs.gridx = 1; ccs.gridy = 1;ccs.weightx = 0;
		panel.add(unitCombo, ccs);

		ccs.gridx = 2; ccs.gridy = 1;ccs.weightx = 1;
		panel.add(helpLabel, ccs);

	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.mapper.MappingPanel#getText()
	 */
	@Override
	public String getText() {
		return errorfield.getText();
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.components.mapper.MappingPanel#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		if( text == null ) {
			errorfield.setText("");	
		}
		else {
			errorfield.setText(text);	
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.components.mapper.MappingPanel#reset()
	 */
	@Override
	public void reset() {
		errorfield.setText("");
		setMode("no");
	}

	public String getUnit(){
		return unitCombo.getSelectedItem().toString().trim();
	}
	public void setUnit(String unit){
		for( int i=0 ; i<unitCombo.getItemCount() ; i++ ) {
			if( unitCombo.getItemAt(i).toString().equalsIgnoreCase(unit) ) {
				unitCombo.setSelectedIndex(i);
			}
		}
	}

	/**
	 * @param parser
	 */
	/*public void setParams(ArgsParser parser) {
		try {
			setMode(parser.getSpectralMappingPriority());
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
		setText(parser.getSpectralColumn());	
		setUnit(parser.getSpectralUnit());	
	}*/
	
	
	public void setParams(ArgsParser parser) {
		try {
			setMode(parser.getPoserrorMappingPriority());
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
		setText(getMergedComponent(parser.getPoserrorMapping()));
		setUnit(parser.getPoserrorUnit());
	}
	 

	/**
	 * @return
	 */
	public ArrayList<String> getParams() {
		ArrayList<String> retour = new ArrayList<String>();
		if( !this.isNo() ) {
			if( forEntry) {
				retour.add("-poserrormapping=" + this.getMode());						
				if(  this.getText().length() > 0 ) {
					retour.add("-poserror=" + this.getText());			
				}
				String param = this.getUnit();
				if( param.length() > 0 ) {
					retour.add("-poserrorunit=" + param);							
				}
			}
			else {
				retour.add("-poserrormapping=" + this.getMode());						
				if(  this.getText().length() > 0 ) {
					retour.add("-poserror=" + this.getText());			
				}
				String param = this.getUnit();
				if( param.length() > 0 ) {
					retour.add("-poserrorunit=" + param);							
				}
			}
		}
		return retour;
	}


}
