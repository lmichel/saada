package saadadb.database;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;

import saadadb.cache.CacheManager;
import saadadb.cache.CacheManagerRelationIndex;
import saadadb.cache.CacheMeta;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.MetaRelation;
import saadadb.sqltable.SQLQuery;
import saadadb.util.DefineType;
import saadadb.util.Messenger;
import saadadb.util.Version;
import cds.astro.Astroframe;
import cds.astro.Qbox;

/**
 * @author michel
 * @version $Id$
 * * @version $Id$

 */
public class Database {

	private static String separ = System.getProperty("file.separator");
	private static SaadaDBConnector connector;
	public static String max_int = Integer.toString(Integer.MAX_VALUE);

	public static String max_long = Long.toString(Long.MAX_VALUE);

	public static String max_short = Short.toString(Short.MAX_VALUE);

	public static CacheManager cache ;
	
	public static CacheManagerRelationIndex cacheindex = null;

	public static CacheMeta cachemeta;

	private static boolean init_done = false;
	


	/**
	 * @param db_name
	 */
	public static void init(String db_name) {
		if( Database.init_done == false ) {
			Messenger.printMsg(Messenger.TRACE, "Initialization of SaadaDB <" + db_name + ">");
			try{
				initConnector(db_name, false);
				cacheindex = new CacheManagerRelationIndex(20, Repository.getIndexrelationsPath() + Database.getSepar());
				cachemeta = new CacheMeta();
				cache = new CacheManager();
				Qbox.setLevel(10);
			}catch(Exception e){
				Messenger.printStackTrace(e);
				System.exit(1);
			}
		DefineType.init();
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
//		if(Database.connector != null && Database.connector.getJDBCConnection() != null ) {
//	      	  Messenger.printMsg(Messenger.WARNING, "Close the former connection"); 
//				Database.connector.getJDBCConnection().close();
//			}
		Database.connector = SaadaDBConnector.getConnector(db_name, force);
	}
	
	/**
	 * Used by the DB confif setup: allows tro use all DB stuff before aby SaadaDB does exist
	 */
	public static void setConnector(SaadaDBConnector connector) throws Exception {
//		if(Database.connector != null && Database.connector.getJDBCConnection() != null ) {
//      	  Messenger.printMsg(Messenger.WARNING, "Close the former connection");
//			Database.connector.getJDBCConnection().close();
//		}
		Database.connector = connector;
	}
	/**
	 * @return
	 */
	public static String version() {
		return Version.getVersion(); 
	}
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
	 * @return
	 */
	public static String getUrl_root() {
		return connector.getUrl_root();
	}
	/**
	 * @return
	 */
	public static double getCoord_equi() {
		return connector.getCoord_equi();
	}
	/**
	 * @return
	 */
	public static String getCoord_sys() {
		return connector.getCoord_sys();
	}
	public static String getCooSys() {
		if( connector.getCoord_equi() != 0.0 ) {
			return "J" + connector.getCoord_equi();
		}
		else {
			return connector.getCoord_sys();
		}
	}
	/**
	 * @return
	 */
	public static Astroframe getAstroframe() {
		return connector.getAstroframe();
	}
	/**
	 * @return
	 */
	public static String getDbname() {
		return connector.getDbname();
	}
	/**
	 * Return the name of the database used by the reader for temporary table
	 * @return
	 * @throws FatalException 
	 */
	public static String getTempodbName() throws FatalException {
		return getWrapper().getTempodbName(connector.getDbname());
	}
	/**
	 * @return
	 */
	public static String getDescription() {
		return connector.getDescription();
	}
	/**
	 * @return
	 */
	public static String getFlux_unit() {
		return connector.getFlux_unit();
	}
	/**
	 * @return
	 */
	public static String getJdbc_driver() {
		return connector.getJdbc_driver();
	}
	/**
	 * @return
	 */
	public static String getJdbc_reader_password() {
		return connector.getJdbc_reader_password();
	}
	/**
	 * @return
	 */
	public String getJdbc_url() {
		return connector.getJdbc_url();
	}
	/**
	 * @return
	 */
	public static String getJdbc_reader() {
		return connector.getJdbc_reader();
	}
	/**
	 * @return
	 */
	public static String getJdbc_administrator() {
		return connector.getJdbc_administrator();
	}
	/**
	 * @return
	 */
	public static String getRepository() {
		return connector.getRepository();
	}
	/**
	 * @return
	 */
	public static String getRoot_dir() {
		return connector.getRoot_dir();
	}
	/**
	 * @return
	 */
	public static String getSpect_unit() {
		return connector.getSpect_unit();
	}
	/**
	 * @return
	 */
	public static String getClassLocation() {
		return Database.getRoot_dir() + Database.getSepar() + "class_mapping";
	}
	/**
	 * @return
	 */
	public static String getLogDir() {
		return Repository.getLogsPath();
	}
	/**
	 * @return
	 */
	public static String getVOreportDir() {
		return Repository.getVoreportsPath();
	}

	/**
	 * @return
	 * @throws FatalException 
	 */
	public static Connection get_connection() throws FatalException {
		if( connector == null ) {
			return null;
		}
		else {
			return connector.getJDBCConnection();
		}
	}

	/**
	 * @return
	 */
	public static String getSpect_type() {
		return connector.getSpect_type();
	}
	

	/**
	 * @return object persistence of this OID
	 * @param OID
	 *            of object persistent
	 * @throws SaadaException 
	 */
	// sybase int8 (cast)
	public static SaadaInstance getObjectBusiness(SaadaInstance obj) throws FatalException {
		try {
			long oid = obj.getOid();
			String _nameclass = Database.cachemeta.getClass(SaadaOID.getClassNum(oid)).getName();
			Field fieldlist[] = obj.getClass().getDeclaredFields();
			String sql = "";
			sql = " Select * from " + _nameclass
							+ " where  oidsaada = " + oid;
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run(sql);
			if (rs.next()) {
				obj.setOid(rs.getLong("oidsaada"));
				obj.setNameSaada(rs.getString("namesaada").trim());
				String sm = rs.getString("md5keysaada");
				/*
				 * ObsCOre obj have no MD5
				 */
				if( sm != null) {
				obj.setContentsignature(rs.getString("md5keysaada").trim());
				}
				for (int i = 0; i < fieldlist.length; i++) {
					obj.changeField(fieldlist[i], rs);
				}
			} else {
				squery.close();
				return null;
			}
			squery.close();
			obj.loaded = true;
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
		obj.setOid(oid);
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
	 * @return Returns the path separator usable in Regexp (\ -> \\).
	 */
	public static String getRegexpSepar() {
		if( separ.equals("\\")) {
			return "\\\\";
		}
		else {
			return separ;	
		}
	}

	/*
	 * Must never be invoked: Exist just to make Servlet code passing the compilation in Saada
	 * (out of a SaadaDB). At database creation Database.init(), is replaced with Database.init("DBNAME")
	 */
	public static void init() {
		
	}
 
}
