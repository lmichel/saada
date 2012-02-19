package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
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


public class TapSelector extends JPanel {
	private TAPServicePanel tapServicePanel;
	private TapServiceList resourceList;
	
	
	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public TapSelector(TAPServicePanel tapServicePanel) {
		this.tapServicePanel = tapServicePanel;
		this.resourceList = new TapServiceList(this);
		
		MyGBC mgbc = new MyGBC(5,5,5,5);
		mgbc.gridx = 0; mgbc.gridy = 0;mgbc.weightx = 1; mgbc.weighty = 1;
		this.setLayout(new GridBagLayout());
		JScrollPane jsp = new JScrollPane(this.resourceList);
		jsp.setPreferredSize(new Dimension(350,100));
		this.add(jsp, mgbc);
	}
	

	public boolean setDataTreePath(DataTreePath dataTreePath) throws FatalException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(tapServicePanel.rootFrame, "Selet a data tree node either at category or class level");
			return false;
		}
		resourceList.addResource(dataTreePath);
		return true;
	}
}
