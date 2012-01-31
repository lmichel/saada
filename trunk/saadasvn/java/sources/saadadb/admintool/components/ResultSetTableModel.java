package saadadb.admintool.components;
/*
 * 
 * Many thanks to http://www.oreillynet.com/pub/a/oreilly/java/news/javaex_1000.html
 */


import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import saadadb.admintool.dialogs.DialogUCD;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

/**
 * This class takes a JDBC ResultSet object and implements the TableModel
 * interface in terms of it so that a Swing JTable component can display the
 * contents of the ResultSet.  Note that it requires a scrollable JDBC 2.0 
 * ResultSet.  Also note that it provides read-only access to the results
 **/
public class ResultSetTableModel implements TableModel {
	SQLJTable frame;	// requested by Dialog boxes
	//	ResultSet results;             // The ResultSet to interpret
	//	ResultSetMetaData metadata;    // Additional information about the results
	//	int numcols, numrows;          // How many rows and columns in the table
	ResultSetCopier rs_copy ;
	HashMap<Integer, String> modified_ucd;
	HashMap<Integer, String> modified_comment;
	HashMap<Integer, String> modified_utype;
	HashMap<Integer, String> modified_unit;
	HashMap<Integer, String> modified_queriable;
	HashMap<Integer, String> modified_format;

