package adql.parser;

/*
 * This file is part of ADQLLibrary.
 * 
 * ADQLLibrary is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ADQLLibrary is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ADQLLibrary.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2012 - UDS/Centre de Données astronomiques de Strasbourg (CDS)
 */

import java.util.Collection;
import java.util.Vector;

import adql.parser.IdentifierItems.IdentifierItem;
import adql.query.ADQLOrder;
import adql.query.ADQLQuery;
import adql.query.ClauseConstraints;
import adql.query.ColumnReference;
import adql.query.IdentifierField;
import adql.query.SelectItem;
import adql.query.TextPosition;
import adql.query.constraint.ADQLConstraint;
import adql.query.constraint.Between;
import adql.query.constraint.Comparison;
import adql.query.constraint.ComparisonOperator;
import adql.query.constraint.ConstraintsGroup;
import adql.query.constraint.Exists;
import adql.query.constraint.In;
import adql.query.constraint.IsNull;
import adql.query.constraint.NotConstraint;
import adql.query.from.ADQLJoin;
import adql.query.from.ADQLTable;
import adql.query.from.CrossJoin;
import adql.query.from.FromContent;
import adql.query.from.InnerJoin;
import adql.query.from.OuterJoin;
import adql.query.from.OuterJoin.OuterType;
import adql.query.operand.ADQLColumn;
import adql.query.operand.ADQLOperand;
import adql.query.operand.Concatenation;
import adql.query.operand.NegativeOperand;
import adql.query.operand.NumericConstant;
import adql.query.operand.Operation;
import adql.query.operand.OperationType;
import adql.query.operand.StringConstant;
import adql.query.operand.WrappedOperand;
import adql.query.operand.function.DefaultUDF;
import adql.query.operand.function.MathFunction;
import adql.query.operand.function.MathFunctionType;
import adql.query.operand.function.SQLFunction;
import adql.query.operand.function.SQLFunctionType;
import adql.query.operand.function.UserDefinedFunction;
import adql.query.operand.function.geometry.AreaFunction;
import adql.query.operand.function.geometry.BoxFunction;
import adql.query.operand.function.geometry.CentroidFunction;
import adql.query.operand.function.geometry.CircleFunction;
import adql.query.operand.function.geometry.ContainsFunction;
import adql.query.operand.function.geometry.DistanceFunction;
import adql.query.operand.function.geometry.ExtractCoord;
import adql.query.operand.function.geometry.ExtractCoordSys;
import adql.query.operand.function.geometry.GeometryFunction;
import adql.query.operand.function.geometry.GeometryFunction.GeometryValue;
import adql.query.operand.function.geometry.IntersectsFunction;
import adql.query.operand.function.geometry.PointFunction;
import adql.query.operand.function.geometry.PolygonFunction;
import adql.query.operand.function.geometry.RegionFunction;

/**
 * <p>This class lets the {@link ADQLParser} to build an object representation of an ADQL query.</p>
 * 
 * <p>To customize the object representation you merely have to extends the appropriate functions of this class.</p>
 * 
 * @author Gr&eacute;gory Mantelet (CDS)
 * @version 08/2011
 * 
 * @see ADQLParser
 */
public class ADQLQueryFactory {

	protected boolean allowUnknownFunctions = false;

	public static enum JoinType {
		CROSS,
		INNER,
		OUTER_LEFT,
		OUTER_RIGHT,
		OUTER_FULL;
	}

	public ADQLQueryFactory(){ ; }

	public ADQLQueryFactory(boolean allowUnknownFunctions){
		this.allowUnknownFunctions = allowUnknownFunctions;
	}

	public ADQLQuery createQuery() throws Exception {
		return new ADQLQuery();
	}

