package saadadb.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.sqlite.SQLiteConfig;

import saadadb.collection.Category;
import saadadb.configuration.RelationConf;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLTable;
import saadadb.util.HardwareDescriptor;
import saadadb.util.Merger;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 *
 */
public class SQLiteWrapper extends DbmsWrapper {
	private static String db_file;
	private static boolean DEV_MODE = false;
	private static boolean EXT_LOADED = false;
	private final static String suffix = ".sqlitedb";
	private static final String driver_classname = "org.sqlite.JDBC";

	/** * @version $Id$

	 * @param server_or_driver
	 * @param port_or_url
	 * @throws ClassNotFoundException
	 */
	private SQLiteWrapper(String server_or_driver, String port_or_url) throws ClassNotFoundException {
		super(true);
		test_base =  "test" + suffix;
		test_table = "TableTest";
		if( server_or_driver.startsWith(driver_classname) ) {
			this.driver = server_or_driver;
			this.url = port_or_url;
			Class.forName(driver);
		}
		else {
			this.driver = driver_classname;
			this.url = "jdbc:sqlite:" + port_or_url;
			Class.forName(driver);
		}
		db_file = port_or_url.replace("jdbc:sqlite:", "");
		Messenger.printMsg(Messenger.TRACE, "Linked with SQLITE db file " + db_file + " by driver " + this.driver);
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getDBMS()
	 */
	public String getDBMS(){
		return "SQLite";
	}

	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getHardReaderConnectionLimit()
	 */
	public int getHardReaderConnectionLimit() {
		/*
		 * TODO  Checking if a multiple connection makes sense for SQLITE
		 */
		return 1;
	}

	/**
	 * @param server_or_url
	 * @param port_or_url
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static DbmsWrapper getWrapper(String server_or_url, String port_or_url) throws Exception {
		if( wrapper != null ) {
			return wrapper; 
		}
		SQLiteWrapper sw = new SQLiteWrapper(server_or_url, port_or_url);
		return sw;
	}

	/**
	 * returns the path of the native module containing extensions
	 * @throws Exception
	 */
	public static String getExtensionFilePath() throws Exception {
		if( DEV_MODE ) {
			/*
			 * Used in dev mode to avoid jar building at any time
			 * The C library is supposed to be named libSQLITEProc.so
			 */
			String root_dir = "/Users/laurentmichel/Documents/workspace/SQLITEProc/Debug/";
			//root_dir = "/home/michel/workspace/SQLITEProc/Debug/";
			root_dir  += HardwareDescriptor.getArchDependentLibName("libSQLITEProc");
			if( new File(root_dir).exists() ) {
				return root_dir;
			}
			else {
				return System.getProperty("user.home") + Database.getSepar() + HardwareDescriptor.getArchDependentLibName("libSQLITEProc");
			}
		}

		/*
		 * Used in prod mode: dynamic lib files are searched by the class loader in some jars.
		 */
		else {
			ClassLoader cl = SQLiteWrapper.class.getClassLoader();
			/*
			 * Link the application with the SQL procedures
			 */				
			String proc_libname  = HardwareDescriptor.getArchDependentLibName("libSQLITEProc");
			Messenger.printMsg(Messenger.TRACE, "Looking for native library " + proc_libname);
			InputStream in = cl.getResourceAsStream(proc_libname);
			if (in == null) {
				throw new Exception("library "+proc_libname+" not found (supposed to be in sqliteprocs.jar)");
			}
			File otmplib = File.createTempFile("sqliteprocjdbc-", ".lib");
			otmplib.deleteOnExit();
			OutputStream out = new FileOutputStream(otmplib);
			byte[] buf = new byte[1024];
			for (int len; (len = in.read(buf)) != -1;) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "UDF file extacted " + otmplib.length() + "b");
			return  otmplib.getAbsolutePath() ;

		}
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#dropTestDB(java.lang.String)
	 */
	protected void dropTestDB(String tmp_dir) throws SQLException {
		/*
		 * Drop the test DB if it exists
		 */
		File f = new File(tmp_dir + Database.getSepar() + test_base);
		if( f.exists() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "DB " + f.getAbsolutePath() + " already exists: drop it");
			f.delete();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#createTestDB(java.lang.String)
	 */
	protected void createTestDB(String tmp_dir) throws Exception {
		test_base = tmp_dir + Database.getSepar() +test_base;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Create SQLITE test DB " +  test_base);
		createDB(test_base);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#testExtraFunctions(java.sql.Statement)
	 */
	public boolean testExtraFunctions(Statement stmt) throws Exception{
		ResultSet rs = stmt.executeQuery("select sprintf('%3s', 'a'), sprintf('0x%03x', 5), sprintf('%08.4f', 233.35656)");
		while( rs.next() ){
			Messenger.printMsg(Messenger.TRACE, "sprintf seems to work: " + rs.getObject(1)  + " "+ rs.getObject(2)  + " " + rs.getObject(3));
		}
		rs.close();
		return true;
	}


	/**
	 * @return
	 * @throws SQLException 
	 */
	@Override
	public Connection getConnection(String url, String user, String password) throws Exception {
		/*
		 * The use SQLITEConfig is very specific to the JDBC driver 
		 * provided by http://www.xerial.org/trac/Xerial/wiki/SQLiteJDBC
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "No-User connecting " + url); 
		SQLiteConfig config = new SQLiteConfig();
		config.enableLoadExtension(true);	
		Connection conn =  DriverManager.getConnection(url, config.toProperties() );
//		/*
//		 * Extension must be loaded once: Tomcat fails at context change otherwise
//		 */
//		EXT_LOADED = false;
//		if( !EXT_LOADED ) { 
			SQLiteUDF.LoadProcedures(conn);
//			String efp = getExtensionFilePath();
//			if (Messenger.debug_mode)
//				Messenger.printMsg(Messenger.DEBUG, "Loading extensions from " + efp );
//
//			Statement stat = conn.createStatement();	
//			try {
//				//int res = stat.executeUpdate( "select load_extension('" + efp + "')");
//				ResultSet rs = stat.executeQuery("select load_extension('" + efp + "')");
//				while( rs.next() ){
//					for( int i=0 ; i<rs.getMetaData().getColumnCount() ; i++ ){
//						System.out.println(rs.getObject(i+1) + " " );
//					}
//					System.out.println("----------------");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				if( !EXT_LOADED ) {
//					//FatalException.throwNewException(SaadaException.DB_ERROR, e);
//				}
//				else {
//					Messenger.printMsg(Messenger.WARNING, "Can not load SQLITE extension, but they seem to be already here");
//				}
//			}
//			/*
//			 * SQLIt setup attempting to improve the performance with multicriteria queries
//			 */
//			stat.executeUpdate("pragma cache_size=200000");
//			stat.executeUpdate("pragma temp_store=FILE");
//
//			stat.close();
//			EXT_LOADED = false;
//		}
		return conn;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getJdbcURL(java.lang.String)
	 */
	@Override
	public String getJdbcURL(String repository,String dbname) {
		return this.getUrl() + repository + Database.getSepar() + "embeddeddb" + Database.getSepar() + dbname + suffix;
	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#closeLargeQueryConnection(java.sql.Connection)
	 */
	public void closeLargeQueryConnection(Connection largeConnection) throws SQLException{
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getDefaultScrollMode()
	 */
	@Override
	public int getDefaultScrollMode() {
		return ResultSet.TYPE_FORWARD_ONLY;
	}

	@Override
	public String abortTransaction() {
		return "ROLLBACK";
	}


	@Override
	public String castToString(String token) {
		return null;
	}

	@Override
	public String[] changeColumnType(DatabaseConnection connection,String table, String column, String type) throws Exception {
		Messenger.printMsg(Messenger.WARNING, "Changing column type may be long with SQLITE");
		DatabaseMetaData dm = connection.getMetaData();
		ResultSet resultat = dm.getColumns(null, null, table, null);
		String select = "";
		while( resultat.next() ) {
			if( select.length() > 0 ) {
				select += ", ";
			}
			String cn = resultat.getString("COLUMN_NAME");
			if( column.equals(cn) ) {
				select += "CAST(" + cn  + " AS " + type + ") AS " + column;
			}
			else {
				select += cn;
			}
		}
		resultat.close();
		return new String[]{this.getCreateTableFromSelectStatement("colchanger", "SELECT " + select + "  FROM " + table)
				, "DROP TABLE  " + table
				, "ALTER TABLE colchanger RENAME TO " + table};
	}

	@Override
	public String[] addColumn(String table, String column, String type) throws Exception {
		return new String[]{"ALTER TABLE " + table + " ADD  COLUMN " + column + " " + type};
	}


	@Override
	public String dropColumn(String table, String column)throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "SQLite can't drop a colmun table");
		return null;
	}

	@Override
	public  String renameColumn(String table, String column, String newName) throws QueryException {
		QueryException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "CSQLite can't drop a colmun table");
		return null;
	}

	@Override
	public void cleanUp() throws SQLException {

	}

	@Override
	public void createDB(String dbname) throws Exception {

	}

	@Override
	public void createRelationshipTable(RelationConf relation_conf)
	throws SaadaException {
		String sqlCreateTable = "";
		sqlCreateTable = " oidprimary  int8, oidsecondary int8, primaryclass int4 , secondaryclass int4";
		for( String q: relation_conf.getQualifier().keySet()) {
			sqlCreateTable = sqlCreateTable
			+ ","
			+ q
			+ "  double precision";
		}

		SQLTable.createTable(relation_conf.getNameRelation().toLowerCase(),sqlCreateTable , null, true);
		SQLTable.addQueryToTransaction("CREATE TRIGGER " + relation_conf.getNameRelation().toLowerCase() + "_secclass \n"
				+ "BEFORE INSERT ON " + relation_conf.getNameRelation().toLowerCase() + " FOR EACH ROW \n"
				+ "BEGIN \n"
				+ "  UPDATE  " + relation_conf.getNameRelation() + "\n"
				+ "  SET  secondaryclass = ((new.oidsecondary>>32) & 65535), primaryclass = ((new.oidprimary>>32) & 65535);\n"
				+ "END");

	}

	@Override
	public void suspendRelationTriggger(String relationName) throws AbortException {
		SQLTable.addQueryToTransaction("DROP TRIGGER  IF EXISTS " +  relationName + "_secclass");
	}

	@Override
	public void setClassColumns(String relationName) throws AbortException{
		SQLTable.addQueryToTransaction("UPDATE " + relationName + " SET primaryclass   = ((oidprimary>>32)  & 65535);");
		SQLTable.addQueryToTransaction("UPDATE " + relationName + " SET secondaryclass = ((oidsecondary>>32) & 65535);");

		SQLTable.addQueryToTransaction("CREATE TRIGGER " + relationName + "_secclass \n"
				+ "BEFORE INSERT ON " + relationName + " FOR EACH ROW \n"
				+ "BEGIN \n"
				+ "  UPDATE  " + relationName + "\n"
				+ "  SET  secondaryclass = ((new.oidsecondary>>32) & 65535), primaryclass = ((new.oidprimary>>32) & 65535);\n"
				+ "END");
	}

	@Override
	public boolean dbExists(String repository, String dbname) {
		String dfn;
		if( repository == null || dbname.startsWith(Database.getSepar()) ) {
			dfn = dbname + suffix;
		}
		else {
			dfn =  repository + Database.getSepar() + "embeddeddb" + Database.getSepar() + dbname + suffix;
		}
		return (new File(dfn)).exists();
	}

	@Override
	public void dropDB(String repository, String dbname) {
		if( dbExists(repository, dbname) ) {
			String dfn;
			if( repository == null || dbname.startsWith(Database.getSepar()) ) {
				dfn = dbname + suffix;
			}
			else {
				dfn =  repository + Database.getSepar() + "embeddeddb" + Database.getSepar() + dbname + suffix;
			}
			Messenger.printMsg(Messenger.TRACE, "Drop DB " + dbname);
			(new File(dfn)).delete();
		}
	}


	@Override
	public String dropTable(String table) {
		return "DROP TABLE " + table;
	}

	@Override
	public String getBooleanAsString(boolean val) {
		if( val ) {
			return "1";
		}
		else {
			return "0";
		}

	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getStrcatoP(java.lang.String, java.lang.String)
	 */
	public String getStrcatOp(String... args) {
		String retour = "";
		for (String arg : args) {
			if( retour.length() != 0 ) retour += "||" ;
			retour += arg;
			}
		return retour;
	}

	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getBooleanAsString(java.lang.Object)
	 */
	public boolean getBooleanValue(Object rsval) {
		if( "1".equalsIgnoreCase(rsval.toString()) ) {
			return true;
		}
		else {
			return false;
		}
	}


	@Override
	public String getCollectionTableName(String collName, int cat)
	throws FatalException {
		return collName + "_" + Category.explain(cat);
	}

	@Override
	public String getCreateTempoTable(String tableName, String fmt) throws FatalException {
		return "CREATE TEMPORARY TABLE " + tableName + " " + fmt;
	}


	@Override
	public String getCreateTableFromSelectStatement(String tablename, String select) {
		return "CREATE TABLE " + tablename + " AS " + select + "";
	}

	@Override
	public String getSelectWithExcept(String main_query, String key, String sec_query) throws FatalException {
		return main_query + " " 
		+ this.getExceptStatement(key) 
		+ " " + sec_query ;
	}
	@Override
	public String getDropIndexStatement(String tableName, String indexName) {
		return "DROP INDEX IF EXISTS " + indexName ;
	}


	@Override
	public boolean isIndexDroppable(String indexname) {
		if( indexname.startsWith("sqlite_autoindex")) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Index " + indexname + " can not be dropped");
			return false;
		}
		return true;
	}

	@Override
	public String getDropTempoTable(String tableName) throws FatalException {
		return "DROP TABLE IF EXISTS " + tableName;
	}

	@Override
	public String getExceptStatement(String key) {
		return " EXCEPT ";
	}

	@Override
	public Map<String, String> getExistingIndex(DatabaseConnection connection, String searched_table)
	throws FatalException {
		try {
			if( !tableExist(connection, searched_table)) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Table <" + searched_table + "> does not exist");
				return null;
			}
			HashMap<String, String> retour = new HashMap<String, String>();
			DatabaseMetaData dm = connection.getMetaData();
			ResultSet resultat =null;			
			/*
			 * When there is no index, SQLITE getIndexInfo genrates an unsane query which rise an error
			 * See JDBC3DatabaseMetaData l 1482
			 */
			try{
				resultat = dm.getIndexInfo(null, null, searched_table.toLowerCase(), false, false);
			} catch(Exception se){
				return retour;
			}
			while (resultat.next()) {
				String col = resultat.getObject("COLUMN_NAME").toString();
				String iname = resultat.getObject("INDEX_NAME").toString();
				if( iname != null && col != null ) {
					retour.put(iname.toString(), col);
				}
			}
			resultat.close();
			return retour;
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			AbortException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			return null;
		}
	}

	@Override
	public String getGlobalAlias(String alias) {
		return "AS " + alias;
	}

	@Override
	public String getIndexableTextType() {
		return "text";
	}

	@Override
	public String getInsertStatement(String where, String[] fields,
			String[] values) {
		return "INSERT INTO " + where + " (" + Merger.getMergedArray(fields) + ") VALUES (" + Merger.getMergedArray(values) + ")";
	}

	@Override
	public String getJavaTypeFromSQL(String typeSQL) throws FatalException {
		if(typeSQL.equals("int2")){
			return "short";
		}
		else if(typeSQL.equals("int8")){
			return "long";
		}
		else if(typeSQL.equals("int") || typeSQL.equals("int4")){
			return "int";
		}
		else if(typeSQL.equals("smallint")){
			return "byte";
		}
		else if(typeSQL.equals("character") || typeSQL.equals("character(1)") || typeSQL.equals("bpchar")){
			return "char";
		}
		else if(typeSQL.equals("bool")){
			return "boolean";
		}
		else if(typeSQL.equals("float8") || typeSQL.equals("numeric")){
			return "double";
		}
		else if(typeSQL.equals("text") || typeSQL.startsWith("character(")){
			return "String";
		}
		else if(typeSQL.equals("date")){
			return "Date";
		}
		else if(typeSQL.equals("float4")){
			return "float";
		}
		else {
			return "String";
		}
	}

	@Override
	public String getPrimaryClassColumn() {
		return "primaryclass";
	}

	@Override
	public String getPrimaryClassRelationshipIndex(String relationName) {
		return "CREATE INDEX " + relationName.toLowerCase()+ "_primoid_class ON "
		+ relationName + " ( primaryclass )";
	}

	@Override
	protected File getProcBaseRef() throws Exception {
		/*
		 * Procs are in native libs. No SQL file to read
		 */
		return null;
	}

	@Override
	public String getRegexpOp() {
		/*
		 * To be created in functions
		 */
		return "REGEXP";
	}


	@Override
	public String getSQLTypeFromJava(String typeJava) throws FatalException {
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
			return "float8";
		}
		else if(typeJava.indexOf("String")>=0){
			return "text";
		}
		else if(typeJava.indexOf("Date")>=0){
			return "date";
		}
		else if(typeJava.equals("float") || typeJava.equals("class java.lang.Float")){
			return "float4";
		}
		else if(typeJava.equals("byte")){
			return "smallint";
		}
		FatalException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Cannot convert " + typeJava + " JAVA type");
		return "";    	

	}

	@Override
	public String getSecondaryClassColumn() {
		return "secondaryclass";
	}

	@Override
	public String getSecondaryClassRelationshipIndex(String relationName) {
		return "CREATE INDEX " + relationName.toLowerCase()+ "_secoid_class ON "
		+ relationName + " ( secondaryclass )";
	}

	@Override
	public ResultSet getTableColumns(DatabaseConnection connection, String searched_table) throws Exception {
		if( !tableExist(connection,searched_table)) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Table <" + searched_table + "> does not exist");
			return null;
		}
		DatabaseMetaData dm = connection.getMetaData();
		ResultSet rsTables = dm.getTables(null, null, searched_table.toLowerCase(), null);
		while (rsTables.next()) {
			String tableName = rsTables.getString("TABLE_NAME");
			if (searched_table.equalsIgnoreCase(tableName.toLowerCase())) {
				rsTables.close();
				return dm.getColumns(null, null, tableName,null);
			}
		}
		rsTables.close();
		return null;
	}

