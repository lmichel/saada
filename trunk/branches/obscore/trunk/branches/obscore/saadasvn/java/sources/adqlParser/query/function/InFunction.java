package adqlParser.query.function;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLQuery;
import adqlParser.query.SearchHandler;

/**
 * It represents the In function of SQL and ADQL.<br />
 * This functions returns <i>true</i> if the value of the given operand is
 * either in the given values list or in the results of the given sub-query, else it returns <i>false</i>.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public class InFunction extends ADQLFunction {

	/** The operand whose the value must be in the given list or in the results of the given sub-query. */
	protected ADQLOperand leftOp;
	
	/** The sub-query which must return a list of values. */
	protected ADQLQuery subQuery;
	
	/** The list of values. */
	protected ADQLOperand[] list;
	
	/** IN or NOT IN ? */
	protected boolean notIn = false;
	
	
	/**
	 * Creates an IN function with a sub-query.
	 * 
	 * @param op				The operand whose the value must be in the results of the given sub-query.
	 * @param query				A sub-query.
	 * @throws ParseException 	If the given operand and/or the given sub-query is <i>null</i>.
	 */
	public InFunction(ADQLOperand op, ADQLQuery query) throws ParseException {
		this(op, query, false);
	}
	
	/**
	 * Creates an IN function with a sub-query.
	 *  
	 * @param op				The operand whose the value must be in the results of the given sub-query.
	 * @param query				A sub-query.
	 * @param notIn				<i>true</i> for NOT IN, <i>false</i> for IN.
	 * @throws ParseException	If the given operand and/or the given sub-query is <i>null</i>.
	 */
	public InFunction(ADQLOperand op, ADQLQuery query, boolean notIn) throws ParseException {
		if (op == null || query == null)
			throw new ParseException("Impossible to create an IN function: the operand or the sub-query is null !");
			
		leftOp = op;
		subQuery = query;
		list = null;
		this.notIn = notIn;
	}
	
	/**
	 * Creates an IN function with a values list.
	 * 
	 * @param op				The operand whose the value must be in the given values list.
	 * @param valuesList		The values list.
	 * @throws ParseException	If the given operand is <i>null</i> and/or the given list is <i>null</i> or empty.
	 */
	public InFunction(ADQLOperand op, ADQLOperand[] valuesList) throws ParseException {
		this(op, valuesList, false);
	}
	
	/**
	 * Creates an IN function with a values list.
	 * 
	 * @param op				The operand whose the value must be in the given values list.
	 * @param valuesList		The values list.
	 * @param notIn				<i>true</i> for NOT IN, <i>false</i> for IN.
	 * @throws ParseException	If the given operand is <i>null</i> and/or the given list is <i>null</i> or empty.
	 */
	public InFunction(ADQLOperand op, ADQLOperand[] valuesList, boolean notIn) throws ParseException {
		if (op == null || valuesList == null || valuesList.length == 0)
			throw new ParseException("Impossible to create an IN function: the operand is null or the given list is empty.");
		
		leftOp = op;
		list = new ADQLOperand[valuesList.length];
		for(int i=0; i<valuesList.length; i++)
			list[i] = valuesList[i];
		subQuery = null;
		this.notIn = notIn;
	}

	@Override
	public void negativate(boolean neg) throws ParseException {
		throw new ParseException("Impossible to negativate an IN function !");
	}
	
	@Override
	public String getName() {
		return notIn?"NOT IN":"IN";
	}

	@Override
	public ADQLOperand[] getParameters() {
		if (subQuery != null)
			return new ADQLOperand[]{null};
		else{
			ADQLOperand[] params = new ADQLOperand[list.length];
			for(int i=0; i<list.length; i++)
				params[i] = list[i];
			return params;
		}
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		InFunction copy = null;
		
		if (subQuery != null)
			copy = new InFunction((ADQLOperand)leftOp.getCopy(), (ADQLQuery)subQuery.getCopy(), notIn);
		else{
			ADQLOperand[] copyOperands = new ADQLOperand[list.length];
			for(int i=0; i<copyOperands.length; i++)
				copyOperands[i] = (ADQLOperand)list[i].getCopy();
			copy = new InFunction((ADQLOperand)leftOp.getCopy(), copyOperands, notIn);
		}
		
		copy.setAlias(alias);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOp) && replacementObject instanceof ADQLOperand){
			matchedObj = leftOp;
			leftOp = (ADQLOperand)replacementObject;
		}else
			matchedObj = leftOp.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null){
			if (subQuery != null){
				if (searchCondition.match(subQuery) && replacementObject instanceof ADQLQuery){
					matchedObj = subQuery;
					subQuery = (ADQLQuery)replacementObject;
				}else
					matchedObj = subQuery.replaceBy(searchCondition, replacementObject);
			}else{
				for(int i=0; matchedObj == null && i<list.length; i++){
					if (searchCondition.match(list[i]) && replacementObject instanceof ADQLOperand){
						matchedObj = list[i];
						list[i] = (ADQLOperand)replacementObject;
					}else
						matchedObj = list[i].replaceBy(searchCondition, replacementObject);
				}
			}
		}
		
		return matchedObj;
	}

}