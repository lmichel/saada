package adqlParser.query.function;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * It represents any basic mathematical function (complete list in {@link MathFunctionType}).
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public class MathFunction extends ADQLFunction {

	/** Type of the mathematical function (which will also be its name). */
	protected final MathFunctionType type;
	
	/** First parameter of this function (may be null). */
	protected ADQLOperand param1;
	
	/** Second parameter of this function (may be null). */
	protected ADQLOperand param2;
		
	
	/**
	 * Creates a mathematical function without parameter.
	 * 
	 * @param t	The type of the function.
	 * 
	 * @see MathFunction#MathFunction(MathFunctionType, ADQLOperand, ADQLOperand)
	 */
	public MathFunction(MathFunctionType t){
		this(t, null, null);
	}
		
	/**
	 * Creates a mathematical function with only one parameter.
	 * 
	 * @param t			The type of the function.
	 * @param parameter	Its only parameter.
	 * 
	 * @see MathFunction#MathFunction(MathFunctionType, ADQLOperand, ADQLOperand)
	 */
	public MathFunction(MathFunctionType t, ADQLOperand parameter){
		this(t, parameter, null);
	}
		
	/**
	 * Creates a mathematical function with two parameters.
	 * @param t				The type of the function.
	 * @param parameter1	Its first parameter.
	 * @param parameter2	Its second parameter.
	 */
	public MathFunction(MathFunctionType t, ADQLOperand parameter1, ADQLOperand parameter2){
		type = t;
		param1 = parameter1;
		param2 = parameter2;
	}
	
	@Override
	public String getName() {
		return type.name();
	}

	@Override
	public ADQLOperand[] getParameters() {
		if (param1 != null)
			if (param2 != null)
				return new ADQLOperand[]{param1, param2};
			else
				return new ADQLOperand[]{param1};
		else if (param2 != null)
			return new ADQLOperand[]{param2};
		else
			return new ADQLOperand[0];
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		MathFunction copy = new MathFunction(MathFunctionType.valueOf(type.toString()), (ADQLOperand)param1.getCopy(), (ADQLOperand)param2.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(param1) && replacementObject instanceof ADQLOperand){
			matchedObj = param1;
			param1 = (ADQLOperand)replacementObject;
		}else
			matchedObj = param1.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null){
			if (searchCondition.match(param2) && replacementObject instanceof ADQLOperand){
				matchedObj = param2;
				param2 = (ADQLOperand)replacementObject;
			}else
				matchedObj = param2.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
