package saadadb.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import saadadb.admin.dialogs.AdminPassword;
import saadadb.admin.mapping.JDataLoaderConfPanel;
import saadadb.admin.popup.VoTreeFrame;
import saadadb.admin.relation.RelationConfPanel;
import saadadb.admin.threads.CmdDeployWebApp;
import saadadb.admin.threads.CmdThread;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;


public class SaadaDBAdmin  extends JFrame {
	/** * @version $Id$

	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected MetaDataPanel meta_data_tree;
	protected JTabbedPane onglets;
	protected JPanel product_panel;
	protected JPanel class_panel;
	private SQLJTable class_table;
	private SQLJTable product_table;
	protected JDataLoaderConfPanel dlconf_panel;
	protected RelationConfPanel relconf_panel;
	protected DBInstallPanel dbinstall_panel;
	public static final int heigh = 500;

	public static final Font plain_font = new Font("Helvetica",Font.PLAIN,14);
	public static final Color beige_color = new Color(255, 255, 240);
	public static final Color gray_color = Color.LIGHT_GRAY;
	private final String log_file ;
	public static String current_dir = System.getProperty("user.home");
	private boolean first_call = true;


	/**
	 * @param p 
	 * @throws SaadaException
	 */
	public SaadaDBAdmin(boolean nolog, Point p) throws SaadaException {
		super("Saada " + Database.version() + ": " + Database.getDbname() + " Admintool");
		this.setResizable(true);
		this.setLayout(new GridBagLayout());		
		/*
		 * Make sure to close and rename the log file when exit
		 */
		Date date = new Date();
		String sdate = DateFormat.getDateInstance(DateFormat.SHORT,Locale.FRANCE).format(date);
		sdate += date.toString().substring(10,19);
		log_file  = Repository.getLogsPath() 
		+  Database.getSepar() + "SaadaAdmin." + sdate.replaceAll("[ :\\/]", "_") + ".log";
		Messenger.setGraphicMode(this);
		if( !nolog ) {
			Messenger.openLog(log_file);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					Messenger.closeLog();
					System.out.println("Log saved in " + log_file);
				}
			});
		}

		/*
		 * Exit after confirmation when click on the window close button
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent we){	        	
				if( SaadaDBAdmin.showConfirmDialog(SaadaDBAdmin.this, "Do you really want to exit?") ==  true ) {
					System.exit(1);
				}
			}
		});

		GridBagConstraints c = new GridBagConstraints();
		/*
		 * meta_data tree is always visible on the left pbutton_panelart of the panel
		 */
		meta_data_tree = new MetaDataPanel(this, 250, heigh);
		c.gridx = 0;
		c.gridy = 0;	
		c.fill  = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
		//this.getContentPane().add(meta_data_tree, c);
		/*
		 * Build the right tab panel
		 */
		buildOnglets();
		activateOnglets();
		c.gridx = 0;
		c.gridy = 0;		
		//this.add(onglets, c);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				meta_data_tree, onglets);		
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(250);
		this.add(splitPane, c);
		/*
		/*
		 * Process buttom buttons
		 */
		JPanel button_panel = new JPanel();
		JButton webapp_btn;
		webapp_btn = new JButton("Deploy Web App.");
		button_panel.setLayout(new FlowLayout());
		button_panel.add(webapp_btn);
		webapp_btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				CmdThread ct = new CmdDeployWebApp(SaadaDBAdmin.this);
				ct.start();		
			}
		});
		JButton debug_btn;
		if( Messenger.debug_mode )
			debug_btn = new JButton("Set Debug OFF");
		else {
			debug_btn = new JButton("Set Debug ON");						    
			/* + " where  name_coll = '" + tree_path_components[1] + "' order by pk";*/

		}
		button_panel.add(debug_btn);
		debug_btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				if( Messenger.debug_mode )  {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Debug mode switched off");
					Messenger.debug_mode = false;
					((JButton)(e.getSource())).setText("Set Debug ON");
				}
				else {
					Messenger.debug_mode = true;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Debug mode switched on");
					((JButton)(e.getSource())).setText("Set Debug OFF");
				}
			}
		});
		/* + " where  name_coll = '" + tree_path_components[1] + "' order by pk";*/

		JButton meta_btn;
		meta_btn = new JButton("Reload meta data");
		meta_btn.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				try {
					Database.getCachemeta().reload(true);
				} catch (FatalException e1) {
					SaadaDBAdmin.showFatalError(null, e1);
				}	
				
			}
		});
		button_panel.add(meta_btn);

		
		