	public ADQLTable createTable(final IdentifierItems idItems, final IdentifierItem alias) throws Exception {
		ADQLTable t = new ADQLTable(idItems.getCatalog(), idItems.getSchema(), idItems.getTable());

		// Set the table alias:
		if (alias != null)
			t.setAlias(alias.identifier);

		// Set the case sensitivity on the table name parts:
		byte caseSensitivity = idItems.getCaseSensitivity();
		if (alias != null)
			caseSensitivity = IdentifierField.ALIAS.setCaseSensitive(caseSensitivity, alias.caseSensitivity);
		t.setCaseSensitive(caseSensitivity);

		// Set the position in the query:
		t.setPosition(idItems.getPosition());

		return t;
	}

	public ADQLTable createTable(ADQLQuery query, IdentifierItem alias) throws Exception {
		ADQLTable t = new ADQLTable(query);

		if (alias != null){
			// Set the table alias:
			t.setAlias(alias.identifier);
			// Set the case sensitivity:
			t.setCaseSensitive(IdentifierField.ALIAS, alias.caseSensitivity);
		}

		return t;
	}

	public ADQLJoin createJoin(JoinType type, FromContent leftTable, FromContent rightTable) throws Exception {
		switch(type){
		case CROSS:
			return new CrossJoin(leftTable, rightTable);
		case INNER:
			return new InnerJoin(leftTable, rightTable);
		case OUTER_LEFT:
			return new OuterJoin(leftTable, rightTable, OuterType.LEFT);
		case OUTER_RIGHT:
			return new OuterJoin(leftTable, rightTable, OuterType.RIGHT);
		case OUTER_FULL:
			return new OuterJoin(leftTable, rightTable, OuterType.FULL);
		default:
			throw new Exception("Unknown join type: "+type);
		}
	}

	public ADQLJoin createJoin(JoinType type, FromContent leftTable, FromContent rightTable, ClauseConstraints condition) throws Exception {
		switch(type){
		case CROSS:
			throw new Exception("A cross join must have no condition (that's to say: no part ON) !");
		default:
			ADQLJoin join = createJoin(type, leftTable, rightTable);
			join.setJoinCondition(condition);
			return join;
		}
	}

	public ADQLJoin createJoin(JoinType type, FromContent leftTable, FromContent rightTable, Collection<ADQLColumn> lstColumns) throws Exception {
		switch(type){
		case CROSS:
			throw new Exception("A cross join must have no columns list (that's to say: no part USING) !");
		default:
			ADQLJoin join = createJoin(type, leftTable, rightTable);
			join.setJoinedColumns(lstColumns);
			return join;
		}
	}

	public SelectItem createSelectItem(ADQLOperand operand, String alias) throws Exception {
		return new SelectItem(operand, alias);
	}

	public ADQLColumn createColumn(final IdentifierItems idItems) throws Exception {
		ADQLColumn col = new ADQLColumn(idItems.getCatalog(), idItems.getSchema(), idItems.getTable(), idItems.getColumn());

		// Set the case sensitivity:
		col.setCaseSensitive(idItems.getCaseSensitivity());

		// Set the position:
		col.setPosition(idItems.getPosition());

		return col;
	}

	public ADQLColumn createColumn(final IdentifierItem columnName) throws Exception {
		ADQLColumn col = new ADQLColumn(null, null, null, columnName.identifier);

		// Set the case sensitivity:
		col.setCaseSensitive(IdentifierField.COLUMN, columnName.caseSensitivity);

		// Set the position:
		col.setPosition(columnName.position);
		return col;
	}

	public NumericConstant createNumericConstant(String value) throws Exception {
		return new NumericConstant(value, true);
	}

	public StringConstant createStringConstant(String value) throws Exception {
		return new StringConstant(value);
	}

	public Operation createOperation(ADQLOperand leftOp, OperationType op, ADQLOperand rightOp) throws Exception {
		return new Operation(leftOp, op, rightOp);
	}

	public NegativeOperand createNegativeOperand(ADQLOperand opToNegativate) throws Exception {
		return new NegativeOperand(opToNegativate);
	}

