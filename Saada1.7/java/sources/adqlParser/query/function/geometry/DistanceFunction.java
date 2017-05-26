package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the DISTANCE function of the ADQL language.</p>
 * <p>This function computes the arc length along a great circle between two points, and returns a numeric value expression in degrees.</p>
 * <p><i><u>Example:</u><br />
 * DISTANCE(POINT('ICRS GEOCENTER', 25.0, -19.5), POINT('ICRS GEOCENTER', 25.4, -20.0))<br />
 * In this example the function computes the distance between two points of coordinates (25, -19.5) and (25.4, -20) both expressed according to the ICRS
 * coordinate system with GEOCENTER reference position.</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class DistanceFunction extends GeometryFunction {

	/** The first point. */
	protected PointFunction p1;
	
	/** The second point. */
	protected PointFunction p2;
	
	
	/**
	 * Creates a DISTANCE function.
	 * 
	 * @param point1			The first point.
	 * @param point2			The second point.
	 * @throws ParseException	If one of the parameters are incorrect.
	 */
	public DistanceFunction(PointFunction point1, PointFunction point2) throws ParseException {
		if (point1 == null || point2 == null)
			throw new ParseException("At least one parameter of the DISTANCE function is null: both must be non-null !");
		
		p1 = point1;
		p2 = point2;
	}

	/**
	 * Gets the first point.
	 * 
	 * @return Its first point.
	 */
	public final PointFunction getP1() {
		return p1;
	}

	/**
	 * Gets the second point.
	 * 
	 * @return Its second point.
	 */
	public final PointFunction getP2() {
		return p2;
	}

	@Override
	public String getName() {
		return "DISTANCE";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{p1, p2};
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getDistance(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		DistanceFunction copy = new DistanceFunction((PointFunction)p1.getCopy(), (PointFunction)p2.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(p1) && replacementObject instanceof PointFunction){
			matchedObj = p1;
			p1 = (PointFunction)replacementObject;
		}else
			matchedObj = p1.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null){
			if (searchCondition.match(p2) && replacementObject instanceof PointFunction){
				matchedObj = p2;
				p2 = (PointFunction)replacementObject;
			}else
				matchedObj = p2.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}
	
}