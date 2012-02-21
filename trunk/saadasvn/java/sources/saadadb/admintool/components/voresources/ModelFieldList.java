package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.dnd.ProductTreePathTransferHandler;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Capabilities;


public class ModelFieldList extends JPanel {
	private ModelViewPanel modelViewPanel;

	protected  ArrayList<ModelFiedlEditor> editors = new ArrayList<ModelFiedlEditor>();
	protected  ArrayList<FieldItem> items = new ArrayList<FieldItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws SaadaException 
	 */
	public ModelFieldList(ModelViewPanel modelViewPanel) throws SaadaException {
		this.modelViewPanel = modelViewPanel;
		this.setTransferHandler(new ProductTreePathTransferHandler(3));	
		this.setLayout(new GridBagLayout());
		this.displayListItems();
	}


	protected void unSelectAll() {System.out.println("AAAAAA");
	for( FieldItem tsi: items) {
		tsi.unSelect();
	}
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	void displayListItems() throws SaadaException {
		this.removeAll();
		MyGBC gbc = new MyGBC(0, 0, 0, 0);
		for( UTypeHandler uth: modelViewPanel.getVor().getUTypeHandlers() ) {
			ModelFiedlEditor tsi = new ModelFiedlEditor(null, uth);
			editors.add(tsi);
			FieldItem fi = new FieldItem(uth);
			items.add(fi);
			fi.setAlignmentX(Component.LEFT_ALIGNMENT);
			gbc.left(true);gbc.rowEnd();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor =GridBagConstraints.FIRST_LINE_START;
			gbc.weighty = 0;
			gbc.weightx = 1;
			this.add(fi, gbc);
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
		//		TapServiceItem toRemove= null;
		//		for( TapServiceItem tsi: items) {
		//			if( dataTreePath.toString().equals(tsi.getDataTreePath())) {
		//				toRemove = tsi;
		//			}
		//		}
		//		items.remove(toRemove);
		//
		//		displayListItems();
	}

	protected String getMapping() {
		// TODO Auto-generated method stub
		return null;
	}


	protected void storeCurrentMapping() {
		for( FieldItem it: items) {
			if( it.selected ) {
				it.mappingStmt = this.modelViewPanel.mapField.getText();
			}
		}	
	}


	public class FieldItem extends JPanel {
		public  final Color SELECTED = AdminComponent.OLD_HEADER;
		public  final Color UNSELECTED = Color.WHITE;
		protected UTypeHandler uth;
		private JButton button = new JButton(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("icons/greenLight.png")));
		private JLabel label = new JLabel();
		private boolean selected = false;
		private String mappingStmt;


		protected FieldItem(UTypeHandler uth) throws SaadaException {
			this.uth = uth;
			this.setUI();
		}

		private void setUI() {
			this.button.setToolTipText("(Un)Mark as to remove");
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			this.button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
				}		
			} );
			this.add(this.button);
			Border border;
			String label;

			switch( uth.getRequ_level()) {
			case UTypeHandler.MANDATORY: 
				label = uth.getNickname() + " (MAN)";
				border = BorderFactory.createLineBorder(Color.BLACK);
				break;
			case UTypeHandler.RECOMMENDED: 				
				label = uth.getNickname() + " (MAN)";
				border = BorderFactory.createLineBorder(Color.DARK_GRAY);
				break;
			default:
				label = uth.getNickname() + " (MAN)";
				border = BorderFactory.createLineBorder(Color.GRAY);
			}
			this.setBorder(border);
			this.label.setText(label);
			this.label.setOpaque(false);
			this.add(this.label);
			this.setBackground(UNSELECTED);
			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
			this.setTransferHandler(new ProductTreePathTransferHandler(3));	
			this.addMouseListener(new MouseListener() {		
				public void mouseReleased(MouseEvent arg0) {
					ModelFieldList mfl= ((ModelFieldList)(getParent()));
					mfl.storeCurrentMapping();
					mfl.modelViewPanel.setUtypeHandler(uth, mappingStmt);
					mfl.unSelectAll();
					select();
				}		
				public void mousePressed(MouseEvent arg0) {}			
				public void mouseExited(MouseEvent arg0) {}		
				public void mouseEntered(MouseEvent arg0) {}	
				public void mouseClicked(MouseEvent arg0) {}
			});
			//		           for(Iterator i = UIManager.getDefaults().entrySet().iterator(); i.hasNext(); ) {
			//			                Map.Entry next = (Map.Entry)i.next();
			//			                System.out.println(next.getKey());
			//		           }
		}

		public void select() {
			this.setBackground(SELECTED);
			this.label.setForeground(Color.WHITE);
			//((TapServiceList)(this.getParent())).setDescription(this.capability.getDescription());
			this.selected = true;
		}
		public void unSelect() {
				this.setBackground(UNSELECTED);
				this.label.setForeground(Color.BLACK);
			if( this.selected) {
				//this.capability.setDescription( ((TapServiceList)(this.getParent())).getDescription());
			}
			this.selected = false;
		}
		public boolean isSelected() {
			return selected;
		}
	}
}
