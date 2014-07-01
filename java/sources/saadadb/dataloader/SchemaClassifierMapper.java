package saadadb.dataloader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.enums.ClassifierMode;
import saadadb.enums.MappingMode;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.MetaClass;
import saadadb.products.DataFile;
import saadadb.products.EntryBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.TableBuilder;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
/**
 * @author michel
 * @version $Id$

 */
public class SchemaClassifierMapper extends SchemaMapper {
	private String requested_classname;
	/**
	 * @param handler 
	 * @param mapping
	 */
	public SchemaClassifierMapper(Loader loader, ArrayList<DataFile> prdvect_list, ProductMapping mapping) {
		super(loader, prdvect_list, mapping);
		if( prdvect_list != null )
			Messenger.setMaxProgress(prdvect_list.size() + 2);
	}

	/**
	 * @param entr
	 */
	public SchemaClassifierMapper(Loader loader, ProductBuilder entr) {
		super(loader, entr);
		Messenger.setMaxProgress(1 + 1);
	}

	/**
	 * @param dataFiles
	 * @param mapping
	 * @throws Exception
	 */
	@Override
	public void ingestProductSet() throws Exception {
		//requested_classname = this.standardizeName("Cl");
		//System.out.println("Requested " + requested_classname);
		/*
		 * Drop SQL indexes
		 */
		Messenger.printMsg(Messenger.TRACE, "Ingesting the product set");
		SQLTable.beginTransaction();
		SQLTable.dropTableIndex(Database.getWrapper().getCollectionTableName(mapping.getCollection(), mapping.getCategory()), this.loader);
		for( int i=0 ; i<this.dataFiles.size()	 ; i++) {
			DataFile file = this.dataFiles.get(i);
			/*
			 * Build the Saada Product instance
			 */
			try {
				this.currentProductBuilder = this.mapping.getNewProductBuilderInstance(file);				
				Messenger.printMsg(Messenger.TRACE, "Build the Saada instance modeling <" + currentProductBuilder.getName()+ ">");
				//this.currentProductBuilder.initProductFile();
			} catch(IgnoreException e) {
				if( Messenger.trapIgnoreException(e) == Messenger.ABORT ) {
					AbortException.throwNewException(SaadaException.USER_ABORT, e);
					return;
				}
				else {
					continue;
				}

			}
			/*
			 * Only AbortException are caught, because they don't stop the process
			 */
			//try {
			this.ingestCurrentProduct();
			//} catch( AbortException e) {
			//	SQLTable.beginTransaction();				
			//}

			if( i > 0 && (i%100) == 0 ) {
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
		if( this.mustIndex ) {
			SQLTable.indexTable(Database.getWrapper().getCollectionTableName(mapping.getCollection(), mapping.getCategory()), this.loader);
			for( String cti: classesToBeIndexed) {
				SQLTable.indexTable(cti, this.loader);			
			}
			if( this.entryMapper != null ) {
				SQLTable.indexTable(Database.getWrapper().getCollectionTableName(mapping.getCollection(), this.entryMapper.mapping.getCategory()), this.loader);
				for( String cti: this.entryMapper.classesToBeIndexed) {
					this.loader.processUserRequest();
					SQLTable.indexTable(cti, this.loader);			
				}			
			}
			Messenger.incrementeProgress();
		}
		SQLTable.commitTransaction();	
	}	
	/**
	 * @throws IOException 
	 * @throws FitsException 
	 * @throws Exception 
	 * 
	 */
	protected void ingestCurrentProduct() throws Exception {
		/*
		 * Products not matching the configare rejected
		 */
		if( !this.mapping.isProductValid(currentProductBuilder) ) {
			Messenger.printMsg(Messenger.TRACE, "<" + currentProductBuilder.getName()+ "> rejected");	
			return;
		}

		/*
		 * No schema update for flatfile: no need to generate classes because product content
		 * is not read
		 */
		if( mapping.getCategory() != Category.FLATFILE ) {
			/*
			 * Create misssing class and load the product
			 */
			this.updateSchemaForProduct();
			/*
			 * Transaction is closed when a class is created
			 */
			//SQLTable.beginTransaction();
			/*
			 * build table entry class  if any
			 */
			if( mapping.getCategory() == Category.TABLE) {
				Messenger.printMsg(Messenger.TRACE, "Check schema for entries");
				EntryBuilder entr = ((TableBuilder) currentProductBuilder).getEntry();
				this.entryMapper = new SchemaClassifierMapper(this.loader, entr);
				this.entryMapper.updateSchemaForProduct();
				/*
				 * Transaction is closed when a class is created
				 */
				//SQLTable.beginTransaction();
			}
		}
		/*
		 * Store product
		 */
		this.loadProduct();
	}

	/**
	 * @param product
	 * @throws Exception 
	 */
	@Override
	protected void updateSchemaForProduct() throws Exception {
		String className = this.mapping.getClassName();
		if( className == null || className.length() == 0){
			className = this.currentProductBuilder.possibleClassName();
		}
		requested_classname = this.getRequestedClassName(className);

		String fmt_signature = this.currentProductBuilder.getFmtsignature();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"product " + this.currentProductBuilder.getClass().getName() + " " + fmt_signature);
		MetaClass mc=null;
		if( !Database.getCachemeta().classWithSignatureExists(fmt_signature) ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No class found with signature <" + fmt_signature + ">");
			mc = this.createClassFromProduct(ClassifierMode.CLASSIFIER);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Class <" + mc.getName() + "> created");
			this.classesToBeIndexed.add(mc.getName());
		}
		else {
			boolean class_found = false;
			/*
			 * Look for a class with the good name/signature/category/collection
			 */
			String[] existing_classes = Database.getCachemeta().getClassWithSignatureNames(fmt_signature);
			/*
			 * Look first for one class with the same signature and name
			 */
			for( int i=0 ; i<existing_classes.length ; i++ ) {
				mc = Database.getCachemeta().getClass(existing_classes[i]);
				if( (mc.getName().equals(requested_classname + "Entry") || mc.getName().matches(requested_classname.replaceAll("Entry$","") + "(_[0-9]+(Entry)?)?" ))
						&&  mc.getCollection_name().equals(mapping.getCollection()) 
						&&  mc.getCategory() == mapping.getCategory() ) {
					Messenger.printMsg(Messenger.TRACE, "Existing class <" + mc.getName() + "> matches the product format");
					//SQLTable.dropTableIndex(mc.getName(), this.loader);
					this.classesToBeIndexed.add(mc.getName());
					this.currentProductBuilder.setMetaclass(mc);	
					class_found = true;
					break;
				}
			}
			/*
			 * If not found, a new class is created
			 */
			if( !class_found ) {
				Messenger.printMsg(Messenger.TRACE, "One class with signature <" + fmt_signature 
						+ "> found (" + mc.getName() + "), but associed with another collection, category or name");
				mc = this.createClassFromProduct(ClassifierMode.CLASSIFIER);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Class <" + mc.getName() + "> created");
				this.classesToBeIndexed.add(mc.getName());
			}
		}
		this.currentClass =  mc;

	}


}
