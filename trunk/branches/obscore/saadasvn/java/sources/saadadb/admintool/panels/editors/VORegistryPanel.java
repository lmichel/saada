/**
 * 
 */
package saadadb.admintool.panels.editors;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.SimpleTextForm;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.vo.registry.Authority;


/**
 * @author laurentmichel
 *
 */
@SuppressWarnings("serial")
public class VORegistryPanel extends EditPanel {
	private SaveButton saveButton = new SaveButton(this);
	private JTextField authTitle;
	private JTextField authIdentifier;
	private JTextField authShortName;
	private JTextField authOrg;

	private JTextField curPublisher;
	private JTextField curName;
	private JTextField curLogo;

	private JTextField contactName;
	private JTextArea contactAdress;
	private JTextField contactEmail;
	private JTextField contactTel;

	private JTextField contentSubject;
	private JTextField contentReferenceURL;
	private JTextArea contentDescription;
	private JTextField contentType;
	private JTextField contentLevel;

	private Authority authority;

	/**
	 * @param rootFrame
	 * @param ancestor
	 */
	public VORegistryPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, VO_REGISTRY, null, ancestor);
	}

	/**
	 * 
	 */
	private void  load() {
		try {
			authority = Authority.getInstance();
			if( authority.hasBeenStored() ) {
				authority.load();
				authTitle.setText(this.authority.getAuthTitle());
				authIdentifier.setText(this.authority.getAuthIdentifier());
				authShortName.setText(this.authority.getAuthShortName());
				authOrg.setText(this.authority.getAuthOrg());

				curPublisher.setText(this.authority.getCurationPublisher());
				curName.setText(this.authority.getCurationName());
				curLogo.setText(this.authority.getCurationLogo());

				contactName.setText(this.authority.getContactName());
				contactAdress.setText(this.authority.getContactAdresse());
				contactEmail.setText(this.authority.getContactMail());
				contactTel.setText(this.authority.getContactTel());

				contentSubject.setText(this.authority.getContentSubject());
				contentReferenceURL.setText(this.authority.getContentRefURL());
				contentDescription.setText(this.authority.getContentDescription());
				contentType.setText(this.authority.getContentType());
				contentLevel.setText(this.authority.getContentLevel());
			} else {
				authTitle.setText(Database.getDbname());
				authIdentifier.setText("ivo://saadadb/" + Database.getDbname());
				authShortName.setText(Database.getDbname() + " authority");
				authOrg.setText("");

				curPublisher.setText("ivo://saadadbcuration/" + Database.getDbname());
				curName.setText(this.authority.getCurationName());
				curLogo.setText(Database.getUrl_root() + "/images/saadatransp-text-small.gif");

				contactName.setText("");
				contactAdress.setText("");
				contactEmail.setText("");
				contactTel.setText("");

				contentSubject.setText("Archive generated by SAADA");
				contentReferenceURL.setText(Database.getUrl_root());
				contentDescription.setText("Archive generated by SAADA");
				contentType.setText("Archive");
				contentLevel.setText("Research");
			}
		} catch (QueryException e) {
			Messenger.trapQueryException(e);
			setDefault();
		}
	}

	/**
	 * 
	 */
	private void setDefault() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String hostname = addr.getHostName();
			String user = System.getProperty("user.name");

			authTitle.setText(Database.getDbname());
			authShortName.setText(Database.getDbname());
			authIdentifier.setText("ivoa://");

			curName.setText(user);
			curLogo.setText("http://code.google.com/p/saada/logo?cct=1311864309");
			curPublisher.setText(user + "'s institute");
			contactName.setText(user);
			contactEmail.setText(user + "@" + hostname);

			contentSubject.setText(Database.getDbname());
			contentReferenceURL.setText(Database.getUrl_root());

			contentType.setText("active");
			contentLevel.setText("General");

		} catch (UnknownHostException e) {
			Messenger.printStackTrace(e);
		}
	}

	@Override
	protected void setToolBar() {
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));		
	}

	@Override
	protected void setActivePanel() {
		authority = Authority.getInstance();
		authTitle = new JTextField(20);
		authIdentifier  = new JTextField(20);
		authShortName  = new JTextField(20);
		authOrg  = new JTextField(20);

		JPanel tPanel = this.addSubPanel("VO Authority");
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new GridBagLayout());
		editorPanel.setBackground(LIGHTBACKGROUND);
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;

		SimpleTextForm cp = new SimpleTextForm("Autority"
				,new String[] {"Title", "Identifier", "Short Name", "Organism"}
				,new Component[]{authTitle, authIdentifier, authShortName, authOrg});
		editorPanel.add(cp, emc);
		emc.newRow();

		curPublisher = new JTextField(20);;
		curName = new JTextField(20);;
		curLogo = new JTextField(20);;

		cp = new SimpleTextForm("Curation"
				,new String[] {"Pubisher", "Name", "Loge URL"}
				,new Component[]{curPublisher, curName, curLogo});
		editorPanel.add(cp, emc);
		emc.newRow();

		contactName = new JTextField(20);
		contactAdress = new JTextArea(5, 20);
		contactEmail = new JTextField(20);
		contactTel = new JTextField(20);

		cp = new SimpleTextForm("Contact"
				,new String[] {"Contact", "Address", "EMail", "Telephone"}
				,new Component[]{contactName, new JScrollPane(contactAdress), contactEmail, contactTel});
		editorPanel.add(cp, emc);
		emc.newRow();

		contentSubject = new JTextField(20);
		contentReferenceURL = new JTextField(20);
		contentDescription= new JTextArea(5, 20);
		contentType = new JTextField(20);
		contentLevel = new JTextField(20);

		cp = new SimpleTextForm("Content"
				,new String[] {"Subject", "Reference URL", "Description", "Type", "Level"}
				,new Component[]{contentSubject, contentReferenceURL, new JScrollPane(contentDescription), contentType, contentLevel});
		editorPanel.add(cp, emc);
		emc.newRow();

		editorPanel.add(getHelpLabel(HelpDesk.VO_CURATION), emc);
		emc.newRow();
		JButton defBtn = new JButton("Default Values");
		defBtn.setToolTipText("Remove the current definition from the database and propose the defaults values");
		defBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					if( AdminComponent.showConfirmDialog(getParent(), "Current values will overriden")) {
						SQLTable.beginTransaction();
						Authority.getInstance().remove(null);
						SQLTable.commitTransaction();
						load();
					}
				} catch (SaadaException e) {
					AdminComponent.showFatalError(getParent(), e);
				}

			}
		});
		editorPanel.add(defBtn, emc);

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(editorPanel), imcep);

		this.load();
		this.setActionBar();
	}

	@Override
	public void active() {
		// TODO Auto-generated method stub	
	}

	protected void setActionBar() {
		this.saveButton = new SaveButton(this);
		this.saveButton.setEnabled(true);
		this.saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					authority.setAuthTitle(authTitle.getText());
					authority.setAuthIdentifier(authIdentifier.getText());
					authority.setAuthShortName(authShortName.getText());
					authority.setAuthOrg(authOrg.getText());

					authority.setCurationPublisher(curPublisher.getText());
					authority.setCurationName(curName.getText());
					authority.setCurationLogo(curLogo.getText());

					authority.setContactName(contactName.getText());
					authority.setContactAdresse(contactAdress.getText());
					authority.setContactMail(contactEmail.getText());
					authority.setContactTel(contactTel.getText());

					authority.setContentSubject(contentSubject.getText());
					authority.setContentRefURL(contentReferenceURL.getText());
					authority.setContentDescription(contentDescription.getText());
					authority.setContentType(contentType.getText());
					authority.setContentLevel(contentLevel.getText());

					SQLTable.beginTransaction();
					Authority.getInstance().create(null);
					SQLTable.commitTransaction();
					load();
					showSuccess(VORegistryPanel.this.rootFrame, "VO Authority saved");
				} catch (Exception e) {
					showFatalError(rootFrame, e);
				}				
			}			
		});
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setBackground(LIGHTBACKGROUND);
		tPanel.setPreferredSize(new Dimension(1000,48));
		tPanel.setMaximumSize(new Dimension(1000,48));
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0; c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		tPanel.add(saveButton, c);
		c.gridx++;		
		/*
		 * Just to push all previous components to the left
		 */
		c.weightx = 1;
		tPanel.add(new JLabel(" "), c);
		this.add(tPanel);	
	}


}
