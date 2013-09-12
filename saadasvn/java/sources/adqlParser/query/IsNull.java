package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;

/**
 * Represents a comparison between a column to the NULL value.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLComparison
 */
public class IsNull extends ADQLComparison {

	/**
	 * Creates a comparison between the given column and NULL.
	 * 
	 * @param column			The column whose the value must be compared to NULL.
	 * @throws ParseException	If <i>column</i> is <i>null</i>.
	 */
	public IsNull(ADQLColumn column) throws ParseException {
		this(column, false);
	}

	/**
	 * Creates a comparison between the column and NULL.
	 * 
	 * @param column			The column whose the value must be compared to NULL.
	 * @param isNot				<i>true</i> means IS NOT, <i>false</i> means IS.
	 * @throws ParseException	If <i>column</i> is <i>null</i>.
	 */
	public IsNull(ADQLColumn column, boolean isNot) throws ParseException {
		super(column, isNot?ComparisonOperator.ISNOT:ComparisonOperator.IS, new ADQLConstantValue());
		this.setNot(isNot);
	}

	/*
	 * setNot overloaded: IS NULL supports 2 NOT operator:
	 * NOT x IS NOT null <=> x IS NULL
	 * NOT x IS NULL <=> x IS NOT NULL
	 * x IS NOT null <=> x IS NOT NULL
	 */
	/* (non-Javadoc)
	 * @see adqlParser.query.ADQLConstraint#setNot(boolean)
	 */
	public void setNot(boolean isNot) {
		if( this.isNot ) {
			if( !isNot ) this.isNot = true;
			else this.isNot = false;
		} else {
			if( !isNot ) this.isNot = false;
			else this.isNot = true;
		}
	}
	@Override
	public String getADQLName() {
		return "IS"+(isNot?" NOT ":" ")+"NULL";
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		return new IsNull((ADQLColumn)leftOperand.getCopy(), isNot);
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
		
		return vMatched;
	}

	@Override
	public ADQLObject primaryGetFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand))
			matchedObj = leftOperand;
		if (matchedObj == null)
			matchedObj = leftOperand.getFirst(searchCondition);
		
		return matchedObj;
	}

	@Override
	public ADQLObject primaryRemove(SearchHandler searchCondition) throws ParseException {		
		return leftOperand.remove(searchCondition);
	}

	@Override
	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOperand) && replacementObject instanceof ADQLOperand){
			matchedObj = leftOperand;
			leftOperand = (ADQLOperand)replacementObject;
		}
		
		if (matchedObj == null)
			matchedObj = leftOperand.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}
