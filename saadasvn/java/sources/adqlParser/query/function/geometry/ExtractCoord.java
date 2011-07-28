package adqlParser.query.function.geometry;

import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLObject;
import adqlParser.query.ADQLOperand;
import adqlParser.query.SearchHandler;

/**
 * <p>It represents the COORD1 and the COORD2 functions of the ADQL language.</p>
 * <p>This function extracts the first or the second coordinate value (in degrees) of a given POINT or column reference.</p>
 * <p><i><u>Example:</u><br />
 * COORD1(POINT('ICRS GEOCENTER', 25.0, -19.5))<br />
 * In this example the function extracts the right ascension of a point with position (25, -19.5) in degrees according to the ICRS coordinate
 * system with GEOCENTER reference position.
 * </i></p> 
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see GeometryFunction
 */
public class ExtractCoord extends GeometryFunction {

	/** Number of the coordinate to extract (1 or 2). */
	protected final int indCoord;
	
	/** The point from which the coordinate must be extracted. */
	protected PointFunction point;
	
	/** The column which contains the point from which the coordinate must be extracted. */
	protected ADQLColumn column;
	
	
	/**
	 * Creates a COORD1 or a COORD2 function with a POINT function.
	 * 
	 * @param indiceCoord		1 or 2: the index of the coordinate to extract.
	 * @param p					The POINT function from which the <i>indiceCoord</i>-th coordinate must be extracted.
	 * @throws ParseException	If the given index is different from 1 and 2 or if the given parameter is incorrect.
	 */
	public ExtractCoord(int indiceCoord, PointFunction p) throws ParseException {
		if (indiceCoord <= 0 || indiceCoord > 2)
			throw new ParseException("Impossible to extract another coordinate that the two first: only COORD1 and COORD2 exists in ADQL !");
		indCoord = indiceCoord;
		
		if (p==null)
			throw new ParseException("The ADQL function "+getName()+" must have exactly one parameter which is either a POINT(...) or a column reference !");
		point = p;
		column = null;
	}
	
	/**
	 * Creates a COORD1 or a COORD2 function with a column reference.
	 * 
	 * @param indiceCoord		1 or 2: the index of the coordinate to extract.
	 * @param col				The reference to the column which contains the POINT function from which the <i>indiceCoord</i>-th coordinate must be extracted.
	 * @throws ParseException	If the given index is not 1 or 2 or if the given parameter is incorrect.
	 */
	public ExtractCoord(int indiceCoord, ADQLColumn col) throws ParseException {
		if (indiceCoord <= 0 || indiceCoord > 2)
			throw new ParseException("Impossible to extract another coordinate that the two first: only COORD1 and COORD2 exists in ADQL !");
		indCoord = indiceCoord;
		
		if (col==null)
			throw new ParseException("The ADQL function "+getName()+" must have exactly one parameter which is either a POINT(...) or a column reference !");
		column = col;
		point = null;
	}
	
	@Override
	public String getName() {
		return "COORD"+indCoord;
	}

	@Override
	public ADQLOperand[] getParameters() {
		return new ADQLOperand[]{(point!=null)?point:column};
	}

	@Override
	public String primaryToString() {
		String sql = (negative?"-":"")+getName()+"("+indCoord+", "+((point!=null)?point.toString():column.toString())+")";
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	@Override
	public String primaryToSQL() throws ParseException {
		String sql = (negative?"-":"")+getName()+"("+indCoord+", "+((point!=null)?point.toSQL():column.toSQL())+")";
		return sql+((alias==null)?"":(" AS \""+alias+"\""));
	}

	@Override
	public String primaryToSQL(SQLTranslator translator) throws ParseException {
		String sql = translator.getExtractCoord(this);
		if (sql == null){
			sql = (negative?"-":"")+getName()+"("+indCoord+", "+((point!=null)?point.toSQL(translator):column.toSQL(translator))+")";
			return sql+((alias==null)?"":(" AS \""+alias+"\""));
		}else
			return sql;
	}

	@Override
	public ADQLConstraint primaryGetCopy() throws ParseException {
		ExtractCoord copy = (point!=null)?(new ExtractCoord(indCoord, point)):(new ExtractCoord(indCoord, column));
		copy.setAlias(alias);
		copy.negativate(negative);
		return copy;
	}

	public ADQLObject primaryReplaceBy(SearchHandler searchCondition, ADQLObject replacementObject) throws ParseException {
		ADQLObject matchedObj = null;
		
		if (point != null){
			if (searchCondition.match(point) && replacementObject instanceof PointFunction){
				matchedObj = point;
				point = (PointFunction)replacementObject;
			}else
				matchedObj = point.replaceBy(searchCondition, replacementObject);
		}else{
			if (searchCondition.match(column) && replacementObject instanceof ADQLColumn){
				matchedObj = column;
				column = (ADQLColumn)replacementObject;
			}else
				matchedObj = point.replaceBy(searchCondition, replacementObject);
		}
		
		return matchedObj;
	}

}