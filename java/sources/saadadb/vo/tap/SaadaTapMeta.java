package saadadb.vo.tap;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import saadadb.database.Database;
import saadadb.database.MysqlWrapper;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.sqltable.SQLLargeQuery;
import tap.TAPException;
import tap.metadata.TAPColumn;
import tap.metadata.TAPMetadata;
import tap.metadata.TAPSchema;
import tap.metadata.TAPTable;

/**
 * Query the Database to  build a TapMetadata instance of data available to the service
 * @author hahn
 *
 */
public class SaadaTapMeta {
	/*
	 * The TAPMetadata that is built for the Tap service
	 */
	protected TAPMetadata tapMeta;

	/*
	 * The columns that won't appear in the TAPMetadata
	 */
	protected ArrayList<String> columnFilters; //

	public SaadaTapMeta() throws SQLException, Exception {
		tapMeta = new TAPMetadata();
	}

	/**
	 * Query the database (if necessary) to build a representation of all available metadata
	 * @return a representation of all available Metadata
	 * @throws TAPException
	 */
	public TAPMetadata getTAPMetadata() throws TAPException {
		columnFilters = new ArrayList<String>();
		loadColumnFilters();
		loadMetadata();
		return tapMeta;
	}

	/**
	 * Load all available MetaData from the DB into the the TAPMetada attribute
	 * @throws TAPException
	 */
	protected void loadMetadata() throws TAPException {
		loadTapSchemas();
		loadTapTables();
		loadTapColumns();
	}

	/**
	 * Load all available Schemas from the DB into the the TAPMetada attribute
	 * @throws TAPException
	 */
	protected void loadTapSchemas() throws TAPException {
		// System.out.println("Load tap meta");
		if (!tableExists("schemas")) {
			throw new TAPException("Table 'schemas' does not exist");
		}

		try {
			// System.out.println("load schema");
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result;
			try {
				if (Database.getWrapper() instanceof MysqlWrapper) {
					result = query.run("Select " + "schema_name, description, utype "
							+ "from `schemas`");
				} else {
					result = query.run("Select " + "schema_name, description, utype "
							+ "from schemas");
				}
			} catch (FatalException e) {
				throw new TAPException("Could not get Database Wrapper", e);
			}
			TAPSchema schema;

			while (result.next()) {
				schema = new TAPSchema(result.getString("schema_name"));
				schema.setDBName(Database.getDbname());
				schema.setDescription(result.getString("description"));
				schema.setUtype(result.getString("utype"));
				tapMeta.addSchema(schema);
			}
			query.close();
		} catch (QueryException | SQLException e) {
			throw new TAPException(e.getMessage());
		}
	}

	/**
	 * Load all available Tables from the DB into the the TAPMetada attribute
	 * @throws TAPException
	 */
	protected void loadTapTables() throws TAPException {
		if (!tableExists("tables")) {
			throw new TAPException("Table 'TABLES' does not exist");
		}
		TAPTable table;
		try {
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result = query.run("Select "
					+ "schema_name, table_name, table_type, description, utype " + "from tables");
			while (result.next()) {
				table = new TAPTable(result.getString("table_name"));
				table.setDBName(Database.getDbname());
				table.setDescription(result.getString("description"));
				table.setType(result.getString("table_type"));
				table.setUtype(result.getString("utype"));

				String schemaName = result.getString("schema_name");
				tapMeta.getSchema(schemaName).addTable(table);
			}
			query.close();
		} catch (QueryException | SQLException e) {
			throw new TAPException(e.getMessage());
		}
	}

	/**
	 * Load all available columns from the DB into the the TAPMetada attribute
	 * It also checks if the current column is on the filter list. If yes, the column is not added to the Metadata
	 * @throws TAPException
	 */
	protected void loadTapColumns() throws TAPException {
		if (!tableExists("columns")) {
			throw new TAPException("Table 'COLUMNS' does not exist");
		}
		TAPColumn column;
		try {
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result = query
					.run("Select "
							+ "table_name, column_name, description, unit, ucd, utype, datatype, size, principal, indexed, std "
							+ "from columns");
			while (result.next()) {
				String tmp;
				column = new TAPColumn(result.getString("column_name"));
				column.setDescription(result.getString("description"));

				tmp = result.getString("size");
				int size = -1;
				try {
					size = Integer.parseInt(tmp);
				} catch (Exception e) {
				}
				column.setDatatype(result.getString("datatype"), size);

				column.setUcd(result.getString("ucd"));
				column.setUnit(result.getString("unit"));
				column.setUtype(result.getString("utype"));

				tmp = result.getString("indexed");
				if (!tmp.isEmpty() && tmp != null)
					column.setIndexed(Database.getWrapper().getBooleanValue(tmp));

				tmp = result.getString("principal");
				if (!tmp.isEmpty() && tmp != null)
					column.setPrincipal(Database.getWrapper().getBooleanValue(tmp));

				tmp = result.getString("std");
				if (!tmp.isEmpty() && tmp != null)
					column.setStd(Database.getWrapper().getBooleanValue(tmp));

				String[] schemaTableName = result.getString("table_name").split("\\.");// [Schema].[Table]

				// Checks if the column should be ignored or not. (see tap.TapProperties.java & config/voconf.tap.properties)
				if (!columnFilters.contains(column.getName().trim().toLowerCase())) {
					tapMeta
							.getSchema(schemaTableName[0])
							.getTable(schemaTableName[1])
							.addColumn(column);
				}
			}
			query.close();
		} catch (SQLException | QueryException | FatalException e1) {
			throw new TAPException(e1.getMessage());
		}
	}

	/**
	 * Checks if the given table name exists in the Database. 
	 * @param tableName
	 * @return true if the given table name exists, false otherwise
	 * @throws TAPException
	 */
	protected boolean tableExists(String tableName) throws TAPException {

		try {
			return Database.getWrapper().tableExist(tableName);
//			DatabaseMetaData dbMeta;
//			DatabaseConnection con = Database.getConnection();
//			dbMeta = con.getMetaData();
//			ResultSet rs = dbMeta.getTables(null, null, tableName, null);
//			boolean result = false;
//			if (rs.next()) {
//				result = true;
//			}
//			Database.giveConnection(con);
//			return result;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * loads an arraylist representing the columns that won't be added in the TAPMetadata
	 */
	protected void loadColumnFilters() {
		this.columnFilters = TapProperties.getColumnFilter();
	}
}