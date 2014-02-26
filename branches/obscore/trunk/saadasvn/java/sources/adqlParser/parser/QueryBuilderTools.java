package adqlParser.parser;

import java.util.ArrayList;
import java.util.Vector;

import adqlParser.query.ADQLColumn;
import adqlParser.query.ADQLComparison;
import adqlParser.query.ADQLConstantValue;
import adqlParser.query.ADQLConstraint;
import adqlParser.query.ADQLGroup;
import adqlParser.query.ADQLJoin;
import adqlParser.query.ADQLOperand;
import adqlParser.query.ADQLOrder;
import adqlParser.query.ADQLQuery;
import adqlParser.query.ADQLTable;
import adqlParser.query.ADQLType;
import adqlParser.query.ColumnReference;
import adqlParser.query.ComparisonOperator;
import adqlParser.query.Concatenation;
import adqlParser.query.IsNull;
import adqlParser.query.JoinType;
import adqlParser.query.Operation;
import adqlParser.query.OperationType;
import adqlParser.query.function.Exists;
import adqlParser.query.function.InFunction;
import adqlParser.query.function.MathFunction;
import adqlParser.query.function.MathFunctionType;
import adqlParser.query.function.SQLFunction;
import adqlParser.query.function.SQLFunctionType;
import adqlParser.query.function.UserFunction;
import adqlParser.query.function.geometry.AreaFunction;
import adqlParser.query.function.geometry.BoxFunction;
import adqlParser.query.function.geometry.CentroidFunction;
import adqlParser.query.function.geometry.CircleFunction;
import adqlParser.query.function.geometry.ContainsFunction;
import adqlParser.query.function.geometry.DistanceFunction;
import adqlParser.query.function.geometry.ExtractCoord;
import adqlParser.query.function.geometry.ExtractCoordSys;
import adqlParser.query.function.geometry.IntersectsFunction;
import adqlParser.query.function.geometry.PointFunction;
import adqlParser.query.function.geometry.PolygonFunction;
import adqlParser.query.function.geometry.RegionFunction;

/**
 * This class lets the {@link AdqlParser} to build an object representation of an ADQL query.<br />
 * To customize the object representation you merely have to extends the appropriate functions of this class.
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 06/2010
 * 
 * @see AdqlParser
 */
public class QueryBuilderTools {

	public QueryBuilderTools(){
		
	}
	
	public ADQLQuery createQuery() throws ParseException {
		return new ADQLQuery();
	}
	
	public ADQLTable createTable(String table, String alias) throws ParseException {
		ADQLTable t = new ADQLTable(table);
		t.setAlias(alias);
		return t;
	}
	
	public ADQLTable createTable(ADQLQuery query, String alias) throws ParseException {
		ADQLTable t = new ADQLTable(query);
		t.setAlias(alias);
		return t;
	}
	
	public ADQLColumn createColumn(String colName, String prefix, String alias) throws ParseException {
		ADQLColumn col = new ADQLColumn(colName, prefix);
		col.setAlias(alias);
		return col;
	}
	
	public ADQLConstantValue createConstant(String value, ADQLType type) throws ParseException {
		return new ADQLConstantValue(value, type);
	}
	
	public Operation createOperation(ADQLOperand leftOp, OperationType op, ADQLOperand rightOp) throws ParseException {
		return new Operation(leftOp, op, rightOp);
	}
	
	public Concatenation createConcatenation(ADQLOperand leftOp, ADQLOperand rightOp) throws ParseException {
		return new Concatenation(leftOp, rightOp);
	}
	
	public ADQLGroup createGroup(ADQLConstraint constraint) throws ParseException {
		return new ADQLGroup(constraint);
	}
	
	public ADQLComparison createComparison(ADQLOperand leftOp, ComparisonOperator op, ADQLOperand rightOp) throws ParseException {
		return new ADQLComparison(leftOp, op, rightOp);
	}
	
	public IsNull createIsNull(ADQLColumn column, boolean notNull) throws ParseException {
		return new IsNull(column, notNull);
	}
	
	public Exists createExists(ADQLQuery query) throws ParseException {
		return new Exists(query);
	}
	
	public InFunction createIn(ADQLOperand leftOp, ADQLQuery query, boolean notIn) throws ParseException {
		return new InFunction(leftOp, query, notIn);
	}
	
	public InFunction createIn(ADQLOperand leftOp, ADQLOperand[] valuesList, boolean notIn) throws ParseException {
		return new InFunction(leftOp, valuesList, notIn);
	}
	
