package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SortOrder;

import org.jdesktop.swingx.JXTable;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.components.input.FreeTextField;
import saadadb.admintool.components.input.NodeNameTextField;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.admintool.utils.MyGBC;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.util.RegExp;

public class DataTableEditor extends TaskPanel
{
	private static final long serialVersionUID = 1L;
	protected String sqlQuery;
	public String sqlWHEREClause, sqlLIMITClause, sqlFROMClause, sqlSELECTClause, sqlORDERClause = "";
	private SQLJTable productTable;
	protected FreeTextField queryWhereArea;
	protected NodeNameTextField queryLimitArea;
	private JPanel tPanel;
	
	public DataTableEditor(AdminTool rootFrame, String ancestor) 
	{
		super(rootFrame, EXPLORE_DATA, null, ancestor);
	}

	@Override
	public void initCmdThread()
	{
		//No Thread
	}

	@Override
	protected void setActivePanel() 
	{
		tPanel = this.addSubPanel("Explore Data", false);
		tPanel.add(AdminComponent.getHelpLabel(HelpDesk.DATATABLE_EDITOR));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#acceptTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public boolean acceptTreePath(DataTreePath dataTreePath) 
	{
		if (dataTreePath.isRootOrCollectionLevel())
		{
			showInputError(rootFrame, "You must select either a category (IMAGE, SPECTRUM, ...) or a class.");
			return false;
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see saadadb.admintool.panels.AdminPanel#setDataTreePath(saadadb.admintool.utils.DataTreePath)
	 */
	public void setDataTreePath(DataTreePath dataTreePath) 
	{	
		super.setDataTreePath(dataTreePath);
		if (dataTreePath != null) 
		{
			if( dataTreePath.isCategorieOrClassLevel()) 
			{
				try 
				{
					this.buildSQL(dataTreePath);
					productTable = new SQLJTable(rootFrame, dataTreePath, this, sqlQuery, SQLJTable.PRODUCT_PANEL);
					this.sqlORDERClause = "";
				} 
				catch (QueryException e) 
				{
					e.printStackTrace();
				}
				tPanel.removeAll();
				productTable.setBackground(AdminComponent.LIGHTBACKGROUND);
				productTable.setAutoResizeMode(JXTable.AUTO_RESIZE_OFF);
				JScrollPane jsp = new JScrollPane(productTable);
				jsp.setBackground(AdminComponent.LIGHTBACKGROUND);
				
				Component comp = null;
				if (this.addCommandComponent() != null)
					comp = this.addCommandComponent();
				
				JPanel querySubmitPanel = new JPanel();
				queryWhereArea = new FreeTextField(4,32);
				queryWhereArea.setText(sqlWHEREClause);
				JScrollPane jtf = new JScrollPane(queryWhereArea);
				
				queryLimitArea = new NodeNameTextField(8, "^" + RegExp.NUMERIC + "$", comp);
				queryLimitArea.setText(sqlLIMITClause);
				
				MyGBC mgbc = new MyGBC(10,10,10,10);
				mgbc.anchor = GridBagConstraints.EAST;
				querySubmitPanel.add(getPlainLabel("WHERE"), mgbc);
				mgbc.anchor = GridBagConstraints.WEST;
				querySubmitPanel.add(jtf, mgbc);
				
				mgbc.anchor = GridBagConstraints.EAST;
				querySubmitPanel.add(getPlainLabel("LIMIT"), mgbc);
				mgbc.anchor = GridBagConstraints.WEST;
				querySubmitPanel.add(queryLimitArea, mgbc);

				querySubmitPanel.add(comp, mgbc);
				
				JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jsp, querySubmitPanel);	
				splitPane.setOneTouchExpandable(true);
				splitPane.setDividerLocation(525);
				
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0; c.gridy = 0;
				c.weightx = 0.5; c.weighty = 0.5; c.fill = GridBagConstraints.BOTH; c.gridwidth = 2;
				tPanel.add(splitPane, c);
			}
		}
	}
		
	protected void buildSQL(DataTreePath dataTreePath) 
	{
		sqlQuery = "SELECT ";
		title= "??";
		String[] rejected_coll_clos = null;
		String coll_table_name = dataTreePath.collection+ "_" + dataTreePath.category.toLowerCase();
		try {
			/*
			 * Remove hidden columns (not managed yet)
			 */
			rejected_coll_clos = SQLTable.getColumnsExceptsThose(coll_table_name
					, new String[]{"oidproduct", "access_right", "loaded", "group_oid_csa", "nb_rows_csa"
					, "y_min_csa", "y_max_csa", "y_unit_csa", "y_colname_csa"
					, "shape_csa"});
		} catch( FatalException e) {
			Messenger.trapFatalException(e);
		}
		/*
		 * metadata tree path = base-coll-cat: show all product of a category
		 */
		if( dataTreePath.isCategoryLevel()  ) {
			for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
				if( i != 0 ) {
					sqlQuery += ", ";
				}
				sqlQuery += rejected_coll_clos[i] ;
			}
			sqlSELECTClause = sqlQuery;
			sqlLIMITClause = "1000";
			sqlFROMClause = "FROM " + coll_table_name;
			sqlQuery += "\n" + sqlFROMClause + "\nLIMIT " + sqlLIMITClause;
			title = dataTreePath.category + " data of collection <" + dataTreePath.collection + "> (truncated to 1000)";
			sqlWHEREClause = "";
		}
		/*
		 * metadata tree path = base-coll-cat-class: show all product of a class
		 */
		else if( dataTreePath.isClassLevel() ) {
			for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
				if( i != 0 ) {
					sqlQuery += ", ";
				}
				sqlQuery += "coll." + rejected_coll_clos[i] ;
			}
			String[] rejected_class_clos = null;
			try {
				/*
				 * Remove hidden columns (not managed yet)
				 */
				rejected_class_clos = SQLTable.getColumnsExceptsThose(dataTreePath.classe
						, new String[]{"oidsaada", "namesaada"});
			} catch (FatalException e) {
				Messenger.trapFatalException(e);
			}
			for( int i=0 ; i<rejected_class_clos.length ; i++  ) {
				sqlQuery += ", class." + rejected_class_clos[i] ;
			}
			sqlSELECTClause = sqlQuery;
			sqlLIMITClause = "1000";
			sqlFROMClause = "FROM " + dataTreePath.collection 
				+ "_" + dataTreePath.category.toLowerCase() + " AS coll, " 
				+  dataTreePath.classe	+ " AS class";
			sqlWHEREClause = "coll.oidsaada = class.oidsaada";
			sqlQuery += "\n" + sqlFROMClause + "\nWHERE " + sqlWHEREClause + "\nLIMIT " + sqlLIMITClause;
			title = "Data (" + dataTreePath.category + ") of class <" + dataTreePath.classe  + ">  of collection <" + dataTreePath.collection + "> (truncated to 1000)";
		}
		else {
			AdminComponent.showFatalError(this, "No datasource selected");
		}
	}
	
