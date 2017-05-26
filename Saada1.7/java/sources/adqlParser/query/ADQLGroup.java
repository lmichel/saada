package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Represents a parenthesized list of constraints.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLConstraint
 */
public class ADQLGroup extends ADQLConstraint {

	/** The linked list of constraints within the parenthesis. */
	protected ADQLConstraint internConstraint;
	
	
	/**
	 * Creates an empty parenthesized list of constraints.
	 */
	public ADQLGroup(){
		internConstraint = null;
	}
	
	/**
	 * Creates a parenthesized list of constraints with the given one.
	 * 
	 * @param cons	The intern linked list of constraints.
	 */
	public ADQLGroup(ADQLConstraint cons){
		internConstraint = cons;
	}
	
	/**
	 * Adds a constraint at the end of the intern linked list with the logical operator AND.
	 * 
	 * @param cons	The constraint to add.
	 */
	public void addInternConstraint(ADQLConstraint cons){
		addInternConstraint(cons, false);
	}
	
	/**
	 * Adds a constraint at the end of the intern linked list with the specified logical operator.
	 * 
	 * @param cons		The constraint to add.
	 * @param orConcat	<i>true</i> means OR, <i>false</i> means AND.
	 */
	public void addInternConstraint(ADQLConstraint cons, boolean orConcat){
		internConstraint.addConstraint(cons, orConcat);
	}
	
	/**
	 * Gets the linked list of constraints within the parenthesis.
	 * 
	 * @return	The intern constraints.
	 */
	public ADQLConstraint getInternConstraint(){
		return internConstraint;
	}

	@Override
	public Vector<ADQLColumn> primaryGetImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		
		if (internConstraint != null){
			Vector<ADQLColumn> temp = internConstraint.getAllImpliedColumns();
			if (temp != null && temp.size() > 0)
				vColumns.addAll(temp);
		}
		
		return vColumns;
	}

	@Override
	public String primaryToSQL() throws ParseException {
		return "("+internConstraint.toSQL()+")";
	}
	
	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		return "("+internConstraint.toSQL(translator)+")";
	}

	@Override
	public String primaryToString() {
		return "("+internConstraint.toString()+")";
	}

	public String getADQLName() {
		return "CONSTRAINTS_GROUP";
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		if (internConstraint == null)
			return new ADQLGroup();
		else
			return new ADQLGroup((ADQLConstraint)internConstraint.getCopy());
	}

	@Override
	public Vector<ADQLObject> primaryGetAll(SearchHandler searchCondition) {
		if (internConstraint == null)
			return new Vector<ADQLObject>();
		else{
			Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
			if (searchCondition.match(internConstraint))
				vMatched.add(internConstraint);
			Vector<ADQLObject> temp = internConstraint.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
			return vMatched;
		}
	}

	@Override
	public ADQLObject primaryGetFirst(SearchHandler searchCondition) {
		if (internConstraint == null)
			return null;
		else if (searchCondition.match(internConstraint))
			return internConstraint;
		else
			return internConstraint.getFirst(searchCondition);
	}

	public ADQLObject primaryRemove(SearchHandler searchCondition) throws ParseException {
		if (internConstraint == null)
			return null;
		else if (searchCondition.match(internConstraint)){
			ADQLObject matchedObj = internConstraint;
			internConstraint = internConstraint.next();
			((ADQLConstraint)matchedObj).nextConstraint = null;
			return matchedObj;
		}else
			return internConstraint.remove(searchCondition);
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		if (internConstraint == null)
			return null;
		else if (searchCondition.match(internConstraint) && replacementObject instanceof ADQLConstraint){
			((ADQLConstraint)replacementObject).addConstraint(internConstraint.next());
			internConstraint.nextConstraint = null;
			ADQLObject matchedObj = internConstraint;
			internConstraint = (ADQLConstraint)replacementObject;
			return matchedObj;
		}else
			return internConstraint.replaceBy(searchCondition, replacementObject);
	}

}