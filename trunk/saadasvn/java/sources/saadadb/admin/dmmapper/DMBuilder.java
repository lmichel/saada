package saadadb.admin.dmmapper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.DialogDMFileChooser;
import saadadb.admin.dialogs.NameInput;
import saadadb.admin.widgets.CollectionTextField;
import saadadb.admin.widgets.DMAttributeTextField;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.Table_Saada_VO_Resources;
import saadadb.unit.Unit;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class DMBuilder extends JFrame{
	
	JPanel dmpanel, buttonpanel;
	JScrollPane scrollPane;
	String[] classes;
	VOResource vor;
	ArrayList<DMAGroupEditor> group_editors;
	GridBagConstraints c = new GridBagConstraints();
	private JButton new_group = new JButton("New Goup");
	String model_name="";
	private boolean dmvalid = false;;

	
	/**
	 * @param data_model
	 * @param classe
	 * @throws Exception
	 */
	public DMBuilder() throws Exception {
		this.setDMPanel();
		this.init();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent we){	        					
	        	setVisible(false);
	        }
		});
	}
	
	/**
	 * 
	 */
	private void init() {
		scrollPane = new JScrollPane(dmpanel);
		scrollPane.setPreferredSize(new Dimension(700, 500));
		this.setLocationRelativeTo(this.getParent());
		this.setButtonPanel();
		new_group.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addGroup();
				} catch (FatalException e1) {
				}
			}
		});
		
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1;
		c.gridx = 0;c.gridy = 0;
		this.add(scrollPane, c);	
		c.gridy++;
		this.add(buttonpanel, c);
		setTitle();
		this.pack();
		this.setVisible(true);
	}
	
	
	/**
	 * 
	 */
	private void setButtonPanel() {
		JButton newdm = new JButton("New");
		newdm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NameInput ni = new NameInput(DMBuilder.this, "Group Name");
				String dm_name = ni.getTyped_name();
				if( dm_name != null && dm_name.length() > 0 ) {	
					if( dm_name.matches(RegExp.COLLNAME)) {
						model_name = dm_name;
						resetGroups();
						c.weightx = 0; c.weighty = 0;
						c.gridx = 0;c.gridy=0;
						dmpanel.add(new_group, c);	
						setTitle();
						pack();
						repaint();
					}
					else {
						SaadaDBAdmin.showInputError(DMBuilder.this, "Bad Data Model Name [\\w_]");
					}
				}
			}
		});
		
		JButton check = new JButton("Check");
		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				check();
			}
		});
		JButton votree = new JButton("Vocabulary Panel");
		votree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//new VoTreeFrame(DMBuilder.this, DMBuilder.this.new_group );
			}
		});
		JButton load = new JButton("Edit a Model");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DialogDMFileChooser dial = new DialogDMFileChooser();
				String conf_path = dial.open(DMBuilder.this);
				if( conf_path.length() > 0 ) {
					try {
						resetGroups();
						loadDataModel(new VOResource(conf_path));
					} catch (Exception e1) {
						SaadaDBAdmin.showFatalError(DMBuilder.this, e1);
					}
				}
			}
		});
		
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( DMBuilder.this.check() ) {
					LinkedHashMap<String, Set<UTypeHandler>> groups = new LinkedHashMap<String, Set<UTypeHandler>>();
					for( DMAGroupEditor dge: group_editors) {
						groups.put(dge.name, dge.getUtypeHandlers());
					}
					VOResource vor = new VOResource(model_name, groups);
					try {
						String filename = Database.getRoot_dir()  + Database.getSepar() + "config" +  Database.getSepar() + "vodm." + model_name + ".xml";
						vor.saveInFile(filename);
						if( SaadaDBAdmin.showConfirmDialog(DMBuilder.this, "Do you want to store it into tye SaadaDB?") ) {
							Table_Saada_VO_Resources.loadFromConfigFile(filename);
							SaadaDBAdmin.showSuccess(DMBuilder.this, "Data model <" + model_name  + "> Ready to be used");
						}
						else {
							SaadaDBAdmin.showSuccess(DMBuilder.this, "Model saved in " + filename);
						}
					} catch (Exception e1) {
						SaadaDBAdmin.showFatalError(DMBuilder.this, e1);
					}
				}
			}
		});
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		this.buttonpanel = new JPanel();
		this.buttonpanel.add(newdm);
		this.buttonpanel.add(check);
		this.buttonpanel.add(votree);
		this.buttonpanel.add(load);
		this.buttonpanel.add(save);
		this.buttonpanel.add(cancel);		
	}
	
	/**
	 * @return
	 */
	private boolean check() {
		this.dmvalid  = true;
		for( DMAGroupEditor dge: group_editors ) {
			if( dge.check() == false ) {
				this.dmvalid = false;
			}
		}
		if( !dmvalid ) {
			SaadaDBAdmin.showInputError(this, "The model cannot be saved while fields have errors.");
		}
		return this.dmvalid;
	}
	
	/**
	 * 
	 */
	private void setTitle(){
		if( model_name.length() > 0 ) {
			this.setTitle("Data Model \"" + this.model_name + "\"");
		}
		else {
			this.setTitle("No Data Model");
		}
	}
	private void loadDataModel(VOResource vor) throws FatalException {
		resetGroups();		
		model_name = vor.getName();
		setTitle();
		Map<String, Set<UTypeHandler>> map = vor.getGroups();
		for( String group: map.keySet()) {
			addGroup(group, map.get(group));
		}
	}

	private void resetGroups() {
		for(DMAGroupEditor dge: group_editors ) {
			dmpanel.remove(dge);
		}
		group_editors = new ArrayList<DMAGroupEditor> ();
	}
	/**
	 * @throws FatalException 
	 * 
	 */
	private void setDMPanel() throws FatalException {
		this.dmpanel = new JPanel();
		group_editors = new ArrayList<DMAGroupEditor>();
		dmpanel.setLayout(new GridBagLayout());
		c.weightx = 0; c.weighty = 0;
		c.gridx = 0;c.gridy=0;
		}
	
	private void addGroup(String name, Set<UTypeHandler> fields) throws FatalException {
		if( name != null && name.length() > 0 ) {
			c.gridx = 0;c.gridy++;
			DMAGroupEditor dmae = new DMAGroupEditor(name, fields);
			group_editors.add(dmae);
			dmpanel.add(dmae, c);	
			dmpanel.remove(new_group);
			c.gridx = 0;c.gridy++;c.anchor = GridBagConstraints.FIRST_LINE_START;
			dmpanel.add(new_group, c);	
			this.pack();
			dmpanel.repaint();
			scrollPane.repaint();
		}
	}
	private void addGroup() throws FatalException {
		NameInput ni = new NameInput(this, "Group Name") ;
		String name = ni.getTyped_name();
		addGroup(name, null);

	}
	private void removeGroup(DMAGroupEditor ge) {
		for( DMAGroupEditor dge: group_editors) {
			dmpanel.remove(dge);
		}
		dmpanel.remove(new_group);
		group_editors.remove(ge);
		c.gridx = 0;c.gridy = 0;
		for( DMAGroupEditor dge: group_editors) {
			dmpanel.remove(dge);
			c.gridy++;
		}
		dmpanel.add(new_group, c);	
		this.pack();
		dmpanel.repaint();
		scrollPane.repaint();		
	}
	
	/**
	 * 
	 * Inner class managing the mapping of one group of fields
	 * @author michel
	 *
	 */
	class DMAGroupEditor extends JPanel {
		ArrayList<DMAttEditor> fields_editors = new ArrayList<DMAttEditor>();
		protected JButton new_field = new JButton("Add New Field");
		protected JButton remove_group = new JButton("Remove Group");
		GridBagConstraints c = new GridBagConstraints();
		private String name;
		
		DMAGroupEditor(String name, Set<UTypeHandler> fields) throws FatalException {
			this.setLayout(new GridBagLayout());
			this.name = name;
			c.weightx = 0.0; c.weighty = 1;c.anchor = GridBagConstraints.FIRST_LINE_START;
			c.gridx=0;c.gridy=0;c.gridwidth=2;
			if( fields == null ) {
				DMAttEditor dmae = new DMAttEditor(this, null);
				fields_editors.add(dmae);
				this.add(dmae, c);	
			}
			else {
				for( UTypeHandler u: fields) {
					DMAttEditor dmae = new DMAttEditor(this, u);
					fields_editors.add(dmae);
					this.add(dmae, c);	
					c.gridy++;
				}
			}
			this.addButtons();
			Border blackline = BorderFactory.createLineBorder(Color.black);
			TitledBorder title = BorderFactory.createTitledBorder(blackline, "Group " + name);
			this.setBorder(title);
			new_field.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						addField();
					} catch (FatalException e1) {
					}
				}
			});
			remove_group.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DMBuilder.this.removeGroup(DMAGroupEditor.this);
				}
			});
		}		
		
		public Set<UTypeHandler> getUtypeHandlers() {
			LinkedHashSet<UTypeHandler> retour = new LinkedHashSet<UTypeHandler>();
			for( DMAttEditor dae: fields_editors) {
				retour.add(dae.getUTypeHandler());
			}
			return retour;
		}

		private boolean check() {
			boolean retour = true;
			for( DMAttEditor dge: fields_editors) {
				if( !dge.checkContent(false)) {
					retour = false;
				}
			}
			return retour;
		}
		private void addButtons() {
			c.gridx=0;c.gridy++;c.gridwidth=1;
			this.add(new_field, c);		
			c.gridx++;
			this.add(remove_group, c);	
			c.gridx=0;c.gridwidth=2;			
			
		}
		private void addField() throws FatalException {
			this.remove(new_field);
			this.remove(remove_group);
			c.gridy++;
			DMAttEditor dmae = new DMAttEditor(this, null);
			fields_editors.add(dmae);
			this.add(dmae, c);	
			addButtons();
			this.repaint();
			DMBuilder.this.pack();
			DMBuilder.this.repaint();
		}
		private void removeField(DMAttEditor ge) {
			if( fields_editors.size() == 1 ) {
				DMBuilder.this.removeGroup(this);
			}
			else {
				for( DMAttEditor dge: fields_editors) {
					this.remove(dge);
				}
				this.remove(new_field);
				group_editors.remove(ge);
				c.gridx = 0;c.gridy = 0;
				for( DMAttEditor dge: fields_editors) {
					this.remove(dge);
					c.gridy++;
				}
				this.add(new_field, c);	
				if( fields_editors.size() == 0 ) {
					DMBuilder.this.removeGroup(this);
				}
				DMBuilder.this.pack();
			}
		}
	}

	/**
	 * 
	 * Inner class managing the mapping of one field
	 * @author michel
	 *
	 */
	class DMAttEditor extends JPanel {
		protected UTypeHandler uth;
		protected String mapping;
		protected JButton check_button = new JButton("Check");
		protected JButton remove_button = new JButton("Remove Field");
		protected DMAttributeTextField mapper;
		private CollectionTextField utype_edit = new CollectionTextField();
		private CollectionTextField ucd_edit = new CollectionTextField();
		private CollectionTextField desc_edit = new CollectionTextField();
		private CollectionTextField unit_edit = new CollectionTextField();
		private DMAGroupEditor ge;
		private JComboBox rec_choice = new JComboBox(new String[]{"Mandatory", "Recommended", "Optional"});
		private JComboBox type_choice = new JComboBox(new String[]{"double", "int", "char(*)"});
		
		DMAttEditor(DMAGroupEditor ge, UTypeHandler u) {
			check_button.setBackground(SaadaDBAdmin.beige_color);
			remove_button.setBackground(SaadaDBAdmin.beige_color);
			this.ge = ge;
			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = 1; c.weighty = 1;
			c.gridx = 0;c.gridy=0; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("utype: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(utype_edit, c);
			
			c.gridx++;c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("Req. Level: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(rec_choice, c);

			c.gridx++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("type: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(type_choice, c);


			c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("ucd: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(ucd_edit, c);
			
			c.gridx++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("unit: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			unit_edit.setColumns(12);
			this.add(unit_edit, c);
			
			c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("description: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;c.gridwidth = 6;
			desc_edit.setColumns(55);
			this.add(desc_edit, c);

			
			c.gridx = 0;c.gridy++; c.gridwidth = 1;
			this.add(check_button, c);
			c.gridx++;
			this.add(remove_button, c);
			
			TitledBorder title;
			title = BorderFactory.createTitledBorder("new Field");
			title.setTitleColor(Color.gray);
			this.setBorder(title);
			this.setBackground(SaadaDBAdmin.beige_color);
			check_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkContent(true);
				}				
			});
			remove_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					DMAttEditor.this.ge.removeField(DMAttEditor.this);
				}
			});
			if( u != null ) {
				setUtypeHandler(u);
			}
		}	

		/**
		 * @param u
		 */
		private void setUtypeHandler(UTypeHandler u) {
			TitledBorder title = (TitledBorder) this.getBorder();
			title.setTitle("Field: " + u.getNickname());
			utype_edit.setText(u.getUtype());
			ucd_edit.setText(u.getUcd());
			desc_edit.setText(u.getComment());
			unit_edit.setText(u.getUnit());
			switch( u.getRequ_level() ) {
			case UTypeHandler.MANDATORY: rec_choice.setSelectedIndex(0); break;
			case UTypeHandler.RECOMMENDED: rec_choice.setSelectedIndex(1); break;
			default: rec_choice.setSelectedIndex(2); break;
			}
			String type = u.getType();
			int as = u.getArraysize();
			String dt;
			if( as == 1 ) {
				dt = type;
			}
			else if( as == -1 ) {
				dt = type + "(*)";
			}
			else {
				dt = type + "(" + as + ")";
				
			}
			int i;
			for( i=0 ; i<type_choice.getItemCount() ; i++ ) {
				if( type_choice.getItemAt(i).equals(dt) ) {
					type_choice.setSelectedIndex(i);
					break;
				}
			}
			if( i == type_choice.getItemCount() ) {
				type_choice.addItem(dt);
				type_choice.setSelectedIndex(i);
			}
		}
		/**
		 * @param with_dialog
		 * @return
		 */
		public boolean checkContent(boolean with_dialog) {
			String message = "";
			boolean retour = true;
			String utype = utype_edit.getText().trim();
			if(  utype.length() > 0 && !utype.matches(RegExp.UTYPE) ) {
				message += "UTYPE not valid\n";
				retour = false;
			}
			String ucd  = ucd_edit.getText().trim();
			if( ucd.length() > 0 && !ucd.matches(RegExp.UCD) ) {
				message += "UCD not valid\n";
				retour = false;
			}					
			if( ucd.length() == 0 && utype.length() == 0 ) {				
				message += "One of UTYPE or UCD must be set at least\n";
				retour = false;
			}
			String unit  = unit_edit.getText().trim();
			if( unit.length() > 0  ) {
				try {
					(new Unit(unit)).convertFromStr(new Unit(unit));
				} catch (Exception e) {
					message += e.getMessage();
					retour = false;
				}
			}

			if( !retour ) {
				if( with_dialog ) {
					SaadaDBAdmin.showFatalError(this.getParent(), message);
				}
				setRedBorder();
			}
			else {
				setGreenBorder(getUTypeHandler());
			}
			return retour;
		}
		/**
		 * @return
		 */
		private UTypeHandler getUTypeHandler() {
			String[] type = type_choice.getSelectedItem().toString().replace("(", " ").replace(")", " ").split(" ");
			String at = type[0];
			int arraysize = 1;
			if( type.length > 1 ) {
				if( type[1].equals("*") ) {
					arraysize = -1;
				}
				else {
					arraysize = Integer.parseInt(type[1]);
				}
			}
			return new UTypeHandler(null, utype_edit.getText().trim()
					, ucd_edit.getText().trim()
					, rec_choice.getSelectedItem().toString().toUpperCase()
					, at
					, arraysize
					, unit_edit.getText().trim()
					, desc_edit.getText().trim());
		}
		/**
		 * 
		 */
		private void setRedBorder() {
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.red)
					, ((TitledBorder)(this.getBorder())).getTitle());
			this.setBorder(title);
		}
		/**
		 * @param u
		 */
		private void setGreenBorder(UTypeHandler u) {
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.green)
					, "Field: " + u.getNickname());
			this.setBorder(title);
		}
		/**
		 * 
		 */
		private void setBlackBorder() {
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black)
					, ((TitledBorder)(this.getBorder())).getTitle());
			this.setBorder(title);
		}
	}
	public static void main(String[] args ) throws Exception {
		
		Messenger.debug_mode = false;
		Database.init("BENCH1_5_1_MSQL");
        Database.getConnector().setAdminMode(null);
		//new ClassToDatamodelMapper("SSA default", "UCDTester1", Category.ENTRY);
		Messenger.debug_mode = true;
		DMBuilder ctdm = new DMBuilder();
	}

}

