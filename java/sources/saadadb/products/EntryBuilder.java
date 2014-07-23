package saadadb.products;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.sqltable.SQLTable;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;


/**
 * @author michel
 * @version $Id$
 */
public class EntryBuilder extends ProductBuilder {
	private static final long serialVersionUID = 1L;
	/** The entries table * */
	protected TableBuilder table;

	/**
	 * Constructor. Alias of the constructor Entry(String fileName, String
	 * typeFile) with a table object
	 * 
	 * @param TableBuilder
	 *            The entries table.
	 * @throws FatalException 
	 */
	public EntryBuilder(TableBuilder table) throws SaadaException {
		super(table.dataFile, table.mapping.getEntryMapping());
		this.table = table;
		/*
		 * This operation is done in super(...) then before this table is set.
		 * SO we do it again
		 */
		try {
			this.mapCollectionAttributes();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
		this.productAttributeHandler = this.table.dataFile.getEntryAttributeHandler();
		System.out.println(this.productAttributeHandler);
		this.setFmtsignature();
	}

	/**
	 * Can be overloaded to use another ingestor
	 * @throws Exception
	 */
	protected void setProductIngestor() throws Exception{
		if( this.productIngestor == null ){
			this.productIngestor = new EntryIngestor(this);
		}		
	}


	/**
	 * The specific method for the table entries redefining the homonymous
	 * method of the products class. This method load the entries attributes in
	 * the data base and makes a persistent object. She loads the collection
	 * values of entries in data base and makes this persistent object.
	 * 
	 * @param Configuration
	 *            The configuration of the product.
	 * @param String
	 *            The name of the class product.
	 * @param SaadaDBConf
	 *            The configuration Object of the Saada database.
	 * @param Database
	 *            The current database.
	 * @return void.
	 * @throws Exception 
	 */
	@Override
	public void loadValue() throws Exception {
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
		int table_size = table.getNRows();
		//boolean vectfieldexist = false;
		long time_tag = (new Date()).getTime();
		String         busdumpfile = Repository.getTmpPath() + Database.getSepar()  + "bus" + time_tag + ".psql";
		BufferedWriter bustmpfile  = new BufferedWriter(new FileWriter(busdumpfile));
		String         coldumpfile = Repository.getTmpPath() + Database.getSepar()  + "col" + time_tag + ".psql";
		BufferedWriter coltmpfile  = new BufferedWriter(new FileWriter(coldumpfile));
		String tcoll_table = Database.getCachemeta().getCollectionTableName(this.metaclass.getCollection_name()
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
			this.productIngestor.bindInstanceToFile(entryInstance);
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
			file_bus_sql.insert(0, entryIngestor.saadaInstance.oidsaada + "\t" + entryIngestor.saadaInstance.getObs_id() + "\t" + MD5Key.calculMD5Key(md5KeySQL.toString()));
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
			SQLTable.addQueryToTransaction("LOADTSVTABLE " + this.metaclass.getName() + " -1 " + busdumpfile);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Business attributes copied");
			String ecoll_table =Database.getCachemeta().getCollectionTableName(entryInstance.getCollection().getName()
					,entryInstance.getCategory());

			SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Collection attributes copied");
			SQLTable.addQueryToTransaction("Update " + tcoll_table
					+ " Set nb_rows_csa=" + line
					+ " Where oidsaada=" + table.getTableOid()
					, tcoll_table);
		} else {
			Messenger.printMsg(Messenger.TRACE,
					this.mapping.getName()
					+ " 0  Entry read");

			SQLTable.addQueryToTransaction("Update " + tcoll_table
					+ " Set nb_rows_csa=" + line + " Where oidsaada=" + table.getTableOid()
					, tcoll_table);			
		}
		//		(new File(coldumpfile)).delete();
		//		(new File(busdumpfile)).delete();
	}



	/**

	 * @return Returns the table.
	 */
	public TableBuilder getTable() {
		return table;
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
	public void bindDataFile(DataFile dataFile) throws SaadaException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Start ENTRY mapping");
		//this.typeFile      = this.table.typeFile;
		//this.productFile   = this.table.productFile;
		/*
		 * This operation van be done in super(...) then before this table is set.
		 */
		if( this.table != null ) {
			this.productAttributeHandler = this.table.dataFile.getEntryAttributeHandler();
			this.setFmtsignature();
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductBuilder#getReport()
	 */
	public Map<String, ColumnSetter> getReportXX() throws Exception {
		this.setProductIngestor();
		if( this.productIngestor.hasMoreElements() ) {
			((EntryIngestor)(this.productIngestor)).mapIndirectionTables();
			this.productIngestor.bindInstanceToFile(null);
		}
		SaadaInstance si = this.productIngestor.saadaInstance;
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("obs_collection"); ah.setNameorg("obs_collection"); 
		ah.setValue(si.getFieldValue("obs_collection").toString());
		ah.setComment(this.getReportOnAttRef("obs_collection", obs_collectionSetter));
		retour.put("obs_collection", ah);

		ah = new AttributeHandler();
		ah.setNameattr("target_name"); ah.setNameorg("target_name"); 
		ah.setValue(si.getFieldValue("target_name").toString());
		ah.setComment(this.getReportOnAttRef("target_name", target_nameSetter));
		retour.put("target_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("facility_name"); ah.setNameorg("facility_name"); 
		ah.setValue(si.getFieldValue("facility_name").toString());
		ah.setComment(this.getReportOnAttRef("facility_name", facility_nameSetter));
		retour.put("facility_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("instrument_name"); ah.setNameorg("instrument_name"); 
		ah.setValue(si.getFieldValue("instrument_name").toString());
		ah.setComment(this.getReportOnAttRef("instrument_name", instrument_nameSetter));
		retour.put("instrument_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("s_ra"); ah.setNameorg("s_ra"); 
		ah.setValue(si.getFieldValue("s_ra").toString());
		ah.setComment(this.getReportOnAttRef("s_ra", s_raSetter));
		retour.put("s_ra", ah);

		ah = new AttributeHandler();
		ah.setNameattr("s_dec"); ah.setNameorg("s_dec"); 
		ah.setValue(si.getFieldValue("s_dec").toString());
		ah.setComment(this.getReportOnAttRef("s_dec", s_decSetter));
		retour.put("s_dec", ah);

		ah = new AttributeHandler();
		ah.setNameattr("s_resolution"); ah.setNameorg("s_resolution"); 
		ah.setValue(si.getFieldValue("s_resolution").toString());
		ah.setComment(this.getReportOnAttRef("s_resolution", s_resolutionSetter));
		retour.put("error_maj_csa", ah);

		ah = new AttributeHandler();
		ah.setNameattr("em_min"); ah.setNameorg("em_min"); 
		ah.setValue(si.getFieldValue("em_min").toString());
		ah.setComment(this.getReportOnAttRef("em_min", em_minSetter));
		retour.put("em_min", ah);

		ah = new AttributeHandler();
		ah.setNameattr("em_max"); ah.setNameorg("em_max"); 
		ah.setValue(si.getFieldValue("em_max").toString());
		ah.setComment(this.getReportOnAttRef("em_max", em_maxSetter));
		retour.put("em_max", ah);

		ah = new AttributeHandler();
		ah.setNameattr("t_max"); ah.setNameorg("t_max"); 
		ah.setValue(si.getFieldValue("t_max").toString());
		ah.setComment(this.getReportOnAttRef("t_max", t_maxSetter));
		retour.put("t_max", ah);

		ah = new AttributeHandler();
		ah.setNameattr("t_min"); ah.setNameorg("t_min"); 
		ah.setValue(si.getFieldValue("t_min").toString());
		ah.setComment(this.getReportOnAttRef("t_min", t_minSetter));
		retour.put("t_min", ah);

		//		for( AttributeHandler eah: this.extended_attributes_ref.values()){
		//			ah = new AttributeHandler();
		//			String ahname = eah.getNameattr();
		//			ah.setNameattr(ahname); ah.setNameorg(ahname); 
		//			ah.setValue(si.getFieldValue(ahname).toString());
		//			ah.setComment(this.getReportOnAttRef(ahname, eah));
		//			retour.put(ahname, ah);      	
		//		}

		for( Field f: si.getCollLevelPersisentFields() ){
			String fname = f.getName();
			if( retour.get(fname) == null ){
				ah = new AttributeHandler();
				ah.setNameattr(fname); ah.setNameorg(fname); 
				Object o = si.getFieldValue(fname);
				ah.setValue((o == null)? SaadaConstant.STRING:o.toString());
				ah.setComment("Computed internally by Saada");				
				retour.put(fname, ah);
			}
		}
		return null;
	}

}
