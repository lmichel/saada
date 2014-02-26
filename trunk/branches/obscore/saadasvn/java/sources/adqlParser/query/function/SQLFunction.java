package adqlParser.query.function;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * It represents any SQL function (COUNT, MAX, MIN, AVG, SUM, etc...).
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public class SQLFunction extends ADQLFunction {

	/** Type of this SQL function. */
	protected final SQLFunctionType type;
	
	/** Distinct values of the parameter ? */
	protected boolean distinct = false;
	
	/** All values (*) ? */
	protected boolean all = false;
	
	/** The only parameter of this function (may be null). */
	protected ADQLOperand param = null;
		
	
	/**
	 * Creates a SQL function without a parameter.
	 * 
	 * @param t	Type of the function.
	 * 
	 * @see SQLFunction#SQLFunction(SQLFunctionType, ADQLOperand)
	 */
	public SQLFunction(SQLFunctionType t){
		this(t, null);
	}
		
	/**
	 * Creates a SQL function with one parameter.
	 * 
	 * @param t			Type of the function.
	 * @param operand	The only parameter of this function.
	 */
	public SQLFunction(SQLFunctionType t, ADQLOperand operand){
		type = t;
		param = operand;
	}
	
	/**
	 * Indicates whether values of the parameter must be distinct or not.
	 * 
	 * @return	<i>true</i> means distinct values, <i>false</i> else.
	 */
	public boolean isDistinct(){
		return distinct;
	}
	
	/**
	 * Tells if distinct values of the given parameter must be taken.
	 * 
	 * @param distinctValues	<i>true</i> means distinct values, <i>false</i> else.
	 */
	public void setDistinct(boolean distinctValues) {
		distinct = distinctValues;
	}
	
	/**
	 * Indicates whether all columns must be considered or just the given parameter.
	 * 
	 * @return	<i>true</i> means all values (*), <i>false</i> else.
	 */
	public boolean allValues(){
		return all;
	}
	
	/**
	 * Tells if all values must be considered.
	 * <i><u>Warning:</u> "all values" implies that the parameter will be deleted and so on for the distinct flag !</i>
	 * 
	 * @param allValues	<i>true</i> means all values, <i>false</i> else.
	 */
	public void setAllValues(boolean allValues) {
		all = allValues;
		param = null;
		distinct = false;
	}
	
	@Override
	public String getName() {
		return type.name();
	}

	@Override
	public ADQLOperand[] getParameters() {
		if (param != null)
			return new ADQLOperand[]{param};
		else
			return new ADQLOperand[0];
	}

	@Override
	public String primaryToSQL() throws ParseException {
		String sql = (negative?"-":"")+getName()+"(";
		
		if (distinct)
			sql += "DISTINCT ";
		
		if (all)
			sql += "*";
		else if (param != null)
			sql += param.toSQL();
		
		sql += ")";
		
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getSQLFunction(this);
		if (sql == null){
			sql = (negative?"-":"")+getName()+"(";
			
			if (distinct)
				sql += "DISTINCT ";
			
			if (all)
				sql += "*";
			else if (param != null)
				sql += param.toSQL(translator);
			
			sql += ")";
			
			return sql+((alias==null)?"":(" AS \""+alias+"\""));
		}else
			return sql;
	}

	@Override
	public String primaryToString() {
		String sql = (negative?"-":"")+getName()+"(";
		
		if (distinct)
			sql += "DISTINCT ";
		
		if (all)
			sql += "*";
		else if (param != null)
			sql += param.toString();
		
		sql += ")";
		
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		SQLFunction copy = new SQLFunction(SQLFunctionType.valueOf(type.toString()), (param==null)?null:((ADQLOperand)param.getCopy()));
		copy.setAlias(alias);
		copy.negativate(negative);
		copy.setAllValues(all);
		copy.setDistinct(distinct);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(param) && replacementObject instanceof ADQLOperand){
			matchedObj = param;
			param = (ADQLOperand)replacementObject;
		}else
			matchedObj = param.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}