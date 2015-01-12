/**
 * 
 */
package saadadb.sqltable;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;

/**
 * @author laurent
 * @version $Id@
 */
public class Table_Tap_Schema_Schemas extends SQLTable {
	public static final LinkedHashMap<String, AttributeHandler> attMap;
	public static final String tableName = "schemas";
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
		ah.setNameattr("schema_name"); ah.setNameattr("schema_name"); ah.setType("String"); ah.setComment("schema name, possibly qualified");
		attMap.put("schema_name", ah);
		ah = new AttributeHandler();
		ah.setNameattr("description"); ah.setNameattr("description"); ah.setType("String"); ah.setComment("brief description of schema");
		attMap.put("description", ah);
		ah = new AttributeHandler();
		ah.setNameattr("utype"); ah.setNameattr("utype"); ah.setType("String"); ah.setComment("UTYPE if schema corresponds to a data model");
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
		SQLTable.createTable(qtableName, sql, "schema_name", false);
	}

	/**
	 * @throws AbortException
	 */
	public static void dropTable() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop table " + Database.getWrapper().encapsTableName(qtableName));
		SQLTable.dropTable(tableName);
	}

	/**
	 * @param schemaName
	 * @param description
	 * @param utype
	 * @throws AbortException
	 */
	public static void addSchema(String schemaName, String description, String utype) throws AbortException {
		Messenger.printMsg(Messenger.TRACE, "Add schema " + schemaName + " to " + qtableName);
		SQLTable.addQueryToTransaction("INSERT INTO " + qtableName + " VALUES (?, ?, ?)"
				, new Object[]{schemaName,description, utype});
	}

	/**
	 * @param schemaName
	 * @throws AbortException 
	 */
	public static void dropPublishedSchema(String schemaName) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Drop schema " + schemaName + " from " + qtableName);
		Table_Tap_Schema_Tables.dropPublishedSchema(schemaName);
		SQLTable.addQueryToTransaction("DELETE FROM " + qtableName + " WHERE schema_name = '" + schemaName + "'");		
	}

	/**
	 * Returns true if bale is already referenced in tap_schema_tables
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public static boolean knowsSchema(String schema) throws Exception {
		SQLQuery sq = new SQLQuery();
		ResultSet rs = sq.run("SELECT schema_name FROM " + qtableName + " WHERE schema_name = '" + schema + "' LIMIT 1");
		while (rs.next()) {
			sq.close();
			return true;
		}
		sq.close();
		return  false;
	}
	
	/**
	 * Return the list of published schemas
	 * @return
	 * @throws Exception
	 */
	public static String[] getSchemaList() throws Exception {
		SQLQuery sq = new SQLQuery();
		ArrayList<String> retour = new ArrayList<String>(); 
		ResultSet rs = sq.run("SELECT schema_name FROM " + qtableName);
		while (rs.next()) {
			retour.add(rs.getString(1));
		}
		sq.close();
		return  retour.toArray(new String[0]);
	}

}
