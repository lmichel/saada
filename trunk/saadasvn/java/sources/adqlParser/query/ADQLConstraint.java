package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * <p>Represents any kind of constraint.</p>
 * <i>To take in account the NOT keyword you just have to use the {@link ADQLConstraint#setNot(boolean)} function.</i>
 * <p>A list of constraints is implemented by a linked list. Thus to add a constraint to a list you must use on the parent constraint
 * the {@link ADQLConstraint#addConstraint(ADQLConstraint)} <i>(the concatenation operator is AND)</i> or the
 * {@link ADQLConstraint#addConstraint(ADQLConstraint,boolean)} <i>(the concatenation operator is specified by the 2nd parameter)</i> functions.</p>
 * 
 * getCopy: add antConcat parmeter to the call to addConstraint
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public abstract class ADQLConstraint implements ADQLObject {

	/** Indicates the presence of the NOT keyword for this constraint. */
	protected boolean isNot = false;

	/** The constraint following this constraint. */
	protected ADQLConstraint nextConstraint = null;
	
	/** The logical operator between this constraint and the next one in the linked list: <i>true</i> for AND, <i>false</i> for OR. */
	protected boolean andConcat = true;
	
	
	/**
	 * Tells whether the result of this constraint must be reversed.
	 * 
	 * @return <i>true</i> there is a NOT, <i>false</i> else.
	 */
	public boolean isNot() {
		return isNot;
	}

	/**
	 * Lets you tell whether the result of this constraint must be reversed or not.
	 * 
	 * @param isNot <i>true</i> to force the presence of NOT, <i>false</i> else.
	 */
	public void setNot(boolean isNot) {
		this.isNot = isNot;
	}
	
	/**
	 * Adds a constraint at the end of the linked list with AND.
	 * 
	 * @param cons	The constraint to add at the end of this list.
	 */
	public void addConstraint(ADQLConstraint cons){
		addConstraint(cons, false);
	}
	
	/**
	 * Adds a constraint at the end of the linked list with the specified logical operator.
	 * 
	 * @param cons		The constraint to add at the end of the list.
	 * @param orConcat	<i>true</i> for OR, <i>false</i> for AND.
	 */
	public void addConstraint(ADQLConstraint cons, boolean orConcat){
		if (nextConstraint == null){
			nextConstraint = cons;
			andConcat = !orConcat;
		}else
			nextConstraint.addConstraint(cons, orConcat);		
	}
	
	/**
	 * Gets the constraint which just follows this one in the linked list.
	 * 
	 * @return	The next constraint.
	 */
	public ADQLConstraint next(){
		return nextConstraint;
	}
	
	/**
	 * Removes the association between this constraint and the next one in the linked stack.
	 */
	public void removeNext(){
		if (nextConstraint != null)
			nextConstraint = nextConstraint.next();
	}

	/**
	 * Tells whether the logical operator between this constraint and the next one in the list is AND.<br />
	 * <i><u>Note:</u> whatever the returned value it doesn't mean this constraint is followed by another constraint (that's to say <i>next()</i> may be <i>null</i>).</i>
	 * 
	 * @return <i>true</i> the logical operator is AND, <i>false</i> else.
	 */
	public boolean isAndConcat() {
		return andConcat;
	}

	/**
	 * Forces the use of the specified logical operator between this constraint and the next one in the linked list.
	 * 
	 * @param andConcat <i>true></i> for AND, <i>false</i> else.
	 */
	public void setAndConcat(boolean andConcat) {
		this.andConcat = andConcat;
	}
	
	/**
	 * Gets all column references contained into this constraint and into the next constraints in the linked list.
	 * 
	 * @return	All column references or <i>null</i> or an empty vector if there is no column reference.
	 */
	public final Vector<ADQLColumn> getAllImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		
		Vector<ADQLColumn> temp = primaryGetImpliedColumns();
		if (temp != null && temp.size() > 0)
			vColumns.addAll(temp);
		
		if (nextConstraint != null){
			temp = nextConstraint.getAllImpliedColumns();
			if (temp != null && temp.size() > 0)
				vColumns.addAll(temp);
		}
		
		return vColumns;
	}
	
	public final Vector<ADQLObject> getAll(SearchHandler searchCondition){
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		
		Vector<ADQLObject> temp = primaryGetAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		if (nextConstraint != null){
			if (searchCondition.match(nextConstraint))
				vMatched.add(nextConstraint);
			
			temp = nextConstraint.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}
	
	public abstract Vector<ADQLObject> primaryGetAll(SearchHandler searchCondition);
	
	public final ADQLObject getFirst(SearchHandler searchCondition){
		ADQLObject matchedObj = primaryGetFirst(searchCondition);
		
		if (matchedObj == null && nextConstraint != null){
			if (searchCondition.match(nextConstraint))
				matchedObj = nextConstraint;
			else
				matchedObj = nextConstraint.getFirst(searchCondition);
		}
		
		return matchedObj;
	}
	
	public abstract ADQLObject primaryGetFirst(SearchHandler searchCondition);
	
	/**
	 * Gets all column references contained ONLY into this constraint.
	 * 
	 * @return	All column references or <i>null</i> or an empty vector if there is no column reference.
	 */
	public abstract Vector<ADQLColumn> primaryGetImpliedColumns();
	
	public final String toSQL() throws ParseException {
		return (isNot?"NOT ":"")+primaryToSQL()+((nextConstraint==null)?"":((andConcat?" AND ":" OR ")+nextConstraint.toSQL()));
	}
	
	public abstract String primaryToSQL() throws ParseException;
	
	/**
	 * Gets the SQL expression of this constraints list.
	 * @param translator		A tool to translate this constraint in the good SQL dialect.
	 * 
	 * @return 					Its SQL translation.
	 * @throws ParseException	If any error during the translation of this constraint.
	 * @see ADQLObject#toSQL(SQLTranslator)
	 */
	public final String toSQL(SQLTranslator translator) throws ParseException {
		return (isNot?"NOT ":"")+primaryToSQL(translator)+((nextConstraint==null)?"":((andConcat?" AND ":" OR ")+nextConstraint.toSQL(translator)));
	}
	
	/**
	 * Gets the SQL expression ONLY of THIS constraint (NOT the next ones).
	 * @param translator		A tool to translate this constraint in the good SQL dialect.
	 * 
	 * @return					Its SQL translation.
	 * @throws ParseException	If any error during the translation of this constraint.
	 */
	public abstract String primaryToSQL(SQLTranslator translator) throws ParseException;
	
	/**
	 * Gets the ADQL expression of this constraints list.
	 * 
	 * @return	Its ADQL expression.
	 * @see ADQLObject#toString()
	 */
	public final String toString(){
		return (isNot?"NOT ":"")+primaryToString()+((nextConstraint==null)?"":((andConcat?" AND ":" OR ")+nextConstraint.toString()));
	}
	
	/**
	 * Gets the ADQL expression ONLY of THIS constraint (NOT the next ones).
	 * 
	 * @return	Its ADQL expression.
	 */
	public abstract String primaryToString();

	public final ADQLObject getCopy() throws ParseException {
		ADQLConstraint copy = primaryGetCopy();
		if (copy != null){
			copy.setNot(isNot);
			copy.setAndConcat(andConcat);
			if (nextConstraint != null)
				copy.addConstraint((ADQLConstraint)nextConstraint.getCopy(),!andConcat);
		}
		return copy;
	}
	
	public abstract ADQLConstraint primaryGetCopy() throws ParseException;

	public final ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = primaryRemove(searchCondition);
		
		if (matchedObj == null && nextConstraint != null){
			if (searchCondition.match(nextConstraint)){
				matchedObj = nextConstraint;
				removeNext();
				((ADQLConstraint)matchedObj).nextConstraint = null;
			}else
				matchedObj = nextConstraint.remove(searchCondition);
		}
		
		return matchedObj;
	}
	
	public abstract ADQLObject primaryRemove(SearchHandler searchCondition) throws ParseException;

	public final ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = primaryReplaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null && nextConstraint != null){
			if (searchCondition.match(nextConstraint) && replacementObject instanceof ADQLConstraint){
				((ADQLConstraint)replacementObject).addConstraint(nextConstraint.next());
				nextConstraint.nextConstraint = null;
				matchedObj = nextConstraint;
				nextConstraint = (ADQLConstraint)replacementObject;
			}else
				matchedObj = nextConstraint.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
	
	public abstract ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException;
	
}