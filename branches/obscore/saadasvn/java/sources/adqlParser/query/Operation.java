package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

/**
 * It represents a simple numeric operation (sum, difference, multiplication and division).
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLOperand
 */
public class Operation implements ADQLOperand {

	/**
	 * Part of the operation at the left of the operator.
	 */
	protected ADQLOperand leftOperand;
	
	/**
	 * Operation symbol: +, -, * ,/ ,...
	 * @see OperationType
	 */
	protected OperationType operation;

	/**
	 * Part of the operation at the right of the operator.
	 */
	protected Operation rightOperand;
	
	/**
	 * Label of this operand.
	 */
	protected String alias = null;
	
	/**
	 * Indicates whether the result of the operation must be "negativate" or not.
	 */
	protected boolean negative = false;
	
	
	/**
	 * Creates an "empty" operation: only one operand (so "not really an operation").
	 * 
	 * @param leftOp			The only operand of this operation.
	 * 
	 * @throws ParseException	If the left operand is <i>null</i>.
	 */
	public Operation(ADQLOperand leftOp) throws ParseException {
		this(leftOp, null, (Operation)null);
	}
	
	/**
	 * Creates a simple operation: an operation between two simple operands.
	 * 
	 * @param leftOp			Left operand.
	 * @param op				Operation symbol.
	 * @param rightOp			Right operand.
	 * 
	 * @throws ParseException If the left operand is <i>null</i>.
	 */
	public Operation(ADQLOperand leftOp, OperationType op, ADQLOperand rightOp) throws ParseException {
		if (leftOp == null)
			throw new ParseException("An operation must have at least one operand (the left one) !");
		
		leftOperand = leftOp;
		
		if (op == null || rightOp == null){
			operation = null;
			rightOperand = null;
		}else{
			operation = op;
			rightOperand = new Operation(rightOp);
		}
	}
	
	/**
	 * Creates an operation between an operand and another operation.
	 * 
	 * @param leftOp			Left operand.
	 * @param op				Operation symbol.
	 * @param rightOp			Right operand.
	 * 
	 * @throws ParseException 	If the left operand is <i>null</i>.
	 */
	public Operation(ADQLOperand leftOp, OperationType op, Operation rightOp) throws ParseException {
		if (leftOp == null)
			throw new ParseException("An operation must have at least one operand (the left one) !");
		
		leftOperand = leftOp;
		
		if (op == null || rightOp == null){
			operation = null;
			rightOperand = null;
		}else{
			operation = op;
			rightOperand = rightOp;
		}
	}

	/**
	 * Gets the left part of the operation.
	 * 
	 * @return The left operand.
	 */
	public ADQLOperand getLeftOperand() {
		return leftOperand;
	}
	
	/**
	 * Gets the operation symbol.
	 * 
	 * @return The operation type.
	 * @see OperationType
	 */
	public OperationType getOperation() {
		return operation;
	}

	/**
	 * Gets the right part of the operation.
	 * 
	 * @return The right operand.
	 */
	public Operation getRightOperand() {
		return rightOperand;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String label) {
		alias = label;
	}
	
	public void negativate(boolean neg) {
		negative = neg;
	}
	
	public Vector<ADQLColumn> getAllImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		
		Vector<ADQLColumn> temp = leftOperand.getAllImpliedColumns();
		if (temp != null && temp.size() > 0)
			vColumns.addAll(temp);
		
		if (rightOperand != null){
			temp = rightOperand.getAllImpliedColumns();
			if (temp != null && temp.size() > 0)
				vColumns.addAll(temp);
		}
		
		return vColumns;
	}

	public String toSQL() throws ParseException {
		String sql = leftOperand.toSQL()+" "+((operation!=null)?(operation.toSQL()+" "):"")+((rightOperand!=null)?rightOperand.toSQL():"")+((alias != null)?(" AS \""+alias+"\""):"");
		
		if (negative)
			return "-("+sql+")";
		else
			return sql;
	}

	public String toSQL(SQLTranslator translator) throws ParseException {
		String sql = leftOperand.toSQL(translator)+" "+((operation!=null)?(operation.toSQL()+" "):"")+((rightOperand!=null)?rightOperand.toSQL(translator):"")+((alias != null)?(" AS \""+alias+"\""):"");
		
		if (negative)
			return "-("+sql+")";
		else
			return sql;
	}
	
	public String toString(){
		String adql = leftOperand.toString()+" "+((operation!=null)?(operation.toString()+" "):"")+((rightOperand!=null)?rightOperand.toString():"")+((alias != null)?(" AS \""+alias+"\""):"");
		
		if (negative)
			return "-("+adql+")";
		else
			return adql;
	}

	public String getADQLName() {
		return "OPERATION "+operation;
	}

	public ADQLObject getCopy() throws ParseException {
		Operation copy = null;
		if (rightOperand == null)
			copy = new Operation((ADQLOperand)leftOperand.getCopy());
		else
			copy = new Operation((ADQLOperand)leftOperand.getCopy(), OperationType.valueOf(operation.toString()), (ADQLOperand)rightOperand.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		Vector<ADQLObject> temp = null;
		
		if (searchCondition.match(leftOperand))
			vMatched.add(leftOperand);
		temp = leftOperand.getAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		if (rightOperand != null){
			if (searchCondition.match(rightOperand))
				vMatched.add(rightOperand);
			temp = rightOperand.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand))
			matchedObj = leftOperand;
		else
			matchedObj = leftOperand.getFirst(searchCondition);
		
		if (matchedObj == null && rightOperand != null){
			if (searchCondition.match(rightOperand))
				matchedObj = rightOperand;
			else
				matchedObj = rightOperand.getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = leftOperand.remove(searchCondition);
		
		if (matchedObj == null && rightOperand != null){
			if (searchCondition.match(rightOperand)){
				matchedObj = rightOperand;
				rightOperand = null;
			}else
				matchedObj = rightOperand.remove(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand) && replacementObject instanceof ADQLOperand){
			matchedObj = leftOperand;
			leftOperand = (ADQLOperand)replacementObject;
		}else
			matchedObj = leftOperand.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null && rightOperand != null){
			if (searchCondition.match(rightOperand) && replacementObject instanceof Operation){
				matchedObj = rightOperand;
				rightOperand = (Operation)replacementObject;
			}else
				matchedObj = rightOperand.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}