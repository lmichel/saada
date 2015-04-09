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
import saadadb.admintool.components.AdminComponent;
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
import saadadb.vo.tap_old.TapServiceManager;
import saadadb.vocabulary.enums.VoProtocol;


/**
 * @author laurentmichel
 *
 */
@SuppressWarnings("serial")
public class SAPServicePanel extends EditPanel {
	private SaveButton saveButton = new SaveButton(this);
	private VOServiceItemSelector itemSelector;
	private Authority authority;
	private String protocolID;
	private VoProtocol capName;
	private int[] allowedCategories;

	/**
	 * @param rootFrame
	 * @param ancestor
	 */
	public SAPServicePanel(AdminTool rootFrame, String ancestor, String protocolID) {
		super(rootFrame, protocolID, null, ancestor, protocolID);
		this.protocolID = protocolID;
		if( this.protocolID.equals(AdminComponent.SIA_PUBLISH)) {
			this.capName = VoProtocol.SIA;
			this.allowedCategories = new int[]{Category.IMAGE, Category.SPECTRUM};			
		} else if( this.protocolID.equals(AdminComponent.SSA_PUBLISH)) {
			this.capName = VoProtocol.SSA;
			this.allowedCategories = new int[]{Category.IMAGE, Category.SPECTRUM};	
		} else  if( this.protocolID.equals(AdminComponent.CONESEARCH_PUBLISH)) {
			this.capName = VoProtocol.ConeSearch;
			this.allowedCategories = new int[]{Category.ENTRY,Category.IMAGE, Category.SPECTRUM};	
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setParam(java.lang.Object)
	 */
	protected void setParam(Object param) {
		this.protocolID = param.toString();;
		if( this.protocolID.equals(AdminComponent.SIA_PUBLISH)) {
			this.capName = VoProtocol.SIA;
			this.allowedCategories = new int[]{Category.IMAGE, Category.SPECTRUM};			
		} else if( this.protocolID.equals(AdminComponent.SSA_PUBLISH)) {
			this.capName = VoProtocol.SSA;
			this.allowedCategories = new int[]{Category.IMAGE, Category.SPECTRUM};	
		} else  if( this.protocolID.equals(AdminComponent.CONESEARCH_PUBLISH)) {
			this.capName = VoProtocol.ConeSearch;
			this.allowedCategories = new int[]{Category.ENTRY,Category.IMAGE, Category.SPECTRUM};	
		}
	}
	
	@Override
	protected void setToolBar() {
		this.initTreePathPanel();
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
			itemSelector = new VOServiceItemSelector(this,capName, allowedCategories);
		} catch (Exception e) {
			showFatalError(rootFrame, e);
		}
		JPanel tPanel = this.addSubPanel("Published " + capName + " tables");

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(itemSelector), imcep);

		JButton b = new JButton("Empty " + capName + " Service");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( showConfirmDialog(rootFrame, "Do you want to empty your " + capName + " service (no data is removed from the DB)?") ) {
					itemSelector.reset();
					try {
						SQLTable.beginTransaction();
						Table_Saada_VO_Capabilities.emptyTable(VoProtocol.SIA);
						SQLTable.commitTransaction();
						showInfo(rootFrame, capName + "emptied");
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
					Table_Saada_VO_Capabilities.emptyTable(capName);
					itemSelector.saveCapabilities();
					SQLTable.commitTransaction();
					itemSelector.loadCapabilities();
					showSuccess(SAPServicePanel.this.rootFrame, capName + "capabilities saved");
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
