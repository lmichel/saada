package saadadb.sqltable;

import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLTable;

/**
 * @author laurent
 * @version $Id@
 *
 */
public class Table_Tap_Schema_Columns extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;

	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("table_name"); ah.setNameattr("table_name"); ah.setType("VARCHAR"); ah.setComment("table name from TAP_SCHEMA_tables");
		attMap.put("table_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("column_name"); ah.setNameattr("column_name"); ah.setType("VARCHAR"); ah.setComment("column name");
		attMap.put("column_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("VARCHAR"); ah.setComment("brief description of column");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("unit"); ah.setNameattr("unit"); ah.setType("VARCHAR"); ah.setComment("unit in VO standard format");
		attMap.put("unit", ah);
		ah = new AttributeHandler();
		ah.setNameattr("ucd"); ah.setNameattr("ucd"); ah.setType("VARCHAR"); ah.setComment("UCD of column if any");
		attMap.put("ucd", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("VARCHAR"); ah.setComment("UTYPE of column if any");
		attMap.put("utype", ah);
		ah = new AttributeHandler();
		ah.setNameattr("datatype"); ah.setNameattr("datatype"); ah.setType("VARCHAR"); ah.setComment("ADQL datatype as in section 2.5");
		attMap.put("datatype", ah);
		ah = new AttributeHandler();
		ah.setNameattr("size"); ah.setNameattr("size"); ah.setType("VARCHAR"); ah.setComment("length of variable length datatypes");
		attMap.put("size", ah);
		ah = new AttributeHandler();
		ah.setNameattr("principal"); ah.setNameattr("principal"); ah.setType("INTEGER"); ah.setComment("a principal column; 1 means true, 0 means false");
		attMap.put("principal", ah);
		ah = new AttributeHandler();
		ah.setNameattr("indexed"); ah.setNameattr("indexed"); ah.setType("INTEGER"); ah.setComment("an indexed column; 1 means true, 0 means false");
		attMap.put("indexed", ah);
		ah = new AttributeHandler();
		ah = new AttributeHandler();ah.setNameattr("std"); ah.setNameattr("std"); ah.setType("INTEGER"); ah.setComment("a standard column; 1 means true, 0 means false");
		attMap.put("std", ah);	
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
		SQLTable.createTable("tap_schema_columns", sql, null, false);
	}

	public static void main(String[] args) throws SaadaException{
		Database.init("XIDResult");
		SQLTable.beginTransaction();
		createTable();
		SQLTable.commitTransaction();
	}

}
