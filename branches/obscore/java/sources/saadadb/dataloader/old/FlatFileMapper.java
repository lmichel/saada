package saadadb.dataloader.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import saadadb.collection.Category;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.old.Loader;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaCollection;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.products.FlatFile;
import saadadb.sqltable.SQLTable;
import saadadb.util.Messenger;

/**
 * This class is dedicated to load quickly flatfiles by short cutting the format analysis and by 
 * building and ASCII dump instead of one query per file.
 * @author michel
 * * @version $Id: FlatFileMapper.java 887 2014-01-14 09:33:09Z laurent.mistahl $

 */
public class FlatFileMapper extends SchemaMapper {


	public FlatFileMapper(Loader loader, ArrayList<File> products,
			ConfigurationDefaultHandler handler, boolean build_index) {
		super(loader, products, handler, build_index);
		Messenger.setMaxProgress(-1);
	}

	@Override
	public void ingestProductSet() throws Exception {
		if( this.products.size() == 0 ) {
			Messenger.printMsg(Messenger.ERROR, "Attempt to load an empty set if flatfiles");
			return;
		}
		else {
			String         ecoll_table = Database.getCachemeta().getCollectionTableName(this.configuration.getCollectionName(), Category.FLATFILE);
			String        busdumpfile  = Repository.getTmpPath() + Database.getSepar()  + "flatfile.psql";
			BufferedWriter  bustmpfile = new BufferedWriter(new FileWriter(busdumpfile));
			@SuppressWarnings("rawtypes")
			Class                  cls = SaadaClassReloader.forGeneratedName("FLATFILEUserColl");
			SaadaInstance           si = (SaadaInstance) cls.newInstance();
			FlatFile               ffp = (FlatFile)(this.configuration.getNewProductInstance(this.products.get(0)));
			Collection<AttributeHandler> it  = MetaCollection.getAttribute_handlers_flatfile().values(); 

			ffp.mapIgnoredAndExtendedAttributes();
			ffp.mapInstanceName();
			/*
			 * Drop SQL indexes
			 */
			SQLTable.beginTransaction();
			SQLTable.dropTableIndex(Database.getWrapper().getCollectionTableName(configuration.getCollectionName(), configuration.getCategorySaada()), this.loader);
			/*
			 * Build the dump table
			 */
			long oid = SaadaOID.newFlatFileOid(this.configuration.getCollectionName());
			int line=0;
			for( File file: this.products) {
				si.setOid(oid++);
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
							" <" + line + "/" + this.products.size()
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
					+ line + "/" + this.products.size()
					+ " Flafiles read: copy the dump files into the database"); 
			/*
			 * Store the dump table
			 */
			SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + busdumpfile);
			if( this.build_index ) {
				SQLTable.indexTable(ecoll_table, this.loader);
			}
			SQLTable.commitTransaction();
		}
	}
}
