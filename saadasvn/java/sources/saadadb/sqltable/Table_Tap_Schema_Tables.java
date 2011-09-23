package saadadb.sqltable;

import java.util.LinkedHashMap;

import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * @author laurent
 * @version $Id@
 *
 */
public class Table_Tap_Schema_Tables extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;

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
		SQLTable.createTable("tap_schema_tables", sql, null, false);
	}

}
