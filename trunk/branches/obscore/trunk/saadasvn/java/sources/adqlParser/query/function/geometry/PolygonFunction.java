package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the POLYGON function of the ADQL language.</p>
 * <p> This function expresses a region on the sky with sides denoted by great circles passing through specified coordinates.
 * It corresponds semantically to the STC Polygon region. The arguments specify the coordinate system and three or more sets of
 * 2-D coordinates.</p>
 * <p>The polygon is a list of vertices in a single coordinate system, with each vertex connected to the next along a great circle
 * and the last vertex implicitly connected to the first vertex.</p>
 * <p><i><u>Example:</u><br />
 * POLYGON('ICRS GEOCENTER', 10.0, -10.5, 20.0, 20.5, 30.0, 30.5)<br />
 * In this example the function expresses a triangle, whose vertices are (10.0, -10.5), (20.0, 20.5) and (30.0, 30.5) in degrees
 * according to the STC coordinate system with GEOCENTER reference position.</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class PolygonFunction extends GeometryFunction {

	/** The coordinate system in which coordinates of the vertices are expressed. */
	protected ADQLOperand coordSys;
	
	/** The coordinates of vertices. */
	protected ADQLOperand[] coordinates;
	
	
	/**
	 * Creates a POLYGON function.
	 * 
	 * @param coordSystem		The coordinate system to use.
	 * @param coord1			The first coordinate of the first vertex.
	 * @param coord2			The second coordinate of the first vertex.
	 * @param coord3			The first coordinate of the second vertex.
	 * @param coord4			The second coordinate of the second vertex.
	 * @throws ParseException	If one the parameters is incorrect or if the coordinate system is unknown.
	 */
	public PolygonFunction(ADQLOperand coordSystem, ADQLOperand coord1, ADQLOperand coord2, ADQLOperand coord3, ADQLOperand coord4) throws ParseException {
		if (coordSystem == null || ((coordSystem instanceof ADQLConstantValue) && !checkCoordinateSystem(coordSystem.toString())))
			throw new ParseException("[POLYGON] The given coordinate system is unknown: \""+coordSystem+"\" !");
			
		if (coord1 == null || coord2 == null || coord3 == null || coord4 == null)
			throw new ParseException("The ADQL function must have either 5 (coord system + 4 coordinates) or 7 (coord system + 6 coordinates) parameters");
		
		coordSys = coordSystem;
		coordinates = new ADQLOperand[]{coord1, coord2, coord3, coord4};
	}
	
	/**
	 * Creates a POLYGON function.
	 * 
	 * @param coordSystem		The coordinate system to use.
	 * @param coord1			The first coordinate of the first vertex.
	 * @param coord2			The second coordinate of the first vertex.
	 * @param coord3			The first coordinate of the second vertex.
	 * @param coord4			The second coordinate of the second vertex.
	 * @param coord5			The first coordinate of the third vertex.
	 * @param coord6			The second coordinate of the third vertex.
	 * @throws ParseException	If one the parameters is incorrect or if the coordinate system is unknown.
	 */
	public PolygonFunction(ADQLOperand coordSystem, ADQLOperand coord1, ADQLOperand coord2, ADQLOperand coord3, ADQLOperand coord4, ADQLOperand coord5, ADQLOperand coord6) throws ParseException {
		if (coordSystem == null || ((coordSystem instanceof ADQLConstantValue) && !checkCoordinateSystem(coordSystem.toString())))
			throw new ParseException("[POLYGON] The given coordinate system is unknown: \""+coordSystem+"\" !");
			
		if (coord1 == null || coord2 == null || coord3 == null || coord4 == null || coord5 == null || coord6 == null)
			throw new ParseException("The ADQL function must have either 5 (coord system + 4 coordinates) or 7 (coord system + 6 coordinates) parameters");
		
		coordSys = coordSystem;
		coordinates = new ADQLOperand[]{coord1, coord2, coord3, coord4, coord5, coord6};
	}
	
	protected PolygonFunction(ADQLOperand coordSystem, ADQLOperand[] coords){
		coordSys = coordSystem;
		coordinates = coords;
	}
	
	@Override
	public String getName() {
		return "POLYGON";
	}

	@Override
	public ADQLOperand[] getParameters() {
		ADQLOperand[] params = new ADQLOperand[coordinates.length+1];
		
		params[0] = coordSys;
		for(int i=0; i<coordinates.length; i++)
			params[i+1] = coordinates[i];
		
		return params;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		ADQLOperand[] coordCopy = new ADQLOperand[coordinates.length];
		for(int i=0; i<coordCopy.length; i++)
			coordCopy[i] = (ADQLOperand)coordinates[i].getCopy();
		
		PolygonFunction copy = new PolygonFunction((ADQLOperand)coordSys.getCopy(), coordCopy);
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
		
		for(int i=0; matchedObj == null && i<coordinates.length; i++){
			if (searchCondition.match(coordinates[i]) && replacementObject instanceof ADQLOperand){
				matchedObj = coordinates[i];
				coordinates[i] = (ADQLOperand)replacementObject;
			}else
				matchedObj = coordinates[i].replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}
