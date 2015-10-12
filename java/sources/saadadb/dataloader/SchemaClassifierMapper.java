package saadadb.dataloader;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.meta.MetaClass;
import saadadb.products.EntryBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;
import saadadb.vocabulary.enums.ClassifierMode;
/**
 * Run the ingestion a a dataset in classifier mode. Input data files are clustered by identical formats
 * Each of these subset is ingested in fusion mode
 * 
 * @author michel
 * @version $Id$
 *
 */
public class SchemaClassifierMapper extends SchemaMapper {
	private String requested_classname;

	private Map<String, DataFileCluster> dataPackets;
	/**
	 * @param handler 
	 * @param mapping
	 */
	public SchemaClassifierMapper(Loader loader, ArrayList<String> prdvect_list, ProductMapping mapping) {
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
	 * returns a map of the data files clusters Each cluster contain all files with the same format. 
	 * The key map is the format signature. The cluster (map value) is contained in a {@linkplain DataFileCluster} 
	 * @return the cluster map
	 * @throws Exception
	 */
	public Map<String, DataFileCluster> getProductClusters()  throws Exception{
		Map<String, DataFileCluster> retour  = new LinkedHashMap<String, DataFileCluster>();
		for( String fn: this.dataFiles) {
			DataFile file = this.getDataFileInstance(fn);
			this.currentProductBuilder = this.mapping.getNewProductBuilderInstance(file, this.currentClass);	
			if( mapping.getCategory() == Category.TABLE ) {
				this.entryMapper = new SchemaClassifierMapper(this.loader, ((TableBuilder)this.currentProductBuilder).entryBuilder);
			}
			String signature = this.currentProductBuilder.getFmtsignature();
			if( mapping.getCategory() == Category.TABLE ) {
				signature += this.entryMapper.currentProductBuilder.getFmtsignature();
			}			
			DataFileCluster dp;
			if( (dp = this.dataPackets.get(signature)) == null ) {
				dp = new DataFileCluster();
				this.dataPackets.put(signature, dp);
				if( mapping.getCategory() == Category.TABLE ) {
					this.entryMapper.updateSchemaForProduct();
					dp.entryClasse = this.entryMapper.currentClass;
				}
				dp.classe = this.currentClass;
			}
			dp.fileList.add(fn);
		}
		return retour;
	}

	/* (non-Javadoc)
	 * @see saadadb.dataloader.SchemaMapper#ingestProductSet()
	 */
	@Override
	public void ingestProductSet() throws Exception{
		/*
		 * Build the per class map of the product to be ingested
		 * Classes are created on the fly to avoid an extra DataFile instanciation 
		 */
		dataPackets = new LinkedHashMap<String, DataFileCluster>();
		for( String fn: this.dataFiles) {
			DataFile file = this.getDataFileInstance(fn);
			this.currentProductBuilder = this.mapping.getNewProductBuilderInstance(file, this.currentClass);	
			if( mapping.getCategory() == Category.TABLE ) {
				this.entryMapper = new SchemaClassifierMapper(this.loader, ((TableBuilder)this.currentProductBuilder).entryBuilder);
			}
			String signature = this.currentProductBuilder.getFmtsignature();
			if( mapping.getCategory() == Category.TABLE ) {
				signature += this.entryMapper.currentProductBuilder.getFmtsignature();
			}			
			DataFileCluster dp;
			if( (dp = this.dataPackets.get(signature)) == null ) {
				dp = new DataFileCluster();
				this.dataPackets.put(signature, dp);
				SQLTable.beginTransaction();
				this.updateSchemaForProduct();
				if( mapping.getCategory() == Category.TABLE ) {
					this.entryMapper.updateSchemaForProduct();
					dp.entryClasse = this.entryMapper.currentClass;
				}
				SQLTable.commitTransaction();
				dp.classe = this.currentClass;
			}
			dp.fileList.add(fn);
		}
		/*
		 * Store files class by class by using a SchemaFusionMapper
		 */
		int cpt=0;
		for( DataFileCluster dp : this.dataPackets.values()){
			SchemaFusionMapper sfm = new SchemaFusionMapper(this.loader, dp.fileList, this.mapping);
			sfm.currentClass = dp.classe;
			sfm.mustIndex = this.mustIndex;
			if( mapping.getCategory() == Category.TABLE ) {
				EntryBuilder entr = ((TableBuilder) currentProductBuilder).entryBuilder;
				sfm.entryMapper = new SchemaFusionMapper(this.loader, entr);
				sfm.entryMapper.currentClass = dp.entryClasse;	
				sfm.storeAllDataFiles();

			} else {
				sfm.storeAllDataFilesByBurst();
			}
			cpt++;
			this.loader.processUserRequest();

		}
	}
	//	/**
	//	 * @param dataFiles
	//	 * @param mapping
	//	 * @throws Exception
	//	 */
	//	@Override
	//	public void ingestProductSetXX() throws Exception {
	//
	//		//requested_classname = this.standardizeName("Cl");
	//		//System.out.println("Requested " + requested_classname);
	//		/*
	//		 * Drop SQL indexes
	//		 */
	//		Messenger.printMsg(Messenger.TRACE, "Ingesting the product set");
	//		SQLTable.beginTransaction();
	//		SQLTable.dropTableIndex(Database.getWrapper().getCollectionTableName(mapping.getCollection(), mapping.getCategory()), this.loader);
	//		for( int i=0 ; i<this.dataFiles.size()	 ; i++) {
	//			DataFile file = this.getDataFileInstance(this.dataFiles.get(i));
	//			if( i==0 ) {			
	//				if (Messenger.debug_mode)
	//					Messenger.printMsg(Messenger.DEBUG, "Build the builder which will be used for the whole data set");
	//				this.currentProductBuilder = this.mapping.getNewProductBuilderInstance(file, this.currentClass);			
	//				this.currentProductBuilder.setMetaclass(this.currentClass);
	//			}
	//			this.currentProductBuilder.mapDataFile(file);
	//			/*
	//			 * Only AbortException are caught, because they don't stop the process
	//			 */
	//			//try {
	//			this.ingestCurrentProduct();
	//			//} catch( AbortException e) {
	//			//	SQLTable.beginTransaction();				
	//			//}
	//
	//			if( i > 0 && (i%100) == 0 ) {
	//				SQLTable.commitTransaction();	
	//				Database.gc();
	//				SQLTable.beginTransaction();
	//			}
	//			this.loader.processUserRequest();
	//			Messenger.incrementeProgress();
	//		}
	//		/*
	//		 * Re-create SQL indexes 
	//		 */
	//		if( this.mustIndex ) {
	//			SQLTable.indexTable(Database.getWrapper().getCollectionTableName(mapping.getCollection(), mapping.getCategory()), this.loader);
	//			for( String cti: classesToBeIndexed) {
	//				SQLTable.indexTable(cti, this.loader);			
	//			}
	//			if( this.entryMapper != null ) {
	//				SQLTable.indexTable(Database.getWrapper().getCollectionTableName(mapping.getCollection(), this.entryMapper.mapping.getCategory()), this.loader);
	//				for( String cti: this.entryMapper.classesToBeIndexed) {
	//					this.loader.processUserRequest();
	//					SQLTable.indexTable(cti, this.loader);			
	//				}			
	//			}
	//			Messenger.incrementeProgress();
	//		}
	//		SQLTable.commitTransaction();	
	//	}	
	//	/**
	//	 * @throws IOException 
	//	 * @throws FitsException 
	//	 * @throws Exception 
	//	 * 
	//	 */
	//	protected void ingestCurrentProduct() throws Exception {
	//		/*
	//		 * Products not matching the configare rejected
	//		 */
	//		if( !this.mapping.isProductValid(currentProductBuilder) ) {
	//			Messenger.printMsg(Messenger.TRACE, "<" + currentProductBuilder.getName()+ "> rejected");	
	//			return;
	//		}
	//
	//		/*
	//		 * No schema update for flatfile: no need to generate classes because product content
	//		 * is not read
	//		 */
	//		if( mapping.getCategory() != Category.FLATFILE ) {
	//			/*
	//			 * Create misssing class and load the product
	//			 */
	//			this.updateSchemaForProduct();
	//			/*
	//			 * Transaction is closed when a class is created
	//			 */
	//			//SQLTable.beginTransaction();
	//			/*
	//			 * build table entry class  if any
	//			 */
	//			if( mapping.getCategory() == Category.TABLE) {
	//				Messenger.printMsg(Messenger.TRACE, "Check schema for entries");
	//				EntryBuilder entr = ((TableBuilder) currentProductBuilder).entryBuilder;
	//				this.entryMapper = new SchemaClassifierMapper(this.loader, entr);
	//				this.entryMapper.updateSchemaForProduct();
	//				/*
	//				 * Transaction is closed when a class is created
	//				 */
	//				//SQLTable.beginTransaction();
	//			}
	//		}
	//		/*
	//		 * Store product
	//		 */
	//		this.loadProduct();
	//	}

	/**
	 * @param product
	 * @throws Exception 
	 */
	@Override
	protected void updateSchemaForProduct() throws Exception {
		String className = this.mapping.getClassName();
		boolean classNameImposed = true;
		if( className == null || className.length() == 0){
			classNameImposed = false;
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

				boolean classFormatFound = false;
				if(  mc.getCollection_name().equals(mapping.getCollection()) &&
						mc.getCategory() == mapping.getCategory() ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Found a class matching the format");
					classFormatFound = true;
				}
				if( classFormatFound ) {
					if( !classNameImposed ) {					
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Take it without regard for the name");
						this.classesToBeIndexed.add(mc.getName());
						this.currentProductBuilder.setMetaclass(mc);	
						class_found = true;
						break;
					} else if( ( (mc.getName().equals(requested_classname) || mc.getName().matches(requested_classname + "(_[0-9]+(Entry)?)?")) ||
							((mc.getName().equals(requested_classname + "Entry") || mc.getName().matches(requested_classname.replaceAll("Entry$","") + "(_[0-9]+(Entry)?)?")))))  {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Name " + requested_classname + " matches as well, take it");
						this.classesToBeIndexed.add(mc.getName());
						this.currentProductBuilder.setMetaclass(mc);	
						class_found = true;
						break;		
					}
				}
			}
			/*
			 * If not found, a new class is created
			 */
			if( !class_found ) {
				Messenger.printMsg(Messenger.TRACE, "One class found (" 
						+ mc.getName() + ") but associed with another collection (" + mc.getCollection_name() + ") or category (" + Category.explain(mc.getCategory()) + ")");
				mc = this.createClassFromProduct(ClassifierMode.CLASSIFIER);
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Class <" + mc.getName() + "> created");
				this.classesToBeIndexed.add(mc.getName());
			}
		}
		this.currentClass =  mc;
	}


}
