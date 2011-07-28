package adqlParser.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLQuery;

/**
 * <p>This class is used by {@link AdqlParser} to ensure the consistency between an ADQL query and 
 * the "database" on which the query must be executed.</p>
 * <p>Since in a query there might have some sub-query, this object must avoid merge columns and alias of the main query and of its sub-queries.
 * To do that it is needed to call the {@link DBConsistency#addContext()} method at the beginning of a sub-query parsing and 
 * the {@link DBConsistency#removeContext()} at the end of a sub-query parsing.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see AdqlParser
 */
public abstract class DBConsistency {
	
	/** All columns contained in the selected tables (selected thanks to the FROM clause).
	 * Each item of the stack corresponds to a context and is a mapping between a column name
	 * to the list of tables which have a column with this column name. */
	protected Stack<HashMap<String, Vector<String> > > availableColumns;
	
	/** Mapping between a column alias and a column name (mainly found in the SELECT clause).
	 * Each item of the stack corresponds to a context and is a mapping between a column alias
	 * to a column name. */
	protected Stack<HashMap<String, String> > columnsAlias;
	
	/** Mapping between a table alias and a table name (mainly found in the FROM clause).
	 * Each item of the stack corresponds to a context and is a mapping between a table alias
	 * to a table name. */
	protected Stack<HashMap<String, String> > tablesAlias;
	
	protected Stack<ADQLQuery> queries;
	
	/** Indicates whether some debugging messages must be printed or not. */
	protected boolean debug;
	

	public DBConsistency(){
		this(false);
	}
	
	public DBConsistency(boolean debugging){
		availableColumns = new Stack<HashMap<String, Vector<String> > >();
		columnsAlias = new Stack<HashMap<String, String> >();
		tablesAlias = new Stack<HashMap<String, String> >();
		queries = new Stack<ADQLQuery>();
		debug = debugging;
	}
	
	public void setDebug(boolean debugging){
		debug = debugging;
	}
	
	public boolean isDebugging(){
		return debug;
	}
	
	/**
	 * Adds an empty context, that's to say, in this new context the available columns, columns alias and tables alias lists are empty.<br />
	 * This is particularly useful if the parser needs to parse a sub-query and so to avoid merging two variables contexts.<br />
	 * <b>This method must be called at each beginning of a sub-query parsing.</b> 
	 */
	public void addContext(ADQLQuery newQuery){
		queries.push(newQuery);
		availableColumns.push(new HashMap<String,Vector<String> >());
		columnsAlias.push(new HashMap<String, String>());
		tablesAlias.push(new HashMap<String, String>());
		if (debug) System.out.println("### NEW CONTEXT !");
	}
	
	/**
	 * Removes the last context, that's to say, the last context is replaced by its preceding.<br />
	 * This is particularly useful if the parser needs to parse a sub-query and so to avoid merging two variables contexts.<br />
	 * <b>This method must be called at each end of a sub-query parsing.</b> 
	 */
	public ADQLQuery removeContext(){
		availableColumns.pop();
		columnsAlias.pop();
		tablesAlias.pop();
		if (debug) System.out.println("### PREVIOUS CONTEXT !");
		return queries.pop();
	}
	
	public final ADQLQuery getCurrentQuery(){
		if (queries.isEmpty())
			return null;
		else
			return queries.peek();
	}
	
	/**
	 * Gets the current list of all available columns according to the selected tables of the FROM clause.
	 * 
	 * @return	A mapping between a column name and a list of all tables which contains a column named like that.
	 */
	protected final HashMap<String,Vector<String> > lstColumns(){
		return availableColumns.peek();
	}
	
	/**
	 * Adds an association between a column and a table.
	 * 
	 * @param name			The name of the column.
	 * @param tableAlias	The alias of the table (if there is no alias, it is merely the table name).
	 */
	public void addColumn(String name, String tableAlias){
		if (debug) System.out.print("### ADD [ColumnTable]: '"+name+"' in '"+tableAlias+"'...");
		
		Vector<String> tables = lstColumns().get(name);
		
		if (tables == null){
			tables = new Vector<String>();
			tables.add(tableAlias);
			lstColumns().put(name, tables);
		}else
			tables.add(tableAlias);
		
		if (debug){
			try {
				System.out.println(columnExists(name, tableAlias)?"OK !":"ERROR !");
			} catch (ParseException e) {
				System.out.println("ERROR {"+e.getMessage()+"} !");
			}
		}
	}
	
