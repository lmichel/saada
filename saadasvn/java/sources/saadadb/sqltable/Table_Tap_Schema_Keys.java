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
public class Table_Tap_Schema_Keys extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "tap_schema_keys";

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
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}

}