	public Concatenation createConcatenation() throws Exception {
		return new Concatenation();
	}

	public WrappedOperand createWrappedOperand(ADQLOperand opToWrap) throws Exception {
		return new WrappedOperand(opToWrap);
	}

	public ConstraintsGroup createGroupOfConstraints() throws Exception {
		return new ConstraintsGroup();
	}

	public NotConstraint createNot(ADQLConstraint constraintToNot) throws Exception {
		return new NotConstraint(constraintToNot);
	}

	public Comparison createComparison(ADQLOperand leftOp, ComparisonOperator op, ADQLOperand rightOp) throws Exception {
		return new Comparison(leftOp, op, rightOp);
	}

	public Between createBetween(boolean not, ADQLOperand value, ADQLOperand min, ADQLOperand max) throws Exception {
		return new Between(value, min, max, not);
	}

	public IsNull createIsNull(boolean notNull, ADQLColumn column) throws Exception {
		return new IsNull(column, notNull);
	}

	public Exists createExists(ADQLQuery query) throws Exception {
		return new Exists(query);
	}

	public In createIn(ADQLOperand leftOp, ADQLQuery query, boolean notIn) throws Exception {
		return new In(leftOp, query, notIn);
	}

	public In createIn(ADQLOperand leftOp, ADQLOperand[] valuesList, boolean notIn) throws Exception {
		return new In(leftOp, valuesList, notIn);
	}

	public SQLFunction createSQLFunction(SQLFunctionType type, ADQLOperand op, boolean distinctValues) throws Exception {
		return new SQLFunction(type, op, distinctValues);
	}

	public MathFunction createMathFunction(MathFunctionType type, ADQLOperand param1, ADQLOperand param2) throws Exception {
		return new MathFunction(type, param1, param2);
	}

	/**
	 * <p>Creates the user defined functions called as the given name and with the given parameters.</p>
	 * <p><b>IMPORTANT: This function must be overridden if some user defined functions are available.</b></p>
	 * 
	 * @param name			Name of the user defined function to create.
	 * @param params		Parameters of the user defined function to create.
	 * 
	 * @return				The corresponding user defined function.
	 * 
	 * @throws Exception	An {@link UnsupportedOperationException} by default, otherwise any other type of error may be
	 * 						thrown if there is a problem while creating the function.
	 */
	public UserDefinedFunction createUserDefinedFunction(String name, ADQLOperand[] params) throws Exception {
		if (allowUnknownFunctions)
			return new DefaultUDF(name, params);
		else
			throw new UnsupportedOperationException("No ADQL function called \""+name+"\" !");
	}

	public DistanceFunction createDistance(PointFunction point1, PointFunction point2) throws Exception {
		return new DistanceFunction(new GeometryValue<PointFunction>(point1), new GeometryValue<PointFunction>(point2));
	}

	public DistanceFunction createDistance(GeometryValue<PointFunction> point1, GeometryValue<PointFunction> point2) throws Exception {
		return new DistanceFunction(point1, point2);
	}

	public PointFunction createPoint(ADQLOperand coordSys, ADQLOperand coords, ADQLOperand coords2) throws Exception {
		return new PointFunction(coordSys, coords, coords2);
	}

	public BoxFunction createBox(ADQLOperand coordinateSystem, ADQLOperand firstCoord, ADQLOperand secondCoord, ADQLOperand boxWidth, ADQLOperand boxHeight) throws Exception {
		return new BoxFunction(coordinateSystem, firstCoord, secondCoord, boxWidth, boxHeight);
	}

	public CircleFunction createCircle(ADQLOperand coordSys, ADQLOperand coord1, ADQLOperand coord2, ADQLOperand radius) throws Exception {
		return new CircleFunction(coordSys, coord1, coord2, radius);
	}

	public CentroidFunction createCentroid(GeometryFunction param) throws Exception {
		return new CentroidFunction(new GeometryValue<GeometryFunction>(param));
	}

