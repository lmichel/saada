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

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.CmdThread;
import saadadb.admintool.cmdthread.ThreadDeployWebApp;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.EditableLabel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.utils.WebsiteChecker;
import saadadb.database.Database;
import saadadb.database.InstallParamValidator;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.newdatabase.NewWebServer;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_SaadaDB;
import saadadb.util.Messenger;
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
	private JButton testWebAp, openWebAp, openLogs;
	
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
		this.initTreePathPanel();
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
						AdminComponent.showFatalError(rootFrame, e);
						return;
					}

					try {
						Messenger.printMsg(Messenger.TRACE, "Set Tomcat dir to " + td);
						SQLTable.beginTransaction();
						Table_SaadaDB.changeTomcatdir(td);
						SQLTable.commitTransaction();
						Database.init(Database.getName());
						dirTomcat.setText(td);
						CmdThread ct = new ThreadDeployWebApp(WebInstallPanel.this.rootFrame, "Deploy Webapp");
						ct.run();
					} catch (SaadaException e) {
						AdminComponent.showFatalError(rootFrame, e);
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
					/*
					 * Update the current application
					 */
					Database.getConnector().setUrl_root(dirUrl.getText());
					/*
					 * Update the curret web application
					 */
					NewWebServer.buildDBNameFile(Database.getRoot_dir(), Database.getDbname(), dirUrl.getText());
					CmdThread ct = new ThreadDeployWebApp(WebInstallPanel.this.rootFrame, "Deploy Webapp");
					ct.run();
				} catch (Exception e) {
					showFatalError(rootFrame, e);
				}
				Database.init(Database.getName());
			}
		});
		testWebAp = new JButton("Test Web Application");
		testWebAp.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				boolean isWorking = (Boolean) WebsiteChecker.checkIfURLExists(Database.getUrl_root())[0];
				String errorMessage = (String) WebsiteChecker.checkIfURLExists(Database.getUrl_root())[1];
				if (isWorking)
					AdminComponent.showSuccess(rootFrame, "Web application is well deployed and accessible at " + Database.getUrl_root());
				else
					AdminComponent.showFatalError(rootFrame, "Web application is not working\nError Message : " + errorMessage);
			}
		});
		openWebAp = new JButton("Open Web Application");
		openWebAp.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				String url = Database.getUrl_root();
				WebsiteChecker.openURL(url);
			}
		});
		openLogs = new JButton("Open Apache Logs");
		openLogs.addActionListener(new ActionListener() 
		{
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				rootFrame.activePanel(AdminComponent.LOGS_DISPLAY_WEB);
			}
		});

		JPanel panel = this.addSubPanel("Web Deployement");
		MyGBC mgbc = new MyGBC(5,5,5,5);	mgbc.anchor = GridBagConstraints.EAST;	
		panel.add(getPlainLabel("Tomcat Directory "), mgbc);
		mgbc.next();mgbc.anchor = GridBagConstraints.WEST;
		panel.add(dirTomcat, mgbc);
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
		mgbc.newRow();mgbc.gridwidth = 1; mgbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(testWebAp, mgbc);
		mgbc.gridy++;
		panel.add(openWebAp, mgbc);
		mgbc.gridy++;
		panel.add(openLogs, mgbc);
	}

	@Override
	public void active() {
		// TODO Auto-generated method stub
		
	}

}