	public SQLFunction createSQLFunction(SQLFunctionType type, ADQLOperand op, boolean distinctValues, boolean allValues) throws ParseException {
		SQLFunction funct = new SQLFunction(type, op);
		funct.setDistinct(distinctValues);
		funct.setAllValues(allValues);
		return funct;
	}
	
	public MathFunction createMathFunction(MathFunctionType type, ADQLOperand param1, ADQLOperand param2) throws ParseException {
		return new MathFunction(type, param1, param2);
	}
	
	public UserFunction createUserFunction(String name, ADQLOperand[] params) throws ParseException {
		return new UserFunction(name, params);
	}
	
	public DistanceFunction createDistance(PointFunction point1, PointFunction point2) throws ParseException {
		return new DistanceFunction(point1, point2);
	}
	
	public PointFunction createPoint(ADQLOperand coordSys, ADQLOperand coords, ADQLOperand coords2) throws ParseException {
		return new PointFunction(coordSys, coords, coords2);
	}
	
	public BoxFunction createBox(ADQLOperand coordinateSystem, ADQLOperand firstCoord, ADQLOperand secondCoord, ADQLOperand boxWidth, ADQLOperand boxHeight) throws ParseException {
		return new BoxFunction(coordinateSystem, firstCoord, secondCoord, boxWidth, boxHeight);
	}
	
	public CircleFunction createCircle(ADQLOperand coordSys, ADQLOperand coords, ADQLOperand coords2, ADQLOperand width) throws ParseException {
		return new CircleFunction(coordSys, coords, coords2, width);
	}
	
	public CentroidFunction createCentroid(ADQLOperand param) throws ParseException {
		return new CentroidFunction(param);
	}
	
	public RegionFunction createRegion(ADQLOperand param) throws ParseException {
		return new RegionFunction(param);
	}
	
	public PolygonFunction createPolygon(ADQLOperand coordSys, ADQLOperand coord1, ADQLOperand coord2, ADQLOperand coord3, ADQLOperand coord4) throws ParseException {
		return new PolygonFunction(coordSys, coord1, coord2, coord3, coord4);
	}
	
	public PolygonFunction createPolygon(ADQLOperand coordSys, ADQLOperand coord1, ADQLOperand coord2, ADQLOperand coord3, ADQLOperand coord4, ADQLOperand coord5, ADQLOperand coord6) throws ParseException {
		return new PolygonFunction(coordSys, coord1, coord2, coord3, coord4, coord5, coord6);
	}
	
	public AreaFunction createArea(ADQLOperand param) throws ParseException {
		return new AreaFunction(param);
	}
	
	public ExtractCoord createExtractCoord(int indCoord, PointFunction point) throws ParseException {
		return new ExtractCoord(indCoord, point);
	}
	
	public ExtractCoord createExtractCoord(int indCoord, ADQLColumn col) throws ParseException {
		return new ExtractCoord(indCoord, col);
	}
	
	public ExtractCoordSys createExtractCoordSys(ADQLOperand param) throws ParseException {
		return new ExtractCoordSys(param);
	}
	
	public ContainsFunction createContains(ADQLOperand left, ADQLOperand right) throws ParseException {
		return new ContainsFunction(left, right);
	}
	
	public IntersectsFunction createIntersects(ADQLOperand left, ADQLOperand right) throws ParseException {
		return new IntersectsFunction(left, right);
	}

	public ADQLOrder createOrder(int ind, boolean desc) throws ParseException {
		return new ADQLOrder(ind, desc);
	}

	public ADQLOrder createOrder(String colName, boolean desc) throws ParseException {
		return new ADQLOrder(colName, desc);
	}

	public ADQLJoin createJoin(JoinType type, ADQLTable table) throws ParseException {
		return new ADQLJoin(type, table);
	}

	public ADQLJoin createJoin(JoinType type, ADQLTable table, ADQLConstraint condition) throws ParseException {
		return new ADQLJoin(type, table, condition);
	}

	public ADQLJoin createJoin(JoinType type, ADQLTable table, ArrayList<String> lstColumns) throws ParseException {
		return new ADQLJoin(type, table, new Vector<String>(lstColumns));
	}
	
	public ColumnReference createColRef(String colName) throws ParseException {
		return new ColumnReference(colName);
	}
	
	public ColumnReference createColRef(int indCol) throws ParseException {
		return new ColumnReference(indCol);
	}
}
