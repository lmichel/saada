/**
 * 
 */
package saadadb.admintool.components.voresources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.panels.AdminPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.windows.MappedTableWindow;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.MetaClass;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_DMVIew;

/**
 * @author michel
 * @version $Id$
 *
 */
@SuppressWarnings("serial")
public class ObsTapComponentList extends JPanel {
	private AdminPanel adminPanel;
	private VOResource vor;
	protected  ArrayList< ObsTapItem> items = new ArrayList< ObsTapItem>();

	/**
	 * @param taskPanel
	 * @param toActive
	 * @throws Exception 
	 */
	public ObsTapComponentList(AdminPanel adminPanel, VOResource vor) throws Exception {
		this.adminPanel = adminPanel;
		this.vor = vor;
		this.setPreferredSize(new Dimension(300, 300));
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		this.displayListItems(); 
	}

	/**
	 * @throws Exception 
	 * @throws FatalException 
	 * 
	 */
	public void displayListItems() throws Exception {
		this.removeAll();
		items = new ArrayList< ObsTapItem>();
		for( String cl: Table_Saada_VO_DMVIew.getReferencedClasses(vor) ) {
			MetaClass mc = Database.getCachemeta().getClass(cl);
			DataTreePath dataTreePath = new DataTreePath(mc.getCollection_name(), mc.getCategory_name(), mc.getName());
			this.items.add(new ObsTapItem(dataTreePath));
		}
		
		JPanel jp = new JPanel();
		jp.setLayout(new GridBagLayout());
		MyGBC gbc = new MyGBC(0, 0, 0, 0);

		for( ObsTapItem tsi: items) {
			tsi.setAlignmentX(Component.LEFT_ALIGNMENT);
			gbc.left(true);gbc.rowEnd();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor =GridBagConstraints.NORTH;
			gbc.weighty = 0;
			gbc.weightx = 1;
			jp.add(tsi, gbc);
			gbc.newRow();
		}
		this.add(jp);
		//jp.setPreferredSize(new Dimension(0,0));
		this.add(AdminPanel.getHelpLabel(HelpDesk.OBSTAP_COMPONENT));
		updateUI();
	}
	/**
	 * @param dataTreePath
	 */
	protected void removeComponent(DataTreePath dataTreePath) throws Exception{
		SQLTable.beginTransaction();
		Table_Saada_VO_DMVIew.removeClass(vor, dataTreePath.classe);
		SQLTable.commitTransaction();
	}


	/**
	 * @author michel
	 * @version $Id$
	 *
	 */
	public class ObsTapItem extends JPanel {
		public  final Color SELECTED = AdminComponent.OLD_HEADER;
		public  final Color UNSELECTED = Color.WHITE;
		private JCheckBox rmButton = new JCheckBox();
		private JButton lookButton = new JButton("Look");
		private JLabel label = new JLabel();
		private DataTreePath dataTreePath;

		ObsTapItem(DataTreePath dataTreePath) {
			this.dataTreePath = dataTreePath;
			setUI();
		}
		private void setUI() {
			this.rmButton.setToolTipText("Remove class " + dataTreePath.classe + " from " + ObsTapComponentList.this.vor.getName() + " table" );
			this.lookButton.setToolTipText("Show " + ObsTapComponentList.this.vor.getName() + " table subset");
			this.rmButton.setSelected(true);
			this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
			this.rmButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if( !rmButton.isSelected() ) {
						label.setForeground(Color.GRAY);
						ObsTapItem.this.setBackground(Color.LIGHT_GRAY);	
						if( AdminComponent.showConfirmDialog(getParent(), "Do you want to remove the class " + dataTreePath + " from th view")) {
							try {
								ObsTapComponentList.this.removeComponent(ObsTapItem.this.dataTreePath);		
								ObsTapComponentList.this.displayListItems();
							} catch (Exception e) {
								AdminComponent.showFatalError(getParent(), e);
							}
						} else {
							label.setForeground(Color.BLACK);
							ObsTapItem.this.setBackground(UNSELECTED);									
							ObsTapItem.this.rmButton.setSelected(true);
						}
					} else {
						label.setForeground(Color.BLACK);
						ObsTapItem.this.setBackground(UNSELECTED);		
					}
				}		
			} );
			this.lookButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						MappedTableWindow dtw = new MappedTableWindow(ObsTapComponentList.this.adminPanel.rootFrame
								, Table_Saada_VO_DMVIew.getClassDataQuery(ObsTapComponentList.this.vor, dataTreePath.classe));
						dtw.open(SQLJTable.DMVIEW_PANEL);
					} catch( Exception e) {
						AdminComponent.showFatalError(getParent(), e);
					}
				}			
			});
			this.add(this.rmButton);
			this.add(this.lookButton);
			this.label.setText(dataTreePath.toString());
			this.label.setOpaque(false);
			this.add(Box.createHorizontalStrut(10));
			this.add(this.label);
			this.setBackground(UNSELECTED);
			this.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		}

		public DataTreePath getDataTreePath() {
			return dataTreePath;
		}

	}
}

