/**
 * 
 */
package saadadb.sqltable;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id$
 */
public class Table_Tap_Schema_Key_Columns extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "key_columns";

	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("key_id"); ah.setNameattr("key_id"); ah.setType("String"); ah.setComment("unique key identifier");
		attMap.put("key_id", ah);
		ah = new AttributeHandler();
		ah.setNameattr("from_column"); ah.setNameattr("from_column"); ah.setType("String"); ah.setComment("key column name in the from_table");
		attMap.put("from_column", ah);
		ah = new AttributeHandler();
		ah.setNameattr("target_column"); ah.setNameattr("target_column"); ah.setType("String"); ah.setComment("key column name in the target_table");
		attMap.put("target_column", ah);
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
		SQLTable.createTable(tableName, sql, null, false);
	}
	
	/**
	 * @param ketId
	 * @throws AbortException
	 */
	public static void dropPublishedKey(String ketId) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop columns of  table " + tableName);
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE  key_id = '" + ketId + "'");		
	}
	
	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}

	/**
	 * Add 2 columns joined on oidsaada with keyId as key 
	 * @param keyId
	 * @throws AbortException
	 */
	public static void addSaadaJoin(String keyId) throws AbortException {
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?)"
				, new Object[]{keyId, "oidsaada", "oidsaada"});
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?)"
				, new Object[]{keyId + "_rev", "oidsaada", "oidsaada"});
	}

	/**
	 * Add column joined on from/target with keyId as key 
	 * @param keyId
	 * @param from
	 * @param target
	 * @throws AbortException
	 */
	public static void addSaadaJoin(String keyId, String from, String target) throws AbortException {
		SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?)"
				, new Object[]{keyId, from, target});
	}


}
