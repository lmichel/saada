package saadadb.dataloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import saadadb.classmapping.TypeMapping;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.database.spooler.DatabaseConnection;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.GenerationClassProduct;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.products.DataResourcePointer;
import saadadb.products.Entry;
import saadadb.products.Product;
import saadadb.products.Table;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.UCDTableHandler;
import saadadb.util.Messenger;

/**
 * @author michel
 *
 */
public class SchemaFusionMapper extends SchemaMapper {

	public SchemaFusionMapper(Loader loader, ArrayList<DataResourcePointer> prdvect_list, ConfigurationDefaultHandler handler, boolean build_index) {
		super(loader, prdvect_list, handler, build_index);
		Messenger.setMaxProgress((2*prdvect_list.size()) + 2);
	}

	/**
	 * @param entr
	 */
	public SchemaFusionMapper(Loader loader, Product entr, boolean build_index) {
		super(loader, entr, build_index);
		Messenger.setMaxProgress((2*1) + 2);
	}

	/**
	 * Make a class matching all product files.
	 * Unit issue is ignored in attribute fusion
	 * @throws Exception
	 */
	public void makeClassFusion() throws Exception {	
		Messenger.printMsg(Messenger.TRACE, "Update class <" + this.configuration.getMapping().getClassName()+ ">");
		/*
		 * Build a set of attributes handlers matching all product to ingest
		 */
		this.current_prd = null;
		for( int i=0 ; i<this.products.size()	 ; i++) {
			DataResourcePointer file = this.products.get(i);
			Messenger.printMsg(Messenger.TRACE, "Update class for product <" + file.inputFileName + "> ");
			if( this.current_prd == null ) {
				this.current_prd = this.configuration.getNewProductInstance(file);
				/*
				 * Discard file non matching the configuration
				 */
				if(  !this.configuration.isProductValid(current_prd) ) {
					Messenger.printMsg(Messenger.TRACE, "Product <" + current_prd.getName()+ "> rejected");	
					this.products.remove(i);
					i--;
				}
				try {
					this.current_prd.loadProductFile(this.configuration);
				} catch(Exception e){
					Messenger.printMsg(Messenger.ERROR, e.toString());
					this.products.remove(i);
					i--;
					this.current_prd = null;
				}
			}
			else {
				/*
				 * AbortException rose of file type not recognized
				 */
				try {
					this.current_prd.mergeProductFormat(file);
				} catch(Exception e){	
					Messenger.printMsg(Messenger.ERROR, e.toString());
					this.products.remove(i);
					i--;
				}
			}
			/*
			 * Files are closed at gc time.
			 * Processing a large data set can possibly produce a too may open files
			 */
			if( (i % 1000) == 0 ) {
				System.gc();
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}
		if( this.products.size() == 0 ) {
			IgnoreException.throwNewException(SaadaException.FILE_ACCESS, "No longer product to load");
		}
		/*
		 * Checks if the requested class does not exist
		 * If not a new class is created
		 */
		if( ! Database.getCachemeta().classExists(this.configuration.getMapping().getClassName()) ) {
			this.createClassFromProduct(TypeMapping.MAPPING_USER);		
			SQLTable.beginTransaction();
			if( configuration.getCategorySaada() == Category.TABLE) {	
				Entry entr = ((Table) current_prd).getEntry();
				this.entry_mapper = new SchemaFusionMapper(this.loader, entr, this.build_index);
				this.entry_mapper.current_class = this.entry_mapper.createClassFromProduct(TypeMapping.MAPPING_USER);	
				SQLTable.beginTransaction();
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}
		/*
		 * else the former class is updated
		 */
		else {
			this.updateSchemaForProduct();
			SQLTable.beginTransaction();
			if( configuration.getCategorySaada() == Category.TABLE) {	
				Entry entr = ((Table) current_prd).getEntry();
				this.entry_mapper = new SchemaFusionMapper(this.loader, entr, this.build_index);
				this.entry_mapper.updateSchemaForProduct();	
				SQLTable.beginTransaction();
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}					
		this.current_prd.close();

	}
	/**
	 * @param products
	 * @param configuration
	 * @throws Exception
	 */
	@Override
	public void ingestProductSet() throws Exception {
		/*
		 * Update or build the class modeling the set of products to load
		 */
		SQLTable.beginTransaction();
		makeClassFusion();
		SQLTable.commitTransaction();

		/*
		 * Ingest all files
		 */
		Messenger.printMsg(Messenger.TRACE, "Start to ingest data");
		this.current_prd = null;
		int commit_frequency = 100;
		if( configuration.getCategorySaada() == Category.TABLE) {	
			commit_frequency = 1;
		}
		/*
		 * Drop SQL indexes
		 */
		SQLTable.beginTransaction();
		SQLTable.dropTableIndex(Database.getWrapper().getCollectionTableName(configuration.getCollectionName() ,configuration.getCategorySaada()), null);
		SQLTable.dropTableIndex(this.current_class.getName(), null);

		for( int i=0 ; i<this.products.size()	 ; i++) {
			DataResourcePointer file = this.products.get(i);
			this.current_prd = this.configuration.getNewProductInstance(file);
			Messenger.printMsg(Messenger.TRACE, "ingest product <" + this.current_prd.getName() +  ">");
			if( this.entry_mapper != null ) {	
				Entry entr = ((Table) current_prd).getEntry();
				this.entry_mapper.setProduct(entr);
			}
			try {
				this.current_prd.initProductFile(this.configuration);
			} catch(IgnoreException e){
				if( Messenger.trapIgnoreException(e) == Messenger.ABORT ) {
					AbortException.throwNewException(SaadaException.USER_ABORT, e);
					return;
				}
				else {
					continue;
				}
			}

			this.loadProduct();	
			this.current_prd.close();
			
			if( (i%commit_frequency) == 0 ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "COMMIT at product #" + i);
				SQLTable.commitTransaction();
				Database.gc();
				SQLTable.beginTransaction();				
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}
		/*
		 * Re-create SQL indexes
		 */
		if( this.build_index) {
			SQLTable.indexTable(Database.getWrapper().getCollectionTableName(configuration.getCollectionName() ,configuration.getCategorySaada()), this.loader);
			SQLTable.indexTable(this.current_class.getName(), this.loader);
			this.loader.processUserRequest();
			if( this.entry_mapper != null ) {
				SQLTable.indexTable(Database.getWrapper().getCollectionTableName(this.entry_mapper.configuration.getCollectionName() , this.entry_mapper.configuration.getCategorySaada()), this.loader);
				SQLTable.indexTable(this.entry_mapper.current_class.getName(), this.loader);			
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
		}
		SQLTable.commitTransaction();
	}	

	/**
	 * @param products
	 * @param configuration
	 * @throws Exception
	 */
	public void ingestProductSetByBurst() throws Exception {
		/*
		 * Update or build the class modeling the set of products to load
		 */
		SQLTable.beginTransaction();
		makeClassFusion();
		SQLTable.commitTransaction();

		/*
		 * Ingest all files
		 */
		Messenger.printMsg(Messenger.TRACE, "Start to ingest data in blob mode");
		this.current_prd = null;
		/*
		 * Drop SQL indexes
		 */
		SQLTable.beginTransaction();
		SQLTable.dropTableIndex(this.current_class.getName(), null);
		SQLTable.commitTransaction();

		String         ecoll_table = Database.getCachemeta().getCollectionTableName(this.configuration.getCollectionName(), this.configuration.getCategorySaada());
		String        busdumpfile  = Repository.getTmpPath() + Database.getSepar()  + this.current_class.getName() +  ".psql";
		BufferedWriter  bustmpfile = new BufferedWriter(new FileWriter(busdumpfile));
		String        coldumpfile  = Repository.getTmpPath() + Database.getSepar()  + ecoll_table +  ".psql";
		BufferedWriter  coltmpfile = new BufferedWriter(new FileWriter(coldumpfile));
		String        loadedfile   = Repository.getTmpPath() + Database.getSepar()  + "saada_loaded_file.psql";
		BufferedWriter loadedtmpfile = new BufferedWriter(new FileWriter(loadedfile));

		for( int i=0 ; i<this.products.size()	 ; i++) {
			DataResourcePointer file = this.products.get(i);
			this.current_prd = this.configuration.getNewProductInstance(file);
			Messenger.printMsg(Messenger.TRACE, "ingest product <" + this.current_prd.getName() +  ">");
			if( this.entry_mapper != null ) {	
				Entry entr = ((Table) current_prd).getEntry();
				this.entry_mapper.setProduct(entr);
			}
			try {
				this.current_prd.initProductFile(this.configuration);
			} catch(IgnoreException e){
				if( Messenger.trapIgnoreException(e) == Messenger.ABORT ) {
					AbortException.throwNewException(SaadaException.USER_ABORT, e);
					return;
				}
				else {
					continue;
				}
			}
			/*
			 * Must not be used with table (entry wouln't be loaded)
			 */
			this.current_prd.setMetaclass(this.current_class);
			this.current_prd.loadValue(coltmpfile, bustmpfile, loadedtmpfile);
			Messenger.printMsg(Messenger.TRACE, "Product file <" + current_prd + "> ingested, <OID = " + current_prd.getSaadainstance().getOid() + ">");
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
			this.current_prd.close();
		}	
		loadedtmpfile.close();
		coltmpfile.close();
		bustmpfile.close();
		/*
		 * Store the dump table
		 */
		SQLTable.beginTransaction();
		SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
		SQLTable.addQueryToTransaction("LOADTSVTABLE " + this.current_class.getName() + " -1 " + busdumpfile);
		SQLTable.addQueryToTransaction("LOADTSVTABLE saada_loaded_file -1 " + loadedfile);
		SQLTable.commitTransaction();

		/*
		 * Re-create SQL indexes
		 */
		if( this.build_index) {
			SQLTable.beginTransaction();

			SQLTable.indexTable(Database.getWrapper().getCollectionTableName(configuration.getCollectionName() ,configuration.getCategorySaada()), this.loader);
			SQLTable.indexTable(this.current_class.getName(), this.loader);
			this.loader.processUserRequest();
			if( this.entry_mapper != null ) {
				SQLTable.indexTable(Database.getWrapper().getCollectionTableName(this.entry_mapper.configuration.getCollectionName() , this.entry_mapper.configuration.getCategorySaada()), this.loader);
				SQLTable.indexTable(this.entry_mapper.current_class.getName(), this.loader);			
			}
			this.loader.processUserRequest();
			Messenger.incrementeProgress();
			SQLTable.commitTransaction();
		}
	}	

	/**
	 * @param product
	 * @throws Exception 
	 */
	@Override
	protected void updateSchemaForProduct() throws Exception {

		MetaClass mc = Database.getCachemeta().getClass(this.configuration.getMapping().getClassName());	
		if( configuration.getCategorySaada() == Category.ENTRY ) {
			String entry_class_name = mc.getAssociate_class();
			mc = Database.getCachemeta().getClass(entry_class_name);			
		}

		/*
		 * The class can only be updated if is attached to the same collection and category as the
		 * current product
		 */
		if( !mc.getCollection_name().equals(configuration.getCollectionName()) ||
				mc.getCategory() != configuration.getCategorySaada()	) {
			AbortException.throwNewException(SaadaException.METADATA_ERROR, "Class  <" + this.configuration.getMapping().getClassName() 
					+ "> already exist (" + mc.getName() + "), but associed with another collection ("  
					+ mc.getCollection_name() + ") or category(" 
					+ mc.getCategory() + ")");
		} else {			
			LinkedHashMap<String, AttributeHandler> org_ah = new  LinkedHashMap<String, AttributeHandler>();
			AttributeHandler[] ahs = mc.getClassAttributes();
			for( int i=0 ; i<ahs.length ; i++ ) {
				org_ah.put(ahs[i].getNameattr(), ahs[i]);
			}
			this.current_prd.mergeAttributeHandlers(org_ah);

			LinkedHashMap<String, AttributeHandler> new_ah = this.current_prd.tableAttributeHandler;
			Iterator<AttributeHandler> it = new_ah.values().iterator();
			boolean modified = false;
			if( ahs.length != new_ah.size() ) {
				modified = true;
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Number of columns differ");
			}
			while( it.hasNext()) {
				AttributeHandler nah = it.next();
				AttributeHandler oah = org_ah.get(nah.getNameattr());			
				/*
				 * new attribute: insert a new column
				 */
				if( oah == null ) {
					modified = true;
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Column <" + nah.getNameattr() + "> of type \"" + nah.getType() + "\" added to table/class <" + mc.getName() + "> ");
					SQLTable.addQueryToTransaction(Database.getWrapper().addColumn(mc.getName(), nah.getNameattr(), Database.getWrapper().getSQLTypeFromJava(nah.getType()))
							, mc.getName());
				}
				/*
				 * Attribute exist but type has been changed
				 */
				else if( oah.getType().equals(nah.getType()) == false ) {
					modified = true;
					DatabaseConnection connection = Database.getConnection();
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Column <" + nah.getNameattr() + "> of table/class <" + mc.getName() + "> converted from \"" + oah.getType() + "\" to \"" + nah.getType() + "\"");
					SQLTable.addQueryToTransaction(Database.getWrapper().changeColumnType(connection, mc.getName(), nah.getNameattr(), Database.getWrapper().getSQLTypeFromJava(nah.getType()))
							, mc.getName());
					Database.giveConnection(connection);
				}
			}
			if( modified ) {
				(new UCDTableHandler(mc.getName()
						, configuration.getCollectionName()
						, configuration.getCategorySaada()
						, new ArrayList<AttributeHandler>(this.current_prd.tableAttributeHandler.values()))).updateUCDTable(mc.getId());
				GenerationClassProduct.buildJavaClass(this.current_prd.tableAttributeHandler
						, SchemaMapper.getClass_location()
						, mc.getName()
						, Category.explain(configuration.getCategorySaada()) + "UserColl");
				/*
				 * Synchronize the cache with the new class
				 */
				//System.out.println(this.current_prd.saadainstance);
				SaadaClassReloader.reloadGeneratedClass(mc.getName());

				SQLTable.commitTransaction();
				/*
				 * reload the cache and the modified class
				 */
				Database.getCachemeta().reload(true);
			}
		}
		this.current_class = Database.getCachemeta().getClass(mc.getName());
	}
}
