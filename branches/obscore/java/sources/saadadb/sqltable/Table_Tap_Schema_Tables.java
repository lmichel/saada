package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.database.Database;
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
		ah.setNameattr("schema_name"); ah.setNameattr("schema_name"); ah.setType("String"); ah.setComment("the schema name from TAP_SCHEMA.schemas");
		attMap.put("schema_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("table_name"); ah.setNameattr("table_name"); ah.setType("String"); ah.setComment("table name as it should be used in queries");
		attMap.put("table_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("table_type"); ah.setNameattr("table_type"); ah.setType("String"); ah.setComment("one of: table, view");
		attMap.put("table_type", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("String"); ah.setComment("brief description of table");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("String"); ah.setComment("UTYPE if table corresponds to a data model");
		attMap.put("utype", ah);
		}
	/**
	 * @throws SaadaException
	 */
	public static  void createTable() throws SaadaException {
		String sql = "";
		for (AttributeHandler ah: attMap.values() ) {
			if( sql.length() > 0 ) sql += ", ";
			sql += ah.getNameattr() + "  " + Database.getWrapper().getSQLTypeFromJava(ah.getType());
		}
		Messenger.printMsg(Messenger.TRACE, "Create table " + tableName);
		SQLTable.createTable(tableName, sql, "table_name", false);
	}
	
	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(Database.getWrapper().encapsTableName(tableName));
	}
	/**
	 * @param schema
	 * @param table
	 * @param description
	 * @param utype
	 * @throws AbortException
	 */
	public static void addTable(String schema, String table, String description, String utype) throws AbortException {
		String fn = schema + "." + table;
		Messenger.printMsg(Messenger.TRACE, "Add table " + table + " to " + tableName);
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?)"
//				, new Object[]{schema, table, "table",description, utype});
		, new Object[]{schema, fn, "table",description, utype});
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedSchema(String schemaName) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop tables of  schema " + schemaName);
		SQLQuery sq = new SQLQuery();
		ArrayList<String> tempo = new ArrayList<String>();
		ResultSet rs = sq.run("SELECT table_name  FROM " + tableName + " WHERE  schema_name = '" + schemaName + "'");
		while( rs.next() ) {
			tempo.add(rs.getString(1));
		}
		sq.close();
		for( String s: tempo) {
			Table_Tap_Schema_Tables.dropPublishedTable(s);
		}
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "'");		
	}

	/**
	 * @param schemaName
	 * @param tableName
	 * @throws AbortException
	 */
	public static void dropPublishedTable(String schemaName, String table) throws Exception {
		String fn = schemaName + "." + table;
		Messenger.printMsg(Messenger.TRACE, "Drop published table " + schemaName + "." + table);
		Table_Tap_Schema_Keys.dropPublishedTable(schemaName, table);
		Table_Tap_Schema_Columns.dropPublishedTable(schemaName, table);
		//SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "' AND table_name = '" + table + "'");		
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "' AND table_name = '" + fn + "'");		
	}

	/**
	 * @param stable: schema.table
	 * @throws Exception
	 */
	public static void dropPublishedTable(String stable) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop published table " + stable);
		Table_Tap_Schema_Keys.dropPublishedTable(stable);
		Table_Tap_Schema_Columns.dropPublishedTable(stable);
		//SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE schema_name = '" + schemaName + "' AND table_name = '" + table + "'");		
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE table_name = '" + stable + "'");		
	}

	/**
	 * Returns true if bale is already referenced in tap_schema_tables
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public static boolean knowsTable(String schemaName, String table) throws Exception {
		String fn = schemaName + "." + table;
		SQLQuery sq = new SQLQuery();
		boolean retour = false;
		ResultSet rs = sq.run("SELECT table_name FROM " + tableName + " WHERE table_name = '" + fn + "' LIMIT 1");
		while (rs.next()) {
			retour = true;
			break;
		}
		sq.close();
		return  retour;
	}

}
