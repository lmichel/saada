package saadadb.admintool.panels.tasks;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.correlator.CollectionCoverage;
import saadadb.admintool.components.correlator.KNNEditor;
import saadadb.admintool.components.correlator.KeywordConditionEditor;
import saadadb.admintool.components.correlator.RelationChooser;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.configuration.RelationConf;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.util.Messenger;

public class RelationPopulatePanel extends TaskPanel {
	/** * @version $Id: MappingRelationPanel.java 118 2012-01-06 14:33:51Z laurent.mistahl $

	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected LinkedHashMap<String, QualifierSetter> qual_setter = new LinkedHashMap<String, QualifierSetter>();

	private RelationChooser relationChooser;
	private CollectionCoverage collectionCoverage;
	private KNNEditor knnEditor;
	private KeywordConditionEditor keywordConditionEditor;
	private RunTaskButton runButton;
	private RelationConf relation;


	public RelationPopulatePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, POPULATE_RELATION, null, ancestor);
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
		//cmdThread = new ThreadRelationPopulate(rootFrame, POPULATE_RELATION);
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		super.setDataTreePath(dataTreePath);
		if( dataTreePath != null && (dataTreePath.isCategoryLevel() || dataTreePath.isClassLevel()) ) {
			try {
				setSelectedResource("No selected resource", null);
				relationChooser.setDataTreePath(dataTreePath);
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
		}
	}

	@Override
	protected Map<String, Object> getParamMap() {
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();

//		String name = this.nameField.getText();
//		if( SQLTable.tableExist(name)  ) {
//			AdminComponent.showFatalError(rootFrame, "The name <" + name + "> is already used");
//			return retour;
//		}
//		else if( !name.matches(RegExp.CLASSNAME) ) {
//			AdminComponent.showFatalError(rootFrame, "Relation name <" + name + "> badly formed");			
//			return retour;
//		}
//		else if( primaryField.getText().trim().length() == 0 ) {
//			AdminComponent.showInfo(rootFrame, "No primary collection given");						
//			return retour;
//		}
//		else if( secondaryField.getText().trim().length() == 0 ) {
//			AdminComponent.showFatalError(rootFrame, "No secondary collection given");						
//			return retour;
//		}
//		else {
//			try {
//				RelationConf relationConfiguration = new RelationConf();
//				String[] collcat = primaryField.getText().trim().split("\\.");
//				relationConfiguration.setNameRelation(name);
//				relationConfiguration.setDescription(this.commentField.getText().trim());
//				relationConfiguration.setColPrimary_name(collcat[0]);
//				relationConfiguration.setColPrimary_type(Category.getCategory(collcat[1]));
//				collcat = secondaryField.getText().trim().split("\\.");
//
//				relationConfiguration.setColSecondary_name(collcat[0]);
//				relationConfiguration.setColSecondary_type(Category.getCategory(collcat[1]));
//				relationConfiguration.setClass_name(name);	
//				for( int i=0 ; i<this.qualList.getItemCount() ; i++ ) {
//					relationConfiguration.setQualifier(qualList.getItemAt(i).toString(), "double");
//				}
//				retour.put("config", relationConfiguration);
//				relationConfiguration.save();
//				return retour;
//			} catch(Exception e) {
//				showFatalError(rootFrame, e);
//			}
//		}
		return retour;
	}

	/**
	 * @param prefix
	 */
	public void updateAvailableAttributes(String prefix) {
		String[] classes=null;;
		String text=null;
		JComboBox combo = null;
		int cat;
		if( this.relation == null ) {
			return;
		}
		/*
		 * Get pointers on primary or secondary widgets
		 */
		if( prefix.equalsIgnoreCase("p.") ) {
			text = collectionCoverage.getPrimaryCLasses();
			combo = primary_att;
			combo.removeAllItems();
			combo.addItem("- Primary Attributes -");
			cat = this.relation.getColPrimary_type();
		}
		else if( prefix.equalsIgnoreCase("s.") ) {
			text = collectionCoverage.getSecondaryCLasses();
			combo = secondary_att;
			combo.removeAllItems();
			combo.addItem("- Secondary Attributes -");
			cat = this.relation.getColSecondary_type();
		}
		else {
			return;
		}
		/*
		 * insert collection attributes
		 */
		classes = text.split("\\s*,\\s*");
		for( AttributeHandler ah: MetaCollection.getAttribute_handlers(cat).values() ) {
			combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
		}
		/*
		 * insert class attributes
		 */
		if( classes.length == 1 && !classes[0].trim().equals("*") ) {
			try {
				MetaClass mcl = Database.getCachemeta().getClass(classes[0]);
				for( AttributeHandler ah: mcl.getAttributes_handlers().values() ) {
					combo.addItem(prefix + ah.getNameattr() + " (" + ah.getType() + ")");
				}
			} catch (SaadaException e) {
				Messenger.printStackTrace(e);
				return;
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, true, true, false));
	}


	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);
		JPanel tPanel = this.addSubPanel("QQQQQ");
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new GridBagLayout());
		editorPanel.setBackground(LIGHTBACKGROUND);
		
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		
		relationChooser = new RelationChooser(this, runButton);
		editorPanel.add(relationChooser, emc);

		collectionCoverage = new CollectionCoverage(this, runButton);
		emc.newRow();
		editorPanel.add(collectionCoverage, emc);
		
		emc.newRow();
		knnEditor = new KNNEditor(this, null);
		editorPanel.add(knnEditor, emc);
		
		emc.newRow();
		keywordConditionEditor = new KeywordConditionEditor(this, null);
		editorPanel.add(keywordConditionEditor, emc);

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(editorPanel), imcep);

		
		this.setActionBar(new Component[]{runButton
				, debugButton});
	}
	
	/**
	 * @author michel
	 *
	 */
	class QualifierSetter {
		JTextArea editor;
		JLabel    label;
		
		QualifierSetter(String qual_name) {
			label = getPlainLabel("Qualifier : <" + qual_name + ">");
			editor = new JTextArea(1, 20);					
			editor.setBorder(BorderFactory.createLineBorder(Color.BLACK, 0));					

		}
	}



}
