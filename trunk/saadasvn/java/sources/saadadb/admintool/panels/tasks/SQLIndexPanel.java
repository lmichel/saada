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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admintool.AdminTool;
import saadadb.admintool.cmdthread.ThreadRelationIndex;
import saadadb.admintool.components.RunTaskButton;
import saadadb.admintool.panels.TaskPanel;
import saadadb.database.Database;
import saadadb.sqltable.SQLQuery;


/**
 * @author laurentmichel
 *
 */
public class SQLIndexPanel extends TaskPanel {
	private String sqlTable;
	private JRadioButton indexAll ;
	private JRadioButton indexSel;
	private JTable jtable;
	private JButton cancelBtn ;
	private JButton applyBtn ;
	private ButtonGroup actionGroup;
	private JRadioButton dropAll ;
	private JRadioButton dropSel;
	private ArrayList<Object[]> table;
	protected RunTaskButton runButton;



	public SQLIndexPanel(AdminTool rootFrame, String ancestor) {
		super(rootFrame, SQL_INDEX, null, ancestor);
	}

	private String getDescription() throws Exception {
		if( sqlTable == null ) {
			return "No table Set";
		}
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select count(oidsaada) from (select oidsaada from " + sqlTable + " Limit 10000) as js") ;
		int size = 0;
		String limit = "";
		while(rs.next()) {
			size = rs.getInt(1);
			if( size < 1000 ) {
				limit = "<BR>Table with less than 1000 rows are not indexed";
				indexAll.setEnabled(false);
				indexAll.setEnabled(false);
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
		return retour;
	}

	private JScrollPane buildColumnTable() throws Exception {		
		if( sqlTable == null ) {
			return new JScrollPane();
		}

		ResultSet rs = Database.getWrapper().getTableColumns(sqlTable);
		//rs.last();
		Collection<String> ci = Database.getWrapper().getExistingIndex(sqlTable).values();
		table = new ArrayList<Object[]>();
		//table = new Object[rs.getRow()][3];
		//rs.beforeFirst();
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
		rs.close();
		jtable = new JTable();
		jtable.setPreferredScrollableViewportSize(new Dimension(300,300));
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
		jtable.setBackground(SaadaDBAdmin.beige_color);
		return new JScrollPane(jtable);

	}

	@Override
	public void initCmdThread() {
		cmdThread = new ThreadRelationIndex(rootFrame, INDEX_RELATION);
	}


	@Override
	protected Map<String, Object> getParamMap() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void setActivePanel() {
		runButton = new RunTaskButton(this);
		indexAll   = new JRadioButton("All Columns");
		indexSel   = new JRadioButton("Selected Columns");

		indexAll   = new JRadioButton("All Columns");
		indexSel   = new JRadioButton("Selected Columns");
		cancelBtn       = new JButton("Cancel");
		applyBtn        = new JButton("Apply");
		actionGroup = new ButtonGroup();
		dropAll    = new JRadioButton("All Columns");
		dropSel    = new JRadioButton("Selected Columns");

		try {
			JPanel tPanel = this.addSubPanel("Column Selector");
			JScrollPane jsp = this.buildColumnTable();

			GridBagConstraints ccs = new GridBagConstraints();
			ccs.gridx = 0; ccs.gridy = 0;ccs.anchor = GridBagConstraints.WEST;
			ccs.weightx = 0.5;ccs.weighty = 0.5;ccs.ipadx = 50;ccs.ipady = 10;
			ccs.gridwidth = 4;
			JLabel label = SaadaDBAdmin.getPlainLabel(getDescription());
			tPanel.add(label, ccs);
			ccs.ipadx = 5;ccs.ipady = 5;
			ccs.gridx = 0; ccs.gridy++;ccs.anchor = GridBagConstraints.CENTER;
			tPanel.add(jsp, ccs);
			/*
			 * Bottom pane
			 */
			ccs.gridy++; ccs.gridwidth = 1;
			JLabel jlb = new JLabel("Drop Indexes On", Label.LEFT);
			ccs.gridx = 0; ccs.anchor = GridBagConstraints.EAST;
			tPanel.add(jlb, ccs);
			ccs.gridx++;ccs.anchor = GridBagConstraints.WEST;
			tPanel.add(dropSel, ccs);
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
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

			buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			buttonPane.add(applyBtn);
			buttonPane.add(Box.createRigidArea(new Dimension(10, 0)), 1);
			buttonPane.add(cancelBtn);

			ccs.gridy+= 1;ccs.ipady = 20;
			tPanel.add(buttonPane, ccs);	
		} catch (Exception e) {
			showFatalError(rootFrame, e);
		}
		this.setActionBar(new Component[]{runButton
				, debugButton});
	}

}
