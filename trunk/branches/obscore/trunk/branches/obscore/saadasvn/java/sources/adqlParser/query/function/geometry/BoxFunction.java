package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the box function of the ADQL language.</p>
 * <p>It is specified by a center position and a size (in both coordinates).
 * The coordinates of the center position are expressed in the given coordinate system.
 * The size of the box is in degrees.</p>
 * <p>The specified coordinate system is checked thanks to the {@link GeometryFunction#checkCoordinateSystem(String)} function.</p>
 * <p><i><u>Example:</u><br />
 * BOX('ICRS GEOCENTER', 25.4, -20.0, 10, 10)<br />
 * In this example the function expressing a box o ten degrees centered in a position (25.4,-20.0) in degrees and defined according
 * to the ICRS coordinate system with GEOCENTER reference position.<br /><br />
 * BOX('ICRS GEOCENTER', t.ra, t.dec, 10, 10)<br />
 * In this second example the coordinates of the center position are extracted from a coordinate's column reference.
 * </i></p>
 * 
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class BoxFunction extends GeometryFunction {
	
	/** The coordinate system in which this box is described. */
	protected ADQLOperand coordSys;
	
	/** The first coordinate of the center of this box. */
	protected ADQLOperand coord1;
	
	/** The second coordinate of the center of this box. */
	protected ADQLOperand coord2;
	
	/** The width of this box (in degrees). */
	protected ADQLOperand width;
	
	/** The height of this box (in degrees). */
	protected ADQLOperand height;
	
	
	/**
	 * Creates a box function.<br />
	 * The given coordinate system is checked thanks to the {@link GeometryFunction#checkCoordinateSystem(String)} function.
	 * 
	 * @param coordinateSystem	The coordinate system of the center position.
	 * @param firstCoord		The first coordinate of the center of this box.
	 * @param secondCoord		The second coordinate of the center of this box.
	 * @param boxWidth			The width of this box (in degrees).
	 * @param boxHeight			The height of this box (in degrees).
	 * @throws ParseException	If the given coordinate system is incorrect or if at least one parameter is missing.
	 * 
	 * @see GeometryFunction#checkCoordinateSystem(String)
	 */
	public BoxFunction(ADQLOperand coordinateSystem, ADQLOperand firstCoord, ADQLOperand secondCoord, ADQLOperand boxWidth, ADQLOperand boxHeight) throws ParseException {
		if (coordinateSystem == null || ((coordinateSystem instanceof ADQLConstantValue) && !checkCoordinateSystem(coordinateSystem.toString())))
			throw new ParseException("[BOX] The given coordinate system is incorrect: \""+coordinateSystem+"\" !");
		else
			coordSys = coordinateSystem;
		
		if (firstCoord == null)
			throw new ParseException("The first given coordinate is null !");
		else
			coord1 = firstCoord;
		
		if (secondCoord == null)
			throw new ParseException("The second given coordinate !");
		else
			coord2 = secondCoord;

		if (boxWidth == null)
			throw new ParseException("The box width is null !");
		else
			width = boxWidth;
		
		if (boxHeight == null)
			throw new ParseException("The box height is null !");
		else
			height = boxHeight;
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
	 * Gets the width of this box (in degrees).
	 * 
	 * @return Its width.
	 */
	public ADQLOperand getWidth() {
		return width;
	}

	/**
	 * Gets the height of this box (in degrees).
	 * 
	 * @return Its height.
	 */
	public ADQLOperand getHeight() {
		return height;
	}

	@Override
	public String getName() {
		return "BOX";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{coordSys, coord1, coord2, width, height};
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		BoxFunction copy = new BoxFunction((ADQLOperand)coordSys.getCopy(), (ADQLOperand)coord1.getCopy(), (ADQLOperand)coord2.getCopy(), (ADQLOperand)width.getCopy(), (ADQLOperand)height.getCopy());
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
			if (searchCondition.match(width) && replacementObject instanceof ADQLOperand){
				matchedObj = width;
				width = (ADQLOperand)replacementObject;
			}else
				matchedObj = width.replaceBy(searchCondition, replacementObject);
		}
		
		if (matchedObj == null){
			if (searchCondition.match(height) && replacementObject instanceof ADQLOperand){
				matchedObj = height;
				height = (ADQLOperand)replacementObject;
			}else
				matchedObj = height.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
