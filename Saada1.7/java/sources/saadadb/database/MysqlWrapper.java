package saadadb.database;


import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.configuration.RelationConf;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.newdatabase.NewSaadaDB;
import saadadb.newdatabase.NewSaadaDBTool;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 * 11/2013 : creatDB: grant access to reader@localhost no to reader
 */
public class MysqlWrapper extends DbmsWrapper {
	private final TreeSet<String> recorded_tmptbl = new TreeSet<String>();

	/** * @version $Id$

	 * @param server_or_driver
	 * @param port_or_url
	 * @throws ClassNotFoundException
	 */
	private MysqlWrapper(String server_or_driver, String port_or_url) throws ClassNotFoundException {
		super(false);
		test_base = "testbasededonnees";
		test_table = "TableTest";
		if( server_or_driver.startsWith("com.mysql.jdbc.Driver") ) {
			this.driver = server_or_driver;
			this.url = port_or_url;

		}
		else {
			driver = "com.mysql.jdbc.Driver";
			url = "jdbc:mysql:";
			Class.forName(driver);
			if( server_or_driver != null && server_or_driver.length() > 0 ) {
				url += "//" + server_or_driver ;
				if( port_or_url != null && port_or_url.length() > 0 ) {
					url += ":" + port_or_url;
				}
				url += "/";
			}
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getDBMS()
	 */
	public String getDBMS(){
		return "MySQL";
	}

	public static DbmsWrapper getWrapper(String server_or_url, String port_or_url) throws ClassNotFoundException {
		if( wrapper != null ) {
			return wrapper; 
		}
		return new MysqlWrapper(server_or_url, port_or_url);
	}


	@Override
	public void createDB(String dbname) throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Create database <" + dbname + "> at " + url);

		Connection admin_connection = getConnection(url +"mysql", admin.getName(), admin.getPassword());
		Statement stmt = admin_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)	;
		try {
			stmt.executeUpdate("CREATE DATABASE " + dbname );
			if( reader != null && !reader.getName().equals(admin.getName()))  {
				Messenger.printMsg(Messenger.TRACE, "Grant select for " +  reader.getName() + "@localhost" );
				stmt.executeUpdate("grant select on " +  dbname + ".* to " + reader.getName() + "@localhost" );
			}
			/*
			 * A second DB must be created in order to allow the reader to use temporary table without
			 * risk of database alteration
			 * http://dev.mysql.com/doc/refman/5.0/en/privileges-provided.html
			 */
			stmt.executeUpdate("CREATE DATABASE " + getTempodbName(dbname) );
			if( reader != null && !reader.getName().equals(admin.getName()))  {
				stmt.executeUpdate("grant SELECT, INSERT, UPDATE, DELETE, DROP, ALTER, CREATE, LOCK TABLES on " +  getTempodbName(dbname) + ".* to " + reader.getName()+ "@localhost" );
			}
			admin_connection.close();
		} catch(SQLException e) {
			Messenger.printStackTrace(e);
			if( admin_connection != null )
				admin_connection.close();
			FatalException.throwNewException(SaadaException.DB_ERROR, e.getMessage()) ;
		}
	}

	@Override
	public boolean dbExists(String repository, String dbname) {
		try {
			Connection admin_connection = DriverManager.getConnection(url + dbname, admin.getName(), admin.getPassword());
			admin_connection.close();
		} catch (SQLException e) {
			return false;
		}
		return true;
	}

