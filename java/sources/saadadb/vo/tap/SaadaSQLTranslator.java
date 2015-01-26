package saadadb.vo.tap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.database.Database;
import saadadb.database.DbmsWrapper;
import saadadb.util.ChangeKey;
import saadadb.util.DBUtils;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.DBType;
import adql.db.DBColumn;
import adql.query.ADQLList;
import adql.query.ADQLObject;
import adql.query.ADQLOrder;
import adql.query.ADQLQuery;
import adql.query.ClauseConstraints;
import adql.query.ClauseSelect;
import adql.query.ColumnReference;
import adql.query.IdentifierField;
import adql.query.SelectAllColumns;
import adql.query.SelectItem;
import adql.query.constraint.ADQLConstraint;
import adql.query.constraint.Between;
import adql.query.constraint.Comparison;
import adql.query.constraint.Exists;
import adql.query.constraint.In;
import adql.query.constraint.IsNull;
import adql.query.constraint.NotConstraint;
import adql.query.from.ADQLJoin;
import adql.query.from.ADQLTable;
import adql.query.from.FromContent;
import adql.query.operand.ADQLColumn;
import adql.query.operand.ADQLOperand;
import adql.query.operand.Concatenation;
import adql.query.operand.NegativeOperand;
import adql.query.operand.NumericConstant;
import adql.query.operand.Operation;
import adql.query.operand.StringConstant;
import adql.query.operand.WrappedOperand;
import adql.query.operand.function.ADQLFunction;
import adql.query.operand.function.MathFunction;
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
import adql.translator.ADQLTranslator;
import adql.translator.TranslationException;

public class SaadaSQLTranslator implements ADQLTranslator {

	protected boolean inSelect = false;
	protected byte caseSensitivity = 0x00;

	public SaadaSQLTranslator() {
		this(true);
	}

	public SaadaSQLTranslator(final boolean column) {
		caseSensitivity = IdentifierField.COLUMN.setCaseSensitive(caseSensitivity, column);
	}

	public SaadaSQLTranslator(final boolean catalog, final boolean schema, final boolean table, final boolean column) {
		caseSensitivity = IdentifierField.CATALOG.setCaseSensitive(caseSensitivity, catalog);
		caseSensitivity = IdentifierField.SCHEMA.setCaseSensitive(caseSensitivity, schema);
		caseSensitivity = IdentifierField.TABLE.setCaseSensitive(caseSensitivity, table);
		caseSensitivity = IdentifierField.COLUMN.setCaseSensitive(caseSensitivity, column);
	}

	// /**
	// * Appends the full name of the given table to the given StringBuffer.
	// *
	// * @param str The string buffer.
	// * @param dbTable The table whose the full name must be appended.
	// *
	// * @return The string buffer + full table name.
	// */
	// public final StringBuffer appendFullDBName(final StringBuffer str, final DBTable dbTable) {
	// if (dbTable != null) {
	// if (dbTable.getDBCatalogName() != null)
	// appendIdentifier(str, dbTable.getDBCatalogName(), IdentifierField.CATALOG);//.append('.');
	//
	// if (dbTable.getDBSchemaName() != null)
	// appendIdentifier(str, dbTable.getDBSchemaName(), IdentifierField.SCHEMA)
	// ;// .append('.');
	//
	// appendIdentifier(str, dbTable.getDBName(), IdentifierField.TABLE);
	// }
	// return str;
	// }
	/**
	 * Get the Temporary DB if necessary (intended for mysql which uses a different database)
	 * @return the TemporaryDbName (for mysql) appended with a "." or an empty string (for Postgres and sqlite)
	 */
	protected String appendTempDBName() {
		String dbName = "";

		if (DBUtils.getDBType() == DBType.MYSQL) {
			dbName = DBUtils.getTempoDBName() + ".";
		}

		return dbName;
	}