	/**
	 * Checks whether the given SELECTed column corresponds to an existing column in, at least, one table.
	 * 
	 * @param column		Column (of the SELECT clause) whose the existence must be checked.
	 * @return				<i>true</i> if the column exists, <i>false</i> else.
	 */
	public boolean selectedColumnExists(ADQLColumn column) throws ParseException {
		if (debug) System.out.print("### SELECTED COLUMN EXISTS: "+column.getColumn()+" [in "+column.getPrefix()+"] ? ");
		
		boolean result = columnExists(column.getColumn(), column.getPrefix());
		if (result)
			addColumnAlias((column.getAlias() != null)?column.getAlias():column.getColumn(), column.getColumn());
		
		if (debug) System.out.println(result?"YES !":"NO !");
		
		return result;
	}
	
	/**
	 * Checks whether the given column name corresponds to an existing column in, at least, one table.
	 * 
	 * @param columnName	Name of the column whose the existence must be checked.
	 * @return				<i>true</i> if the column exists, <i>false</i> else.
	 */
	public boolean columnExists(String columnName){
		try{
			return columnExists(columnName, null);
		}catch(ParseException pe){ return false; }
	}

	/**
	 * Checks whether the given column name corresponds to an existing column in the given table.
	 * 
	 * @param columnName	Name of the column whose the existence must be checked.
	 * @param tableAlias	Alias (or name) of the table which has to contain the column.
	 * @return				<i>true</i> if the column exists, <i>false</i> else.
	 */
	public boolean columnExists(String columnName, String tableAlias) throws ParseException {
		if (tableAlias == null && lstColumnsAlias().get(columnName) != null)
			return true;
		else{
			Vector<String> table = lstColumns().get(columnName);
			
			if (table != null){
				if (tableAlias != null){
					// Check whether the table alias corresponds to an existing table:
					if (getTableName(tableAlias) == null){
						if (getTableAlias(tableAlias) == null)
							throw new ParseException("The table or table's alias \""+tableAlias+"\" doesn't exist ! The existence of \""+columnName+"\" can't be checked !");
						else
							tableAlias = getTableAlias(tableAlias);
					}
					// Look for the given table's alias in the returned tables list:
					for(String str : table){
						if (str.equalsIgnoreCase(tableAlias))
							return true;
					}
				}else
					return table.size() >= 1;
			}
		}

		return false;
	}
	
	/**
	 * Gets the number of the tables in which the given column exists.
	 * 
	 * @param columnName	The column name.
	 * @return				The number of tables which contain the given column.
	 */
	public int getNbTables(String columnName){
		Vector<String> tables = lstColumns().get(columnName);
		return (tables==null)?0:tables.size();
	}
	
	/**
	 * Gets the list of tables which contain the given column.
	 * 
	 * @param columnName	The name of the column.
	 * @return				All the tables which contain the specified column.
	 */
	public String[] getTables(String columnName){
		String[] lstTables = new String[0];
		
		Vector<String> vTables = lstColumns().get(columnName);
		if (vTables != null){
			lstTables = new String[vTables.size()];
			int i = 0;
			for(String str : vTables)
				lstTables[i++] = str;
		}
		
		return lstTables;
	}
	
	/**
	 * Gets the current list of all columns' alias.
	 * 
	 * @return	A mapping between a column alias and a column name.
	 */
	protected final HashMap<String,String> lstColumnsAlias(){
		return columnsAlias.peek();
	}
	
	/**
	 * Adds a column alias.
	 * 
	 * @param alias	Column alias.
	 * @param name	Column name.
	 */
	public void addColumnAlias(String alias, String name){
		if (debug) System.out.print("### ADD [ColumnAlias]: '"+((alias==null)?name:alias)+"' = '"+name+"'...");
		
		lstColumnsAlias().put((alias==null)?name:alias, name);
		
		if (debug) System.out.println((lstColumnsAlias().get((alias==null)?name:alias)!=null)?"OK !":"ERROR !");
	}
	
