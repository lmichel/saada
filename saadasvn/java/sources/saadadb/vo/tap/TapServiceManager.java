/**
 * 
 */
package saadadb.vo.tap;

import java.sql.SQLException;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Tap_Schema_Columns;
import saadadb.sqltable.Table_Tap_Schema_Key_Columns;
import saadadb.sqltable.Table_Tap_Schema_Keys;
import saadadb.sqltable.Table_Tap_Schema_Schemas;
import saadadb.sqltable.Table_Tap_Schema_Tables;
import saadadb.util.Messenger;

/**
 * @author laurent
 *
 */
public class TapServiceManager extends EntityManager {

	/**
	 * @param name
	 */
	public TapServiceManager(String name) {
		super(name);
	}

	/**
	 * 
	 */
	public TapServiceManager() {
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#create(saadadb.command.ArgsParser)
	 */
	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#create(saadadb.command.ArgsParser)
	 */
	@Override
	public void create(ArgsParser ap) throws SaadaException {
		try {
			int missingTable = serviceExists();
			if( missingTable < 0 ) {
				QueryException.throwNewException(SaadaException.CORRUPTED_DB, "There are some relics of a previous implementation of TAP service. Please run the remove command first");
			}
			else if( missingTable == 1 ) {
				QueryException.throwNewException(SaadaException.CORRUPTED_DB, "TAP service already exists. Please remove it first before to overide");
			}
			int detectedTables = 0;
			if( Database.getWrapper().tableExist(Table_Tap_Schema_Keys.tableName) ) detectedTables++;
			if( Database.getWrapper().tableExist(Table_Tap_Schema_Key_Columns.tableName) ) detectedTables++;
			if( Database.getWrapper().tableExist(Table_Tap_Schema_Columns.tableName) ) detectedTables++;
			if( Database.getWrapper().tableExist(Table_Tap_Schema_Tables.tableName) ) detectedTables++;
			if( Database.getWrapper().tableExist(Table_Tap_Schema_Schemas.tableName) ) detectedTables++;
			
			/*
			 * Create TAP_SCHEMA tables
			 */
			Table_Tap_Schema_Keys.createTable();
			Table_Tap_Schema_Key_Columns.createTable();
			Table_Tap_Schema_Columns.createTable();
			Table_Tap_Schema_Tables.createTable();
			Table_Tap_Schema_Schemas.createTable();
			/*
			 * Reference the TAP_SCHEMA itself in the service
			 */
			/*
			 * Reference TAPS_SCHEMA
			 */
			Table_Tap_Schema_Schemas.addSchema("TAP_SCHEMA"
					,  "Schema dedicated to TAP. It contains all information about available schemas, tables and columns" 
					,  null);
			/*
			 * Reference TAPS_SCHEMA tables
			 */
			Table_Tap_Schema_Tables.addTable("TAP_SCHEMA", Table_Tap_Schema_Schemas.tableName    , "Table of schemas referenced by the service", null);
			Table_Tap_Schema_Tables.addTable("TAP_SCHEMA", Table_Tap_Schema_Tables.tableName     , "Table of tables referenced by the service", null);
			Table_Tap_Schema_Tables.addTable("TAP_SCHEMA", Table_Tap_Schema_Columns.tableName    , "Table of table columns referenced by the service", null);
			Table_Tap_Schema_Tables.addTable("TAP_SCHEMA", Table_Tap_Schema_Keys.tableName       , "Table of foreign keys used the service", null);
			Table_Tap_Schema_Tables.addTable("TAP_SCHEMA", Table_Tap_Schema_Key_Columns.tableName, "Table of columns bound by foreign keys", null);
			/*
			 * Reference TAPS_SCHEMA table columns 
			 */
			Table_Tap_Schema_Columns.addTable(Table_Tap_Schema_Schemas.tableName    , Table_Tap_Schema_Schemas.attMap, true);
			Table_Tap_Schema_Columns.addTable(Table_Tap_Schema_Tables.tableName     , Table_Tap_Schema_Tables.attMap, true);
			Table_Tap_Schema_Columns.addTable(Table_Tap_Schema_Columns.tableName    , Table_Tap_Schema_Columns.attMap, true);
			Table_Tap_Schema_Columns.addTable(Table_Tap_Schema_Keys.tableName       , Table_Tap_Schema_Keys.attMap, true);
			Table_Tap_Schema_Columns.addTable(Table_Tap_Schema_Key_Columns.tableName, Table_Tap_Schema_Key_Columns.attMap, true);


		} catch (SaadaException e) {
			e.printStackTrace();
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
		catch (Exception e) {
			Messenger.printStackTrace(e);
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}

	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#empty(saadadb.command.ArgsParser)
	 */
	@Override
	public void empty(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#remove(saadadb.command.ArgsParser)
	 */
	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try {
			int missingTable = serviceExists();
			if( missingTable == 0 ) {
				QueryException.throwNewException(SaadaException.CORRUPTED_DB, "No Tap service detected");
			}
			Table_Tap_Schema_Keys.dropTable();
			Table_Tap_Schema_Key_Columns.dropTable();
			Table_Tap_Schema_Columns.dropTable();
			Table_Tap_Schema_Tables.dropTable();
			Table_Tap_Schema_Schemas.dropTable();
		} catch (Exception e) {
			QueryException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#populate(saadadb.command.ArgsParser)
	 */
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#index(saadadb.command.ArgsParser)
	 */
	@Override
	public void index(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#comment(saadadb.command.ArgsParser)
	 */
	@Override
	public void comment(ArgsParser ap) throws SaadaException {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Check the status of the TAP service
	 * @return 1 if all TAP_SCHEMA tables are here, 0 if no table is here or minus the number of missing tables
	 * @throws FatalException
	 * @throws Exception
	 */
	public static int serviceExists() throws FatalException, Exception {
		int missingTables = 0;
		if( !Database.getWrapper().tableExist(Table_Tap_Schema_Keys.tableName) ) missingTables++;
		if( !Database.getWrapper().tableExist(Table_Tap_Schema_Key_Columns.tableName) ) missingTables++;
		if( !Database.getWrapper().tableExist(Table_Tap_Schema_Columns.tableName) ) missingTables++;
		if( !Database.getWrapper().tableExist(Table_Tap_Schema_Tables.tableName) ) missingTables++;
		if( !Database.getWrapper().tableExist(Table_Tap_Schema_Schemas.tableName) ) missingTables++;
		if( missingTables == 0 ) {
			return 1;
		}
		else if( missingTables == 5 ) {
			return 0;
		}
		else {
			return -missingTables;
		}

	}

}