	/**
	 * Appends the given identifier in the given StringBuffer.
	 * 
	 * @param str		The string buffer.
	 * @param id		The identifier to append.
	 * @param field		The type of identifier (column, table, schema, catalog or alias ?).
	 * 
	 * @return			The string buffer + identifier.
	 */
	public final StringBuffer appendIdentifier(
			final StringBuffer str,
			final String id,
			final IdentifierField field) {
		return appendIdentifier(str, id, field.isCaseSensitive(caseSensitivity));
	}

	/**
	 * Appends the given identifier to the given StringBuffer.
	 * 
	 * @param str				The string buffer.
	 * @param id				The identifier to append.
	 * @param caseSensitive		<i>true</i> to format the identifier so that preserving the case sensitivity, <i>false</i> otherwise.
	 * 
	 * @return					The string buffer + identifier.
	 */
	public static final StringBuffer appendIdentifier(
			final StringBuffer str,
			final String id,
			final boolean caseSensitive) {
	
		return str.append(id);
	}

	@Override
	public String translate(ADQLObject obj) throws TranslationException {
		if (obj instanceof ADQLQuery)
			return translate((ADQLQuery) obj);
		else if (obj instanceof ADQLList)
			return translate((ADQLList) obj);
		else if (obj instanceof SelectItem)
			return translate((SelectItem) obj);
		else if (obj instanceof ColumnReference)
			return translate((ColumnReference) obj);
		else if (obj instanceof ADQLTable)
			return translate((ADQLTable) obj);
		else if (obj instanceof ADQLJoin)
			return translate((ADQLJoin) obj);
		else if (obj instanceof ADQLOperand)
			return translate((ADQLOperand) obj);
		else if (obj instanceof ADQLConstraint)
			return translate((ADQLConstraint) obj);
		else
			return obj.toADQL();
	}

	/**
	 * Gets the default SQL output for the given ADQL function.
	 * 
	 * @param fct	The ADQL function to format into SQL.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException	If there is an error during the translation.
	 */
	protected String getDefaultADQLFunction(ADQLFunction fct) throws TranslationException {
		String sql = fct.getName() + "(";

		for (int i = 0; i < fct.getNbParameters(); i++)
			sql += ((i == 0) ? "" : ", ") + translate(fct.getParameter(i));

		return sql + ")";
	}

	protected String getDefaultADQLList(ADQLList<? extends ADQLObject> list)
			throws TranslationException {
		String sql = (list.getName() == null) ? "" : (list.getName() + " ");
		boolean oldInSelect = inSelect;
		inSelect = (list.getName() != null) && list.getName().equalsIgnoreCase("select");

		try {
			for (int i = 0; i < list.size(); i++) {
				sql += ((i == 0) ? "" : (" " + list.getSeparator(i) + " "))
						+ translate(list.get(i));
				// sql += ((i == 0) ? "" : (""))
				// + translate(list.get(i));
				// System.out.println("class "+ list.get(i).getClass());

			}
		} finally {
			inSelect = oldInSelect;
		}
		return sql;
	}

	/**
	 * Gets the default SQL output for a column reference.
	 * 
	 * @param ref	The column reference to format into SQL.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException If there is an error during the translation.
	 */
	protected String getDefaultColumnReference(ColumnReference ref) throws TranslationException {
		if (ref.isIndex()) {
			return "" + ref.getColumnIndex();
		} else {
			if (ref.getDBLink() == null) {
				return (ref.isCaseSensitive() ? ("\"" + ref.getColumnName() + "\"") : ref
						.getColumnName());
			} else {

				DBColumn dbCol = ref.getDBLink();
				StringBuffer colName = new StringBuffer();
				// Use the table alias if any:
				if (ref.getAdqlTable() != null && ref.getAdqlTable().hasAlias())
					// TODO WARNING : check .apprend("."). Can cause the query to fail
					appendIdentifier(colName, ref.getAdqlTable().getAlias(), ref
							.getAdqlTable()
							.isCaseSensitive(IdentifierField.ALIAS));// .append('.');

				// // Use the DBTable if any:
				// else if (dbCol.getTable() != null)
				// colName.append(dbCol.getTable());//(colName, dbCol.getTable());//.append('.');

				appendIdentifier(colName, dbCol.getDBName(), IdentifierField.COLUMN);

				return colName.toString();
			}
		}
	}

