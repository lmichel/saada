package saadadb.dataloader;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.EntryBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.datafile.AnyFile;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.JsonDataFile;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.sqltable.SQLQuery;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Business;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.Table_Saada_Metacat;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.ClassifierMode;


/**
 * Super class of the classes doing the actual data loading 
 * @author michel
 * @version $Id$
 */
public abstract class SchemaMapper {

	protected static String separ = System.getProperty("file.separator");
	protected ProductMapping mapping;
	private static String class_location;
	private static String package_name;
	protected List<String> dataFiles;
	protected ProductBuilder currentProductBuilder;
	protected MetaClass currentClass;
	protected SchemaMapper entryMapper = null;
	protected TreeSet<String> classesToBeIndexed = new TreeSet<String>();
	protected boolean mustIndex = true;
	protected Loader loader;

	/**
	 * @param loader
	 * @param prd
	 */
	public SchemaMapper(Loader loader, ProductBuilder prd) {
		setProduct(prd);
		this.loader = loader;
		package_name = "generated." + Database.getName();
		class_location = Database.getRoot_dir() + separ+ "class_mapping";
		this.mustIndex = !mapping.noIndex();
	}

	/**
	 * @param products
	 * @param handler
	 */
	public SchemaMapper(Loader loader, List<String> products, ProductMapping mapping) {
		this.loader = loader;
		this.mapping = mapping;
		this.dataFiles = products;
		package_name = "generated." + Database.getName();
		class_location = Database.getRoot_dir() + separ+ "class_mapping";
		this.mustIndex = !mapping.noIndex();
	}
	
	/**
	 * @param prd
	 */
	protected void  setProduct(ProductBuilder prd) {
		this.mapping = prd.getMapping();
		this.currentProductBuilder= prd;		
	}

	/**
	 * @throws Exception
	 */
	protected void loadProduct() throws Exception {

		try {
			this.currentProductBuilder.setMetaclass(this.currentClass);
			if( this.entryMapper != null ) {
				this.entryMapper.currentProductBuilder.setMetaclass(this.entryMapper.currentClass);
			}
			this.currentProductBuilder.mapDataFile();
			this.currentProductBuilder.loadProduct();
			Messenger.printMsg(Messenger.TRACE, "Product file <" + currentProductBuilder + "> ingested, <OID = " + currentProductBuilder.getActualOidsaada() + ">");

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
	public MetaClass createClassFromProduct(ClassifierMode mode) throws Exception {


		String class_name;
		/*
		 * Compute the class name: 
		 * Here must be used the class prefix given by the future configuration
		 */
		String className = this.mapping.getClassName();
		if( className == null || className.length() == 0){
			className = this.currentProductBuilder.possibleClassName();
		}
		class_name = this.getNewcLassName(className);
		/*
		 * Check the class name
		 */
		StringBuffer message = new StringBuffer();
		if( !class_name.matches(RegExp.CLASSNAME) ) {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Class name must match " + RegExp.CLASSNAME);
		} else if( !Database.getCachemeta().isNameAvailable(class_name, message) )  {
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, message.toString());
		}

		Messenger.printMsg(Messenger.TRACE,"Creation of the new class <" + class_name + ">");
		// Retrieve   attributeslist in the model
		Map<String, AttributeHandler> tableAttribute = this.currentProductBuilder.getProductAttributeHandler();
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
			Messenger.printMsg(Messenger.TRACE, "Storing the meta data of the class  " + class_name);
			SQLTable.beginTransaction();
			Table_Saada_Metacat uth = new Table_Saada_Metacat(class_name
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
				EntryBuilder entry = (EntryBuilder)this.currentProductBuilder;
				String table_class_name = entry.tableClass.getName();
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
			Table_Saada_Business.createBusinessTable(class_name, cls);
			// Load values and update UCD of valid list of all
			// products with identical type
			// Updates the UCD tables in current data base for the products list
			uth.updateUCDTable(class_id);
			if( !Database.getWrapper().supportDropTableInTransaction()){
				SQLTable.commitTransaction();
				SQLTable.beginTransaction();
			}
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
		this.currentClass = Database.getCachemeta().getClass(class_name);
		if( this.currentProductBuilder != null )
			this.currentProductBuilder.setMetaclass(this.currentClass);
		return this.currentClass;
	}

	/**
	 * @throws Exception
	 */
	protected void updateSchemaForProduct() throws Exception {
		FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "schemaMapper.updateSchemaForProduct should never be invoked");
	}
	
	/**
	 * Create an appropriate DataFile from the file name 
	 * @param fullPath
	 * @return the datafile
	 * @throws Exception
	 */
	public DataFile getDataFileInstance(String fullPath) throws Exception {
		if( FitsDataFile.isFitsFile(fullPath) ) {
			return(new FitsDataFile(fullPath, this.mapping));
		} else if( fullPath.matches(RegExp.VOTABLE_FILE) ) {
			return(new VOTableDataFile(fullPath, this.mapping));
		} else if( fullPath.matches(RegExp.JSON_FILE) ) {
			return(new JsonDataFile(fullPath, this.mapping));
		} else {
			return(new AnyFile(fullPath, this.mapping));
		}		
	}
	
	/**
	 * Create an appropriate DataFile from the file name 
	 * @param fullPath
	 * @param productMapping needed for the data product to build the ma
	 * @return
	 * @throws Exception
	 */
	public static DataFile getDataFileInstance(String fullPath, ProductMapping productMapping) throws Exception {

		if( (new File(fullPath)).exists()) {
			if( FitsDataFile.isFitsFile(fullPath) ) {		 
				return(new FitsDataFile(fullPath, productMapping));
			} else if( fullPath.matches(RegExp.VOTABLE_FILE) ) {
				return(new VOTableDataFile(fullPath, productMapping));
			} else if( fullPath.matches(RegExp.JSON_FILE) ) {
				return(new JsonDataFile(fullPath, productMapping));
			} else {
				return(new AnyFile(fullPath, productMapping));
			}	
		} else {
			AbortException.throwNewException(SaadaException.FILE_ACCESS, "can't access " + fullPath);
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public MetaClass getCurrentClass(){
		return this.currentClass;
	}
	/**
	 * @throws AbortException
	 */
	protected void processUserRequest() throws AbortException {
	 if( this.loader != null ) this.loader.processUserRequest();
	}

}
