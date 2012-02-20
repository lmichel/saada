package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capability;


public class TapServiceList extends JPanel {
	private TapSelector tapSelector;

	private ArrayList<TapServiceItem> items = new ArrayList<TapServiceItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public TapServiceList(TapSelector tapSelector) {
		this.tapSelector = tapSelector;
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.setLayout(new GridBagLayout());
		this.setBackground(Color.GRAY);
	}

	public void reset() {
		items = new ArrayList<TapServiceItem>();
		displayListItems();
	}

	protected void unSelectAll() {
		for( TapServiceItem tsi: items) {
			if( tsi.isSelected() ) {
				System.out.println("###### " + tsi.getDataTreePath() + " " + this.getDescription());
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
		for( TapServiceItem tsi: items) {
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

		for( TapServiceItem tsi: items) {
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
		TapServiceItem toRemove= null;
		for( TapServiceItem tsi: items) {
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
			AdminComponent.showInputError(this.getParent(), "Selet a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath.toString()) )  return false;
			this.items.add(new TapServiceItem(dataTreePath));
			this.displayListItems();
			return true;
		}
	}
	public boolean addResource(Capability capability) throws SaadaException {
			if( this.checkExist(capability.getDataTreePath()) )  return false;
			this.items.add(new TapServiceItem(capability));
			this.displayListItems();
			return true;
	}
	
	
	/**
	 * @param dataTreePath
	 * @param neighbour
	 * @return
	 * @throws FatalException
	 */
	public boolean addResource(DataTreePath dataTreePath, TapServiceItem neighbour) throws SaadaException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Selet a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath.toString()) )  return false;
			for(int i=0  ; i< items.size() ; i++ ) {
				if( this.items.get(i).getDataTreePath().equals(neighbour.getDataTreePath())) {
					this.items.add(i, new TapServiceItem(dataTreePath));
					this.displayListItems();
					return true;
				}
			}
			this.items.add(new TapServiceItem(dataTreePath));
			this.displayListItems();
			return true;
		}
	}
	
	protected void setDescription(String description) {
		this.tapSelector.setDescription(description);
	}
	protected String getDescription() {
		return this.tapSelector.getDescription();
	}
	public void makeSaveQuery() throws Exception {
		for( TapServiceItem tsi: items) {
			if( !tsi.isRemoved()) {
				Table_Saada_VO_Capabilities.addCapability(tsi.capability);
			}
		}
	}
}