	@Override
	public String translate(ADQLQuery query) throws TranslationException {
		StringBuffer sql = new StringBuffer(translate(query.getSelect()));
		sql.append("\nFROM ").append(translate(query.getFrom()));

		if (!query.getWhere().isEmpty())
			sql.append('\n').append(translate(query.getWhere()));

		if (!query.getGroupBy().isEmpty())
			sql.append('\n').append(translate(query.getGroupBy()));

		if (!query.getHaving().isEmpty())
			sql.append('\n').append(translate(query.getHaving()));

		if (!query.getOrderBy().isEmpty())
			sql.append('\n').append(translate(query.getOrderBy()));

		if (query.getSelect().hasLimit())
			sql.append("\nLimit ").append(query.getSelect().getLimit());

		return sql.toString();
	}

	/* *************************** */
	/* ****** LIST & CLAUSE ****** */
	/* *************************** */

	@Override
	public String translate(ADQLList<? extends ADQLObject> list) throws TranslationException {
		if (list instanceof ClauseSelect)
			return translate((ClauseSelect) list);
		else if (list instanceof ClauseConstraints)
			return translate((ClauseConstraints) list);
		else
			return getDefaultADQLList(list);
	}

	@Override
	public String translate(ClauseSelect clause) throws TranslationException {
		String sql = null;

		for (int i = 0; i < clause.size(); i++) {
			if (i == 0) {
				sql = clause.getName() + (clause.distinctColumns() ? " DISTINCT" : "");
			} else
				sql += " " + clause.getSeparator(i);
			Messenger.locateCode("CLAUSE SELECT " + translate(clause.get(i)));
			sql += " " + translate(clause.get(i));
		}

		return sql;
	}

	@Override
	public String translate(ClauseConstraints clause) throws TranslationException {
		return getDefaultADQLList(clause);
	}

	@Override
	public String translate(SelectItem item) throws TranslationException {
		if (item instanceof SelectAllColumns)
			return translate((SelectAllColumns) item);
		StringBuffer translation = new StringBuffer(translate(item.getOperand()));
		if (item.hasAlias()) {
			translation.append(" AS ");
			appendIdentifier(translation, item.getAlias(), item.isCaseSensitive());
		} else
			translation.append(" AS \"").append(item.getName() + "\"");

		return translation.toString();
	}

	@Override
	public String translate(SelectAllColumns item) throws TranslationException {
		HashMap<String, String> mapAlias = new HashMap<String, String>();
		// Fetch the full list of columns to display:
		Iterable<DBColumn> dbCols = null;
		if (item.getAdqlTable() != null && item.getAdqlTable().getDBLink() != null) {
			ADQLTable table = item.getAdqlTable();
			dbCols = table.getDBLink();
			if (table.hasAlias()) {
				String key = table.getDBLink().toString();// appendFullDBName(new StringBuffer(), table.getDBLink()).toString();
				mapAlias
						.put(
								key,
								table.isCaseSensitive(IdentifierField.ALIAS) ? ("\""
										+ table.getAlias() + "\"") : table.getAlias());
			}
		} else if (item.getQuery() != null) {
			dbCols = item.getQuery().getFrom().getDBColumns();
			ArrayList<ADQLTable> tables = item.getQuery().getFrom().getTables();
			for (ADQLTable table : tables) {
				if (table.hasAlias()) {
					String key = table.getDBLink().toString();// appendFullDBName(new StringBuffer(), table.getDBLink()).toString();
					mapAlias
							.put(
									key,
									table.isCaseSensitive(IdentifierField.ALIAS) ? ("\""
											+ table.getAlias() + "\"") : table.getAlias());
				}
			}
		}
		// Write the DB name of all these columns:
		if (dbCols != null) {
			StringBuffer cols = new StringBuffer();
			// cols.append("*");
			for (DBColumn col : dbCols) {

				if (cols.length() > 0)
					cols.append(", ");
				// if (col.getTable() != null) {
				// String fullDbName = col.getTable().toString();//appendFullDBName(new StringBuffer(), col.getTable()).toString();
				// if (mapAlias.containsKey(fullDbName))
				// appendIdentifier(cols, mapAlias.get(fullDbName), false);//.append('.');
				// else
				// cols.append(fullDbName);//.append('.');
				// }
				// cols.append(col.getTable().getDBName()).append(".");
				cols.append(col.getTable().getADQLName()).append(".");
				appendIdentifier(cols, col.getDBName(), IdentifierField.COLUMN);
				cols.append(" AS \"").append(col.getADQLName()).append('\"');

			}

			return cols.toString();
		} else {
			// System.out.println("DB col is null");
			return item.toADQL();
		}
	}