	protected Component addCommandComponent() 
	{
		JButton jb = new JButton("SUBMIT");
		jb.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent arg0) 
			{
				refresh(true);
			}
		});
		return jb;
	}
	
	@Override
	protected void setToolBar() 
	{
		this.initTreePathPanel();
		this.add(new ToolBarPanel(this, true, false, false));		
	}

	public void refresh(boolean isNewQuery) 
	{
		String newQuery = "";
		if( productTable != null ) 
		{
			sqlWHEREClause = queryWhereArea.getText();
			sqlLIMITClause = queryLimitArea.getText();
			
			// TODO Refresh the field that are selected only
			/*ArrayList<TableColumnExt> hiddenColumns = new ArrayList<TableColumnExt>();
			ArrayList<Integer> indexHiddenColumns = new ArrayList<Integer>();
			// Search the hidden columns
			/*for (int i=0 ; i<productTable.getColumnCount() ; i++)
			{
				Messenger.printMsg(Messenger.DEBUG, "@@@@C " + productTable.getColumnCount() + " : Visible? " + productTable.getColumnExt(i).isVisible());
				if (!productTable.getColumnExt(i).isVisible())
				{
					indexHiddenColumns.add(i);
					hiddenColumns.add(productTable.getColumnExt(i));
				}
			}
			TableColumnExt test = new TableColumnExt(((JXTable)productTable).getColumnExt(2));
			test.setVisible(false);
			Messenger.printMsg(Messenger.DEBUG, "@@@@@ " + test.isVisible());*/	
			
			// Build the rest of the query
			newQuery +=sqlSELECTClause + "\n" + 
					sqlFROMClause + "\n" + 
					(sqlWHEREClause.compareTo("")==0?"":"WHERE ") + sqlWHEREClause + 
					(sqlORDERClause.compareTo("")==0?"":"\nORDER BY ") + sqlORDERClause + 
					"\nLIMIT " + sqlLIMITClause;
			
			// Set the newQuery in the sqlQuery and the model
			this.sqlQuery = newQuery;
			try 
			{
				this.productTable.setModel(sqlQuery);
			} 
			catch (QueryException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void refresh() {
		if( productTable != null ) {
			try {
				productTable.setModel(sqlQuery);
			} catch (QueryException e) {
				Messenger.trapQueryException(e);
			}
		}
	}
	
	public JTable getProductTable() {
		return this.productTable;
	}
	
	public DataTreePath getDataTreePath() {
		return dataTreePath;
	}

	@Override
	protected Map<String, Object> getParamMap() 
	{
		return null;
	}

}