	@Override
	public String getTempoTableName(String tableName) throws FatalException {
		return  tableName;
	}

	@Override
	public String getTempodbName(String dbname) {
		return dbname;
	}

	@Override
	public String getUpdateWithJoin(String tableToUpdate, String tableToJoin,
			String joinCriteria, String keyAlias, String[] keys,
			String[] values, String selectCriteria) {
		//		insert into datatopixels 
		//		select i.oidsaada, p.oidsaada, i.oidsaada >> 32, p.oidsaada >> 32, null, null, null, null, null 
		//		from datacube_image as i, pixtable_entry as p		
		//e.g.:  UPDATE saada_metacoll_table   SET ass_error = a.pk   FROM saada_metacoll_table a  WHERE a.name_coll = saada_metacoll_table.name_coll AND a.name_attr = 'error_ra_csa'    AND saada_metacoll_table.name_attr = 'pos_ra_csa';
		//return "UPDATE " + table_to_update  + " SET " + set_to_update  + " FROM " + table_to_join + " WHERE " + join_criteria + " AND " + select_criteria;
		/*		UPDATE customer_detail SET customer_id = (SELECT customer_id FROM
				> customers WHERE customers.customer_name = customer_detail.customer_name); 
		 */		return null;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getNullLeftJoinSelect(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getNullLeftJoinSelect(String leftTable, String leftKey, String rightTable, String rightKey) throws FatalException {		
		return "SELECT  " + leftTable + "." + leftKey + " FROM " + leftTable 
		+ " LEFT JOIN " + rightTable  
		+ " ON " + leftTable + "." + leftKey + " = " + rightTable + "." + rightKey 
		+ " WHERE " + rightTable + "." + rightKey + " IS NULL";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getNullLeftJoinDelete(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getNullLeftJoinDelete(String leftTable, String leftKey, String rightTable, String rightKey) throws FatalException {		
		return "DELETE  FROM " + leftTable 
				+ " WHERE  " + leftTable + "." + leftKey + " IN ("
				+ getNullLeftJoinSelect(leftTable, leftKey, rightTable, rightKey)
				+ ")";
	}

	@Override
	public String grantSelectToPublic(String table) {
		return "";
	}

	@Override
	public String lockTable(String table) {
		return "";
	}

	@Override
	public String lockTables(String[] writeTable, String[] readTable) {
		return "";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getInsertAutoincrementStatement()
	 */
	public  String getInsertAutoincrementStatement() {
		return "null ";
	}
	
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getSerialToken()
	 */
	public  String getSerialToken() {
		return "INTEGER";
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getEscapeQuote(java.lang.String)
	 */
	public   String getEscapeQuote(String val) {
		if( val == null ) {
			return null;
		}
		return val.replaceAll("'", "''");	
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#getAsciiNull()
	 */
	public String getAsciiNull() {
		return "null";
	}


	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#supportDropTableInTransaction()
	 */
	@Override
	public boolean supportDropTableInTransaction() {
		return false;
	}
	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#supportJDBCUpdate()
	 */
	public boolean supportJDBCUpdate() {
		return false;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#supportConcurrentAccess()
	 */
	public boolean supportConcurrentAccess(){
		return false;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#supportAlterColumn()
	 */
	@Override
	public boolean supportAlterColumn() {
		return true;
	}

//	@Override
//	public void storeTable(DatabaseConnection connection, String tableName, int ncols, String tableFile) throws Exception {
//		if (Messenger.debug_mode)
//			Messenger.printMsg(Messenger.DEBUG, "Loading ASCII  file " + tableFile + " in table " + tableName);
//		int nb_col=0;
//		if( ncols == -1 ) {
//			DatabaseMetaData meta = connection.getMetaData();
//			ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
//			/*
//			 * Only TYPE_FORWARD supported: must read all columns to get the size
//			 */
//			while( rsColumns.next() ) nb_col++;
//			rsColumns.close();
//		}
//		/*
//		 * If the table is temporary, we must use the given column number (JDBC XERIAL weakness?)
//		 */
//		else {
//			nb_col = ncols;
//		}
//		String ps = "insert into " + tableName.toLowerCase() + "  values (";
//		/*
//		 * Build the prepared statement
//		 */
//		for( int i=0 ; i< nb_col ; i++ ) {
//			if( i > 0 )  ps += ",";
//			ps += "?";
//		}
//		ps += ")";
//		PreparedStatement prep = connection.getPreparedStatement(ps);
//		/*
//		 * Maps file row in the prepared segment
//		 */
//		BufferedReader br = new BufferedReader(new FileReader(tableFile));
//		String str = "";
//		int line = 0;
//		while( (str = br.readLine()) != null ) {
//			line++;
//			String fs[] = str.split("\\t");
//			if( fs.length != nb_col ) {
//				QueryException.throwNewException(SaadaException.FILE_FORMAT, "Error at line " + line + " number of values (" + fs.length + ") does not match the number of columns (" +  nb_col + ")");
//			}
//			for( int i=0 ; i< nb_col; i++ ) {
//				if( "null".equals(fs[i]) )
//					prep.setObject(i+1, null);
//				else
//					prep.setObject(i+1, fs[i]);
//			}
//			prep.addBatch();
//			if( (line%5000) == 0  )  {
//				if (Messenger.debug_mode)
//					Messenger.printMsg(Messenger.DEBUG, "Store 5000 lines into the DB");
//				prep.executeBatch();
//			}
//		}
//		prep.close();
//		/*
//		 * Load data
//		 */
//		if (Messenger.debug_mode)
//			Messenger.printMsg(Messenger.DEBUG, line + " lines stored");
//		prep.executeBatch();
//		(new File(tableFile)).delete();
//		}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#storeTable(java.sql.Connection, java.lang.String, int, java.lang.String)
	 */
	@Override
	protected void storeTable(Connection connection, String tableName, int ncols, String tableFile) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Loading ASCII  file " + tableFile + " in table " + tableName);
		int nb_col=0;
		if( ncols == -1 ) {
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet rsColumns = meta.getColumns(null, null, tableName, null);
			/*
			 * Only TYPE_FORWARD supported: must read all columns to get the size
			 */
			while( rsColumns.next() ) nb_col++;
			rsColumns.close();
		}
		/*
		 * If the table is temporary, we must use the given column number (JDBC XERIAL weakness?)
		 */
		else {
			nb_col = ncols;
		}
		String ps = "insert into " + tableName.toLowerCase() + "  values (";
		/*
		 * Build the prepared statement
		 */
		for( int i=0 ; i< nb_col ; i++ ) {
			if( i > 0 )  ps += ",";
			ps += "?";
		}
		ps += ")";
		connection.setAutoCommit(false);
		PreparedStatement prep = connection.prepareStatement(ps);
		/*
		 * Maps file row in the prepared segment
		 */
		BufferedReader br = new BufferedReader(new FileReader(tableFile));
		String str = "";
		int line = 0;
		while( (str = br.readLine()) != null ) {
			line++;
			/*
			 * Adding a trailing \n in order to take in account lines ending with a \t
			 * split("+", "+A+B+C").length = 4
			 * split("+", "A+B+C+").length = 3
			 */
			str += "\n";
			String fs[] = str.split("\\t");
			int length = fs.length;
			if( length != nb_col ) {
				QueryException.throwNewException(SaadaException.FILE_FORMAT, "Error at line " + line + " number of values (" + length + ") does not match the number of columns (" +  nb_col + ")");
			}
			for( int i=0 ; i< nb_col; i++ ) {
				if( "null".equals(fs[i]) )
					prep.setObject(i+1, null);
				else
					prep.setObject(i+1, fs[i]);
			}
			prep.addBatch();
			if( (line%5000) == 0  )  {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Store 5000 lines into the DB");
				prep.executeBatch();
			}
		}
		br.close();
		prep.executeBatch();
		prep.close();
		/*
		 * Load data
		 */
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, line + " lines stored");
		prep.executeBatch();
		(new File(tableFile)).delete();
		}

	@Override
	public  String[] getStoreTable(String table_name, int ncols, String table_file) throws Exception {
		return null;
	}

	@Override
	public boolean tsvLoadNotSupported() {
		return true;
	}


	@Override
	public boolean tableExist(DatabaseConnection connexion, String searched_table) throws Exception {
		DatabaseMetaData dm = connexion.getMetaData();
		//ResultSet rsTables = dm.getTables(null, null, searched_table.toLowerCase(), null);
		ResultSet rsTables = dm.getTables(null, null, null, null);
		while (rsTables.next()) {
			String tableName = rsTables.getString("TABLE_NAME");
			if (searched_table.equalsIgnoreCase(tableName.toLowerCase()) 
					||  searched_table.equalsIgnoreCase(getQuotedEntity(tableName.toLowerCase()))) {
				rsTables.close();
				return true;
			}
		}
		rsTables.close();
		return false;
	}


	/**
	 * @param table table to be populated
	 * @param file   datafile (TSV)
	 * @param db_file database file
	 * @return
	 * @throws Exception
	 */
	public static int importTSV(String table, String file, String db_file) throws Exception {
		return importASCIIFile(table, file, "\t", db_file);
	}

	/**
	 * @param table table to be populated
	 * @param file datafile (TSV)
	 * @param separ field separator (one char string)
	 * @param db_file database file
	 * @return
	 * @throws Exception
	 */
	native static int importASCIIFile(String table, String file, String separ, String db_file) throws Exception;

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#supportAccount()
	 */
	public boolean supportAccount() {
		return false;
	}

	/* (non-Javadoc)
	 * @see saadadb.database.DbmsWrapper#setFetchSize(java.sql.Statement, int)
	 */
	public void setFetchSize(Statement stmt, int size) throws SQLException {
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
			
	    	Locale.setDefault(new Locale ("en", "US"));
			
			Messenger.debug_mode = true;
			DbmsWrapper dbmswrapper = SQLiteWrapper.getWrapper("", ""); 
//			Database.init("SEB");
//			Database.setAdminMode(null);
//			SQLTable.beginTransaction();
//			SQLTable.addQueryToTransaction("DELETE FROM EntCl_ConfigTable_1Entry");
//			SQLTable.commitTransaction();
//			System.out.println("COUOCU0 " + Spooler.getSpooler());
//			DatabaseConnection dbc = Database.getConnection();
//			System.out.println("COUOCU " + Spooler.getSpooler());
//			System.out.println("COUOCU2 " + Spooler.getSpooler());
//			Database.giveAdminConnection();
//			Database.getWrapper().storeTable(Database.getConnection(), "EntCl_ConfigTable_1Entry", -1, "/data/repository/SEB/tmp/bus1427300906129.psql");
//			Database.close();
//			System.exit(1);
			System.out.println(dbmswrapper.test_base);
			dbmswrapper.setAdminAuth("", "");
			System.out.println(dbmswrapper.test_base);
			dbmswrapper.checkAdminPrivileges("/tmp", false);
			dbmswrapper.setReaderAuth("", "");
			dbmswrapper.checkReaderPrivileges();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			System.err.println(e.getMessage());
		}
	}
}
