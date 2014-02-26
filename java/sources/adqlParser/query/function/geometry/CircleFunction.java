package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the CIRCLE function of the ADQL language.</p>
 * <p>This function expresses a circular region on the sky (a cone in space) and corresponds semantically to the STC circle region.
 * The function arguments specify the coordinate system, the center position, and the radius (in degrees).</p>
 * <p>The specified coordinate system is checked thanks to the {@link GeometryFunction#checkCoordinateSystem(String)} function.</p>
 * <p><i><u>Example:</u><br />
 * CIRCLE('ICRS GEOCENTER', 25.4, -20.0, 1)<br />
 * In this example the function expresses a circle of one degree radius centered in a position of (25.4, -20.0) degrees and defined
 * according to the ICRS coordinate system with GEOCENTER reference position.
 * </i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class CircleFunction extends GeometryFunction {

	/** The coordinate system in which the given coordinates are expressed. */
	protected ADQLOperand coordSys;
	
	/** The first coordinate of the center position. */
	protected ADQLOperand coord1;
	
	/** The second coordinate of the center position. */
	protected ADQLOperand coord2;
	
	/** The radius of the circle (in degrees). */
	protected ADQLOperand radius;
	
	
	/**
	 * Creates a CIRCLE function.
	 * 
	 * @param coordinateSystem	The coordinate system in which the center position is expressed.
	 * @param firstCoord		The first coordinate of the center position.
	 * @param secondCoord		The second coordinate of the center position.
	 * @param radius			The radius of the circle (in degrees).
	 * @throws ParseException	If at least one parameter is incorrect or if the coordinate system is unknown.
	 */
	public CircleFunction(ADQLOperand coordinateSystem, ADQLOperand firstCoord, ADQLOperand secondCoord, ADQLOperand radius) throws ParseException{
		if (coordinateSystem == null || ((coordinateSystem instanceof ADQLConstantValue) && !checkCoordinateSystem(coordinateSystem.toString())))
			throw new ParseException("[CIRCLE] The given coordinate system is incorrect: \""+coordinateSystem+"\" !");
		else
			coordSys = coordinateSystem;
		
		if (firstCoord == null)
			throw new ParseException("The first given coordinates is null !");
		else
			coord1 = firstCoord;
		
		if (secondCoord == null)
			throw new ParseException("The second given coordinates is null !");
		else
			coord2 = secondCoord;

		if (radius == null)
			throw new ParseException("The circle radius is null !");
		else
			this.radius = radius;
	}

	/**
	 * Gets the used coordinate system.
	 * 
	 * @return Its coordinate system.
	 */
	public ADQLOperand getCoordSys() {
		return coordSys;
	}

	/**
	 * Gets the first coordinate of the center position.
	 * 
	 * @return The first coordinate of its center.
	 */
	public ADQLOperand getCoord1() {
		return coord1;
	}

	/**
	 * Gets the second coordinate of the center position.
	 * 
	 * @return The second coordinate of its center.
	 */
	public ADQLOperand getCoord2() {
		return coord2;
	}

	/**
	 * Gets the radius of this circle.
	 * 
	 * @return Its radius.
	 */
	public ADQLOperand getRadius() {
		return radius;
	}

	@Override
	public String getName() {
		return "CIRCLE";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{coordSys, coord1, coord2, radius};
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		CircleFunction copy = new CircleFunction((ADQLOperand)coordSys.getCopy(), (ADQLOperand)coord1.getCopy(), (ADQLOperand)coord2.getCopy(), (ADQLOperand)radius.getCopy());
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
		
		if (matchedObj == null){
			if (searchCondition.match(radius) && replacementObject instanceof ADQLOperand){
				matchedObj = radius;
				radius = (ADQLOperand)replacementObject;
			}else
				matchedObj = radius.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