	@Override
	public String translate(ColumnReference ref) throws TranslationException {
		if (ref instanceof ADQLOrder)
			return translate((ADQLOrder) ref);
		else
			return getDefaultColumnReference(ref);
	}

	@Override
	public String translate(ADQLOrder order) throws TranslationException {
		return getDefaultColumnReference(order) + (order.isDescSorting() ? " DESC" : " ASC");

	}

	@Override
	public String translate(ADQLTable table) throws TranslationException {
		StringBuffer sql = new StringBuffer();

		// CASE: SUB-QUERY:
		if (table.isSubQuery())
			sql.append('(').append(translate(table.getSubQuery())).append(')');

		// CASE: TABLE REFERENCE:
		else {
			// // Use the corresponding DB table, if known:
			// if (table.getDBLink() != null)
			// appendFullDBName(sql, table.getDBLink());
			// // Otherwise, use the whole table name given in the ADQL query:
			// else
			if (table.getSchemaName() != null
					&& table.getSchemaName().equalsIgnoreCase("tap_upload")) {
				table.setSchemaName(null);
				sql.append(appendTempDBName());
			} else {

				if (DBUtils.getDBType() == DBType.MYSQL) {
					sql.append(Database.getDbname()).append(".");
				}
			}
			sql.append(table.getName());
		}

		// Add the table alias, if any:
		if (table.hasAlias()) {
			sql.append(" AS ");
			String encapsTable = DBUtils.encapsulate(table.getAlias());
			appendIdentifier(sql, encapsTable, table.isCaseSensitive(IdentifierField.ALIAS));
		}
		return sql.toString();
	}

	@Override
	public String translate(ADQLJoin join) throws TranslationException {
		StringBuffer sql = new StringBuffer(translate(join.getLeftTable()));

		if (join.isNatural())
			sql.append(" NATURAL");
		String joinType = join.getJoinType();
		joinType = joinType.trim().equalsIgnoreCase("inner join") && join.isNatural() ? " JOIN "
				: joinType;
		sql
				.append(' ')
				.append(joinType)
				.append(' ')
				.append(translate(join.getRightTable()))
				.append(' ');

		if (!join.isNatural()) {
			if (join.getJoinCondition() != null)
				sql.append(translate(join.getJoinCondition()));
			else if (join.hasJoinedColumns()) {
				StringBuffer cols = new StringBuffer();
				Iterator<ADQLColumn> it = join.getJoinedColumns();
				while (it.hasNext()) {
					ADQLColumn item = it.next();
					if (cols.length() > 0)
						cols.append(", ");
					if (item.getDBLink() == null)
						appendIdentifier(
								cols,
								item.getColumnName(),
								item.isCaseSensitive(IdentifierField.COLUMN));
					else
						appendIdentifier(cols, item.getDBLink().getDBName(), IdentifierField.COLUMN);
				}
				sql.append("USING (").append(cols).append(')');
			}
		}

		return sql.toString();
	}

