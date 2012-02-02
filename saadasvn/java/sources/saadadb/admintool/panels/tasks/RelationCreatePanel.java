package saadadb.admintool.panels.tasks;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadCreateCollection;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.CollectionTextField;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class RelationCreatePanel extends TaskPanel {
	/** * @version $Id: MappingRelationPanel.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GridBagConstraints c = new GridBagConstraints();
	protected JTextField        att_name        = new JTextField(10);
	protected JComboBox         att_list        = new JComboBox();		
	protected JButton           qual_add        = new JButton("Add");
	protected JButton           qual_del        = new JButton("Remove");
	protected JPanel rel_name_panel;
	protected JPanel coll_panel;
	protected JPanel qual_panel;

	protected FreeTextField commentField;
	protected RunTaskButton runButton;
	protected NodeNameTextField nameField ;
	protected NodeNameTextField qualifField ;
	private CollectionTextField primaryField;
	private CollectionTextField secondaryField;


	public RelationCreatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, CREATE_RELATION, null, ancestor);
	}

//	
//	public RelationCreatePanel(RelationConfPanel panel)  {
//		super();
//		c.gridwidth = GridBagConstraints.REMAINDER;     
//		c.anchor = GridBagConstraints.NORTH;
//		c.fill =GridBagConstraints.BOTH	;
//		c.weightx = c.weighty = 0.5;
//		this.frame = panel;
//		this.setLayout(new GridBagLayout());
//		/*
//		 * 
//		 */
//		rel_name_panel = new JPanel();			
//		rel_name_panel.setLayout(new GridBagLayout());
//		GridBagConstraints cl = new GridBagConstraints();
//		//cl.weightx = cl.weighty = 0.5;
//		rel_name_panel.setBackground(SaadaDBAdmin.beige_color);
//		rel_name_panel.setBorder(BorderFactory.createTitledBorder("Relation Name"));
//		cl.gridx = 0 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_END;
//		rel_name_panel.add(SaadaDBAdmin.getPlainLabel("Relation "), cl);		
//		cl.gridx = 1 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_START;
//		rel_name_panel.add(name_field, cl);		
//		cl.gridx = 0 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_END;
//		rel_name_panel.add(SaadaDBAdmin.getPlainLabel("Description "), cl);	
//		cl.gridx = 0 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_END;
//		rel_name_panel.add(save_desc, cl);	
//		description.setBorder(new LineBorder(Color.black));
//		cl.gridx = 1 ; cl.gridy = 1;; cl.gridheight = 2; cl.anchor = GridBagConstraints.LINE_START;
//		rel_name_panel.add(description, cl);		
//		c.weightx = 0;
//		this.add(rel_name_panel, c);
//		save_desc.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				if( e.getButton() == 1 ) {
//					String rel_name = name_field.getText();
//					if( rel_name.length() == 0 || "No relation loaded yet".equals(rel_name)) {
//						SaadaDBAdmin.showFatalError(frame, "No relation name given");					
//					}
//					else {
//						try {
//							saveDescription();						
//						} catch (Exception e1) {
//							Messenger.printStackTrace(e1);
//							SaadaDBAdmin.showFatalError(frame, e1.getMessage());
//							return;
//						}
//						SaadaDBAdmin.showInfo(frame, "Description saved");					
//					}
//				}
//			}			
//		});
//		/*
//		 * 
//		 */
//		coll_panel = new JPanel();			
//		coll_panel.setBackground(SaadaDBAdmin.beige_color);
//		coll_panel.setLayout(new GridBagLayout());
//		cl = new GridBagConstraints();
//		coll_panel.setBorder(BorderFactory.createTitledBorder("Collections"));
//		cl.gridx = 0 ; cl.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
//		coll_panel.add(SaadaDBAdmin.getPlainLabel("Primary"), cl);
//		cl.gridx = 1 ; cl.gridy = 0; c.anchor = GridBagConstraints.LINE_START;
//		coll_panel.add(primary_field, cl);
//		cl.gridx = 0 ; cl.gridy = 1; c.anchor = GridBagConstraints.LINE_END;
//		coll_panel.add(SaadaDBAdmin.getPlainLabel("Secondary"), cl);
//		cl.gridx = 1 ; cl.gridy = 1;c.anchor = GridBagConstraints.LINE_START;
//		coll_panel.add(secondary_field, cl);
//		c.gridy = 1;
//		primary_field.setToolTipText("Drag&drop the category node (IMAGE ...) of the primary collection from the data tree");
//		secondary_field.setToolTipText("Drag&drop the category node (IMAGE ...) of the secondary collection from the data tree");
//		this.add(coll_panel, c);
//		
//		/*
//		 * 
//		 */
//		qual_panel = new JPanel();			
//		qual_panel.setBackground(SaadaDBAdmin.beige_color);
//		qual_panel.setLayout(new GridBagLayout());
//		cl = new GridBagConstraints();
//		qual_panel.setBorder(BorderFactory.createTitledBorder("Qualifiers"));
//		cl.fill  = GridBagConstraints.HORIZONTAL;
//		cl.insets = new Insets(5,5,5,5);
//
//		cl.gridx = 0; cl.gridy = 0;	
//		cl.gridwidth = 1;
//		cl.fill  = GridBagConstraints.NONE;
//		qual_panel.add(SaadaDBAdmin.getPlainLabel("Qualifier Name."), cl);
//		cl.gridx = 1; cl.gridy = 0;	
//		cl.fill  = GridBagConstraints.NONE;
//		att_name.setToolTipText("Write here the name of the new qualifier.");
//		qual_panel.add(att_name, cl);
//		cl.gridx = 2; cl.gridy = 0;	
//		cl.anchor = GridBagConstraints.LINE_START;
//		cl.fill  = GridBagConstraints.NONE;
//		qual_add.addActionListener(new ActionListener() {
//			public void actionPerformed(@SuppressWarnings("unused") ActionEvent arg0) {
//				String name = att_name.getText().trim();
//				for( int i=0 ; i<RelationCreatePanel.this.att_list.getItemCount() ;  i++ ) {
//					if( RelationCreatePanel.this.att_list.getItemAt(i).toString().equals(name)) {
//						JOptionPane.showMessageDialog(frame,
//								"Duplicate qualifier <" + name + "> ",
//								"Configuration Error",
//								JOptionPane.ERROR_MESSAGE);					
//						return ;
//					}
//				}
//				if( name.matches(RegExp.EXTATTRIBUTE) ) {
//					RelationCreatePanel.this.att_list.addItem(name) ;
//
//				}
//				else {
//					JOptionPane.showMessageDialog(frame,
//							"Qualifier name <" + name + "> badly formed",
//							"Configuration Error",
//							JOptionPane.ERROR_MESSAGE);					
//				}
//			}
//		});
//		qual_add.setToolTipText("Add the new qualifier to the relationship.");
//		qual_panel.add(qual_add, cl);
//		cl.gridwidth = 2;
//		cl.gridy = 1; cl.gridx = 0;
//		cl.anchor = GridBagConstraints.LINE_END;
//		cl.fill  = GridBagConstraints.NONE;
//		att_list.setToolTipText("Qualifier list of the relationship");
//		qual_panel.add(att_list, cl);
//		qual_del.addActionListener(new ActionListener() {
//			public void actionPerformed(@SuppressWarnings("unused") ActionEvent arg0) {
//				RelationCreatePanel.this.att_list.removeItem(RelationCreatePanel.this.att_list.getSelectedItem());
//			}
//		});
//		qual_del.setToolTipText("Remove the selected qualfier from the relationship");
//		cl.gridwidth = 1;
//		cl.gridy = 1; cl.gridx = 2;
//		cl.fill  = GridBagConstraints.NONE;
//		qual_panel.add(qual_del, cl);
//		
//		c.gridy = 2;
//		this.add(qual_panel, c);
//		/*
//		 * 
//		 */
//		query_panel = new CorrQueryEditor();			
//		c.gridy = 3;
//		this.add(query_panel, c);
//		this.setEditable(false);
//	}
//	
//	public void paintInGray() {
//		rel_name_panel.setBackground(SaadaDBAdmin.gray_color);
//		coll_panel.setBackground(SaadaDBAdmin.gray_color);
//		qual_panel.setBackground(SaadaDBAdmin.gray_color);
//	}
//	
//	public void paintInBeige() {
//		rel_name_panel.setBackground(SaadaDBAdmin.beige_color);
//		coll_panel.setBackground(SaadaDBAdmin.beige_color);
//		qual_panel.setBackground(SaadaDBAdmin.beige_color);
//	}
//	
//	/**
//	 * @param editable
//	 */
//	public void setEditable(boolean editable) {
//		primary_field.setEditable(editable);
//		secondary_field .setEditable(editable);
//		att_name.setEditable(editable);
//		att_list.setEnabled(editable);		
//		qual_add.setEnabled(editable);
//		qual_del.setEnabled(editable);
//		query_panel.setEditable(editable);	
//	}
//	/**
//	 * @param relation
//	 * @throws SaadaException 
//	 */
//	public void reset()  {
//		this.name_field.setText("");
//		this.primary_field.setText("");
//		this.secondary_field.setText("");
//		this.att_list.removeAllItems();
//		this.query_panel.reset();
//	}
//
//	/**
//	 * @param conf
//	 * @throws SaadaException 
//	 */
//	public void load(RelationConf conf) throws SaadaException {
//		this.name_field.setText(conf.getNameRelation());
//		this.description.setText(conf.getDescription());
//		this.primary_field.setText(conf.getColPrimary_name() + "." +  Category.explain(conf.getColPrimary_type()));
//		this.secondary_field.setText(conf.getColSecondary_name() + "." +  Category.explain(conf.getColSecondary_type()));
//		Set<String> quals = conf.getQualifier().keySet();
//		this.att_list.removeAllItems();
//		for( String qualifier: quals) {
//			this.att_list.addItem(qualifier);
//		}
//		this.query_panel.load(conf);
//		if( (conf.getColPrimary_type() == Category.ENTRY || conf.getColPrimary_type() == Category.IMAGE || conf.getColPrimary_type() == Category.SPECTRUM) &&  
//			(conf.getColSecondary_type() == Category.ENTRY || conf.getColSecondary_type() == Category.IMAGE || conf.getColSecondary_type() == Category.SPECTRUM) ) {
//			this.query_panel.activateKNN(true);
//		}
//		else {
//			this.query_panel.activateKNN(false);			
//		}
//	}
//
//	/**
//	 * @param b 
//	 * @throws Exception 
//	 */
//	public RelationConf getConfig() throws Exception {
//		String name = this.getName();
//		if( Database.getCachemeta().getRelation(name) != null  ) {
//			SaadaDBAdmin.showFatalError(this.frame, "Relationship <" + name + "> already exists");
//			return null;
//		}
//		else if( !name.matches(RegExp.CLASSNAME) ) {
//			SaadaDBAdmin.showFatalError(this.frame, "Relation name <" + name + "> badly formed");			
//			return null;
//		}
//		else if( primary_field.getText().trim().length() == 0 ) {
//			SaadaDBAdmin.showFatalError(this.frame, "No primary collection given");						
//			return null;
//		}
//		else if( secondary_field.getText().trim().length() == 0 ) {
//			SaadaDBAdmin.showFatalError(this.frame, "No secondary collection given");						
//			return null;
//		}
//		else {
//			RelationConf relationConfiguration = new RelationConf();
//			String[] collcat = primary_field.getText().trim().split("\\.");
//			relationConfiguration.setNameRelation(name);
//			relationConfiguration.setDescription(this.description.getText());
//			relationConfiguration.setColPrimary_name(collcat[0]);
//			relationConfiguration.setColPrimary_type(Category.getCategory(collcat[1]));
//			collcat = secondary_field.getText().trim().split("\\.");
//			
//			relationConfiguration.setColSecondary_name(collcat[0]);
//			relationConfiguration.setColSecondary_type(Category.getCategory(collcat[1]));
//			relationConfiguration.setClass_name(name);	
//			for( int i=0 ; i<att_list.getItemCount() ; i++ ) {
//				relationConfiguration.setQualifier(att_list.getItemAt(i).toString(), "double");
//			}
//			relationConfiguration.setQuery(this.query_panel.getCorrelator().trim());	
//			return relationConfiguration;
//		}
//	}
//	
//	/**
//	 * Save the edited correlateor query in the configuration file.
//	 * @throws Exception
//	 */
//	public void saveCorrelator()  throws Exception {
//		String name = this.getName();
//		MetaRelation mr = Database.getCachemeta().getRelation(name);
//		if( mr == null  ) {
//			SaadaDBAdmin.showFatalError(this.frame, "Relationship <" + name + "> does not exist exists into the DB");
//			return;
//		}
//		else {
//			/*
//			 * Copy the meta relation int the configuration ....
//			 */
//			RelationConf relationConfiguration = new RelationConf();
//
//			relationConfiguration.setNameRelation(name);
//			relationConfiguration.setDescription(mr.getDescription());
//			relationConfiguration.setColPrimary_name(mr.getPrimary_coll());
//			relationConfiguration.setColPrimary_type(mr.getPrimary_category());
//			
//			relationConfiguration.setColSecondary_name(mr.getSecondary_coll());
//			relationConfiguration.setColSecondary_type(mr.getSecondary_category());
//			relationConfiguration.setClass_name(name);	
//			for( String q: mr.getQualifier_names()) {
//				relationConfiguration.setQualifier(q, "double");
//			}
//			/*
//			 * ... except the correlator query which is read from the panel
//			 */
//			relationConfiguration.setQuery(this.query_panel.getCorrelator().trim());
//			/*
//			 * TODO No longer use relation.xml
//			 */
////			relationConfiguration.remove();
////			relationConfiguration.save();
//			Table_Saada_Relation.saveCorrelator(relationConfiguration);
//		}
//	}
//	
//	/**
//	 * Save the edited description query in the configuration file.
//	 * @throws Exception
//	 */
//	public void saveDescription()  throws Exception {
//		String name = this.getName();
//		MetaRelation mr = Database.getCachemeta().getRelation(name);
//		if( mr == null  ) {
//			SaadaDBAdmin.showFatalError(this.frame, "Relationship <" + name + "> does not exist exists into the DB");
//			return;
//		}
//		else {
//			/*
//			 * Copy the meta relation int the configuration ....
//			 */
//			RelationConf relationConfiguration = new RelationConf();
//
//			relationConfiguration.setNameRelation(name);
//			/*
//			 * ... except the description
//			 */
//			relationConfiguration.setDescription(this.description.getText());
//			relationConfiguration.setColPrimary_name(mr.getPrimary_coll());
//			relationConfiguration.setColPrimary_type(mr.getPrimary_category());
//			
//			relationConfiguration.setColSecondary_name(mr.getSecondary_coll());
//			relationConfiguration.setColSecondary_type(mr.getSecondary_category());
//			relationConfiguration.setClass_name(name);	
//			for( String q: mr.getQualifier_names()) {
//				relationConfiguration.setQualifier(q, "double");
//			}
//			relationConfiguration.setQuery(mr.getCorrelator());	
//			/*
//			 * TODO No longer use relation.xml
//			 */
////			relationConfiguration.remove();
////			relationConfiguration.save();
//			Table_Saada_Relation.saveDescription(relationConfiguration);
//		}
//	}



	@Override
	public void initCmdThread() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected Map<String, Object> getParamMap() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void setActivePanel() {
		JPanel tPanel;
		MyGBC mc = new MyGBC(5,5,5,5);
		
		tPanel = this.addSubPanel("Relationship");
		mc.right();
		tPanel.add(getPlainLabel("Relation Name"), mc);
		
		mc.next();mc.left();
		try {
			nameField = new NodeNameTextField(16, "^" + RegExp.COLLNAME + "$", runButton);
		} catch (ParseException e) {
			AdminComponent.showFatalError(rootFrame, e);
		}
		tPanel.add(nameField, mc);
		mc.rowEnd();
		tPanel.add(getHelpLabel(HelpDesk.NODE_NAME), mc);
		
		mc.newRow();mc.right();
		tPanel.add(getPlainLabel("Description"), mc);
		mc.next();mc.left();
		commentField = new FreeTextField(6, 24);
		tPanel.add(new JScrollPane(commentField), mc);

		
		tPanel = this.addSubPanel("Primary Collection");
		c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.EAST;
		primaryField = new CollectionTextField();
		tPanel.add(primaryField, c);

		tPanel = this.addSubPanel("Secondary Collection");
		c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		c.gridx = 0;
		c.gridy = 0;	
		c.weightx = 0;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.EAST;
		secondaryField = new CollectionTextField();
		tPanel.add(secondaryField, c);
		
		tPanel = this.addSubPanel("Qualifiers");
		
		runButton = new RunTaskButton(this);
		this.setActionBar(new Component[]{runButton
				, debugButton
				, (new AntButton(this))});
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));
	}



}
