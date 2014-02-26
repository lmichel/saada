package adqlParser.query.function;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * It represents unknown functions: they are thus considered as user functions.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public class UserFunction extends ADQLFunction {

	/** Its name. */
	protected final String functionName;
	
	/** Its parameters. */
	protected final ADQLOperand[] parameters;
	
	
	/**
	 * Creates a user function without parameters.
	 * 
	 * @param name				Name of the function.
	 * @throws ParseException	If the name is <i>null</i> or is an empty string.
	 * 
	 * @see UserFunction#UserFunction(String, ADQLOperand[])
	 */
	public UserFunction(String name) throws ParseException {
		this(name, null);
	}
	
	/**
	 * Creates a user function without parameters.
	 * 
	 * @param name				Name of the function.
	 * @param params			Parameters of the function.
	 * @throws ParseException	If the name is <i>null</i> or is an empty string.
	 * 
	 * @see UserFunction#UserFunction(String, ADQLOperand[])
	 */
	public UserFunction(String name, ADQLOperand[] params) throws ParseException {
		if (name == null || name.trim().length() == 0)
			throw new ParseException("The name of an user function can not be null !");
		
		functionName = name;
		parameters = (params==null)?(new ADQLOperand[0]):params;
	}
	
	@Override
	public String getName() {
		return functionName;
	}

	@Override
	public ADQLOperand[] getParameters() {
		ADQLOperand[] params = new ADQLOperand[parameters.length];
		for(int i=0; i<params.length; i++)
			params[i] = parameters[i];
		return params;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		ADQLOperand[] copyParams = new ADQLOperand[parameters.length];
		for(int i=0; i<copyParams.length; i++)
			copyParams[i] = (ADQLOperand)parameters[i].getCopy();
		
		UserFunction copy = new UserFunction(functionName, copyParams);
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		for(int i=0; matchedObj == null && i<parameters.length; i++){
			if (searchCondition.match(parameters[i]) && replacementObject instanceof ADQLOperand){
				matchedObj = parameters[i];
				parameters[i] = (ADQLOperand)replacementObject;
			}else
				matchedObj = parameters[i].replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
