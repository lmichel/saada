package saadadb.database;

import healpix.core.HealpixIndex;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.ResultSet;

import saadadb.cache.CacheManager;
import saadadb.cache.CacheManagerRelationIndex;
import saadadb.cache.CacheMeta;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.MetaRelation;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Collection;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;
import saadadb.util.Version;
import saadadb.vocabulary.DefineType;
import saadadb.vocabulary.enums.DispersionType;
import cds.astro.Astroframe;
import cds.astro.Qbox;

/**
 * @author michel
 * @version $Id$
 * 01/2014: Add healpixIndex filed with its getter
 * 02/2014: Use of the spooler for the admin connections
 */
public class Database {

	private static String separ = File.separator;
	private static SaadaDBConnector connector;

	public static CacheManager cache ;
	public static CacheManagerRelationIndex cacheindex = null;
	public static CacheMeta cachemeta;

	private static boolean init_done = false;
	/*
	 * Used when a the rooturl is specific to one WEB applicaiton instance different from this
	 * stored in the DB (Multiple WEB apps on one DB)
	 */
	private static String localUrl_root = "";
	private static HealpixIndex healpixIndex;

	/**
	 * @param db_name
	 */
	public static void init(String db_name) {
		if( Database.init_done == false ) {
			Messenger.printMsg(Messenger.TRACE, "Initialization of SaadaDB <" + db_name + ">");
			try{
				initConnector(db_name, false);
				Spooler.getSpooler();
				cacheindex = new CacheManagerRelationIndex(20, Repository.getIndexrelationsPath() + Database.getSepar());
				cachemeta = new CacheMeta();
				cache = new CacheManager();
				Qbox.setLevel(10);
				DefineType.init();
			}catch(Exception e){
				Messenger.printStackTrace(e);
				Messenger.trapFatalException( new FatalException(e.getMessage(), SaadaException.getExceptionMessage(e)));
			}
			/*
			 * Flag must be set here to avoid recursivity in cacheindex.getCache(20);
			 */
			Database.init_done = true;
			cache.getCache(5000);
		}
	}

	/**
	 * This operation is separated from the above init because the DBMSwrapper is used to create tables and table creation
	 * must be achieved before to init the cache
	 * @param db_name
	 * @throws FatalException 
	 */
	public static void initConnector(String db_name, boolean force) throws Exception {
		Database.connector = SaadaDBConnector.getConnector(db_name, force);
	}

	/**
	 * Used by the DB config setup: allows to use all DB stuff before any SaadaDB does exist
	 */
	public static void setConnector(SaadaDBConnector connector) throws Exception {
		Database.connector = connector;
	}
	/**
	 * @return
	 */
	public static String version() {
		return Version.getVersion(); 
	}

	/**
	 * This method traps the admin authority authication to do som smooth schema update
	 * That way, new columns can be added to  meta table each time the admin tool is started
	 * @param password
	 * @throws Exception
	 */
	public static void setAdminMode(String password) throws Exception {
		Spooler.getSpooler().openAdminConnection(password);
		Database.updatSchema();
	}

	/**
	 * Do some light weight schema update
	 * @throws Exception
	 */
	public static final void updatSchema()  throws Exception {
		Table_Saada_Relation.addStatColumn();
		Table_Saada_Class.addStatColumn();
		Table_Saada_Collection.addStatColumn();		
	}
	
	/******************************
	 * Data ase access methods
	 *****************************/
	/**
	 * @return
	 */
	public static SaadaDBConnector getConnector() {
		return connector;
	}

	/**
	 * @return
	 * @throws FatalException 
	 */
	public static DbmsWrapper getWrapper() throws FatalException {
		try {
			return connector.getWrapper();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
			return null;
		}
	}

