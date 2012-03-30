package saadadb.sqltable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.util.Merger;
import saadadb.vo.registry.Authority;
import saadadb.vo.registry.Capability;


public class Table_Saada_VO_Capabilities extends SQLTable {
	public static final String  tableName = "saadadb_vo_capability";
	
	/**
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public static  void createTable() throws Exception {
		if( !SQLTable.tableExist(tableName)) {
			Class c = Class.forName("saadadb.vo.registry.Capabilities");
			String createStmt = "";
			for( Field f: c.getDeclaredFields()) {
				if( f.getModifiers() == Modifier.PRIVATE) {
					if( createStmt.length() > 0 ) {
						createStmt +=  " ,";
					}
					String fn =  f.getName();
					createStmt += fn + " text";
				}
			}
			/*
			 * This operation is hidden for the user. That is why the transaction
			 * is completed at this level
			 */
			SQLTable.beginTransaction();
			SQLTable.createTable(tableName
					,createStmt
					, null
					, false);
			SQLTable.commitTransaction();
		}
	}

	/**
	 * @param capability
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  void addCapability(Capability capability) throws Exception {
		createTable();
		Class c = capability.getClass();
		ArrayList<String> insertColumns = new ArrayList<String>();
		ArrayList<String> insertValues = new ArrayList<String>();
		for( Field f: c.getDeclaredFields()) {
			if( f.getModifiers() == Modifier.PRIVATE) {
				String fn =  f.getName();
				insertColumns.add(fn);
				StringBuilder bfn = new StringBuilder(fn);
				String mn = "get" + (bfn.replace(0, 1, fn.substring(0,1).toUpperCase())).toString();
				Method meth = c.getMethod(mn);
				insertValues.add( Merger.quoteString(
						Database.getWrapper().getEscapeQuote((String)meth.invoke(capability)))
				);
			}
		}
		SQLTable.addQueryToTransaction(
				Database.getWrapper().getInsertStatement(tableName
						, insertColumns.toArray(new String[0])
						, insertValues.toArray(new String[0]))
						,  tableName);
	}

	/**
	 * @param capability
	 * @throws AbortException
	 * @throws FatalException
	 */
	public static void commentCapability(Capability capability) throws AbortException, FatalException{
		SQLTable.addQueryToTransaction("UPDATE " 
				+ tableName 
				+ " SET description = '" 
				+ Database.getWrapper().getEscapeQuote(capability.getDescription())
				+ "' WHERE datatreepath = '" + capability.getDataTreePath() 
				+ "' AND protocol = '" + capability.getProtocol() + "'");
	}
	/**
	 * @param capability
	 * @throws AbortException
	 * @throws FatalException
	 */
	public static void removeCapability(Capability capability) throws AbortException, FatalException{
		SQLTable.addQueryToTransaction("DELETE FROM " 
				+ tableName 
				+ "' WHERE datatreepath = '" + capability.getDataTreePath() 
				+ "' AND protocol = '" + capability.getProtocol() + "'");
	}
	/**
	 * @throws AbortException
	 */
	public static void emptyTable(String protocol) throws AbortException {
			SQLTable.addQueryToTransaction("DELETE FROM " + tableName 
					+ ((protocol != null && protocol.length()> 0 )? " WHERE protocol = '" + protocol + "'": ""));
	}
	/**
	 * @throws FatalException
	 */
	public static void removeTable() throws FatalException {
		SQLTable.addQueryToTransaction(Database.getWrapper().dropTable(tableName));
	}
	
	/**
	 * @param capabilities
	 * @param protocol
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static  void loadCapabilities(Collection<Capability> capabilities, String protocol) throws Exception {
		// avoid errors at first call
		createTable();
		Class c = Class.forName("saadadb.vo.registry.Capability");
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
		ResultSet rs = squery.run("SELECT * FROM " + tableName + " WHERE protocol = '" + protocol + "'");
		ResultSetMetaData rsm = rs.getMetaData();
		int colCount = rsm.getColumnCount();
		while( rs.next()) {
			Capability cap = new Capability();
			capabilities.add(cap);
			for( int i=1 ; i<=colCount ; i++ ) {
				String cn = rsm.getColumnName(i);
				setter.get(i-1).invoke(cap, rs.getString(cn));
			}
		}
		squery.close();
	}
}

