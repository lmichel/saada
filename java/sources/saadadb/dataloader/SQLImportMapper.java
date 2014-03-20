package saadadb.dataloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ClassifierMode;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Class;
import saadadb.sqltable.UCDTableHandler;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;

public class SQLImportMapper extends SchemaMapper {
	private String tableName;

	public SQLImportMapper(Loader loader, String tableName, ProductMapping mapping) {
		super(loader, null, mapping);
		this.tableName = tableName;
	}

	/**
	 * Make a class matching all product files.
	 * Unit issue is ignored in attribute fusion
	 * @throws Exception
	 */
	public void makeClassFusion() throws Exception {	
		Messenger.printMsg(Messenger.TRACE, "Update class <" + this.mapping.getClassName()+ ">");
		/*
		 * Build a set of attributes handlers matching all product to ingest
		 */
		//this.current_prd = new ImportedTable(this.tableName, this.mapping) ;
		this.current_prd =null;
		this.loader.processUserRequest();


		/*
		 * Checks if the requested class does not exist
		 * If not a new class is created
		 */
		if( ! Database.getCachemeta().classExists(this.mapping.getClassName()) ) {
			this.createClassFromProduct(ClassifierMode.CLASS_FUSION);		
			SQLTable.beginTransaction();
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}
		/*
		 * else the former class is updated
		 */
		else {
			IgnoreException.throwNewException(SaadaException.METADATA_ERROR, "Class " + this.tableName + " already exists, cannot import the table");
		}
	}
	@Override
	public void ingestProductSet() throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * @param mapping_type
	 * @param class_name
	 * @param tableAttribute
	 * @return
	 * @throws Exception
	 */
	public MetaClass createClassFromProduct(ClassifierMode mapping_type, String class_name,  LinkedHashMap<String, AttributeHandler> tableAttribute) throws Exception {
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
					, mapping.getName()
					, mapping_type
					, MD5Key.getFmtsignature(tableAttribute)
					, mapping.getLoaderParams());
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
}
