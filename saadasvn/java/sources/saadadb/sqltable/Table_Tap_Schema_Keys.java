/**
 * 
 */
package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.LinkedHashMap;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id$
 */
public class Table_Tap_Schema_Keys extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "keys";

	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("key_id"); ah.setNameattr("key_id"); ah.setType("VARCHAR"); ah.setComment("unique key identifie");
		attMap.put("key_id", ah);
		ah = new AttributeHandler();
		ah.setNameattr("from_table"); ah.setNameattr("from_table"); ah.setType("VARCHAR"); ah.setComment("fully qualified table name");
		attMap.put("from_table", ah);
		ah = new AttributeHandler();
		ah.setNameattr("target_table"); ah.setNameattr("target_table"); ah.setType("VARCHAR"); ah.setComment("fully qualified table name");
		attMap.put("target_table", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("VARCHAR"); ah.setComment("description of this key");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("VARCHAR"); ah.setComment("utype of this key");
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
		SQLTable.createTable(tableName, sql, null, false);
	}

	/**
	 * Make a collection <> class join on oidsaada.
	 * 2 joins are created: classTable and callTable_rev
	 * @param collTable
	 * @param classTable
	 * @throws AbortException
	 */
	public static void addSaadaJoin(String collTable, String classTable) throws AbortException {
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)"
				, new Object[]{classTable, collTable, classTable , "Collection to Class Saada join"});
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)"
				, new Object[]{classTable + "_rev", classTable, collTable,  "Collection to Class Saada join"});
		Table_Tap_Schema_Key_Columns.addSaadaJoin(classTable);
	}

	/**
	 * Make a fromTable <> targetTable join on fromKey/targetKey.
	 * @param fromTable
	 * @param targetTable
	 * @param fromKey
	 * @param targetKey
	 * @throws AbortException
	 */
	public static void addSaadaJoin(String fromTable, String targetTable, String fromKey, String targetKey) throws AbortException {
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?)"
				, new Object[]{fromTable, fromTable, targetTable , "Standard join"});
		Table_Tap_Schema_Key_Columns.addSaadaJoin(fromTable,fromKey, targetKey );
	}

	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedTable(String table) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop key of  table " + table);
		SQLQuery sq = new SQLQuery();
		ResultSet rs = sq.run("SELECT key_id FROM " + tableName + " WHERE  from_table = '" + table + "' OR target_table = '" + table + "'" );
		while( rs.next() ) {
			Table_Tap_Schema_Key_Columns.dropPublishedKey(rs.getString(1));
		}
		sq.close();
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE  from_table = '" + table + "' OR target_table = '" + table + "'");		
	}

}
