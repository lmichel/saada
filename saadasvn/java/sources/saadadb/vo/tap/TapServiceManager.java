/**
 * 
 */
package saadadb.vo.tap;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import saadadb.admintool.utils.DataTreePath;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.command.EntityManager;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_VO_Capabilities;
import saadadb.sqltable.Table_Tap_Schema_Columns;
import saadadb.sqltable.Table_Tap_Schema_Key_Columns;
import saadadb.sqltable.Table_Tap_Schema_Keys;
import saadadb.sqltable.Table_Tap_Schema_Schemas;
import saadadb.sqltable.Table_Tap_Schema_Tables;
import saadadb.util.Messenger;
import saadadb.vo.registry.Capability;

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
	@Override
	public void create(ArgsParser ap) throws SaadaException {
		try {
			int missingTable = serviceExists();
			if( missingTable < 0 ) {
				FatalException.throwNewException(SaadaException.CORRUPTED_DB, "There are some relics of a previous implementation of TAP service. Please run the remove command first");
			}
			else if( missingTable == 1 ) {
				FatalException.throwNewException(SaadaException.WRONG_RESOURCE, "TAP service already exists. Please remove it first before to overide");
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
	}

	/**
	 * Removes all schemas except ivoa if all is false
	 * @throws Exception
	 */
	public void removeAllTables() throws Exception {
		int missingTable = serviceExists();
		if( missingTable == 0 ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No Tap service detected");
		}
		String[] schemas = Table_Tap_Schema_Schemas.getSchemaList();
		for( String schema: schemas) {
//			if( all || !schema.equalsIgnoreCase("ivoa") ) {
				this.remove(new ArgsParser(new String[]{"-remove=" + schema}));
	//		}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#remove(saadadb.command.ArgsParser)
	 */
	@Override
	public void remove(ArgsParser ap) throws SaadaException {
		try {
			String toRemove = ap.getRemove();
			if( toRemove.equalsIgnoreCase("service")) {
				int missingTable = serviceExists();
				if( missingTable == 0 ) {
					FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No Tap service detected");
				}
				Table_Tap_Schema_Keys.dropTable();
				Table_Tap_Schema_Key_Columns.dropTable();
				Table_Tap_Schema_Columns.dropTable();
				Table_Tap_Schema_Tables.dropTable();
				Table_Tap_Schema_Schemas.dropTable();
				Table_Saada_VO_Capabilities.emptyTable(Capability.TAP);
			}
			else {
				String[] sc = toRemove.split("\\.");
				/*
				 * One field: that is a request to unpublish a whole schema;
				 */
				if( sc.length == 1 ) {
					if( ! Table_Tap_Schema_Schemas.knowsSchema(sc[0]) ) {
						FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "There is not schema '" + sc[0] + "' published");			
					}
					Table_Tap_Schema_Schemas.dropPublishedSchema(sc[0]);
				}
				/*
				 * 2 fields: that is a request to unpublish a table within a schema;
				 */
				else if( sc.length == 2 ){
					Table_Tap_Schema_Tables.dropPublishedTable(sc[0], sc[1]);
				}
				else {
					FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "-empty must be like 'schema.table' or 'schema'");			
				}

			}
		} catch (SaadaException e) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}catch (Exception e2) {
			Messenger.printStackTrace(e2);
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, e2);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.command.EntityManager#populate(saadadb.command.ArgsParser)
	 */
	@SuppressWarnings("static-access")
	@Override
	public void populate(ArgsParser ap) throws SaadaException {
		try {
			int missingTable = serviceExists();
			if( missingTable < 0 ) {
				FatalException.throwNewException(SaadaException.CORRUPTED_DB, "There are some relics of a previous implementation of TAP service. Please run the remove and create again the service");
			}
			else if( missingTable == 0 ) {
				FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "TAP service does nor exist exists. Please create it first");
			}
			// parameters of the resource to be published
			String table = ap.getPopulate();
			String description = ap.getComment();
			String schema = null;
			/*
			 * A same table cannot be published twice
			 */
			if( Table_Tap_Schema_Tables.knowsTable(table)) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Table " + table + " already published in the TAP service");
			}

			LinkedHashMap<String, AttributeHandler> mah = new LinkedHashMap<String, AttributeHandler>();
			VOResource vor;
			/*
			 * If classe is a VO model, we just publish the SQL table in the "ivoa" schema
			 */
			if( (vor = VOResource.getResource(table)) != null) {
				Messenger.printMsg(Messenger.TRACE, table + " is a VO model");
				ArrayList<UTypeHandler> uths = vor.getUTypeHandlers();
				schema = "ivoa";
				for( UTypeHandler uth: uths) {
					AttributeHandler ah = uth.getAttributeHandler();
					ah.setNameattr(ah.getNameorg());
					mah.put(ah.getNameattr(), ah);
				}
			}
			/*
			 * If classe is a Saada class, we publish the join class/collection
			 */
			else if(Database.getCachemeta().classExists(table)) {
				Messenger.printMsg(Messenger.TRACE, table + " is a Saada class");
				MetaClass mc = Database.getCachemeta().getClass(table);
				schema = mc.getCollection_name();

//				Collection<AttributeHandler> coll = Database.getCachemeta().getCollection(mc.getCollection_name()).getAttribute_handlers(mc.getCategory()).values();
//				for(AttributeHandler ah : coll) {
//					String na = ah.getNameattr();
//					if( !ignoreCollAttrs.contains(na)) {
//						mah.put(na, ah);
//					}
//				}
				mah.putAll(mc.getAttributes_handlers());
			}
			else {
				int pos = table.lastIndexOf("_");
				if( pos == -1 ) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Table " + table + " is neither q class nor a DM, and it can not be a collection table");					
				}
				String coll = table.substring(0, pos);
				String cat = table.substring(pos + 1);
				int ncat = -1;
				try {
					ncat = Category.getCategory(cat);
				} catch (FatalException e) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Category " + cat + " does not exist");					
				}
				if( ! Database.getCachemeta().collectionExists(coll) ) {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Collection " + coll + " does not exist");					
				}
				Messenger.printMsg(Messenger.TRACE, table + " is a Saada collection");
				Collection<AttributeHandler> ahs = Database.getCachemeta().getCollection(coll).getAttribute_handlers(ncat).values();
				for(AttributeHandler ah : ahs) {
					String na = ah.getNameattr();
					if( !ignoreCollAttrs.contains(na)) {
						mah.put(na, ah);
					}
				}
				schema = coll;
			}
	
			/*
			 * TAP SCHEMA table update
			 */
			if( !Table_Tap_Schema_Schemas.knowsSchema(schema) ) {
				Table_Tap_Schema_Schemas.addSchema(schema, "Schema matching the Saada collection " + schema, null);
			}
			Table_Tap_Schema_Tables.addTable(schema, table, description, null);
			Table_Tap_Schema_Columns.addTable(table, mah, false);
		} catch (SaadaException e) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, e);
		}catch (Exception e2) {
			Messenger.printStackTrace(e2);
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, e2);
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

	/**
	 * Returns the XML description of the TAP resources
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer getXMLTables() throws Exception{	
		StringBuffer retour = new StringBuffer("<vosi:tableset xmlns:vosi=\"http://www.ivoa.net/xml/VOSITables/v1.0\"\n" 
				+ "     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n"
				+ "     xmlns:vod=\"http://www.ivoa.net/xml/VODataService/v1.1\">\n");

		/*
		 * Loop on schemas
		 */
		SQLQuery qschema = new SQLQuery();
		ResultSet rs_schema = qschema.run("SELECT schema_name, description FROM " + Table_Tap_Schema_Schemas.tableName);
		while(rs_schema.next()) {
			String schema_name = rs_schema.getString(1);
			String schema_desc = rs_schema.getString(2);
			retour.append("<schema>\n");
			retour.append("    <name>" + schema_name + "</name>\n");
			if( schema_desc != null && schema_desc.length() > 0)			
				retour.append("    <description><![CDATA[" + schema_desc + "]]></description>\n");
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
				retour .append(getXMLTable(table_name, table_desc, table_type));
			}
			rs_tables.close();
			retour.append("</schema>\n");
		}
		retour.append("</vosi:tableset>\n");
		rs_schema.close();
		return retour;
	}

	/**
	 * Returns the XML description of one table of the TAP service.
	 * Parameters are those read from one row of the TAP_SCHEMA_TABLES table
	 * @param table_name  
	 * @param table_desc
	 * @param table_type
	 * @return
	 * @throws Exception
	 */
	public static StringBuffer getXMLTable(String table_name, String table_desc, String table_type) throws Exception{	
		StringBuffer retour = new StringBuffer();
		retour.append("    <table>\n");
		retour.append("        <name>" + table_name + "</name>\n");
		if( table_desc != null && table_desc.length() > 0)			
			retour.append("        <description><![CDATA[" + table_desc + "]]></description>\n");
		if( table_type != null && table_type.length() > 0)			
			retour.append("        <type>" + table_type + "</type>\n");
		/*
		 * Loop on columns
		 */
		SQLQuery qcolumns= new SQLQuery();
		ResultSet rs_columns = qcolumns.run("SELECT * FROM " + Table_Tap_Schema_Columns.tableName 
				+ " WHERE table_name = '" + table_name + "'");
		while(rs_columns.next()) {
			retour.append("        <column>\n");
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
				Object v2 = rs_columns.getObject(8);
				if( v2 != null ) {
					retour.append("            <dataType xsi:type=\"vod:TAPType\" arraysize=\"" + v2 + "\">" + v );		
				} else {
					retour.append("            <dataType xsi:type=\"vod:TAPType\" >" + v );							
				}
				retour.append("</dataType>\n");		
			}
			retour.append("        </column>\n");				
		}
		rs_columns.close();
		retour.append("    </table>\n");
		return retour;
	}


	/**
	 * republish all TAP tables declared in the saada_vo_capabilities table.
	 * All previous table published are first removed except those of the ivoa schema.
	 * All schemas must be first removed and the transaction committed
	 * @throws Exception
	 */
	public void synchronizeWithGlobalCapabilities() throws Exception {
		int missingTable = serviceExists();
		if( missingTable < 0 ) {
			FatalException.throwNewException(SaadaException.CORRUPTED_DB, "There are some relics of a previous implementation of TAP service. Please run the remove and create again the service");
		}
		else if( missingTable == 0 ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "TAP service does nor exist exists. Please create it first");
		}
		/*
		 * republish all TAP tables refenced in the capability table 
		 */
		ArrayList<Capability> lc = new ArrayList<Capability>();
		Table_Saada_VO_Capabilities.loadCapabilities(lc, Capability.TAP);
		for( Capability cap: lc) {
			DataTreePath dataTreePath = cap.getDataTreePath();
			String collName = cap.getDataTreePath().collection;
			String catName = cap.getDataTreePath().category;
			String collTable = Database.getCachemeta().getCollectionTableName(collName, Category.getCategory(catName));
			String classTable = "";

			Messenger.printMsg(Messenger.TRACE, "Add " + cap.getDataTreePath() + " to TAP service");

			ArgsParser ap;
			if( dataTreePath.isCategoryLevel() ) {
				ap = new ArgsParser(
						new String[] {"-populate=" + collTable, "-comment=" + Database.getCachemeta().getCollection(collName).getDescription(), Messenger.getDebugParam()});
				/*
				 * Transaction are pushed at Manager level because the populate method do some DB ckecking with
				 * SELECT which are processed out of the current transaction ... sorry for that 
				 */
				SQLTable.beginTransaction();
				this.populate(ap);
				/*
				 * Add joins to subclasses are already recorded
				 */
				for( String sclass: Database.getCachemeta().getClassNames(catName, Category.getCategory(catName))) {			
					if( Table_Tap_Schema_Tables.knowsTable(sclass)) {
						Messenger.printMsg(Messenger.TRACE, "Add join" + collTable + " [X] " + classTable);
						Table_Tap_Schema_Keys.addSaadaJoin(collTable, sclass);					
					}
				}
				SQLTable.commitTransaction();
			}
			
			else if( dataTreePath.isClassLevel()) {
				classTable = dataTreePath.classe;
				ap = new ArgsParser(
						new String[] {"-populate=" + classTable, "-comment=" + cap.getDescription(), Messenger.getDebugParam()});
				SQLTable.beginTransaction();
				this.populate(ap);
				Messenger.printMsg(Messenger.TRACE, "Add join" + collTable + " [X] " + classTable);
				Table_Tap_Schema_Keys.addSaadaJoin(collTable, classTable);
				SQLTable.commitTransaction();
				/*
				 * Add join to collection table if it has been recorded
				 */
				if( Table_Tap_Schema_Tables.knowsTable(collTable) )  {
					Messenger.printMsg(Messenger.TRACE, "Add join" + collTable + " [X] " + classTable);
					Table_Tap_Schema_Keys.addSaadaJoin(collTable, classTable);					
				}
			}
		}
	}
}
