package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the POINT function of the ADQL language.</p>
 * <p>This function expresses a single location on the sky, and corresponds semantically to an STC SpatialCoord.
 * The arguments specify the coordinate system and the position.</p>
 * <p><i><u>Example:</u><br />
 * POINT('ICRS GEOCENTER', 25.0, -19.5)<br />
 * In this example the function expresses a point with right ascension of 25 degrees and declination of  -19.5 degrees according
 * to the ICRS coordinate system with GEOCENTER reference position.</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class PointFunction extends GeometryFunction {

	/** The coordinate system used to express the coordinates. */
	protected ADQLOperand coordSys;
	
	/** The first coordinate for this position. */
	protected ADQLOperand coord1;
	
	/** The second coordinate for this position. */
	protected ADQLOperand coord2;

	
	/**
	 * Creates a POINT function.
	 * 
	 * @param coordinateSystem	The coordinate system to use.
	 * @param firstCoord		The first coordinate.
	 * @param secondCoord		The second coordinate.
	 * @throws ParseException	If at least one of the given parameters is incorrect.
	 */
	public PointFunction(ADQLOperand coordinateSystem, ADQLOperand firstCoord, ADQLOperand secondCoord) throws ParseException {
		if (coordinateSystem == null || ((coordinateSystem instanceof ADQLConstantValue) && !checkCoordinateSystem(coordinateSystem.toString())))
			throw new ParseException("[POINT] The given coordinate system is incorrect: \""+coordinateSystem+"\" !");
		coordSys = coordinateSystem;
		
		if (firstCoord == null)
			throw new ParseException("The POINT function must have non-null coordinates !");
		coord1 = firstCoord;
		
		if (secondCoord == null)
			throw new ParseException("The POINT function must have non-null coordinates !");
		coord2 = secondCoord;
	}

	/**
	 * Gets the used coordinate system.
	 * 
	 * @return Its coordinate system.
	 */
	public final ADQLOperand getCoordSys() {
		return coordSys;
	}

	/**
	 * Gets the first coordinate of this point.
	 * 
	 * @return Its first coordinate.
	 */
	public final ADQLOperand getCoord1() {
		return coord1;
	}

	/**
	 * Gets the second coordinate of this point. 
	 * 
	 * @return Its second coordinate.
	 */
	public final ADQLOperand getCoord2() {
		return coord2;
	}

	@Override
	public String getName() {
		return "POINT";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{coordSys, coord1, coord2};
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		PointFunction copy = new PointFunction((ADQLOperand)coordSys.getCopy(), (ADQLOperand)coord1.getCopy(), (ADQLOperand)coord2.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(coordSys) && replacementObject instanceof ADQLOperand){
			matchedObj = coordSys;
			coordSys = (ADQLOperand)replacementObject;
		}else
			matchedObj = coordSys.replaceBy(searchCondition, replacementObject);
		
		if (matchedObj == null){
			if (searchCondition.match(coord1) && replacementObject instanceof ADQLOperand){
				matchedObj = coord1;
				coord1 = (ADQLOperand)replacementObject;
			}else
				matchedObj = coord1.replaceBy(searchCondition, replacementObject);
		}

		if (matchedObj == null){
			if (searchCondition.match(coord2) && replacementObject instanceof ADQLOperand){
				matchedObj = coord2;
				coord2 = (ADQLOperand)replacementObject;
			}else
				matchedObj = coord2.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
