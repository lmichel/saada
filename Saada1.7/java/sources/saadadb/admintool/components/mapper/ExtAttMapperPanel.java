package saadadb.admintool.components.mapper;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.input.ReplaceMappingTextField;
import saadadb.admintool.panels.editors.MappingKWPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;

public class ExtAttMapperPanel extends MappingPanel {
	public final ReplaceMappingTextField[] mappingTextFields;
	public final JLabel[] mappingTextLabels;
	public final int category;

	public ExtAttMapperPanel(MappingKWPanel mappingPanel, String title, int category) {
		super(mappingPanel, title);
		this.category = category;
		JPanel panel =  container.getContentPane();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(AdminComponent.LIGHTBACKGROUND);
		boolean forEntry = (category == Category.ENTRY)? true: false;
		String[] ext_att;
		switch( category ) {
		case Category.MISC: ext_att = Database.getCachemeta().getAtt_extend_misc_names(); break;
		case Category.IMAGE: ext_att = Database.getCachemeta().getAtt_extend_image_names(); break;
		case Category.SPECTRUM: ext_att = Database.getCachemeta().getAtt_extend_spectrum_names(); break;
		case Category.TABLE: ext_att = Database.getCachemeta().getAtt_extend_table_names();
		case Category.ENTRY: ext_att = Database.getCachemeta().getAtt_extend_entry_names();break;
		case Category.FLATFILE: ext_att = Database.getCachemeta().getAtt_extend_flatfile_names(); break;
		default: ext_att  = null;
		}
		GridBagConstraints cae = new GridBagConstraints();
		cae.anchor = GridBagConstraints.EAST;
		int numLabels = ext_att.length;
		mappingTextFields = new ReplaceMappingTextField[numLabels];
		mappingTextLabels = new JLabel[numLabels];
		if( numLabels > 0 ) {
			for (int i = 0; i < numLabels; i++) {
				cae.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
				cae.fill = GridBagConstraints.NONE;      //reset to default
				cae.weightx = 0.0;                       //reset to default
				mappingTextLabels[i] = AdminComponent.getPlainLabel(ext_att[i]);
				mappingTextFields[i] = new ReplaceMappingTextField(this.mappingPanel, 2, forEntry, null);
				mappingTextFields[i].setColumns(10);
				panel.add(mappingTextLabels[i], cae);
				cae.gridwidth = GridBagConstraints.REMAINDER;     //end row
				cae.fill = GridBagConstraints.HORIZONTAL;
				cae.weightx = 1.0;
				panel.add(mappingTextFields[i], cae);
			}
		}
		else {
			cae.anchor = GridBagConstraints.WEST;
			cae.weightx = 1.0;                       //reset to default
			panel.add(AdminComponent.getHelpLabel(HelpDesk.get(HelpDesk.EXTATT_MISSING)), cae);
		}


	}

	public ArrayList<String> getParams() {
		ArrayList<String> retour = new ArrayList<String>();
		for( int i=0 ; i<mappingTextFields.length ; i++) {
			if( mappingTextFields[i].getText().length() > 0 ) {
				if( category == Category.ENTRY ){
					retour.add("-eukw");		
				}
				else {
					retour.add("-ukw");							
				}
				retour.add(mappingTextLabels[i].getText() + "=" + mappingTextFields[i].getText());							
			}			
		}
		return retour;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#getText()
	 */
	public  String getText() {
		return null;
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#setText(java.lang.String)
	 */
	public  void setText(String text) {
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.components.MappingPanel#reset()
	 */
	public  void reset() {
		for( ReplaceMappingTextField rmt: mappingTextFields) {
			rmt.setText("");		
		}
	}

	public void setParams(ArgsParser parser) {
		String[] ext_att = null;
		switch( this.category ) {
		case Category.MISC: ext_att = Database.getCachemeta().getAtt_extend_misc_names(); break;
		case Category.IMAGE: ext_att = Database.getCachemeta().getAtt_extend_image_names(); break;
		case Category.SPECTRUM: ext_att = Database.getCachemeta().getAtt_extend_spectrum_names(); break;
		case Category.TABLE: ext_att = Database.getCachemeta().getAtt_extend_table_names();break;
		case Category.FLATFILE: ext_att = Database.getCachemeta().getAtt_extend_flatfile_names(); break;
		}
		if( ext_att != null ) {
			for( int i=0 ; i<mappingTextLabels.length ; i++ ) {
				if( category == Category.ENTRY ) {
					mappingTextFields[i].setText(parser.getEntryUserKeyword(mappingTextLabels[i].getText()));					
				}
				else {
					mappingTextFields[i].setText(parser.getUserKeyword(mappingTextLabels[i].getText()));
				}
			}
		}
	}
}
