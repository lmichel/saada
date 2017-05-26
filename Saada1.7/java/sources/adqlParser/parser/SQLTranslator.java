package adqlParser.parser;

import adqlParser.query.ADQLJoin;
import adqlParser.query.function.Exists;
import adqlParser.query.function.InFunction;
import adqlParser.query.function.SQLFunction;
import adqlParser.query.function.geometry.AreaFunction;
import adqlParser.query.function.geometry.CentroidFunction;
import adqlParser.query.function.geometry.ContainsFunction;
import adqlParser.query.function.geometry.DistanceFunction;
import adqlParser.query.function.geometry.ExtractCoord;
import adqlParser.query.function.geometry.ExtractCoordSys;
import adqlParser.query.function.geometry.IntersectsFunction;

public class SQLTranslator {
	
	public SQLTranslator getCopy(){
		return new SQLTranslator();
	}
	
	public String getJoin(ADQLJoin join) throws ParseException{
		return null;
	}
	
	public String getSQLFunction(SQLFunction fct) throws ParseException{
		return null;
	}

	public String getExtractCoord(ExtractCoord extractCoord) throws ParseException {
		return null;
	}

	public String getExtractCoordSys(ExtractCoordSys extractCoordSys) throws ParseException {
		return null;
	}

	public String getArea(AreaFunction areaFunction) throws ParseException {
		return null;
	}

	public String getCentroid(CentroidFunction centroidFunction) throws ParseException {
		return null;
	}
	
	public String getDistance(DistanceFunction fct) throws ParseException{
		return null;
	}
	
	public String getContains(ContainsFunction fct) throws ParseException{
		return null;
	}
	
	public String getIntersects(IntersectsFunction fct) throws ParseException{
		return null;
	}
	
	public String getExists(Exists fct) throws ParseException{
		return null;
	}
	
	public String getIn(InFunction fct) throws ParseException{
		return null;
	}
	
}
