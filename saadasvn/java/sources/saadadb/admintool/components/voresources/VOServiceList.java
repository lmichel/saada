package saadadb.admintool.components.voresources;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capability;


/**
 * JPanel containing a list of {@link VOServiceListItem}.
 * Behave more or less as a JList
 * @author michel
 * @version $Id$
 *
 */
public class VOServiceList extends JPanel {
	private static final long serialVersionUID = 1L;
	private int[] allowedCategories;
	private VOServiceItemSelector itemSelector;
	private String protocol;
	protected  ArrayList<VOServiceListItem> items = new ArrayList<VOServiceListItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public VOServiceList(VOServiceItemSelector itemSelector, String protocol, int[] allowedCategories) {
		System.out.println("@@@@@@@@@@@@@@@ " + protocol);
		this.allowedCategories = allowedCategories;
		this.itemSelector = itemSelector;
		this.protocol = protocol;
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.setLayout(new GridBagLayout());
	}

	public void reset() {
		items = new ArrayList<VOServiceListItem>();
		displayListItems();
	}

	protected void unSelectAll() {
		for( VOServiceListItem tsi: items) {
			if( tsi.isSelected() ) {
				tsi.capability.setDescription(this.getDescription());
			}
			tsi.unSelect();
		}
		setDescription("");
	}
	/**
	 * @param dataTreePath
	 * @return
	 */
	private boolean checkExist(String dataTreePath) {
		for( VOServiceListItem tsi: items) {
			if( dataTreePath.equals(tsi.getDataTreePath())) {
				return true;
			}
		}
		return false;	
	}

	/**
	 * 
	 */
	void displayListItems() {
		this.removeAll();
		MyGBC gbc = new MyGBC(0, 0, 0, 0);

		for( VOServiceListItem tsi: items) {
			tsi.setAlignmentX(Component.LEFT_ALIGNMENT);
			gbc.left(true);gbc.rowEnd();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor =GridBagConstraints.FIRST_LINE_START;
			gbc.weighty = 0;
			gbc.weightx = 1;
			this.add(tsi, gbc);
			gbc.newRow();
		}
		gbc.weighty = 1;
		JPanel jp = new JPanel();
		jp.setPreferredSize(new Dimension(0,0));
		this.add(jp, gbc);
		updateUI();
	}
	/**
	 * @param dataTreePath
	 */
	protected void removeResource(String dataTreePath){
		VOServiceListItem toRemove= null;
		for( VOServiceListItem tsi: items) {
			if( dataTreePath.toString().equals(tsi.getDataTreePath())) {
				toRemove = tsi;
			}
		}
		items.remove(toRemove);
		displayListItems();
	}
	/**
	 * @param dataTreePath
	 * @return
	 * @throws FatalException
	 * @throws Exception 
	 */
	public boolean addResource(DataTreePath dataTreePath) throws SaadaException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Select a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath.toString()) )  return false;
			int cat = Category.getCategory(dataTreePath.category);
			if( allowedCategories != null ) {
				for( int c: allowedCategories ) {
					if( cat == c ) {
						this.items.add(new VOServiceListItem(dataTreePath, protocol));
						this.displayListItems();
						return true;
					}
				}
				AdminComponent.showInputError(this.getParent(), "Data category " 
						+ dataTreePath.category + " not allowed for protocol " + this.protocol);
				return false;				
			} else {
				this.items.add(new VOServiceListItem(dataTreePath, protocol));
				this.displayListItems();
				return true;
			}
		}
	}
	public boolean addResource(Capability capability) throws SaadaException {
		if( this.checkExist(capability.getDataTreePathString()) )  return false;
		this.items.add(new VOServiceListItem(capability));
		this.displayListItems();
		return true;
	}
	
	public VOServiceListItem getSelectedItem()
	{
		VOServiceListItem selectedItem = null;
		for( VOServiceListItem tsi: items) 
		{
			if( tsi.isSelected() ) 
			{
				selectedItem = tsi;
			}
		}
		return selectedItem;
	}

	/**
	 * @param dataTreePath
	 * @param neighbour
	 * @return
	 * @throws FatalException
	 */
	public boolean addResource(DataTreePath dataTreePath, VOServiceListItem neighbour) throws SaadaException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Select a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath.toString()) )  return false;
			for(int i=0  ; i< items.size() ; i++ ) {
				if( this.items.get(i).getDataTreePath().equals(neighbour.getDataTreePath())) {
					this.items.add(i, new VOServiceListItem(dataTreePath, protocol));
					this.displayListItems();
					return true;
				}
			}
			this.items.add(new VOServiceListItem(dataTreePath, protocol));
			this.displayListItems();
			return true;
		}
	}

	protected void setDescription(String description) {
		this.itemSelector.setDescription(description);
	}
	protected String getDescription() {
		return this.itemSelector.getDescription();
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;		
	}
	public void saveCapabilities() throws Exception {
		for( VOServiceListItem tsi: items) {
			if( !tsi.isRemoved()) {
				Table_Saada_VO_Capabilities.addCapability(tsi.capability);
				/*
				 * Used by the Datalink prototype
				 */
				Table_Saada_VO_Capabilities.setDataLinks(tsi.capability);
			}
		}
	}
}
