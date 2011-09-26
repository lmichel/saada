/**
 * 
 */
package saadadb.vo.tap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLQuery;
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
	public final static Set<String> ignoreCollAttrs = new LinkedHashSet<String>();
	

	static {
			ignoreCollAttrs.add("date_load");
			ignoreCollAttrs.add("product_url_csa");
			ignoreCollAttrs.add("sky_pixel_csa");
			ignoreCollAttrs.add("oidtable");
			ignoreCollAttrs.add("shape_csa");
			ignoreCollAttrs.add("date_load");
			ignoreCollAttrs.add("y_colname_csa");
			ignoreCollAttrs.add("x_colname_csa");
			ignoreCollAttrs.add("y_min_csa");
			ignoreCollAttrs.add("y_max_csa");
			ignoreCollAttrs.add("y_max_csa");
		}

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
	@SuppressWarnings("static-access")
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
		String classe = ap.getClassName();
		String description = ap.getComment();
		MetaClass mc = Database.getCachemeta().getClass(classe);
		String collection = mc.getCollection_name();
		if( Table_Tap_Schema_Tables.knowsTable(classe)) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Table " + classe + " alreday published in the TAP service");
		}
		if( !Table_Tap_Schema_Schemas.knowsSchema(collection) ) {
			Table_Tap_Schema_Schemas.addSchema(collection, "Schema matching the Saada collection " + collection, null);
		}
		Table_Tap_Schema_Tables.addTable(collection, classe, description, null);
		
		Collection<AttributeHandler> coll = Database.getCachemeta().getCollection(mc.getCollection_name()).getAttribute_handlers(mc.getCategory()).values();
		LinkedHashMap<String, AttributeHandler> mah = new LinkedHashMap<String, AttributeHandler>();
		for(AttributeHandler ah : coll) {
			String na = ah.getNameattr();
			if( !ignoreCollAttrs.contains(na)) {
				mah.put(na, ah);
			}
		}
		mah.putAll(mc.getAttributes_handlers());
		Table_Tap_Schema_Columns.addTable(classe, mah, false);
		} catch (SaadaException e) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}catch (Exception e2) {
			Messenger.printStackTrace(e2);
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, e2);
		}
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

	public static StringBuffer getXML() throws Exception{	
		StringBuffer retour = new StringBuffer("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\"\n" 
				+ "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
				+ "     xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">\n");

		/*
		 * Loop on schames
		 */
		SQLQuery qschema = new SQLQuery();
		ResultSet rs_schema = qschema.run("SELECT schema_name, description FROM " + Table_Tap_Schema_Schemas.tableName);
		while(rs_schema.next()) {
			String schema_name = rs_schema.getString(1);
			String schema_desc = rs_schema.getString(2);
			retour.append("<schema>\n");
			retour.append("    <name>" + schema_name + "</name>\n");
			if( schema_desc != null && schema_desc.length() > 0)			
				retour.append("    <description><![CDATA[" + schema_desc + "]]></description\n");
			/*
			 * Loop on tables
			 */
			SQLQuery qtables= new SQLQuery();
			ResultSet rs_tables = qtables.run("SELECT table_name, description, table_type FROM " + Table_Tap_Schema_Tables.tableName 
					+ " WHERE schema_name = '" + schema_name + "'");
			while(rs_tables.next()) {
				String table_name = rs_tables.getString(1);
				String table_desc = rs_tables.getString(2);
				String table_type = rs_tables.getString(3);
				retour.append("    <table>\n");
				retour.append("        <name>" + table_name + "</name>\n");
				if( table_desc != null && table_desc.length() > 0)			
					retour.append("        <description><![CDATA[" + table_desc + "]]></description\n");
				if( table_type != null && table_type.length() > 0)			
					retour.append("        <type>" + table_type + "</type\n");
				/*
				 * Loop on columns
				 */
				SQLQuery qcolumns= new SQLQuery();
				ResultSet rs_columns = qcolumns.run("SELECT * FROM " + Table_Tap_Schema_Columns.tableName 
						+ " WHERE table_name = '" + table_name + "'");
				while(rs_columns.next()) {
				    retour.append("        <columns>\n");
					retour.append("            <name>" + rs_columns.getString(2) + "</name>\n");
					retour.append("            <description>" + rs_columns.getString(3) + "</description>\n");
					Object v = rs_columns.getObject(4);
					if( v != null ) {
						retour.append("            <unit>" + v + "</unit>\n");					
					}
				    v = rs_columns.getObject(5);
				    if( v != null ) {
						retour.append("            <ucd>" + v + "</ucd>\n");					
					}
				    v = rs_columns.getObject(6);
				    if( v != null ) {
						retour.append("            <utype>" + v + "</utype>\n");					
					}
				    v = rs_columns.getObject(7);
				    if( v != null ) {
						retour.append("            <dataType xsi:type=\"vod:TAPType\">" + v );		
					    v = rs_columns.getObject(8);
					    if( v != null ) {
							retour.append("\n                <arraysize>" + v + "</arraysize>\n           ");		
					    }
						retour.append("</dataType>\n");		
	
					}
					retour.append("        </columns>\n");				
				}
				rs_columns.close();
				retour.append("    </table>\n");
			}
			rs_tables.close();
			retour.append("</schema>\n");
		}
		retour.append("</vosi:tableset>\n");
		rs_schema.close();
		return retour;
	}
	
	public static void main(String[] args) throws Exception {
		Database.init("XIDResult");
		System.out.println(TapServiceManager.getXML());
	}
}
