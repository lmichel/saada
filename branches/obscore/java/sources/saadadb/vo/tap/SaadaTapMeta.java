package saadadb.vo.tap;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import saadadb.database.Database;
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
	//	System.out.println("Load tap meta");
		if (!tableExists("schemas")) {
			throw new TAPException("Table 'SCHEMAS' does not exist");
		}

		try {
			// System.out.println("load schema");
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result = query.run("Select " + "SCHEMA_NAME, DESCRIPTION, UTYPE "
					+ "from SCHEMAS");
			TAPSchema schema;
			
			while (result.next()) {
				schema = new TAPSchema(result.getString("SCHEMA_NAME"));
				schema.setDBName(Database.getDbname());
				schema.setDescription(result.getString("DESCRIPTION"));
				schema.setUtype(result.getString("UTYPE"));

				tapMeta.addSchema(schema);

				// System.out.println("Schema : "+schema.getName());
			}
			query.close();
		} catch (QueryException | SQLException e) {
			// System.out.println("failed to load schema");
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
		// System.out.println("load table");
		TAPTable table;
		try {
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result = query.run("Select "
					+ "SCHEMA_NAME, TABLE_NAME, TABLE_TYPE, DESCRIPTION, UTYPE " + "from tables");
			while (result.next()) {
				table = new TAPTable(result.getString("TABLE_NAME"));
				table.setDBName(Database.getDbname());
				table.setDescription(result.getString("DESCRIPTION"));
				table.setType(result.getString("TABLE_TYPE"));
				table.setUtype(result.getString("UTYPE"));

				String schemaName = result.getString("SCHEMA_NAME");

				tapMeta.getSchema(schemaName).addTable(table);
				// System.out.println("Table : "+schemaName+"."+table.getName());
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
		// System.out.println("load columns");
		TAPColumn column;
		try {
			SQLLargeQuery query = new SQLLargeQuery();
			ResultSet result = query
					.run("Select "
							+ "TABLE_NAME, COLUMN_NAME, DESCRIPTION, UNIT, UCD, UTYPE, DATATYPE, SIZE, PRINCIPAL, INDEXED, STD "
							+ "from columns");
			while (result.next()) {
				String tmp;
				column = new TAPColumn(result.getString("COLUMN_NAME"));
				column.setDescription(result.getString("DESCRIPTION"));

				tmp = result.getString("SIZE");
				int size = -1;
				try {
					size = Integer.parseInt(tmp);
				} catch (Exception e) {
				}
				column.setDatatype(result.getString("DATATYPE"), size);

				column.setUcd(result.getString("UCD"));
				column.setUnit(result.getString("UNIT"));
				column.setUtype(result.getString("UTYPE"));

				tmp = result.getString("INDEXED");
				if (!tmp.isEmpty() && tmp != null)
					column.setIndexed(Database.getWrapper().getBooleanValue(tmp));

				tmp = result.getString("PRINCIPAL");
				if (!tmp.isEmpty() && tmp != null)
					column.setPrincipal(Database.getWrapper().getBooleanValue(tmp));

				tmp = result.getString("STD");
				if (!tmp.isEmpty() && tmp != null)
					column.setStd(Database.getWrapper().getBooleanValue(tmp));

				String[] schemaTableName = result.getString("TABLE_NAME").split("\\.");// [Schema].[Table]
				
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
			DatabaseMetaData dbMeta;
			DatabaseConnection con = Database.getConnection();
			dbMeta = con.getMetaData();
			ResultSet rs = dbMeta.getTables(null, null, tableName, null);
			boolean result = false;
			if (rs.next()) {
				result = true;
			}
			Database.giveConnection(con);
			return result;
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