	/**
	 * Get a free reader connection
	 * @return
	 * @throws Exception
	 */
	public static DatabaseConnection getConnection() throws Exception {
		return Spooler.getSpooler().getConnection();
	}
	/**
	 * Give back the reader connection to the spooler
	 * @param connection
	 * @throws Exception
	 */
	public static void giveConnection(DatabaseConnection connection) throws Exception {
		Spooler.getSpooler().give(connection);
	}
	/**
	 * Get the admin connection
	 * @return
	 * @throws Exception
	 */
	public static DatabaseConnection getAdminConnection() throws Exception {
		return Spooler.getSpooler().getAdminConnection();
	}
	/**
	 * Give back the admon connection to the spooler
	 * @param connection
	 * @throws Exception
	 */
	public static void giveAdminConnection() throws Exception {
		Spooler.getSpooler().giveAdmin();
	}
	/**
	 * @return
	 * @throws Exception
	 */
	public static HealpixIndex getHealpixIndex() throws Exception {
		if( healpixIndex == null ) {
			healpixIndex = new HealpixIndex(1 << Database.getHeapix_level());
		}
		return healpixIndex;
	}
	/**
	 * Close the spooler: otherwise a thread continue to run 
	 */
	public static void close() {
		Messenger.printMsg(Messenger.TRACE, "Closing database");
		try {
			Spooler.getSpooler().close();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}
	}

	
	/******************************
	 * Getters
	 *****************************/
	public static String getUrl_root() {
		if( localUrl_root.length() > 0 ) {
			return localUrl_root;
		} else {
			return  (connector == null)? null : connector.getUrl_root();
		}
	}
	public static void setUrl_root(String url_root) {
		localUrl_root = url_root;
	}
	public static double getCoord_equi() {
		return  (connector == null)? null : connector.getCoord_equi();
	}
	public static String getCoord_sys() {
		return  (connector == null)? null : connector.getCoord_sys();
	}
	public static int getHeapix_level() {
		return  (connector == null)? 14: connector.getHealpix_level();
	}
	public static String getCooSys() { 
		if(connector == null) return null ;
		if( connector.getCoord_equi() != 0.0 ) {
			return "J" + connector.getCoord_equi();
		}
		else {
			return connector.getCoord_sys();
		}
	}
	public static Astroframe getAstroframe() {
		return  (connector == null)? null : connector.getAstroframe();
	}
	public static String getDbname() {
		return  (connector == null)? null : connector.getDbname();
	}
	/**
	 * Return the name of the database used by the reader for temporary table
	 * @return
	 * @throws FatalException 
	 */
	public static String getTempodbName() throws FatalException {
		return getWrapper().getTempodbName(connector.getDbname());
	}
	public static String getDescription() {
		return  (connector == null)? null : connector.getDescription();
	}
	public static String getFlux_unit() {
		return  (connector == null)? null : connector.getFlux_unit();
	}
	public static String getJdbc_driver() {
		return  (connector == null)? null : connector.getJdbc_driver();
	}
	public static String getJdbc_reader_password() {
		return  (connector == null)? null : connector.getJdbc_reader_password();
	}
	public String getJdbc_url() {
		return  (connector == null)? null : connector.getJdbc_url();
	}
	public static String getJdbc_reader() {
		return  (connector == null)? null : connector.getJdbc_reader();
	}
	public static String getJdbc_administrator() {
		return  (connector == null)? null : connector.getJdbc_administrator();
	}
	public static String getRepository() {
		return  (connector == null)? null : connector.getRepository();
	}
	/**
	 * @return
	 */
	public static String getRoot_dir() {
		return (connector == null)? null : connector.getRoot_dir();
	}
	/**
	 * @return
	 */
	public static String getSpect_unit() {
		return  (connector == null)? null : connector.getSpect_unit();
	}
	public static String getClassLocation() {
		return Database.getRoot_dir() + Database.getSepar() + "class_mapping";
	}
	public static String getLogDir() {
		return Repository.getLogsPath();
	}
	public static String getVOreportDir() {
		return Repository.getVoreportsPath();
	}
	public static DispersionType getSpect_type() {
		return connector.getSpect_type();
	}

	/******************************
	 * Data acces methods
	 *****************************/

	/**
	 * @return object persistence of this OID
	 * @param OID
	 *            of object persistent
	 * @throws SaadaException 
	 */
	public static SaadaInstance getObjectBusiness(SaadaInstance obj) throws FatalException {
		try {
			long oid = obj.oidsaada;
			String _nameclass = Database.cachemeta.getClass(SaadaOID.getClassNum(oid)).getName();
			Field fieldlist[] = obj.getClass().getDeclaredFields();
			String sql = "";
			sql = " Select * from " + _nameclass + " where  oidsaada = " + oid;
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run(sql);
			if (rs.next()) {
				obj.oidsaada = rs.getLong("oidsaada");
				obj.obs_id = rs.getString("namesaada").trim();
				String sm = rs.getString("md5keysaada");
				/*
				 * ObsCOre obj have no MD5
				 */
				if( sm != null) {
					obj.contentsignature = rs.getString("md5keysaada").trim();
				}
				for (int i = 0; i < fieldlist.length; i++) {					
					if( fieldlist[i].getName().startsWith("_")) {
						obj.setFieldValue(fieldlist[i], rs);
					}
				}
			} else {
				squery.close();
				return null;
			}
			squery.close();
			obj.markAsLoaded();
			return obj;
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
			return null;
		}
	}



	/**
	 * @return instance of this OID
	 * @param OID of object persistent
	 * @exception SaadaException
	 */
	public SaadaInstance getInstance(long oid) throws Exception, InstantiationException {
		String _nameclass = "";
		_nameclass = Database.cachemeta.getClass(SaadaOID.getClassNum(oid)).getName();
		if (_nameclass.equals("")) {
			return null;
		}
		SaadaInstance obj = (SaadaInstance)  SaadaClassReloader.forGeneratedName( _nameclass.trim())
		.newInstance();
		obj.oidsaada = oid;
		return obj;
	}



	/**
	 * @return id of Relation, return -1 if not found
	 * @param name name of relation
	 */
	public static int getIdRelation(String name) {
		MetaRelation mr =  Database.cachemeta.getRelation(name);
		if( mr != null ) {
			return mr.getId();
		}
		else {
			Messenger.printMsg(Messenger.ERROR, "getIdRelation could not get relation <" + name + ">");
			return -1;
		}

	}


	/**
	 * @return Returns the cache.
	 */
	public static CacheManager getCache() {
		return cache;
	}
	/**
	 * @return Returns the cacheindex.
	 */
	public static CacheManagerRelationIndex getCacheindex() {
		return cacheindex;
	}
	/**
	 * @return Returns the cachemeta.
	 */
	public static CacheMeta getCachemeta() {
		return cachemeta;
	}

	/**
	 * @return Returns the name.
	 */
	public static String getName() {
		return Database.connector.getDbname();
	}

	/**
	 * @return Returns the separ.
	 */
	public static String getSepar() {
		return separ;
	}

	/**
	 *  Wrapper for the garbage collector: can enable/disable any explicit callback to the gc
	 */
	public static void gc() {
		//@@@ System.gc();
	}
	/*
	 * Must never be invoked: Exist just to make Servlet code passing the compilation in Saada
	 * (out of a SaadaDB). At database creation Database.init(), is replaced with Database.init("DBNAME")
	 */
	public static void init() {

	}
	
	/**
	 * 
	 */
	public static void exit(){
		Messenger.printMsg(Messenger.ERROR, "Exit on API request");
		Messenger.printStackTrace(new Exception());
		Database.close();
		System.exit(1);
	}


}