	/**
	 * This constructor creates a TableModel from a ResultSet.  It is package
	 * private because it is only intended to be used by 
	 * ResultSetTableModelFactory, which is what you should use to obtain a
	 * ResultSetTableModel
	 **/
	ResultSetTableModel(ResultSet results, SQLJTable frame) throws QueryException {
		try {
			rs_copy = new ResultSetCopier(results);
			modified_ucd = new HashMap<Integer, String>();
			modified_comment = new HashMap<Integer, String>();
			modified_utype = new HashMap<Integer, String>();
			modified_unit = new HashMap<Integer, String>();
			modified_queriable = new HashMap<Integer, String>();
			modified_format = new HashMap<Integer, String>();
			this.frame = frame;
		}catch(SQLException e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	// These two TableModel methods return the size of the table
	public int getColumnCount() { return rs_copy.nb_columns; }
	public int getRowCount() { return rs_copy.nb_rows;}

	// This TableModel method returns columns names from the ResultSetMetaData
	public String getColumnName(int column) {
		return rs_copy.labels.get(column);
	}

	// This TableModel method specifies the data type for each column.  
	// We could map SQL types to Java types, but for this example, we'll just
	// convert all the returned data to strings.
	public Class getColumnClass(int column) { return String.class; }

	/**
	 * This is the key method of TableModel: it returns the value at each cell
	 * of the table.  We use strings in this case.  If anything goes wrong, we
	 * return the exception as a string, so it will be displayed in the table.
	 * Note that SQL row and column numbers start at 1, but TableModel column
	 * numbers start at 0.
	 **/
	public Object getValueAt(int row, int column) {
		/*
		 * Attempt first to Display the edited cell value if it exist
		 */
		String edited_cell = null;
		if( getColumnName(column).equals("ucd") && (edited_cell = modified_ucd.get(row)) != null ) {
			return edited_cell;
		}
		else if( getColumnName(column).equals("utype") && (edited_cell = modified_utype.get(row)) != null ) {
			return edited_cell;
		}
		else if( getColumnName(column).equals("comment") && (edited_cell = modified_comment.get(row)) != null ) {
			return edited_cell;
		}
		else if( getColumnName(column).equals("unit") && (edited_cell = modified_unit.get(row)) != null ) {
			return edited_cell;
		}
		else if( getColumnName(column).equals("queriable") && (edited_cell = modified_queriable.get(row)) != null ) {
			return edited_cell;
		}
		else if( getColumnName(column).equals("format") && (edited_cell = modified_format.get(row)) != null ) {
			return edited_cell;
		}
		else {
			Object o = rs_copy.getValue(row, column); // Get value of the column
			if (o == null) {
				return "";       
			}
			else {
				if( getColumnName(column).equals("date_load")) {
					return (new Date(Long.parseLong(o.toString()))).toString();
				}
				else {
					return o.toString();               // Convert it to a string
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) { 
		if( this.frame.getPanel_type() == SQLJTable.CLASS_PANEL ) {
			if( getColumnName(column).equals("ucd") || getColumnName(column).equals("comment")||
					getColumnName(column).equals("utype") || getColumnName(column).equals("unit") || 
					getColumnName(column).equals("format")) {
				return true;
			}
			else {
				return false; 
			}
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
	 */
	public void setValueAt(Object value, int row, int column) {
		if( getColumnName(column).equals("ucd") ) {
			modified_ucd.put(row, (String)value);        
			frame.updateUI();
		}
		else if( getColumnName(column).equals("utype") ) {
			modified_utype.put(row, (String)value);
			frame.updateUI();
		}
		else if( getColumnName(column).equals("comment") ) {
			modified_comment.put(row, (String)value);
			frame.updateUI();
		}
		else if( getColumnName(column).equals("unit") ) {
			modified_unit.put(row, (String)value);
			frame.updateUI();
		}
		else if( getColumnName(column).equals("queriable") ) {
			modified_queriable.put(row, (String)value);
			frame.updateUI();
		}
		else if( getColumnName(column).equals("format") ) {
			modified_format.put(row, (String)value);
			frame.updateUI();
		}
	}
	public void addTableModelListener(TableModelListener l) {}
	public void removeTableModelListener(TableModelListener l) {}

	/**
	 * Update both ucd and comment with ucd string
	 * @param str
	 * @param row
	 */
	public void setUCD(String str, int row) {
		int nbc=rs_copy.nb_columns;
		/*
		 * Extract the current comment
		 */
		String previous_comment = "";
		int comment_col = -1;
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("comment") ) {
				previous_comment = (String) getValueAt(row, i);
				comment_col = i;
				break;
			}
		}
		/*
		 * Points onto the ucd column
		 */
		for( int i=0 ; i<nbc ; i++ ) {
			System.out.println(getColumnName(i));
			if( getColumnName(i).equals("ucd") ) {
				/*
				 * Store the previous ucd
				 */
				String previous_ucd = (String) getValueAt(row, i);
				String new_ucd = str.trim();
				String comment = "";
				/*
				 * Extract comment and UCD from the string supposed looking like 
				 * "ucd (comment)"
				 */
				int pos = new_ucd.indexOf('(');
				if( pos > 0 ) {
					comment = new_ucd.substring(pos).trim();	
					new_ucd = new_ucd.substring(0, pos).trim();
				}
				/*
				 * Open a dialog box if the ucd was already set for this column
				 * This dialog propose various combinations to build a compisite UCD and a
				 * texte filed for the comment
				 */
				if( previous_ucd != null && previous_ucd.length() > 0 ) {
					DialogUCD cd = null;
					cd = new DialogUCD(frame.getRootFrame()
							, "Set UCD"
							, new String[]{new_ucd, new_ucd + ";" + previous_ucd, previous_ucd + ";" + new_ucd, previous_ucd, "no UCD"}
					, (previous_comment.length() > 0)? (previous_comment + " " + comment): comment);					
					cd.pack();
					cd.setLocationRelativeTo(frame.getRootFrame());
					cd.setVisible(true);
					/*
					 * Comment field in dialog is set to null when Cancel is clicked
					 */
					if( cd.getTyped_comment() != null ) {
						if( cd.getSelectedUCD().equals("no UCD") ) {
							setValueAt("", row, i);				    		
						}
						else {
							setValueAt(cd.getSelectedUCD(), row, i);				    						    		
						}
						setValueAt(cd.getTyped_comment(), row, comment_col);				    		
					}
				}
				else {
					setValueAt(new_ucd, row, i);
					if( previous_comment.length() == 0 ) {
						setValueAt(comment, row, comment_col);				    								
					}
					/*
					 * Propose a comment merged with the previous one and the DM attribute description
					 */
					else {
						DialogUCD cd =  new DialogUCD(frame.getRootFrame()
								, "Set UCD"
								, null
								, (previous_comment.length() > 0)? (previous_comment + " " + comment): comment );					
						cd.pack();
						cd.setLocationRelativeTo(frame.getRootFrame());
						cd.setVisible(true);
						if( cd.getTyped_comment() != null ) {
							setValueAt(cd.getTyped_comment(), row, comment_col);
						}
					}
				}
				return;
			}
		}
	}

	/**
	 * Update both ucd and comment with ucd string
	 * @param str
	 * @param row
	 */
	public void setUtype(String str, int row) {
		int nbc = rs_copy.nb_columns;;
		/*
		 * Extract the current comment
		 */
		String previous_comment = "";
		int comment_col = -1;
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("comment") ) {
				previous_comment = (String) getValueAt(row, i);
				comment_col = i;
				break;
			}
		}
		/*
		 * Points onto the ucd column
		 */
		boolean colFound = false;
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("utype") ) {
				colFound = true;
				/*
				 * Store the previous ucd
				 */
				String previous_utype = (String) getValueAt(row, i);
				String new_utype = str.trim();
				String comment = "";
				/*
				 * Extract comment and UCD from the string supposed looking like 
				 * "ucd (comment)"
				 */
				int pos = new_utype.indexOf('(');
				if( pos > 0 ) {
					comment = new_utype.substring(pos).trim();	
					new_utype = new_utype.substring(0, pos).trim();
				}
				/*
				 * Open a dialog box if the utype was already set for this column
				 * This dialog propose to keep the current utype or to replce it with the
				 * new one and a text field for the comment
				 */
				if( previous_utype != null && previous_utype.length() > 0 ) {

					DialogUCD cd = null;
					cd = new DialogUCD(frame.getRootFrame()
							, "Set UType"
							, new String[]{new_utype, previous_utype, "no Utype"}
					, (previous_comment.length() > 0)? (previous_comment + " " + comment): comment );					
					cd.pack();
					cd.setLocationRelativeTo(frame.getRootFrame());
					cd.setVisible(true);
					/*
					 * Comment field in dialog is set to null when Cancel is clicked
					 */
					if( cd.getTyped_comment() != null ) {
						if( cd.getSelectedUCD().equals("no Utype") ) {
							setValueAt("", row, i);				    		
						}
						else {
							setValueAt(cd.getSelectedUCD(), row, i);				    						    		
						}
						setValueAt(cd.getTyped_comment(), row, comment_col);				    		
					}
				}
				else {
					if( previous_comment.length() == 0 ) {
						setValueAt(new_utype, row, i);
						setValueAt(comment, row, comment_col);				    								
					}
					/*
					 * Propose a comment merged with the previous one and the DM attribute description
					 */
					else {
						DialogUCD cd =  new DialogUCD(frame.getRootFrame()
								, "Set UType"
								, null
								, (previous_comment.length() > 0)? (previous_comment + " " + comment): comment );					
						cd.pack();
						cd.setLocationRelativeTo(frame.getRootFrame());
						cd.setVisible(true);
						if( cd.getTyped_comment() != null ) {
							setValueAt(new_utype, row, i);
							setValueAt(cd.getTyped_comment(), row, comment_col);				    		
						}
					}
				}
				return;
			}
		}
		if( !colFound ) {
			AdminComponent.showInfo(frame, "This table has no editable UType column:");
		}
	}

	/**
	 * Update both ucd and comment with ucd string
	 * @param str
	 * @param row
	 */
	public void setUnit(String str, int row) {
		int nbc=rs_copy.nb_columns;
		boolean colFound = false;
		/*
		 * Points onto the ucd column
		 */
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("unit") ) {
				colFound = true;
				/*
				 * Store the previous ucd
				 */
				String previous_unit = (String) getValueAt(row, i);
				String new_unit = str.trim();
				/*
				 * Open a dialog box if the utype was already set for this column
				 * This dialog propose to keep the current utype or to replce it with the
				 * new one and a text field for the comment
				 */
				if( previous_unit != null && previous_unit.length() > 0 ) {

					DialogUCD cd =  new DialogUCD(frame.getRootFrame()
							, "Set UType"
							, new String[]{new_unit, previous_unit, "none"}
					, null );					
					cd.pack();
					cd.setLocationRelativeTo(frame.getRootFrame());
					cd.setVisible(true);
					/*
					 * Selected UCD field in dialog is set to null when Cancel is clicked
					 */
					if( cd.getSelectedUCD() != null ) {
						if( cd.getSelectedUCD().equals("none") ) {
							setValueAt("", row, i);				    		
						}
						else {
							setValueAt(cd.getSelectedUCD(), row, i);				    						    		
						}
					}
				}
				else {
					setValueAt(new_unit, row, i);
					return;
				}
			}
		}
		if( !colFound ) {
			AdminComponent.showInfo(frame, "This table has no editable Unit column:");
		}
	}

	/**
	 * if col matches the "queriable column, its value is is toggled true-false 
	 * @param row
	 * @param col
	 */
	protected void toggleQueriable(int row, int col) {
		int nbc =  rs_copy.nb_columns;
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("queriable") ) {
				if( col == i ) {
					if( getValueAt(row, col).toString().equals("true") ) {
						setValueAt("false", row, col);
					}
					else {
						setValueAt("true", row, col);				
					}
				}
				break;
			}
		}
	}
	/**
	 * Returns true if at least one modifiable cell has been modified in the row
	 * @param row
	 * @return
	 */
	public boolean hasModifiedItem(int row) {
		if( modified_ucd.get(row) != null      || modified_comment.get(row) != null ||
				modified_utype.get(row) != null    || modified_unit.get(row) != null    || 
				modified_queriable.get(row) != null|| modified_format.get(row) != null) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Return the list of SQL statements (expect "UPDATE table" because table is not known here) 
	 * to be run to update the table
	 * @return
	 * @throws FatalException 
	 */
	public ArrayList<String> getUpdateSQLStatements() throws FatalException {
		TreeSet<Integer> ts = new TreeSet<Integer>();
		ArrayList<String> retour = new ArrayList<String>();
		ts.addAll(modified_ucd.keySet());
		ts.addAll(modified_utype.keySet());
		ts.addAll(modified_comment.keySet());
		ts.addAll(modified_unit.keySet());
		ts.addAll(modified_queriable.keySet());
		ts.addAll(modified_format.keySet());
		int nbc = rs_copy.nb_columns;
		/*
		 * Extract the column numbers of the field to be updated
		 */
		int comment_col = -1, ucd_col = -1,  utype_col = -1, unit_col = -1, quer_col = -1, form_col = -1, name_col = -1;;
		for( int i=0 ; i<nbc ; i++ ) {
			if( getColumnName(i).equals("comment") ) {
				comment_col = i;
			}
			else if( getColumnName(i).equals("ucd") ) {
				ucd_col = i;
			}
			else if( getColumnName(i).equals("utype") ) {
				utype_col = i;
			}
			else if( getColumnName(i).equals("unit") ) {
				unit_col = i;
			}
			else if( getColumnName(i).equals("queriable") ) {
				quer_col = i;
			}
			else if( getColumnName(i).equals("format") ) {
				form_col = i;
			}
			else if( getColumnName(i).equals("name_attr") ) {
				name_col = i;
			}
		}
		for( int i: ts){
			String strQuery = "\nSET "
					+ "ucd = '"      + ((String)(this.getValueAt(i, ucd_col))).replaceAll("'", "")    + "'"
					+ ", comment = '"  + ((String)(this.getValueAt(i, comment_col))).replaceAll("'", "")+ "'";
			if( utype_col != -1 ) 
				strQuery += ", utype = '"    + ((String)(this.getValueAt(i, utype_col))).replaceAll("'", "")  + "'";
			if( unit_col != -1 ) 
				strQuery +=  ", unit = '"     + ((String)(this.getValueAt(i, unit_col))).replaceAll("'", "")   + "'";
			if( quer_col != -1 ) {
				String rbq = this.getValueAt(i, quer_col).toString();
				String bq;
				if( "true".equalsIgnoreCase(rbq) ) {
					bq = Database.getWrapper().getBooleanAsString(true);

				}
				else {
					bq = Database.getWrapper().getBooleanAsString(false);
				}
				strQuery += ", queriable = " + bq   + " ";
			}
			if( form_col != -1 )
				strQuery += ", format = '"   + ((String)(this.getValueAt(i, form_col))).replaceAll("'", "")   + "'";
			
					/*
					 * pk = -1 is used for updating collection metadata tables. AS metadata are the same
					 * for all collection, they are defined once with pk = -1
					 */
			strQuery += "\nWHERE name_attr = '"  + this.getValueAt(i, name_col) + "'";
			retour.add(strQuery);
		}
		return retour;
	}

	class ResultSetCopier {
		ArrayList<String> copy = new ArrayList<String>();
		ArrayList<String> labels = new ArrayList<String>();

		int nb_columns;
		int nb_rows = 0;

		ResultSetCopier(ResultSet rs) throws SQLException {
			Messenger.printMsg(Messenger.DEBUG, "Make a copy of the resultset");
			nb_columns = rs.getMetaData().getColumnCount();
			for( int i=1 ; i<=nb_columns ; i++) {
				labels.add(rs.getMetaData().getColumnLabel(i));
			}

			while( rs.next() ) {
				nb_rows ++;
				String str = "";
				for( int i=1 ; i<=nb_columns ; i++) {
					if( i > 1 ) {
						str += "\t";
					}
					str += rs.getObject(i);
				}
				copy.add(str);
			}
			Messenger.printMsg(Messenger.DEBUG, nb_rows + " rows " + nb_columns + "columns");

		}
		Object getValue(int row, int column) {
			String str = copy.get(row);
			return str.split("\t",-1)[column];	
		}
	}
}
