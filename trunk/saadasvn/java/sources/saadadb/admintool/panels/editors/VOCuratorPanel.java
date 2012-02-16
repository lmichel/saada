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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.SimpleTextForm;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.QueryException;
import saadadb.util.Messenger;
import saadadb.vo.registry.Authority;


/**
 * @author laurentmichel
 *
 */
public class VOCuratorPanel extends EditPanel {
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
	public VOCuratorPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, VO_CURATOR, null, ancestor);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Authority.getInstance().create(null);
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}				
			}			
		});
	}

	@Override
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));		
	}

	@Override
	protected void setActivePanel() {
		authority = Authority.getInstance();
		try {
			authority.load();
		} catch (QueryException e) {
			Messenger.trapQueryException(e);
			return;
		}
		authTitle = new JTextField(20);
		authIdentifier  = new JTextField(20);
		authShortName  = new JTextField(20);
		
		JPanel tPanel = this.addSubPanel("VO Authority");
		JPanel editorPanel = new JPanel();
		editorPanel.setLayout(new GridBagLayout());
		editorPanel.setBackground(LIGHTBACKGROUND);
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		
		SimpleTextForm cp = new SimpleTextForm("Autority"
				,new String[] {"Title", "Identifier", "Short Name"}
				,new Component[]{authTitle, authIdentifier, authShortName});
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

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(editorPanel), imcep);
		

		this.setActionBar();
	}

	@Override
	public void active() {
		// TODO Auto-generated method stub	
	}
	
	protected void setActionBar() {
		saveButton = new SaveButton(this);
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
