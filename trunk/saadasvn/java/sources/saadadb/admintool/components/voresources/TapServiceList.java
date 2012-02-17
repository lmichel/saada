package saadadb.admintool.components.voresources;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

	private Set<TapServiceItem> items = new LinkedHashSet<TapServiceItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 */
	public TapServiceList(TapSelector tapSelector) {
		this.tapSelector = tapSelector;
		this.setPreferredSize(new Dimension(350, 100));	
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.setLayout(new GridBagLayout());
		this.setBackground(Color.yellow);
	}


	public boolean addResource(DataTreePath dataTreePath) throws FatalException {
		if( dataTreePath.isCollectionLevel() ) {
			AdminComponent.showInputError(this.getParent(), "Selet a data tree node either at category or class level");
			return false;
		}
		else {
			items.add(new TapServiceItem(dataTreePath));
			//this.removeAll();
			MyGBC gbc = new MyGBC(0, 0, 0, 0);
			for( TapServiceItem tsi: items) {
gbc.left(true);
this.add(tsi, gbc);
				gbc.newRow();
			}
			updateUI();
			return true;
		}
	}

}
