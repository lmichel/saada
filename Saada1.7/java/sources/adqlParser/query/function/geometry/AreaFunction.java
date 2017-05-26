package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the AREA function of ADQL.</p>
 * <p>This function computes the area, in square degrees, of a given geometry.</p>
 * <i><u>Example:</u><br/>AREA(CIRCLE('ICRS GEOCENTER', 25.4, -20.0, 1)).</i>
 * <p>Inappropriate geometries for this construct (e.g. POINT) SHOULD either return zero or throw an error message. <b>This choice must be done in an extended class of {@link AreaFunction}</b>.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 */
public class AreaFunction extends GeometryFunction {

	/** The only parameter of this function. */
	protected GeometryFunction parameter;
	
	
	/**
	 * Creates an AREA function with its parameter.
	 * 
	 * @param param				Parameter of AREA.
	 * @throws ParseException	If the given operand is <i>null</i> or if it's not a {@link GeometryFunction}. 
	 */
	public AreaFunction(ADQLOperand param) throws ParseException {
		if (param == null)
			throw new ParseException("The ADQL function AREA must have one non-null parameter !");
		if (!(param instanceof GeometryFunction))
			throw new ParseException("The ADQL function AREA must have a geometric parameter (GeometryFunction) !");
		
		parameter = (GeometryFunction)param;
	}
	
	/**
	 * Creates an AREA function with its parameter.
	 * 
	 * @param param				Parameter of AREA.
	 * @throws ParseException	If the given operand is <i>null</i>. 
	 */
	public AreaFunction(GeometryFunction param) throws ParseException {
		if (param == null)
			throw new ParseException("The ADQL function AREA must have one non-null parameter !");
		
		parameter = param;
	}
	
	@Override
	public String getName() {
		return "AREA";
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{parameter};
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getArea(this);
		if (sql == null)
			return super.primaryToSQL(translator);
		else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		AreaFunction copy = new AreaFunction((GeometryFunction)parameter.getCopy());
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (searchCondition.match(parameter) && replacementObject instanceof GeometryFunction){
			matchedObj = parameter;
			parameter = (GeometryFunction)replacementObject;
		}else
			matchedObj = parameter.replaceBy(searchCondition, replacementObject);
		
		return matchedObj;
	}

}