	@Override
	public void dropDB(String repository, String dbname) throws SQLException {
		Messenger.printMsg(Messenger.TRACE, "Remove database <" + dbname + ">");
		Connection admin_connection = DriverManager.getConnection(url +"mysql", admin.getName(), admin.getPassword());
		Statement stmt = admin_connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)	;
		try {
			stmt.executeUpdate("DROP DATABASE " + dbname + "");
			stmt.executeUpdate("DROP DATABASE " + getTempodbName(dbname) + "");
			admin_connection.close();
		} catch(SQLException e) {
			admin_connection.close();
			throw new SQLException(e.getMessage());
		}
	}



	@Override
	public void cleanUp() throws SQLException {
		this.dropDB(null, test_base);
	}

	@Override
	public String getOptions() {
		/**
		 * This option allows Tomact to sleep a long time and to wake up
		 * in a good shape
		 */
		return "?autoReconnect=true";
	}


	@Override
	public String[] getStoreTable(String table_name, int ncols, String table_file) throws Exception {
		return  new String[] {
				//"LOCK TABLE " + table_name + " WRITE",
				"ALTER TABLE " + table_name + " DISABLE KEYS",
				"LOAD DATA INFILE '" + table_file.replaceAll("\\\\", "\\\\\\\\") + "' INTO TABLE "  +  table_name,
				"ALTER TABLE " + table_name + " ENABLE KEYS"};

	}


	@Override
	public String abortTransaction() {
		return "ROLLBACK";
	}

	@Override
	public String lockTable(String table) {
		//return "LOCK TABLE " + table + " WRITE";
		return "";
	}
	@Override
	public String grantSelectToPublic(String table_name) {
		/*
		 * MySQL does not support grant to public before 5.2
		 */
		if( this.reader != null ) {
			return "GRANT select ON TABLE " + table_name + " TO '" + this.reader.getName() + "'";
		}
		else {
			return "";
		}
	}
	@Override
	public String lockTables(String[] write_table, String[] read_table) throws Exception {
		String wt = "";
		if(write_table != null ) {
			for( int i=0 ; i<write_table.length ; i++) {
				String tn = write_table[i].trim();
				if( tn.length() == 0 /*||  can be an alias !tableExist(tn) */ ) {
					continue;
				}
				if( i > 0 ) {
					wt += ", ";
				}
				wt += tn + " WRITE";
			}
		}

		String rt = "";
		if(read_table != null ) {
			for( int i=0 ; i<read_table.length ; i++) {
				String tn = read_table[i].trim();
				if( tn.length() == 0 /* can be an alias || !tableExist(tn) */) {
					continue;
				}
				if( i > 0 ) {
					rt += ", ";
				}
				rt += tn + " READ";
			}
		}
		//		if( wt.length() > 0 && rt.length() > 0) {
		//			return "LOCK TABLES " + wt + ",  " + rt ;
		//		}
		//		else if( wt.length() > 0 && rt.length() == 0) {
		//			return "LOCK TABLES " + wt;
		//		}
		//		else if( wt.length() == 0 && rt.length() > 0) {
		//			return "LOCK TABLES " + rt;
		//		}
		//		else {
		//			return "";
		//		}
		return "";
	}

	@Override
	public String dropTable(String table) {
		return "DROP TABLE " + table;
	}

	/**
	 * On Mysql, text type can neither be indexed nor used as primary key. So varchar(255) is used despite of 
	 * the limitation length 
	 * @return
	 */
	@Override
	public String getIndexableTextType() {
		return "varchar(255) binary";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getBooleanValue(boolean)
	 */
	@Override
	public  String getBooleanAsString(boolean val){
		if( val ) {
			return "1";
		}
		else {
			return "0";
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getBooleanValue(java.lang.Object)
	 */
	public boolean getBooleanValue(Object rsval) {
		if( "1".equalsIgnoreCase(rsval.toString()) ) {
			return true;
		}
		else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getUpdateWithJoin(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String[], java.lang.String)
	 */
	@Override
	public String getUpdateWithJoin(String table_to_update, String table_to_join, String join_criteria, String key_alias, String[] keys, String[] values, String select_criteria) {
		String set_to_update  = "";
		String ka = "";
		if( key_alias != null && key_alias.length() > 0 ) {
			ka = key_alias + ".";
		}
		for( int i=0 ; i<keys.length ; i++ ) {
			if( i > 0 ) {
				set_to_update += ", ";
			}
			set_to_update += ka + keys[i] + " = " + values[i];
		}
		//e.g.: "UPDATE saada_metacoll_table  JOIN saada_metacoll_table as a ON a.name_coll = saada_metacoll_table.name_coll  SET saada_metacoll_table.ass_error = a.pk  WHERE a.name_attr = 'error_ra_csa'    AND saada_metacoll_table.name_attr = 'pos_ra_csa';
		return "UPDATE " + table_to_update + "  JOIN " +  table_to_join + " ON " + join_criteria  + " SET " + set_to_update  + " WHERE " + select_criteria;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getSQLTypeFromJava(java.lang.String)
	 */
	@Override
	public String getSQLTypeFromJava( String typeJava) throws FatalException {
		if(typeJava.equals("short")){
			return "int2";
		}
		else if(typeJava.equals("class java.lang.Long") || typeJava.equals("long")){
			return "int8";
		}
		else if(typeJava.equals("class java.lang.Integer") || typeJava.equals("int")){
			return "int4";
		}
		else if(typeJava.equals("class java.lang.Byte")){
			return "smallint";
		}
		else if(typeJava.equals("class java.lang.Character")){
			return "Character";
		}
		else if(typeJava.equals("char")){
			return "character(1)";
		}
		else if(typeJava.equals("boolean")){
			return "boolean";
		}
		else if(typeJava.equals("class java.lang.Double") || typeJava.equals("double")){
			return "double precision";
		}
		else if(typeJava.indexOf("String")>=0 || typeJava.equalsIgnoreCase("region") || typeJava.equalsIgnoreCase("clob")){
			return "varchar(255)";
		}
		else if(typeJava.indexOf("Date")>=0){
			return "date";
		}
		else if(typeJava.equals("float")){
			return "float4";
		}
		else if(typeJava.equals("byte") || typeJava.equals("class java.lang.Float")){
			return "smallint";
		}
		FatalException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Cannot convert " + typeJava + " JAVA type");
		return "";    	
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getSQLTypeFromJava(java.lang.String)
	 */
	@Override
	public String getJavaTypeFromSQL( String typeSQL) throws FatalException {
		if(typeSQL.equalsIgnoreCase("smallint") || typeSQL.equalsIgnoreCase("int2")){
			return "short";
		}
		else if(typeSQL.equalsIgnoreCase("int8") || typeSQL.equalsIgnoreCase("bigint")){
			return "long";
		}
		else if(typeSQL.equalsIgnoreCase("int")|| typeSQL.equalsIgnoreCase("int4")){
			return "int";
		}
		/*
		 * Mysql stores boolean as bytes: can be confusing but no workaround yet
		 */
		else if(typeSQL.equalsIgnoreCase("tinyint")){
			return "boolean";
		}
		else if(typeSQL.equalsIgnoreCase("char")|| typeSQL.equalsIgnoreCase("character")|| typeSQL.equalsIgnoreCase("character(1)")){
			return "char";
		}
		else if(typeSQL.equalsIgnoreCase("boolean")){
			return "boolean";
		}
		else if(typeSQL.equalsIgnoreCase("double precision")|| typeSQL.equalsIgnoreCase("float8")|| typeSQL.equalsIgnoreCase("double")
				|| typeSQL.equalsIgnoreCase("decimal") || typeSQL.equalsIgnoreCase("numeric")){
			return "double";
		}
		else if(typeSQL.equalsIgnoreCase("text")  || typeSQL.startsWith("character(")|| typeSQL.equalsIgnoreCase("varchar") || typeSQL.equalsIgnoreCase("varbinary") ){
			return "String";
		}
		else if(typeSQL.equalsIgnoreCase("date")){
			return "Date";
		}
		else if(typeSQL.equalsIgnoreCase("float") || typeSQL.equalsIgnoreCase("float4")){
			return "float";
		}
		else {
			return "String";			
		}
		//		FatalException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Cannot convert " + typeSQL + " SQL type");
		//		return "";    	
	}
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getUserTable()
	 */
	@Override
	public String[] getUserTables() {
		return new String[]{"mysql.user", "mysql.tables_priv"};
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#unlockTables()
	 */
	@Override
	public String unlockTables() {
		return "UNLOCK TABLES";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getRegexpOp()
	 */
	@Override
	public String getRegexpOp(){
		return "REGEXP";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getStrcatoP(java.lang.String, java.lang.String)
	 */
	public String getStrcatOp(String... args) {
		String retour = "";
		for (String arg : args) {
			if( retour.length() != 0 ) retour += "," ;
			retour += arg;
		}
		return  "CONCAT(" +retour + ")";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getQuotedEntity(java.lang.String)
	 */
	public String getQuotedEntity(String entity) {
		return "`" + entity + "`";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getInsertStaement(java.lang.String, java.lang.String)
	 */
	@Override
	public String getInsertStatement(String where, String[] fields, String[] values){
		String retour = "INSERT " + where + " SET ";
		for( int i=0 ; i<fields.length ; i++ ) {
			if( i > 0 ) {
				retour += ", ";
			}
			retour += fields[i] + " = " + values[i];
		}
		return retour;
	}



	@Override
	public void createRelationshipTable(RelationConf relation_conf) throws SaadaException {
		String sqlCreateTable = "";
		sqlCreateTable = " oidprimary  int8, oidsecondary int8, primaryclass int4 , secondaryclass int4";
		for( String q: relation_conf.getQualifier().keySet()) {
			sqlCreateTable = sqlCreateTable
			+ ","
			+ q
			+ "  double precision";
		}

		/*
		 * According to the Mysql release, create trigger requires or refuses a lock in a transaction.
		 */
		SQLTable.createTable(relation_conf.getNameRelation(),sqlCreateTable , null, true);
		SQLTable.addQueryToTransaction("CREATE TRIGGER " + relation_conf.getNameRelation() + "_secclass  \n"
				+ "BEFORE INSERT ON " + relation_conf.getNameRelation() +"\n"
				+ "FOR EACH ROW \n"
				+ "SET NEW.secondaryclass = ((new.oidsecondary>>32) & 65535), NEW.primaryclass = ((new.oidprimary>>32) & 65535)");
	}

	@Override
	public void suspendRelationTriggger(String relationName) throws AbortException {
		SQLTable.addQueryToTransaction("DROP TRIGGER " +  relationName + "_secclass");
	}

	@Override
	public void setClassColumns(String relationName) throws AbortException{
		SQLTable.addQueryToTransaction("UPDATE " + relationName + " SET primaryclass   = ((oidprimary>>32)  & 65535);");
		SQLTable.addQueryToTransaction("UPDATE " + relationName + " SET secondaryclass = ((oidsecondary>>32) & 65535);");

		SQLTable.addQueryToTransaction("CREATE TRIGGER " + relationName + "_secclass  \n"
				+ "BEFORE INSERT ON " + relationName + "\n"
				+ "FOR EACH ROW \n"
				+ "SET NEW.secondaryclass = ((new.oidsecondary>>32) & 65535), NEW.primaryclass = ((new.oidprimary>>32) & 65535)" );
	}

	@Override
	public String getSecondaryClassRelationshipIndex(String relationName) {
		return "CREATE INDEX " + relationName.toLowerCase()+ "_secoid_class ON "
		+ relationName + " ( secondaryclass )";
	}

	@Override
	public String getPrimaryClassRelationshipIndex(String relationName) {
		return "CREATE INDEX " + relationName.toLowerCase()+ "_primoid_class ON "
		+ relationName + " ( primaryclass )";
	}
	
	@Override
	public String [] getPrimaryRelationshipIndex(String relationName) {
		return new String[]{
				"ALTER TABLE " + relationName + " ROW_FORMAT=FIXED", 
				"CREATE INDEX " + relationName + "_oidprimary ON " + relationName + "(oidprimary)"
				};
	}

	@Override
	public String getPrimaryClassColumn(){
		return "primaryclass";
	}
	@Override
	public String getSecondaryClassColumn(){
		return "secondaryclass";
	}

	@Override
	public boolean tableExist(DatabaseConnection connection, String searched_table) throws Exception {
		String[] sc = searched_table.split("\\.");
		DatabaseMetaData dm= connection.getMetaData();;
		ResultSet rsTables;
		/*
		 * one field: Should be in the main databae
		 */
		if( sc.length == 1 ) {
			rsTables = dm.getTables(null, null, null, null);
			while (rsTables.next()) {
				String tableName = rsTables.getString("TABLE_NAME");
				if (searched_table.equalsIgnoreCase(tableName) 
						||  searched_table.equalsIgnoreCase(getQuotedEntity(tableName) ) ) {
					rsTables.close();
					return true;
				}
			}
		}
		/*
		 * Should be a temporary table
		 */
		else {
			rsTables = dm.getTables(sc[0], null, sc[1], null);
			while (rsTables.next()) {
				String tableName = rsTables.getString("TABLE_NAME");
				if (sc[1].equalsIgnoreCase(tableName)) {
					rsTables.close();
					return true;
				}
			}
		}
		rsTables.close();
		return false;
	}

	@Override
	public ResultSet getTableColumns(DatabaseConnection connection, String searched_table) throws Exception{
		if( !tableExist(connection, searched_table)) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Table <" + searched_table + "> does not exist");
			return null;
		}
		DatabaseMetaData dm = connection.getMetaData();
		ResultSet rsTables;
		String[] sc = searched_table.split("\\.");
		if( sc.length == 1 ) {
			rsTables = dm.getTables(null, null, searched_table, null);
			while (rsTables.next()) {
				String tableName = rsTables.getString("TABLE_NAME");
				if (searched_table.equalsIgnoreCase(tableName)) {
					return dm.getColumns(null, null, tableName,null);
				}
			}
			return null;
		}
		else {
			rsTables = dm.getTables(sc[0], null, sc[1], null);
			while (rsTables.next()) {
				String tableName = rsTables.getString("TABLE_NAME");
				if (sc[1].equalsIgnoreCase(tableName)) {
					return dm.getColumns(sc[0], null, sc[1],null);
				}
			}
			return null;			
		}
	}

	@Override
	public Map<String, String> getExistingIndex(DatabaseConnection connection, String searched_table) throws FatalException {
		try {
			if( !tableExist(connection, searched_table)) {
				Messenger.printMsg(Messenger.WARNING, "Table <" + searched_table + "> does not exist");
				return null;
			}
			DatabaseMetaData dm = connection.getMetaData();
			ResultSet resultat;
			String[] sc = searched_table.split("\\.");
			if( sc.length == 1 ) {
				resultat = dm.getIndexInfo(null, null, searched_table, false, false);
				HashMap<String, String> retour = new HashMap<String, String>();
				while (resultat.next()) {
					String col = resultat.getObject("COLUMN_NAME").toString();
					String iname = resultat.getObject("INDEX_NAME").toString();
					/*
					 * With mysql, PRIMARY constraints are stored as index but can not be removed.
					 * So we prefer hide them
					 */
					if( iname != null && col != null /*&& !iname.equals("PRIMARY")*/ ) {
						retour.put(iname.toString(), col);
					}
				}
				resultat.close();
				return retour;
			} else {
				resultat = dm.getIndexInfo(sc[0], null, sc[1], false, false);
				HashMap<String, String> retour = new HashMap<String, String>();
				while (resultat.next()) {
					String col = resultat.getObject("COLUMN_NAME").toString();
					String iname = resultat.getObject("INDEX_NAME").toString();
					/*
					 * With mysql, PRIMARY constraints are stored as index but can not be removed.
					 * So we prefer hide them
					 */
					if( iname != null && col != null /*&& !iname.equals("PRIMARY") */) {
						retour.put(iname.toString(), col);
					}
				}
				resultat.close();
				return retour;				
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			return null;
		}
	}

	@Override
	public String getExceptStatement(String key) {
		return " WHERE " + key + " NOT IN ";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getNullLeftJoinSelect(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getNullLeftJoinSelect(String table1, String key1, String table2, String key2) throws FatalException {		
		return "SELECT  " + key1 + " FROM " + table1 + " l"
		+ " LEFT JOIN " + table2 + " r"
		+ " ON l." + key1 + " = r." + key2 
		+ " WHERE r." + key2 + " IS NULL";
	}

	@Override
	public String getDropIndexStatement(String table_name, String index_name) {
		return "DROP INDEX " + index_name + " ON " + table_name;
	}
	@Override
	public  String getCollectionTableName(String coll_name, int cat) throws FatalException {
		return coll_name + "_" + Category.explain(cat).toLowerCase();
	}
	@Override
	public  String getGlobalAlias(String alias) {
		//Mysql wants no alias after USING(oidsaada)
		return "";
	}
	@Override
	public  String castToString(String token){
		//return "CAST(" + token + " AS VARCHAR)";
		return token ;
	}

	@Override
	public String getTempodbName(String dbname) {
		return dbname + "_tempo";
	}
	@Override
	public String getTempoTableName(String table_name) throws FatalException{
		return Database.getTempodbName() + "." + table_name;
	}

	@Override
	public String getCreateTempoTable(String table_name, String fmt) throws FatalException{
		recorded_tmptbl.add(table_name);
		return "CREATE TABLE " + Database.getTempodbName() + "." + table_name + " " + fmt;
	}

	@Override
	public String getDropTempoTable(String table_name) throws FatalException{
		return "DROP TABLE IF EXISTS " + Database.getTempodbName() + "." + table_name ;
	}
	@Override
	public  String[] changeColumnType(DatabaseConnection connection, String table, String column, String type) {
		return new String[] {"ALTER TABLE " + table + " MODIFY  " + column + " " + type};

	}
	@Override
	public  String[] addColumn(String table, String column, String type) {
		return new String[] {"ALTER TABLE " + table + " ADD  COLUMN " + column + " " + type};
	}
	@Override
	public  String renameColumn(String table, String column, String newName) throws Exception {
		/*
		 * MySQL requires to give the type of the column even if it is the same.
		 */
		SQLQuery q = new SQLQuery("select * from " + table + " limit 1");
		ResultSetMetaData rsmd = q.run().getMetaData();		
		String type = "";
		int NumOfCol = rsmd.getColumnCount();
		for(int i=1;i<=NumOfCol;i++) {
			if( rsmd.getColumnName(i).equalsIgnoreCase(column)) {
				type = rsmd.getColumnTypeName(i);
			}
		}
		q.close();
		return "ALTER TABLE " + table + " CHANGE  " + column + " " + newName + " " + type;
	}

	@Override
	public String renameTable(String table, String newName) {
		return "RENAME TABLE " + table + " TO " + newName;
	}

	@Override
	public Set<String> getReferencedTempTable() {
		return recorded_tmptbl;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getProcBaseRef()
	 */
	@Override
	protected File getProcBaseRef() throws Exception {
		String base_dir = System.getProperty("user.home") 
		+ Database.getSepar() 
		+ "workspace" 
		+ Database.getSepar() 
		+ "Saada1.7"
		+ Database.getSepar() 
		+ "sqlprocs" 
		+ Database.getSepar() 
		+ "mysql" ;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Look for SQL procs in " + base_dir);
		File bf = new File(base_dir) ;
		/*
		 * Try first to look in the ECLIPSE workspace for dev. convenience
		 */
		if( !(bf.exists() && bf.isDirectory()) ) {
			base_dir = Database.getRoot_dir()
			+ Database.getSepar() 
			+ "sqlprocs" 
			+ Database.getSepar() 
			+ "mysql" ;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Look for SQL procs in " + base_dir);
			bf = new File(base_dir) ;
			if( !(bf.exists() && bf.isDirectory()) ) {
				base_dir = NewSaadaDB.SAADA_HOME
				+ Database.getSepar() 
				+ "dbtemplate" 
				+ Database.getSepar() 
				+ "sqlprocs" 
				+ Database.getSepar() 
				+ "mysql" ;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Look for SQL procs in " + base_dir);
				Messenger.printMsg(Messenger.TRACE, "No SQL procedure found try SAADA install dir " + base_dir);
				bf = new File(base_dir) ;
				if( !(bf.exists() && bf.isDirectory()) ) {		
					base_dir = NewSaadaDBTool.saada_home
					+ Database.getSepar() 
					+ "dbtemplate" 
					+ Database.getSepar() 
					+ "sqlprocs" 
					+ Database.getSepar() 
					+ "mysql" ;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Look for SQL procs in " + base_dir);
					Messenger.printMsg(Messenger.TRACE, "No SQL procedure found try SAADA install dir " + base_dir);
					bf = new File(base_dir) ;
					if( !(bf.exists() && bf.isDirectory()) ) {		
						FatalException.throwNewException(SaadaException.FILE_ACCESS, "Can not access SQL procedure directory");
					}
				}
			}
		}
		return bf;

	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#removeProc()
	 */
	@Override
	protected String[] removeProc() throws Exception {
		SQLQuery sq = new SQLQuery("SHOW FUNCTION STATUS WHERE Db = '" + Database.getConnector().getDbname() + "'");
		ResultSet rs = sq.run();
		ArrayList<String> retour = new ArrayList<String>();
		while( rs.next() ) {
			retour.add("DROP FUNCTION "+ rs.getString("Db") + "." + rs.getString("Name"));
		}
		sq.close();
		return retour.toArray(new String[0]);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#removeProc(java.sql.Connection, java.lang.String)
	 */
	@Override
	public String[]  removeProc(Connection connection, String dbname) throws Exception {
		Statement stmt = connection.createStatement(this.getDefaultScrollMode(), this.getDefaultConcurentMode());
		ResultSet rs= stmt.executeQuery("SHOW FUNCTION STATUS WHERE Db = '" + dbname + "'");
		ArrayList<String> retour = new ArrayList<String>();
		while( rs.next() ) {
			retour.add("DROP FUNCTION "+ rs.getString("Db") + "." + rs.getString("Name"));
		}
		stmt.close();
		return retour.toArray(new String[0]);
	
	
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getConditionHelp()
	 */
	public Map<String, String> getConditionHelp(){
		Map<String, String> helpItems = new LinkedHashMap<String, String>();
		helpItems.put("- Join Operator Templates -", "");
		helpItems.put("Partial comparison of names", "substr(p.namesaada, 1, 5) = substr(s.namesaada, 1, 5) ");
		helpItems.put("Row number equality"        , "(p.oidsaada >> 32) = (s.oidsaada >> 32)");
		helpItems.put("Regular expression op", "p.namesaada " + this.getRegexpOp() + " 'RegExp'");
		helpItems.put("Same sky pixel", "p.sky_pixel_csa = s.sky_pixel_csa");
		return helpItems;
	}

	public static void main(String[] args) {
		try {
			ArgsParser ap = new ArgsParser(args);
			Messenger.debug_mode = true;
			DbmsWrapper dbmswrapper = MysqlWrapper.getWrapper("localhost", ""); 
			dbmswrapper.setAdminAuth("saadmin", ap.getPassword());
			dbmswrapper.checkAdminPrivileges("/tmp", false);
			dbmswrapper.setReaderAuth("reader", "");
			dbmswrapper.checkReaderPrivileges();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.err.println(e.getMessage());
		}
	}


}
