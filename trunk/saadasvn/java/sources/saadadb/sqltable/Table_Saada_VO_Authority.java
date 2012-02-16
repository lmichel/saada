package saadadb.sqltable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import saadadb.database.Database;
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
				StringBuilder bfn = new StringBuilder(f.getName());
				String mn = "get" + (bfn.replace(0, 1, fn.substring(0,1).toUpperCase())).toString();
				Method meth = c.getMethod(mn);
				insertValues.add( Merger.quoteString((String)meth.invoke(authority)));
			}
		}

		SQLTable.createTable("saadadb_vo_authority"
				,createStmt
				, null
				, false);
		SQLTable.addQueryToTransaction(
				Database.getWrapper().getInsertStatement("saadadb_vo_authority"
						, insertColumns.toArray(new String[0])
						, insertValues.toArray(new String[0]))
		        ,  "saadadb_vo_authority");
	}
	
//	public static  void loadTable(Authority authority) throws Exception {
//		SQLQuery..
//	}


}

