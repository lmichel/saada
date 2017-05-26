package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the CENTROID function of the ADQL language.</p>
 * <p>This function computes the centroid of a given geometry and returns a POINT.</p>
 * <p><i><u>Example:</u><br />
 * CENTROID(CIRCLE('ICRS GEOCENTER', 25.4, -20.0, 1))<br />
 * In this example the function computes the centroid of a circle of one degree radius centered
 * in a position of (25.4,-20.0) degrees and defined according to the ICRS coordinate system with GEOCENTER reference position.
 * </i></p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class CentroidFunction extends GeometryFunction {

	/** The geometry whose the centroid must be extracted. */
	protected ADQLOperand parameter;
	
	
	/**
	 * Creates a CENTROID function.
	 * 
	 * @param param				The geometry whose the centroid must be extracted.
	 * @throws ParseException	If the given parameter is incorrect.
	 */
	public CentroidFunction(ADQLOperand param) throws ParseException {
		if (param == null)
			throw new ParseException("The ADQL function CENTROID must have exactly one parameter !");
		
		parameter = param;
	}
	
	@Override
	public String getName() {
		return "CENTROID";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{parameter};
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getCentroid(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		CentroidFunction copy = new CentroidFunction((ADQLOperand)parameter.getCopy());
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