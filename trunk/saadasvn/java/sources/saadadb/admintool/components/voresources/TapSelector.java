package saadadb.admintool.components.voresources;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.panels.editors.TAPServicePanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capabilities;


public class TapSelector extends JPanel {
	private TAPServicePanel tapServicePanel;
	private TapServiceList resourceList;
	private JTextArea descPanel;
	
	
	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public TapSelector(TAPServicePanel tapServicePanel) throws Exception {
		this.tapServicePanel = tapServicePanel;
		this.resourceList = new TapServiceList(this);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.gridx = 0; mgbc.gridy = 0;mgbc.weightx = 1; mgbc.weighty = 1;
		this.setLayout(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(this.resourceList);
		jsp.setPreferredSize(new Dimension(350,100));
		this.add(jsp, mgbc);
		mgbc.newRow();
		descPanel = new JTextArea(6,24);
		this.add(new JScrollPane(this.descPanel), mgbc);
		
		
		this.loadCapabilities();
	}
	/**
	 * @throws Exception 
	 * 
	 */
	public void loadCapabilities() throws Exception {
		ArrayList<Capabilities> lc = new ArrayList<Capabilities>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, "TAP");
		this.resourceList.reset();
		for( Capabilities cap: lc) {
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
	public void makeSaveQuery() throws Exception {
		resourceList.makeSaveQuery();
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
