package saadadb.admin.dmmapper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.TitledBorder;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.dialogs.SelectClass;
import saadadb.admin.tree.VoClassTree;
import saadadb.admin.widgets.DMAttributeTextField;
import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.DMImplementer;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;

public class ClassToDatamodelMapper extends JFrame{

	VoClassTree class_att_tree;
	JPanel dmpanel, buttonpanel;
	JScrollPane scrollPane;
	JSplitPane splitPane;
	MetaClass mc;
	MetaCollection mcoll;
	SaadaInstance dm_impl;
	String[] classes;
	VOResource vor;
	ArrayList<DMAttEditor> field_editors;



	/** * @version $Id$

	 * @param data_model
	 * @param classe
	 * @throws Exception
	 */
	public ClassToDatamodelMapper(String data_model, String collection, int category ) throws Exception {
		vor = Database.getCachemeta().getVOResource(data_model);
		if( vor == null ) {
			IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "VO resource <" + data_model + "> not found");
		}
		mc = null;
		mcoll = Database.getCachemeta().getCollection(collection);
		classes = Database.getCachemeta().getClassesOfCollection(collection, category);
		setTitle("Map DM <" + data_model + "> on " + Category.explain(category) + " classes of collection " + collection);
		this.connectToClass();
		this.setDMPanel();
		this.setClassTree();
		this.init();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				setVisible(false);			
			}
		});
	}

	/**
	 * @throws Exception
	 */
	private void connectToClass() throws Exception {
		if( mc != null ) {
			dm_impl = (SaadaInstance)  SaadaClassReloader.forGeneratedName(mc.getName()).newInstance();			
			dm_impl.activateDataModel(vor.getName());
		}		
	}
	/**
	 * 
	 */
	private void init() {
		this.setResizable(false);
		this.setLocationRelativeTo(this.getParent());
		scrollPane = new JScrollPane(dmpanel);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, class_att_tree);	
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(700);
		splitPane.setPreferredSize(new Dimension(900, 500));
		this.setButtonPanel();

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1; c.weighty = 1;
		c.gridx = 0;c.gridy = 0;
		this.add(splitPane, c);	
		c.gridy++;
		this.add(buttonpanel, c);
		this.pack();
		this.setVisible(true);
	}


	/**
	 * 
	 */
	private void setButtonPanel() {
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LinkedHashMap<String, String> mapping = new LinkedHashMap <String, String>();
				for( DMAttEditor dmae: field_editors) {
					mapping.put(dmae.uth.getNickname(), dmae.getMappingText());
				}
				try {
					vor.saveClassMapping(mc.getName(), mapping);
					DMImplementer dmi= new DMImplementer(vor.getMappingFilepath(mc.getName()));
					dmi.putDMInJavaClass(Database.getClassLocation());
				} catch (Exception e1) {
					SaadaDBAdmin.showFatalError(ClassToDatamodelMapper.this, e1);
				}
			}
		});

		JButton check = new JButton("Check");
		check.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for( DMAttEditor dmae: field_editors) {
					dmae.checkContent(false);
				}
			}
		});

		JButton load = new JButton("Load a Class");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {		
				SelectClass sc = new SelectClass(ClassToDatamodelMapper.this, classes);
				String c = sc.getTyped_name();
				if( c!= null ) {
					ClassToDatamodelMapper.this.selectClass(c);
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
		this.buttonpanel.add(save);
		this.buttonpanel.add(check);
		this.buttonpanel.add(load);
		this.buttonpanel.add(cancel);		
	}

	/**
	 * 
	 */
	public void selectClass(String c) {
		if( c!= null ) {
			try {
				this.mc = Database.getCachemeta().getClass(c);
				dm_impl = (SaadaInstance)  SaadaClassReloader.forGeneratedName(mc.getName()).newInstance();
				try {
					dm_impl.activateDataModel(vor.getName());
				} catch(FatalException ef) {
					Messenger.printMsg(Messenger.TRACE, "Datamodel " + vor.getName() + " not implemented yet in class " + this.mc.getName());
				}
				for( DMAttEditor dmae: field_editors) {
					dmae.setMetaClass(mc);
				}
				setClassTree();
				if( splitPane != null ) {
					int dl = splitPane.getDividerLocation();
					splitPane.setRightComponent(class_att_tree);
					splitPane.setDividerLocation(dl);
					splitPane.repaint();
				}

			} catch (Exception e1) {
				SaadaDBAdmin.showFatalError(this, e1);
			}
		}
	}
	/**
	 * @throws FatalException 
	 * 
	 */
	private void setDMPanel() throws FatalException {
		this.dmpanel = new JPanel();
		field_editors = new ArrayList<DMAttEditor>();
		int size=0;
		for( String g: vor.getGroups().keySet() ) {
			size += vor.getGroups().get(g).size();
		}
		dmpanel.setLayout(new GridLayout(size, 1));
		for(UTypeHandler uth: vor.getUTypeHandlers() ) {
			DMAttEditor dmae = new DMAttEditor(mc, uth);
			field_editors.add(dmae);
			dmpanel.add(dmae);
		}	

	}

	/**
	 * @throws Exception
	 */
	private void setClassTree() throws Exception {
		if( mc != null ) {
			class_att_tree = new VoClassTree((Window) this.getParent(), mc);
			class_att_tree.buildTree(new Dimension(300, 500));
			class_att_tree.setPreferredSize(new Dimension(300, 500));		
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
		protected DMAttributeTextField mapper;

		DMAttEditor(MetaClass mc, UTypeHandler uth) throws FatalException {
			this.setLayout(new GridBagLayout());
			this.uth = uth;
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = 1; c.weighty = 1;
			c.gridx = 0;c.gridy = 0;c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("utype: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(SaadaDBAdmin.getPlainLabel(uth.getUtype()), c);

			c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("ucd: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(SaadaDBAdmin.getPlainLabel(uth.getUcd()), c);

			c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("type: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			int as = uth.getArraysize();
			String size = "";
			if( as == -1 ) {
				size = " (*)";
			}
			else if( as != 1 ) {
				size = " (" + as+ ")";				
			}
			this.add(SaadaDBAdmin.getPlainLabel(uth.getType() + size), c);

			c.gridx = 0;c.gridy++; c.anchor = GridBagConstraints.LINE_END;
			this.add(new JLabel("desc: "), c);
			c.gridx++;c.anchor = GridBagConstraints.LINE_START;
			this.add(SaadaDBAdmin.getPlainLabel(uth.getComment()), c);

			c.gridx = 0;c.gridy++; c.gridwidth = 1;
			this.add(check_button, c);

			this.mapper = new DMAttributeTextField(mc, "");
			this.mapper.setColumns(32);
			if( dm_impl != null ) {
				String str = dm_impl.getSQLField(uth.getUtypeOrNickname());
				if( !"'null'".equals(str))
					this.mapper.setText(str);
			}
			else {
				this.mapper.setText("");
			}
			c.gridx++;c.fill = GridBagConstraints.HORIZONTAL;
			this.add(mapper, c);

			c.gridx++;
			this.add(SaadaDBAdmin.getPlainLabel(uth.getUnit()), c);

			TitledBorder title;
			switch( uth.getRequ_level()) {
			case UTypeHandler.MANDATORY: 				
				title = BorderFactory.createTitledBorder(uth.getNickname() + " (MAN)");
				title.setTitleColor(Color.BLACK);
				break;
			case UTypeHandler.RECOMMENDED: 				
				title = BorderFactory.createTitledBorder(uth.getNickname() + " (REC)");
				title.setTitleColor(Color.darkGray);
				break;
			default:
				title = BorderFactory.createTitledBorder(uth.getNickname() + " (OPT)");
				title.setTitleColor(Color.gray);
			}
			this.setBorder(title);
			this.setBackground(SaadaDBAdmin.beige_color);

			check_button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					checkContent(true);
				}

			});
		}	

		public void setMetaClass(MetaClass mc) throws FatalException {					
			mapper.setText("");
			mapper.setMetaClass(mc);
			if( dm_impl != null ) {
				try {
					String str = dm_impl.getSQLField(uth.getUtypeOrNickname());
					if( !"'null'".equals(str)) {
						this.mapper.setText(str);
					}
					else {
						this.mapper.setText("");
					}
				} catch(FatalException e){}
			}
			else {
				this.mapper.setText("");
			}
		}
		/**
		 * @return
		 */
		public String getMappingText() {
			String mt = mapper.getText().trim();
			if( mt.length() == 0 ) {
				mt = "'null'";
			}
			if( mt.startsWith("'") ){
				if( uth.getType().equals("char")) {
					return mt;
				}
				/*
				 * Remove quotes for numerics
				 */
				else {
					return mt.replaceAll("'", "");														
				}
			}
			else if( mt.startsWith("\"") ){
				if( uth.getType().equals("char")) {
					return mt.replaceAll("\"", "'");		
				}
				/*
				 * Remove quotes for numerics
				 */
				else {
					return mt.replaceAll("\"", "");														
				}
			}
			else {
				String mtt = mt.trim();
				if( mtt.startsWith("(") && mtt.endsWith(")") ) {
					return mtt;
				}
				else {
					return "(" + mtt + ")";
				}
			}		
		}

		public boolean checkContent(boolean with_dialog) {
			try {
				if( mapper.getText().trim().length() == 0 ) {
					mapper.setBackground(Color.WHITE);
					return true;								
				}
				String mt = getMappingText();
				SQLQuery squery = new SQLQuery();
				if( squery.run("Select " + mt + " from " + mc.getName()+  ", " + Database.getCachemeta().getCollectionTableName(mcoll.getName(), mc.getCategory()) + "  limit 1") != null ) {
					mapper.setBackground(Color.GREEN);
					squery.close();
					return true;			
				}
				else {
					SaadaDBAdmin.showInputError(this.getParent(), "Expression <" + mapper.getText() + "> can not be computed in SQL on table " + mc.getName());
					squery.close();
					mapper.setForeground(Color.RED);
				}
			} catch (Exception e) {
				mapper.setBackground(Color.RED);
				if( with_dialog)SaadaDBAdmin.showInputError(this.getParent(), e.toString());
			}
			return false;
		}

	}

	public static void main(String[] args ) throws Exception {

		Messenger.debug_mode = false;
		Database.init("XIDResult");
		new ClassToDatamodelMapper("XIDSrcModel", "SpectroscopicSample", Category.ENTRY);
		//		SaadaInstance dm_impl = (SaadaInstance) Class.forName("generated." + Database.getDbname() + ".GPS_WFIEntry" ) .newInstance();
		//		try {
		//			System.out.println("-----------");
		//			dm_impl.activateDataModel("WFSrcModel");
		//			System.out.println("-----------");
		//		} catch(FatalException ef) {
		//			Messenger.printMsg(Messenger.TRACE, "Datamodel  not implemented yet in class " );
		//		}
		//		Messenger.debug_mode = true;
		//ClassToDatamodelMapper ctdm = new ClassToDatamodelMapper("MonModele", "UCDTester1", Category.ENTRY);
	}

}