	/* ********************* */
	/* ****** OPERAND ****** */
	/* ********************* */
	@Override
	public String translate(ADQLOperand op) throws TranslationException {
		if (op instanceof ADQLColumn)
			return translate((ADQLColumn) op);
		else if (op instanceof Concatenation)
			return translate((Concatenation) op);
		else if (op instanceof NegativeOperand)
			return translate((NegativeOperand) op);
		else if (op instanceof NumericConstant)
			return translate((NumericConstant) op);
		else if (op instanceof StringConstant)
			return translate((StringConstant) op);
		else if (op instanceof WrappedOperand)
			return translate((WrappedOperand) op);
		else if (op instanceof Operation)
			return translate((Operation) op);
		else if (op instanceof ADQLFunction)
			return translate((ADQLFunction) op);
		else
			return op.toADQL();
	}

	@Override
	public String translate(ADQLColumn column) throws TranslationException {
		// Use its DB name if known:
		if (column.getDBLink() != null) {
			DBColumn dbCol = column.getDBLink();
			StringBuffer colName = new StringBuffer();
			// Use the table alias if any:
			if (column.getAdqlTable() != null && column.getAdqlTable().hasAlias())
				appendIdentifier(
						colName,
						column.getAdqlTable().getAlias(),
						column.getAdqlTable().isCaseSensitive(IdentifierField.ALIAS)).append('.');

			
			// Otherwise, use the prefix of the column given in the ADQL query:
			else if (column.getTableName() != null)
				colName = column.getFullColumnPrefix().append('.');

			// appendIdentifier(colName, dbCol.getDBName(), IdentifierField.COLUMN);
			appendIdentifier(colName, (dbCol.getDBName()), IdentifierField.COLUMN);
			return colName.toString();
		}
		// Otherwise, use the whole name given in the ADQL query:
		else {
			return column.getFullColumnName();
		}
	}

	@Override
	public String translate(Concatenation concat) throws TranslationException {
		return translate((ADQLList<ADQLOperand>) concat);
	}

	@Override
	public String translate(NegativeOperand negOp) throws TranslationException {
		return "-" + translate(negOp.getOperand());
	}

	@Override
	public String translate(NumericConstant numConst) throws TranslationException {
		return numConst.getValue();
	}

	@Override
	public String translate(StringConstant strConst) throws TranslationException {
		return "'" + strConst.getValue() + "'";
	}

	@Override
	public String translate(WrappedOperand op) throws TranslationException {
		return "(" + translate(op.getOperand()) + ")";
	}

	@Override
	public String translate(Operation op) throws TranslationException {
		return translate(op.getLeftOperand()) + op.getOperation().toADQL()
				+ translate(op.getRightOperand());
	}

	/* ************************ */
	/* ****** CONSTRAINT ****** */
	/* ************************ */
	@Override
	public String translate(ADQLConstraint cons) throws TranslationException {
		if (cons instanceof Comparison)
			return translate((Comparison) cons);
		else if (cons instanceof Between)
			return translate((Between) cons);
		else if (cons instanceof Exists)
			return translate((Exists) cons);
		else if (cons instanceof In)
			return translate((In) cons);
		else if (cons instanceof IsNull)
			return translate((IsNull) cons);
		else if (cons instanceof NotConstraint)
			return translate((NotConstraint) cons);
		else
			return cons.toADQL();
	}

	@Override
	public String translate(Comparison comp) throws TranslationException {
		StringBuffer result = new StringBuffer();
		result
				.append(translate(comp.getLeftOperand()))
				.append(" ")
				.append(comp.getOperator().toADQL())
				.append(" ");

		/*
		 * Postgresql and sqlite need a value true/false for logical comparison instead of the default 0/1 of ADQL
		 * 
		 */
		String leftOp = comp.getLeftOperand().toADQL().trim();
		String rightOp = comp.getRightOperand().toADQL().trim();

		if (leftOp.startsWith("CONTAINS")) {

			if (DBUtils.getDBType() == DBType.MYSQL || DBUtils.getDBType() == DBType.SQLITE) {
				result.append(rightOp);
			} else {
				if (rightOp.equals("0")) {
					result.append("false");
				} else if (rightOp.equals("1")) {
					result.append("true");
				}
			}

		} else {
			result.append(rightOp);
		}

		return result.toString();
	}

