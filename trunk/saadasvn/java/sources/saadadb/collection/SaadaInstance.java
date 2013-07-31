package saadadb.collection;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saadadb.api.SaadaClass;
import saadadb.api.SaadaCollection;
import saadadb.api.SaadaDB;
import saadadb.api.SaadaLink;
import saadadb.api.SaadaRelation;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.DMInterface;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.VOResource;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.prdconfiguration.ConfigurationFlatfile;
import saadadb.prdconfiguration.ConfigurationImage;
import saadadb.prdconfiguration.ConfigurationMisc;
import saadadb.prdconfiguration.ConfigurationSpectrum;
import saadadb.prdconfiguration.ConfigurationTable;
import saadadb.query.executor.Query;
import saadadb.query.matchpattern.CounterpartSelector;
import saadadb.query.matchpattern.Qualif;
import saadadb.query.result.SaadaQLResultSet;
import saadadb.relationship.LongCPIndex;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.util.ChangeType;
import saadadb.util.DefineType;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;

/**
 * @author michel
 *
 * 04/2009: methods getLoaderConfig and getRepositoryPath added 
 * 09/2009: fix of getCounterpartsMatchingQuery: take class constrains in account, support of multiple qualif constrains
 */
public abstract class SaadaInstance implements DMInterface {
	public long oidsaada;
	public long oidproduct = saadadb.util.SaadaConstant.LONG;
	public String contentsignature = saadadb.util.SaadaConstant.STRING;
	public String namesaada = saadadb.util.SaadaConstant.STRING;
	public long date_load = saadadb.util.SaadaConstant.LONG;
	public char access_right = 'X';
	public VignetteFile vignetteFile = null;
	public boolean loaded = false;
	private DMInterface current_dm_interface;
	/**
	 * SaadaInstance Constructor
	 */
	public SaadaInstance() {
	}

	/**
	 * Set all instance field from result set. There is no control of 
	 * the compliance between both result set and Java class. That must me done at higher level.
	 * Used to generate quickly query reports (VOTables or FITS)
	 * @param rs
	 * @throws Exception
	 */
	public void init(SaadaQLResultSet rs) throws Exception {
		Field[] fs = this.getClass().getFields();
		boolean classLevel = false;
		for( int i=0 ; i< fs.length ; i++ ) {
			Field f = fs[i];
			String type = f.getType().toString();
			String name = f.getName();
			if( name.startsWith("_") ) {
				classLevel=true;
			}
			if( "short".equals(type)) {
				f.setShort(this, rs.getShort(name));
			}
			else if( "int".equals(type)) {
				f.setInt(this, rs.getInt(name));
			}
			else if( "long".equals(type)) {
				f.setLong(this, rs.getLong(name));
			}
			else if( "float".equals(type)) {
				f.setFloat(this, rs.getFloat(name));
			}
			else if( "double".equals(type)) {
				f.setDouble(this, rs.getDouble(name));
			}
			else if( "boolean".equals(type)) {
				f.setBoolean(this, rs.getBoolean(name));
			}
			else if( "char".equals(type)) {
				f.setChar(this, rs.getChar(name));
			}
			else if( "byte".equals(type)) {
				f.setByte(this, rs.getByte(name));
			}
			else  {
				Object o = rs.getObject(name);
				if( o == null) {
					f.set(this, SaadaConstant.STRING);
				}
				else {
					f.set(this, o.toString());
				}
			}
		}
		if( classLevel ) this.loaded = true;
	}

	/**
	 * Set all instance field from result set. There is no control of 
	 * the compliance between both result set and Java class. That must me done at higher level.
	 * Used to generate quickly query reports (VOTables or FITS)
	 * @param rs
	 * @throws Exception
	 */
	public void init(ResultSet rs) throws Exception {
		Field[] fs = this.getClass().getFields();
		boolean classLevel = false;
		for( int i=0 ; i< fs.length ; i++ ) {
			Field f = fs[i];
			String type = f.getType().toString();
			String name = f.getName();
			if( name.startsWith("_") ) {
				classLevel=true;
			}
			if( "short".equals(type)) {
				f.setShort(this, rs.getShort(name));
			}
			else if( "int".equals(type)) {
				f.setInt(this, rs.getInt(name));
			}
			else if( "long".equals(type)) {
				f.setLong(this, rs.getLong(name));
			}
			else if( "float".equals(type)) {
				f.setFloat(this, rs.getFloat(name));
			}
			else if( "double".equals(type)) {
				f.setDouble(this, rs.getDouble(name));
			}
			else if( "boolean".equals(type)) {
				f.setBoolean(this, rs.getBoolean(name));
			}
			else if( "char".equals(type)) {
				f.setChar(this, rs.getString(name).charAt(0));
			}
			else if( "byte".equals(type)) {
				f.setByte(this, rs.getByte(name));
			}
			else  {
				Object o = rs.getObject(name);
				if( o == null) {
					f.set(this, SaadaConstant.STRING);
				}
				else {
					f.set(this, o.toString());
				}
			}
		}
		if( classLevel ) this.loaded = true;
	}

