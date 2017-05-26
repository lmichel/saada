package saadadb.sqltable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class Table_Tap_Schema_Columns extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "columns";
	public static final Pattern sqlTypePattern = Pattern.compile("([A-Za-z]+)\\(([0-9]+)\\)");
	
	static {
		attMap = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah;
		ah = new AttributeHandler();
		ah.setNameattr("table_name"); ah.setNameattr("table_name"); ah.setType("String"); ah.setComment("table name from TAP_SCHEMA_tables");
		attMap.put("table_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("column_name"); ah.setNameattr("column_name"); ah.setType("String"); ah.setComment("column name");
		attMap.put("column_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("String"); ah.setComment("brief description of column");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("unit"); ah.setNameattr("unit"); ah.setType("String"); ah.setComment("unit in VO standard format");
		attMap.put("unit", ah);
		ah = new AttributeHandler();
		ah.setNameattr("ucd"); ah.setNameattr("ucd"); ah.setType("String"); ah.setComment("UCD of column if any");
		attMap.put("ucd", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("String"); ah.setComment("UTYPE of column if any");
		attMap.put("utype", ah);
		ah = new AttributeHandler();
		ah.setNameattr("datatype"); ah.setNameattr("datatype"); ah.setType("String"); ah.setComment("ADQL datatype as in section 2.5");
		attMap.put("datatype", ah);
		ah = new AttributeHandler();
		ah.setNameattr("size"); ah.setNameattr("size"); ah.setType("int"); ah.setComment("length of variable length datatypes");
		attMap.put("size", ah);
		ah = new AttributeHandler();
		ah.setNameattr("principal"); ah.setNameattr("principal"); ah.setType("int"); ah.setComment("a principal column; 1 means true, 0 means false");
		attMap.put("principal", ah);
		ah = new AttributeHandler();
		ah.setNameattr("indexed"); ah.setNameattr("indexed"); ah.setType("int"); ah.setComment("an indexed column; 1 means true, 0 means false");
		attMap.put("indexed", ah);
		ah = new AttributeHandler();
		ah = new AttributeHandler();ah.setNameattr("std"); ah.setNameattr("std"); ah.setType("int"); ah.setComment("a standard column; 1 means true, 0 means false");
		attMap.put("std", ah);	
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
	 * @throws AbortException
	 */
	public static void dropTable() throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + tableName);
		SQLTable.dropTable(tableName);
	}

	/**
	 * Stores the description of the columns of the table in tap_schema.columns.
	 * All columns are declared as principal as default ADQL queries return all columns
	 * of the queried table
	 * @param table     : name of the table whose columns are to be referenced
	 * @param attMap	: Map of attribute handler describing the columns
	 * @param standard  : Compliant with a DM or not
	 * @throws Exception
	 */
	public static void addTable(String schemaName, String table, Map<String, AttributeHandler> attMap, boolean standard) throws Exception {
		String fn = schemaName + "." + table;
		Messenger.printMsg(Messenger.TRACE, "Add columns of table " + fn + " to " + tableName);
		Map<String, String> mi = Database.getWrapper().getExistingIndex(table);
		Collection<String> colIndexed  = null;
		if( mi != null )colIndexed =  mi.values();
		for( AttributeHandler ah: attMap.values()) {
			String colName = ah.getNameattr();
			if( colName.startsWith("_") || colName.equalsIgnoreCase("size")){
				colName = "\"" + colName + "\"";
			}
			int indexed = 0;
			if( colIndexed != null ) for( String col : colIndexed ) {
				if( col.equals(colName) ) {
					indexed = 1;
					break;
				}
			}
			String type;
			int size = -1;
			/*
			 * Type can be either in SQL or in  Java.
			 * Let's try Java first
			 */
			try {
				type = Database.getWrapper().getADQLType(ah.getType() ) ;
				/*
				 * Conversion failed: must be a native SQL type
				 */
			} catch (Exception e) {
				type = ah.getType();
			}
			/*
			 * Extract type and size if needed
			 */
			Matcher m;
			if( (m = sqlTypePattern.matcher(type)).find() && m.groupCount() == 2) {
				type = m.group(1);
				size = Integer.parseInt(m.group(2));
			}
			SQLTable.addQueryToTransaction("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
					, new Object[]{fn, colName, ah.getComment() , ah.getUnit(), ah.getUcd(), ah.getUtype()
					             , type, size, indexed, 1, ((standard)?1: 0)});
		}
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedTable(String schemaName, String table) throws AbortException {
		String fn = schemaName + "." + table;
		Messenger.printMsg(Messenger.TRACE, "Drop columns of  table " + fn);
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE  table_name = '" + fn + "'");		
	}
	/**
	 * @param stable schema.table
	 * @throws AbortException
	 */
	public static void dropPublishedTable(String stable) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Drop columns of  table " + stable);
		SQLTable.addQueryToTransaction("DELETE FROM " + tableName + " WHERE  table_name = '" + stable + "'");		
	}

}
