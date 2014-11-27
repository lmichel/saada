package saadadb.dataloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.products.FlatFileBuilder;
import saadadb.products.datafile.DataFile;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * This class is dedicated to load quickly flatfiles by short cutting the format analysis and by 
 * building and ASCII dump instead of one query per file.
 * @author michel
 * * @version $Id$

 */
public class FlatFileMapper extends SchemaMapper {


	public FlatFileMapper(Loader loader, List<String> products,
			ProductMapping productMapping) {
		super(loader, products, productMapping);
		Messenger.setMaxProgress(-1);
	}

	@Override
	public void ingestProductSet() throws Exception {
		if( this.dataFiles.size() == 0 ) {
			Messenger.printMsg(Messenger.ERROR, "Attempt to load an empty set if flatfiles");
			return;
		} else {
			String         ecoll_table = Database.getCachemeta().getCollectionTableName(this.mapping.getCollection(), Category.FLATFILE);
			String        busdumpfile  = Repository.getTmpPath() + Database.getSepar()  + "flatfile.psql";
			BufferedWriter  bustmpfile = new BufferedWriter(new FileWriter(busdumpfile));
			@SuppressWarnings("rawtypes")
			Class                  cls = SaadaClassReloader.forGeneratedName("FLATFILEUserColl");
			SaadaInstance           si = (SaadaInstance) cls.newInstance();
			FlatFileBuilder               ffp = (FlatFileBuilder)(this.mapping.getNewProductBuilderInstance(this.getDataFileInstance(this.dataFiles.get(0)), null));
			Collection<AttributeHandler> it  = MetaCollection.getAttribute_handlers_flatfile().values(); 

			ffp.mapIgnoredAndExtendedAttributes();
			ffp.mapInstanceName();
			/*
			 * Drop SQL indexes
			 */
			SQLTable.beginTransaction();
			SQLTable.dropTableIndex(Database.getWrapper().getCollectionTableName(this.mapping.getCollection(), this.mapping.getCategory()), this.loader);
			/*
			 * Build the dump table
			 */
			long oid = SaadaOID.newFlatFileOid(this.mapping.getCollection());
			int line=0;
			for( String fn: this.dataFiles) {
				DataFile file = this.getDataFileInstance(fn);
				si.oidsaada = oid++;
				ffp.bindInstanceToFile(si, file);
				ffp.storeCopyFileInRepository();
				StringBuffer file_sql = new StringBuffer();
				for( AttributeHandler ah: it) {
					Field field = cls.getField(ah.getNameattr());
					String val = si.getSQL(field).replaceAll("'", "");
					if( file_sql.length() > 0 ) {
						file_sql.append("\t");
					}
					if( val.equals("null") ) {
						file_sql.append(Database.getWrapper().getAsciiNull());	
					}
					else {
						file_sql.append(val);					
					}
				}
				file_sql.append("\n");
				
				bustmpfile.write(file_sql.toString());
				line++;
				if( (line%100) == 0 ) {
					Messenger.printMsg(Messenger.TRACE,
							" <" + line + "/" + this.dataFiles.size()
							+ "> : Flatfiles loaded ");
					if( (line % 15000) == 0 ) {
						/*
						 * Commit the storage of loaded files in 'able Saada_Loaded_File
						 */
						SQLTable.commitTransaction();
						SQLTable.beginTransaction();
						Database.gc();
					}
				}
			}
			bustmpfile.close();
			Messenger.printMsg(Messenger.TRACE,
					+ line + "/" + this.dataFiles.size()
					+ " Flafiles read: copy the dump files into the database"); 
			/*
			 * Store the dump table
			 */
			SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + busdumpfile);
			if( this.mustIndex ) {
				SQLTable.indexTable(ecoll_table, this.loader);
			}
			SQLTable.commitTransaction();
		}
	}
}
