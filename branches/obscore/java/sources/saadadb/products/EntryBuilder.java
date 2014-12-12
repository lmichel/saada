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
	
	public void loadProductXX(long tableOid) throws Exception {
		// Initializes the lines meter at 0
		int line = 0;
		// A java type of a attribute
		// A field name (corresponding to the changed name of a entry and
		// the field name in data base)
		//String nameField;
		StringBuffer file_bus_sql = new StringBuffer();
		StringBuffer md5KeySQL = new StringBuffer();
		// Initialzes an enumeration of entries (table rows)
		//Enumeration enumerateRow = table.elements();
		//Object[]  values        = new Object[0];
		//@@@@@@ int nb_bus_att          = -1;
		int table_size = this.getNRows();
		//boolean vectfieldexist = false;
		long time_tag = (new Date()).getTime();
		String         busdumpfile = Repository.getTmpPath() + Database.getSepar()  + "bus" + time_tag + ".psql";
		BufferedWriter bustmpfile  = new BufferedWriter(new FileWriter(busdumpfile));
		String         coldumpfile = Repository.getTmpPath() + Database.getSepar()  + "col" + time_tag + ".psql";
		BufferedWriter coltmpfile  = new BufferedWriter(new FileWriter(coldumpfile));
		String tcoll_table = Database.getCachemeta().getCollectionTableName(this.metaClass.getCollection_name()
				, Category.TABLE);
		this.setProductIngestor();		
		/** Keep a ref on the casted ingestor */
		EntryIngestor entryIngestor = (EntryIngestor) this.productIngestor; 
		EntrySaada entryInstance = (EntrySaada) entryIngestor.saadaInstance;
		int nb_bus_att = -1;
		int[] index_pos_att = null;
		boolean[] index_pos_md5 = null;
		while (this.productIngestor.hasMoreElements()) {
			file_bus_sql.setLength(0);
			md5KeySQL.setLength(0);
			/*
			 * Use the first row to map attribute read into the product file onto the 
			 * Saada class attribute (collection)
			 */
			if( line == 0 ) {
				entryIngestor.mapIndirectionTables();
				nb_bus_att = entryIngestor.nb_bus_att;
				index_pos_att = entryIngestor.index_pos_att;
				index_pos_md5 = entryIngestor.index_pos_md5;

			} // first line processing
			line++;
			/* next line readout */
			this.productIngestor.bindInstanceToFile();
			/*
			 * Build the SQL query for business table
			 */
			String val;
			Object [] values = entryIngestor.values;
			for( int i=0 ; i<nb_bus_att ; i++  ) {
				file_bus_sql.append("\t");
				if( index_pos_att[i] != -1 ) {
					val = values[index_pos_att[i]].toString();
				} else {
					val = Database.getWrapper().getAsciiNull();
				}

				if( val.equals("NaN") || val.equals("") || val.equals("Infinity")) {					
					file_bus_sql.append(Database.getWrapper().getAsciiNull());
				} else if( val.equalsIgnoreCase("false") ) {	
					file_bus_sql.append(Database.getWrapper().getBooleanAsString(false));
				}  else if(  val.equalsIgnoreCase("true")) {	
					file_bus_sql.append(Database.getWrapper().getBooleanAsString(true));
				} else {
					file_bus_sql.append(val);
				}
				if( index_pos_md5[i] ) {
					md5KeySQL.append(val);
				}
			}
			file_bus_sql.insert(0, entryIngestor.saadaInstance.oidsaada + "\t" + entryIngestor.saadaInstance.obs_id + "\t" + MD5Key.calculMD5Key(md5KeySQL.toString()));
			file_bus_sql.append("\n");
			bustmpfile.write(file_bus_sql.toString());
			/*
			 * Append the collection level SQL to the writer
			 */
			entryInstance.storeCollection(coltmpfile);
			/*
			 * Limit transaction size and messaging
			 */
			if( (line%1000) == 0 ) {
				Messenger.printMsg(Messenger.TRACE,
						this.mapping.getName()
						+ " <" + line + ((table_size <= 0 )?"":("/" + table_size))
						+ "> : Entries  read ");
				Database.gc();
			}
		}
		bustmpfile.close();
		coltmpfile.close();
		if( line > 0 ) {
			Messenger.printMsg(Messenger.TRACE,
					+ line + ((table_size <= 0 )?"":("/" + table_size))
					+ " Entries read: copy the dump files into the database"); 
			SQLTable.addQueryToTransaction("LOADTSVTABLE " + this.metaClass.getName() + " -1 " + busdumpfile);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Business attributes copied");
			String ecoll_table =Database.getCachemeta().getCollectionTableName(entryInstance.getCollection().getName()
					,entryInstance.getCategory());

			SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Collection attributes copied");
			SQLTable.addQueryToTransaction("Update " + tcoll_table
					+ " Set nb_rows_csa=" + line
					+ " Where oidsaada=" + tableOid
					, tcoll_table);
		} else {
			Messenger.printMsg(Messenger.TRACE,
					this.mapping.getName()
					+ " 0  Entry read");

			SQLTable.addQueryToTransaction("Update " + tcoll_table
					+ " Set nb_rows_csa=" + line + " Where oidsaada=" + tableOid
					, tcoll_table);			
		}
		//		(new File(coldumpfile)).delete();
		//		(new File(busdumpfile)).delete();
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
		System.out.println("@@@@@@@@@@ ENTRY  updateAttributeHandlerValues");
		this.dataFile.updateEntryAttributeHandlerValues(this);
	}

}
