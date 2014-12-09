/**
 * 
 */
package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Label;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadIndexTable;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.AntButton;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.sqltable.SQLQuery;


/**
 * @author laurentmichel
 *
 */
public class SQLIndexPanel extends TaskPanel {
	private String sqlTable;
	private JRadioButton indexAll ;
	private JRadioButton indexSel;
	private JXTable jtable;
	private ButtonGroup actionGroup;
	private JRadioButton dropAll ;
	private JRadioButton dropSel;
	private ArrayList<Object[]> table;
	protected RunTaskButton runButton;
	private JScrollPane columnPanel;
	private JPanel description;

	/**
	 * @param rootFrame
	 * @param ancestor
	 */
	public SQLIndexPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, SQL_INDEX, null, ancestor);
		cmdThread = new ThreadIndexTable(this, INDEX_RELATION);
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private void setDescription() throws Exception {
		description.removeAll();
		if( sqlTable == null ) {
			description.add(getHelpLabel(HelpDesk.SQL_INDEX));
		} else {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("select count(oidsaada) from (select oidsaada from " + sqlTable + " Limit 10000) as js") ;
			int size = 0;
			String limit = "";
			runButton.activate();
			while(rs.next()) {
				size = rs.getInt(1);
				if( size < 1000 ) {
					limit = "<BR>Table with less than 1000 rows are not indexed";
					runButton.inactivate();
					//					indexAll.setEnabled(false);
					//					indexAll.setEnabled(false);
				}
				else if( size >= 10000) {
					limit = "(truncated)"; 
				}
			}
			squery.close();
			int nbi=0;
			for( int r=0 ; r<jtable.getRowCount() ; r++) {
				if( !jtable.getModel().getValueAt(r, 2).toString().equals("-") ) {
					nbi++;
				}
			}
			String retour = "<HTML>Table " + sqlTable + "<BR>" + size + " rows " + limit + "<BR>" + jtable.getRowCount() + " columns(" +  nbi + " indexed)<BR>";
			description.add(getPlainLabel(retour));
		}
	}

	/**
	 * @throws Exception
	 */
	public void buildColumnTable() throws Exception {		
		DatabaseConnection connection = Database.getConnection();
		ResultSet rs = Database.getWrapper().getTableColumns(connection, sqlTable);
		Collection<String> ci = Database.getWrapper().getExistingIndex(connection, sqlTable).values();
		table = new ArrayList<Object[]>();
		int r=0 ;
		while(rs.next() ) {
			String col_name = rs.getString("COLUMN_NAME");
			Object[] lr = (new Object[3]);
			table.add(lr);
			lr[0] = col_name;
			lr[1] = rs.getString("TYPE_NAME").toLowerCase();
			if( ci.contains(col_name)) {
				lr[2] = "indexed";
			}
			else {
				lr[2] = "-";
			}
			r++;
		}
		Database.giveConnection(connection);
		jtable = new JXTable();
		jtable.setModel(new AbstractTableModel() {
			public Object getValueAt(int row, int col) {
				return table.get(row)[col];
			}
			@Override
			public boolean isCellEditable(int row, int col){ 
				return false; 
			}
			@Override
			public void setValueAt(Object value, int row, int col) {
				table.get(row)[col] = value;
				fireTableCellUpdated(row, col);
			}
			public int getColumnCount() {
				return 3;
			}
			public int getRowCount() {
				return table.size();
			}
		});
		jtable.getColumnModel().getColumn(0).setHeaderValue("Column Name");
		jtable.getColumnModel().getColumn(1).setHeaderValue("Data Type");
		jtable.getColumnModel().getColumn(2).setHeaderValue("Indexed");
		jtable.setBackground(AdminComponent.IVORY);
		columnPanel.setViewportView(jtable);
		jtable.setBackground(AdminComponent.LIGHTBACKGROUND);
		jtable.setHighlighters(HighlighterFactory.createSimpleStriping());
		updateUI();
	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadIndexTable(this, INDEX_RELATION);
	}

	@Override
	protected Map<String, Object> getParamMap() {		
		if( jtable != null ) {
			LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();
			if( dropAll.isSelected() || dropSel.isSelected()) {
				retour.put("remove", true ) ;
			} else {
				retour.put("remove", false ) ;			
			}
			retour.put("table", sqlTable);
			if( !dropAll.isSelected() && !indexAll.isSelected() ) {
				ArrayList<String> scols = new ArrayList<String>();
				int[] cs = jtable.getSelectedRows();
				for( int c=0 ; c<cs.length ; c++ ) {
					scols.add(jtable.getValueAt(cs[c], 0).toString());
				}
				retour.put("columns", scols);			
			}
			return retour;
		}
		return null;
	}

	@Override
	protected void setToolBar() {
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));		
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a collection, a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) {
		if( dataTreePath != null ) {
			try {
				if( dataTreePath.isRootOrCollectionLevel()) {
					//showInputError(rootFrame, "Selected node must be either at category (IMAGE, ...) or at class level (leaf)");
					return;
				} else {
					sqlTable = dataTreePath.getSQLTableName();
				} 
				super.setDataTreePath(dataTreePath);
				this.buildColumnTable();
				this.setDescription();
			} catch (Exception e) {
				showFatalError(rootFrame, e);
			}
		}
	}

	@Override
	protected void setActivePanel() {
		runButton   = new RunTaskButton(this);
		indexAll    = new JRadioButton("All Columns");
		indexAll.setBackground(AdminComponent.LIGHTBACKGROUND);
		indexSel    = new JRadioButton("Selected Columns");
		indexSel.setBackground(AdminComponent.LIGHTBACKGROUND);
		actionGroup = new ButtonGroup();
		
		dropAll     = new JRadioButton("All Columns");
		dropAll.setBackground(AdminComponent.LIGHTBACKGROUND);
		dropSel     = new JRadioButton("Selected Columns");
		dropSel.setBackground(AdminComponent.LIGHTBACKGROUND);
		
		columnPanel = new JScrollPane(getPlainLabel("No table selected"));
		columnPanel.setPreferredSize(new Dimension(500, 400));
		description = new JPanel();
		description.setBackground(LIGHTBACKGROUND);
		try {
			this.setDescription();
			JPanel tPanel = this.addSubPanel("Column Selector");
			GridBagConstraints ccs = new GridBagConstraints();
			ccs.gridx = 0; ccs.gridy = 0;ccs.anchor = GridBagConstraints.WEST;
			ccs.weightx = 0.5;ccs.weighty = 0.5;ccs.ipadx = 50;ccs.ipady = 10;
			ccs.gridwidth = 4;
			tPanel.add(description, ccs);
			ccs.ipadx = 5;ccs.ipady = 5;
			ccs.gridx = 0; ccs.gridy++;ccs.anchor = GridBagConstraints.CENTER;
			tPanel.add(columnPanel, ccs);
			/*
			 * Bottom pane
			 */
			ccs.gridy++; ccs.gridwidth = 1;
			JLabel jlb = new JLabel("Drop Indexes On", Label.LEFT);
			ccs.gridx = 0; ccs.anchor = GridBagConstraints.EAST;
			tPanel.add(jlb, ccs);
			ccs.gridx++;ccs.anchor = GridBagConstraints.WEST;
			tPanel.add(dropAll, ccs);
			ccs.gridx++; 
			tPanel.add(dropSel, ccs);
			jlb = new JLabel("Build Indexes On", Label.LEFT);
			ccs.gridx = 0; ccs.gridy++; ccs.anchor = GridBagConstraints.EAST;
			tPanel.add(jlb, ccs);	
			ccs.gridx++; ccs.anchor = GridBagConstraints.WEST;
			tPanel.add(indexAll, ccs);
			ccs.gridx++; 
			tPanel.add(indexSel, ccs);
			//Group the radio buttons.
			actionGroup.add(dropAll);
			actionGroup.add(dropSel);
			actionGroup.add(indexSel);
			actionGroup.add(indexAll);
		} catch (Exception e) {
			showFatalError(rootFrame, e);
		}
		this.setActionBar(new Component[]{runButton, debugButton, (new AntButton(this))});
	}

}
