package saadadb.configuration;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaRelation;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

/* * @version $Id$

 * TODO class to be replaced with MetaRelation
 */
public class RelationConf {
	private String relation_name="";
	private String primary_coll_name="";
	private int primary_coll_type;
	private String secondary_coll_name="";
	private int secondary_coll_type;
	private String query="";
	private String description="";
	private LinkedHashMap<String, String> qualifier =new LinkedHashMap<String, String>();

	private String class_name;

	/**
	 * Build a relation configuration without going through an XML file
	 * @param relation_name
	 * @param primary_coll_name
	 * @param primary_coll_type
	 * @param secondary_coll_name
	 * @param secondary_coll_type
	 * @param query
	 * @param qualifiers
	 * @param description
	 * @throws FatalException
	 */
	public RelationConf(String relation_name, String primary_coll_name, String primary_coll_type
			, String secondary_coll_name, String secondary_coll_type
			, String query, String[] qualifiers, String description) throws FatalException {

		Database.getCachemeta().getCollectionTableName(primary_coll_name, Category.getCategory(primary_coll_type));
		Database.getCachemeta().getCollectionTableName(secondary_coll_name, Category.getCategory(secondary_coll_type));
		if( !relation_name.matches(RegExp.COLLNAME) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Relation name must match [_a-zA-Z][_a-zA-Z0-9]*");
		}
		if( Database.getCachemeta().getRelation(relation_name) != null ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Relation " + relation_name + " already exists");			
		}
		this.relation_name=relation_name;
		this.primary_coll_name=primary_coll_name;
		this.primary_coll_type=Category.getCategory(primary_coll_type);
		this.secondary_coll_name=secondary_coll_name;
		this.secondary_coll_type=Category.getCategory(secondary_coll_type);
		this.query=query;
		this.description=description;
		if( qualifiers != null ) {
			for( String q: qualifiers) {
				this.qualifier.put(q, "double");
			}
		}

	}

	public RelationConf(String relation_name) throws FatalException {
		MetaRelation mr = Database.getCachemeta().getRelation(relation_name);
		if( mr == null ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Relation " + relation_name + " does not exist");			
		}

		this.relation_name = relation_name;
		this.primary_coll_name = mr.getPrimary_coll();
		this.primary_coll_type = mr.getPrimary_category();
		this.secondary_coll_name = mr.getSecondary_coll();
		this.secondary_coll_type = mr.getSecondary_category();
		this.query = mr.getCorrelator();
		this.description        = mr.getDescription();
		for( String q:mr.getQualifier_names()) {
			this.qualifier.put(q, "double");			
		}		
	}

	public RelationConf() {
	}

	public void setNameRelation(String _name)
	{
		this.relation_name+=_name;
	}

	public String getNameRelation()
	{
		return this.relation_name;
	}


	public void setColPrimary_name(String _nameCol)
	{
		this.primary_coll_name+=_nameCol;
	}

	public String getColPrimary_name()
	{
		return this.primary_coll_name;
	}

	public void setColPrimary_type(int _type)
	{
		this.primary_coll_type=_type;
	}

	public int getColPrimary_type()
	{
		return this.primary_coll_type;
	}

	public void setColSecondary_name(String _nameCol)
	{
		this.secondary_coll_name+=_nameCol;
	}

	public String getColSecondary_name()
	{
		return this.secondary_coll_name;
	}

	public void setColSecondary_type(int _type)
	{
		this.secondary_coll_type =_type;
	}

	public int getColSecondary_type()
	{
		return this.secondary_coll_type;
	}

	public void setClass_name(String _class)
	{
		this.class_name+=_class;
	}

	public String getClass_name()
	{
		return this.class_name;
	}

	public void setQualifier(String name, String type)
	{
		this.qualifier.put(name,type);
	}

	public LinkedHashMap<String, String> getQualifier()
	{
		return this.qualifier;
	}


	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	public void save() throws Exception {
		String fn = Repository.INDEXRELATIONS_PATH + Database.getSepar() + this.getNameRelation() + ".xml";
		Messenger.printMsg(Messenger.TRACE, "Save relation description in " + fn);
		FileWriter fw = new FileWriter(fn);
		fw.write("<?xml version=\"1.0\" encoding=\"iso-8859-1\" standalone=\"no\"?>\n");
		fw.write("<!DOCTYPE relation SYSTEM \"" + Database.getRoot_dir() + Database.getSepar() + "config" + Database.getSepar() + "relation.dtd\">\n");
		fw.write("<relation>\n");
		fw.write("  <N_M>\n");
		fw.write("    <relation_name>" + this.getNameRelation() + "</relation_name>\n");
		fw.write("      <description/>" + this.description +"\n");
		fw.write("      </description>\n");
		fw.write("      <primary_coll name=\"" + this.primary_coll_name + "\" type=\"" + Category.explain(this.primary_coll_type) + "\"/>\n");
		fw.write("      <secondary_coll name=\"" + this.secondary_coll_name + "\" type=\"" + Category.explain(this.secondary_coll_type) + "\"/>\n");
		fw.write("      <algorithm>\n");
		fw.write("        <class_name></class_name>\n");
		fw.write("        <query><![CDATA[" + this.query + "]]></query>\n");
		for(Entry<String, String> e: this.qualifier.entrySet() ) {
			fw.write("        <qualifier name =\"" + e.getKey() + "\" type=\"" + e.getValue() + "\"/>\n");			
		}
		fw.write("     </algorithm>\n");
		fw.write("  </N_M>\n");
		fw.write("</relation>\n");
		fw.close();
	}


	/**
	 * @return Returns the query.
	 */
	public String getQuery() {
		/*
		 * Can start with a \n to make XML style more fancy
		 */
		return query.trim();
	}

	/**
	 * @param query The query to set.
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	/**
	 * @param append query substring to query (invoked while reading CDATA)
	 */
	public void appendToQuery(String query) {
		if( this.query == null )
			this.query = query;
		else {
			this.query += query;			
		}
	}


}

