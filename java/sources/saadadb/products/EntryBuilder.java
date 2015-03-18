package saadadb.products;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.products.mergeandcast.ClassMerger;
import saadadb.products.mergeandcast.DownCasting;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.sqltable.SQLTable;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;


/**
 * @author michel
 * @version $Id$
 */
public class EntryBuilder extends ProductBuilder {
	private static final long serialVersionUID = 1L;
	public MetaClass tableClass;
	public long oidTable;
	/**
	 * Constructor.
	 * 
	 * @param TableBuilder  The entries table.
	 * @throws SaadaException if something goes wrong
	 */
	public EntryBuilder(TableBuilder table) throws SaadaException {
		super(table.dataFile, table.mapping.getEntryMapping(), null);
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#setProductIngestor()
	 */
	@Override
	protected void setProductIngestor() throws Exception{
		if( this.productIngestor == null ){
			this.productIngestor = new EntryIngestor(this);
		}		
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#loadProduct()
	 */
	public void loadProduct() throws Exception {
		IgnoreException.throwNewException(SaadaException.INTERNAL_ERROR, "Method loadProduct() must never be used with entris");
	}
	/**
	 * @param tableOid oid of the table
	 * @throws Exception
	 */
	public void loadProduct(long tableOid) throws Exception {
		this.oidTable = tableOid;
		String        busdumpfile  = Repository.getTmpPath() + Database.getSepar()  + this.metaClass.getName() +  ".psql";
		BufferedWriter  bustmpfile = new BufferedWriter(new FileWriter(busdumpfile));
		String         ecoll_table = Database.getCachemeta().getCollectionTableName(this.mapping.getCollection(), this.mapping.getCategory());
		String        coldumpfile  = Repository.getTmpPath() + Database.getSepar()  + ecoll_table +  ".psql";
		BufferedWriter  coltmpfile = new BufferedWriter(new FileWriter(coldumpfile));
		this.productIngestor.loadValue(coltmpfile, bustmpfile, null);
		Messenger.printMsg(Messenger.TRACE, "Store data into the DB");
		bustmpfile.close();
		coltmpfile.close();
		SQLTable.addQueryToTransaction("LOADTSVTABLE " + this.metaClass.getName() + " -1 " + busdumpfile);
		SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
	}
	
	/**
	 * The name must be recomputed for each entry. No default value computed here
	 */
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#mapInstanceName()
	 */
	@Override
	public void mapInstanceName() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Building the name");
		this.obs_idSetter = this.getSetterForMappedColumn("obs_id", mapping.getObservationAxisMapping().getColumnMapping("obs_id"));
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getCategory()
	 */
	public int getCategory(){
		return Category.ENTRY;
	}


	/* ######################################################
	 * 
	 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
	 * 
	 *#######################################################*/

	/* (non-Javadoc)
	 * @see saadadb.products.Product#loadProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
	 */
	@Override
	public void bindDataFile(DataFile dataFile) throws Exception {
		this.dataFile.bindEntryBuilder(this);
	}

	/**
	 * Update the vlaues of the local AHs with those read within the data file
	 * @throws Exception
	 */
	public void updateAttributeHandlerValues() throws Exception {
		this.dataFile.updateEntryAttributeHandlerValues(this);
	}

}
