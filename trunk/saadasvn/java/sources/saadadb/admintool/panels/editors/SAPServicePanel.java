/**
 * 
 */
package saadadb.admintool.panels.editors;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.SaveButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.voresources.VOServiceItemSelector;
import saadadb.admintool.panels.EditPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.collection.Category;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.vo.registry.Authority;
import saadadb.vo.registry.Capability;
import saadadb.vo.tap.TapServiceManager;


/**
 * @author laurentmichel
 *
 */
@SuppressWarnings("serial")
public class SAPServicePanel extends EditPanel {
	private SaveButton saveButton = new SaveButton(this);
	private VOServiceItemSelector itemSelector;
	private Authority authority;

	/**
	 * @param rootFrame
	 * @param ancestor
	 */
	public SAPServicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, SIA_PUBLISH, null, ancestor);
	}

	@Override
	protected void setToolBar() {
		this.initTreePathLabel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));		
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( itemSelector != null ) {
			try {
				itemSelector.loadCapabilities();
				Authority.load();
			} catch (Exception e) {
				showFatalError(rootFrame, e);
			}
		}
	}

	@Override
	protected void setActivePanel() {
		try {
			itemSelector = new VOServiceItemSelector(this, Capability.SIA, new int[]{Category.IMAGE, Category.SPECTRUM});
		} catch (Exception e) {
			showFatalError(rootFrame, e);
		}
		JPanel tPanel = this.addSubPanel("Published SIA tables");

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(itemSelector), imcep);

		JButton b = new JButton("Empty SIA service");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( showConfirmDialog(rootFrame, "Do you want to empty your SIA service (no data is removed from the DB)?") ) {
					itemSelector.reset();
					try {
						SQLTable.beginTransaction();
						Table_Saada_VO_Capabilities.emptyTable(Capability.SIA);
						SQLTable.commitTransaction();
						showInfo(rootFrame, "SIA service empty");
					} catch (SaadaException e) {
						SQLTable.abortTransaction();
						showFatalError(rootFrame, e);
					}
				}
			}
		});
		imcep.weightx = 0;imcep.weighty = 0;imcep.fill = GridBagConstraints.NONE;
		imcep.newRow();
		tPanel.add(b, imcep);
		
		this.setActionBar();
	}

	@Override
	public void active() {
	}

	protected void setActionBar() {
		this.saveButton = new SaveButton(this);
		this.saveButton.setEnabled(true);
		this.saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				TapServiceManager tsm = new TapServiceManager();
				try {
					SQLTable.beginTransaction();
					Table_Saada_VO_Capabilities.emptyTable(Capability.SIA);
					itemSelector.saveCapabilities();
					SQLTable.commitTransaction();
					itemSelector.loadCapabilities();
					showSuccess(SAPServicePanel.this.rootFrame, "SIA capabilities saved");
				} catch (Exception e) {
					SQLTable.abortTransaction();
					showFatalError(rootFrame, e);
				}				
			}			
		});
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		tPanel.setBackground(LIGHTBACKGROUND);
		tPanel.setPreferredSize(new Dimension(1000,48));
		tPanel.setMaximumSize(new Dimension(1000,48));
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 0; c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		tPanel.add(saveButton, c);
		c.gridx++;	
//		XMLButton xmlButton = new XMLButton(this, new Runnable() {
//			public void run() {
//				try {
//					(new TextSaver(
//							rootFrame
//							, "TAP Registry Record"
//							, Database.getDbname() + "_TAPRegistry.xml"
//							,(new Record()).getTAPRecord().toString())).open();
//				} catch (QueryException e) {
//					Messenger.trapQueryException(e);
//				}
//
//			}			
//		});
//		xmlButton.setToolTipText("Show the registry record of the TAP service");
//		tPanel.add(xmlButton, c);
		c.gridx++;		
		/*
		 * Just to push all previous components to the left
		 */
		c.weightx = 1;
		tPanel.add(new JLabel(" "), c);
		this.add(tPanel);	
	}
}
