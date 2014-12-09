package saadadb.vo.tap.tap2;

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
 * Query the Database to to build a TapMetadata instance of data available to the seervice
 * @author hahn
 *
 */
public class SaadaTapMeta {

	protected TAPMetadata tapMeta;
	protected ArrayList<String> columnFilters; // the columns that should not appear in the metadata

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
		if (!tableExists("SCHEMAS")) {
			throw new TAPException("Table 'SCHEMAS' does not exist");
		}

		try {
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
			}
			query.close();
		} catch (QueryException | SQLException e) {
			e.printStackTrace();
			throw new TAPException(e.getMessage());
		}
	}
/**
 * Load all available Tables from the DB into the the TAPMetada attribute
 * @throws TAPException
 */
	protected void loadTapTables() throws TAPException {
		if (!tableExists("TABLES")) {
			throw new TAPException("Table 'TABLES' does not exist");
		}

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

			}
			query.close();
		} catch (QueryException | SQLException e) {
			e.printStackTrace();
			throw new TAPException(e.getMessage());
		}
	}
/**
 * Load all available columns from the DB into the the TAPMetada attribute
 * It also checks if the current column is on the filter list. If yes, the column is not added to the Metadata
 * @throws TAPException
 */
	protected void loadTapColumns() throws TAPException {
		if (!tableExists("COLUMNS")) {
			throw new TAPException("Table 'COLUMNS' does not exist");
		}

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
				// System.out.println("Schema\t" + schemaTableName[0] + " table\t"
				// + schemaTableName[1] + "columnColumnName\t " + column.getName()
				// + "Description:\t" + column.getDescription());
				// Add the column to the right Schema and Table

				//Checks if the column should be ignored or not.
				if (!columnFilters.contains(column.getName().trim().toLowerCase())) {
					tapMeta
							.getSchema(schemaTableName[0])
							.getTable(schemaTableName[1])
							.addColumn(column);
				}
//				 else {
//				 System.out.println("@@@\n"
//				 + "Column '"+column.getName()+"' has been filtered. It won't appear in the Tap Metadata");
//				 }
			}
			query.close();
		} catch (SQLException | QueryException | FatalException e1) {
			e1.printStackTrace();
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
 * loads an arraylist representing the columns not to add in the metadata and put it in the columnFilter attribute.
 */
	protected void loadColumnFilters()  {
		this.columnFilters = SaadaTapProperties.getColumnFilter();
	}
}