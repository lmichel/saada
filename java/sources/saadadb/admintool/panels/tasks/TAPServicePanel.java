package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadSaveTAPCapabilities;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.XMLButton;
import saadadb.admintool.components.voresources.VOServiceItemSelector;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.MyGBC;
import saadadb.admintool.windows.TextSaver;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.util.Messenger;
import saadadb.vo.registry.Authority;
import saadadb.vo.registry.Capability;
import saadadb.vo.registry.Record;
import saadadb.vo.tap_old.TapServiceManager;


public class TAPServicePanel extends TaskPanel {
	private static final long serialVersionUID = 1L;
	private RunTaskButton runButton;
	private XMLButton xmlButton;
	private VOServiceItemSelector itemSelector;

	public TAPServicePanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, TAP_PUBLISH, null, ancestor);
		initCmdThread();
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#initCmdThread()
	 */
	public void initCmdThread() {
		cmdThread = new ThreadSaveTAPCapabilities(rootFrame, TAP_PUBLISH);
	}

	@Override
	protected Map<String, Object> getParamMap() {
		if( itemSelector.getResourceList() != null ) {
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			map.put("itemSelector", itemSelector);
			return map;
		}
		else {
			return null;	
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.TaskPanel#setToolBar()
	 */
	@Override
	protected void setToolBar() {
		this.initTreePathPanel();
		this.initSelectResourceLabel();
		this.add(new ToolBarPanel(this, false, false, false));		
	}


	@Override
	protected void setActivePanel() {
		try {
			itemSelector = new VOServiceItemSelector(this, Capability.TAP, null);
			itemSelector.loadCapabilities();
			Authority.load();
		} catch (Exception e) {
			showFatalError(rootFrame, e);
		}
		JPanel tPanel = this.addSubPanel("Published TAP tables");

		MyGBC imcep = new MyGBC(0,0,0,0);
		imcep.reset(5,5,5,5);imcep.weightx = 1;imcep.weighty = 1;imcep.fill = GridBagConstraints.BOTH;
		tPanel.add(new JScrollPane(itemSelector), imcep);

		JButton b = new JButton("Drop TAP service");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if( showConfirmDialog(rootFrame, "Do you want to empty your TAP service (ObsCore table won't be removed)?") ) {
					TapServiceManager tsm = new TapServiceManager();					
					itemSelector.reset();
					try {
						SQLTable.beginTransaction();
						Table_Saada_VO_Capabilities.emptyTable(Capability.TAP);
						tsm.remove(new ArgsParser(new String[]{"-remove=service"}));
						SQLTable.commitTransaction();
						showInfo(rootFrame, "Tap Service removed");
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
		this.runButton = new RunTaskButton(this);
		this.runButton.activate();
		this.xmlButton = new XMLButton(this, new Runnable() {
			public void run() {
				try {
					(new TextSaver(
							TAPServicePanel.this.rootFrame
							, "TAP Registry Record"
							, Database.getDbname() + "_TAPRegistry.xml"
							,(new Record()).getTAPRecord().toString())).open(SQLJTable.DMVIEW_PANEL);
				} catch (QueryException e) {
					Messenger.trapQueryException(e);
				}

			}			
		});
		
		this.setActionBar(new Component[]{runButton
				, debugButton
				, xmlButton});

		}
	
	public void activate(boolean active){
		if( this.runButton != null )
			if(active) this.runButton.activate(); else this.runButton.inactivate();		
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {		
		this.activate(true);

//		if( itemSelector != null ) {
//			try {
//				itemSelector.loadCapabilities();
//				Authority.load();
//			} catch (Exception e) {
//				showFatalError(rootFrame, e);
//			}
//		}
	}
	

//	protected void setActionBarORG() {
//		this.saveButton = new SaveButton(this);
//		this.saveButton.setEnabled(true);
//		this.saveButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				//				if( tapSelector.isEmpty() ) {
//				//					showInputError(rootFrame, "No data collection selected" );
//				//					return;
//				//				}
//				TapServiceManager tsm = new TapServiceManager();
//				try {
//					SQLTable.beginTransaction();
//					Table_Saada_VO_Capabilities.emptyTable(Capability.TAP);
//					itemSelector.saveCapabilities();
//					SQLTable.commitTransaction();
//					
//					SQLTable.beginTransaction();
//					tsm.removeAllTables();
//					SQLTable.commitTransaction();
//					
//					tsm.synchronizeWithGlobalCapabilities();
//					
//					Messenger.printMsg(Messenger.TRACE, "Add selected tables to TAP service");
//					itemSelector.loadCapabilities();
//					showSuccess(TAPServicePanel.this.rootFrame, "Exposed tables saved");
//				} catch (SaadaException e1) {
//					SQLTable.abortTransaction();
//					if( e1.getMessage().equals(SaadaException.MISSING_RESOURCE)) {
//						if( showConfirmDialog(rootFrame, "No TAP service detected. Do you want to create it?") ) {
//							try {
//								SQLTable.beginTransaction();
//								Messenger.printMsg(Messenger.TRACE, "Create TAP service");
//								tsm.create(null);
//								SQLTable.commitTransaction();
//								
//								Messenger.printMsg(Messenger.TRACE, "Add selected tables to TAP service");
//								tsm .synchronizeWithGlobalCapabilities();
//								
//								itemSelector.loadCapabilities();
//								showSuccess(TAPServicePanel.this.rootFrame, "Exposed tables saved");
//							} catch (Exception e) {
//								SQLTable.abortTransaction();
//								showFatalError(rootFrame, e);
//							}
//						}
//
//					} else {
//						showFatalError(rootFrame, e1);
//					}
//				} catch (Exception e) {
//					SQLTable.abortTransaction();
//					showFatalError(rootFrame, e);
//				}				
//			}			
//		});
//		JPanel tPanel = new JPanel();
//		tPanel.setLayout(new GridBagLayout());
//		tPanel.setBackground(LIGHTBACKGROUND);
//		tPanel.setPreferredSize(new Dimension(1000,48));
//		tPanel.setMaximumSize(new Dimension(1000,48));
//		GridBagConstraints c = new GridBagConstraints();
//		c.gridy = 0; c.gridx = 0;
//		c.anchor = GridBagConstraints.PAGE_END;
//		c.weightx = 0;
//		c.weighty = 0;
//		c.fill = GridBagConstraints.NONE;
//		tPanel.add(saveButton, c);
//		c.gridx++;	
//		XMLButton xmlButton = new XMLButton(this, new Runnable() {
//			public void run() {
//				try {
//					(new TextSaver(
//							rootFrame
//							, "TAP Registry Record"
//							, Database.getDbname() + "_TAPRegistry.xml"
//							,(new Record()).getTAPRecord().toString())).open(SQLJTable.DMVIEW_PANEL);
//				} catch (QueryException e) {
//					Messenger.trapQueryException(e);
//				}
//
//			}			
//		});
//		xmlButton.setToolTipText("Show the registry record of the TAP service");
//		tPanel.add(xmlButton, c);
//		c.gridx++;		
//		/*
//		 * Just to push all previous components to the left
//		 */
//		c.weightx = 1;
//		tPanel.add(new JLabel(" "), c);
//		this.add(tPanel);	
//	}
}
