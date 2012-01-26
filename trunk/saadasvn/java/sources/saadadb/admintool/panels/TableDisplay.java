/**
 * 
 */
package saadadb.admintool.panels;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import saadadb.admin.SQLJTable;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author laurentmichel
 *
 */
public class TableDisplay  {

	/**
	 * @param tree_path_components
	 * @throws Exception 
	 * @throws QueryException 
	 */
	public void showProduct(Object[] tree_path_components) throws QueryException {
		if( tree_path_components.length < 3 ) {
			showFatalError(this, "No datasource selected");
			return ;			
		}
		else {
			String sql = "select ", title= "??";
			String[] rejected_coll_clos = null;
			String coll_table_name = tree_path_components[1] + "_" + tree_path_components[2].toString().toLowerCase();
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
			if( tree_path_components.length == 3 ) {
				for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
					if( i != 0 ) {
						sql += ", ";
					}
					sql += rejected_coll_clos[i] ;
				}
				sql += " from " + coll_table_name + " limit 1000";
				title = tree_path_components[2]  + " data of collection <" + tree_path_components[1] + "> (truncated to 1000)";
			}
			/*
			 * metadata tree path = base-coll-cat-class: show all product of a class
			 */
			else if( tree_path_components.length == 4 ) {
				for( int i=0 ; i<rejected_coll_clos.length ; i++  ) {
					if( i != 0 ) {
						sql += ", ";
					}
					sql += "coll." + rejected_coll_clos[i] ;
				}
				String[] rejected_class_clos = null;
				try {
					/*
					 * Remove hidden columns (not managed yet)
					 */
					rejected_class_clos = SQLTable.getColumnsExceptsThose(tree_path_components[3].toString(), new String[]{"oidsaada", "namesaada"});
				} catch (FatalException e) {
					Messenger.trapFatalException(e);
				}
				for( int i=0 ; i<rejected_class_clos.length ; i++  ) {
					sql += ", class." + rejected_class_clos[i] ;
				}
				sql += " from " + tree_path_components[1] + "_" + tree_path_components[2].toString().toLowerCase() + " as coll, " +  tree_path_components[3]	+ " as class where coll.oidsaada = class.oidsaada limit 1000";
				title = "Data (" + tree_path_components[2] + ") of class <" + tree_path_components[3]  + ">  of collection <" + tree_path_components[1] + "> (truncated to 1000)";
			}
			else {
				showFatalError(this, "No datasource selected");
				return ;
			}
			/*
			 * Cannot manage product for entries because entries are not products
			 * => no popup menu on tabel.
			 */
			if( tree_path_components[2].equals("ENTRY") ) {
				product_table = new SQLJTable(this, sql, tree_path_components, SQLJTable.PRODUCT_PANEL, false);
			}
			else {
				product_table = new SQLJTable(this, sql, tree_path_components, SQLJTable.PRODUCT_PANEL, true);				
			}
			/*
			 * Needed to activate an horizontal scrolling
			 * and to see something in the table
			 */
			product_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JScrollPane scrollPane = new JScrollPane(product_table);    
			scrollPane.setBorder(BorderFactory.createTitledBorder(title));
			/*
			 * Needed too to activate scrolling
			 */
			scrollPane.setPreferredSize(product_panel.getSize());
			product_panel.setLayout(new BorderLayout());
			product_panel.add(scrollPane, BorderLayout.CENTER);
			product_panel.updateUI();
		}
	}

}
