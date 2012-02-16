package saadadb.sqltable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.util.Merger;
import saadadb.vo.registry.Authority;


public class Table_Saada_VO_Authority extends SQLTable {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static  void createTable(Authority authority) throws Exception {
		Authority a = Authority.getInstance();
		Class c = a.getClass();
		String createStmt = "";
		ArrayList<String> insertColumns = new ArrayList<String>();
		ArrayList<String> insertValues = new ArrayList<String>();
		for( Field f: c.getDeclaredFields()) {
			if( f.getModifiers() == Modifier.PRIVATE) {
				if( createStmt.length() > 0 ) {
					createStmt +=  " ,";
				}
				String fn =  f.getName();
				createStmt += fn + " text";
				insertColumns.add(fn);
				StringBuilder bfn = new StringBuilder(fn);
				String mn = "get" + (bfn.replace(0, 1, fn.substring(0,1).toUpperCase())).toString();
				Method meth = c.getMethod(mn);
				insertValues.add( Merger.quoteString(
						Database.getWrapper().getEscapeQuote((String)meth.invoke(authority)))
				);
			}
		}

		if( !SQLTable.tableExist("saadadb_vo_authority")) {
			SQLTable.createTable("saadadb_vo_authority"
					,createStmt
					, null
					, false);
		} else {
			emptyTable();
		}

		SQLTable.addQueryToTransaction(
				Database.getWrapper().getInsertStatement("saadadb_vo_authority"
						, insertColumns.toArray(new String[0])
						, insertValues.toArray(new String[0]))
						,  "saadadb_vo_authority");
	}

	public static void emptyTable() throws AbortException {
		SQLTable.addQueryToTransaction("DELETE FROM saadadb_vo_authority");
	}
	public static void removeTable() throws FatalException {
		SQLTable.addQueryToTransaction(Database.getWrapper().dropTable("saadadb_vo_authority"));
	}
	
	public static  void loadTable(Authority authority) throws Exception {
		Authority a = Authority.getInstance();
		Class c = a.getClass();
		ArrayList<String> columns = new ArrayList<String>();
		ArrayList<Method> setter = new ArrayList<Method>();
		Class[] params = new Class[1];
		params[0] = String.class;
		for( Field f: c.getDeclaredFields()) {
			if( f.getModifiers() == Modifier.PRIVATE) {
				String fn = f.getName();
				columns.add(fn);
				StringBuilder bfn = new StringBuilder(f.getName());
				setter.add( c.getMethod("set" + (bfn.replace(0, 1, fn.substring(0,1).toUpperCase())).toString(), params));
			}
		}

		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("SELECT * FROM saadadb_vo_authority");
		ResultSetMetaData rsm = rs.getMetaData();
		int colCount = rsm.getColumnCount();
		while( rs.next()) {
			for( int i=1 ; i<=colCount ; i++ ) {
				String cn = rsm.getColumnName(i);
				setter.get(i-1).invoke(authority, rs.getString(cn));
			}
		}
		squery.close();
	}
}

