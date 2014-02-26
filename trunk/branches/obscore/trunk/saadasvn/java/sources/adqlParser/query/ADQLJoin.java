package adqlParser.query;

import java.util.Iterator;
import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Defines a join with one table.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLTable
 */
public class ADQLJoin implements ADQLObject {

	/** Natural join (use of table keys) ? */
	protected boolean natural;
	
	/** Type of the join ({@link JoinType}). */
	protected JoinType joinType;
	
	/** The table used to make the join. */
	protected ADQLTable joinedTable;
	
	/** The join condition. */
	protected ADQLConstraint condition;
	
	/** Lists of columns (name) on which the join must be done. */
	protected Vector<String> lstColumns;
	
	
	/**
	 * Creates a <b>natural</b> join with the given table.
	 * 
	 * @param type	The join type.
	 * @param table	The joined table.
	 */
	public ADQLJoin(JoinType type, ADQLTable table) {
		natural = true;
		joinType = type;
		joinedTable = table;
		condition = null;
		lstColumns = null;
	}

	/**
	 * Creates a join with the given table by respecting the given condition.
	 * 
	 * @param type		The join type.
	 * @param table		The joined table.
	 * @param condition	The join condition.
	 */
	public ADQLJoin(JoinType type, ADQLTable table, ADQLConstraint condition) {
		natural = false;
		joinType = type;
		joinedTable = table;
		this.condition = condition;
		lstColumns = null;
	}
	
	/**
	 * Creates a join with the given table on the given columns.
	 * 
	 * @param type		The join type.
	 * @param table		The joined table.
	 * @param columns	The list of columns on which the join must be done.
	 */
	public ADQLJoin(JoinType type, ADQLTable table, Vector<String> columns) {
		natural = false;
		joinType = type;
		joinedTable = table;
		lstColumns = columns;
		condition = null;
	}
	
	/**
	 * Indicates whether this join is natural or not.
	 * 
	 * @return <i>true</i> means this join is natural, <i>false</i> else.
	 */
	public boolean isNatural() {
		return natural;
	}

	/**
	 * Lets indicating that this join is natural (it must use the table keys).
	 * 
	 * @param natural <i>true</i> means this join must be natural, <i>false</i> else.
	 */
	public void setNatural(boolean natural) {
		this.natural = natural;
	}

	/**
	 * Gets the type of this join.
	 * 
	 * @return Its join type.
	 */
	public JoinType getJoinType() {
		return joinType;
	}

	/**
	 * Sets the type of this join.
	 * 
	 * @param joinType The join type to set.
	 */
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	/**
	 * Gets the table used to make the join.
	 * 
	 * @return The joined table.
	 */
	public ADQLTable getJoinedTable() {
		return joinedTable;
	}

	/**
	 * Gets the condition of this join.
	 * 
	 * @return The join condition.
	 */
	public ADQLConstraint getCondition() {
		return condition;
	}
	
	/**
	 * Gets the list of all columns on which the join is done.
	 * 
	 * @return	The joined columns.
	 */
	public Iterator<String> getLstColumns(){
		return lstColumns.iterator();
	}

	/**
	 * Gets the SQL expression of this join.
	 *  
	 * @return					Its SQL translation.
	 * @throws ParseException	If any error in the joined table if this one is a sub-query.
	 * @see ADQLObject#toSQL()
	 */
	public String toSQL() throws ParseException {
		String sql = (natural?"NATURAL ":"")+joinType.toString()+" JOIN "+joinedTable.toSQL()+" ";
			
		if (condition != null)
			sql += "ON "+condition.toSQL();
		else if (lstColumns != null){
			String cols = null;
			for(String item : lstColumns)
				cols = (cols==null)?("\""+item+"\""):(cols+", \""+item+"\"");
			sql += "USING ("+cols+")";
		}
		
		return sql;
	}

	/**
	 * Gets the SQL expression of this join.
	 * 
	 * @param translator 		The tool to use to translate this join in SQL.
	 * 
	 * @return					Its SQL translation.
	 * @throws ParseException	If any error in the joined table if this one is a sub-query.
	 * @see ADQLObject#toSQL(SQLTranslator)
	 */
	public String toSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getJoin(this);
		if (sql == null){
			sql = (natural?"NATURAL ":"")+joinType.toString()+" JOIN "+joinedTable.toSQL(translator)+" ";
			
			if (condition != null)
				sql += "ON "+condition.toSQL(translator);
			else if (lstColumns != null){
				String cols = null;
				for(String item : lstColumns)
					cols = (cols==null)?("\""+item+"\""):(cols+", \""+item+"\"");
				sql += "USING ("+cols+")";
			}
			
			return sql;
		}else
			return sql;
	}
	
	/**
	 * Gets the ADQL expression of this join.
	 * 
	 * @return	Its ADQL expression.
	 */
	public String toString(){
		String adql = (natural?"NATURAL ":"")+joinType.toString()+" JOIN "+joinedTable.toString()+" ";
		
		if (condition != null)
			adql += "ON "+condition.toString();
		else if (lstColumns != null) {
			String cols = null;
			for(String item : lstColumns)
				cols = (cols==null)?("\""+item+"\""):(cols+", \""+item+"\"");
			adql += "USING ("+cols+")";
		}
		
		return adql;
	}

	public String getADQLName() {
		return ((joinType != null)?(joinType.toString()+" "):" ")+"JOIN";
	}

	@SuppressWarnings("unchecked")
	public ADQLObject getCopy() throws ParseException {
		ADQLJoin copy = null;
		JoinType typeCopy = JoinType.valueOf(joinType.toString());
		
		if (natural){
			copy = new ADQLJoin(typeCopy, (ADQLTable)joinedTable.getCopy());
		}else{
			if (condition != null)
				copy = new ADQLJoin(typeCopy, (ADQLTable)joinedTable.getCopy(), (ADQLConstraint)condition.getCopy());
			else
				copy = new ADQLJoin(typeCopy, (ADQLTable)joinedTable.getCopy(), (Vector<String>)lstColumns.clone());
		}
		
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		
		if (searchCondition.match(joinedTable))
			vMatched.add(joinedTable);
		
		Vector<ADQLObject> temp = joinedTable.getAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		if (condition != null){
			if (searchCondition.match(condition))
				vMatched.add(condition);
			
			temp = condition.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(joinedTable))
			matchedObj = joinedTable;
		else
			matchedObj = joinedTable.getFirst(searchCondition);
			
		if (matchedObj == null && condition != null){
			if (searchCondition.match(condition))
					matchedObj = condition;
			else
				matchedObj = condition.getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = joinedTable.remove(searchCondition);
		
		if (matchedObj == null && condition != null)
			matchedObj = condition.remove(searchCondition);
		
		return matchedObj;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(joinedTable) && replacementObject instanceof ADQLTable){
			matchedObj = joinedTable;
			joinedTable = (ADQLTable)replacementObject;
		}else
			matchedObj = joinedTable.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null && condition != null){
			if (searchCondition.match(condition) && replacementObject instanceof ADQLConstraint){
				matchedObj = condition;
				condition = (ADQLConstraint)replacementObject;
			}else
				matchedObj = condition.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
	
}