	@Override
	public String translate(Between comp) throws TranslationException {
		return translate(comp.getLeftOperand()) + " BETWEEN " + translate(comp.getMinOperand())
				+ " AND " + translate(comp.getMaxOperand());
	}

	@Override
	public String translate(Exists exists) throws TranslationException {
		return "EXISTS(" + translate(exists.getSubQuery()) + ")";
	}

	@Override
	public String translate(In in) throws TranslationException {
		return translate(in.getOperand()) + " " + in.getName() + " ("
				+ (in.hasSubQuery() ? translate(in.getSubQuery()) : translate(in.getValuesList()))
				+ ")";
	}

	@Override
	public String translate(IsNull isNull) throws TranslationException {
		return translate(isNull.getColumn()) + " IS " + (isNull.isNotNull() ? "NOT " : "") + "NULL";

	}

	@Override
	public String translate(NotConstraint notCons) throws TranslationException {
		return "NOT " + translate(notCons.getConstraint());
	}

	/* *********************** */
	/* ****** FUNCTIONS ****** */
	/* *********************** */
	@Override
	public String translate(ADQLFunction fct) throws TranslationException {
		if (fct instanceof GeometryFunction)
			return translate((GeometryFunction) fct);
		else if (fct instanceof MathFunction)
			return translate((MathFunction) fct);
		else if (fct instanceof SQLFunction)
			return translate((SQLFunction) fct);
		else if (fct instanceof UserDefinedFunction)
			return translate((UserDefinedFunction) fct);
		else
			return getDefaultADQLFunction(fct);
	}

	@Override
	public String translate(SQLFunction fct) throws TranslationException {
		if (fct.getType() == SQLFunctionType.COUNT_ALL)
			return "COUNT(" + (fct.isDistinct() ? "DISTINCT " : "") + "*)";
		else
			return fct.getName() + "(" + (fct.isDistinct() ? "DISTINCT " : "")
					+ translate(fct.getParameter(0)) + ")";
	}

	@Override
	public String translate(MathFunction fct) throws TranslationException {
		switch (fct.getType()) {
		case LOG:
			return "ln(" + ((fct.getNbParameters() >= 1) ? translate(fct.getParameter(0)) : "")
					+ ")";
		case LOG10:
			return "log(10, "
					+ ((fct.getNbParameters() >= 1) ? translate(fct.getParameter(0)) : "") + ")";
		case RAND:
			return "random()";
		case TRUNCATE:
			return "trunc("
					+ ((fct.getNbParameters() >= 2) ? (translate(fct.getParameter(0)) + ", " + translate(fct
							.getParameter(1))) : "") + ")";
		default:
			return getDefaultADQLFunction(fct);
		}
	}

	@Override
	public String translate(UserDefinedFunction fct) throws TranslationException {
		return getDefaultADQLFunction(fct);
	}

	/* *********************************** */
	/* ****** GEOMETRICAL FUNCTIONS ****** */
	/* *********************************** */
	@Override
	public String translate(GeometryFunction fct) throws TranslationException {
		if (fct instanceof AreaFunction)
			return translate((AreaFunction) fct);
		else if (fct instanceof BoxFunction)
			return translate((BoxFunction) fct);
		else if (fct instanceof CentroidFunction)
			return translate((CentroidFunction) fct);
		else if (fct instanceof CircleFunction)
			return translate((CircleFunction) fct);
		else if (fct instanceof ContainsFunction)
			return translate((ContainsFunction) fct);
		else if (fct instanceof DistanceFunction)
			return translate((DistanceFunction) fct);
		else if (fct instanceof ExtractCoord)
			return translate((ExtractCoord) fct);
		else if (fct instanceof ExtractCoordSys)
			return translate((ExtractCoordSys) fct);
		else if (fct instanceof IntersectsFunction)
			return translate((IntersectsFunction) fct);
		else if (fct instanceof PointFunction)
			return translate((PointFunction) fct);
		else if (fct instanceof PolygonFunction)
			return translate((PolygonFunction) fct);
		else if (fct instanceof RegionFunction)
			return translate((RegionFunction) fct);
		else
			return getDefaultGeometryFunction(fct);
	}

