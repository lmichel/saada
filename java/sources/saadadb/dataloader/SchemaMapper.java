package saadadb.dataloader;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ClassifierMode;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.Entry;
import saadadb.products.Product;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Data;
import saadadb.sqltable.UCDTableHandler;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;

public abstract class SchemaMapper {

	protected static String separ = System.getProperty("file.separator");
	protected ProductMapping mapping;
	private static String class_location;
	private static String package_name;
	protected List<File> products;
	protected Product current_prd;
	protected MetaClass current_class;
	protected SchemaMapper entry_mapper = null;
	protected TreeSet<String> class_to_index = new TreeSet<String>();
	protected boolean build_index = true;
	protected Loader loader;

	/** * @version $Id: SchemaMapper.java 617 2013-06-18 13:37:23Z laurent.mistahl $

	 * @param products
	 * @param handler
	 */
	public SchemaMapper(Loader loader, Product prd) {
		setProduct(prd);
		this.loader = loader;
		package_name = "generated." + Database.getName();
		class_location = Database.getRoot_dir() + separ+ "class_mapping";
		this.build_index = !mapping.noIndex();
	}

	/**
	 * @param products
	 * @param handler
	 */
	public SchemaMapper(Loader loader, List<File> products, ProductMapping mapping) {
		this.loader = loader;
		this.mapping = mapping;
		this.products = products;
		package_name = "generated." + Database.getName();
		class_location = Database.getRoot_dir() + separ+ "class_mapping";
		this.build_index = !mapping.noIndex();
	}

	/**
	 * @param prd
	 */
	protected void  setProduct(Product prd) {
		this.mapping = prd.getMapping();
		this.current_prd= prd;		
	}

	/**
	 * @throws Exception
	 */
	protected void loadProduct() throws Exception {

		try {
			this.current_prd.setMetaclass(this.current_class);
			if( this.entry_mapper != null ) {
				this.entry_mapper.current_prd.setMetaclass(this.entry_mapper.current_class);
			}
			this.current_prd.loadValue();
			Messenger.printMsg(Messenger.TRACE, "Product file <" + current_prd + "> ingested, <OID = " + current_prd.getSaadainstance().oidsaada + ">");

		} catch (Exception ex) {
			Messenger.printStackTrace(ex);
			AbortException.throwNewException(SaadaException.INTERNAL_ERROR, ex);
		}	
	}


	/**
	 * Returns the class requested in the configuration or the default class name
	 * @param standardSybase
	 * @return
	 * @throws Exception
	 */
	protected String getRequestedClassName(String classname) throws Exception {
		String prefixe = "";
		String suffixe = "";
		int  categorySaada = this.mapping.getCategory();
		switch(categorySaada) {
		case Category.SPECTRUM:
			prefixe = "Spe";
			suffixe = "";
			break;
		case Category.IMAGE:
			prefixe = "Img";
			suffixe = "";
			break;
		case Category.MISC:
			prefixe = "Mis";
			suffixe = "";
			break;
		case Category.TABLE:
			prefixe = "Tbl";
			suffixe = "";
			break;
		case Category.ENTRY:
			prefixe = "Ent";
			suffixe = "Entry";
			break;
		}
		if( classname == null || classname.length() == 0 ) {
			classname = prefixe 
			+ "Cl_"
			+ this.mapping.name ;
		}

		/*
		 * Suffix is for associated class (table <-> entry e.g.)
		 */
		classname  +=  suffixe;
		return classname;
	}

	/**
	 * Returns the a classname derived from the the requested classname (class_name + _#)
	 * This feature is used by the classifier mode.
	 * @param standardSybase
	 * @return
	 * @throws Exception
	 */
	protected String getNewcLassName(String classname) throws Exception {

		String prefixe = "";
		String suffixe = "";
		int  categorySaada = this.mapping.getCategory();
		switch(categorySaada) {
		case Category.SPECTRUM:
			prefixe = "Spe";
			suffixe = "";
			break;
		case Category.IMAGE:
			prefixe = "Img";
			suffixe = "";
			break;
		case Category.MISC:
			prefixe = "Mis";
			suffixe = "";
			break;
		case Category.TABLE:
			prefixe = "Tbl";
			suffixe = "";
			break;
		case Category.ENTRY:
			prefixe = "Ent";
			suffixe = "Entry";
			break;
		}
		if( classname == null || classname.length() == 0 ) {
			classname = prefixe 
			+ "Cl_"
			+ this.mapping.name ;
		}
		SQLTable.lockTable("saada_class");
		SQLQuery squery = new SQLQuery();
		ResultSet rs = squery.run("SELECT count(*) FROM saada_class WHERE name " + Database.getWrapper().getRegexpOp() + " '^" + classname + "(_[0-9]+)?" + suffixe + "$'");
		int num=0;
		/*
		 * If a class exists with the requested name, _num is appended.
		 */
		while( rs.next()) {
			num = rs.getInt(1);
		}
		rs.close();
		squery.close();
		if( num > 0 ) {
			Messenger.printMsg(Messenger.TRACE, num + " classes with a name starting with " + classname + " found: take " + classname + "_" + num + " as class name");
			classname  +=  "_" + num  ;
		}
		/*
		 * Suffix is for associated class (table <-> entry e.g.)
		 */
		classname  +=  suffixe;
		return classname;
	}

