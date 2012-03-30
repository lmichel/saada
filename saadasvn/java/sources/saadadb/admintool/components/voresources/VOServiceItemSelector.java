package saadadb.admintool.components.voresources;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capability;


/**
 * Panel showing a list of items published in a VO resource.
 * The VO resource is defined by all Capabilities matching the protocol
 * A description is shown when an item is selected
 * @author michel
 * @version $Id$
 *
 */
public class VOServiceItemSelector extends JPanel {
	private static final long serialVersionUID = -5324001371175383374L;
	private EditPanel tapServicePanel;
	private VOServiceList resourceList;
	private JTextArea descPanel;
	private String protocol;
	
	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public VOServiceItemSelector(EditPanel tapServicePanel, String protocol) throws Exception {
		this.protocol = protocol;
		this.tapServicePanel = tapServicePanel;
		this.resourceList = new VOServiceList(this, protocol);
		this.resourceList.setBorder(BorderFactory.createTitledBorder("Saada resources currently published in " + protocol));
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.gridx = 0; mgbc.gridy = 0;mgbc.weightx = 0.5; mgbc.weighty = 0.5;
		this.setLayout(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(this.resourceList);
		jsp.setPreferredSize(new Dimension(350,200));
		this.add(jsp, mgbc);
		
		mgbc.rowEnd();
		JPanel dp = new JPanel();
		dp.setLayout(new BoxLayout(dp, BoxLayout.PAGE_AXIS));
		this.descPanel = new JTextArea(6,24);
		JScrollPane jscp = new JScrollPane(this.descPanel);
		jscp.setBorder(BorderFactory.createTitledBorder("Description of the selected item"));
		dp.add(jscp);	
		dp.add(AdminComponent.getHelpLabel(HelpDesk.VOITEM_DESCRIPTION));
		this.add(dp, mgbc);
		
		mgbc.newRow();mgbc.weightx = 1; mgbc.weighty =1 ;mgbc.anchor = GridBagConstraints.NORTHWEST;		
		this.add(AdminComponent.getHelpLabel(HelpDesk.VOITEM_EDITION), mgbc);
		this.loadCapabilities();
	}
	/**
	 * @throws Exception 
	 * 
	 */
	public void loadCapabilities() throws Exception {
		ArrayList<Capability> lc = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, protocol);
		this.resourceList.reset();
		for( Capability cap: lc) {
			this.resourceList.addResource(cap);
		}		
		this.resourceList.displayListItems();
	}
	public boolean setDataTreePath(DataTreePath dataTreePath) throws SaadaException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(tapServicePanel.rootFrame, "Selet a data tree node either at category or class level");
			return false;
		}
		resourceList.addResource(dataTreePath);
		return true;
	}
	public void reset() {
		resourceList.reset();
	}
	public void saveCapabilities() throws Exception {
		resourceList.saveCapabilities();
	}
	public void setDescription(String description){
		this.descPanel.setText(description);
	}
	public String getDescription() {
		return this.descPanel.getText();
	}
	public boolean isEmpty() {
		return (this.resourceList.items.size() == 0) ? true: false;
	}
}
