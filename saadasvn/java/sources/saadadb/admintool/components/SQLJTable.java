package saadadb.admintool.components;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import saadadb.admintool.AdminTool;
import saadadb.admintool.popups.PopupClassEdit;
import saadadb.admintool.popups.PopupCollEdit;
import saadadb.admintool.popups.PopupNode;
import saadadb.admintool.popups.PopupProduct;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.windows.DataTableWindow;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class SQLJTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int CLASS_PANEL = 0;
	public static final int COLL_PANEL = 1;
	public static final int PRODUCT_PANEL = 2;

	private final AdminTool rootFrame;
	private DataTreePath dataTreePath;
	private DataTableWindow jtable;
	private final int  panel_type;
	private final String sql;

	private boolean down_sorting = true;
	private static Triangle down_triangle = new Triangle(true);
	private static Triangle up_triangle = new Triangle(false);
	private static Triangle both_triangle = new Triangle();
	private  int order_column = -1;

	public SQLJTable(String sql, DataTableWindow jtable, int panel_type, boolean with_menu) throws QueryException {
		this.rootFrame = jtable.rootFrame;
		this.panel_type = panel_type;
		this.jtable = jtable;
		this.dataTreePath = jtable.getDataTreePath();;
		this.sql = sql;
		/*
		 * No action can be done simultaneoulsy on several class attributes
		 */
		if( this.panel_type ==  CLASS_PANEL) {
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.setDragEnabled(true);  
			/*
			 * done by PopupNode because the TransferHandler is not the same
			 * for UCD or UTYPES
			 */
			//this.setTransferHandler(new DnDUCDTransferHandler());			
		}
		else if( this.panel_type ==  COLL_PANEL) {
			this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		else {
			this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);                
			this.setRowSelectionAllowed(true);
			this.makeOrderingEnabled();
		}
		this.setModel(sql);
		this.setBackground(AdminComponent.LIGHTBACKGROUND);
		/*
		 * popup menus allowed
		 */
		if( with_menu ) {
			this.makPopupEnabled();
		}
	}

	public Component prepareRenderer(TableCellRenderer renderer,
			int rowIndex, int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if(isCellSelected(rowIndex, vColIndex))  {
			c.setForeground(Color.WHITE);
			c.setBackground(Color.GRAY);

		} else {
			c.setForeground(Color.BLACK);
			if (rowIndex % 2 == 0 ) {
				c.setBackground(AdminComponent.LIGHTBACKGROUND);
			} else {
				c.setBackground(Color.WHITE);
			}
		}
		return c;
	}


	/**
	 * 
	 */
	private void makPopupEnabled() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger() || e.getButton() == 3 /*&& SQLJTable.this.getSelectedRowCount() > 0*/ ) {
					PopupNode menu = null;;
					switch(SQLJTable.this.panel_type) {
					/*ColorCellRenderer
					 * Popup on product: propose to remove
					 */
					case PRODUCT_PANEL: try {
							menu = new PopupProduct(SQLJTable.this.jtable,  "Actions on Ingested Product Files");
						} catch (SaadaException e1) {
							AdminComponent.showFatalError(rootFrame, e1);
							return;
						} 
					menu.show(SQLJTable.this, e.getX(), e.getY());
					break;
					/*
					 * Popup on class attributes: edit UCD, UTYPES....
					 * for business level.
					 * nothing for collection level
					 */
					case CLASS_PANEL: menu=null;
					if( dataTreePath.isClassLevel() ) {
						//enu = new PopupClassEdit(rootFrame, dataTreePath, SQLJTable.this, "Actions on Class"); 
					}
					else if(  dataTreePath.isCategoryLevel() ) {
						//menu = new PopupCollEdit(rootFrame, dataTreePath, SQLJTable.this, "Actions on Collection"); 
					}
					menu.show(SQLJTable.this, e.getX(), e.getY());
					break;
					}
				}
			}
			@Override
			public void mouseClicked(MouseEvent e) {
				/*
				 * double click a "queriable" cell toggles its boolean value
				 */
				if(e.getClickCount() == 2) {
					JTable table = (SQLJTable)e.getSource();
					int row = table.rowAtPoint(e.getPoint());
					int col = table.columnAtPoint(e.getPoint());
					ResultSetTableModel model = (ResultSetTableModel)table.getModel();
					model.toggleQueriable(row, col);
				}
			}
		});		
	}

	/**
	 * 
	 */
	private void makeOrderingEnabled() {
		JTableHeader header = this.getTableHeader();
		/*
		 * Inner class drwaing little arrows in column labels
		 */
		class IconTableCellRenderer extends DefaultTableCellRenderer {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setIcon(SQLJTable.this.getSortingIcon(column));
				label.setBackground(Color.LIGHT_GRAY);
				label.setBorder(BorderFactory.createLineBorder(Color.gray));
				return label;
			}

		}
		header.setDefaultRenderer(new IconTableCellRenderer());
		/*
		 * Run the query with the good order and update table data
		 */
		header.addMouseListener(new MouseAdapter() {			
			@Override
			public void mouseClicked(MouseEvent e) {						

				int col = SQLJTable.this.columnAtPoint(e.getPoint());
				try {
					String desc = "";
					if( SQLJTable.this.down_sorting ) {
						desc = " desc";
					}
					if( col == SQLJTable.this.order_column ) {
						SQLJTable.this.down_sorting = !SQLJTable.this.down_sorting;
					}
					else {
						SQLJTable.this.down_sorting = false;
					}
					SQLJTable.this.order_column = col;
					SQLJTable.this.setModel(SQLJTable.this.sql.replace("limit 1000", "") + " order by " + SQLJTable.this.getColumnName(col) + desc + " limit 1000");
				} catch (Exception e1) {
					Messenger.printStackTrace(e1);
				}
			}
		});
	}
	/**
	 * @throws FatalException 
	 * @throws SQLException 
	 * @throws Exception 
	 * 
	 */
	public void setModel(String sql) throws QueryException{
		SQLQuery squery = new SQLQuery();

		ResultSet resultSet = squery.run(sql);
		this.setModel(new ResultSetTableModel(resultSet, this));
		int columnCount = this.getColumnCount();          
		for ( int i=0; i<columnCount; i++ ) {
			TableColumn tableColumn = this.getColumnModel().getColumn ( i );
			String cid = this.getColumnModel().getColumn(i).getIdentifier().toString();
			if( cid.equals("pk") ) {
				tableColumn.setPreferredWidth (30);
			}
			else if( cid.equals("type_attr") ) {
				tableColumn.setPreferredWidth (60);	
			}
			else if( cid.equals("queriable") ) {
				tableColumn.setPreferredWidth (60);	
			}
			else if( cid.equals("unit") ) {
				tableColumn.setPreferredWidth (60);	
			}
			else if( cid.equals("utype") ) {
				tableColumn.setPreferredWidth (150);	
			}
			else if( cid.equals("oidsaada") ) {
				tableColumn.setPreferredWidth (160);	
			}
			/*
			 * ColorCellRenderer disabled row selection mode: just used for editable table.
			 * (need to be improved)
			 */
			if( this.panel_type ==  CLASS_PANEL) {
				columnModel.getColumn(i).setCellRenderer(new ColorCellRenderer());
			}
		} 
		squery.close();
	}

	/**
	 * @return
	 */
	public Icon getSortingIcon(int col) {
		if( this.order_column != col ) {
			return both_triangle;
		}
		else if( this.down_sorting ) {
			return down_triangle;
		}
		else {
			return up_triangle;
		}
	}

	/**
	 * Returns true if at least one cell has been modified in the row
	 * @param row
	 * @return
	 */
	public boolean hasModifiedItem(int row) {
		return  ((ResultSetTableModel)(this.getModel())).hasModifiedItem(row);
	}

	/**
	 * Save into the DB the modified rows
	 * @throws FatalException 
	 */
	public void saveModifiedRows() throws FatalException {
		String base_stmt;
		if( dataTreePath.isClassLevel()) {
			base_stmt = "UPDATE saada_metaclass_" + dataTreePath.category.toString().toLowerCase() + " ";
		}
		else {
			base_stmt = "UPDATE saada_metacoll_" + dataTreePath.category.toString().toLowerCase() + " ";			
		}
		for( String stmt: ((ResultSetTableModel)(this.getModel())).getUpdateSQLStatements()) {
			SQLTable.addQueryToTransaction(base_stmt + stmt);
		}
	}

	/**
	 * @return Returns the panel_type.
	 */
	public int getPanel_type() {
		return panel_type;
	}


	/**
	 * Inner class providing litle ordering arrows
	 * @author michel
	 *
	 */
	private static class Triangle implements Icon {
		private final boolean up;
		private boolean both=false;
		private static final int size = 16;
		private static final int[] xxdown = { 3 , 12, 7 };
		private static final int[] yydown = { 5 , 5, 10 };
		private static final int[] xxup = { 2 , 12, 7 };
		private static final int[] yyup = { 10 , 10, 4 };       
		Triangle(boolean up) {
			this.up = up;
		}

		Triangle() {
			this.both = true;
			this.up = true;
		}
		public int getIconHeight() {
			return size;
		}

		public int getIconWidth() {
			return size;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			int[] xp = new int[3];
			int[] yp = new int[3];
			for (int i=0; i<3; i++) {
				xp[i] = x + (up ? xxup[i] : xxdown[i]);
				yp[i] = y + (up ? yyup[i] : yydown[i]);
			}
			g.setColor(c.getForeground());
			g.fillPolygon(xp,yp,3);
			if( both ) {
				for (int i=0; i<3; i++)  {
					xp[i] = x + (xxdown[i]);
					yp[i] = y + (yydown[i]);
				}
			}
			g.fillPolygon(xp,yp,3);
		}

	}
}
