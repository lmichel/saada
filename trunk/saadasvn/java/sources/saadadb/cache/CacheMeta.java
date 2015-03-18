package saadadb.cache;
import java.awt.Frame;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import saadadb.admintool.popups.PopupReloadCache;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.database.spooler.Spooler;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.meta.MetaCollection;
import saadadb.meta.MetaRelation;
import saadadb.meta.VOResource;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.Table_Saada_Relation;
import saadadb.util.Messenger;

/**
 * @author michel
 * 05/2011: add method getUCDs
 * 05/2011: add methods getRelationNamesStarting/Ending from/on  Class
 */
public class CacheMeta {

	private LinkedHashMap<String, MetaCollection> collections;
	private String[] collection_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_table;
	private String[] att_extend_table_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_entry;
	private String[] att_extend_entry_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_spectra;
	private String[] att_extend_spectrum_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_image;
	private String[] att_extend_image_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_misc;
	private String[] att_extend_misc_names;

	private LinkedHashMap<String, AttributeHandler> att_extend_flatfile;
	private String[] att_extend_flatfile_names;

	private String[] class_names;
	private LinkedHashMap<String, MetaClass> classes;

	private LinkedHashMap<String, MetaRelation> relations;
	private String[] relation_names;

	private boolean loaded = false;

	private String tables = "";
	/*
	 * Collection attributes not published by TAP
	 */
	private static Set<String> ignoreCollAttrs = null;


	public CacheMeta() throws FatalException {
		if( ignoreCollAttrs == null ) {
			ignoreCollAttrs = new LinkedHashSet<String>();
			ignoreCollAttrs.add("date_load");
			ignoreCollAttrs.add("product_url_csa");
			ignoreCollAttrs.add("sky_pixel_csa");
			ignoreCollAttrs.add("healpix_csa");
			ignoreCollAttrs.add("oidtable");
			ignoreCollAttrs.add("shape_csa");
			ignoreCollAttrs.add("date_load");
			ignoreCollAttrs.add("y_colname_csa");
			ignoreCollAttrs.add("x_colname_csa");
			ignoreCollAttrs.add("y_min_csa");
			ignoreCollAttrs.add("y_max_csa");
			ignoreCollAttrs.add("y_max_csa");
		}
		this.reload(true);
	}


	/**
	 * @param force
	 * @throws FatalException 
	 * @throws Exception 
	 */
	public void reload(boolean force) throws FatalException  {
		boolean dm = Messenger.debug_mode;
		Messenger.debug_mode = false;
		if (this.loaded == false || force == true) {
			Messenger.printMsg(Messenger.TRACE, "Reload cache meta");
			try {
				this.loadAllExtAttr();
				this.loadClasses();
				this.loadCollections();
				this.loadRelations();
				this.loaded = true;
			} catch(Exception e) {
				Messenger.debug_mode = dm;
				Messenger.printStackTrace(e);
				FatalException.throwNewException(SaadaException.METADATA_ERROR, e);
			}
		}
		Messenger.debug_mode = dm;
	}
	
	// Method used in Admintool to display a popup to inform the user that the cache is reloading
	public void reloadGraphical(Frame frame, boolean force) throws FatalException
	{
		Messenger.printMsg(Messenger.DEBUG, "Graphical reload");
		PopupReloadCache prc = new PopupReloadCache(frame);
		prc.showPopup();
		this.reload(force);
		prc.hipePopup();
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	private void loadAllExtAttr() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load extended attributes for collections");
		HashMap<String, AttributeHandler> dest=null;
		String table=null;
		for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
			switch(cat) {
			case Category.ENTRY: table = "saada_metacoll_entry";
			this.att_extend_entry = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_entry;
			break;
			case Category.TABLE: table = "saada_metacoll_table";
			this.att_extend_table = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_table;
			break;
			case Category.IMAGE: table = "saada_metacoll_image";
			this.att_extend_image = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_image;
			break;
			case Category.SPECTRUM: table = "saada_metacoll_spectrum";
			this.att_extend_spectra = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_spectra;
			break;
			case Category.MISC: table = "saada_metacoll_misc";
			this.att_extend_misc = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_misc;
			break;
			case Category.FLATFILE: table = "saada_metacoll_flatfile";
			this.att_extend_flatfile = new LinkedHashMap<String, AttributeHandler>();
			dest = this.att_extend_flatfile;
			break;
			}
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("select * from " + table + " where level = 'E'");
			while( rs.next()) {
				AttributeHandler ah = new AttributeHandler(rs);
				dest.put(ah.getNameattr(), ah);
			}
			squery.close();
		}