	public CentroidFunction createCentroid(GeometryValue<GeometryFunction> param) throws Exception {
		return new CentroidFunction(param);
	}

	public RegionFunction createRegion(ADQLOperand param) throws Exception {
		return new RegionFunction(param);
	}

	public PolygonFunction createPolygon(ADQLOperand coordSys, Vector<ADQLOperand> coords) throws Exception {
		return new PolygonFunction(coordSys, coords);
	}

	public AreaFunction createArea(GeometryFunction param) throws Exception {
		return new AreaFunction(new GeometryValue<GeometryFunction>(param));
	}

	public AreaFunction createArea(GeometryValue<GeometryFunction> param) throws Exception {
		return new AreaFunction(param);
	}

	public ExtractCoord createCoord1(PointFunction point) throws Exception {
		return new ExtractCoord(1, new GeometryValue<PointFunction>(point));
	}

	public ExtractCoord createCoord1(ADQLColumn point) throws Exception {
		return new ExtractCoord(1, new GeometryValue<PointFunction>(point));
	}

	public ExtractCoord createCoord2(PointFunction point) throws Exception {
		return new ExtractCoord(2, new GeometryValue<PointFunction>(point));
	}

	public ExtractCoord createCoord2(ADQLColumn point) throws Exception {
		return new ExtractCoord(2, new GeometryValue<PointFunction>(point));
	}

	public ExtractCoordSys createExtractCoordSys(GeometryFunction param) throws Exception {
		return new ExtractCoordSys(new GeometryValue<GeometryFunction>(param));
	}

	public ExtractCoordSys createExtractCoordSys(ADQLColumn param) throws Exception {
		return new ExtractCoordSys(new GeometryValue<GeometryFunction>(param));
	}

	public ExtractCoordSys createExtractCoordSys(GeometryValue<GeometryFunction> param) throws Exception {
		return new ExtractCoordSys(new GeometryValue<GeometryFunction>(param));
	}

	public ContainsFunction createContains(GeometryFunction left, GeometryFunction right) throws Exception {
		return new ContainsFunction(new GeometryValue<GeometryFunction>(left), new GeometryValue<GeometryFunction>(right));
	}

	public ContainsFunction createContains(GeometryValue<GeometryFunction> left, GeometryValue<GeometryFunction> right) throws Exception {
		return new ContainsFunction(left, right);
	}

	public IntersectsFunction createIntersects(GeometryFunction left, GeometryFunction right) throws Exception {
		return new IntersectsFunction(new GeometryValue<GeometryFunction>(left), new GeometryValue<GeometryFunction>(right));
	}

	public IntersectsFunction createIntersects(GeometryValue<GeometryFunction> left, GeometryValue<GeometryFunction> right) throws Exception {
		return new IntersectsFunction(left, right);
	}

	public ADQLOrder createOrder(final int ind, final boolean desc, final TextPosition position) throws Exception {
		ADQLOrder order = new ADQLOrder(ind, desc);
		if (order != null)
			order.setPosition(position);
		return order;
	}

	public ADQLOrder createOrder(final IdentifierItems idItems, final boolean desc) throws Exception {
		ADQLOrder order = new ADQLOrder(idItems.join("."), desc);
		if (order != null){
			order.setPosition(idItems.getPosition());
			order.setCaseSensitive(idItems.getColumnCaseSensitivity());
		}
		return order;
	}

	public ColumnReference createColRef(final IdentifierItems idItems) throws Exception {
		ColumnReference colRef = new ColumnReference(idItems.join("."));
		if (colRef != null){
			colRef.setPosition(idItems.getPosition());
			colRef.setCaseSensitive(idItems.getColumnCaseSensitivity());
		}
		return colRef;
	}

	public ColumnReference createColRef(final int index, final TextPosition position) throws Exception {
		ColumnReference colRef = new ColumnReference(index);
		if (colRef != null)
			colRef.setPosition(position);
		return colRef;
	}
}
