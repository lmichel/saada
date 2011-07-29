package saadadb.sqltable;

import java.lang.reflect.Field;

import saadadb.database.Database;

public class Table_Saada_Data extends SQLTable {

	/** * @version $Id$

	 * @param tableName
	 * @param cls
	 * @throws Exception
	 */
	public static void createBusinessTable(String tableName, Class cls) throws Exception{
		
		String sql = "oidsaada int8, namesaada " + Database.getWrapper().getIndexableTextType() + " NULL, md5keysaada " + Database.getWrapper().getIndexableTextType() + "  NULL ";
		Field fld[] = cls.getDeclaredFields();
		for (int i = 0; i < fld.length; i++) {
			String s = Database.getWrapper().getSQLTypeFromJava(fld[i].getType().toString());
			sql += ",  " + fld[i].getName() + "  ";
			if (!s.equals("")) {
				if (!s.equals("bit") || s.indexOf("char") >= 0) {
					sql += s + " NULL ";
				} else {
					sql += s; // Sybase
				}
			}
		}
		// @@@@@ lower
		SQLTable.createTable(tableName, sql, "oidsaada", false);
	}

}
