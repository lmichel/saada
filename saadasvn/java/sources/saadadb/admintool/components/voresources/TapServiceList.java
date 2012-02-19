package saadadb.admintool.components.voresources;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.CollapsiblePanel;
import saadadb.admintool.components.RelationshipChooser;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.panels.editors.TAPServicePanel;
import saadadb.admintool.panels.tasks.RelationPopulatePanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaRelation;


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
		this.setBackground(Color.yellow);
	}


	/**
	 * @param dataTreePath
	 * @return
	 */
	private boolean checkExist(DataTreePath dataTreePath) {
		for( TapServiceItem tsi: items) {
			if( dataTreePath.toString().equals(tsi.dataTreePath.toString())) {
				System.out.println(dataTreePath.toString() + " already here");return true;
			}
		}
		return false;	
	}

	/**
	 * 
	 */
	private void displayListItems() {
		this.removeAll();
		MyGBC gbc = new MyGBC(0, 0, 0, 0);

		for( TapServiceItem tsi: items) {
			tsi.setAlignmentX(Component.LEFT_ALIGNMENT);
			gbc.left(true);gbc.rowEnd();
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor =GridBagConstraints.FIRST_LINE_START;
			gbc.weighty = 0;
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
	 * @return
	 * @throws FatalException
	 */
	public boolean addResource(DataTreePath dataTreePath) throws FatalException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Selet a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath) )  return false;
			this.items.add(new TapServiceItem(dataTreePath));
			this.displayListItems();
			return true;
		}
	}
	/**
	 * @param dataTreePath
	 * @param neighbour
	 * @return
	 * @throws FatalException
	 */
	public boolean addResource(DataTreePath dataTreePath, TapServiceItem neighbour) throws FatalException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Selet a data tree node either at category or class level");
			return false;
		}
		else {
			if( this.checkExist(dataTreePath) )  return false;
			for(int i=0  ; i< items.size() ; i++ ) {
				if( this.items.get(i).dataTreePath.toString().equals(neighbour.dataTreePath.toString())) {
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
}
