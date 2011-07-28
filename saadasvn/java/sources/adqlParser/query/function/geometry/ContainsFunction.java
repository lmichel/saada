package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the CONTAINS function of the ADQL language.</p>
 * <p>This numeric function determines if a geometry is wholly contained within another.
 * This is most commonly used to express the "point-in-shape" condition.</p>
 * <p><i><u>Example:</u><br />
 * CONTAINS(POINT('ICRS GEOCENTER', 25.0, -19.5), CIRCLE('ICRS GEOCENTER', 25.4, -20.0, 1)) = 1<br />
 * In this example the function determines if the point (25.0,-19.5) is within a circle of one degree radius centered in a position of (25.4,-20.0).
 * </i></p>
 * <p><b><u>Warning:</u>
 * <ul><li>The CONTAINS function returns 1 (true) if the first argument is in or on the boundary of the circle and 0 (false) otherwise.</li>
 * <li>Since the two argument geometries may be expressed in different coordinate systems, the function is responsible for converting one (or both).
 * If it can not do so, it SHOULD throw an error message, to be defined by the service making use of ADQL.</li></ul>
 * </b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class ContainsFunction extends GeometryFunction {

	/** The first geometry. */
	protected ADQLOperand leftParam;
	
	/** The second geometry. */
	protected ADQLOperand rightParam;
	
	
	/**
	 * Creates a CONTAINS function.
	 * 
	 * @param left				Its first geometry (the one which must be included the second).
	 * @param right				Its second geometry (the one which must include the first).
	 * @throws ParseException	If at least one parameter is incorrect.
	 */
	public ContainsFunction(ADQLOperand left, ADQLOperand right) throws ParseException {
		if (left == null || right == null)
			throw new ParseException("At least one parameter of the CONTAINS function is null: both must be non-null !");
		
		leftParam = left;
		rightParam = right;
	}

	/**
	 * Gets the first geometry (the one which must be included in the second).
	 * 
	 * @return Its first geometry.
	 */
	public ADQLOperand getFirstGeom() {
		return leftParam;
	}

	/**
	 * Gets the second geometry (the one which must included the first).
	 * 
	 * @return Its second geometry.
	 */
	public ADQLOperand getSecondGeom() {
		return rightParam;
	}

	@Override
	public String getName() {
		return "CONTAINS";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{leftParam, rightParam};
	}
	
	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getContains(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		ContainsFunction copy = new ContainsFunction((ADQLOperand)leftParam.getCopy(), (ADQLOperand)rightParam.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(leftParam) && replacementObject instanceof ADQLOperand){
			matchedObj = leftParam;
			leftParam = (ADQLOperand)replacementObject;
		}else
			matchedObj = leftParam.replaceBy(searchCondition, replacementObject);
			
		if (matchedObj == null){
			if (searchCondition.match(rightParam) && replacementObject instanceof ADQLOperand){
				matchedObj = rightParam;
				rightParam = (ADQLOperand)replacementObject;
			}else
				matchedObj = rightParam.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
}