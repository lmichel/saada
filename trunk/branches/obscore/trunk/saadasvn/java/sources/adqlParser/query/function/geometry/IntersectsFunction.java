package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the INTERSECTS function of the ADQL language.</p>
 * <p>This numeric function determines if two geometry values overlap.
 * This is most commonly used to express a "shape-vs-shape" intersection test.</p>
 * <p><i><u>Example:</u><br />
 * INTERSECTS(CIRCLE('ICRS GEOCENTER', 25.4, -20.0, 1), BOX('ICRS GEOCENTER', 20.0, -15.0, 10, 10)) = 1<br />
 * In this example the function determines whether the circle of one degree radius centered in a position (25.4, -20.0) degrees and defined
 * according to the ICRS coordinate system with GEOCENTER reference position overlaps with a box of ten degrees centered in a position
 *  (20.0, -15.0) in degrees and defined according to the same coordinate system.</i></p>
 * <p><b><u>Warning:</u>
 * <ul><li>The INTERSECTS function returns 1 (true) if the two arguments overlap and 0 (false) otherwise.</li>
 * <li>Since the two argument geometries may be expressed in different coordinate systems, the function is responsible for converting one (or both).
 * If it can not do so, it SHOULD throw an error message, to be defined by the service making use of ADQL.</li></ul>
 * </b></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class IntersectsFunction extends GeometryFunction {
	
	/** The first geometry. */
	protected ADQLOperand leftParam;
	
	/** The second geometry. */
	protected ADQLOperand rightParam;

	
	/**
	 * Creates an INTERSECTS function.
	 * 
	 * @param param1			The first geometry.
	 * @param param2			The second geometry.
	 * @throws ParseException	If there is an error with at least one of the parameters.
	 */
	public IntersectsFunction(ADQLOperand param1, ADQLOperand param2) throws ParseException {
		if (param1 == null || param2 == null)
			throw new ParseException("At least one parameter of the INTERSECTS function is null: both must be non-null !");
		
		leftParam = param1;
		rightParam = param2;
	}
	
	/**
	 * Gets the first geometry.
	 * 
	 * @return Its first geometry.
	 */
	public final ADQLOperand getFirstGeom() {
		return leftParam;
	}

	/**
	 * Gets the second geometry.
	 * 
	 * @return Its second geometry.
	 */
	public final ADQLOperand getRightParam() {
		return rightParam;
	}

	@Override
	public String getName() {
		return "INTERSECTS";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{leftParam, rightParam};
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getIntersects(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		IntersectsFunction copy = new IntersectsFunction((ADQLOperand)leftParam.getCopy(), (ADQLOperand)rightParam.getCopy());
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