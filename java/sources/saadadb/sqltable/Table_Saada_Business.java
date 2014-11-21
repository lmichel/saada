package saadadb.sqltable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
public class Table_Saada_Business extends SQLTable {

	/**
	 * @param tableName
	 * @param cls
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public static void createBusinessTable(String tableName, Class cls) throws Exception{
		
		String sql = "oidsaada int8, obs_id " 
			+ Database.getWrapper().getIndexableTextType() 
			+ " NULL, md5keysaada " 
			+ Database.getWrapper().getIndexableTextType() 
			+ "  NULL ";
		List<Field> fld = ((SaadaInstance) cls.newInstance()).getClassLevelPersisentFields();
		for (Field f: fld) {
			String s = Database.getWrapper().getSQLTypeFromJava(f.getType().toString());
			sql += ",  " + f.getName() + "  ";
			if (!s.equals("")) {
				if (!s.equals("bit") || s.indexOf("char") >= 0) {
					sql += s + " NULL ";
				} else {
					sql += s; // Sybase
				}
			}
		}
		SQLTable.createTable(tableName, sql, "oidsaada", false);
	}


	/**
	 * Build a new business table matching the mapAh and having the same field order as the former one
	 * @param tableName
	 * @param cls
	 * @throws Exception
	 */
	public static void updateBusinessTable(String tableName, Map<String, AttributeHandler> mapAh) throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Update business table <" + tableName + ">");
		String tmpTable = "tmp_" +  tableName;
		/*
		 * Store existing columns in the old business table
		 */
		DatabaseConnection connection = Database.getConnection();
		ResultSet rs = Database.getWrapper().getTableColumns(connection, tableName);
		Set<String> colorg = new HashSet<String>();
		while( rs.next()) {
			colorg.add(rs.getString("COLUMN_NAME"));
		}
		Database.giveConnection(connection);
		/*
		 * Build the query creating the new table setting a NULL value for the column not existing
		 * in th old table.
		 */
		String sql = "oidsaada int8, obs_id " 
			+ Database.getWrapper().getIndexableTextType() 
			+ " NULL, md5keysaada " 
			+ Database.getWrapper().getIndexableTextType() 
			+ "  NULL ";
		for (AttributeHandler ah: mapAh.values()) {
			String s = Database.getWrapper().getSQLTypeFromJava(ah.getType().toString());
			sql += ",  " + ah.getNameattr() + "  " + s;
			}
		/*
		 * Do the job
		 * The strange transaction policy is due to SQLITE which refuses to modify the schema within a transaction.
		 */
		SQLTable.addQueryToTransaction("DROP TABLE IF EXISTS " + tmpTable ) ;
		SQLTable.createTable(tmpTable, sql, "oidsaada", false);
		 
		sql = "INSERT INTO " +  tmpTable + " SELECT oidsaada, obs_id, md5keysaada " ;
		for (AttributeHandler ah: mapAh.values()) {
			sql += ", " + ((colorg.contains(ah.getNameattr()))? ah.getNameattr(): "NULL");
		}
		sql += " FROM " + tableName;
		SQLTable.addQueryToTransaction(sql);
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		/*
		 * SQLITE cannot drop a table during a transaction containing others db updates.
		 * The taht is renamed. TIt must be dropped in a further atomic transaction
		 */
		SQLTable.addQueryToTransaction("DROP TABLE  " + tableName) ;
		SQLTable.commitTransaction();
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("ALTER TABLE " + tmpTable + " RENAME TO " + tableName) ;
		/*
		 * Index name in lower case to avoid case issues
		 */
		SQLTable.addQueryToTransaction("CREATE UNIQUE INDEX " + tableName.toLowerCase() + "_oidsaada ON " + tableName + "(oidsaada)") ;
		SQLTable.lockTables(Database.getWrapper().getUserTables(), null);
		SQLTable.addQueryToTransaction(Database.getWrapper().grantSelectToPublic(tableName));	
	}
}
