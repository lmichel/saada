package adqlParser.query;

import java.util.Iterator;
import java.util.Vector;

import adqlParser.parser.AdqlParser;
import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Object representation of an ADQL query or sub-query.<br />
 * The resulting object of the {@link AdqlParser} is an object of this class.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public class ADQLQuery implements ADQLObject {

	/** List of all selected columns. */
	protected Vector<ADQLOperand> lstColumns;
	
	/** Distinct values for the first selected column ? */
	protected boolean distinct;
	
	/** List of all selected tables. */
	protected Vector<ADQLTable> lstTables;
	
	/** Line constraint (constraint of the clause WHERE). */
	protected ADQLConstraint where;
	
	/** The way to order the results. */
	protected Vector<ADQLOrder> order;
	
	/** List of all columns to use to group results. */
	protected Vector<ColumnReference> lstGroupBy;
	
	/** Group constraint (constraint of the clause HAVING). */
	protected ADQLConstraint having;
	
	/** Maximum number of results. */
	protected int limit;
	
	/** Special value of the attribute {@link ADQLQuery#limit} which means there is no limit. */
	public static final int NO_LIMIT = -1;
	
	/** A tool to translate this query in the good SQL dialect. */
	protected SQLTranslator translator = new SQLTranslator();
	
	
	/**
	 * Creates an empty query (no selected columns, no selected tables, no any kind of constraint, ...).
	 * 
	 * @see ADQLQuery#reset()
	 */
	public ADQLQuery() {
		reset();
	}
	
	/**
	 * Creates an empty query (no selected columns, no selected tables, no any kind of constraint, ...).
	 * 
	 * @param trans		The tool to use to translate this query in the good SQL dialect.
	 * 
	 * @see ADQLQuery#reset()
	 */
	public ADQLQuery(SQLTranslator trans) {
		reset();
		translator = trans;
	}
	
	/**
	 * Clears the query: no selected columns, no selected tables, no any kind of constraint, ...
	 */
	public void reset() {
		lstColumns = new Vector<ADQLOperand>();
		distinct = false;
		lstTables = new Vector<ADQLTable>();
		where = null;
		lstGroupBy = new Vector<ColumnReference>();
		having = null;
		order = new Vector<ADQLOrder>();
		limit = NO_LIMIT;
	}
	
	/**
	 * Gets the selected columns.
	 * 
	 * @return An iterator on selected columns.
	 */
	public Iterator<ADQLOperand> getColumns(){
		return lstColumns.iterator();
	}
	
	/**
	 * Gets the specified selected column.
	 * 
	 * @param indCol	Index of the wanted column.
	 * @return			The selected column whose the index is given or <i>null</i> if the index is incorrect.
	 */
	public ADQLOperand getColumn(int indCol){
		if (indCol >= 0 && indCol < lstColumns.size())
			return lstColumns.get(indCol);
		else
			return null;
	}
	
	/**
	 * Gets the number of selected columns.
	 * 
	 * @return	The number of selected columns.
	 */
	public int getNbColumns(){
		return lstColumns.size();
	}
	
	/**
	 * Adds the given operand as a selected column of this query.
	 * 
	 * @param col				The operand to add as a selected column.
	 * @throws ParseException	If <i>col</i> is null or if there is already a column with the same alias than the one of <i>col</i>.
	 */
	public void addSelectColumn(ADQLOperand col) throws ParseException {
		if (col == null)
			throw new ParseException("Impossible to add a NULL column !");
		
		boolean alreadyExist = false;
		for(int i=0; !alreadyExist && i<lstColumns.size(); i++){
			String alias = lstColumns.get(i).getAlias();
			alreadyExist = alias != null && alias.equalsIgnoreCase(col.getAlias());
		}
		
		if (alreadyExist)
			throw new ParseException("Impossible to add the column \""+col.toString()+"\" because there is already a column with the alias \""+col.getAlias()+"\" !");
		else
			lstColumns.add(col);
	}
	
	/**
	 * Removes the specified column selected in this query.
	 * 
	 * @param indCol	The index of the column to remove (from 1).
	 * @return			The removed column or <i>null</i> if the given index is incorrect.
	 */
	public ADQLOperand removeSelectColumn(int indCol) {
		indCol--;
		
		if (indCol < 0 || indCol >= lstColumns.size())
			return null;
		
		return lstColumns.remove(indCol);
	}
	
	/**
	 * Removes the specified column selected in this query.
	 * 
	 * @param colAlias	The alias of the column to remove.
	 * @return			The removed column or <i>null</i> if not found.
	 */
	public ADQLOperand removeSelectColumn(String colAlias) {
		if (colAlias == null)
			return null;
		
		int indCol = -1;
		for(int i=0; indCol == -1 && i < lstColumns.size(); i++)
			if (lstColumns.get(i).getAlias() != null && lstColumns.get(i).getAlias().equalsIgnoreCase(colAlias))
				indCol = i;
		
		return (indCol > -1)?lstColumns.remove(indCol):null;
	}
	
	/**
	 * Gets the list of selected tables.
	 * 
	 * @return	An iterator on the selected tables.
	 */
	public Iterator<ADQLTable> getTables(){
		return lstTables.iterator();
	}
	
	/**
	 * Gets the number of selected tables.
	 * 
	 * @return The number of selected tables (tables of the clause FROM).
	 */
	public int getNbTables(){
		return lstTables.size();
	}
	
	/**
	 * Adds the given table as a selected table of this query.
	 * 
	 * @param table				The table to add.
	 * @throws ParseException	If <i>table</i> is <i>null</i> or if it doesn't have a name or if it is a sub-query without an alias.
	 */
	public void addTable(ADQLTable table) throws ParseException {
		if (table == null || (!table.isSubQuery() && table.getTable().trim().length() == 0))
			throw new ParseException("Impossible to add the table \""+((table==null)?"NULL":table.toString())+"\" because this table has no name !");
		
		if (table.getAlias() == null){
			if (table.isSubQuery())
				throw new ParseException("Impossible to add the table \""+table.toString()+"\" because a subquery item of the clause FROM must have an alias !");
		}
		
		lstTables.add(table);
	}
	
	/**
	 * Removes the specified table selected in this query.
	 * 
	 * @param indTable	The index of the table to remove (from 1).
	 * @return			The removed table or <i>null</i> if the given index is incorrect.
	 */
	public ADQLTable removeTable(int indTable) {
		indTable--;
		
		if (indTable < 0 || indTable >= lstTables.size())
			return null;
		
		return lstTables.remove(indTable);
	}
	
	/**
	 * Removes the specified table selected in this query.
	 * 
	 * @param tableAlias	The alias of the table to remove.
	 * @return				The removed table or <i>null</i> if not found.
	 */
	public ADQLTable removeTable(String tableAlias) {
		if (tableAlias == null)
			return null;
		
		int indTable = -1;
		for(int i=0; indTable == -1 && i < lstTables.size(); i++)
			if (lstTables.get(i).getAlias() != null && lstTables.get(i).getAlias().equalsIgnoreCase(tableAlias))
				indTable = i;
		
		return (indTable > -1)?lstTables.remove(indTable):null;
	}
	
	/**
	 * Gets the first line constraint.<br />
	 * <i><u>Note:</u> to access the next constraints use the {@link ADQLConstraint#next()} function.</i>
	 * 
	 * @return The first constraint of the line constraints of this query.
	 */
	public ADQLConstraint getConstraint(){
		return where;
	}
	
	/**
	 * Adds the given constraint to the list of line constraints (constraints of the clause WHERE) with an <i>AND</i> keyword if there is already another line constraint.
	 * 
	 * @param cons	The constraint to add.
	 * 
	 * @see ADQLQuery#addConstraint(ADQLConstraint, boolean)
	 */
	public void addConstraint(ADQLConstraint cons){
		addConstraint(cons, false);
	}
	
	/**
	 * Adds the given constraint to the list of line constraints (constraints of the clause WHERE) with the specified keyword (<i>AND</i> or <i>OR</i>) if there is already another line constraint.
	 * 
	 * @param cons	The constraint to add.
	 * @param or	<i>true</i> to concatenate the current list of line constraints and the given constraint with an <i>OR</i> keyword, <i>false</i> to concatenate with an <i>AND</i> keyword.
	 */
	public void addConstraint(ADQLConstraint cons, boolean or){
		if (cons == null)
			return;
		
		if (where == null)
			where = cons;
		else
			where.addConstraint(cons, or);
	}
	
	/**
	 * Clears all line constraints of this query.<br />
	 * <i><u>Note:</u> to remove a specific constraint use the {@link ADQLConstraint#removeNext()} function on the parent of the constraint to remove.</i>
	 * 
	 * @return	The removed constraint.
	 */
	public ADQLConstraint clearConstraints(){
		ADQLConstraint constraint = where;
		where = null;
		return constraint;
	}
	
	/**
	 * Indicates whether distinct values are wanted for the first selected column of this query.
	 * 
	 * @return	<i>true</i> means there is a DISTINCT keyword, <i>false</i> else.
	 */
	public boolean hasDistinct(){
		return distinct;
	}
	
	/**
	 * Lets you indicate whether the values of the first selected column must be distinct.
	 * 
	 * @param distinctValues	<i>true</i> means this query must have a distinct values for the 1st selected column, <i>false</i> else.
	 */
	public void setDistinct(boolean distinctValues){
		distinct = distinctValues;
	}
	
	/**
	 * Gets the list of columns to use to group the results of the execution of this query.
	 * 
	 * @return	An iterator on the columns of the clause GROUP BY.
	 */
	public Iterator<ColumnReference> getGroupByList(){
		return lstGroupBy.iterator();
	}
	
	/**
	 * Adds a grouped column to this query.
	 * 
	 * @param colRef	The column to add as a GROUP BY item.
	 */
	public void addGroupBy(ColumnReference colRef){
		if (colRef != null)
			lstGroupBy.add(colRef);
	}
	
	/**
	 * Removes the specified GROUP BY item.
	 * 
	 * @param ind	The index of the item to remove (from 1).
	 * @return		The removed column reference.
	 */
	public ColumnReference removeGroupBy(int ind){
		ind--;
		
		if (ind < 0 || ind >= lstGroupBy.size())
			return null;
		
		return lstGroupBy.remove(ind);
	}
	
	/**
	 * Gets the first constraint of the group constraint of this query.
	 * <i><u>Note:</u> to access the next constraints use the {@link ADQLConstraint#next()} function.</i>
	 * 
	 * @return	The first group constraint of this query.
	 */
	public ADQLConstraint getHaving(){
		return having;
	}
	
	/**
	 * Adds a group constraint to this query (constraint of the clause HAVING) with an <i>AND</i> keyword if there is already another group constraint.
	 * 
	 * @param condition	The constraint to add.
	 * 
	 * @see ADQLQuery#addHaving(ADQLConstraint, boolean)
	 */
	public void addHaving(ADQLConstraint condition){
		addHaving(condition, false);
	}
	
	/**
	 * Adds a group constraint to this query (constraints of the clause HAVING) with the specified keyword (<i>AND</i> or <i>OR</i>) if there is already another group constraint.
	 * 
	 * @param condition	The constraint to add.
	 * @param or		<i>true</i> to concatenate the current list of group constraints and the given constraint with an <i>OR</i> keyword, <i>false</i> to concatenate with an <i>AND</i> keyword.
	 */
	public void addHaving(ADQLConstraint condition, boolean or){
		if (condition == null)
			return;
		
		if (having == null)
			having = condition;
		else
			having.addConstraint(condition, or);
	}
	
	/**
	 * Clears all group constraints of this query.<br />
	 * <i><u>Note:</u> to remove a specific constraint use the {@link ADQLConstraint#removeNext()} function on the parent of the constraint to remove.</i>
	 * 
	 * @return	The removed constraint.
	 */
	public ADQLConstraint clearHaving(){
		ADQLConstraint constraint = having;
		having = null;
		return constraint;
	}
	
	/**
	 * Indicates how the results of this query must sorted.
	 * 
	 * @return	An iterator on the order indications of this query.
	 */
	public Iterator<ADQLOrder> getOrderedColumns(){
		return order.iterator();
	}
	
	/**
	 * Indicates how the results must be sorted.
	 * 
	 * @param colOrder	The order indication to add.
	 */
	public void addOrder(ADQLOrder colOrder){
		order.add(colOrder);
	}
	
	/**
	 * Removes the specified ORDER BY item.
	 * 
	 * @param ind	The index of the item to remove (from 1).
	 * @return		The removed column reference.
	 */
	public ColumnReference removeOrder(int ind){
		ind--;
		
		if (ind < 0 || ind >= order.size())
			return null;
		
		return order.remove(ind);
	}
	
	public void clearOrderBy(){
		order.clear();
	}
	
	/**
	 * Gets the maximum number of wanted results.
	 * 
	 * @return	The maximum number of results or {@link ADQLQuery#NO_LIMIT} if none.
	 */
	public int getLimit(){
		return limit;
	}
	
	/**
	 * Sets the maximum number of results.
	 * 
	 * @param nbRowsReturned	The maximum number of results or {@link ADQLQuery#NO_LIMIT} if none.
	 */
	public void setLimit(int nbRowsReturned){
		limit = (nbRowsReturned<0)?NO_LIMIT:nbRowsReturned;
	}
	
	/**
	 * Indicates there must be no limit for the number of results.
	 * 
	 * @see ADQLQuery#setLimit(int)
	 * @see ADQLQuery#NO_LIMIT
	 */
	public void setNoLimit(){
		setLimit(NO_LIMIT);
	}
	
	/**
	 * @return The translator.
	 */
	public final SQLTranslator getTranslator() {
		return translator;
	}

	/**
	 * @param translator The translator to set.
	 */
	public final void setTranslator(SQLTranslator translator) {
		this.translator = translator;
	}

//	public Vector<ADQLColumn> getAllImpliedColumns(){
//		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
//		Vector<ADQLColumn> temp = null;
//		
//		// SELECT:
//		for(ADQLOperand op : lstColumns){
//			temp = op.getAllImpliedColumns();
//			if (temp != null && temp.size() > 0)
//				vColumns.addAll(temp);
//		}
//		
//		// WHERE:
//		if (where != null){
//			temp = where.getAllImpliedColumns();
//			if (temp != null && temp.size() > 0)
//				vColumns.addAll(temp);
//		}
//		
//		// HAVING:
//		if (where != null){
//			temp = where.getAllImpliedColumns();
//			if (temp != null && temp.size() > 0)
//				vColumns.addAll(temp);
//		}
//		
//		return vColumns;
//	}
	
	/**
	 * Gets the SQL translation of this ADQL query.
	 * 
	 * @return					The corresponding SQL query.
	 * @throws ParseException	If there is any error during the translation.
	 * 
	 * @see adqlParser.query.ADQLObject#toSQL(adqlParser.parser.SQLTranslator)
	 * @see ADQLQuery#toSQL(boolean, SQLTranslator)
	 */
	public String toSQL() throws ParseException{
		return toSQL(true, translator);
	}
	
	/**
	 * Gets the SQL translation of this ADQL query.
	 * 
	 * @param end				<i>true</i> to end the SQL query by a semicolon, <i>false</i> else.
	 * @return					The corresponding SQL query.
	 * @throws ParseException	If there is any error during the translation.
	 * 
	 * @see ADQLQuery#toSQL(boolean, SQLTranslator)
	 */
	public String toSQL(boolean end) throws ParseException{
		return toSQL(end, translator);
	}

	/**
	 * Gets the SQL translation of this ADQL query.
	 * 
	 * @param altTranslator		The translator to use to translate this ADQL query in SQL.
	 * @return 					The corresponding SQL query.
	 * @throws ParseException	If there is any error during the translation.
	 * 
	 * @see adqlParser.query.ADQLObject#toSQL(adqlParser.parser.SQLTranslator)
	 * @see ADQLQuery#toSQL(boolean, SQLTranslator)
	 */
	public String toSQL(SQLTranslator altTranslator) throws ParseException {
		return toSQL(true, altTranslator);
	}
	
	/**
	 * Gets the SQL translation of this ADQL query.
	 * 
	 * @param end				<i>true</i> to end the SQL query by a semicolon, <i>false</i> else.
	 * @param altTranslator		The translator to use to translate this ADQL query in SQL.
	 * @return					The corresponding SQL query.
	 * @throws ParseException	If there is any error during the translation.
	 */
	public String toSQL(boolean end, SQLTranslator altTranslator) throws ParseException{
		String sql = null;
		
	// SELECT part:
		String columns = null;
		for(ADQLOperand col : lstColumns)
			columns = (columns == null)?col.toSQL(altTranslator):(columns + ", "+col.toSQL(altTranslator));
		if (columns == null)
			columns = "*";
		sql = "SELECT "+(distinct?"DISTINCT ":"")+columns;
		
	// FROM part:
		String tables = null;
		for(ADQLTable t : lstTables)
			tables = (tables==null)?t.toSQL(altTranslator):(tables + ", "+t.toSQL(altTranslator));
		
		sql += "\nFROM "+tables;
		
	// WHERE part:
		if (where != null)
			sql += "\nWHERE "+where.toSQL(altTranslator);
		
	// GROUP BY part:
		String groupBy = null;
		for(ColumnReference colRef : lstGroupBy)
			groupBy = (groupBy==null)?colRef.toSQL():(groupBy+", "+colRef.toSQL());
		if (groupBy != null)
			sql += "\nGROUP BY "+groupBy;
		
	// HAVING part:
		if (having != null)
			sql += "\nHAVING "+having.toSQL(altTranslator);
		
	// ORDER BY part:
		String orderList = null;
		for(ADQLOrder colOrder : order)
			orderList = (orderList == null)?colOrder.toSQL():(orderList + ", "+colOrder.toSQL());
		if (orderList != null)
			sql += "\nORDER BY "+orderList;
		
	// LIMIT part:
		sql += (limit > NO_LIMIT)?("\nLIMIT "+limit):"";
		
		return sql + (end?";":"");
	}
	
	public String toString(){
		String adql = null;
		
	// SELECT part:
		String columns = null;
		for(ADQLOperand col : lstColumns)
			columns = (columns == null)?col.toString():(columns + ", "+col.toString());
		if (columns == null)
			columns = "*";
		adql = "SELECT "+(distinct?"DISTINCT ":"")+((limit > NO_LIMIT)?("TOP "+limit+" "):"")+columns;
		
	// FROM part:
		String tables = null;
		for(ADQLTable t : lstTables)
			tables = (tables==null)?t.toString():(tables + ", "+t.toString());
		adql += "\nFROM "+tables;
		
	// WHERE part:
		if (where != null)
			adql += "\nWHERE "+where.toString();
		
	// GROUP BY part:
		String groupBy = null;
		for(ColumnReference colRef : lstGroupBy)
			groupBy = (groupBy==null)?colRef.toString():(groupBy + ", "+colRef.toString());
		if (groupBy != null)
			adql += "\nGROUP BY "+groupBy.toString();
		
	// HAVING part:
		if (having != null)
			adql += "\nHAVING "+having.toString();
		
	// ORDER BY part:
		String orderList = null;
		for(ADQLOrder colOrder : order)
			orderList = (orderList == null)?colOrder.toString():(orderList + ", "+colOrder.toString());
		if (orderList != null)
			adql += "\nORDER BY "+orderList;
		
		return adql;
	}

	public String getADQLName() {
		return "QUERY";
	}

	public ADQLObject getCopy() throws ParseException {
		ADQLQuery copy = (translator == null)?(new ADQLQuery()):(new ADQLQuery(translator.getCopy()));
		
		// SELECT:
		for(ADQLOperand op: lstColumns)
			copy.addSelectColumn((ADQLOperand)op.getCopy());
		
		// DISTINCT:
		copy.setDistinct(distinct);
		
		// FROM:
		for(ADQLTable table : lstTables)
			copy.addTable((ADQLTable)table.getCopy());
		
		// WHERE:
		if (where != null)
			copy.addConstraint((ADQLConstraint)where.getCopy());
		
		// GROUP BY:
		for(ColumnReference colRef : lstGroupBy)
			copy.addGroupBy((ColumnReference)colRef.getCopy());
		
		// HAVING:
		if (having != null)
			copy.addHaving((ADQLConstraint)having.getCopy());
		
		// ORDER BY:
		for(ADQLOrder orderItem : order)
			copy.addOrder((ADQLOrder)orderItem.getCopy());
		
		// LIMIT:
		copy.setLimit(limit);
		
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		Vector<ADQLObject> temp = null;
		
		// SELECT:
		for(ADQLOperand op : lstColumns){
			if (searchCondition.match(op))
				vMatched.add(op);
			temp = op.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		// FROM:
		for(ADQLTable table : lstTables){
			if (searchCondition.match(table))
				vMatched.add(table);
			temp = table.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		// WHERE:
		if (where != null){
			if (searchCondition.match(where))
				vMatched.add(where);
			temp = where.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		// GROUP BY:
		for(ColumnReference colRef : lstGroupBy){
			if (searchCondition.match(colRef))
				vMatched.add(colRef);
			temp = colRef.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		// HAVING:
		if (having != null){
			if (searchCondition.match(having))
				vMatched.add(having);
			temp = having.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		// ORDER BY:
		for(ADQLOrder orderItem : order){
			if (searchCondition.match(orderItem))
				vMatched.add(orderItem);
			temp = orderItem.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		// SELECT:
		Iterator<ADQLOperand> itOp = lstColumns.iterator();
		while(matchedObj == null && itOp.hasNext()){
			ADQLOperand op = itOp.next();
			if (searchCondition.match(op))
				matchedObj = op;
			else
				matchedObj = op.getFirst(searchCondition);
		}
		
		// FROM:
		Iterator<ADQLTable> itTables = lstTables.iterator();
		while(matchedObj == null && itTables.hasNext()){
			ADQLTable table = itTables.next();
			if (searchCondition.match(table))
				matchedObj = table;
			else
				matchedObj = table.getFirst(searchCondition);
		}
		
		// WHERE:
		if (matchedObj == null && where != null){
			if (searchCondition.match(where))
				matchedObj = where;
			else
				matchedObj = where.getFirst(searchCondition);
		}
		
		// GROUP BY:
		Iterator<ColumnReference> itColRef = lstGroupBy.iterator();
		while(matchedObj == null && itColRef.hasNext()){
			ColumnReference colRef = itColRef.next();
			if (searchCondition.match(colRef))
				matchedObj = colRef;
			else
				matchedObj = colRef.getFirst(searchCondition);
		}
		
		// HAVING:
		if (matchedObj == null && having != null){
			if (searchCondition.match(having))
				matchedObj = having;
			else
				matchedObj = having.getFirst(searchCondition);
		}
		
		// ORDER BY:
		Iterator<ADQLOrder> itOrder = order.iterator();
		while(matchedObj == null && itOrder.hasNext()){
			ADQLOrder orderItem = itOrder.next();
			if (searchCondition.match(orderItem))
				matchedObj = orderItem;
			else
				matchedObj = orderItem.getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = null;
		
		// SELECT:
		for(int i=0; matchedObj == null && i < lstColumns.size(); i++){
			ADQLOperand op = lstColumns.get(i);
			if (searchCondition.match(op))
				matchedObj = lstColumns.remove(i);
			else
				matchedObj = op.remove(searchCondition);
		}
		
		// FROM:
		for(int i=0; matchedObj == null && i < lstTables.size(); i++){
			ADQLTable table = lstTables.get(i);
			if (searchCondition.match(table))
				matchedObj = lstTables.remove(i);
			else
				matchedObj = table.remove(searchCondition);
		}
		
		// WHERE:
		if (matchedObj == null && where != null){
			if (searchCondition.match(where)){
				matchedObj = where;
				where = where.next();
				((ADQLConstraint)matchedObj).nextConstraint = null;
			}else
				matchedObj = where.remove(searchCondition);
		}
		
		// GROUP BY:
		for(int i=0; matchedObj == null && i < lstGroupBy.size(); i++){
			ColumnReference colRef = lstGroupBy.get(i);
			if (searchCondition.match(colRef))
				matchedObj = lstGroupBy.remove(i);
			else
				matchedObj = colRef.remove(searchCondition);
		}
		
		// HAVING:
		if (matchedObj == null && having != null){
			if (searchCondition.match(having)){
				matchedObj = having;
				having = having.next();
				((ADQLConstraint)matchedObj).nextConstraint = null;
			}else
				matchedObj = having.remove(searchCondition);
		}
		
		// ORDER BY:
		for(int i=0; matchedObj == null && i < order.size(); i++){
			ADQLOrder orderItem = order.get(i);
			if (searchCondition.match(orderItem))
				matchedObj = order.remove(i);
			else
				matchedObj = orderItem.remove(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		// SELECT:
		for(int i=0; matchedObj == null && i < lstColumns.size(); i++){
			ADQLOperand op = lstColumns.get(i);
			if (searchCondition.match(op) && replacementObject instanceof ADQLOperand){
				matchedObj = op;
				lstColumns.setElementAt((ADQLOperand)replacementObject, i);
			}else
				matchedObj = op.replaceBy(searchCondition, replacementObject);
		}
		
		// FROM:
		for(int i=0; matchedObj == null && i < lstTables.size(); i++){
			ADQLTable table = lstTables.get(i);
			if (searchCondition.match(table) && replacementObject instanceof ADQLTable){
				matchedObj = table;
				lstTables.setElementAt((ADQLTable)replacementObject, i);
			}else
				matchedObj = table.replaceBy(searchCondition, replacementObject);
		}
		
		// WHERE:
		if (matchedObj == null && where != null){
			if (searchCondition.match(where) && replacementObject instanceof ADQLConstraint){
				((ADQLConstraint)replacementObject).addConstraint(where.next());
				where.nextConstraint = null;
				matchedObj = where;
				where = (ADQLConstraint)replacementObject;
			}else
				matchedObj = where.replaceBy(searchCondition, replacementObject);
		}
		
		// GROUP BY:
		for(int i=0; matchedObj == null && i < lstGroupBy.size(); i++){
			ColumnReference colRef = lstGroupBy.get(i);
			if (searchCondition.match(colRef) && replacementObject instanceof ColumnReference){
				matchedObj = colRef;
				lstGroupBy.setElementAt((ColumnReference)replacementObject, i);
			}else
				matchedObj = colRef.replaceBy(searchCondition, replacementObject);
		}
		
		// HAVING:
		if (matchedObj == null && having != null){
			if (searchCondition.match(having)){
				((ADQLConstraint)replacementObject).addConstraint(having.next());
				having.nextConstraint = null;
				matchedObj = having;
				having = (ADQLConstraint)replacementObject;
			}else
				matchedObj = having.replaceBy(searchCondition, replacementObject);
		}
		
		// ORDER BY:
		for(int i=0; matchedObj == null && i < order.size(); i++){
			ADQLOrder orderItem = order.get(i);
			if (searchCondition.match(orderItem) && replacementObject instanceof ADQLOrder){
				matchedObj = orderItem;
				order.setElementAt((ADQLOrder)replacementObject, i);
			}else
				matchedObj = orderItem.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
	
	public static void main(String[] args) throws Exception {
		ADQLQuery query = new ADQLQuery();

		query.addSelectColumn(new ADQLConstantValue("123", ADQLType.INTEGER));
		ADQLOperand opUseless = new ADQLConstantValue("Blabla");
		opUseless.setAlias("USELESS");
		query.addSelectColumn(opUseless);
		query.addSelectColumn(new ADQLColumn("colonne1"));
		query.addSelectColumn(new adqlParser.query.function.SQLFunction(adqlParser.query.function.SQLFunctionType.SUM, new ADQLColumn("numVal")));
		query.addSelectColumn(new Operation(new ADQLColumn("numVal"), OperationType.MULT, new ADQLConstantValue("3.14", ADQLType.DOUBLE)));
		query.addSelectColumn(new Concatenation(new ADQLConstantValue("Hello "), new ADQLConstantValue(" you !")));
		
		query.setDistinct(true);
		query.setLimit(100);
		
		query.addTable(new ADQLTable("MyTable"));
		ADQLQuery q = new ADQLQuery();
		q.addSelectColumn(new ADQLColumn("truc"));
		q.addSelectColumn(new ADQLColumn("machin"));
		q.addTable(new ADQLTable("BiduleTable"));
		ADQLTable t = new ADQLTable(q);
		t.setAlias("Bidule");
		Vector<String> joinedColumns = new Vector<String>();
		joinedColumns.add("oidsaada"); joinedColumns.add("ra"); joinedColumns.add("dec");
		t.setJoin(new ADQLJoin(JoinType.INNER, new ADQLTable("Chose"), joinedColumns));
		query.addTable(t);
		
		query.addConstraint(new ADQLComparison(new ADQLColumn("numVal"), ComparisonOperator.GREATER_OR_EQUAL, new Operation(new ADQLConstantValue("3.14", ADQLType.DOUBLE), OperationType.DIV, new ADQLConstantValue("10", ADQLType.INTEGER))));
		query.addConstraint(new adqlParser.query.function.geometry.ContainsFunction(new adqlParser.query.function.geometry.PointFunction(new ADQLConstantValue("ICRS GEOCENTER"), new ADQLColumn("ra"), new ADQLColumn("dec")), new adqlParser.query.function.geometry.CircleFunction(new ADQLConstantValue("ICRS GEOCENTER"), new ADQLConstantValue("277.925", ADQLType.DOUBLE), new ADQLConstantValue("-19.1166667", ADQLType.DOUBLE), new ADQLConstantValue("0.1", ADQLType.DOUBLE))));
		ADQLConstraint const1 = new ADQLGroup(query.getConstraint());
		query.clearConstraints();
		query.addConstraint(const1);
		query.addConstraint(new IsNull(new ADQLColumn("exists ?")), true);
		query.addConstraint(new ADQLComparison(new ADQLColumn("distance"), ComparisonOperator.LESS_THAN, new ADQLConstantValue("10.5", ADQLType.DOUBLE)), false);
		
		query.addGroupBy(new ColumnReference("truc"));
		query.addGroupBy(new ColumnReference(2));
		
		query.addHaving(new ADQLComparison(new adqlParser.query.function.SQLFunction(adqlParser.query.function.SQLFunctionType.COUNT), ComparisonOperator.LESS_THAN, new ADQLConstantValue("100", ADQLType.INTEGER)));
		
		query.addOrder(new ADQLOrder(3));
		query.addOrder(new ADQLOrder("ra", true));
		
		System.out.println("*** ADQL Query:\n"+query+"\n\n*** SQL Translation:\n"+query.toSQL());
		//query.removeSelectColumn(2);
		query.remove(new SearchHandler() {
			
			public boolean match(ADQLObject obj) {
				return (obj instanceof ADQLOperand) && ((ADQLOperand)obj).getAlias() != null && ((ADQLOperand)obj).getAlias().equals("USELESS");
			}
		});
		System.out.println("\n*** AFTER deleting the column \"USELESS\":\n*** ADQL Query:\n"+query+"\n\nSQL Translation:\n"+query.toSQL());
		
		System.out.println("\n*** ALL IMPLIED COLUMNS ***"); //query.getAllImpliedColumns();
		Vector<ADQLObject> vCols = query.getAll(new SearchHandler() {
			
			public boolean match(ADQLObject obj) {
				return obj instanceof ADQLColumn;
			}
		});
		if (vCols != null)
			for(ADQLObject col : vCols)
				System.out.println("\t- "+col);
	}

}