/**
 * 
 */
package saadadb.admintool.panels.editors;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admin.DBInstallPanel;
import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.widgets.EditableLabel;
import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadDeployWebApp;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.StringInput;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.database.InstallParamValidator;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_SaadaDB;
import saadadb.util.RegExp;


/**
 * @author michel
 * @version $Id$
 *
 */
public class WebInstallPanel extends EditPanel {
	private JLabel  dirTomcat ;
	private JButton	modTomcat;
	private EditableLabel  dirUrl ;
	private JButton	modUrl;
	
	public WebInstallPanel(AdminTool rootFrame, String title, String icon,
			String ancestor) {
		super(rootFrame, title, null, ancestor);
	}
	
	public WebInstallPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, WEB_INSTALL, null, ancestor);
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));
	}

	@Override
	protected void setActivePanel() {
		modTomcat = new JButton("Modify");		
		modUrl = new JButton("Modify");	
		dirTomcat = getPlainLabel(Database.getConnector().getWebapp_home());
		modTomcat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fcd = new JFileChooser();
				fcd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);			
				int retour = fcd.showOpenDialog(rootFrame);
				if (retour == JFileChooser.APPROVE_OPTION) {
					File selected_file = fcd.getSelectedFile();
					String td ="Not Set";
					try {						
						td = InstallParamValidator.getTomcatDir(selected_file.getAbsolutePath());
					} catch(QueryException e) {
						SaadaDBAdmin.showFatalError(rootFrame, e);
						return;
					}

					try {
						SQLTable.beginTransaction();
						Table_SaadaDB.changeTomcatdir(td);
						SQLTable.commitTransaction();
						Database.init(Database.getName());
						dirTomcat.setText(td);
						CmdThread ct = new ThreadDeployWebApp(WebInstallPanel.this.rootFrame, "Deploy Webapp");
						ct.run();
					} catch (SaadaException e) {
						SaadaDBAdmin.showFatalError(rootFrame, e);
					}
				}
			}
		});		
		dirUrl = new EditableLabel(Database.getUrl_root(), RegExp.URL, Database.getUrl_root().trim().length(), "Not an URL", new Runnable(){
			public void run() {
				try {
					SQLTable.beginTransaction();
					Table_SaadaDB.changeURL(dirUrl.getText());
					SQLTable.commitTransaction();
					CmdThread ct = new ThreadDeployWebApp(WebInstallPanel.this.rootFrame, "Deploy Webapp");
					ct.run();
				} catch (AbortException e) {
					showFatalError(rootFrame, e);
				}
				Database.init(Database.getName());
			}
		});


		JPanel panel = this.addSubPanel("Web Deployement");
		MyGBC mgbc = new MyGBC(5,5,5,5);	mgbc.anchor = GridBagConstraints.EAST;	
		panel.add(getPlainLabel("Tomcat Directory "), mgbc);
		mgbc.next();mgbc.anchor = GridBagConstraints.WEST;
		panel.add(getPlainLabel(Database.getConnector().getWebapp_home()), mgbc);
		mgbc.rowEnd();
		panel.add(modTomcat, mgbc);
		mgbc.newRow();mgbc.gridwidth = 3;
		panel.add(getHelpLabel(HelpDesk.WEBINSTALL_DIR), mgbc);
		
		mgbc.newRow();mgbc.anchor = GridBagConstraints.EAST;mgbc.gridwidth = 1;
		panel.add(getPlainLabel("Root URL of the SaadaDB "), mgbc);		
		mgbc.rowEnd();mgbc.gridwidth=2; mgbc.anchor = GridBagConstraints.WEST;
		panel.add(dirUrl, mgbc);
		mgbc.newRow();mgbc.gridwidth = 3;
		panel.add(getHelpLabel(HelpDesk.WEBINSTALL_URL), mgbc);

	}

	@Override
	public void active() {
		// TODO Auto-generated method stub
		
	}

}
