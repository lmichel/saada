package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.windows.TextSaver;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capability;
import saadadb.vo.registry.Record;


/**
 * Small panel used as item in {@link VOServiceList}. representing resources published in one VO service
 * @author michel
 * @version $Id$
 *
 */
public class VOServiceListItem extends JPanel {
	private static final long serialVersionUID = 1L;
	public static final Color SELECTED = AdminComponent.OLD_HEADER;
	public static final Color UNSELECTED = Color.WHITE;
	protected Capability capability;
	private JCheckBox button = new JCheckBox();
	private JLabel label = new JLabel();
	private boolean removed = false;
	private boolean selected = false;

	/**
	 * Creator used when items are added from the data tree
	 * @param dataTreePath
	 * @param protocol
	 * @throws SaadaException
	 */
	protected VOServiceListItem(DataTreePath dataTreePath, String protocol) throws SaadaException {
		this.capability = new Capability();
		this.capability.setProtocol(protocol);
		this.capability.setDataTreePath(dataTreePath);
		this.button.setSelected(true);
		this.setDefaultDescription();
		this.setUI();
	}
	/**
	 * Used when capabilities are reloaded from the table
	 * @param capability
	 * @throws SaadaException
	 */
	protected VOServiceListItem(Capability capability) throws SaadaException {
		this.capability = capability;
		this.setDefaultDescription();
		this.button.setSelected(true);
		this.setUI();
	}
	private void setDefaultDescription() throws FatalException {
		if( this.capability.getDescription() == null || this.capability.getDescription().length() == 0 ) {
			this.capability.setDescription(
					Database.getCachemeta().getCollection(this.capability.getDataTreePath().collection).getDescription());
		}
	}
	private void setUI() {
		this.button.setToolTipText("(Un)Mark as to remove");
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( !button.isSelected() ) {
					label.setForeground(Color.GRAY);
					VOServiceListItem.this.setBackground(Color.LIGHT_GRAY);	
					if( getBackground() == SELECTED ) ((VOServiceList)(getParent())).unSelectAll();
					removed = true;
				} else {
					label.setForeground(Color.BLACK);
					VOServiceListItem.this.setBackground(UNSELECTED);		
					removed = false;
				}
			}		
		} );
		this.add(this.button);
		this.label.setText(capability.getDataTreePathString());
		this.label.setOpaque(false);
		this.add(Box.createHorizontalStrut(10));
		this.add(this.label);
		if( !this.capability.getProtocol().equals(Capability.TAP )) {
			addRegistryAnchor();
		}
		this.setBackground(UNSELECTED);
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.addMouseListener(new MouseListener() {		
			public void mouseReleased(MouseEvent arg0) {
				if( !removed ) {
					((VOServiceList)(getParent())).unSelectAll();
					VOServiceListItem.this.select();
				}
			}		
			public void mousePressed(MouseEvent arg0) {}			
			public void mouseExited(MouseEvent arg0) {}		
			public void mouseEntered(MouseEvent arg0) {}	
			public void mouseClicked(MouseEvent arg0) {}
		});
	}
	private void addRegistryAnchor() {
		JLabel anchor = AdminComponent.getAnchorLabel("show Reg");
		anchor.addMouseListener(new MouseListener() {		
			public void mouseReleased(MouseEvent arg0) {
				try {
					(new TextSaver(
							null
							, capability.getProtocol() + " Registry Record"
							, capability.getProtocol() + "Registry." + Database.getDbname() + "." + capability.getDataTreePath() + ".xml"
							,(new Record()).getRecord(capability).toString())).open(SQLJTable.DMVIEW_PANEL);
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}
			}		
			public void mousePressed(MouseEvent arg0) {}			
			public void mouseExited(MouseEvent arg0) {}		
			public void mouseEntered(MouseEvent arg0) {}	
			public void mouseClicked(MouseEvent arg0) {}
		});

		this.add(Box.createHorizontalStrut(10));
		this.add(anchor);
		

	}
	protected String getDataTreePath() {
		return this.capability.getDataTreePathString();
	}
	public boolean fireAddResource(DataTreePath dataTreePath) throws SaadaException {
		return ((VOServiceList)(this.getParent())).addResource(dataTreePath, this);
	}
	public void select() {
		this.setBackground(SELECTED);
		this.label.setForeground(Color.WHITE);
		((VOServiceList)(this.getParent())).setDescription(this.capability.getDescription());
		this.selected = true;
	}
	public void unSelect() {
		if( !removed ) {
			this.setBackground(UNSELECTED);
			this.label.setForeground(Color.BLACK);
		}
		else {
			this.label.setForeground(Color.GRAY);
			this.setBackground(Color.LIGHT_GRAY);	
		}
		if( this.selected) {
			this.capability.setDescription( ((VOServiceList)(this.getParent())).getDescription());
		}
		this.selected = false;
	}
	public boolean isSelected() {
		return selected;
	}
	public boolean isRemoved() {
		return removed;
	}

}
