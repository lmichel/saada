package saadadb.admin.mapping;



import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DialogConfigFileChooser;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class JDataLoaderConfPanel extends JPanel {
	protected JButton save_conf_button;
	protected JButton saveas_conf_button;
	protected JButton load_conf_button;
	protected MappingKWPanel mapping_misc_panel = null;
	protected MappingKWPanel mapping_img_panel = null;
	protected MappingKWPanel mapping_spect_panel = null;
	protected MappingKWPanel mapping_table_panel = null;
	protected MappingKWPanel mapping_flat_panel = null;
	protected JTabbedPane  onglets = null;
	//protected JScrollPane config_scroller;
	protected JFrame frame;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param frame
	 * @param dim
	 */
	public JDataLoaderConfPanel(JFrame frame, Dimension dim) {
		super();
		this.frame = frame;
		this.setPreferredSize(dim);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		this.insertMappingPanels(c, dim);
		
		JPanel btn_panel = new JPanel();
		
		
		load_conf_button = new JButton("Load Config");
		load_conf_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				DialogConfigFileChooser dial = new DialogConfigFileChooser();
				String conf_path = dial.open(JDataLoaderConfPanel.this.frame);
				JDataLoaderConfPanel.this.loadConfFile(conf_path);
			}
		});
		btn_panel.add(load_conf_button);
		
		save_conf_button= new JButton("Save Config.");
		save_conf_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				MappingKWPanel selected_panel = getMapping_panel() ;
				if( selected_panel.checkParams() ) {
					selected_panel.save();
				}
			}
		});
		btn_panel.add(save_conf_button);

		saveas_conf_button = new JButton("Save Config as");
		saveas_conf_button.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				MappingKWPanel selected_panel = getMapping_panel() ;
				if( selected_panel.checkParams() ) {
					selected_panel.saveAs();
				}
			}
		});
		btn_panel.add(saveas_conf_button);

		c.gridx = 0;
		c.gridy = 1;		
		c.fill =GridBagConstraints.NONE	;
		c.weightx = c.weighty = 0.0;
		this.add(btn_panel, c);		
		this.setPreferredSize(dim);
	}
	
	/**
	 * 
	 */
	private void insertMappingPanels(GridBagConstraints c, Dimension dim) {

		onglets = new JTabbedPane();
		c.gridx = 0;
		c.gridy = 0;
		c.fill =GridBagConstraints.BOTH	;
		c.weightx = c.weighty = 0.5;
		
		mapping_misc_panel = new MappingKWPanel(this, Category.MISC);
		JScrollPane misc_config_scroller = new JScrollPane(mapping_misc_panel);
		mapping_misc_panel.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		onglets.addTab("Misc", null, misc_config_scroller, "Configuration editor for Miscellaneous loading");
		
		mapping_img_panel = new MappingKWPanel(this, Category.IMAGE);
		JScrollPane img_config_scroller = new JScrollPane(mapping_img_panel);
		img_config_scroller.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		onglets.addTab("Image", null, img_config_scroller, "Configuration editor for images loading");

		mapping_spect_panel = new MappingKWPanel(this, Category.SPECTRUM);
		JScrollPane spect_config_scroller = new JScrollPane(mapping_spect_panel);
		spect_config_scroller.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		onglets.addTab("Spectrum", null, spect_config_scroller, "Configuration editor for spectra loading");

		mapping_table_panel = new MappingKWPanel(this, Category.TABLE);
		JScrollPane table_config_scroller = new JScrollPane(mapping_table_panel);
		table_config_scroller.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		onglets.addTab("Table", null, table_config_scroller, "Configuration editor for tables loading");

		mapping_flat_panel = new MappingKWPanel(this, Category.FLATFILE);
		JScrollPane flat_config_scroller = new JScrollPane(mapping_flat_panel);
		flat_config_scroller.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		onglets.addTab("Flatfile", null, flat_config_scroller, "Configuration editor for flatfiles loading");

		this.add(onglets, c);
		
	}
	
	/**
	 * @param conf_path
	 */
	public void loadConfFile(String conf_path) {
		ArgsParser ap = null;
		if( conf_path.length() != 0 ) {
			
			try {
				FileInputStream fis = new FileInputStream(conf_path);
				ObjectInputStream in = new ObjectInputStream(fis);
				ap = (ArgsParser)in.readObject();
				in.close();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				SaadaDBAdmin.showFatalError(JDataLoaderConfPanel.this, ex.toString());
				return;
			}				
			String conf_name = (new File(conf_path)).getName();
			MappingKWPanel selected_panel = null;
			if( conf_name.startsWith("MISC") ) {
				selected_panel = mapping_misc_panel;
				onglets.setSelectedIndex(0);
			}
			else if( conf_name.startsWith("IMAGE") ) {
				selected_panel = mapping_img_panel;
				onglets.setSelectedIndex(1);
			}
			else if( conf_name.startsWith("SPECTRUM") ) {
				selected_panel = mapping_spect_panel;
				onglets.setSelectedIndex(2);
			}
			else if( conf_name.startsWith("TABLE") ) {
				selected_panel = mapping_table_panel;
				onglets.setSelectedIndex(3);
			}
			else if( conf_name.startsWith("FLATFILE") ) {
				selected_panel = mapping_flat_panel;
				onglets.setSelectedIndex(4);
			}
			else {
				SaadaDBAdmin.showFatalError(JDataLoaderConfPanel.this, "Wrong product category");
				return;						
			}
			selected_panel.loadConfig(ap);
		}
	}
	
	/**
	 * @return
	 */
	public MappingKWPanel getMapping_panel() {
		int index = this.onglets.getSelectedIndex();
		switch( index) {
		case 0 : return mapping_misc_panel;
		case 1 : return mapping_img_panel;
		case 2 : return mapping_spect_panel;
		case 3 : return mapping_table_panel;
		case 4 : return mapping_flat_panel;
		default: return null;
		}
	}
	
	/**
	 * @return
	 */
	public void selectMapping_panel(String cat) {
		if( cat.equals("MISC") ) {
			this.onglets.setSelectedIndex(0);
		}
		else if( cat.equals("IMAGE") ) {
			this.onglets.setSelectedIndex(1);
		}
		else if( cat.equals("SPECTRUM") ) {
			this.onglets.setSelectedIndex(2);
		}
		else if( cat.equals("TABLE") ) {
			this.onglets.setSelectedIndex(3);
		}
		else if( cat.equals("FLATFILE") ) {
			this.onglets.setSelectedIndex(4);
		}
	}
	/**
	 * @return
	 */
	public void selectMapping_panel(int cat) {
		switch(cat){
		case Category.MISC: this.onglets.setSelectedIndex(0);break;
		case Category.IMAGE: this.onglets.setSelectedIndex(1);break;
		case Category.SPECTRUM: this.onglets.setSelectedIndex(2);break;
		case Category.TABLE: this.onglets.setSelectedIndex(3);break;
		case Category.FLATFILE: this.onglets.setSelectedIndex(4);break;
		}
	}
	
	/**
	 * @param cat
	 * @return
	 */
	public ArgsParser getArgsParser(int cat) {
		switch(cat) {
		case Category.MISC: return this.mapping_misc_panel.getArgsParser();
		case Category.IMAGE: return this.mapping_img_panel.getArgsParser();
		case Category.SPECTRUM: return this.mapping_spect_panel.getArgsParser();
		case Category.TABLE: return this.mapping_table_panel.getArgsParser();
		case Category.FLATFILE: return this.mapping_flat_panel.getArgsParser();
		default: return null;	
		}	
	}
	/**
	 * @param cat
	 * @return
	 */
	public ArgsParser getArgsParser(String cat) {
		if( cat.equals("MISC") ) {
			return this.mapping_misc_panel.getArgsParser();
		}
		else if( cat.equals("IMAGE") ) {
			return this.mapping_img_panel.getArgsParser();
		}
		else if( cat.equals("FLATFILE") ) {
			return this.mapping_flat_panel.getArgsParser();
		}
		else if( cat.equals("SPECTRUM") ) {
			return this.mapping_spect_panel.getArgsParser();
		}
		else if( cat.equals("TABLE") ) {
			return this.mapping_table_panel.getArgsParser();
		}
		else {
			return null;
		}
	}

	/**
	 * 
	 */
	public void setExteAtt() {
		if( mapping_misc_panel != null ) mapping_misc_panel.addAttExtendPanel();
		if( mapping_img_panel != null ) mapping_img_panel.addAttExtendPanel();
		if( mapping_spect_panel != null ) mapping_spect_panel.addAttExtendPanel();
		if( mapping_flat_panel != null ) mapping_flat_panel.addAttExtendPanel();
		if( mapping_table_panel != null ) {
			mapping_table_panel.addAttExtendPanel();
			mapping_table_panel.addEAttExtendPanel();
		}
	}

}
