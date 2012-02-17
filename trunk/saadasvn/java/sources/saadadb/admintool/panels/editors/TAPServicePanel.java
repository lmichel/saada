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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.SimpleTextForm;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.voresources.TapSelector;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLTable;
import saadadb.util.HardwareDescriptor;
import saadadb.util.Messenger;
import saadadb.vo.registry.Authority;


/**
 * @author laurentmichel
 *
 */
public class TAPServicePanel extends EditPanel {
	private SaveButton saveButton = new SaveButton(this);
	private TapSelector tapSelector;
	private Authority authority;
	
	/**
	 * @param rootFrame
	 * @param ancestor
	 */
	public TAPServicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, VO_CURATOR, null, ancestor);
	}
	

	


	@Override
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));		
	}

	@Override
	protected void setActivePanel() {
		tapSelector = new TapSelector(this);
		JPanel tPanel = this.addSubPanel("Published TAP tables");
		MyGBC emc = new MyGBC(5,5,5,5);
		emc.weightx = 1;emc.fill = GridBagConstraints.BOTH;emc.anchor = GridBagConstraints.NORTH;
		


		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(tapSelector), imcep);
		
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
					showSuccess(TAPServicePanel.this.rootFrame, "VO Authority saved");
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
