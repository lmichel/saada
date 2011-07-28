package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * It represents any item of the clause FROM: a table name or a sub-query.<br />
 * A table reference may have an alias (MUST if it is a sub-query) and a join with another table.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public class ADQLTable implements ADQLObject {

	/**
	 * The name of the table (possible schema prefix (i.e. TAPSchema.ObsCore)).
	 */
	protected String tableRef;
	
	/**
	 * A sub-query as table.
	 */
	protected ADQLQuery subQuery;
	
	/**
	 * Label of the table reference.
	 */
	protected String alias = null;
	
	/**
	 * Join with another table.
	 */
	protected ADQLJoin join = null;
	
	
	/**
	 * Creates a reference to a table with its name.
	 * 
	 * @param table	Name of the table.
	 */
	public ADQLTable(String table){
		tableRef = table;
		subQuery = null;
	}
	
	/**
	 * Creates a reference to a sub-query.
	 * 
	 * @param query	Sub-query.
	 */
	public ADQLTable(ADQLQuery query){
		subQuery = query;
		tableRef = null;
	}
	
	/**
	 * Gets the name of the table.
	 * 
	 * @return Table name.
	 */
	public String getTable(){
		return tableRef;
	}

	/**
	 * Sets the name of the table.
	 * 
	 * @param newTableName	The new name of the table.
	 */
	public void setTable(String newTableName) {
		tableRef = newTableName;
	}
	
	/**
	 * Gets the sub-query used as table.
	 * 
	 * @return	Sub-query.
	 */
	public ADQLQuery getSubQuery(){
		return subQuery;
	}
	
	/**
	 * Tells whether this table reference is a sub-query or a table name/alias.
	 * 
	 * @return	<i>true</i> if this table is a sub-query, <i>false</i> else.
	 */
	public boolean isSubQuery(){
		return subQuery != null;
	}
	
	/**
	 * Gets the label of this table.
	 * 
	 * @return	Table label.
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * Sets the label of this table.
	 * 
	 * @param alias	Label to put on this table.
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * Gets the join definition of this table.
	 * 
	 * @return	Its join or <i>null</i> if none.
	 */
	public ADQLJoin getJoin() {
		return join;
	}

	/**
	 * Puts a join on this table.
	 * 
	 * @param join	Join to put.
	 */
	public void setJoin(ADQLJoin join) {
		this.join = join;
	}

	/**
	 * Gets the SQL expression of this table reference.
	 * 
	 * @return					Its SQL translation.
	 * @throws ParseException	If any error in the sub-query.
	 * @see ADQLObject#toSQL()
	 */
	public String toSQL() throws ParseException {
		return (isSubQuery()?("("+subQuery.toSQL(false)+")"):tableRef)+((alias==null)?"":(" AS \""+alias+"\""))+((join==null)?"":(" "+join.toSQL()));
	}

	/**
	 * Gets the SQL expression of this table reference.
	 * 
	 * @param translator 		The tool to use to translate this table in the good SQL dialect.
	 * @return					Its SQL translation.
	 * @throws ParseException	If any error in the sub-query.
	 * @see ADQLObject#toSQL(SQLTranslator)
	 */
	public String toSQL(SQLTranslator translator) throws ParseException {
		return (isSubQuery()?("("+subQuery.toSQL(false, translator)+")"):tableRef)+((alias==null)?"":(" AS "+alias))+((join==null)?"":(" "+join.toSQL(translator)));
	}
	
	/**
	 * Gets the ADQL expression of this table reference.
	 * 
	 * @return 	Its ADQL expression.
	 * @see ADQLObject#toString()
	 */
	public String toString(){
		return (isSubQuery()?("("+subQuery.toString()+")"):tableRef)+((alias==null)?"":(" AS "+alias))+((join==null)?"":(" "+join.toString()));
	}

	public String getADQLName() {
		return isSubQuery()?"TABLE_SUBQUERY":"TABLE_NAME";
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		
		if (join != null){
			if (searchCondition.match(join))
				vMatched.add(join);
			
			Vector<ADQLObject> temp = join.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (join != null){
			if (searchCondition.match(join))
				matchedObj = join;
			else
				matchedObj = join.getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject getCopy() throws ParseException {
		ADQLTable copy = (isSubQuery())?(new ADQLTable((ADQLQuery)subQuery.getCopy())):(new ADQLTable(tableRef));
		copy.setAlias(alias);
		if (join != null)
			copy.setJoin((ADQLJoin)join.getCopy());
		return copy;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (join != null){
			if (searchCondition.match(join)){
				matchedObj = join;
				join = null;
			}else
				matchedObj = join.remove(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (join != null){
			if (searchCondition.match(join) && replacementObject instanceof ADQLJoin){
				matchedObj = join;
				join = (ADQLJoin)replacementObject;
			}else
				matchedObj = join.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
}