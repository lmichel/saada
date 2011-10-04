/**
 * 
 */
package saadadb.sqltable;

import java.util.LinkedHashMap;

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
	public static final String tableName = "tap_schema_key_columns";

	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("key_id"); ah.setNameattr("key_id"); ah.setType("VARCHAR"); ah.setComment("unique key identifier");
		attMap.put("key_id", ah);
		ah = new AttributeHandler();
		ah.setNameattr("from_column"); ah.setNameattr("from_column"); ah.setType("VARCHAR"); ah.setComment("key column name in the from_table");
		attMap.put("from_column", ah);
		ah = new AttributeHandler();
		ah.setNameattr("target_column"); ah.setNameattr("target_column"); ah.setType("VARCHAR"); ah.setComment("key column name in the target_table");
		attMap.put("target_column", ah);
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
	 * @param ketId
	 * @throws AbortException
	 */
	public static void dropPublishedKey(int ketId) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop columns of  table " + tableName);
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE  key_id = " + ketId );		
	}
	
	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}


}