	/**
	 * <p>Gets the default SQL output for the given geometrical function.</p>
	 * 
	 * <p><i><u>Note:</u> By default, only the ADQL serialization is returned.</i></p>
	 * 
	 * @param fct	The geometrical function to translate.
	 * 
	 * @return		The corresponding SQL.
	 * 
	 * @throws TranslationException If there is an error during the translation.
	 */
	protected String getDefaultGeometryFunction(GeometryFunction fct) throws TranslationException {
		if (inSelect)
			return "'" + fct.toADQL().replaceAll("'", "''") + "'";
		else
			return getDefaultADQLFunction(fct);
	}

	@Override
	public String translate(GeometryValue<? extends GeometryFunction> geomValue)
			throws TranslationException {
		return translate(geomValue.getValue());
	}

	@Override
	public String translate(ExtractCoord extractCoord) throws TranslationException {
		return "translate(ExtractCoord) not implemented)";
	}

	@Override
	public String translate(ExtractCoordSys extractCoordSys) throws TranslationException {
		return getDefaultGeometryFunction(extractCoordSys);
	}

	@Override
	public String translate(AreaFunction areaFunction) throws TranslationException {
		return "translate(AreaFunction) not implemented)";
	}

	@Override
	public String translate(CentroidFunction centroidFunction) throws TranslationException {
		return "translate(CentroidFunction) not implemented)";
	}

	protected boolean pointContainsRAandDEC(PointFunction point) {
		String nameC1, nameC2;
		nameC1 = point.getCoord1().getName().trim();
		nameC2 = point.getCoord2().getName().trim();
		if (!nameC1.equalsIgnoreCase("s_ra") || !nameC2.equalsIgnoreCase("s_dec")) {
			return false;
		}
		return true;
	}

	@Override
	public String translate(DistanceFunction fct) throws TranslationException {
		if (fct == null)
			return null;
		
		PointFunction p1 = (PointFunction) fct.getP1().getValue();
		PointFunction p2 = (PointFunction) fct.getP2().getValue();
		if (!pointContainsRAandDEC(p1) ^ !pointContainsRAandDEC(p2)) {
			throw new TranslationException(
					"Either of the point should contains 's_ra' and 's_dec' parameters");
		}
		String result = "distancedegree(" + translate(p1.getCoord1()) + ", "
				+ translate(p1.getCoord2()) + ", " + translate(p2.getCoord1()) + ", "
				+ translate(p2.getCoord2()) + ")";
		// System.out.println("SaadaSQLTranslator: distance function='" + result + "'");
		return result;
	}

	@Override
	public String translate(ContainsFunction fct) throws TranslationException {
		if (fct == null)
			return null;
		StringBuffer str = new StringBuffer();
		if (fct.getLeftParam().getName().equalsIgnoreCase("point")) {
			//Point
			PointFunction p = (PointFunction) fct.getLeftParam().getValue();
			if (!pointContainsRAandDEC(p)) {
				throw new TranslationException(
						"The parameter POINT must contains 's_ra' and 's_dec");
			}
			//Box
			if (fct.getRightParam().getName().equalsIgnoreCase("box")) {
				BoxFunction b = (BoxFunction) fct.getRightParam().getValue();
				str
						.append("(")
						.append(
								DbmsWrapper.getIsInBoxConstraint(
										(p.getCoord1().toADQL()),
										(p.getCoord2().toADQL()),
										(b.getCoord1().toADQL()),
										(b.getCoord2().toADQL()),
										(translate(b.getWidth())),
										(translate(b.getHeight()))))
						.append(")");
				//CIRCLE
			} else if (fct.getRightParam().getName().equalsIgnoreCase("circle")) {
				CircleFunction c = (CircleFunction) fct.getRightParam().getValue();

				try {
					
					str.append("(").append(
							DbmsWrapper.getADQLIsInCircleConstraint(
									(p.getCoord1().toADQL()),
									(p.getCoord2().toADQL()),
									(c.getCoord1().toADQL()),
									(c.getCoord2().toADQL()),
									(translate(c.getRadius()))));
				} catch (Exception e) {
					throw new TranslationException("Failed to translate circle constraint", e);
				}
				str.append(")");
			}
		}
		// System.out.println("contains: " + str.toString());
		return str.toString();
	}

