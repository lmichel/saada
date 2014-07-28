package saadadb.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.enums.ClassifierMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.sqltable.SQLQuery;
import saadadb.util.Messenger;



/** * @version $Id$

 * @author michel
 * 12/2009: add method getUCDField
 * 05/2001: add methods getUCDs
 */
public class MetaClass extends MetaObject{

	private final LinkedHashMap<String,AttributeHandler> attribute_handlers;
	private String[] attribute_names;
	private int  category;
	private String collection_name;
	private int collection_id;
	private ClassifierMode mapping_type;
	private String signature;
	private String associate_class;
	private String description;
	private boolean has_instances=false;

	/**
	 * @param name
	 * @throws SQLException
	 */
	public MetaClass(String name) {
		super(name, -1);
		attribute_handlers    = new LinkedHashMap<String, AttributeHandler>();
	}

	/**
	 * @param rs
	 * @param cat
	 * @param collName : given to workaround the class without attribute (the collmane is usually taken from the ah)
	 * @throws Exception
	 */
	public MetaClass(ResultSet rs, int cat, String collName) throws Exception {
		super(rs.getString("cname"), -1);
		attribute_handlers    = new LinkedHashMap<String, AttributeHandler>();
		String t = rs.getString("mapping_type");
		if( t.equals(ClassifierMode.CLASSIFIER)) this.mapping_type = ClassifierMode.CLASSIFIER;
		else  this.mapping_type = ClassifierMode.CLASS_FUSION;
		this.id = rs.getInt("cclass_id");
		this.category = cat;
		this.collection_id = rs.getInt("ccollection_id");
		this.collection_name = collName;
		this.signature = rs.getString("signature");
		this.associate_class = rs.getString("associate_class");
		this.description = rs.getString("description");
	}

	/**
	 * add the attribute handler read in the current row of rs
	 * @param rs
	 * @throws FatalException
	 */
	public void readAttribute(ResultSet rs) throws FatalException  {
		try {
			/*
			 * Do not prohibit classes with no attributes
			 */
			if( rs.getString("name_attr") != null ) {
				AttributeHandler ah = new AttributeHandler(rs);
				attribute_handlers.put(ah.getNameattr()  , ah);
			}
		} catch(Exception  e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.DB_ERROR, e);
		}
	}

	/**
	 * Make an internal join in the collection attribute table (saada_metacoll_*)
	 * to build links between attribute handlers and their associated errors
	 * @throws SQLException
	 */
	public void bindAssociatedAttributeHandler() throws Exception {
		if( this.associate_class != null && this.associate_class.length() > 0 ) {
			SQLQuery squery = new SQLQuery();
			ResultSet rs = squery.run("SELECT mc.name_attr, mc.ass_error, mc2.name_attr "
					+ " FROM saada_metaclass_" + Category.explain(this.category).toLowerCase() + " mc, saada_metaclass_" + Category.explain(this.category).toLowerCase() + " mc2 "
					+ " WHERE mc.name_coll = '" + this.name + "' "
					+ " AND mc.ass_error IS NOT NULL  AND mc2.pk = mc.ass_error" );
			while (rs.next()) {
				AttributeHandler ah_prim = attribute_handlers.get(rs.getString(1));
				AttributeHandler ah_ass = attribute_handlers.get(rs.getString(3));
				ah_prim.setAss_error(ah_ass);	
			}
			squery.close();
		}
	}

	/**
	 * @throws FatalException 
	 * @throws QueryException 
	 * 
	 */
	public void checkForInstances() throws Exception{
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("Select filename from saada_loaded_file where classname = '" + name + "' limit 1");
		/*
		 * WHile there is no statistic management
		 */
		this.has_instances = false;
		while( rs.next() ) {
			this.has_instances = true;
			squery.close();
			return;
		}
		squery.close();
	}

	/**
	 * Returns the first field with the ucd. If queriable_only is true, only queriable fields are considered
	 * @param ucd
	 * @param queriable_only
	 * @return
	 */
	public AttributeHandler getUCDField(String ucd, boolean queriable_only) {
		for( AttributeHandler ah: attribute_handlers.values() ) {
			if( ucd.equals(ah.getUcd()) ) {
				if( queriable_only ) {
					if( ah.isQueriable() ) {
						return ah;
					}
				}
				else {
					return ah;
				}
			}
		}
		return null;
	}

	/**
	 * return AttributeHandlers with UCDs
	 * @param queriable_only : return only queriables AH if true
	 * @return
	 */
	public AttributeHandler[] getUCDFields(boolean queriable_only) {
		ArrayList<AttributeHandler> retour = new ArrayList<AttributeHandler>();
		for( AttributeHandler ah: attribute_handlers.values() ) {
			if( ah.getUcd() != null && ah.getUcd().length() > 0 )  {
				if( (queriable_only && ah.isQueriable()) ||  !queriable_only) {
					retour.add(ah);
				}
			}
		}
		return retour.toArray(new AttributeHandler[0]);	
	}

	/**
	 * @return
	 */
	public AttributeHandler[] getClassAttributes() {
		return this.attribute_handlers.values().toArray(new AttributeHandler[0]);
	}
	public HashMap<String,AttributeHandler> getAttributes_handlers() {
		return (HashMap<String, AttributeHandler>) this.attribute_handlers.clone();
	}
	public final Set<String> getClassAttribute_names() {
		return this.attribute_handlers.keySet();
	}
	public final boolean attributeExists(String attrName){
		return this.attribute_handlers.containsKey(attrName);
	}


	/**
	 * @return
	 */
	public int getCategory() {
		return this.category;
	}

	/**
	 * @return
	 */
	public String getCategory_name() {
		try {
			return Category.explain(this.category);
		} catch (SaadaException e) {
			return null;
		}
	}
	/**
	 * @param collection The collection to get.
	 */
	public String getCollection_name() {
		return this.collection_name;
	}

	/**
	 * @param collection The collection to get.
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * @return Returns the collection_id.
	 */
	public int getCollection_id() {
		return collection_id;
	}

	/**
	 * @return
	 */
	public ClassifierMode getMapping_type() {
		return this.mapping_type ;
	}
	/**
	 * @return Returns the associate_class.
	 */
	public String getAssociate_class() {
		return associate_class;
	}

	/**
	 * @param prefix
	 * @throws SaadaException 
	 */
	public void show(String prefix) throws SaadaException {
		System.out.println(prefix + " class: " + this.getName() + " collec: " + this.getCollection_name() + " category: " + Category.explain(this.category));
		System.out.print(prefix + "  attributes: " );
		Iterator it = attribute_handlers.values().iterator();
		while( it.hasNext()) {
			System.out.print(((AttributeHandler)(it.next())).getNameattr() + " " );
		}
		System.out.println("");	
	}

	/**
	 * @return Returns the attribute_names.
	 */
	public String[] getAttribute_names() {
		return attribute_names;
	}

	/**
	 * @param attribute_names The attribute_names to set.
	 */
	public void setAttribute_names() {
		this.attribute_names = this.attribute_handlers.keySet().toArray(new String[0]);
	}

	/**
	 * @return
	 * @throws FatalException 
	 */
	public boolean hasInstances() throws SaadaException {
		return this.has_instances;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns ths size of the class table 
	 * @return
	 * @throws Exception
	 */
	public int getSize() throws Exception{
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("SELECT count(oidsaada) FROM " + this.name);
		int retour = 0;
		while(rs.next()) {
			retour = rs.getInt(1);
		}
		rs.close();
		return retour;
	}


	@Override
	public String toString() {
		return "class " + this.name + " of collection " + this.getCollection_name()  + "." + this.getCategory_name();
	}
}
