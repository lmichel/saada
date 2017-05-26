package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.function.ADQLFunction;

/**
 * <p>It represents any geometric function of ADQL.</p>
 * <p>By default the SQL translation of a geometry is a string literal corresponding to the ADQL expression.<br />
 * <i><u>Example:</u><br />
 * POINT('ICRS GEOCENTER', 12, 34)<br />
 * donne en SQL:<br />
 * 'POINT(''ICRS GEOCENTER'', 12, 34)'</i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see ADQLFunction
 */
public abstract class GeometryFunction extends ADQLFunction {
	
	@Override
	public String primaryToSQL() throws ParseException {
		return "'"+super.primaryToSQL().replaceAll("'", "''")+"'";
	}
	
	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		return "'"+super.primaryToSQL(translator).replaceAll("'", "''")+"'";
	}

	/**
	 * Checks whether the given coordinates system exists or not.
	 * 
	 * @param coordinateSystem	The coordinate system whose the existence must be checked.
	 * @return					<i>true</i> if the given coordinate system exists, <i>false</i> else.
	 * @throws ParseException	If there is some description of the non-existence of the given coordinate system.
	 */
	public static boolean checkCoordinateSystem(String coordinateSystem) throws ParseException {
		
//		// Gets the coordinate system frame:
//		if (coordinateSystem != null && coordinateSystem.trim().length() > 0){
//			int pos = coordinateSystem.indexOf(' ');
//			if (pos > -1){
//				try {
//					return Coord.getAstroframe(coordinateSystem.substring(0, pos).replaceAll("'", ""), null) != null;
//				} catch (FatalException e) {
//					throw new ParseException(e.getMessage()+" - "+e.getContext());
//				}
//			}else
//				throw new ParseException("A valid coordinate system must be specified !");
//		}else
//			throw new ParseException("A valid coordinate system must be specified !");
		
		return true;
	}
	
	/**
	 * Converts the given value from the first coordinate system to the second one.
	 * 
	 * @param srcCoordSys		The coordinate system of the given value.
	 * @param value				The value to convert.
	 * @param destCoordSys		The coordinate system in which the result of this function must be.
	 * @return					The converted value.
	 * @throws ParseException	If there is an error.
	 */
	public static double convert(String srcCoordSys, double value, String destCoordSys) throws ParseException {
		return value;
	}
	
}