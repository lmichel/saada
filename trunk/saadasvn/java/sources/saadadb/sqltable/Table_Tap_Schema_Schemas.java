/**
 * 
 */
package saadadb.sqltable;

import java.util.LinkedHashMap;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * @author laurent
 * @version $Id@
 */
public class Table_Tap_Schema_Schemas extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;

	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("schema_name"); ah.setNameattr("schema_name"); ah.setType("VARCHAR"); ah.setComment("schema name, possibly qualified");
		attMap.put("schema_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("VARCHAR"); ah.setComment("brief description of schema");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("VARCHAR"); ah.setComment("UTYPE if schema corresponds to a data model");
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
		SQLTable.createTable("tap_schema_schemas", sql, null, false);
	}

}
