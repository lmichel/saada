package saadadb.admintool.panels.tasks;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import saadadb.admintool.AdminTool;
import saadadb.admintool.components.AdminComponent;
import saadadb.admintool.components.SQLJTable;
import saadadb.admintool.components.ToolBarPanel;
import saadadb.admintool.panels.TaskPanel;
import saadadb.admintool.utils.DataTreePath;
import saadadb.admintool.utils.HelpDesk;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

public class DataTableEditor extends TaskPanel
{
	private static final long serialVersionUID = 1L;
	protected String sqlQuery;
	private SQLJTable productTable;
	protected  JTextArea queryTextArea = new JTextArea(sqlQuery);
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
	protected Map<String, Object> getParamMap() 
	{
		LinkedHashMap<String, Object> retour = new LinkedHashMap<String, Object>();
		retour.put("datatable", productTable);
		return retour;
	}

	@Override
	protected void setActivePanel() 
	{
		tPanel = this.addSubPanel("Explore Data");
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
				GridBagConstraints ccs = new GridBagConstraints();
				try 
				{
					this.buidSQL(dataTreePath);
					productTable = new SQLJTable(rootFrame, dataTreePath, this,  sqlQuery, SQLJTable.PRODUCT_PANEL);
				} 
				catch (QueryException e) 
				{
					e.printStackTrace();
				}
				tPanel.removeAll();
				productTable.setBackground(AdminComponent.LIGHTBACKGROUND);
				productTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				JScrollPane jsp = new JScrollPane(productTable);
				jsp.setBackground(AdminComponent.LIGHTBACKGROUND);
				ccs.gridx = 0; ccs.gridy = 0;
				ccs.weightx = 0.0; ccs.weighty = 0.0; 
				ccs.fill = GridBagConstraints.BOTH;
				ccs.gridwidth = 2;
				tPanel.add(jsp, ccs);
				
				
				queryTextArea = new JTextArea(sqlQuery);
				JScrollPane jtf = new JScrollPane(queryTextArea);
				ccs.gridy++;
				tPanel.add(jtf, ccs);
				
				Component c;
				if (( c = this.addCommandComponent()) != null ) 
				{
					ccs.weightx = 0; ccs.weighty = 0; 
					ccs.gridy++;ccs.fill = GridBagConstraints.BOTH;// ccs.gridwidth = 1;
					tPanel.add(c, ccs);
				}
			}
		}

	}
		
	protected void buidSQL(DataTreePath dataTreePath) 
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
			sqlQuery += "\nFROM " + coll_table_name + "\nLIMIT 1000";
			title = dataTreePath.category + " data of collection <" + dataTreePath.collection + "> (truncated to 1000)";
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
			sqlQuery += "\nFROM " + dataTreePath.collection 
				+ "_" + dataTreePath.category.toLowerCase() + " AS coll, " 
				+  dataTreePath.classe	+ " AS class\nWHERE coll.oidsaada = class.oidsaada\nLIMIT 1000";
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
				refresh(queryTextArea.getText());
			}
		});
		return jb;
	}
	
	@Override
	protected void setToolBar() 
	{
		this.initTreePathLabel();
		this.add(new ToolBarPanel(this, true, false, false));		
	}

	public void refresh(String newQuery) {
		if( productTable != null ) {
			try {
				this.sqlQuery = newQuery;
				this.productTable.setModel(sqlQuery);
			} catch (QueryException e) {
				Messenger.trapQueryException(e);
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

}
