package adqlParser.query.function;

import java.util.Vector;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * Represents any kind of function.<br />
 * A function can be used either as a constraint or as an operand.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLConstraint
 * @see ADQLOperand
 */
public abstract class ADQLFunction extends ADQLConstraint implements ADQLOperand {
	
	/** The label of this operand. */
	protected String alias = null;

	/** Indicates whether the result of this function must be "negativate" or not. */
	protected boolean negative = false;

	
	public String getAlias() {
		return alias;
	}

	public void setAlias(String label) {
		alias = label;
	}

	public void negativate(boolean neg) throws ParseException {
		negative = neg;
	}

	/**
	 * Gets the name of this function.
	 * 
	 * @return	Its name.
	 */
	public abstract String getName();
	
	/**
	 * Gets the list of all parameters of this function.
	 * 
	 * @return	Its parameters list.
	 */
	public abstract ADQLOperand[] getParameters();

	public Vector<ADQLColumn> primaryGetImpliedColumns(){
		Vector<ADQLColumn> vColumns = new Vector<ADQLColumn>();
		
		ADQLOperand[] operands = getParameters();
		Vector<ADQLColumn> temp = null;
		for(int i=0; i<operands.length; i++){
			temp = operands[i].getAllImpliedColumns();
			if (temp != null && temp.size() > 0)
				vColumns.addAll(temp);
		}
		
		return vColumns;
	}

	@Override
	public String primaryToSQL() throws ParseException {
		String sql = (negative?"-":"")+getName()+"(";
		
		ADQLOperand[] params = getParameters();
		String paramStr = null;
		for(int i=0; i<params.length; i++){
			if (paramStr == null)
				paramStr = params[i].toSQL();
			else
				paramStr += ", "+params[i].toSQL();
		}
		
		sql += paramStr+")";
		
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}
	
	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = (negative?"-":"")+getName()+"(";
		
		ADQLOperand[] params = getParameters();
		String paramStr = null;
		for(int i=0; i<params.length; i++){
			if (paramStr == null)
				paramStr = params[i].toSQL(translator);
			else
				paramStr += ", "+params[i].toSQL(translator);
		}
		
		sql += paramStr+")";
		
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	@Override
	public String primaryToString() {
		String sql = (negative?"-":"")+getName()+"(";
		
		ADQLOperand[] params = getParameters();
		String paramStr = null;
		for(int i=0; i<params.length; i++){
			if (paramStr == null)
				paramStr = params[i].toString();
			else
				paramStr += ", "+params[i].toString();
		}
		
		sql += paramStr+")";
		
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	public String getADQLName() {
		return "FCT_"+getName();
	}

	@Override
	public Vector<ADQLObject> primaryGetAll(SearchHandler searchCondition) {
		Vector<ADQLObject> vMatched = new Vector<ADQLObject>();
		Vector<ADQLObject> temp = null;
		
		ADQLOperand[] params = getParameters();
		for(int i=0; i<params.length; i++){
			if (searchCondition.match(params[i]))
				vMatched.add(params[i]);
			temp = params[i].getAll(searchCondition);
			if (temp != null && temp.size() > 0)
				vMatched.addAll(temp);
		}
		
		return vMatched;
	}

	@Override
	public ADQLObject primaryGetFirst(SearchHandler searchCondition) {
		ADQLObject matchedObj = null;
		
		ADQLOperand[] params = getParameters();
		for(int i=0; matchedObj == null && i<params.length; i++){
			if (searchCondition.match(params[i]))
				matchedObj = params[i];
			if (matchedObj == null)
				matchedObj = params[i].getFirst(searchCondition);
		}
		
		return matchedObj;
	}

	public ADQLObject primaryRemove(SearchHandler searchCondition) throws ParseException {
		ADQLObject matchedObj = null;
		
		ADQLOperand[] params = getParameters();
		for(int i=0; matchedObj == null && i<params.length; i++){
			if (matchedObj == null)
				matchedObj = params[i].remove(searchCondition);
		}
		
		return matchedObj;
	}

}