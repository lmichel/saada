package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.Concatenation;
import adqlParser.query.SearchHandler;
import adqlParser.query.function.UserFunction;

/**
 * <p>It represents the REGION function the ADQL language.</p>
 * <p>This function provides a generic way of expressing a region represented by a single string input parameter.
 * The format of the string MUST be specified by a service that accepts ADQL by referring to a standard format.
 * Currently STC/s is the only standardized string representation a service can declare.</p>
 * <p><i><u>Example:</u><br />
 * REGION('Convex ... Position ... Error ... Size')<br />
 * In this example the function embeds a string serialization of an STC region within parenthesis.</i></p>
 * <p><b><u>Warning:</u><br />
 * Inappropriate geometries for this construct SHOULD throw an error message, to be defined by the service making use of ADQL.</b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class RegionFunction extends GeometryFunction {

	/** The only parameter of this function. */
	protected ADQLOperand parameter;
	
	
	/**
	 * Creates a REGION function.
	 * 
	 * @param param				The parameter (a string or a column reference or a concatenation or a user function).
	 * @throws ParseException	If there is a problem with at least one of the given parameters.
	 */
	public RegionFunction(ADQLOperand param) throws ParseException {
		if (param == null)
			throw new ParseException("The ADQL function REGION must have exactly one parameter !");
		else if (!(param instanceof ADQLConstantValue) && !(param instanceof ADQLColumn) && !(param instanceof Concatenation) && !(param instanceof UserFunction))
			throw new ParseException("The only kinds of parameter the ADQL function REGION can accept are string, concatenation, column reference and user function ! But the given parameter is the type of \""+param.getClass().getName()+"\"");
		
		parameter = param;
	}
	
	@Override
	public String getName() {
		return "REGION";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{parameter};
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		RegionFunction copy = new RegionFunction((ADQLOperand)parameter.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(parameter) && replacementObject instanceof ADQLOperand){
			matchedObj = parameter;
			parameter = (ADQLOperand)replacementObject;
		}else
			matchedObj = parameter.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}
