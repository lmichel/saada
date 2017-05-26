package adqlParser.query.function;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLQuery;
import adqlParser.query.SearchHandler;

/**
 * Represents the function EXISTS of SQL and ADQL.<br />
 * This function returns <i>true</i> if the sub-query given in parameter returns at least one result, else it returns <i>false</i>.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public class Exists extends ADQLFunction {
	
	/** The sub-query. */
	protected ADQLQuery subQuery;

	
	/**
	 * Creates an Exists function instance.
	 * 
	 * @param query	Its sub-query.
	 */
	public Exists(ADQLQuery query){
		subQuery = query;
	}
	
	@Override
	public String getName() {
		return "EXISTS";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{null};
	}

	@Override
	public void negativate(boolean neg) throws ParseException {
		throw new ParseException("Impossible to negativate the result of the function \""+getName()+"\" !");
	}

	@Override
	public String primaryToSQL() throws ParseException {
		return getName()+" ("+subQuery.toSQL(false)+")"+((alias!=null)?(" AS \""+alias+"\""):"");
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getExists(this);
		if (sql == null)
			return getName()+" ("+subQuery.toSQL(false, translator)+")"+((alias!=null)?(" AS \""+alias+"\""):"");
		else
			return sql;
	}

	@Override
	public String primaryToString() {
		return getName()+" ("+subQuery.toString()+")"+((alias!=null)?(" AS \""+alias+"\""):"");
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		Exists copy = new Exists((ADQLQuery)subQuery.getCopy());
		copy.setAlias(alias);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(subQuery) && replacementObject instanceof ADQLQuery){
			matchedObj = subQuery;
			subQuery = (ADQLQuery)replacementObject;
		}else
			matchedObj = subQuery.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}