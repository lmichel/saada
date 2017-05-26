package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * Represents a comparison (numeric or not) between two operands.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLOperand
 * @see ADQLConstraint
 */
public class ADQLComparison extends ADQLConstraint {

	/** The left part of the comparison. */
	protected ADQLOperand leftOperand;
	
	/** The comparison symbol. */
	protected ComparisonOperator compOperator;
	
	/** The right part of the comparison. */
	protected ADQLOperand rightOperand;
	
	
	/**
	 * Creates a comparison between two operands.
	 * 
	 * @param left	The left part.
	 * @param comp	The comparison symbol.
	 * @param right	The right part.
	 */
	public ADQLComparison(ADQLOperand left, ComparisonOperator comp, ADQLOperand right) throws ParseException {
		if (left == null || comp == null || right == null)
			throw new ParseException("Impossible to build the comparison: "+left+" "+comp+" "+right+". All parts of a comparison must be non-null !");
			
		leftOperand = left;
		compOperator = comp;
		rightOperand = right;
	}
	
	/**
	 * Gets the left part of the comparison.
	 * 
	 * @return The left operand.
	 */
	public ADQLOperand getLeftOperand() {
		return leftOperand;
	}

	/**
	 * Gets the comparison symbol.
	 * 
	 * @return The comparison operator.
	 */
	public ComparisonOperator getOperator() {
		return compOperator;
	}

	/**
	 * Gets the right part of the comparison.
	 * 
	 * @return The right operand.
	 */
	public ADQLOperand getRightOperand() {
		return rightOperand;
	}
	
	@Override
	public Vector<ADQLColumn> primaryGetImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		
		Vector<ADQLColumn> temp = leftOperand.getAllImpliedColumns();
		if (temp != null && temp.size() > 0)
			vColumns.addAll(temp);
		
		 temp = rightOperand.getAllImpliedColumns();
		 if (temp != null && temp.size() > 0)
			vColumns.addAll(temp);
		
		return vColumns;
	}

	@Override
	public String primaryToSQL() throws ParseException {
		return leftOperand.toSQL()+" "+compOperator.toSQL()+" "+rightOperand.toSQL();
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		return leftOperand.toSQL(translator)+" "+compOperator.toSQL()+" "+rightOperand.toSQL(translator);
	}

	@Override
	public String primaryToString() {
		return leftOperand.toString()+" "+compOperator.toString()+" "+rightOperand.toString();
	}

	public String getADQLName() {
		return "COMPARISON";
	}

	public ADQLConstraint primaryGetCopy() throws ParseException {
		return new ADQLComparison((ADQLOperand)leftOperand.getCopy(), ComparisonOperator.getOperator(compOperator.toString()), (ADQLOperand)rightOperand.getCopy());
	}

	@Override
	public Vector<ADQLObject> primaryGetAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		Vector<ADQLObject> temp = null;
		
		if (searchCondition.match(leftOperand))
			vMatched.add(leftOperand);
		temp = leftOperand.getAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		if (searchCondition.match(rightOperand))
			vMatched.add(rightOperand);
		temp = rightOperand.getAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		return vMatched;
	}

	@Override
	public ADQLObject primaryGetFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand))
			matchedObj = leftOperand;
		if (matchedObj == null)
			matchedObj = leftOperand.getFirst(searchCondition);
		
		if (matchedObj == null && searchCondition.match(rightOperand))
			matchedObj = rightOperand;
		if (matchedObj == null)
			matchedObj = rightOperand.getFirst(searchCondition);
		
		return matchedObj;
	}

	public ADQLObject primaryRemove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = null;
		
		matchedObj = leftOperand.remove(searchCondition);
		
		if (matchedObj == null)
			matchedObj = rightOperand.remove(searchCondition);
		
		return matchedObj;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand) && replacementObject instanceof ADQLOperand){
			matchedObj = leftOperand;
			leftOperand = (ADQLOperand)replacementObject;
		}
		
		if (matchedObj == null)
			matchedObj = leftOperand.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null && searchCondition.match(rightOperand) && replacementObject instanceof ADQLOperand){
			matchedObj = rightOperand;
			rightOperand = (ADQLOperand)replacementObject;
		}
		if (matchedObj == null)
			matchedObj = rightOperand.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}