		this.att_extend_entry_names    = this.att_extend_entry.keySet().toArray(new String[0]);
		this.att_extend_table_names    = this.att_extend_table.keySet().toArray(new String[0]);
		this.att_extend_image_names    = this.att_extend_image.keySet().toArray(new String[0]);
		this.att_extend_spectrum_names = this.att_extend_spectra.keySet().toArray(new String[0]);
		this.att_extend_misc_names     = this.att_extend_misc.keySet().toArray(new String[0]);
		this.att_extend_flatfile_names = this.att_extend_flatfile.keySet().toArray(new String[0]);
	}

	/**
	 * @return
	 */
	private void loadCollections() throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load collections");
		this.collections = new LinkedHashMap<String, MetaCollection>(); 
		for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
			String str_cat = Category.NAMES[cat].toLowerCase();
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT c.id as id_collect, c.name as name_collec, c.description as  description, mc.* "
					+ "  FROM saada_collection c, saada_metacoll_" + str_cat + " mc "
					+ "  ORDER by c.id, mc.pk");
			String last_collname = "";
			String collname = "";
			MetaCollection mc = null;
			while ( rs.next() ) {
				collname = rs.getString("name_collec");
				if( !last_collname.equals(collname) ) {
					if( (mc = this.collections.get(collname)) == null ) {
						mc = new MetaCollection(collname);	
						this.collections.put(mc.getName(), mc);
					}
					last_collname = collname;
				}
				mc.update(rs, cat);
			}			
			squery.close();
		}
		this.collection_names = (this.collections.keySet().toArray(new String[0]));
		for( MetaCollection mc: this.collections.values() ) {
			mc.bindAssociatedAttributeHandler();
			mc.lookAtFlatfiles();					
		}
	}

	/**
	 * @return
	 */
	private void loadClasses() throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load classes");
		this.classes = new LinkedHashMap<String, MetaClass>(); 
		for( int cat=1 ; cat<Category.NB_CAT ; cat++ ) {
			String str_cat = Category.NAMES[cat].toLowerCase();

			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT c.class_id as cclass_id, c.mapping_type, c.signature, c.associate_class, c.description, mc.* "
					+ "  FROM saada_class c, saada_metaclass_" + str_cat + " mc "
					+ "  WHERE c.class_id = mc.class_id "
					+ "  ORDER by c.class_id, mc.pk");
			String last_classname = "";
			String classname = "";
			MetaClass mc = null;
			while ( rs.next() ) {
				classname = rs.getString("name_class");
				if( !last_classname.equals(classname) ) {
					/*
					 * create the new metaclass
					 */
					if( (mc = this.classes.get(classname)) == null ) {
						mc = new MetaClass(classname);						
						this.classes.put(mc.getName(), mc);
					}
					last_classname = classname;
				}
				mc.update(rs, cat);
			}	
			squery.close();
			/*
			 * SQLIte cannot make nested queries since the spooler supports once connection at the time
			 */
			for( MetaClass mcl: this.classes.values()) {
				mcl.checkForInstances();
			}
		}
		/*
		 * Just kep a direct reference to the keyset of attribute names
		 */
		for( MetaClass mc2: this.classes.values() ) {
			mc2.setAttribute_names();
		}
		Iterator<MetaClass> it = this.classes.values().iterator();
		while( it.hasNext()) {
			((MetaClass)(it.next())).bindAssociatedAttributeHandler();
		}
		this.class_names = (this.classes.keySet().toArray(new String[0]));
	}

	/**
	 * @param mc
	 * @return
	 * @throws FatalException
	 */
	private  String generateXMLTable(MetaClass mc) throws FatalException{
		String xml = "\t\t<table xsi:type=\"output\">\n\t\t\t<name>"+mc.getName()+"</name>\n\t\t\t<description>"+mc.getDescription()+"</description>";
		Collection<AttributeHandler> coll = this.getCollection(mc.getCollection_name()).getAttribute_handlers(mc.getCategory()).values();
		for(AttributeHandler ah : coll) {
			if( !ignoreCollAttrs.contains(ah.getNameattr())) {
				xml += "\n\t\t\t<column>\n\t\t\t\t<name>"
					+ah.getNameattr()+"</name>\n\t\t\t\t<description><![CDATA["
					+ah.getComment()+"]]></description>"
					+"\n\t\t\t\t<unit>"
					+ah.getUnit()+"</unit>\n\t\t\t\t<ucd>"
					+ah.getUcd()+"</ucd>\n\t\t\t\t<utype>"
					+ah.getUtype()+"</utype>"
					+"\n\t\t\t\t<dataType xsi:type=\"vod:TAPType\">"
					+((ah.getType().equalsIgnoreCase("String"))?"varchar":ah.getType()).toUpperCase()+"</dataType>\n\t\t\t</column>";
			}
		}

		coll = mc.getAttributes_handlers().values();
		for(AttributeHandler ah : coll)
			xml += "\n\t\t\t<column>\n\t\t\t\t<name>"
				+ah.getNameattr()+"</name>\n\t\t\t\t<description><![CDATA["
				+ah.getComment()+"]]></description>"
				+"\n\t\t\t\t<unit>"
				+ah.getUnit()+"</unit>\n\t\t\t\t<ucd>"
				+ah.getUcd()+"</ucd>\n\t\t\t\t<utype>"
				+ah.getUtype()+"</utype>"
				+"\n\t\t\t\t<dataType xsi:type=\"vod:TAPType\">"
				+((ah.getType().equalsIgnoreCase("String"))?"varchar":ah.getType()).toUpperCase()+"</dataType>\n\t\t\t</column>";
		return xml+"\n\t\t</table>";
	}

	/**
	 * @return
	 */
	private void loadRelations() throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Load relations");
		this.relations = new LinkedHashMap<String, MetaRelation>();
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("select * from " + Table_Saada_Relation.tableName);
		while (rs.next()) {
			MetaRelation mr = new MetaRelation(rs);
			this.relations.put(mr.getName(), mr);
		}
		squery.close();
		this.relation_names = (this.relations.keySet().toArray(new String[0]))	;
		//Out of the creator in order to avoid nested queries not supported by SQLITE		
		for( MetaRelation mr: this.relations.values()){
			mr.setQualifiers();
		}
	}

	public boolean isCollectionFilled(String collection_name) throws FatalException
	{
		boolean res = this.getClassesOfCollection(collection_name, Category.TABLE).length>0 
		|| this.getClassesOfCollection(collection_name, Category.ENTRY).length>0 
		|| this.getClassesOfCollection(collection_name, Category.IMAGE).length>0
		|| this.getClassesOfCollection(collection_name, Category.SPECTRUM).length>0
		|| this.getClassesOfCollection(collection_name, Category.MISC).length>0
		|| this.getCollection(collection_name).hasFlatFiles();
		return res;
	}

	/**
	 * @param collection (null for all collections)
	 * @param Category (-1 for all categories)
	 * @return
	 */
	public String[] getClassNames(String collection, int category){

		/*
		 * return all classes
		 */
		if( (collection == null || collection.equals("")) && (category == -1) ) {
			return this.class_names;			
		}
		/*
		 * return all classes matching the given category
		 */
		else if( (collection == null || collection.equals("")) && category != -1 ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.class_names.length ; i++) {
				MetaClass mc = (this.classes.get(this.class_names[i]));
				if( category == mc.getCategory() ) {
					retour.add(this.class_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return all classes matching the given collection
		 */
		else if( category < 0  && collection != null && !collection.equals("") ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.class_names.length ; i++) {
				MetaClass mc = (this.classes.get(this.class_names[i]));
				if( collection.equals(mc.getCollection_name()) ) {
					retour.add(this.class_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return classes matching the given collection and the given category
		 */
		else {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.class_names.length ; i++) {
				MetaClass mc = (this.classes.get(this.class_names[i]));
				if( collection.equals(mc.getCollection_name()) && category == mc.getCategory() ) {
					retour.add(this.class_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
	}

	/**
	 * @param collection
	 * @param Category
	 * @return
	 */
	public String[] getRelationNamesStartingFromColl(String collection, int category) {
		/*
		 * return all relations
		 */
		if( (collection == null || collection.equals("")) && category < 0 ) {
			return this.relation_names;			
		}
		/*
		 * return all relations starting from data matching the given category
		 */
		else if( (collection == null || collection.equals("")) && category >= 0 ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));
				if( category == mr.getPrimary_category() || category == mr.getPrimary_category() 
						|| mr.getPrimary_category() == category) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return all relations starting from the given collection
		 */
		else if( category <= 0 && collection != null && !collection.equals("") ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));
				if( collection.equals(mr.getPrimary_coll()) ) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return relation starting from the collection data matching the given category
		 */
		else {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));
				if( collection.equals(mr.getPrimary_coll()) &&  category == mr.getPrimary_category() ) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
	}


	/**
	 * @param collection
	 * @param Category
	 * @return
	 */
	public String[] getRelationNamesEndingOnColl(String collection, int category) {
		/*
		 * return all relations
		 */
		if( (collection == null || collection.equals("")) && category <= 0 ) {
			return this.relation_names;			
		}
		/*
		 * return all relations starting from data matching the given category
		 */
		else if( (collection == null || collection.equals("")) && category <= 0 ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));
				if( category == mr.getSecondary_category() ) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return all realtion sarting from the given collection
		 */
		else if( category <= 0  && collection != null && !collection.equals("") ) {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));
				if( collection.equals(mr.getSecondary_coll()) ) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
		/*
		 * return relation starting from the collection data matching the given category
		 */
		else {
			ArrayList<String> retour = new ArrayList<String>();
			for( int i=0 ; i<this.relation_names.length ; i++) {
				MetaRelation mr = (this.relations.get(this.relation_names[i]));

				if( collection.equals(mr.getSecondary_coll()) && 
						category == mr.getSecondary_category() ) {
					retour.add(this.relation_names[i]);					
				}
			}
			return (retour.toArray(new String[0]));
		}
	}

	/**
	 * @param classname
	 * @return
	 * @throws FatalException
	 */
	public String[] getRelationNamesStartingFromClass(String classname) throws FatalException {
		MetaClass mc = this.getClass(classname);
		return this.getRelationNamesStartingFromColl(mc.getCollection_name(), mc.getCategory());

	}
	/**
	 * @param classname
	 * @return
	 * @throws FatalException
	 */
	public String[] getRelationNamesEndingOnClass(String classname) throws FatalException {
		MetaClass mc = this.getClass(classname);
		return this.getRelationNamesEndingOnColl(mc.getCollection_name(), mc.getCategory());
	}

	/**
	 * @param category
	 * @return
	 */
	public final String[] getAtt_extend_names(int category){
		switch(category){
		case Category.ENTRY : return this.getAtt_extend_entry_names();
		case Category.IMAGE:return this.getAtt_extend_image_names();
		case Category.MISC:return this.getAtt_extend_misc_names();
		case Category.SPECTRUM:return this.getAtt_extend_spectrum_names();
		case Category.TABLE:return this.getAtt_extend_table_names();
		case Category.FLATFILE:return this.getAtt_extend_flatfile_names();
		}
		return null;
	}

	/**
	 * @return Returns the att_extend_entry_names.
	 */
	public String[] getAtt_extend_entry_names() {
		return att_extend_entry_names;
	}

	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public AttributeHandler getAtt_extend_entry(String name) throws FatalException {
		AttributeHandler ret =  (this.att_extend_entry.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "ENTRY extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}

	/**
	 * @return Returns the att_extend_image2d_names.
	 */
	public String[] getAtt_extend_image_names() {
		return att_extend_image_names;
	}
	/**
	 * @param name
	 * @return
	 */
	public AttributeHandler getAtt_extend_image(String name) throws FatalException{
		AttributeHandler ret =  (this.att_extend_image.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "IMAGE extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}


	/**
	 * @return Returns the att_extend_misc_names.
	 */
	public String[] getAtt_extend_misc_names() {
		return att_extend_misc_names;
	}
	/**
	 * @param name
	 * @return
	 */
	public AttributeHandler getAtt_extend_misc(String name) throws FatalException{
		AttributeHandler ret =  (this.att_extend_misc.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "MISC extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}

	/**
	 * @return Returns the att_extend_spectrum_names.
	 */
	public String[] getAtt_extend_spectrum_names() {
		return att_extend_spectrum_names;
	}
	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public AttributeHandler getAtt_extend_spectra(String name) throws FatalException {
		AttributeHandler ret =  (this.att_extend_spectra.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "SPECTRA extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}

	/**
	 * @return Returns the att_extend_table_names.
	 */
	public String[] getAtt_extend_table_names() {
		return att_extend_table_names;
	}
	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public AttributeHandler getAtt_extend_table(String name) throws FatalException {
		AttributeHandler ret =  (this.att_extend_table.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Table extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}
	/**
	 * @return Returns the att_extend_table_names.
	 */
	public String[] getAtt_extend_flatfile_names() {
		return att_extend_flatfile_names;
	}
	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public AttributeHandler getAtt_extend_flatfile(String name) throws FatalException {
		AttributeHandler ret =  (this.att_extend_flatfile.get(name));
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Flatfile extended attribute <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}


	/**
	 * @param name
	 * @return
	 * @throws SaadaException
	 */
	public AttributeHandler[] getClassAttributes(String name) throws FatalException {
		MetaClass mc = this.getClass(name);
		return mc.getClassAttributes();
	}

	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public MetaClass getClass(String name) throws FatalException{
		MetaClass ret = (this.classes.get(name));	
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Class <" + name + "> doesn't exist");
			return null;
		} else {
			return ret;
		}
	}

	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public boolean classExists(String name) {
		MetaClass ret = (this.classes.get(name));	
		if( ret == null ) {
			return false;
		} else {
			return true;
		}
	}
	/**
	 * @param name
	 * @return
	 */
	public boolean relationExists(String name) {
		MetaRelation ret = (this.relations.get(name));	
		if( ret == null ) {
			return false;
		} else {
			return true;
		}
	}

	public boolean classExistsIn(String className,String collName,int category){
		MetaClass mc = classes.get(className);
		if(mc==null) return false;
		return (mc.getCategory()==category && mc.getCollection_name().equals(collName));
	}
	/**
	 * @param id
	 * @return
	 */
	public String[] getClassesOfCollection(int id, int category) {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		ArrayList<String> retour = new ArrayList<String>();
		while (ie.hasNext()) {
			MetaClass mc = (MetaClass)(ie.next());
			if( mc.getCollection_id() == id && mc.getCategory() == category) {
				retour.add(mc.getName());
			}
		}
		return (retour.toArray(new String[0]));
	}


	/**
	 * @param name
	 * @param category
	 * @return
	 */
	public String[] getClassesOfCollection(String name, int category) {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		ArrayList<String> retour = new ArrayList<String>();

		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getCollection_name().equals(name) && mc.getCategory() == category ) {
				retour.add(mc.getName());
			}
		}
		return (retour.toArray(new String[0]));
	}


	/**
	 * @param id
	 * @return
	 * @throws SaadaException 
	 */
	public MetaClass getClass(int id) throws FatalException {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getId() == id ) {
				return mc;
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Could not get class with id <" + id + ">");
		return null;
	}


	/**
	 * Return the names of all classes having the signature
	 * @param signature
	 * @return
	 * @throws SaadaException
	 */
	public String[] getClassWithSignatureNames(String signature)  {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		ArrayList<String> retour = new ArrayList<String>();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getSignature().equals(signature) ) {
				retour.add(mc.getName());
			}
		}
		return retour.toArray(new String[0]);
	}

	/**
	 * @param probaKey
	 * @return
	 * @throws SaadaException 
	 */
	public MetaClass getClassWithSignature(String signature) throws FatalException {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getSignature().equals(signature) ) {
				return mc;
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Could not get class with signature <" + signature + ">");
		return null;
	}

	/**
	 * @param id
	 * @return
	 * @throws SaadaException 
	 */
	public boolean classExists(int id)  {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getId() == id ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param probaKey
	 * @return
	 * @throws SaadaException 
	 */
	public boolean classWithSignatureExists(String signature)  {
		Set entries = this.classes.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaClass mc = (MetaClass)(e.getValue());
			if( mc.getSignature().equals(signature)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 * @throws SaadaException 
	 */
	public MetaCollection getCollection(int id) throws FatalException {
		Set entries = this.collections.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaCollection mc = (MetaCollection)(e.getValue());
			if( mc.getId() == id ) {
				return mc;
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Could not get collection with id <" + id + ">");
		return null;
	}

	/**
	 * @param id
	 * @return
	 * @throws SaadaException 
	 */
	public boolean collectionExists(int id)  {
		Set entries = this.collections.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaCollection mc = (MetaCollection)(e.getValue());
			if( mc.getId() == id ) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public MetaCollection getCollection(String name) throws FatalException{
		MetaCollection ret = this.collections.get(name);
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Collection <" + name + "> doesn't exist");
			return null;
		}
		else {
			return ret;
		}
	}
	/**
	 * @param name
	 * @return
	 * @throws SaadaException 
	 */
	public boolean collectionExists(String name) {
		MetaCollection ret = this.collections.get(name);
		if( ret == null ) {
			return false;
		}
		else {
			return true;
		}
	}

	/**
	 * Return the name of the SQL table of the collection id for the category cat
	 * @param coll_name
	 * @param cat
	 * @return
	 * @throws SaadaException 
	 */
	public String getCollectionTableName(int id, int cat) throws FatalException {
		Set entries = this.collections.entrySet();
		Iterator ie = entries.iterator();
		while (ie.hasNext()) {
			Entry e = (Entry)(ie.next());
			MetaCollection mc = (MetaCollection)(e.getValue());
			if( mc.getId() == id ) {
				return mc.getName() + "_" + Category.explain(cat).toLowerCase();
			}
		}
		FatalException.throwNewException(SaadaException.METADATA_ERROR, "Could not get collection with id <" + id + ">");
		return null;
	}

	/**
	 * Return the name of the SQL table of the collection coll_name for the category cat
	 * @param coll_name
	 * @param cat
	 * @return
	 * @throws SaadaException 
	 */
	public String getCollectionTableName(String coll_name, int cat) throws FatalException {
		MetaCollection ret = this.collections.get(coll_name);
		if( ret == null ) {
			FatalException.throwNewException(SaadaException.METADATA_ERROR, "Collection <" + coll_name + "> doesn't exist");
			return null;
		}
		else {
			return Database.getWrapper().getCollectionTableName(coll_name, cat);
		}
	}

	/**
	 * @param name
	 * @return
	 */
	public MetaRelation getRelation(String name){
		return (this.relations.get(name));	
	}
	/**
	 * @return Returns the class_names.
	 */
	public String[] getClass_names() {
		return class_names;
	}

	/**
	 * @return Returns the collection_names.
	 */
	public String[] getCollection_names() {
		return collection_names;
	}

	/**
	 * @return Returns the relation_names.
	 */
	public String[] getRelation_names() {
		return relation_names;
	}

	/**
	 * @throws SaadaException
	 */
	public void showCollections() throws SaadaException {
		String[] coll = this.getCollection_names();
		System.out.println("* Collections");
		for( int i=0 ; i<coll.length ; i++ ) {
			MetaCollection mc = this.getCollection(coll[i]);
			System.out.println(" -" + coll[i] + " (" + mc.getId() + ")");
			mc.show("    ");
		}		
	}

	/**
	 * @throws SaadaException
	 */
	public void showClasses() throws SaadaException {
		String[] coll = this.getCollection_names();
		for(String colName:coll){
			System.out.println("* Collections: "+colName);
			System.out.println("** Categorie: ENTRY");
			for(String className:this.getClassesOfCollection(colName,Category.ENTRY)){
				System.out.println("*** Classes: "+className);

			}
		}		
	}



	/**
	 * @return Returns the att_extend_entry.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_entry() {
		return att_extend_entry;
	}

	/**
	 * @return Returns the att_extend_flatfile.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_flatfile() {
		return att_extend_flatfile;
	}

	/**
	 * @return Returns the att_extend_image.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_image() {
		return att_extend_image;
	}

	/**
	 * @return Returns the att_extend_misc.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_misc() {
		return att_extend_misc;
	}

	/**
	 * @return Returns the att_extend_spectra.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_spectra() {
		return att_extend_spectra;
	}

	/**
	 * @return Returns the att_extend_table.
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend_table() {
		return att_extend_table;
	}

	/**
	 * @param category
	 * @return
	 */
	public LinkedHashMap<String, AttributeHandler> getAtt_extend(int category) {
		switch(category) {
		case Category.ENTRY: return this.att_extend_entry;
		case Category.TABLE: return this.att_extend_table;
		case Category.IMAGE: return this.att_extend_image;
		case Category.SPECTRUM: return this.att_extend_spectra;
		case Category.MISC: return this.att_extend_misc;
		case Category.FLATFILE: return this.att_extend_flatfile;
		default: return null;
		}
	}


	/**
	 * Return the VOresource. VO Resources are not cached but read within the DB
	 * @param resource_name
	 * @return
	 * @throws FatalException 
	 * @throws QueryException 
	 */
	public VOResource getVOResource(String resource_name) throws SaadaException {
		try {
			return VOResource.getResource(resource_name);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, e);
			return null;
		}
	}

	/**
	 * Returns a table with all VO resources stored into the DB
	 * @return
	 * @throws FatalException 
	 */
	public String[] getVOResourceNames() throws Exception {
		ArrayList<String> retour = new ArrayList<String>();
		SQLQuery squery = new SQLQuery();

		ResultSet rs = squery.run("SELECT DISTINCT resource FROM saada_vo_resources");
		try {
			while( rs.next()) {
				retour.add(rs.getString(1));
			}
			squery.close();
			return retour.toArray(new String[0]);
		} catch (SQLException e) {
			squery.close();
			FatalException.throwNewException(SaadaException.MISSING_RESOURCE, e);
			return null;
		}
	}

	public AttributeHandler[] getUCDs(String collection, int category, boolean queriable_only) throws FatalException {
		ArrayList<AttributeHandler> retour = new ArrayList<AttributeHandler>();
		// Collection level UCD not supported by the query engine (1.6.0)
		//		for( AttributeHandler ah: this.getCollection(collection).getUCDs(category, queriable_only) ) {
		//			retour.add(ah);
		//		}
		String[] classes = getClassesOfCollection(collection, category);
		for( String cl: classes) {
			for( AttributeHandler ah: this.getClass(cl).getUCDFields(queriable_only) ) {
				retour.add(ah);				
			}
		}
		return retour.toArray(new AttributeHandler[0]);	
	}

	/**
	 * Returns true if name cane be used to name a Saada entity (collection, class or relation)
	 * @param name
	 * @param message
	 * @return
	 */
	public boolean isNameAvailable(String name, String message){
		if( name == null || name.length() == 0 ) {
			if( message != null ) message += "No name given";
			return false;
		}
		if( this.collectionExists(name)) {
			if( message != null ) message += "<" + name + "> already used for a collection";
			return false;			
		}
		if( this.classExists(name)) {
			if( message != null ) message += "<" + name + "> already used for a class";
			return false;			
		}
		if( this.relationExists(name)) {
			if( message != null ) message += "<" + name + "> already used for a relation";
			return false;			
		}
		try {
			if( Database.getWrapper().tableExist(name) ){
				if( message != null ) message += "<" + name + "> already used for a SQL table";
				return false;							
			}
			if( Database.getWrapper().tableExist(name.toLowerCase())){
				if( message != null ) message += "<" + name.toLowerCase() + "> already used for a SQL table";
				return false;							
			}
		} catch (Exception e) {
			if( message != null ) message += e.getMessage();
			return false;			
		}
		return true;
	}
}