	@Override
	public String translate(IntersectsFunction fct) throws TranslationException {
		throw new TranslationException("translate(IntersectsFunction) not implemented");
	}

	@Override
	public String translate(PointFunction point) throws TranslationException {
		throw new TranslationException("translate(PointFunction) not implemented");
	}

	@Override
	public String translate(CircleFunction circle) throws TranslationException {
		// healpix
		throw new TranslationException("translate(CircleFunction) not implemented");
	}

	@Override
	public String translate(BoxFunction box) throws TranslationException {

		throw new TranslationException("translate(BoxFunction) not implemented");

	}

	@Override
	public String translate(PolygonFunction polygon) throws TranslationException {
		// hecds

		throw new TranslationException("translate(PolygonFunction) not implemented");
	}

	@Override
	public String translate(RegionFunction region) throws TranslationException {

		throw new TranslationException("translate(Regionfunction) not implemented");
	}

	@Override
	public String translate(FromContent content) throws TranslationException {
		if (content instanceof ADQLTable)
			return translate((ADQLTable) content);
		else if (content instanceof ADQLJoin)
			return translate((ADQLJoin) content);
		else
			return content.toADQL();
	}

	protected static Boolean isNumericOnly(String adql) {
		Pattern pattern = Pattern.compile("^[0-9\\.]*$");
		Matcher matcher = pattern.matcher(adql);
		if (matcher.matches())
			return true;
		return false;
	}

	/**
	 * Checks if the param is only numeric. return the param as is if yes. Otherwise
	 * it performs a Changekey#changeKey on the column name the remove illegal characters to match the column name stored in DB
	 * @param adql the
	 * @return
	 */
	protected static String addUnderscoreToColumnName(String adql) {

		if (isNumericOnly(adql))
			return adql;

		String[] split = adql.split("\\.");
		switch (split.length) {
		case 1:
			return split[0].isEmpty() ? "" : ChangeKey.changeKey(split[0]);
		case 2:
			return split[0] + "." + ChangeKey.changeKey(split[1]);
		case 3:
			return split[0] + "." + split[1] + "." + ChangeKey.changeKey(split[2]);
		default:
			String columnName = split[2];
			for (int i = 3; i < split.length; i++) {
				columnName += "." + split[i];
			}
			columnName = ChangeKey.changeKey(columnName);
			String retour = split[0] + "." + split[1] + "." + columnName;
			return retour;
		}
	}

	protected String getFullDBTableName(ADQLTable table) {
		StringBuffer name = new StringBuffer();
		if (table == null)
			return "";

		// CATALOG:
		String tmp = table.getCatalogName();
		if (tmp != null) {
			name.append(tmp).append('.');
		}
		// SCHEMA:
		tmp = table.getSchemaName();
		if (tmp != null) {
			name.append(tmp).append('.');
		}
		// TABLE:

		if (table.getDBLink() != null) {
			tmp = table.getDBLink().getDBName();
		} else {
			tmp = table.getTableName();
		}
		name.append(tmp);
		Messenger.locateCode("Return get FullDBTableName: " + name.toString());
		return name.toString();
	}

	public static void main(String[] args) {
		String test = "vizier.s_ra";
		String test2 = "collection.vizier.s_ra";
		String test3 = "collection.vizier.s_ra.ytucm.bidule.coucou.c.moi";
		System.out.println(SaadaSQLTranslator.addUnderscoreToColumnName(""));
		System.out.println(SaadaSQLTranslator.addUnderscoreToColumnName("test"));
		System.out.println(SaadaSQLTranslator.addUnderscoreToColumnName(test));
		System.out.println(SaadaSQLTranslator.addUnderscoreToColumnName(test2));
		System.out.println(SaadaSQLTranslator.addUnderscoreToColumnName(test3));
	}
}