//		JButton console_btn = new JButton("Show Console");
//		console_btn.setEnabled(false);
//		button_panel.add(console_btn);
//		JButton help_btn = new JButton("Help");
//		help_btn.setEnabled(false);
//		button_panel.add(help_btn);
		JButton exit_btn = new JButton("Exit");
		button_panel.add(exit_btn);
		exit_btn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ 
				if( SaadaDBAdmin.showConfirmDialog(SaadaDBAdmin.this, "Do you really want to exit ?") ) {
					System.exit(0);
				}
			}
		});
		c.gridx = 0;
		c.gridy = 1;		
		c.weightx = 0;
		c.weighty = 0;		
		c.fill  = GridBagConstraints.NONE;
		this.add(button_panel, c);
		/*
		 * and show
		 */
		Dimension screenSize =
			Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = this.getPreferredSize();
		setLocation(screenSize.width/2 - (labelSize.width/2),
				screenSize.height/2 - (labelSize.height/2));
		first_call = false;
		this.pack();
		this.setVisible(true);	
		Messenger.hideSplash();		
		//setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("/base2/XIDResult/saadadbs/XIDResult/web/images/saadatransp-text.gif")));
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getClassLoader().getResource("saadadb/icons/saada_transp_square.png")));

		/*
		 * Ask for the administrator password
		 */
		if( Database.getWrapper().supportAccount() ) {
			AdminPassword	ap = new AdminPassword(this);
			if( ap.getTyped_name() == null ) {
				System.exit(1);
			}
		}
	}

	/**
	 * Activate tab panel if there is at least one collection
	 * ACtivation is done once in order to avoid extended att duplication in 
	 * mapping panel
	 */
	public void activateOnglets() {
		if( Database.getCachemeta().getCollection_names().length == 0 ) {
			onglets.setEnabled(false);
		}
		/*
		 * When the tool, is started on DB with collection and the the config panel are user, 
		 * dlconf_panel is already enable. Thus we need another flag to load extended attributes
		 */
		else if( !onglets.isEnabled() || first_call ){
			dlconf_panel.setExteAtt() ;
			onglets.setEnabled(true);
		}
	}

	/**
	 * 
	 */
	private void buildOnglets() {
		onglets = new JTabbedPane();
		onglets.setPreferredSize(new Dimension(650, heigh));
		class_panel = new JPanel();
		class_panel.add(getPlainLabel("<html><B>Meta-data of the selected data class or collection</B><BR><BR>" 
				+ "<UL><LI> Right click on a category (IMAGE SPECTRUM, ...) of a collection node <BR>on the database map to display collection level meta-data.<BR>"
				+ "Meta-data can not be annoted with Utypes, UCDs or units at collection level.</LI>"
				+ "<LI> Right click on a product class on the database map to display class meta-data.<BR>"
				+ "Meta-data can be annoted with Utypes, UCDs or units at class level.</LI>"
				+ "<br><i>This panel is not active while no collection has been created."
		));

		JTextArea jta = new JTextArea();
		jta.setEditable(false);
		class_panel.add(jta);
		onglets.addTab("Meta Data", null, class_panel, "Display meta-data (keyword descriptions)");

		product_panel = new JPanel();
		product_panel.add(new JLabel("Display data by using popup menus on the meta-data tree"));
		onglets.addTab("Data Products", null, product_panel, "Display data (keyword values) limited to 1000");


		dlconf_panel = new JDataLoaderConfPanel(this, onglets.getPreferredSize());
		onglets.addTab("DataLoader Configuration", null, dlconf_panel, "Dataloader setup panels");

		relconf_panel = new RelationConfPanel(this, onglets.getPreferredSize());
		onglets.addTab("Relationships", null, relconf_panel , "Relationship setup panel");

		dbinstall_panel = new DBInstallPanel(this, onglets.getPreferredSize());
		onglets.addTab("Database Installation", null, dbinstall_panel , "Database installation setup panel");
	}

	/**
	 * Synchronize the collection nodes of the tree with real meta_data
	 */
	public void refreshTree() {
		try {
			meta_data_tree.synchronizeCollectionNodes();
		} catch (SaadaException e) {
			showFatalError(this, "An internal error occured (see console)");
		}
	}

	/**
	 * Synchronize the category leaf of the collection nodes of the tree with real meta_data
	 */
	public void refreshTree(String collection, String category) {
		try {
			meta_data_tree.synchronizeCategoryNodes(collection, category);
			if( category.equalsIgnoreCase("TABLE")) {
				meta_data_tree.synchronizeCategoryNodes(collection, "ENTRY");			
			}
		} catch (SaadaException e) {
			showFatalError(this, "An internal error occured (see console)");
		}
	}


	/**
	 * @param frame
	 * @param message
	 * @return
	 */
	public static final boolean showConfirmDialog(Component frame, String message) {
		Object[] options = {"Yes", "No"};
		int n = JOptionPane.showOptionDialog(frame,
				message,
				"Need a Confirmation",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);

		if( n == 0 ) {
			return true;
		}
		else {
			return false;
		}
	}

	public static final void showSuccess(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Success",
				JOptionPane.PLAIN_MESSAGE);		
	}	

	public static final void showFatalError(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Fatal Internal Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static final void showFatalError(Component frame, Exception e) {
		Messenger.printStackTrace(e);
		JOptionPane.showMessageDialog(frame,
				e.toString(),
				"Fatal Internal Error",
				JOptionPane.ERROR_MESSAGE);
	} 

	public static final void showInputError(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				message,
				"Input Error",
				JOptionPane.ERROR_MESSAGE);
	}

	public static final void showInfo(Component frame, String message) {
		JOptionPane.showMessageDialog(frame,
				SaadaDBAdmin.getPlainLabel(message),
				"Information",
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * @param frame
	 * @param message
	 */
	public static final void showCopiableInfo(Component frame, String message, String title) {
		JTextArea jta = new JTextArea(message);
		jta.setEditable(false);
		/*
		 * Mettre un scroller
		 */
		JOptionPane.showMessageDialog(frame,
				jta,
				"Loader Parameters",
				JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * @param txt
	 * @return
	 */
	public final static JLabel getPlainLabel(String txt) {
		JLabel retour = new JLabel(txt);
		retour.setFont(SaadaDBAdmin.plain_font);		
		return retour;
	}
	/**
	 * @param tree_path_components
	 * @throws Exception 
	 * @throws QueryException 
	 */
	public void showProduct(Object[] tree_path_components) throws QueryException {
		if( tree_path_components.length < 3 ) {
			showFatalError(this, "No datasource selected");
			return ;			
		}
		else {
			String sql = "select ", title= "??";
			String[] rejected_coll_clos = null;
			String coll_table_name = tree_path_components[1] + "_" + tree_path_components[2].toString().toLowerCase();
			try {
				/*
				 * Remove hidden columns (not managed yet)
				 */
				rejected_coll_clos = SQLTable.getColumnsExceptsThose(coll_table_name
						, new String[]{"oidproduct", "access_right", "loaded", "group_oid_csa", "nb_rows_csa"
						, "y_min_csa", "y_max_csa", "y_unit_csa", "y_colname_csa"
						, "shape_csa"});
			} catch( FatalException e) {
				Messenger.trapFatalException(e);
			}

			/*
			 * metadata tree path = base-coll-cat: show all product of a category
			 */
			if( tree_path_components.length == 3 ) {
				for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
					if( i != 0 ) {
						sql += ", ";
					}
					sql += rejected_coll_clos[i] ;
				}
				sql += " from " + coll_table_name + " limit 1000";
				title = tree_path_components[2]  + " data of collection <" + tree_path_components[1] + "> (truncated to 1000)";
			}
			/*
			 * metadata tree path = base-coll-cat-class: show all product of a class
			 */
			else if( tree_path_components.length == 4 ) {
				for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
					if( i != 0 ) {
						sql += ", ";
					}
					sql += "coll." + rejected_coll_clos[i] ;
				}
				String[] rejected_class_clos = null;
				try {
					/*
					 * Remove hidden columns (not managed yet)
					 */
					rejected_class_clos = SQLTable.getColumnsExceptsThose(tree_path_components[3].toString(), new String[]{"oidsaada", "namesaada"});
				} catch (FatalException e) {
					Messenger.trapFatalException(e);
				}
				for( int i=0 ; i<rejected_class_clos.length ; i++  ) {
					sql += ", class." + rejected_class_clos[i] ;
				}
				sql += " from " + tree_path_components[1] + "_" + tree_path_components[2].toString().toLowerCase() + " as coll, " +  tree_path_components[3]	+ " as class where coll.oidsaada = class.oidsaada limit 1000";
				title = "Data (" + tree_path_components[2] + ") of class <" + tree_path_components[3]  + ">  of collection <" + tree_path_components[1] + "> (truncated to 1000)";
			}
			else {
				showFatalError(this, "No datasource selected");
				return ;
			}
			product_panel.removeAll();
			showClassPanel();
			/*
			 * Cannot manage product for entries because entries are not products
			 * => no popup menu on tabel.
			 */
			if( tree_path_components[2].equals("ENTRY") ) {
				product_table = new SQLJTable(this, sql, tree_path_components, SQLJTable.PRODUCT_PANEL, false);
			}
			else {
				product_table = new SQLJTable(this, sql, tree_path_components, SQLJTable.PRODUCT_PANEL, true);				
			}
			/*
			 * Needed to activate an horizontal scrolling
			 * and to see something in the table
			 */
			product_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrollPane = new JScrollPane(product_table);    
			scrollPane.setBorder(BorderFactory.createTitledBorder(title));
			/*
			 * Needed too to activate scrolling
			 */
			scrollPane.setPreferredSize(product_panel.getSize());
			product_panel.setLayout(new BorderLayout());
			product_panel.add(scrollPane, BorderLayout.CENTER);
			product_panel.updateUI();
		}
	}

	/**
	 * @param tree_path_components
	 * @throws Exception 
	 * @throws QueryException 
	 */
	public void showClass(Object[] tree_path_components) throws QueryException {
		String sql, title= "??";
		int table_class = 0;
		/*
		 * metadata tree path = base-coll-cat-class: show a business class
		 */
		if( tree_path_components.length == 4 ) {
			sql = "select pk, name_attr, type_attr, name_origin, ucd, utype, queriable, unit , comment, format from saada_metaclass_" + tree_path_components[2] .toString().toLowerCase()
			+ " where  name_class = '" + tree_path_components[3] + "' order by pk";
			title = "Product meta-data for " +  tree_path_components[2] + " class  <" + tree_path_components[3] + "> of collection <" + tree_path_components[1] + ">";
			table_class = SQLJTable.CLASS_PANEL;
		}
		/*
		 * metadata tree path = base-coll-cat: show category class (collection level)
		 */
		else if( tree_path_components.length == 3 ) {
			sql = "select  name_attr, type_attr, name_origin, ucd, comment from saada_metacoll_" + tree_path_components[2].toString().toLowerCase()
			+ " order by pk";
			title = "Collection level meta-data (common for all products) for category <" + tree_path_components[2] + ">";
			table_class = SQLJTable.COLL_PANEL;
		}
		else {
			showFatalError(this, "No datasource selected");
			return ;
		}
		class_panel.removeAll();
		showMetadataPanel();
		class_table = new SQLJTable(this, sql, tree_path_components, table_class, true);
		VoTreeFrame.setTable(class_table);

		/*
		 * Needed to activate an horizontal scrolling
		 * and to see something in the table
		 */
		class_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(class_table);    
		scrollPane.setBorder(BorderFactory.createTitledBorder(title));
		/*
		 * Needed to activate scrolling too 
		 */
		scrollPane.setPreferredSize(product_panel.getSize());
		class_panel.setLayout(new BorderLayout());
		class_panel.add(scrollPane, BorderLayout.CENTER);
		class_panel.updateUI();
	}

	/**
	 * 
	 */
	public void showMetadataPanel() {
		onglets.setSelectedIndex(0);
	}
	/**
	 * 
	 */
	public void showClassPanel() {
		onglets.setSelectedIndex(1);
	}
	/**
	 * 
	 */
	public void showConfPanel(int category) {
		onglets.setSelectedIndex(2);
		dlconf_panel.selectMapping_panel(category);
	}
	/**
	 * 
	 */
	public void showRelationPanel() {
		onglets.setSelectedIndex(1);
	}

	/**
	 * @return Returns the class_table.
	 */
	public SQLJTable getClass_table() {
		return class_table;
	}

	/**
	 * @return Returns the product_table.
	 */
	public SQLJTable getProduct_table() {
		return product_table;
	}

	/**
	 * @param cat
	 */
	public void displayConfigPanel(String cat) {
		this.onglets.setSelectedComponent(dlconf_panel);
		dlconf_panel.selectMapping_panel(cat);
	}


	public JDataLoaderConfPanel getDlconf_panel() {
		return dlconf_panel;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args){
		try {		
			Messenger.showSplash();
			final ArgsParser ap = new  ArgsParser(args);
			ap.setDebugMode();
			Database.init(ap.getDBName());
			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					try {
						new SaadaDBAdmin(ap.isNolog(), null);
					} catch (SaadaException e) {
						Messenger.printStackTrace(e);
						System.exit(1);
					}
				}

			});
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.exit(1);
		}
	}



}

