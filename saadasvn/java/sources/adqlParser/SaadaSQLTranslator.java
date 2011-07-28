package adqlParser;

import saadadb.database.DbmsWrapper;
import adqlParser.parser.ParseException;
import adqlParser.parser.SQLTranslator;
import adqlParser.query.ADQLOperand;
import adqlParser.query.function.ADQLFunction;
import adqlParser.query.function.geometry.BoxFunction;
import adqlParser.query.function.geometry.CircleFunction;
import adqlParser.query.function.geometry.ContainsFunction;
import adqlParser.query.function.geometry.DistanceFunction;
import adqlParser.query.function.geometry.PointFunction;

public class SaadaSQLTranslator extends SQLTranslator {

	public SaadaSQLTranslator() {;}

	@Override
	public SQLTranslator getCopy() {
		return new SaadaSQLTranslator();
	}

	@Override
	public String getDistance(DistanceFunction fct) throws ParseException {
		if (fct == null)
			return null;
		
		PointFunction p1 = fct.getP1(), p2 = fct.getP2();
		return "distancedegree("+p1.getCoord1().toSQL(null)+", "+p1.getCoord2().toSQL(null)+", "+p2.getCoord1().toSQL(null)+", "+p2.getCoord2().toSQL(null)+")";
	}

	@Override
	public String getContains(ContainsFunction fct) throws ParseException {
		if (fct == null)
			return null;
		
		ADQLOperand leftParam=fct.getFirstGeom(), rightParam=fct.getSecondGeom();
		String sql = fct.getName()+"("+leftParam.toSQL(this)+", "+rightParam.toSQL(this)+")";
		// POINT, ...
		if (leftParam instanceof ADQLFunction && ((ADQLFunction)leftParam).getName().equalsIgnoreCase("POINT")){
			PointFunction p = (PointFunction)leftParam;
			// BOX
			if (rightParam instanceof ADQLFunction && ((ADQLFunction)rightParam).getName().equalsIgnoreCase("BOX")){
				BoxFunction b = (BoxFunction)rightParam;
				sql = DbmsWrapper.getIsInBoxConstraint(p.getCoord1().toSQL(null), p.getCoord2().toSQL(null), b.getCoord1().toSQL(null), b.getCoord2().toSQL(null), b.getWidth().toSQL(null), b.getHeight().toSQL(null));
				
			// CIRCLE
			}else if (rightParam instanceof ADQLFunction && ((ADQLFunction)rightParam).getName().equalsIgnoreCase("CIRCLE")){
				CircleFunction c = (CircleFunction)rightParam;
				sql = DbmsWrapper.getADQLIsInCircleConstraint(p.getCoord1().toSQL(null), p.getCoord2().toSQL(null), c.getCoord1().toSQL(null), c.getCoord2().toSQL(null), c.getRadius().toSQL(null));
			}
		}
		return sql;
	}
	
}
