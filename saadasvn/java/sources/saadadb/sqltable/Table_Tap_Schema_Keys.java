/**
 * 
 */
package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
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
	public static final String qtableName;

	static {
		String tn  = null;
		try {
			tn = Database.getWrapper().getQuotedEntity(tableName);
		} catch (FatalException e) {
			Messenger.printStackTrace(e);
		}			
		qtableName = tn;

		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("key_id"); ah.setNameattr("key_id"); ah.setType("String"); ah.setComment("unique key identifie");
		attMap.put("key_id", ah);
		ah = new AttributeHandler();
		ah.setNameattr("from_table"); ah.setNameattr("from_table"); ah.setType("String"); ah.setComment("fully qualified table name");
		attMap.put("from_table", ah);
		ah = new AttributeHandler();
		ah.setNameattr("target_table"); ah.setNameattr("target_table"); ah.setType("String"); ah.setComment("fully qualified table name");
		attMap.put("target_table", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("String"); ah.setComment("description of this key");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("String"); ah.setComment("utype of this key");
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
		SQLTable.createTable(qtableName, sql, null, false);
	}

	/**
	 * Make a collection <> class join on oidsaada.
	 * 2 joins are created: classTable and callTable_rev
	 * @param collTable
	 * @param classTable
	 * @throws AbortException
	 */
	public static void addSaadaJoin(String collTable, String classTable) throws AbortException {
		SQLTable.addQueryToTransaction("INSERT INTO " + qtableName + " VALUES (?, ?, ?, ?, ?)"
				, new Object[]{classTable, collTable, classTable , "Collection to Class Saada join", "null"});
		SQLTable.addQueryToTransaction("INSERT INTO " + qtableName + " VALUES (?, ?, ?, ?, ?)"
				, new Object[]{classTable + "_rev", classTable, collTable,  "Collection to Class Saada join", "null"});
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
		SQLTable.addQueryToTransaction("INSERT INTO " + qtableName + " VALUES (?, ?, ?, ?, ?)"
				, new Object[]{fromTable, fromTable, targetTable , "Standard join", "null"});
		Table_Tap_Schema_Key_Columns.addSaadaJoin(fromTable,fromKey, targetKey );
	}

	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(qtableName);
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedTable(String table) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop key of  table " + table);
		SQLQuery sq = new SQLQuery();
		ResultSet rs = sq.run("SELECT key_id FROM " + qtableName + " WHERE  from_table = '" + table + "' OR target_table = '" + table + "'" );
		while( rs.next() ) {
			Table_Tap_Schema_Key_Columns.dropPublishedKey(rs.getString(1));
		}
		sq.close();
		SQLTable.addQueryToTransaction("DELETE FROM " + qtableName + " WHERE  from_table = '" + table + "' OR target_table = '" + table + "'");		
	}

}
