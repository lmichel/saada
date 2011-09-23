/**
 * 
 */
package saadadb.sqltable;

import java.util.LinkedHashMap;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * @author laurent
 * @version $Id$
 */
public class Table_Tap_Schema_Key_Columns extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;

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
		SQLTable.createTable("tap_schema_keys_columns", sql, null, false);
	}


}
