package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the COORDSYS function the ADQL language.</p>
 * <p>This function extracts the coordinate system string value from a given geometry.</p>
 * <p><i><u>Example:</u><br />
 * COORDSYS(POINT('ICRS GEOCENTER', 25.0, -19.5))<br />
 * In this example the function extracts the coordinate system of a point with position (25, -19.5) in degrees according to the ICRS coordinate
 * system with GEOCENTER reference position.
 * </i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class ExtractCoordSys extends GeometryFunction {
	
	/** The geometry from which the coordinate system string must be extracted. */
	protected ADQLOperand geomExpr;

	
	/**
	 * Creates a COORDSYS function.
	 * 
	 * @param param	The geometry from which the coordinate system string must be extracted.
	 */
	public ExtractCoordSys(ADQLOperand param){
		geomExpr = param;
	}
	
	/**
	 * Gets the geometry from which the coordinate system string must be extracted.
	 * 
	 * @return	The used geometry.
	 */
	public ADQLOperand getGeometry(){
		return geomExpr;
	}
	
	@Override
	public String getName() {
		return "COORDSYS";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{geomExpr};
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getExtractCoordSys(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		ExtractCoordSys copy = new ExtractCoordSys((ADQLOperand)geomExpr.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(geomExpr) && replacementObject instanceof ADQLOperand){
			matchedObj = geomExpr;
			geomExpr = (ADQLOperand)replacementObject;
		}else
			matchedObj = geomExpr.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}