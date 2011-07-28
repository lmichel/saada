package saadadb.admin.dialogs;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import saadadb.admin.SaadaDBAdmin;
import saadadb.admin.threads.BuildIndex;
import saadadb.admin.threads.DropIndex;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;


public class SQLIndexPanel extends JFrame {
	String sql_table;
	ArrayList<Object[]> table;
	JTable jtable;
	JButton cancel_btn       = new JButton("Cancel");
	JButton apply_btn        = new JButton("Apply");
    ButtonGroup action_group = new ButtonGroup();
	JRadioButton drop_all    = new JRadioButton("All Columns");
	JRadioButton drop_sel    = new JRadioButton("Selected Columns");
	JRadioButton index_all   = new JRadioButton("All Columns");
	JRadioButton index_sel   = new JRadioButton("Selected Columns");

	public SQLIndexPanel(Object[] tree_path_elements) throws FatalException {

		if( tree_path_elements.length == 4 ) {
			sql_table = tree_path_elements[3].toString();
		}
		else {
			sql_table = tree_path_elements[1].toString() + "_" + tree_path_elements[2].toString().toLowerCase();
		}
		init();
	}

	public SQLIndexPanel(String classe_name) throws FatalException {
		sql_table = classe_name;
		init();
	}

	private void init() throws FatalException {
		try {
			setTitle("Index Management Tools");
			getRootPane().setDefaultButton(cancel_btn);
			JScrollPane jsp = this.buildColumnTable();
			this.setBehavior();
			/*
			 * Top pane
			 */
			Container listPane = this.getContentPane();
			listPane.setLayout(new GridBagLayout());
			GridBagConstraints ccs = new GridBagConstraints();
			ccs.gridx = 0; ccs.gridy = 0;ccs.anchor = GridBagConstraints.WEST;
			ccs.weightx = 0.5;ccs.weighty = 0.5;ccs.ipadx = 50;ccs.ipady = 10;
			ccs.gridwidth = 4;
			JLabel label = SaadaDBAdmin.getPlainLabel(getDescription());
			listPane.add(label, ccs);
			ccs.ipadx = 5;ccs.ipady = 5;
			ccs.gridx = 0; ccs.gridy++;ccs.anchor = GridBagConstraints.CENTER;
			listPane.add(jsp, ccs);
			/*
			 * Bottom pane
			 */
			ccs.gridy++; ccs.gridwidth = 1;
			JLabel jlb = new JLabel("Drop Indexes On", Label.LEFT);
			ccs.gridx = 0; ccs.anchor = GridBagConstraints.EAST;
			listPane.add(jlb, ccs);
			ccs.gridx++;ccs.anchor = GridBagConstraints.WEST;
			listPane.add(drop_all, ccs);
			ccs.gridx++; 
			listPane.add(drop_sel, ccs);
			jlb = new JLabel("Build Indexes On", Label.LEFT);
			ccs.gridx = 0; ccs.gridy++; ccs.anchor = GridBagConstraints.EAST;
			listPane.add(jlb, ccs);	
			ccs.gridx++; ccs.anchor = GridBagConstraints.WEST;
			listPane.add(index_all, ccs);
			ccs.gridx++; 
			listPane.add(index_sel, ccs);
		    //Group the radio buttons.
		    action_group.add(drop_all);
		    action_group.add(drop_sel);
		    action_group.add(index_sel);
		    action_group.add(index_all);
			JPanel buttonPane = new JPanel();
	        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

			buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
			buttonPane.add(apply_btn);
			buttonPane.add(Box.createRigidArea(new Dimension(10, 0)), 1);
			buttonPane.add(cancel_btn);

			ccs.gridy+= 1;ccs.ipady = 20;
			listPane.add(buttonPane, ccs);
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException("Can not build index dialog", e);
		}
		pack();
		setLocationRelativeTo(this.getParent());
		setVisible(true);

	}

	private String getDescription() throws Exception {
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select count(oidsaada) from (select oidsaada from " + sql_table + " Limit 10000) as js") ;
		int size = 0;
		String limit = "";
		while(rs.next()) {
			size = rs.getInt(1);
			if( size < 1000 ) {
				limit = "<BR>Table with less than 1000 rows are not indexed";
				index_all.setEnabled(false);
				index_sel.setEnabled(false);
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
		String retour = "<HTML>Table " + sql_table + "<BR>" + size + " rows " + limit + "<BR>" + jtable.getRowCount() + " columns(" +  nbi + " indexed)<BR>";
		return retour;
	}
	private void setBehavior() {
		apply_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if( drop_all.isSelected() ) {
					DropIndex bi = new DropIndex(SQLIndexPanel.this, sql_table,  null);
					bi.setDaemon(true);
					bi.start();
					bi.setPriority(Thread.MIN_PRIORITY);					
				}
				else if( drop_sel.isSelected() ) {
					int[] sr = jtable.getSelectedRows();
					String[] cols = new String[sr.length];
					for( int i=0 ; i<sr.length ; i++ ) {
						cols[i] = jtable.getModel().getValueAt(sr[i], 0).toString();
					}
					DropIndex bi = new DropIndex(SQLIndexPanel.this, sql_table,  cols);
					bi.setDaemon(true);
					bi.start();
					bi.setPriority(Thread.MIN_PRIORITY);
				}
				else if( index_all.isSelected() ) {
					BuildIndex bi = new BuildIndex(SQLIndexPanel.this, sql_table, null);
					bi.setDaemon(true);
					bi.start();
					bi.setPriority(Thread.MIN_PRIORITY);					
				}
				else if( index_sel.isSelected() ) {
					int[] sr = jtable.getSelectedRows();
					String[] cols = new String[sr.length];
					for( int i=0 ; i<sr.length ; i++ ) {
						cols[i] = jtable.getModel().getValueAt(sr[i], 0).toString();
					}
					BuildIndex bi = new BuildIndex(SQLIndexPanel.this, sql_table,  cols);
					bi.setDaemon(true);
					bi.start();
					bi.setPriority(Thread.MIN_PRIORITY);
				}
			}
		});

		cancel_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
	}

	public void update() throws Exception{
		ResultSet rs = Database.getWrapper().getTableColumns(sql_table);
		//rs.last();
		Collection<String> ci = Database.getWrapper().getExistingIndex(sql_table).values();
		//table = new Object[rs.getRow()][3];
		table = new ArrayList<Object[]>();
		//rs.beforeFirst();
		int r=0 ;
		while(rs.next() ) {
			String col_name = rs.getString("COLUMN_NAME");
			Object[] lr = (new Object[3]);
			table.add(lr);
			lr[0] = col_name;
			lr[1] = rs.getString("TYPE_NAME").toLowerCase();
			if( ci.contains(col_name)) {
				jtable.getModel().setValueAt("indexed", r, 2);
			}
			else {
				jtable.getModel().setValueAt("-", r, 2);
			}
			r++;
		}	
		rs.close();
	}
	private JScrollPane buildColumnTable() throws Exception {
		ResultSet rs = Database.getWrapper().getTableColumns(sql_table);
		//rs.last();
		Collection<String> ci = Database.getWrapper().getExistingIndex(sql_table).values();
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

	public static void main(String[] args) throws Exception {
		Messenger.debug_mode = false;
		Database.init("DEVBENCH1_5_1");
		Database.getConnector().setAdminMode("");
		JFrame frame = new JFrame();
		Messenger.setGraphicMode(frame);
		//new ProgressDialog(frame, "Progress", null);
		SQLIndexPanel cd = new SQLIndexPanel("GrosFITSEntry");
		//	    cd.setVisible(true);
	}
}
