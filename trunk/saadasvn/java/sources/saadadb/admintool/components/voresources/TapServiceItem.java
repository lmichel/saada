package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.vo.registry.Capabilities;


public class TapServiceItem extends JPanel {
	public static final Color SELECTED = AdminComponent.OLD_HEADER;
	public static final Color UNSELECTED = Color.WHITE;
	protected Capabilities capability;
	private JButton button = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/greenLight.png")));
	private JLabel label = new JLabel();
	private boolean removed = false;
	private boolean selected = false;

	protected TapServiceItem(DataTreePath dataTreePath) throws SaadaException {
		this.capability = new Capabilities();
		this.capability.setProtocol("TAP");
		this.capability.setDataTreePath(dataTreePath.toString());
		this.setDefaultDescription();
		this.setUI();
	}
	protected TapServiceItem(Capabilities capability) throws SaadaException {
		this.capability = capability;
		this.setDefaultDescription();
		this.setUI();
	}
	private void setDefaultDescription() throws FatalException {
		if( this.capability.getDescription() == null || this.capability.getDescription().length() == 0 ) {
			this.capability.setDescription(
					Database.getCachemeta().getCollection(this.capability.getDataTreePath().split("\\.")[0]).getDescription());
		}
	}
	private void setUI() {
		this.button.setToolTipText("(Un)Mark as to remove");
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( !removed ) {
					button.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/redLight.png")));
					label.setForeground(Color.GRAY);
					TapServiceItem.this.setBackground(Color.LIGHT_GRAY);	
					if( getBackground() == SELECTED ) ((TapServiceList)(getParent())).unSelectAll();
					removed = true;
				} else {
					button.setIcon(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/greenLight.png")));
					label.setForeground(Color.BLACK);
					TapServiceItem.this.setBackground(UNSELECTED);		
					removed = false;

				}
			}		
		} );
		this.add(this.button);
		this.label.setText(capability.getDataTreePath());
		this.label.setOpaque(false);
		this.add(this.label);
		this.setBackground(UNSELECTED);
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.addMouseListener(new MouseListener() {		
			public void mouseReleased(MouseEvent arg0) {
				if( !removed ) {
					((TapServiceList)(getParent())).unSelectAll();
					TapServiceItem.this.select();
				}
			}		
			public void mousePressed(MouseEvent arg0) {}			
			public void mouseExited(MouseEvent arg0) {}		
			public void mouseEntered(MouseEvent arg0) {}	
			public void mouseClicked(MouseEvent arg0) {}
		});
	}
	protected String getDataTreePath() {
		return this.capability.getDataTreePath();
	}
	public boolean fireAddResource(DataTreePath dataTreePath) throws SaadaException {
		return ((TapServiceList)(this.getParent())).addResource(dataTreePath, this);
	}
	public void select() {
		this.setBackground(SELECTED);
		this.label.setForeground(Color.WHITE);
		((TapServiceList)(this.getParent())).setDescription(this.capability.getDescription());
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
			this.capability.setDescription( ((TapServiceList)(this.getParent())).getDescription());
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
