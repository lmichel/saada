package saadadb.admintool.panels.editors;


import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AppendMappingTextField;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.ExtMappingTextField;
import saadadb.admintool.components.MapperPrioritySelector;
import saadadb.admintool.components.ReplaceMappingTextField;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.dialogs.DialogConfName;
import saadadb.admintool.dialogs.DialogConfigFileChooser;
import saadadb.admintool.dialogs.DialogFileChooser;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.tree.VoDataProductTree;
import saadadb.api.SaadaDB;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.util.DefineType;
import saadadb.util.Messenger;
import saadadb.util.RegExp;



/**
 * TODO : specialize this class by inheritance
 * @author laurentmichel
 *
 */
public class MappingKWPanel extends EditPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int category;
	private String confName = "Default";

	private AppendMappingTextField name_compo;
	protected AppendMappingTextField e_name_compo;

	protected AppendMappingTextField ignored_compo;
	protected AppendMappingTextField e_ignored_compo;

	protected ReplaceMappingTextField[] extatt_fields;
	protected ReplaceMappingTextField[] e_extatt_fields;
	protected String[] ext_att = null;
	protected String[] e_ext_att = null;

	protected JRadioButton coosys_only_btn;
	protected JRadioButton coosys_first_btn ;
	protected JRadioButton coosys_last_btn;
	protected JRadioButton coosys_no_btn;
	protected JComboBox coosys_combo;
	protected AppendMappingTextField coosys_field;

	protected JRadioButton coo_only_btn;
	protected JRadioButton coo_first_btn ;
	protected JRadioButton coo_last_btn;
	protected JRadioButton coo_no_btn;
	protected AppendMappingTextField coo_field;

	protected JRadioButton errcoo_only_btn;
	protected JRadioButton errcoo_first_btn ;
	protected JRadioButton errcoo_last_btn;
	protected JRadioButton errcoo_no_btn;
	protected AppendMappingTextField errcoo_field;
	protected JComboBox errcoo_unit;

	protected JRadioButton spec_only_btn;
	protected JRadioButton spec_first_btn ;
	protected JRadioButton spec_last_btn ;
	protected JRadioButton spec_no_btn ;
	protected ReplaceMappingTextField spec_field;
	protected JComboBox specunit_combo;

	public ExtMappingTextField extension_field;
	protected JRadioButton classifier_btn;
	protected JRadioButton fusion_btn ;
	protected JRadioButton noclass_btn ;
	protected JTextField class_field;

	private JPanel category_panel;
	private JPanel extension_panel;
	private JPanel class_panel;
	private JPanel ignored_panel;
	private JPanel e_ignored_panel;
	private JPanel entry_panel;
	private JPanel name_panel;
	private JPanel e_name_panel;
	private JPanel extatt_panel;
	private JPanel e_extatt_panel;
	private JPanel coordsys_panel;	
	private JPanel coord_panel;	
	private JPanel coorderror_panel;	
	private JPanel spccoord_panel;	
	private GridBagConstraints globalGridConstraint ;
	private String last_saved = "";
	JPanel editorPanel;

	public MappingKWPanel(AdminTool rootFrame, String title, int category, String ancestor) {
		super(rootFrame, title, null, ancestor);
		this.category = category;
	}



	/**
	 * 
	 */
	private void addCategoryPanel() {
		if( category_panel == null ) {
			category_panel = new JPanel(new GridBagLayout());			
			category_panel.setBackground(LIGHTBACKGROUND);

			GridBagConstraints ccs = new GridBagConstraints();

			ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0.33;ccs.anchor = GridBagConstraints.CENTER;
			JLabel ds = AdminComponent.getPlainLabel("<HTML><A HREF=>Form Reset</A>");
			ds.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) {
					reset(false);
				}
			});
			category_panel.add(ds, ccs);
			ccs.gridx++; 

			ds = AdminComponent.getPlainLabel("<HTML><A HREF=>Loader Parameters</A>");
			ds.setToolTipText("Show dataloader parameters matching the current configuration.");
			ds.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) {
					ArgsParser args_parser = getArgsParser();
					if( args_parser != null ) {
						String[] args = args_parser.getArgs();
						String summary = "";
						for( int i=0 ; i<args.length ; i++ ) {
							summary += args[i] + "\n";
						}
						AdminComponent.showCopiableInfo(rootFrame, summary,"Loader Parameters");
					}
				}
			});
			category_panel.add(ds, ccs);
			ccs.gridx++; 

			ds = AdminComponent.getPlainLabel("<HTML><A HREF=>Data Sample</A> ");
			ds.setToolTipText("Show dataloader parameters matching the current configuration.");
			ds.addMouseListener(new MouseAdapter(){
				public void mouseReleased(MouseEvent e) {
					DialogFileChooser fcd = new DialogFileChooser();
					String filename  = fcd.open(rootFrame, true);
					if( filename.length() == 0 ) {
						return ;
					}
					rootFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					JFrame window = new JFrame(new File(filename).getName());
					VoDataProductTree vot;
					vot = new VoDataProductTree(window
							, "ext/keywords (drag & drop to the loader configuration panel)"
							, filename);
					rootFrame.setCursor(Cursor.getDefaultCursor());
					vot.buildTree(new Dimension(300, 500));
					vot.setPreferredSize(new Dimension(300, 500));
					window.add(vot);
					window.pack();
					window.setVisible(true);				
				}
			});
			category_panel.add(ds, ccs);

			globalGridConstraint.weightx = 1;
			editorPanel.add(category_panel, globalGridConstraint);
		}
	}

	public int getCategory() {
		return category;
	}

	/**			GridBagConstraints ccs = new GridBagConstraints();

	 * 
	 */
	private void addExtensionPanel() {
		if( extension_panel == null ) {
			CollapsiblePanel extension_collpanel = new CollapsiblePanel("Extension to Load");	
			extension_panel = extension_collpanel.getContentPane();
			extension_panel.setBackground(LIGHTBACKGROUND);
			extension_panel.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();

			ccs.gridx = 0; ccs.gridy = 0; ccs.anchor = GridBagConstraints.LINE_START; ccs.weightx = 0;
			extension_field = new ExtMappingTextField(this, 1, false, null);
			extension_field.setColumns(15);		
			extension_panel.add(extension_field, ccs);

			ccs.gridx++; ccs.weightx = 1.0;
			extension_panel.add(AdminComponent.getHelpLabel(
					new String[]{"Drop an extension from the Data Sample window" 
							, "or put a number prefixed with a #"
							, "Keywords of the first extension are loaded by default"})
							, ccs);


			globalGridConstraint.weightx = 0;
			editorPanel.add(extension_collpanel, globalGridConstraint);
			/*
			 * changing the extension may induce a form reset
			 */
			extension_field.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {		
					MappingKWPanel.this.checkExtensionChange();					
				}

			});
		}

	}

	/**
	 * 
	 */
	private void addClassPanel() {
		if( class_panel == null ) {
			CollapsiblePanel class_collpanel = new CollapsiblePanel("Class Mapping");	
			class_panel = class_collpanel.getContentPane();
			class_panel.setLayout(new GridBagLayout());					
			class_panel.setBackground(LIGHTBACKGROUND);;					
			GridBagConstraints ccs = new GridBagConstraints();

			classifier_btn = new JRadioButton("Automatic Classifier");
			classifier_btn.setToolTipText("One class (with a name derived from this you give) created for each group of identical product files");
			fusion_btn     = new JRadioButton("Class Fusion");
			fusion_btn.setToolTipText("One class merging all product files will be created");
			noclass_btn    = new JRadioButton("Default");
			fusion_btn.setToolTipText("One class (with a default name) created for each group of identical product files");
			class_field    = new JTextField();

			ccs.gridx = 0; ccs.gridy = 0;
			ccs.weightx = 0.0;                       //reset to default
			ccs.anchor = GridBagConstraints.LINE_END;
			class_panel.add(AdminComponent.getPlainLabel("Mapping Mode "), ccs);

			ccs.anchor = GridBagConstraints.LINE_START;
			ccs.gridx = 1; ccs.gridy = 0; ccs.gridwidth = 2;
			new MapperPrioritySelector(new JRadioButton[] {classifier_btn, fusion_btn, noclass_btn}, noclass_btn, new ButtonGroup(), new JComponent[] {class_field}, class_panel, ccs);

			ccs.gridx = 0; ccs.gridy = 1;ccs.anchor = GridBagConstraints.LINE_END;ccs.gridwidth = 1;
			class_panel.add(AdminComponent.getPlainLabel("Class Name"), ccs);
			class_field.setColumns(10);
			class_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( MappingKWPanel.this.noclass_btn.isSelected() ) {
						MappingKWPanel.this.fusion_btn.setSelected(true) ;
					}
				}	
			});
			ccs.gridx = 1; ccs.gridy = 1;
			ccs.anchor = GridBagConstraints.LINE_START;
			class_panel.add(class_field, ccs);

			ccs.gridx = 2; ccs.gridy = 1; 
			ccs.weightx = 1.0;                       
			class_panel.add(getHelpLabel(
					new String[] {"The class name must only contain letters, numbers or undescores."
							, "It can not starts with a number"
							, "It must be unique in the DB."})
							, ccs);

			editorPanel.add(class_collpanel, globalGridConstraint);
		}

	}
	/**
	 * 
	 */
	private void addNamePanel() {
		if( name_panel == null ) {
			CollapsiblePanel name_collpanel = new CollapsiblePanel("Instance Name");	
			name_panel = name_collpanel.getContentPane();
			name_panel.setLayout(new GridBagLayout());
			name_panel.setBackground(LIGHTBACKGROUND);
			
			name_compo = new AppendMappingTextField(this, 2, false, null);
			name_compo.setColumns(STRING_FIELD_NAME);
			GridBagConstraints cae = new GridBagConstraints();
			cae.anchor = GridBagConstraints.WEST;
			cae.weightx = 0.0;                       
			cae.gridx = 0; cae.gridy = 0;
			name_panel.add(name_compo, cae);
			cae.weightx = 1.0;                       //reset to default
			cae.gridx++;
			name_panel.add(getHelpLabel("List of keywords used to build the product name\nKWs must be comme separated.\nConstant string must be quoted"), cae);
			editorPanel.add(name_collpanel, globalGridConstraint);
		}
	}	

	/**
	 * 
	 */
	private void addIgnoredPanel() {
		if( ignored_panel == null ) {
			CollapsiblePanel ignored_collpanel = new CollapsiblePanel("Ignored Keywords");	
			ignored_panel = ignored_collpanel.getContentPane();
			ignored_panel.setLayout(new GridBagLayout());
			ignored_panel.setBackground(LIGHTBACKGROUND);
			ignored_compo = new AppendMappingTextField(this, 2, false, null);
			ignored_compo.setColumns(STRING_FIELD_NAME);
			GridBagConstraints cae = new GridBagConstraints();
			cae.anchor = GridBagConstraints.WEST;
			cae.weightx = 0.0;                       //reset to default
			cae.gridx = 0; cae.gridy = 0;
			ignored_panel.add(ignored_compo, cae);
			cae.weightx = 1.0;                       //reset to default
			cae.gridx++;
			ignored_panel.add(getHelpLabel("List of ignored Keywords\nKWs must be comme separated."), cae);
			editorPanel.add(ignored_collpanel, globalGridConstraint);
		}
	}	
	/**
	 * 
	 */
	void addAttExtendPanel() {
		if( extatt_panel == null ) {
			CollapsiblePanel extatt_collpanel = new CollapsiblePanel("Extended Collection Attributes");	
			extatt_panel = extatt_collpanel.getContentPane();
			extatt_panel.setBackground(LIGHTBACKGROUND);
			extatt_panel.setLayout(new GridBagLayout());
			switch( this.category ) {
			case Category.MISC: ext_att = Database.getCachemeta().getAtt_extend_misc_names(); break;
			case Category.IMAGE: ext_att = Database.getCachemeta().getAtt_extend_image_names(); break;
			case Category.SPECTRUM: ext_att = Database.getCachemeta().getAtt_extend_spectrum_names(); break;
			case Category.TABLE: ext_att = Database.getCachemeta().getAtt_extend_table_names();
			e_ext_att = Database.getCachemeta().getAtt_extend_entry_names();break;
			case Category.FLATFILE: ext_att = Database.getCachemeta().getAtt_extend_flatfile_names(); break;
			default: ext_att = e_ext_att = null;
			}
			GridBagConstraints cae = new GridBagConstraints();
			cae.anchor = GridBagConstraints.EAST;
			int numLabels = ext_att.length;
			extatt_fields = new ReplaceMappingTextField[numLabels];
			if( numLabels > 0 ) {
				for (int i = 0; i < numLabels; i++) {
					cae.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
					cae.fill = GridBagConstraints.NONE;      //reset to default
					cae.weightx = 0.0;                       //reset to default
					extatt_panel.add(AdminComponent.getPlainLabel(ext_att[i]), cae);
					extatt_fields[i] = new ReplaceMappingTextField(this, 2, false, null);
					extatt_fields[i].setColumns(10);
					cae.gridwidth = GridBagConstraints.REMAINDER;     //end row
					cae.fill = GridBagConstraints.HORIZONTAL;
					cae.weightx = 1.0;
					extatt_panel.add(extatt_fields[i], cae);
				}
			}
			else {
				cae.anchor = GridBagConstraints.WEST;
				//				cae.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
				//				cae.fill = GridBagConstraints.NONE;      //reset to default
				cae.weightx = 1.0;                       //reset to default
				extatt_panel.add(AdminComponent.getHelpLabel("No extended attribute.\nExtended attributes must be set at DB creation time.\nThey can no longer be added after that."), cae);
			}

			editorPanel.add(extatt_collpanel, globalGridConstraint);		
		}
	}
	/**
	 * 
	 */
	private void addCoordSysPanel() {
		if( coordsys_panel == null ) {
			CollapsiblePanel coordsys_collpanel = new CollapsiblePanel("Coordinate System");	
			coordsys_panel = coordsys_collpanel.getContentPane();
			coordsys_panel.setBackground(LIGHTBACKGROUND);	
			coordsys_panel.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();

			coosys_only_btn  = new JRadioButton("only");
			coosys_first_btn = new JRadioButton("first");
			coosys_last_btn  = new JRadioButton("last");
			coosys_no_btn    = new JRadioButton("no mapping");
			ButtonGroup bg   = new ButtonGroup();
			coosys_field = new AppendMappingTextField(this, 2, false, bg);
			coosys_combo = new JComboBox(new String[]{"ICRS", "FK5,J2000", "Galactic", "Ecliptic"});

			ccs.gridx = 0; ccs.gridy = 0; ccs.weightx = 0;ccs.gridwidth = 2;
			new MapperPrioritySelector(new JRadioButton[] {coosys_only_btn, coosys_first_btn, coosys_last_btn, coosys_no_btn}, coosys_no_btn, bg
					, new JComponent[]{coosys_field, coosys_combo}
					, coordsys_panel, ccs);
			
			ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START; ccs.gridwidth = 1;
			coordsys_panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);

			ccs.gridx = 0; ccs.gridy = 1; ccs.weightx = 0;
			coosys_combo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( e.getSource() == coosys_combo) {
						coosys_field.setText("'" + coosys_combo.getSelectedItem() + "'");
						if( MappingKWPanel.this.coosys_no_btn.isSelected() ) {
							MappingKWPanel.this.coosys_first_btn.setSelected(true) ;
						}
					}
				}

			});
			coordsys_panel.add(coosys_combo, ccs);
			
			ccs.gridx = 1; ccs.gridy = 1; ccs.weightx = 0;ccs.fill = GridBagConstraints.HORIZONTAL;
			coosys_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( MappingKWPanel.this.coosys_no_btn.isSelected() ) {
						MappingKWPanel.this.coosys_first_btn.setSelected(true) ;
					}
				}	
			});
			coordsys_panel.add(coosys_field, ccs);
			
			ccs.gridx = 2; ccs.gridy = 1; ccs.weightx = 1;
			coordsys_panel.add(AdminComponent.getHelpLabel(
					new String[] {
							"Select a Coordinate Systems"
							, "or give quoted constant values"
							, "or a keyword dropped from the Data Sample window "})
					, ccs);
		
		
			editorPanel.add(coordsys_collpanel, globalGridConstraint);
		}
	}

	/**
	 * 
	 */
	private void addPositionPanel() {
		if( coord_panel == null ) {
			CollapsiblePanel coord_collpanel = new CollapsiblePanel("Error on Position");	
			coord_panel = coord_collpanel.getContentPane();
			coord_panel.setBackground(LIGHTBACKGROUND);
			coord_panel.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();
			ButtonGroup bg = new ButtonGroup();
			coo_only_btn = new JRadioButton("only");
			coo_first_btn = new JRadioButton("first");
			coo_last_btn = new JRadioButton("last");
			coo_no_btn = new JRadioButton("no mapping");
			coo_field = new AppendMappingTextField(this, 2, false, bg);

			ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0;
			new MapperPrioritySelector(new JRadioButton[] {coo_only_btn, coo_first_btn, coo_last_btn, coo_no_btn}, coo_no_btn, bg
					, new JComponent[]{coo_field}
			        , coord_panel, ccs);

			ccs.gridx = 1; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START;
			coord_panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);
			
			ccs.gridx = 0; ccs.gridy = 1;ccs.weightx = 0;ccs.fill = GridBagConstraints.HORIZONTAL;
			coo_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( MappingKWPanel.this.coo_no_btn.isSelected() ) {
						MappingKWPanel.this.coo_first_btn.setSelected(true) ;
					}
				}	
			});
			coord_panel.add(coo_field, ccs);

			ccs.gridx = 1; ccs.gridy = 1;ccs.weightx = 1;
			coord_panel.add(AdminComponent.getHelpLabel(
					new String[] {
							"Give a quoted object name or position or keywords"
					  	  , "with the following format RA[,DEC]"
					  	  , "Keywords can (must) be dropped from the Data Sample window"
					})
					, ccs);

			editorPanel.add(coord_collpanel, globalGridConstraint);
		}
	}

	/**
	 * 
	 */
	private void addErrorPositionPanel() {
		if( coorderror_panel == null ) {
			CollapsiblePanel coorderror_collpanel = new CollapsiblePanel("Error on Position");	
			coorderror_panel = coorderror_collpanel.getContentPane();
			coorderror_panel.setBackground(LIGHTBACKGROUND);
			coorderror_panel.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();
			ButtonGroup bg = new ButtonGroup();
			errcoo_only_btn = new JRadioButton("only");
			errcoo_first_btn = new JRadioButton("first");
			errcoo_last_btn = new JRadioButton("last");
			errcoo_no_btn = new JRadioButton("no mapping");
			errcoo_unit = new JComboBox(new String[]{"deg", "arcsec", "arcmin", "mas"});
			errcoo_field = new AppendMappingTextField(this, 2, false, bg);
			errcoo_field.setColumns(15);

			ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 2;
			new MapperPrioritySelector(new JRadioButton[] {errcoo_only_btn, errcoo_first_btn, errcoo_last_btn, errcoo_no_btn}, errcoo_no_btn, bg
					, new JComponent[]{errcoo_unit, errcoo_field}
			    	, coorderror_panel, ccs);

			ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 1;
			coorderror_panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);
			
			ccs.gridx = 0; ccs.gridy = 1;ccs.weightx = 0;
			errcoo_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( MappingKWPanel.this.errcoo_no_btn.isSelected() ) {
						MappingKWPanel.this.errcoo_first_btn.setSelected(true) ;
					}
				}	
			});
			coorderror_panel.add(errcoo_field, ccs);
			
			ccs.gridx = 1; ccs.gridy = 1;ccs.weightx = 0;
			coorderror_panel.add(errcoo_unit, ccs);
			
			ccs.gridx = 2; ccs.gridy = 1;ccs.weightx = 1;
			coorderror_panel.add(AdminComponent.getHelpLabel(
					new String[] {
							"Give quoted constant values or keywords"
					  	  , "with the following format ERA[,EDEC[,EANGLE]]"
					  	  , "Keywords can (must) be dropped from the Data Sample window"
					})
					, ccs);

			editorPanel.add(coorderror_collpanel, globalGridConstraint);
		}

	}

	/**
	 * 
	 */
	private void addSpectCoordPanel() {
		if( spccoord_panel == null ) {
			CollapsiblePanel spccoord_collpanel = new CollapsiblePanel("Spectral Coordinate");	
			spccoord_panel = spccoord_collpanel.getContentPane();
			spccoord_panel.setBackground(LIGHTBACKGROUND);
			spccoord_panel.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();
//			Border b = BorderFactory.createTitledBorder("Spectral Coordinate");
//			b.
//			spccoord_panel.setBorder();
			ButtonGroup bg = new ButtonGroup();
			spec_only_btn = new JRadioButton("only");
			spec_first_btn = new JRadioButton("first");
			spec_last_btn = new JRadioButton("last");
			spec_no_btn = new JRadioButton("no mapping");
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
			spec_field = new ReplaceMappingTextField(this, 2, false, bg);
			spec_field.setColumns(15);


			ccs.gridx = 0; ccs.gridy = 0;ccs.weightx = 0;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 2;
			new MapperPrioritySelector(new JRadioButton[] {spec_only_btn, spec_first_btn, spec_last_btn, spec_no_btn}, spec_no_btn, bg
					, new JComponent[]{specunit_combo, spec_field}
					, spccoord_panel, ccs);

			ccs.gridx = 2; ccs.gridy = 0;ccs.weightx = 1;ccs.anchor = GridBagConstraints.LINE_START;ccs.gridwidth = 1;
			spccoord_panel.add(AdminComponent.getHelpLabel("Mapping priority Vs automatic detection"), ccs);
			
			ccs.gridx = 0; ccs.gridy = 1;ccs.weightx = 0;
			spec_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if( MappingKWPanel.this.spec_no_btn.isSelected() ) {
						MappingKWPanel.this.spec_first_btn.setSelected(true) ;
					}
				}	
			});
			spccoord_panel.add(spec_field, ccs);

			ccs.gridx = 1; ccs.gridy = 1;ccs.weightx = 0;
			spccoord_panel.add(specunit_combo, ccs);			
		
			ccs.gridx = 2; ccs.gridy = 1;ccs.weightx = 1;
			spccoord_panel.add(AdminComponent.getHelpLabel(
					new String[] {
							"Give a quoted range or the keyword representing"
						  , "the dispersion column (in case of table store)."
					  	  , "Keywords can (must) be dropped from the Data Sample window"
					})
					, ccs);
			
			editorPanel.add(spccoord_collpanel, globalGridConstraint);
		}
	}

	/**
	 * 
	 */
	private void addSourcePanel() {

		if( entry_panel == null ) {
			GridBagConstraints localGridConstraint = new GridBagConstraints();
			localGridConstraint.weightx = 1;			
			localGridConstraint.fill = GridBagConstraints.HORIZONTAL;
			localGridConstraint.anchor = GridBagConstraints.PAGE_START;
			localGridConstraint.gridx = 0;
			localGridConstraint.gridy = 0;

			entry_panel = new JPanel();
			entry_panel.setBackground(LIGHTBACKGROUND);
			entry_panel.setBorder(BorderFactory.createTitledBorder("Table Entry Mapping"));
			entry_panel.setLayout(new GridBagLayout());
			entry_panel.add(AdminComponent.getHelpLabel(
					new String[] {
							"The following parameters are related to the table entries"
							, "Requested Keywords must be searched in the table columns"})
					, localGridConstraint);
			localGridConstraint.gridy++;
					

			e_name_panel = new JPanel();	
			e_name_panel.setBackground(LIGHTBACKGROUND);
			e_name_compo = new AppendMappingTextField(this, 2, true, null);
			e_name_compo.setColumns(STRING_FIELD_NAME);
			e_name_panel.add(e_name_compo);
			e_name_panel.setBorder(BorderFactory.createTitledBorder("Entry Name"));
			entry_panel.add(e_name_panel, localGridConstraint);
			localGridConstraint.gridy++;
			
			editorPanel.add(entry_panel, globalGridConstraint);

		}
		if( e_ignored_panel == null ) {
			e_ignored_panel = new JPanel();	
			e_ignored_panel.setBackground(LIGHTBACKGROUND);
			e_ignored_compo = new AppendMappingTextField(this, 2, true, null);
			e_ignored_compo.setColumns(STRING_FIELD_NAME);
			e_ignored_panel.add(e_ignored_compo);
			e_ignored_panel.setBorder(BorderFactory.createTitledBorder("Ignored Keywords"));
			//c.gridx = 0;
			//c.gridy = 1;		
			editorPanel.add(e_ignored_panel, globalGridConstraint);
			globalGridConstraint.gridy++;
		}
		if( e_extatt_panel == null ) {
			e_extatt_panel = new JPanel();	
			e_extatt_panel.setBackground(LIGHTBACKGROUND);	
			e_extatt_panel.setBorder(BorderFactory.createTitledBorder("Extended Collection Attributes for Entries"));
			e_extatt_panel.setLayout(new GridBagLayout());
			this.addEAttExtendPanel();
			editorPanel.add(e_extatt_panel, globalGridConstraint);		
			globalGridConstraint.gridy++;
		}
		this.addCoordSysPanel();
		this.addPositionPanel();
		this.addErrorPositionPanel();
		/*
		 * Switch field control on entry mode: can only take KW in table columns
		 */
		coo_field.setForEntry();
		coosys_field.setForEntry();
		errcoo_field.setForEntry();
	}

	/**
	 * 
	 */
	public void addEAttExtendPanel() {
		e_ext_att = Database.getCachemeta().getAtt_extend_entry_names();

		GridBagConstraints cae = new GridBagConstraints();
		cae.anchor = GridBagConstraints.EAST;
		int numLabels = e_ext_att.length;
		e_extatt_fields = new ReplaceMappingTextField[numLabels];

		for (int i = 0; i < numLabels; i++) {
			cae.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
			cae.fill = GridBagConstraints.NONE;      //reset to default
			cae.weightx = 0.0;                       //reset to default
			e_extatt_panel.add(AdminComponent.getPlainLabel(e_ext_att[i]), cae);
			e_extatt_fields[i] = new ReplaceMappingTextField(this, 2, true, null);
			e_extatt_fields[i].setColumns(10);
			cae.gridwidth = GridBagConstraints.REMAINDER;     //end row
			cae.fill = GridBagConstraints.HORIZONTAL;
			cae.weightx = 1.0;
			e_extatt_panel.add(e_extatt_fields[i], cae);
		}
	}
	/**
	 * @return
	 */
	public ArgsParser getArgsParser() {
		//		if( this.conf_name.getText().equalsIgnoreCase("null")) {
		//			return null;
		//		}
		ArrayList<String> params = new ArrayList<String>();

		/*
		 * extension parameter
		 */
		if( extension_panel != null ) {
			if( extension_field.getText().length() > 0 ) {
				params.add("-extension=" + extension_field.getText());			
			}			
		}
		/*
		 * Class mapping parameter
		 */
		if( class_panel != null ) {
			if( class_field.getText().length() > 0 ) {
				if( classifier_btn.isSelected()  ) {
					params.add("-classifier=" + class_field.getText());							
				}
				else if( fusion_btn.isSelected()  ) {
					params.add("-classfusion=" + class_field.getText());											

				}
			}
			else if(classifier_btn.isSelected() || fusion_btn.isSelected()) {
				JOptionPane.showMessageDialog(rootFrame,
						"A class mapping mode is selected but there is no class name given: ignored",
						"Mapping warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
		/*
		 * ignored keyword parameters
		 */
		if( ignored_panel != null ) {
			if( ignored_compo.getText().length() > 0 ) {
				params.add("-ignore=" + ignored_compo.getText());			
			}			
		}
		/*
		 * ignored keyword parameters
		 */
		if( e_ignored_panel != null ) {
			if( e_ignored_compo.getText().length() > 0 ) {
				params.add("-eignore=" + e_ignored_compo.getText());			
			}			
		}
		/*
		 * Category parameter
		 */
		switch (this.category ) {
		case Category.MISC: params.add("-category=misc"); break;
		case Category.IMAGE: params.add("-category=image"); break;
		case Category.SPECTRUM: params.add("-category=spectrum"); break;
		case Category.TABLE: params.add("-category=table");break;
		case Category.FLATFILE: params.add("-category=flatfile"); break;
		default: break;
		}

		if( name_compo != null &&  name_compo.getText().length() > 0 ) {
			params.add("-name=" + name_compo.getText());			
		}

		if( ext_att != null ) {
			for( int i=0 ; i<ext_att.length ; i++ ) {
				if( extatt_fields[i].getText().length() > 0 ) {
					params.add("-ukw");			
					params.add(ext_att[i] + "=" + extatt_fields[i].getText());							
				}
			}
		}

		/*
		 * Coordinate System parameters
		 */
		if( coordsys_panel != null && !coosys_no_btn.isSelected() ) {
			if( coosys_first_btn.isSelected()) {
				params.add("-sysmapping=first");						
			}
			else if(  coosys_last_btn.isSelected()) {
				params.add("-sysmapping=last");						
			}
			else if( coosys_only_btn.isSelected()) {
				params.add("-sysmapping=only");						
			}
			if( coosys_field.getText().length() > 0 ) {
				params.add("-system=" + coosys_field.getText());			
			}
		}
		/*
		 * Coordinate  parameters
		 */
		if( coord_panel != null && !coo_no_btn.isSelected() ) {
			if( coo_first_btn.isSelected()) {
				params.add("-posmapping=first");						
			}
			else if( coo_last_btn.isSelected()) {
				params.add("-posmapping=last");						
			}
			else if( coo_only_btn.isSelected()) {
				params.add("-posmapping=only");						
			}
			if( coo_field.getText().length() > 0 ) {
				params.add("-position=" + coo_field.getText());			
			}
		}
		/*
		 * Coordinate  Error parameters
		 */
		if( coorderror_panel != null && !errcoo_no_btn.isSelected()) {
			if( errcoo_first_btn.isSelected()) {
				params.add("-poserrormapping=first");						
			}
			else if( errcoo_last_btn.isSelected()) {
				params.add("-poserrormapping=last");						
			}
			else if( errcoo_only_btn.isSelected()) {
				params.add("-poserrormapping=only");						
			}
			if( errcoo_field.getText().length() > 0 ) {
				params.add("-poserror=" + errcoo_field.getText());			
			}
			params.add("-poserrorunit=" + errcoo_unit.getSelectedItem().toString());			
		}

		/*
		 * Spectral coordinate
		 */
		if( spccoord_panel != null && !spec_no_btn.isSelected() ) {
			if( spec_first_btn.isSelected()) {
				params.add("-spcmapping=first");						
			}
			else if(  spec_last_btn.isSelected()) {
				params.add("-spcmapping=last");						
			}
			else if(  spec_only_btn.isSelected()) {
				params.add("-spcmapping=only");						
			}
			if(  spec_field.getText().length() > 0 ) {
				params.add("-spccolumn=" + spec_field.getText());			
			}
			String param = specunit_combo.getSelectedItem().toString().trim();
			if( param.length() > 0 ) {
				params.add("-spcunit=" + param);							
			}
		}
		/*
		 * Parameters specific for entries
		 */
		if( e_name_panel != null ) {
			if( e_name_compo.getText().length() > 0 ) {
				params.add("-ename=" + e_name_compo.getText());			
			}			
		}
		if( e_extatt_panel != null ) {
			e_ext_att = Database.getCachemeta().getAtt_extend_entry_names();
			for( int i=0 ; i<e_ext_att.length ; i++ ) {
				if( e_extatt_fields[i].getText().trim().length() > 0 ) {
					params.add("-eukw");			
					params.add(e_ext_att[i] + "=" + e_extatt_fields[i].getText());							
				}
			}			
		}
		ArgsParser retour;
		try {
			retour = new ArgsParser((String[])(params.toArray(new String[0])));
			retour.setName(confName);
			return retour;
		} catch (FatalException e) {
			Messenger.trapFatalException(e);
		}
		return null;
	}

	public void buildMiscPanel() {
		this.addClassPanel();
		globalGridConstraint.gridy++;

		this.addExtensionPanel();
		globalGridConstraint.gridy ++;
		this.addNamePanel();
		globalGridConstraint.gridy ++;
		this.addIgnoredPanel();
		globalGridConstraint.gridy ++;
		//this.removeExtensionPanel();
		//this.removeAttExtendPanel();
		//this.removeSourcePanel();
		this.addAttExtendPanel();
		globalGridConstraint.gridy ++;
		//this.removeCoordSysPanel();
		//this.removePositionPanel();
		//this.removeErrorPositionPanel();
		//this.removeSpectCoordPanel();
		this.updateUI();
	}
	public void buildFlatfilePanel() {
		System.out.println("FLATFILE");
		this.addNamePanel();
		globalGridConstraint.gridy ++;
		//this.removeIgnoredPanel();
		//this.removeExtensionPanel();
		//this.removeAttExtendPanel();
		//this.removeSourcePanel();
		this.addAttExtendPanel();
		//this.removeCoordSysPanel();
		//this.removePositionPanel();
		//this.removeErrorPositionPanel();
		//this.removeSpectCoordPanel();
		this.updateUI();	
	}
	public void buildImagePanel() {
		this.addClassPanel();
		globalGridConstraint.gridy++;
		this.addExtensionPanel();
		globalGridConstraint.gridy++;
		this.addNamePanel();
		globalGridConstraint.gridy++;
		this.addIgnoredPanel();
		globalGridConstraint.gridy++;
		//this.removeAttExtendPanel();
		//this.removeSpectCoordPanel();			
		//this.removeSourcePanel();
		//this.addAttExtendPanel();
		this.addCoordSysPanel();
		globalGridConstraint.gridy++;
		this.addPositionPanel();
		globalGridConstraint.gridy++;
		this.addErrorPositionPanel();
		this.updateUI();
	}
	public void buildSpectraPanel() {
		this.addClassPanel();
		globalGridConstraint.gridy++;
		this.addExtensionPanel();
		globalGridConstraint.gridy++;
		this.addNamePanel();
		globalGridConstraint.gridy++;
		this.addIgnoredPanel();
		globalGridConstraint.gridy++;
		//this.removeAttExtendPanel();
		//this.removeSourcePanel();
		//this.addAttExtendPanel();
		this.addCoordSysPanel();
		globalGridConstraint.gridy++;
		this.addPositionPanel();
		globalGridConstraint.gridy++;
		this.addErrorPositionPanel();
		globalGridConstraint.gridy++;
		this.addSpectCoordPanel();
		this.updateUI();
	}
	public void buildTablePanel() {
		this.addClassPanel();
		globalGridConstraint.gridy++;
		this.addExtensionPanel();
		globalGridConstraint.gridy++;
		this.addNamePanel();
		globalGridConstraint.gridy++;
		this.addIgnoredPanel();
		globalGridConstraint.gridy++;
		//this.removeAttExtendPanel();
		this.addAttExtendPanel();
		globalGridConstraint.gridy++;
		//this.removeCoordSysPanel();
		//this.removePositionPanel();
		//this.removeErrorPositionPanel();
		//this.removeSpectCoordPanel();
		this.addSourcePanel();
		this.updateUI();
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	/**
	 * @param parser
	 */
	public void loadConfig(ArgsParser parser)  {
		try {
			if( parser != null ) {
				this.last_saved = parser.toString();
				setConfName(parser.getName());
				String param=null;
				/*
				 * load class mapping
				 */
				if( class_panel != null ) {
					if( parser.getMappingType() == DefineType.TYPE_MAPPING_USER ){
						this.fusion_btn.setSelected(true);
						this.class_field.setText(parser.getClassName());
						this.class_field.setEnabled(true);
						this.class_field.setEditable(true);
					}
					else if( parser.getMappingType() == DefineType.TYPE_MAPPING_CLASSIFIER ){
						this.classifier_btn.setSelected(true);
						this.class_field.setText(parser.getClassName());
						this.class_field.setEnabled(true);
						this.class_field.setEditable(true);
					}
					else {
						this.noclass_btn.setSelected(true);
						this.class_field.setText("");
						this.class_field.setEnabled(false);
						this.class_field.setEditable(false);
					}
				}
				/*
				 * Load extension param
				 */
				if( extension_panel != null && (param = parser.getExtension()) != null) {
					this.extension_field.setText(param);
				}
				/*
				 * load ignored attributes
				 */
				if( ignored_panel != null  ) {
					ignored_compo.setText(getMergedComponent(parser.getIgnoredAttributes()));
				}
				/*
				 * load ignored attributes for entries
				 */
				if( e_ignored_panel != null ) {
					e_ignored_compo.setText(getMergedComponent(parser.getEntryIgnoredAttributes()));
				}
				/*
				 * Load name components
				 */
				if( name_panel != null ) {
					name_compo.setText(getMergedComponent(parser.getNameComponents()));			
				}
				/*
				 * Load name components for entries
				 */
				if( e_name_panel != null ) {
					e_name_compo.setText(getMergedComponent(parser.getEntryNameComponents()));			
				}
				/*
				 * load extended attributes
				 */
				if( extatt_panel != null ) {
					String[] ext_att = null;
					switch( this.category ) {
					case Category.MISC: ext_att = Database.getCachemeta().getAtt_extend_misc_names(); break;
					case Category.IMAGE: ext_att = Database.getCachemeta().getAtt_extend_image_names(); break;
					case Category.SPECTRUM: ext_att = Database.getCachemeta().getAtt_extend_spectrum_names(); break;
					case Category.TABLE: ext_att = Database.getCachemeta().getAtt_extend_table_names();break;
					case Category.FLATFILE: ext_att = Database.getCachemeta().getAtt_extend_flatfile_names(); break;
					}
					if( ext_att != null ) {
						param = null;
						for( int i=0 ; i<ext_att.length ; i++ ) {
							param = parser.getUserKeyword(ext_att[i]);
							if( param != null ) extatt_fields[i].setText(param);
							else extatt_fields[i].setText("");
						}
					}

				}
				/*
				 * load extended attributes for entries
				 */
				if( e_extatt_panel != null ) {
					String[] ext_att = null;
					ext_att = Database.getCachemeta().getAtt_extend_entry_names();
					if( ext_att != null ) {
						param = null;
						for( int i=0 ; i<ext_att.length ; i++ ) {
							param = parser.getEntryUserKeyword(ext_att[i]);
							if( param != null ) e_extatt_fields[i].setText(param);
							else e_extatt_fields[i].setText("");
						}
					}			
				}
				/*
				 * load coordinate system parameter
				 */
				if( coordsys_panel != null ) {
					coosys_field.setText("");
					param = parser.getSysMappingPriority();
					if( param == null ) {
						coosys_field.setEditable(false);									
						coosys_field.setEnabled(false);									
						coosys_no_btn.setSelected(true);		
					}
					else if( param.equalsIgnoreCase("first") ) {
						coosys_first_btn.setSelected(true);
						coosys_field.setEditable(true);									
						coosys_field.setEnabled(true);									
					}
					else if( param.equalsIgnoreCase("last") ) {
						coosys_last_btn.setSelected(true);
						coosys_field.setEditable(true);									
						coosys_field.setEnabled(true);									
					}
					else if( param.equalsIgnoreCase("only") ) {
						coosys_only_btn.setSelected(true);
						coosys_field.setEditable(true);									
						coosys_field.setEnabled(true);									
					}
					else {
						coosys_no_btn.setSelected(true);						
						coosys_field.setEditable(false);									
						coosys_field.setEnabled(false);									
					}
					if( param != null ) {
						coosys_field.setText(getMergedComponent(parser.getCoordinateSystem()));
					}
					for( int i=0 ; i<coosys_combo.getItemCount() ; i++ ) {
						if( coosys_combo.getItemAt(i).toString().equalsIgnoreCase(param) ) {
							coosys_combo.setSelectedIndex(i);
						}
					}
				}
				/*
				 * Load position  parameters
				 */
				if( coord_panel != null ) {
					coo_field.setText("");
					param = parser.getPositionMappingPriority();
					if( param == null ) {
						coo_no_btn.setSelected(true);		
						coo_field.setEditable(false);									
						coo_field.setEnabled(false);									
					}
					else if( param.equalsIgnoreCase("first") ) {
						coo_first_btn.setSelected(true);
						coo_field.setEditable(true);									
						coo_field.setEnabled(true);									
					}
					else if( param.equalsIgnoreCase("last") ) {
						coo_last_btn.setSelected(true);
						coo_field.setEnabled(true);									
						coo_field.setEditable(true);									
					}
					else if( param.equalsIgnoreCase("only") ) {
						coo_only_btn.setSelected(true);
						coo_field.setEditable(true);									
						coo_field.setEnabled(true);									
					}
					else {
						coo_no_btn.setSelected(true);						
						coo_field.setEditable(false);									
						coo_field.setEnabled(false);									
					}
					if( param != null ) {
						coo_field.setText(getMergedComponent(parser.getPositionMapping()));	
					}
				}	
				/*
				 * Load position errors parameters
				 */
				if( coorderror_panel != null ) {
					errcoo_field.setText("");
					param = parser.getPoserrorMappingPriority();
					if( param == null ) {
						errcoo_no_btn.setSelected(true);												
						errcoo_field.setEditable(false);									
						errcoo_field.setEnabled(false);									
					}
					else if( param.equalsIgnoreCase("first") ) {
						errcoo_first_btn.setSelected(true);
						errcoo_field.setEditable(true);									
						errcoo_field.setEnabled(true);									
					}
					else if( param.equalsIgnoreCase("last") ) {
						errcoo_last_btn.setSelected(true);
						errcoo_field.setEditable(true);									
						errcoo_field.setEnabled(true);									
					}
					else if( param.equalsIgnoreCase("only") ) {
						errcoo_only_btn.setSelected(true);
						errcoo_field.setEditable(true);									
						errcoo_field.setEnabled(true);									
					}
					else {
						errcoo_no_btn.setSelected(true);						
						errcoo_field.setEditable(false);									
						errcoo_field.setEnabled(false);									
					}
					if( param != null ) {
						errcoo_field.setText(getMergedComponent(parser.getPoserrorMapping()));	
					}
					param = parser.getPoserrorUnit();
					for( int i=0 ; i<errcoo_unit.getItemCount() ; i++ ) {
						if( errcoo_unit.getItemAt(i).toString().equalsIgnoreCase(param) ) {
							errcoo_unit.setSelectedIndex(i);
						}
					}
				}	
				/*
				 * Load spectral coordinate parameters
				 */
				if( spccoord_panel != null ) {
					spec_field.setText("");	
					param = parser.getSpectralMappingPriority();
					if( param == null ) {
						spec_no_btn.setSelected(true);												
						spec_field.setEditable(false);									
						spec_field.setEnabled(false);									
					}
					else if( param.equalsIgnoreCase("first") ) {
						spec_first_btn.setSelected(true);
						spec_field.setEditable(true);									
						spec_field.setEnabled(true);									
						specunit_combo.setEnabled(true);
					}
					else if( param.equalsIgnoreCase("last") ) {
						spec_last_btn.setSelected(true);
						spec_field.setEditable(true);									
						spec_field.setEnabled(true);									
						specunit_combo.setEnabled(true);
					}
					else if( param.equalsIgnoreCase("only") ) {
						spec_only_btn.setSelected(true);
						spec_field.setEditable(true);									
						spec_field.setEnabled(true);									
						specunit_combo.setEnabled(true);
					}
					else {
						spec_no_btn.setSelected(true);						
						spec_field.setEditable(false);									
						spec_field.setEnabled(false);									
					}
					param = parser.getSpectralColumn();									
					if( param != null ) {
						spec_field.setText(param);	
					}
					param = parser.getSpectralUnit();									
					for( int i=0 ; i<specunit_combo.getItemCount() ; i++ ) {
						if( specunit_combo.getItemAt(i).toString().equalsIgnoreCase(param) ) {
							specunit_combo.setSelectedIndex(i);
						}
					}
				}
			}				
			else {
				setConfName(null);
			}

		} catch (Exception e) {
			Messenger.printStackTrace(e);
			JOptionPane.showMessageDialog(rootFrame,
					e.toString(),
					"Error while loading configuration",
					JOptionPane.ERROR_MESSAGE);
		}

	}					

	/**
	 * @param name
	 */
	protected void setConfName(String name) {
		confName = name;
		setSelectedResource(confName, null);
	}

	/**
	 * Clear all widgets
	 */
	public void reset(boolean keep_ext) {	
		this.last_saved = "";
		this.confName = null;
		if( name_compo != null ) name_compo.setText("");
		if( e_name_compo != null ) e_name_compo.setText("");

		if( ignored_compo != null ) ignored_compo.setText("");
		if( e_ignored_compo != null ) e_name_compo.setText("");

		if( extatt_fields != null ){
			for( int i=0 ; i<extatt_fields.length ; i++) 
				extatt_fields[i].setText("");
		}
		if( e_extatt_fields != null ){
			for( int i=0 ; i<extatt_fields.length ; i++) 
				e_extatt_fields[i].setText("");
		}

		if( coosys_no_btn != null ) coosys_no_btn.setSelected(true);
		if( coosys_field != null ) coosys_field.setText("");

		if( coo_no_btn != null ) coo_no_btn.setSelected(true);
		if( coo_field != null ) coo_field.setText("");

		if( errcoo_no_btn != null ) errcoo_no_btn.setSelected(true);
		if( errcoo_field != null ) errcoo_field.setText("");

		if( spec_no_btn != null ) spec_no_btn.setSelected(true);
		if( spec_field != null ) spec_field.setText("");

		if( !keep_ext && extension_field != null ) extension_field.setText("");

		if( noclass_btn != null )  noclass_btn.setSelected(true);
		if( class_field != null )  class_field.setText("");
	}
	/**
	 * @param param_compo
	 * @return
	 */
	private static final String getMergedComponent(String[] param_compo) {
		String param = "";
		if( param_compo != null ) {
			for( int i=0 ; i<param_compo.length; i++ ) {
				if( i >= 1) param += ",";
				param += param_compo[i];
			}
		}
		return param;
	}


	/*
	 * Reset the form if the extension number has been changed
	 */
	protected void checkExtensionChange() {
		if( extension_field != null && extension_field.checkChange(extension_field.getText()) ) {
			extension_field.setPrevious_value(extension_field.getText());
		}
	}

	/**
	 * 
	 */
	protected void saveAs() {
		try {
			ArgsParser ap = this.getArgsParser();

			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			String name = ap.getClassName();
			if( name == null || name.length() == 0 || name.equalsIgnoreCase("null")) {
				name = "NewConfig";
			}
			while( 1 == 1 ) {
				DialogConfName dial = new DialogConfName(rootFrame, "Configuration Name", name);
				dial.pack();
				dial.setLocationRelativeTo(rootFrame);
				dial.setVisible(true);
				String prefix = null;
				prefix = Category.explain(this.category);
				name = dial.getTyped_name();
				if( name == null ) {
					return;
				}
				else if( name.equalsIgnoreCase("null") ) {
					AdminComponent.showFatalError(rootFrame, "Wrong config name.");
				}
				else {
					String filename = SaadaDB.getRoot_dir() 
					+ Database.getSepar() + "config" 
					+ Database.getSepar() + prefix + "." + name + ".config";
					if( (new File(filename)).exists() 
							&& AdminComponent.showConfirmDialog(rootFrame
									, "Loader configuration <" + name + "> for \"" + prefix + "\" already exists.\nOverwrite it?") == false ) {
						ap.setName(name);
					}
					else {
						ap.setName(name);
						this.setConfName(name);
						fos = new FileOutputStream(SaadaDB.getRoot_dir() 
								+ Database.getSepar() + "config" 
								+ Database.getSepar() + prefix + "." + name + ".config");
						out = new ObjectOutputStream(fos);
						out.writeObject(ap);
						out.close();
						return;
					}
				}
			}
		} catch(Exception ex) {
			Messenger.printStackTrace(ex);
			AdminComponent.showFatalError(this, ex);
			return;
		}
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
				loadConfig(ap);
			} catch(Exception ex) {
				Messenger.printStackTrace(ex);
				showFatalError(rootFrame, ex);
				return;
			}				
		}
	}

	/**
	 * 
	 */
	public void save() {
		if( "Default".equals(confName) || confName.equals("") || confName.equalsIgnoreCase("null") ) {
			this.saveAs();
		}
		else if( !this.hasChanged() ){
			//SaadaDBAdmin.showFatalError(this.frame, "Already saved " + this.last_saved + " " + this.getArgsParser().toString());
			return;
		}
		else {
			ArgsParser ap = this.getArgsParser();
			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try {
				String prefix = Category.explain(this.category);
				fos = new FileOutputStream(SaadaDB.getRoot_dir() 
						+ Database.getSepar() + "config" 
						+ Database.getSepar() + prefix + "." + confName + ".config");
				out = new ObjectOutputStream(fos);
				out.writeObject(ap);
				out.close();
				this.last_saved  = ap.toString();

			} catch(Exception ex) {
				Messenger.printStackTrace(ex);
				AdminComponent.showFatalError(this, ex);
				return;
			}
		}
	}

	/**
	 * @return
	 */
	public boolean hasChanged() {
		return !last_saved.equals(this.getArgsParser().toString());
	}

	/**
	 * Does a basic param checking. Basically, parameter subject to a mapping must not be empty
	 * @return
	 */
	public boolean checkParams() {
		String msg = "";

		if( class_panel != null ) {
			if( (classifier_btn.isSelected() || fusion_btn.isSelected()) ) {
				if( this.class_field.getText().length() == 0 ) {
					msg += "<LI>Empty class name not allowed in this classification mode</LI>";
				}

				else if( !this.class_field.getText().matches(RegExp.CLASSNAME)) {
					msg += "<LI>Bad class name</LI>";
				}
			}
		}			
		if( coordsys_panel != null ) {
			if( (coosys_first_btn.isSelected() || coosys_last_btn.isSelected() || coosys_only_btn.isSelected()) ) {
				if( this.coosys_field.getText().length() == 0 ) {
					msg += "<LI>Empty coord. system  not allowed in this mapping mode</LI>";
				}
			}
		}
		if( coord_panel != null ) {
			if( (coo_first_btn.isSelected() || coo_last_btn.isSelected() || coo_only_btn.isSelected()) ) {
				if( this.coo_field.getText().length() == 0 ) {
					msg += "<LI>Empty coordinates not allowed in this mapping mode</LI>";
				}
			}
		}
		if( coorderror_panel != null ) {
			if( (errcoo_first_btn.isSelected() || errcoo_last_btn.isSelected() || errcoo_only_btn.isSelected()) ) {
				if( this.errcoo_field.getText().length() == 0 ) {
					msg += "<LI>Empty coordinate error not allowed in this mapping mode</LI>";
				}
			}
		}
		if( spccoord_panel != null ) {
			if( (spec_first_btn.isSelected() || spec_last_btn.isSelected() || spec_only_btn.isSelected()) ) {
				if( this.spec_field.getText().length() == 0 ) {
					msg += "<LI>Empty spectral coordinates not allowed in this mapping mode</LI>";
				}
			}
		}
		if( msg.length() > 0 ) {
			AdminComponent.showInputError(rootFrame, "<HTML><UL>" + msg);
			return false;
		}
		else {
			return true;
		}

	}
	@Override
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setActivePanel()
	 */
	@Override
	protected void setActivePanel() {
		globalGridConstraint = new GridBagConstraints();
		globalGridConstraint.weightx = 1;			
		globalGridConstraint.fill = GridBagConstraints.HORIZONTAL;
		globalGridConstraint.anchor = GridBagConstraints.PAGE_START;
		globalGridConstraint.gridx = 0;
		globalGridConstraint.gridy = 0;

		GridBagConstraints localGridConstraint = new GridBagConstraints();
		localGridConstraint.weightx = 1;			
		localGridConstraint.weighty = 1;			
		localGridConstraint.fill = GridBagConstraints.BOTH;
		localGridConstraint.anchor = GridBagConstraints.NORTH;
		localGridConstraint.gridx = 0;
		localGridConstraint.gridy = 0;

		JPanel tPanel = this.addSubPanel("Filter Editor");
		editorPanel = new JPanel( );
		editorPanel.setBackground(LIGHTBACKGROUND);
		editorPanel.setLayout(new GridBagLayout());

		this.addCategoryPanel();
		globalGridConstraint.gridy++;

		/*
		 * Category set here because this code is executed by a super class creator
		 * before the category parameter is used
		 */
		if( this.title.equals(MISC_MAPPER) ){
			category = Category.MISC;
		}
		if( this.title.equals(FLATFILE_MAPPER) ){
			category = Category.FLATFILE;
		}
		if( this.title.equals(IMAGE_MAPPER) ){
			category = Category.IMAGE;
		}
		if( this.title.equals(SPECTRUM_MAPPER) ){
			category = Category.SPECTRUM;
		}
		if( this.title.equals(TABLE_MAPPER) ){
			category = Category.TABLE;
		}
		switch( this.category ) {
		case Category.MISC: this.buildMiscPanel(); break;
		case Category.IMAGE: this.buildImagePanel(); break;
		case Category.SPECTRUM: this.buildSpectraPanel(); break;
		case Category.TABLE: this.buildTablePanel();break;
		case Category.FLATFILE: this.buildFlatfilePanel(); break;
		}
		tPanel.add(new JScrollPane(editorPanel), localGridConstraint);

		this.setActionBar();
		this.setConfName("Default");

		loadButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				DialogConfigFileChooser dial = new DialogConfigFileChooser(category);
				String conf_path = dial.open(rootFrame);
				loadConfFile(conf_path);
			}
		});
		saveButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				save();				
			}
		});
		saveAsButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent arg0) {
				saveAs();				
			}
		});


	}

	// TODO Auto-generated method stub

	//	}

	@Override
	public void active() {
		// TODO Auto-generated method stub

	}

}