	/**
	 * Gets the alias which corresponds to the given column name, if any.
	 * 
	 * @param name	Name of the column for which the alias is wanted.
	 * @return		The alias of the given column, or <i>null</i> if not found.
	 */
	public String getColumnAlias(String name){
		String alias = null;
		Iterator<Map.Entry<String,String> > it = lstColumnsAlias().entrySet().iterator();
		
		while(alias == null && it.hasNext()){
			Map.Entry<String, String> item = it.next();
			if (item.getValue().equalsIgnoreCase(name))
				alias = item.getKey();
		}
		
		return alias;
	}
	
	/**
	 * Gets the column name corresponding to the given alias, if any.
	 * 
	 * @param alias	The alias of the column for which the name is wanted.
	 * @return		The column name of the given alias, or <i>null</i> if not found.
	 */
	public String getColumnName(String alias){
		return lstColumnsAlias().get(alias);
	}
	
	/**
	 * Gets the current list of all tables' alias.
	 * 
	 * @return	A mapping between a table alias and a table name.
	 */
	protected final HashMap<String,String> lstTablesAlias(){
		return tablesAlias.peek();
	}
	
	/**
	 * Adds a table alias.
	 * 
	 * @param alias	The table alias to add.
	 * @param name	The corresponding table name.
	 * 
	 * @return 		The added alias (= <i>name</i> if the given <i>alias</i> was <i>null</i>), or <i>null</i> if the alias can't have been added.
	 */
	public String addTableAlias(String alias, String name) throws ParseException {
		alias = ((alias==null)?name:alias);
		if (debug) System.out.print("### ADD [TableAlias]: '"+alias+"' = '"+name+"'...");
		
		if (lstTablesAlias().get(alias) == null)
			lstTablesAlias().put(alias, name);
		else
			throw new ParseException("The table \""+alias+"\" is referenced more than one time in the clause FROM"+((!alias.equals(name))?(" (it is already the alias of \""+lstTablesAlias().get(alias)+"\")"):"")+" !");
		
		if (debug) System.out.println((lstTablesAlias().get(alias)!=null)?"OK !":"ERROR !");
		
		if (lstTablesAlias().get(alias) != null)
			return alias;
		else
			return null;
	}
	
	/**
	 * Gets the number of tables which are associated to an alias.
	 * <i><u>Note:</u> theoretically its the number of selected tables (see the FROM clause).</i>
	 * 
	 * @return	The number of aliased tables.
	 */
	public int getNbAliasedTables(){
		return lstTablesAlias().size();
	}
	
	/**
	 * Gets the alias corresponding to the given table name, if any.
	 * 
	 * @param name	The table name for which the alias is wanted.
	 * @return		The alias of the given table name, or <i>null</i> if not found.
	 */
	public String getTableAlias(String name){
		String alias = null;
		Iterator<Map.Entry<String,String> > it = lstTablesAlias().entrySet().iterator();
		
		while(alias == null && it.hasNext()){
			Map.Entry<String, String> item = it.next();
			if (item.getValue().equalsIgnoreCase(name))
				alias = item.getKey();
		}
		
		return alias;
	}
	
	/**
	 * Gets the table name corresponding to the given alias, if any.
	 * 
	 * @param alias	The alias for which the table name is wanted.
	 * @return		The table name of the given alias, or <i>null</i> if not found.
	 */
	public String getTableName(String alias){
		return lstTablesAlias().get(alias);
	}
	
	/**
	 * Does the last verifications on the whole query generated by the parser.
	 * 
	 * @param q					The query to check.
	 * @return					<i>true</i> if the query has passed successfully all the last modifications, <i>false</i> otherwise.
	 * @throws ParseException	If there is some error !
	 */
	public boolean queryVerif(ADQLQuery q) throws ParseException {
		// Nothing to do by default !
		return true;
	}

	/**
	 * Checks whether the given table exists.
	 * 
	 * @param tableName	The name of the table whose the existence must be checked.
	 * @return			<i>true</i> if the table exists, <i>false</i> else.
	 */
	public abstract boolean tableExists(String tableName);
	
	/**
	 * Adds in the available columns list all columns contained in the specified table.
	 * 
	 * @param tableAlias	The alias of the table.
	 */
	public abstract void addColumns(String tableAlias);

}
