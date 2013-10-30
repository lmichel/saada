package saadadb.admin;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admin.dialogs.StringInput;
import saadadb.admin.widgets.EditableLabel;
import saadadb.database.Database;
import saadadb.database.InstallParamValidator;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_SaadaDB;
import saadadb.util.RegExp;

public class DBInstallPanel extends JPanel{
	protected JFrame frame;
	private JButton	mod_dir = new JButton("Modify");
	private JLabel  dir_rep = SaadaDBAdmin.getPlainLabel(Database.getRepository());
	private JButton	mod_rep = new JButton("Modify");
	private JLabel  dir_tomcat = SaadaDBAdmin.getPlainLabel(Database.getConnector().getWebapp_home());
	private JButton	mod_tomcat = new JButton("Modify");
	private EditableLabel  dir_url ;

	public DBInstallPanel(SaadaDBAdmin saadaDBAdmin, Dimension dim) {
		super();
		this.frame = saadaDBAdmin;
		this.setPreferredSize(dim);
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;     
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill =GridBagConstraints.BOTH	;
		/*
		 * Create a scroll panel
		 */
		JPanel panel = new JPanel();		
		panel.setLayout(new GridLayout(3, 1));
		JScrollPane scroll_panel = new JScrollPane(panel);			
		scroll_panel.setPreferredSize(new Dimension((int)(dim.getWidth())-50, (int)(dim.getHeight()) - 100));
		scroll_panel.setBackground(SaadaDBAdmin.beige_color);
		c.gridx = 0;
		c.gridy = 0;
		c.fill =GridBagConstraints.BOTH	;
		c.weightx = c.weighty = 1.0;
		this.add(scroll_panel, c);
		
		JPanel saadadb_panel = new JPanel();					
		saadadb_panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = new GridBagConstraints();
		saadadb_panel.setBackground(SaadaDBAdmin.beige_color);
		saadadb_panel.setBorder(BorderFactory.createTitledBorder("SaadaDB installation"));
		cl.insets = new Insets(5, 5, 5,5); 
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(new JLabel("Name:"), cl);
		cl.weightx  = 0.0; cl.gridx = 1 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(SaadaDBAdmin.getPlainLabel(Database.getName()), cl);
		
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(new JLabel("Install. Dir.:"), cl);
		cl.weightx  = 0.0; cl.gridx = 1 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.NONE;
		cl.gridwidth = 2;
		saadadb_panel.add(SaadaDBAdmin.getPlainLabel(Database.getRoot_dir()), cl);
		cl.weightx  = 1.0; cl.gridx = 3 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		cl.gridwidth = 1;
		saadadb_panel.add(mod_dir, cl);

		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(new JLabel("Repository:"), cl);
		cl.weightx  = 0.0; cl.gridx = 1 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(dir_rep, cl);
		cl.weightx  = 1.0; cl.gridx = 2 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.NONE;
		saadadb_panel.add(mod_rep, cl);
		c.gridx = 0; c.gridy = 0;
		panel.add(saadadb_panel, 0, 0);
		
		JPanel web_panel = new JPanel();			
		web_panel.setLayout(new GridBagLayout());
		web_panel.setBackground(SaadaDBAdmin.beige_color);
		web_panel.setBorder(BorderFactory.createTitledBorder("Web Interface"));
		
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_END; cl.fill = GridBagConstraints.NONE;
		web_panel.add(new JLabel("Tomcat Dir."), cl);
		cl.weightx  = 1.0; cl.gridx = 1 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_START; cl.fill = GridBagConstraints.NONE;
		web_panel.add(dir_tomcat, cl);
		cl.weightx  = 1.0; cl.gridx = 2 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_START; cl.fill = GridBagConstraints.NONE;
		web_panel.add(mod_tomcat, cl);
		
		dir_url = new EditableLabel(Database.getUrl_root(), RegExp.URL, Database.getUrl_root().trim().length(), "Not an URL", new Runnable(){
			public void run() {
				try {
					SQLTable.beginTransaction();
					Table_SaadaDB.changeURL(DBInstallPanel.this.dir_url.getText());
					SQLTable.commitTransaction();
				} catch (AbortException e) {
					SaadaDBAdmin.showFatalError(DBInstallPanel.this, e);
				}
				Database.init(Database.getName());
			}
			
		});
		dir_url.setBackground(web_panel.getBackground());	
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		web_panel.add(new JLabel("SaadaDB Root URL"), cl);
		cl.weightx  = 0.0; cl.gridx = 1 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.NONE;
		cl.gridwidth = 2;
		web_panel.add(dir_url, cl);
		panel.add(web_panel, 0, 1);
		
		JPanel jdbc_panel = new JPanel();			
		jdbc_panel.setLayout(new GridBagLayout());
		jdbc_panel.setBackground(SaadaDBAdmin.beige_color);
		jdbc_panel.setBorder(BorderFactory.createTitledBorder("JDBC Connection"));
		
		cl.gridwidth = 1;
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		jdbc_panel.add(new JLabel("JDBC Driver:"), cl);
		cl.weightx  = 1.0; cl.gridx = 1 ; cl.gridy = 0; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.HORIZONTAL;
		jdbc_panel.add(SaadaDBAdmin.getPlainLabel(Database.getConnector().getJdbc_driver()), cl);
		
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		jdbc_panel.add(new JLabel("JDBC URL:"), cl);
		cl.weightx  = 1.0; cl.gridx = 1 ; cl.gridy = 1; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.HORIZONTAL;
		jdbc_panel.add(SaadaDBAdmin.getPlainLabel(Database.getConnector().getJdbc_url()), cl);
		
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		jdbc_panel.add(new JLabel("Admin Role:"), cl);
		cl.weightx  = 1.0; cl.gridx = 1 ; cl.gridy = 2; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.HORIZONTAL;
		jdbc_panel.add(SaadaDBAdmin.getPlainLabel(Database.getConnector().getJdbc_administrator()), cl);
		
		cl.weightx  = 0.0; cl.gridx = 0 ; cl.gridy = 3; cl.anchor = GridBagConstraints.LINE_END;cl.fill = GridBagConstraints.NONE;
		jdbc_panel.add(new JLabel("Reader Role:"), cl);
		cl.weightx  = 1.0; cl.gridx = 1 ; cl.gridy = 3; cl.anchor = GridBagConstraints.LINE_START;cl.fill = GridBagConstraints.HORIZONTAL;
		jdbc_panel.add(SaadaDBAdmin.getPlainLabel(Database.getConnector().getJdbc_reader()), cl);
		c.gridx = 0; c.gridy = 2;
		panel.add(jdbc_panel, 0,2);

		setListener();
	}
	
	
	/**
	 * 
	 */
	private void setListener() {
		mod_dir.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if( SaadaDBAdmin.showConfirmDialog(frame, "<HTML>You are going to move your SaadaDB" 
						                                + "<BR>This operation must be done very carefully"
						                                + "<BR>1- Change the installation dir from this panel"
						                                + "<BR>2- Move the content of the directory" + Database.getRoot_dir() + " into the new directory"
						                                + "<BR>3- Rename NEW_INSTALL_DIR/bin/saadadb.properties.new as saadadb.properties"
						                                + "<BR>4- Start saadmintool from the new location"
						                                + "<BR>5- Update Tomcat params and the repository directory"
						                                + "<BR>6- Deploy the Web application."
						                                ) ) {
					StringInput ni = new StringInput(DBInstallPanel.this.frame, "Type or paste the new install directory");
					String new_name = ni.getNew_name();
					if( new_name != null && new_name.length() > 0 && SaadaDBAdmin.showConfirmDialog(frame, "Are you really sure?") ) {
						try {
							SQLTable.beginTransaction();
							Table_SaadaDB.changeBasedir(new_name);
							SQLTable.commitTransaction();
							String old_dir = Database.getRoot_dir();
							SaadaDBAdmin.showInfo(frame, "<HTML>You have changed your SaadaDB installation directory" 
	                                + "<BR>Dot now by hand the following operations"
	                                + "<BR>1- Move the content of the directory" + old_dir + " into the directory " + new_name
	                                + "<BR>2- Rename " + new_name + Database.getSepar() + "bin " + Database.getSepar() + "saadadb.properties.new as saadadb.properties"
	                                + "<BR>3- Start saadmintool from the new location"
	                                + "<BR>4- Update Tomcat params and the repository directory"						                                
	                                + "<BR>5- Deploy the Web application."
	                                );
						} catch (SaadaException e1) {
							SaadaDBAdmin.showFatalError(ni, e1);
						}
						
						
					}
					
				}
			}
			
		});
		mod_rep.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fcd = new JFileChooser();
				fcd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
				int retour = fcd.showOpenDialog(DBInstallPanel.this.frame);
				if (retour == JFileChooser.APPROVE_OPTION) {
					File selected_file = fcd.getSelectedFile();
					try {
						InstallParamValidator.canBeRepository(selected_file.getAbsolutePath());
					} catch(QueryException e) {
						SaadaDBAdmin.showFatalError(DBInstallPanel.this.frame, e);
						return;
					}
					try {
						SQLTable.beginTransaction();
						Table_SaadaDB.changeRepdir(selected_file.getAbsolutePath());
						SQLTable.commitTransaction();
						Database.init(Database.getName());
						dir_rep.setText(selected_file.getAbsolutePath());
					} catch (AbortException e) {
						SaadaDBAdmin.showFatalError(DBInstallPanel.this.frame, e);
					}
				}
			}
		});		

		mod_tomcat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fcd = new JFileChooser();
				fcd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
				int retour = fcd.showOpenDialog(DBInstallPanel.this.frame);
				if (retour == JFileChooser.APPROVE_OPTION) {
					File selected_file = fcd.getSelectedFile();
					String td ="Not Set";
					try {						
						td = InstallParamValidator.getTomcatDir(selected_file.getAbsolutePath());
					} catch(QueryException e) {
						SaadaDBAdmin.showFatalError(DBInstallPanel.this.frame, e);
						return;
					}

					try {
						SQLTable.beginTransaction();
						Table_SaadaDB.changeTomcatdir(td);
						SQLTable.commitTransaction();
						Database.init(Database.getName());
						dir_tomcat.setText(td);
					} catch (SaadaException e) {
						SaadaDBAdmin.showFatalError(DBInstallPanel.this.frame, e);
					}
				}
			}
		});		
	}

}
