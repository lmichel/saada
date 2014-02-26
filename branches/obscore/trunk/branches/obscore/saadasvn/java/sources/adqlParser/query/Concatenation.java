package adqlParser.query;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;

public class Concatenation implements ADQLOperand {
	
	/**
	 * Left part of the concatenation.
	 */
	protected ADQLOperand leftOp;
	
	/**
	 * Right part of the concatenation.
	 */
	protected Concatenation next = null;
	
	/**
	 * Label of this operand.
	 */
	protected String alias = null;
	
	
	/**
	 * Creates an "empty" concatenation: only one operand (so "not really a concatenation").
	 * 
	 * @param op
	 * @throws ParseException	If the given operand is <i>null</i>.
	 * 
	 * @see Concatenation#Concatenation(ADQLOperand, Concatenation)
	 */
	public Concatenation(ADQLOperand op) throws ParseException {
		this(op, (Concatenation)null);
	}
	
	/**
	 * Creates a simple concatenation between two simple operands.
	 * 
	 * @param op	Left part of the concatenation.
	 * @param op2	Right part of the concatenation.
	 * @throws ParseException	If the given operand is <i>null</i>.
	 */
	public Concatenation(ADQLOperand op, ADQLOperand op2) throws ParseException {
		if (op == null)
			throw new ParseException("Impossible to build a concatenation without its left part: the left operand is null !");
		
		leftOp = op;
		next = (op2 == null)?null:new Concatenation(op2);
	}
	
	/**
	 * Creates a concatenation.
	 * 
	 * @param op	Left part of the concatenation.
	 * @param op2	Right part of the concatenation.
	 * @throws ParseException	If the given operand is <i>null</i>.
	 */
	public Concatenation(ADQLOperand op, Concatenation op2) throws ParseException {
		if (op == null)
			throw new ParseException("Impossible to build a concatenation without its left part: the left operand is null !");
		
		leftOp = op;
		next = op2;
	}
	
	/**
	 * Gets the left part of the concatenation.
	 * 
	 * @return	Left operand.
	 */
	public ADQLOperand getLeftOperand(){
		return leftOp;
	}
	
	/**
	 * Gets the right part of the concatenation.
	 * 
	 * @return	Right operand.
	 */
	public Concatenation next(){
		return next;
	}
	
	/**
	 * Allows concatenating this operand with the given operand.
	 * 
	 * @param op				The operand to add at the end of this concatenation.
	 * @throws ParseException	If the given operand is <i>null</i>.
	 * 
	 * @see Concatenation#concat(ADQLOperand)
	 */
	public void concat(ADQLOperand op) throws ParseException {
		if (next == null)
			next = new Concatenation(op);
		else
			next.concat(op);
	}
	
	/**
	 * Allows concatenating this operand with the given operand.
	 * 
	 * @param op The concatenation to add at the end of this concatenation.
	 */
	public void concat(Concatenation op){
		if (next == null)
			next = op;
		else
			next.concat(op);
	}
	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String label) {
		alias = label;
	}
	
	public void negativate(boolean neg) throws ParseException {
		throw new ParseException("Impossible to negativate a string value (\""+toString()+"\") !");
	}
	
	public Vector<ADQLColumn> getAllImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();

		Vector<ADQLColumn> temp = leftOp.getAllImpliedColumns();
		if (temp != null && temp.size() > 0)
			vColumns.addAll(temp);
		
		if (next != null){
			temp = next.getAllImpliedColumns();
			if (temp != null && temp.size() > 0)
				vColumns.addAll(temp);
		}
		
		return vColumns;
	}

	public String toSQL() throws ParseException {
		return leftOp.toSQL()+((next == null)?"":(" || "+next.toSQL()));
	}

	public String toSQL(SQLTranslator translator) throws ParseException {
		return leftOp.toSQL(translator)+((next == null)?"":(" || "+next.toSQL(translator)));
	}
	
	public String toString(){
		return leftOp.toString()+((next == null)?"":(" || "+next.toString()));
	}

	public String getADQLName() {
		return "CONCATENATION";
	}

	public ADQLObject getCopy() throws ParseException {
		Concatenation copy = new Concatenation((ADQLOperand)leftOp.getCopy());
		copy.concat((Concatenation)next.getCopy());
		copy.setAlias(alias);
		return copy;
	}

	public Vector<ADQLObject> getAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		Vector<ADQLObject> temp = null;
		
		if (searchCondition.match(leftOp))
			vMatched.add(leftOp);
		temp = leftOp.getAll(searchCondition);
		if (temp != null && temp.size() > 0)
			vMatched.addAll(temp);
		
		if (next != null){
			if (searchCondition.match(next))
				vMatched.add(next);
			temp = next.getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	public ADQLObject getFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftOp))
			matchedObj = leftOp;
		else
			matchedObj = leftOp.getFirst(searchCondition);
		
		if (matchedObj == null && next != null){
			if (searchCondition.match(next))
				matchedObj = next;
			else
				matchedObj = next.getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject remove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (matchedObj == null)
			matchedObj = leftOp.remove(searchCondition);
		
		if (matchedObj == null && next != null){
			if (searchCondition.match(next)){
				matchedObj = next;
				next = next.next();
				((Concatenation)matchedObj).next = null;
			}else
				matchedObj = next.remove(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject replaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;

		if (searchCondition.match(leftOp) && replacementObject instanceof ADQLOperand){
			matchedObj = leftOp;
			leftOp = (ADQLOperand)replacementObject;
		}else
			matchedObj = leftOp.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null && next != null){
			if (searchCondition.match(next) && replacementObject instanceof Concatenation){
				((Concatenation)replacementObject).concat(next.next());
				next.next = null;
				matchedObj = next;
				next = (Concatenation)replacementObject;
			}else
				matchedObj = next.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}