	/**
	 * @return Returns the class_location.
	 */
	public static String getClass_location() {
		return class_location;
	}

	/**
	 * @return Returns the package_name.
	 */
	public static String getPackage_name() {
		return package_name;
	}

	/**
	 * @throws Exception
	 */
	abstract public void ingestProductSet() throws Exception ;
	/**
	 * @param product
	 * @param configuration
	 * @return
	 * @throws Exception
	 */
	protected MetaClass createClassFromProduct(ClassifierMode mode) throws Exception {


		String class_name;
		/*
		 * Compute the class name: 
		 * Here must be used the class prefix given by the future configuration
		 */
		String className = this.mapping.getClassName();
		if( className == null || className.length() == 0){
			className = this.current_prd.possibleClassName();
		}
		class_name = this.getNewcLassName(className);
		//class_name = "Aldebaran010";
		Messenger.printMsg(Messenger.TRACE,"Creation of the new class <" + class_name + ">");

		// Retrieve   attributeslist in the model
		Map<String, AttributeHandler> tableAttribute = this.current_prd.getProductAttributeHandler();
		// Create and load of  java class
		return createClassFromProduct(mode, class_name, tableAttribute);
	}

	/**
	 * @param mapping_type
	 * @param class_name
	 * @param tableAttribute
	 * @return
	 * @throws Exception
	 */
	public MetaClass createClassFromProduct(ClassifierMode mode, String class_name,  Map<String, AttributeHandler> tableAttribute) throws Exception {
		boolean dontforgettoreopentransaction = false;
		GenerationClassProduct.buildJavaClass(tableAttribute
				, SchemaMapper.getClass_location()
				, class_name
				, Category.explain(mapping.getCategory()) + "UserColl");
		@SuppressWarnings("rawtypes")
		Class cls = SaadaClassReloader.reloadGeneratedClass(class_name);
		// create SQL tables and update meta data
		try {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"insert into saada_class <" + class_name + ">");
			if( SQLTable.isTransactionOpen() ) {
				SQLTable.commitTransaction();
				dontforgettoreopentransaction = true;
			}
			SQLTable.beginTransaction();
			UCDTableHandler uth = new UCDTableHandler(class_name
					, mapping.getCollection()
					, mapping.getCategory()
					, new ArrayList<AttributeHandler>(tableAttribute.values()));
			int class_id = Table_Saada_Class.addClass(class_name
					, mapping.getCollection()
					, mapping.getCategory()
					, mapping.name
					, mode
					, MD5Key.getFmtsignature(tableAttribute)
					, mapping.loaderParams);
			/*
			 * Store the association between table class and entry class
			 */
			if( mapping.getCategory() == Category.ENTRY ) {
				Entry entry = (Entry)this.current_prd;
				String table_class_name = entry.getTable().getMetaclass().getName();
				Table_Saada_Class.setAssociateClass(class_name
						, mapping.getCollection() 
						, Category.ENTRY
						, table_class_name);
				Table_Saada_Class.setAssociateClass(table_class_name
						, mapping.getCollection()
						, Category.TABLE 
						, class_name);

			}
			// Creates a new table for this class in data base (with
			// all fields in this class)
			// Attention the table name in database is different
			// from the java class name (without package)
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Creation of the SQL table for the class <" + class_name + ">");
			Table_Saada_Data.createBusinessTable(class_name, cls);
			// Load values and update UCD of valid list of all
			// products with identical type
			// Updates the UCD tables in current data base for the products list
			uth.updateUCDTable(class_id);
			SQLTable.commitTransaction();
			dontforgettoreopentransaction = true;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, e);
		}
		if( dontforgettoreopentransaction ) {
			SQLTable.beginTransaction();
		}
		Database.getCachemeta().reload(true);
		this.current_class = Database.getCachemeta().getClass(class_name);
		if( this.current_prd != null )
			this.current_prd.setMetaclass(this.current_class);
		return this.current_class;
	}

	protected void updateSchemaForProduct() throws Exception {
		FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "schemaMapper.updateSchemaForProduct should never be invoked");
	}
}