	/**
	 * @param oid
	 * @throws SaadaException
	 * @throws CollectionException
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public void init(long oid) throws Exception {
		/*
		 * Here we test the consistance between the oid and class
		 */
		this.oidsaada = oid;
		if( !this.getClass().getName().endsWith(this.getSaadaClass().getName()) ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't make an instance of class <"
					+ this.getClass().getName()
					+ "> with an oid="
					+ oid
					+ " which refers the the class <"
					+ this.getSaadaClass().getName()
					+">");
		}
		String sql = " Select * from " 
			+ Database.getWrapper().getCollectionTableName(SaadaOID.getCollectionName(oid), SaadaOID.getCategoryNum(oid))
			+ " where  oidsaada = " + oid;
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run(sql);
		int cpt=1;
		while( rs.next() ) {
			if( cpt > 1 ) {
				Messenger.printMsg(Messenger.ERROR, "FATAL: Multiple instances with oid " + oid);
				System.exit(1);
			}
			cpt++;

			this.setOid(rs.getLong("oidsaada"));
			this.setNameSaada(rs.getString("namesaada"));
			this.setDateLoad(rs.getLong("date_load"));
			/** ----------Attention Super Class-------------* */
			// Class cls = obj.getClass();
			Class cls = this.getClass().getSuperclass();
			Vector<Class> vt_class = new Vector<Class>();
			while (!cls.getName().equals("saadadb.collection.SaadaInstance")) {
				vt_class.add(cls);
				cls = cls.getSuperclass();
			}
			for (int k = vt_class.size() - 1; k >= 0; k--) {
				Field fieldlist[] = (vt_class.get(k)).getDeclaredFields();
				for (int i = 0; i < fieldlist.length; i++) {
					changeField(fieldlist[i], rs);
				}
			}
		}
		squery.close();;
	}
	/**
	 * @return
	 */
	public long getDateLoad() {
		return this.date_load;
	}

	/**
	 * @param _dateload
	 */
	public void setDateLoad(long _dateload) {
		this.date_load = _dateload;
	}

	/**
	 * @return
	 */
	public char getAccessRight() {
		return this.access_right;
	}

	/**
	 * @param access
	 */
	public void setAccessRight(char access) {
		this.access_right = access;
	}

	/**
	 * @return long
	 * @roseuid 3EF196BF0131
	 */
	public void setOid(long oid) {
		this.oidsaada = oid;
	}

	/**
	 * @return
	 */
	public long getOid() {
		return oidsaada;
	}

	/**
	 * @return
	 */
	public String getNameClass() {
		return this.getClass().getName();
	}

	/**
	 * @param s
	 * @return
	 */
	public String computeContentSignature(String s) {
		contentsignature = MD5Key.calculMD5Key(s);
		return contentsignature;
	}

	/**
	 * @return
	 */
	public String getContentsignature() {
		return this.contentsignature;
	}

	/**
	 * @param key
	 */
	public void setContentsignature(String key) {
		this.contentsignature = key;
	}

	/**
	 * @param instance_name
	 */
	public void setNameSaada(String instance_name) {
		this.namesaada = instance_name;
	}

	/**
	 * @return
	 */
	public String getNameSaada() {
		return this.namesaada;
	}


	/**
	 * @return
	 */
	public int getCategory() {
		return SaadaOID.getCategoryNum(this.oidsaada);
	}

	/**
	 * @return
	 */
	public SaadaCollection getCollection() throws FatalException {

		return new SaadaCollection(Database.getCachemeta().getCollection(SaadaOID.getCollectionNum(this.oidsaada)).getName());
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public SaadaClass getSaadaClass() throws FatalException {
		return new SaadaClass(Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada)).getName());
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public String[] getStartingRelationNames() throws FatalException {
		return Database.getCachemeta().getRelationNamesStartingFromColl(getCollection().getName(), getCategory());
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public SaadaRelation getStartingRelation(String rel_name) throws FatalException {
		String names[] = this.getStartingRelationNames();
		for( int i=0 ; i<names.length ; i++ ) {
			if( names[i].toString().equals(rel_name) ) {
				return new SaadaRelation(rel_name);
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't find relation " + rel_name 
				+ " starting from class " + this.getNameClass());
		return null;
	}

	/**
	 * Returns the counterparts of the instance by the relation
	 * Take the current time in hundreds of ms as lock
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public long[] getCounterparts(String rel_name) throws FatalException {
		return getCounterparts(rel_name, ((new Date()).getTime())/100);
	}
	/**
	 * Returns the counterparts of the instance by the relation
	 * @param rel_name   name of the relation
	 * @param lock       Relationship index lock
	 * @return
	 * @throws FatalException
	 */
	public long[] getCounterparts(String rel_name, long lock) throws FatalException {
		LongCPIndex ind = (LongCPIndex) Database.getCacheindex().getCorrIndex(rel_name, lock);
		if( ind == null ) {
			FatalException.throwNewException(SaadaException.FILE_ACCESS, "Returned index is null");
		}
		long[] ret =  ind.getLongCP(this.oidsaada);
		Database.getCacheindex().freeCorrIndex(rel_name, lock);
		/*
		 * If there are no CP, the index returns null
		 */
		if( ret == null ) {
			return new long[0];
		} else {
			return ret;
		}
	}

	/**
	 * @return
	 * @throws Exception 
	 * @throws SaadaException 
	 */
	public SaadaLink[] getStartingLinks(String rel_name) throws SaadaException, Exception {
		return this.getStartingRelation(rel_name).getCounterparts(this.oidsaada, false);
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public String[] getEndingRelationNames() throws FatalException {
		return Database.getCachemeta().getRelationNamesEndingOnColl(getCollection().getName(), getCategory());
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public SaadaRelation getEndingRelation(String rel_name) throws FatalException {
		String names[] = this.getEndingRelationNames();
		for( int i=0 ; i<names.length ; i++ ) {
			if( names[i].toString().equals(rel_name) ) {
				return new SaadaRelation(rel_name);
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't find relation " + rel_name 
				+ " ending to class " );
		return null;
	}

	/**
	 * @return
	 * @throws Exception 
	 * @throws SaadaException 
	 */
	public SaadaLink[] getEndingLinks(String rel_name) throws Exception {
		return this.getEndingRelation(rel_name).getCounterparts(this.oidsaada, false);
	}

	/**
	 * Return all counterparts of the relation rel_name matching the selector (CP query + qualifiers).
	 * The query is usually build by the query engine when processing matchPatterns
	 * @param rel_name
	 * @param selector
	 * @return
	 * @throws Exception
	 */
	public Set<SaadaLink> getCounterpartsMatchingQuery(String rel_name, CounterpartSelector selector) throws Exception {
		Set<SaadaLink> retour = new TreeSet<SaadaLink>();
		if( selector == null ) { 	
			return new TreeSet<SaadaLink>(Arrays.asList(this.getStartingLinks(rel_name)));
		}
		else {
			ArrayList<SaadaLink>  sls = this.getCounterpartsMatchingQuery(rel_name, selector.getCp_query());
			LinkedHashMap<String, Qualif> qualif_query = selector.getQualif_query();   	
			TreeSet<String> tsmc = selector.getMetaClassTab();
			for( SaadaLink sl : sls) {
				boolean bad = false;
				for( Entry<String, Qualif> e: qualif_query.entrySet()) {
					if( !sl.match(e.getKey(), e.getValue())) {
						bad = true;
						break;
					}
				}
				if( !bad ) {
					if( tsmc != null && tsmc.size() != 0 ) {
						String cl = SaadaOID.getClassName(sl.getEndindOID());
						if( tsmc.contains(cl) ) {
							retour.add(sl);						
						}
					}
					else {
						retour.add(sl);
					}
				}
			}
		}
		return retour;
	}
	/**
	 * Return all counterparts of the relation rel_name matching the query rel_query.
	 * The query is usually build by the query engine when processing matchPatterns
	 * @param rel_name
	 * @param selector
	 * @return
	 * @throws Exception
	 */
	public ArrayList<SaadaLink> getCounterpartsMatchingQuery(String rel_name, String rel_query ) throws Exception {
		SaadaLink sls[] = this.getStartingLinks(rel_name);
		ArrayList<SaadaLink> retour = new ArrayList<SaadaLink>();
		if( rel_query == null || rel_query.length() == 0) {
			for( SaadaLink sl : sls) {
				retour.add(sl);
			}
		}
		else {
			String query = "";
			for( SaadaLink sl : sls) {
				if( query.length() > 0 ) {
					query += ", ";
				}
				query += sl.getEndindOID() ;
			}
			/*
			 * In case of cardinality 0 pattern, instances without counterpart are selected
			 * So query can be empty
			 */
			if( query.length() != 0  ) {
				SQLQuery squery = new SQLQuery();
				query = "SELECT oidsaada FROM (" + rel_query + ") As subquery WHERE oidsaada IN (" + query + ")";
				ResultSet rs = squery.run(query);
				while( rs.next()) {
					long soid = rs.getLong(1);
					for( SaadaLink sl: sls) {
						if( sl.getEndindOID() == soid) {
							retour.add(sl);
						}
					}
				}
				squery.close();
			}
		}
		return retour;
	}
	/**
	 * @throws SaadaException 
	 * 
	 */
	public void loadBusinessAttribute() throws FatalException {
		if( this.loaded == false && this.getCategory() != Category.FLATFILE ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, getClass().getName()
					+ " load business attribute");
			SaadaDB.getCache().getObjectBusiness(this);
			this.loaded = true;
		}
	}

	/**
	 * @param name
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws SaadaException 
	 */
	public Object getFieldValue(String field) throws Exception {
		if( field.startsWith("_")) {
			this.loadBusinessAttribute();
		}
		/*
		 * Field can be taken from the database which is not case sensitive
		 * If the field is not found by Java, we look for it manually
		 */
		try {
			return this.getClass().getField(field).get(this);
		} catch (Exception e) {
			Field[] flds = this.getClass().getFields();
			for( Field f: flds) {
				if( f.getName().equalsIgnoreCase(field)) {
					return f.get(this);
				}
			}
			throw new NoSuchFieldException(field);
		}
	}

	/**
	 * Returns the field value as a String. This string is empty if the field is not set
	 * @param field
	 * @return
	 * @throws Exception
	 */
	public String getFieldString(String field) throws Exception{
		Object fv = getFieldValue(field);
		if( fv == null ) {
			return "";
		}
		else {
			String val = getFieldValue(field).toString();
			/*
			 * Fields typed as CHAR can not be trapped with a REGEXP because they insert a non printable char in a string
			 * They are caught as char
			 */
			if( val.matches(RegExp.NOT_SET_VALUE) 
					||(fv instanceof java.lang.Character && ((Character)getFieldValue(field) == SaadaConstant.CHAR)) ){
				return "";
			}
			else return val;
		}
	}
	/**
	 * @param name
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws Exception 
	 */
	public Object getFormatedFieldValue(String field) throws Exception {
		this.loadBusinessAttribute();
		if( field.startsWith("_") ){
			MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
			for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
				if( field.equals(ah.getNameattr()) ) {
					return ah.getFormatedValue(this.getFieldValue(ah.getNameattr()));
				}
			}
		}
		else {
			for( AttributeHandler ah: MetaCollection.getAttribute_handlers(SaadaOID.getCategoryNum(this.oidsaada)).values() ) {
				if( field.equals(ah.getNameattr()) ) {
					if( field.equals("date_load")) {
						return(new Date(Long.parseLong(this.getFieldValue(ah.getNameattr()).toString())));
					}
					else {
						return ah.getFormatedValue(this.getFieldValue(ah.getNameattr()));
					}
				}
			}   		
		}
		return null;
	}


	/**
	 * Return the value of the first field having the requested Utype
	 * @param utype
	 * @param queriable_only
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public Object getFieldValueByUtype(String utype, boolean queriable_only) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( (!queriable_only || ah.isQueriable() ) && utype.equals(ah.getUtype()) ) {
				return this.getFieldValue(ah.getNameattr());
			}
		}
		return null;
	}

	/**
	 * @param utype
	 * @param queriable_only
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public AttributeHandler getFieldByUtype(String utype, boolean queriable_only) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException, SaadaException {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( (!queriable_only || ah.isQueriable() ) && utype.equals(ah.getUtype()) ) {
				return ah;
			}
		}
		return null;
	}

	/**
	 * Returns a string like "kw = val unit" with kw matching the first attribute queriable with utype as utype
	 * @param utype
	 * @param queriable_only
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public String getFieldDescByUtype(String utype) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( ah.isQueriable() && utype.equals(ah.getUtype()) ) {
				return ah.getNameattr() + "=" + this.getFieldValue(ah.getNameattr()) + ah.getUnit();
			}
		}
		return null;
	}

	/**
	 * Return an hashmap with all valued attribute handler matching the requested Utype
	 * @param utype
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public LinkedHashMap<String, AttributeHandler>getAllFieldValuesByUtype(String utype) throws Exception {
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( utype.equals(ah.getUtype()) ) {
				ah.setValue(this.getFieldValue(ah.getNameattr()).toString());
				retour.put(ah.getNameattr(), ah);
			}
		}
		return retour;
	}

	/**
	 * Return an hashmap with all valued attribute handler matching the requested UCD
	 * @param utype
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public LinkedHashMap<String, AttributeHandler>getAllFieldValuesByUCD(String ucd) throws Exception {
		LinkedHashMap<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( ucd.equals(ah.getUcd()) ) {
				ah.setValue(this.getFieldValue(ah.getNameattr()).toString());
				retour.put(ah.getNameattr(), ah);
			}
		}
		return retour;
	}
	/**
	 * Return the value of the first field having the requested UCD
	 * @param name
	 * @return
	 * @throws NoSuchFieldException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws SaadaException 
	 */
	public Object getFieldValueByUCD(String ucd, boolean queriable_only) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( (!queriable_only || ah.isQueriable() ) && ucd.equals(ah.getUcd()) ) {
				return this.getFieldValue(ah.getNameattr());
			}
		}
		return null;
	}

	/**
	 * @param ucd
	 * @param queriable_only
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws SaadaException
	 */
	public AttributeHandler getFieldByUCD(String ucd, boolean queriable_only) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		Field[] fs = this.getClass().getFields();
		for( Field f: fs) {
			AttributeHandler ah;
			if( (ah = mc.getAttributes_handlers().get(f.getName())) != null ) {
				if( (!queriable_only || ah.isQueriable() ) && ucd.equals(ah.getUcd()) ) {
					return ah;
				}				
			}
		}
		//		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
		//			if( (!queriable_only || ah.isQueriable() ) && ucd.equals(ah.getUcd()) ) {
		//				return ah;
		//			}
		//		}
		return null;
	}

	/**
	 * Returns a string like "kw = val unit" with kw matching the first attribute queriable with ucd as ucd
	 * @param utype
	 * @param queriable_only
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 * @throws SaadaException
	 */
	public String getFieldDescByUCD(String ucd) throws Exception {
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		for( AttributeHandler ah: mc.getAttributes_handlers().values() ) {
			if( ah.isQueriable() && ucd.equals(ah.getUcd()) ) {
				return ah.getNameattr() + "=" + this.getFieldValue(ah.getNameattr()) + ah.getUnit();
			}
		}
		return null;
	}
	/**
	 * @throws Exception
	 */
	public void store() throws Exception {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Store Saada instance (business level) <" + this.getOid() + ">");
		String sql = "";
		String nametable = this.getClass().getName();
		// if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "SaadaInstance class
		// name: "+nametable);
		nametable = nametable.substring(nametable.lastIndexOf(".") + 1,
				nametable.length());
		Class cls = this.getClass();
		Field fieldlist[] = cls.getDeclaredFields();
		String attr = "oidsaada,namesaada,md5keysaada";
		for (int i = 0; i < fieldlist.length; i++) {
			attr += "," + fieldlist[i].getName();
		}
		sql = "Insert into " + nametable + "(" + attr + ")" + " values ( "
		+ this.oidsaada + ",'" + this.getNameSaada() + "' ,'"
		+ this.contentsignature + "' ";
		for (int i = 0; i < fieldlist.length; i++)  {
			sql +=  ", " + this.getSQL(fieldlist[i]);

		}
		sql += " )";
		/*
		 * Run the query: Transaction open at higher level
		 */
		SQLTable.addQueryToTransaction(sql, nametable);
		this.storeCollection();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Object <"
				+ this.getClass().getName() + "> with OID <" + this.oidsaada
				+ "> is persistent");
	}

	public void store(BufferedWriter colwriter, BufferedWriter buswfriter) throws Exception {
		String sql = "";
		String nametable = this.getClass().getName();
		// if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "SaadaInstance class
		// name: "+nametable);
		nametable = nametable.substring(nametable.lastIndexOf(".") + 1,
				nametable.length());
		Class cls = this.getClass();
		Field fieldlist[] = cls.getDeclaredFields();
		String file_bus_sql = "";
		file_bus_sql = this.oidsaada + "\t" + this.getNameSaada() + "\t"
		+ this.contentsignature;

		for( int i=0 ; i<fieldlist.length ; i++  ) {
			Field field = fieldlist[i];
			file_bus_sql += "\t";
			String val = field.get(this).toString();

			/*
			 * "NULL and "2147483647" matches util.SaadaConstant.INT/STRING 
			 * which belong to default values applied for Saada objects
			 */
			if( val.equals("Infinity") || val.equals("NaN") || val.equals("") || 
					val.equals("NULL")|| val.equals("2147483647") || val.equals("9223372036854775807")) {
				file_bus_sql +=Database.getWrapper().getAsciiNull();
			} else {
				String type = field.getType().toString();
				if( type.equals("char") || type.endsWith("String") ) {
					file_bus_sql += val.replaceAll("'", "");
				} else if( type.equals("boolean")  ) {
					file_bus_sql += Database.getWrapper().getBooleanAsString(Boolean.parseBoolean(val));
				} else {
					file_bus_sql +=  val;
				}
			}
		}
		buswfriter.write(file_bus_sql + "\n");

		this.storeCollection(colwriter);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Object <"
				+ this.getClass().getName() + "> with OID <" + this.oidsaada
				+ "> is persistent");
	}


	/**
	 * Store values of collection level. Attribute are read in the meta cache but not in the class
	 * in order ot avoid conflicts with attribute order or possible future non persistent attributes
	 * @throws Exception
	 */
	public  void storeCollection() throws FatalException {
		try {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Store Saada instance (collection level) <" + this.getOid() + ">");
			String sql = "";
			Class _cls ;
			if( this.getCategory() != Category.FLATFILE) {
				_cls = this.getClass().getSuperclass();
			} else {
				_cls = Class.forName("generated." + Database.getDbname() + ".FLATFILEUserColl");
			}
			Iterator it=null;
			switch(this.getCategory()) {
			case Category.CUBE:     it = MetaCollection.getAttribute_handlers_cube().values().iterator(); break;
			case Category.ENTRY:    it = MetaCollection.getAttribute_handlers_entry().values().iterator(); break;
			case Category.TABLE:    it = MetaCollection.getAttribute_handlers_table().values().iterator(); break;
			case Category.IMAGE:    it = MetaCollection.getAttribute_handlers_image().values().iterator(); break;
			case Category.SPECTRUM: it = MetaCollection.getAttribute_handlers_spectrum().values().iterator(); break;
			case Category.MISC:     it = MetaCollection.getAttribute_handlers_misc().values().iterator(); break;
			case Category.FLATFILE: it = MetaCollection.getAttribute_handlers_flatfile().values().iterator(); break;
			default: FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Internal error: category<" + this.getCategory() + "> unknown");
			}
			/*
			 * Build the VALUE statement of the INSERT query
			 */
			boolean first = true;
			while( it.hasNext()) {
				AttributeHandler ah = (AttributeHandler)(it.next());
				String val = this.getSQL(_cls.getField(ah.getNameattr()));
				if(!first) {
					sql += ", ";				
				} else {
					first = false;
				}
				sql += val;				

			}
			/*
			 * Run the query: Transaction open at higher level
			 */
			String coll_table = Database.getWrapper().getCollectionTableName(this.getCollection().getName(), this.getCategory());
			SQLTable.addQueryToTransaction("INSERT INTO " + coll_table 
					+ " VALUES (" + sql + ")", coll_table);   	
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}
	}
	/**
	 * @throws Exception
	 */
	public  void storeCollection(Writer writer) throws FatalException {
		try {
			String sql = "";
			Class _cls = this.getClass().getSuperclass();
			Iterator it=null;
			switch(this.getCategory()) {
			case Category.CUBE:     it = MetaCollection.getAttribute_handlers_cube().values().iterator(); break;
			case Category.ENTRY:    it = MetaCollection.getAttribute_handlers_entry().values().iterator(); break;
			case Category.TABLE:    it = MetaCollection.getAttribute_handlers_table().values().iterator(); break;
			case Category.IMAGE:    it = MetaCollection.getAttribute_handlers_image().values().iterator(); break;
			case Category.SPECTRUM: it = MetaCollection.getAttribute_handlers_spectrum().values().iterator(); break;
			case Category.MISC:     it = MetaCollection.getAttribute_handlers_misc().values().iterator(); break;
			case Category.FLATFILE: it = MetaCollection.getAttribute_handlers_flatfile().values().iterator(); break;
			default: FatalException.throwNewException(SaadaException.WRONG_PARAMETER,"Internal error: category<" + this.getCategory() + "> unknown");
			}
			/*
			 * Build the VALUE statement of the INSERT query
			 */
			boolean first = true;
			while( it.hasNext()) {
				AttributeHandler ah = (AttributeHandler)(it.next());
				Field f = _cls.getField(ah.getNameattr());
				String val = "";
				if( f.get(this) == null ) {
					val = "null";
				} else {
					val = f.get(this).toString();
					if( "true".equals(val)) {
						val = Database.getWrapper().getBooleanAsString(true);
					} else if( "false".equals(val)) {
						val = Database.getWrapper().getBooleanAsString(false);
					}
				}
				/*
				 * "NULL and "2147483647" match util.SaadaConstant.INT/STRING 
				 * which belong to default values applied for Saada objects
				 */
				if( val.equals("Infinity") || val.equals("NaN") || val.equals("") || 
						val.equals("NULL")|| val.equals("2147483647") || val.equals("9223372036854775807")) {
					val = Database.getWrapper().getAsciiNull();
				}

				if(!first) {
					sql += "\t";				
				} else {
					first = false;
				}
				sql += val;				
			}
			sql += "\n";
			/*
			 * Append the ASCII row to the file
			 */
			writer.write(sql);
		} catch(Exception e) {
			e.printStackTrace();
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}
	}   

	/**
	 * Return the value of the field usable in an INSERT SQL statement
	 * @param field
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public final  String getSQL(Field field) throws Exception {
		String val;
		Object obj_val;
		if( (obj_val = field.get(this)) == null ) {
			val = "null";
		} else {
			val = obj_val.toString();
		}
		/*
		 * "NULL and "2147483647" matches util.SaadaConstant.INT/STRING 
		 * which belong to default values applied for Saada objects
		 */
		if( val.equals("Infinity") || val.equals("NaN") || val.equals("") || 
				val.equals("NULL")|| val.equals("2147483647") || val.equals("9223372036854775807")) {
			return "null";
		}
		/*
		 * Relevant types  numeric or char/String for SQL
		 */
		String type = field.getType().toString();
		if( type.equals("char") || type.endsWith("String") ) {
			return "'" + val.replaceAll("'", "") + "'";
		} else if( type.equals("boolean")  ) {
			return Database.getWrapper().getBooleanAsString(Boolean.parseBoolean(val));
		} else {
			return val;
		}
	}

	/**
	 * @param obj
	 * @param field
	 * @param rs
	 * @throws SaadaException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SQLException
	 */
	public  void changeField(Field field, ResultSet rs)
	throws SaadaException, IllegalArgumentException, IllegalAccessException, SQLException {
		int type = DefineType.getType(ChangeType.getTypeJavaFromTypeClass(field.getType().toString()));
		boolean is_null = false ;
		if( rs.getObject(field.getName()) == null ) {
			is_null = true;
		}
		switch (type) {
		case DefineType.FIELD_LONG:
			if( is_null) {
				field.setLong(this, SaadaConstant.LONG);				
			} else {
				field.setLong(this, rs.getLong(field.getName()));
			}
			break;
		case DefineType.FIELD_INT:
			if( is_null) {
				field.setInt(this, SaadaConstant.INT);				
			} else {
				field.setInt(this, rs.getInt(field.getName()));
			}
			break;
		case DefineType.FIELD_DOUBLE:
			if( is_null) {
				field.setDouble(this, SaadaConstant.DOUBLE);				
			} else {
				field.setDouble(this, rs.getDouble(field.getName()));
			}
			break;
		case DefineType.FIELD_FLOAT:
			if( is_null) {
				field.setFloat(this, SaadaConstant.FLOAT);				
			} else {
				field.setFloat(this, rs.getFloat(field.getName()));
			}
			break;
		case DefineType.FIELD_BOOLEAN:
			field.setBoolean(this, rs.getBoolean(field.getName()));
			break;
		case DefineType.FIELD_STRING:
			Object o = rs.getObject(field.getName());
			if( o != null ) {
				field.set(this, o.toString().trim());
			} else {
				field.set(this, null);					
			}
			break;
		case DefineType.FIELD_SHORT:
			if( is_null) {
				field.setShort(this, SaadaConstant.SHORT);				
			} else {
				field.setShort(this, rs.getShort(field.getName()));
			}
			break;
		case DefineType.FIELD_BYTE:
			field.setByte(this, rs.getByte(field.getName()));
			break;
		case DefineType.FIELD_CHAR:
			if( is_null) {
				field.setChar(this, SaadaConstant.CHAR);				
			} else {
				/*
				 * Chars are returned (with PSQL) as character(1)
				 */
				String s =  (String) rs.getObject(field.getName());
				if( s.length() > 0 ) {
					field.setChar(this, s.charAt(0));
				} else {
					field.setChar(this, SaadaConstant.CHAR);
				}
			}
			break;
		case DefineType.FIELD_DATE:
			if( is_null) {
				field.set(this, null);				
			} else {
				field.set(this, rs.getDate(field.getName()));
			}
			break;
		default:
			FatalException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Database : Unknown field  type <" + type + "> see class saadadb.utils.DefineType");
		}
	}

	/**
	 * @param fld
	 * @param value
	 * @throws Exception
	 */
	public  void setInField(Field fld, String value) throws Exception {
		String tv = (value == null)?"":value.trim();
		// Tests the Integer corresponding to this value type, and sets the
		// value in the field with corresponding method
		// The value Integer is defined in the class DefineType (package
		// saadadb.util)
		switch (DefineType.getType(ChangeType.getTypeJavaFromTypeClass(fld.getType().toString()))) {
		case DefineType.FIELD_DOUBLE:
			try {
				fld.setDouble(this
						, (tv.length() == 0)?SaadaConstant.DOUBLE:Double.parseDouble(value));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName() + " " + e.getMessage());
				fld.setDouble(this, SaadaConstant.DOUBLE);					
			}
			break;
		case DefineType.FIELD_SHORT:
			try {
				fld.setShort(this
						, (tv.length() == 0)?SaadaConstant.SHORT:Short.parseShort(value.replaceAll("\\+", "")));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setShort(this, SaadaConstant.SHORT);					
			}
			break;
		case DefineType.FIELD_INT:
			try {
				fld.setInt(this
						, (tv.length() == 0)?SaadaConstant.INT:Integer.parseInt(value.replaceAll("\\+", "")));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setInt(this, SaadaConstant.INT);					
			}
			break;
		case DefineType.FIELD_LONG:
			try {
				fld.setLong(this
						, (tv.length() == 0)?SaadaConstant.LONG:Long.parseLong(value));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setLong(this, SaadaConstant.LONG);
			}
			break;
		case DefineType.FIELD_BYTE:
			try {
				fld.setByte(this
						, (tv.length() == 0)?SaadaConstant.BYTE:Byte.parseByte(value));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setByte(this, SaadaConstant.BYTE);
			}
			break;
		case DefineType.FIELD_STRING:
			fld.set(this, value);
			break;
		case DefineType.FIELD_FLOAT:
			try {
				fld.setFloat(this
						, (tv.length() == 0)?SaadaConstant.FLOAT:Float.parseFloat(value));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setFloat(this, SaadaConstant.FLOAT);
			}
			break;
		case DefineType.FIELD_CHAR:
			try {
				fld.setChar(this
						, (tv.length() == 0)?SaadaConstant.CHAR:value.charAt(0));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setChar(this, SaadaConstant.CHAR);
			}
			break;
		case DefineType.FIELD_BOOLEAN:
			try {
				fld.setBoolean(this
						, (tv.length() == 0)?false:Boolean.getBoolean(value));
			} catch(NumberFormatException e) {
				Messenger.printMsg(Messenger.ERROR, "Cast Error on fields " +  fld.getName()+ " " + e.getMessage());
				fld.setBoolean(this, false);
			}
			break;
		default:
			Messenger.printMsg(Messenger.ERROR,
					"SaadaInstance.setInField : Unknow type: " + fld.getType().toString());
			break;
		}
	}

	/**
	 * @return Returns the oidproduct.
	 */
	public long getOidproduct() {
		return oidproduct;
	}

	/**
	 * @param oidproduct The oidproduct to set.
	 */
	public void setOidproduct(long oidproduct) {
		this.oidproduct = oidproduct;
	}

	/**
	 * @param full_path
	 * @return
	 */
	public String getURL(boolean full_path) {
		String retour = "getinstance?oid=" + this.oidsaada;
		if( full_path ) {
			return Database.getUrl_root() + "/" + retour;
		}
		else {
			return retour;
		}
	}

	/**
	 * @param full_path
	 * @return
	 * @throws FatalException 
	 */
	public String getDownloadURL(boolean full_path) throws FatalException {
		String retour;
		retour = "download?oid=" + this.oidsaada;
		if( full_path ) {
			return Database.getUrl_root() + "/" + retour;
		}
		else {
			return retour;
		}
	}	
	/**
	 * @param full_path
	 * @return
	 * @throws FatalException 
	 */
	public String getSecureDownloadURL(boolean full_path) throws FatalException {
		String retour;
		retour = "securedownload?oid=" + this.oidsaada;
		if( full_path ) {
			return Database.getUrl_root() + "/" + retour;
		}
		else {
			return retour;
		}
	}	

	/**
	 * @param filename
	 * @return
	 */
	public String getMimeType(String filename) {
		if( filename == null ) {
			return "";
		}
		else if( filename.matches(RegExp.FITS_FILE)) {
			return "application/fits";						
		}
		else if( filename.matches(RegExp.VOTABLE_FILE)) {
			return "application/x-votable+xml";						
		}
		else {
			return "text/html";												
		}
	}
	/**
	 * @return
	 */
	public String getMimeType() {
		return getMimeType(null);
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	public long getFileSize() throws SaadaException {
		try {
			File f = new File(this.getRepositoryPath());
			return f.length();
		} catch (FatalException e) {
			return SaadaConstant.LONG;
		}
	}

	/**
	 *  @return
	 * @throws SaadaException 
	 */
	public String getFileName() throws SaadaException {
		try {
			File f = new File(this.getRepositoryPath());
			return f.getName();
		} catch (FatalException e) {
			return null;
		}
	}

	/**
	 * Returns the full path of the repository file
	 * @return
	 * @throws SaadaException
	 */
	public String getRepositoryPath() throws SaadaException {
		if( this.getProduct_url_csa() == null ) {
			return null;
		}
		else if( this.getProduct_url_csa().indexOf(Database.getSepar()) != -1 ) {
			return this.getProduct_url_csa();
		} else {
			return Database.getRepository() + Database.getSepar() 
			+ this.getCollection().getName() + Database.getSepar() 
			+ Category.explain(this.getCategory()).toUpperCase() + Database.getSepar() 
			+ this.getProduct_url_csa();
		}
	}
	/***
	 * returns the name of the repository file
	 * @return
	 * @throws SaadaException
	 */
	public String getRepositoryName() throws SaadaException {
		if( this.getProduct_url_csa() == null ) {
			return null;
		}
		else if( this.getProduct_url_csa().indexOf(Database.getSepar()) == -1 ) {
			return this.getProduct_url_csa();
		} else {
			return new File(Database.getRepository() + Database.getSepar() 
			+ this.getCollection().getName() + Database.getSepar() 
			+ Category.explain(this.getCategory()).toUpperCase() + Database.getSepar() 
			+ this.getProduct_url_csa()).getName();
		}
	}
	
	/**
	 * @return
	 */
	public String getVignetteName() throws Exception{
		if( this.vignetteFile == null ) {
			this.setVignetteFile();
		}
		return this.vignetteFile.getName();
	}
	/**
	 * @return
	 * @throws IOException 
	 */
	public String getVignettePath() throws Exception{
		if( this.vignetteFile == null ) {
			this.setVignetteFile();
		}
		return this.vignetteFile.getPath();
	}

	public void setVignetteFile() throws FatalException, IOException, SaadaException{
		this.vignetteFile = new VignetteFile(Database.getRepository() 
				+ File.separator + this.getCollection().getName() 
				+ File.separator + Category.explain(this.getCategory()) 
				, new File(this.getRepositoryPath()).getName() );
	}

	/**
	 * @param name
	 * @throws AbortException 
	 */
	public abstract void setProduct_url_csa(String name) throws AbortException ;
	/**
	 * @return
	 */
	public abstract String getProduct_url_csa() ;

	/*
	 * Data model implementation
	 */
	/**
	 * Activate the datamodel dm_name for this instance
	 * @param dm_name
	 * @throws Exception
	 */
	public void activateDataModel(String dm_name) throws FatalException {
		/*
		 * http://mail-archives.apache.org/mod_mbox/commons-user/200512.mbox/%3C200512281543.jBSFh6J3015001@fidel.intranet.ef.pt%3E
		 * Inner class constructors have an hidden parameter managed by the compiler. This parameter is the reference to the outer object.
		 * For this reason we can not build an inner instance by using Class.newInstance() but by using
		 * a reference of the inner class constructor
		 * A lot of forum people say the this operation is not possible
		 */
		/*
		 * Try with dm_name as a Datamodel name or as a Java encoded name
		 */
		try {
			Constructor constructor = Class.forName(this.getClass().getName() + "$" + dm_name).getDeclaredConstructor(new Class[] { this.getClass()});
			this.current_dm_interface = (DMInterface) constructor.newInstance(new Object[] { this });	
		} catch(Exception e0) {
			try {
				Constructor constructor = Class.forName(this.getClass().getName() + "$" + VOResource.getJavaName(dm_name)).getDeclaredConstructor(new Class[] { this.getClass()});
				this.current_dm_interface = (DMInterface) constructor.newInstance(new Object[] { this });	
			} catch(Exception e) {
				FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "Can not activate data model <" + dm_name + "> for objects of class " + this.getClass().getName());
			}
		}
	}
	/**
	 * Desactivate the data model usage for the instance
	 */
	public void desactivateDataModel() {
		this.current_dm_interface = null;
	}
	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getName()
	 */
	public String getDMName() throws FatalException {
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null;
		}
		else {
			return this.current_dm_interface.getDMName();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getSQLField(java.lang.String)
	 */
	public String getSQLField(String utype_or_nickname)  throws FatalException{
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null; 
		}
		else {
			return this.current_dm_interface.getSQLField(utype_or_nickname);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getSQLAlias(java.lang.String)
	 */
	public String getSQLAlias(String utype) throws FatalException{ 
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null;
		}
		else {
			return this.current_dm_interface.getSQLAlias(utype);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getSQLFields()
	 */
	public LinkedHashMap<String, String> getSQLFields()  throws FatalException{
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null;
		}
		else {
			return this.current_dm_interface.getSQLFields();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getDMFieldValue(java.lang.String)
	 */
	public Object getDMFieldValue(String utype_or_nickname) throws FatalException {
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null;
		}
		else {
			this.loadBusinessAttribute();
			return this.current_dm_interface.getDMFieldValue(utype_or_nickname);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.meta.DMInterface#getFieldValue(java.lang.String, java.sql.ResultSet)
	 */
	public Object getFieldValue(String utype_or_nickname, SaadaQLResultSet srs)  throws FatalException{
		if( this.current_dm_interface == null ) {
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, "No active DM for objects of class " + this.getClass().getName());
			return null;
		}
		else {
			return this.current_dm_interface.getFieldValue(utype_or_nickname, srs);
		}
	}


	/**
	 * Attempt to re-build the loader configuration from the class description stored in saada_class table.
	 * Can be confusing if multiple configuration have been used to load one class.
	 * Must be replaced with the storage of some relevant parameters at product level (e.g. extension#))
	 * @return
	 * @throws SaadaException
	 */
	public ConfigurationDefaultHandler getLoaderConfig() throws SaadaException {
		if( this.getCategory() == Category.ENTRY ) {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not get loader configuration for entries: try with the table");
			return null;
		}
		MetaClass mc = Database.getCachemeta().getClass(SaadaOID.getClassNum(this.oidsaada));
		String description = mc.getDescription().trim();
		Pattern p = Pattern.compile("ArgsParser\\((.*)\\)");
		Matcher m = p.matcher(description);
		if( m.find() && m.groupCount() == 1 ) {
			switch (this.getCategory()) {
			case Category.MISC: return new ConfigurationMisc("Read in DB", new ArgsParser( m.group(1).trim().split(" ")));
			case Category.IMAGE: return new ConfigurationImage("Read in DB", new ArgsParser( m.group(1).trim().split(" ")));
			case Category.SPECTRUM: return new ConfigurationSpectrum("Read in DB", new ArgsParser( m.group(1).trim().split(" ")));
			case Category.TABLE: return new ConfigurationTable("Read in DB", new ArgsParser( m.group(1).trim().split(" ")));
			case Category.FLATFILE: return new ConfigurationFlatfile("Read in DB", new ArgsParser( m.group(1).trim().split(" ")));
			default: FatalException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Unknown category <" + this.getCategory() + ">");
			}
		}
		else {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not read configuration for class " + mc.getName() + " in SQL table saada_class");			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retour = "";
		try {
			this.loadBusinessAttribute();
			if( this.current_dm_interface != null ) {
				String dm_name = this.getDMName();
				this.loadBusinessAttribute();
				LinkedHashMap<String, String> sf = this.getSQLFields();
				retour = "View with DM " + dm_name + "\nOID = " + this.oidsaada;
				for( Entry<String, String> e: sf.entrySet()) {
					retour += "\n" + e.getKey() + " (" + e.getValue() + ") = " + this.getDMFieldValue(e.getKey());
				}
				return retour;
			}
			else {
				retour = "Native Attribute\nCollection: " + this.getCollection().getName() 
				+ " Category: " + Category.explain(this.getCategory()) 
				+ " class: " + this.getSaadaClass().getName() + "\n";

				Field[] fs = this.getClass().getFields();
				for( int i=0 ; i< fs.length ; i++ ) {
					Field f = fs[i];
					retour += f.getName() + " (" + f.getType() + "): " + f.get(this) + "\n";
				}
				return retour;
			}
		} catch (Exception e2) {
			return e2.getMessage();
		}
	}


	public static void main(String[] args) throws Exception {
		Database.init("XIDResult");
		Query q = new Query("Select ENTRY From * In * ");
		q.setDM("XIDSrcModel");
		SaadaQLResultSet srs = q.runQuery("Select ENTRY From * In * ");
		while(srs.next()) {
			SaadaInstance si = Database.getCache().getObject(srs.getOid());
			//			System.out.println(si);
			si.activateDataModel("XIDSrcModel");
			//System.out.println(si);
			System.out.println(si.getOid() + " " + si.getDMFieldValue("E_Mag"));
		}
	}
}
