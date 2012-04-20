package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.LinkedHashMap;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id@
 *
 */
public class Table_Tap_Schema_Tables extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "tables";
	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("schema_name"); ah.setNameattr("schema_name"); ah.setType("VARCHAR"); ah.setComment("the schema name from TAP_SCHEMA.schemas");
		attMap.put("schema_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("table_name"); ah.setNameattr("table_name"); ah.setType("VARCHAR"); ah.setComment("table name as it should be used in queries");
		attMap.put("table_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("table_type"); ah.setNameattr("table_type"); ah.setType("VARCHAR"); ah.setComment("one of: table, view");
		attMap.put("table_type", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("VARCHAR"); ah.setComment("brief description of table");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("VARCHAR"); ah.setComment("UTYPE if table corresponds to a data model");
		attMap.put("utype", ah);
		}
	/**
	 * @throws SaadaException
	 */
	public static  void createTable() throws SaadaException {
		String sql = "";
		for (AttributeHandler ah: attMap.values() ) {
			if( sql.length() > 0 ) sql += ", ";
			sql += ah.getNameattr() + "  " + ah.getType();
		}
		Messenger.printMsg(Messenger.TRACE, "Create table " + tableName);
		SQLTable.createTable(tableName, sql, "table_name", false);
	}
	
	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}
	/**
	 * @param schema
	 * @param table
	 * @param description
	 * @param utype
	 * @throws AbortException
	 */
	public static void addTable(String schema, String table, String description, String utype) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Add table " + table + " to " + tableName);
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?)"
				, new Object[]{schema, table, "table",description, utype});
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedSchema(String schemaName) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop table of  schema " + schemaName);
		SQLQuery sq = new SQLQuery();
		ResultSet rs = sq.run("SELECT table_name  FROM " + tableName + " WHERE  schema_name = '" + schemaName + "' AND table_name = '" + tableName + "'" );
		while( rs.next() ) {
			Table_Tap_Schema_Tables.dropPublishedTable(schemaName, rs.getString(1));
		}
		sq.close();

		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "'");		
	}

	/**
	 * @param schemaName
	 * @param tableName
	 * @throws AbortException
	 */
	public static void dropPublishedTable(String schemaName, String table) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + schemaName + "." + table);
		Table_Tap_Schema_Keys.dropPublishedTable(table);
		Table_Tap_Schema_Columns.dropPublishedTable(table);
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "' AND table_name = '" + table + "'");		
	}

	/**
	 * Returns true if bale is already referenced in tap_schema_tables
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public static boolean knowsTable(String table) throws Exception {
		SQLQuery sq = new SQLQuery();
		ResultSet rs = sq.run("SELECT table_name FROM " + tableName + " WHERE table_name = '" + table + "' LIMIT 1");
		while (rs.next()) {
			return true;
		}
		rs.close();
		return  false;
	}

}
