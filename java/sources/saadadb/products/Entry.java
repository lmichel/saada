package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.EntrySaada;
import saadadb.collection.SaadaInstance;
import saadadb.collection.SaadaOID;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.prdconfiguration.ConfigurationEntry;
import saadadb.prdconfiguration.ConfigurationTable;
import saadadb.sqltable.SQLTable;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;


public class Entry extends Product {
	/**
	 *  * @version $Id: Entry.java 887 2014-01-14 09:33:09Z laurent.mistahl $

	 */
	private static final long serialVersionUID = 1L;

	/** The entries table * */
	protected Table table;

	/** A md5 value corresponding to a sql query for a entry loading* */
	public String md5KeySQL = "";

	/**
	 * Constructor. Alias of the constructor Entry(String fileName, String
	 * typeFile) with a table object
	 * 
	 * @param Table
	 *            The entries table.
	 */
	public Entry(Table table) {
		super(table.file, table.mapping.getEntryMapping());
		this.table = table;
	}



	/**
	 * Returns the list which maps entry names formated in the standard of Saada
	 * (keys) to their objects modelling attribute informations (values).
	 * Generally the object modelling attribute informations is a
	 * AttributeHandler. This method contrary in the method getKW() does not
	 * model the list in memory. So that this one does not return null, it is
	 * beforehand necessary to have already created the list with the other
	 * method (getKW()). Cross_reference of the homonymous method defined in the
	 * current object product (and by relationship in the current product file).
	 * 
	 * @return Hashtable The list which maps attribute names to their
	 *         informations.
	 */
	@Override
	public Map<String, AttributeHandler> getProductAttributeHandler() {
		return productAttributeHandler;
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
		// A entry attribute
		// A table line of objets (equals a entry in objects format)
		Object[] row;
		// A identification number of entry class
		// A instance name of a entry
		String instanceName;
		// A md5 value of a entry
		String mD5keySaada;
		// A java type of a attribute
		// A field name (corresponding to the changed name of a entry and
		// the field name in data base)
		String nameField;
		String file_bus_sql;
		// Initialzes an enumeration of entries (table rows)
		long oidtable = table.getSaadainstance().getOid();
		Enumeration enumerateRow = table.elements();
		int num_col_ra      = -1;
		int num_col_dec     = -1;
		int num_col_ra_err  = -1;
		int num_col_dec_err = -1;
		int num_col_angle_err = -1;
		int[] num_ext_att   = new int[0];
		int[] num_name_att  = new int[0];
		int[] index_pos_att = new int[0];
		/*
		 * indice: position in table of attribute handler
		 * value: position in data read;
		 */
		int[] index_pos_col = new int[0];
		boolean[] index_pos_md5 = new boolean[0];
		String[]  type_bus_att  = new String[0];
		String[]  sqlfields     = new String[0];
		//int[]     jtypefields   = new int[0];
		Object[]  values        = new Object[0];
		int nb_bus_att          = -1;
		int table_size = table.getNRows();
		//boolean vectfieldexist = false;
		long time_tag = (new Date()).getTime();
		String         busdumpfile = Repository.getTmpPath() + Database.getSepar()  + "bus" + time_tag + ".psql";
		BufferedWriter bustmpfile  = new BufferedWriter(new FileWriter(busdumpfile));
		String         coldumpfile = Repository.getTmpPath() + Database.getSepar()  + "col" + time_tag + ".psql";
		BufferedWriter coltmpfile  = new BufferedWriter(new FileWriter(coldumpfile));
		long newoid = SaadaOID.newOid(this.metaclass.getName());
		this.saadainstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(this.metaclass.getName()).newInstance();
		String tcoll_table = Database.getCachemeta().getCollectionTableName(this.metaclass.getCollection_name()
				, Category.TABLE);
		EntrySaada entrysaada = (EntrySaada)this.saadainstance;
		/*
		 * The astroframe is the same for all entries it must be computed once
		 */
		this.setAstrofFrame();
		while (enumerateRow.hasMoreElements()) {
			// Initializes the row with the next element of this enumeration
			// if this enumeration of rows has at least one more element to
			// provide
			Object x =  enumerateRow.nextElement();
			row = (Object[]) x;
			instanceName = "";
			md5KeySQL = "";
			/*
			 * Use the first row to map atttribute read into the product file onto the 
			 * Saada class attribute (collection)
			 * A FAIRE: Traitement des attributs ignor�s. 
			 * Fait dans VOTable mais pas dans FITS
			 */
			if( line == 0 ) {
				AttributeHandler[] saada_ah = this.metaclass.getClassAttributes();
				int num_att_read=0;
				nb_bus_att    = saada_ah.length;
				type_bus_att  = new String[nb_bus_att];
				index_pos_col = new int[nb_bus_att];
				index_pos_att = new int[nb_bus_att];
				index_pos_md5 = new boolean[nb_bus_att];
				sqlfields     = new String[productAttributeHandler.size()];
				//jtypefields   = new int[tableAttributeHandler.size()];
				values        = new Object[row.length];
				num_ext_att   = new int[this.extended_attributes.size()];
				for( int i=0 ; i<num_ext_att.length ; i++ ) {
					num_ext_att[i] = -1;
				}
				num_name_att = new int[this.name_components.size()];
				for( int i=0 ; i<num_name_att.length ; i++ ) {
					num_name_att[i] = -1;
				}
				String emsg = "Extended attribute: ";
				String nmsg = "Instance name component: ";
				for( int ba=0 ; ba<saada_ah.length ; ba++ ) {
					index_pos_att[ba] = -1;
					index_pos_col[ba] = -1;
				}
				int read_col=0;
				int num_att=0;
				for( AttributeHandler attribute: productAttributeHandler.values()) { 
					while( this.mapping.hasInFreeIndex(read_col)  ) read_col++;
					index_pos_col[num_att] = read_col;
					num_att++;
					read_col++;
				}
				//System.exit(1);
				for( AttributeHandler attribute: productAttributeHandler.values()) { 
					/*
					 * table index_pos_att gives for each class attribute the position of
					 * its value in the vector return by the product file
					 */
					String nameattr = attribute.getNameattr();
					for( int ba=0 ; ba<saada_ah.length ; ba++ ) {
						if( saada_ah[ba].getNameattr().equals(nameattr) ) {
							// att pos in meta class = pos in data read
							index_pos_att[ba] = index_pos_col[num_att_read];
							/*
							 * Ugly patch for ACDS source unicity in the XCATDB
							 */
							if( (this.mapping.getCollection().equals("ACDS") || this.mapping.getCollection().equals("ARCH_CAT")) &&
									("_cat_num".equals(nameattr) || "_d_epic_cat".equals(nameattr) || "_src_num".equals(nameattr)) ) {
								index_pos_md5[ba] = false;						
							}
							else {
								index_pos_md5[ba] = true;														
							}
						}
					}
					Object rw = row[index_pos_col[num_att_read]];
					/*
					 * Map business attributes. 
					 * They can be read in an order differenet as the class order
					 */
					type_bus_att[num_att_read] = attribute.getType();
					sqlfields[num_att_read] = attribute.getNameattr();
					/*
					 * map collection attributes
					 */
					nameField = attribute.getNameattr();
					if( this.ra_attribute != null && nameField.equals(this.ra_attribute.getNameattr()) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA col (" + nameField + ") read in column #" + num_att_read);
						num_col_ra = index_pos_col[num_att_read];
					}
					else if( this.dec_attribute != null && nameField.equals(this.dec_attribute.getNameattr()) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC col (" + nameField + ") read in column #" + num_att_read);
						num_col_dec = index_pos_col[num_att_read];
					}
					else if( this.maj_err_attribute != null && nameField.equals(this.maj_err_attribute.getNameattr()) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA error col (" + nameField + ") read in column #" + num_att_read);
						num_col_ra_err = index_pos_col[num_att_read];
					}
					else if( this.min_err_attribute != null && nameField.equals(this.min_err_attribute.getNameattr()) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC error col (" + nameField + ") read in column #" + num_att_read);
						num_col_dec_err = index_pos_col[num_att_read];
					}
					else if( this.angle_err_attribute != null && nameField.equals(this.angle_err_attribute.getNameattr()) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC error col (" + nameField + ") read in column #" + num_att_read);
						num_col_angle_err = index_pos_col[num_att_read];
					}
					if( this.extended_attributes != null ) {
						int extpos=0;
						//for( AttributeHandler extah: this.extended_attrikeySetbutes ) {
						for( String ext_att_name: this.extended_attributes.keySet() ) {
							String mapped_ext_att = this.extended_attributes.get(ext_att_name).getNameattr();
							if( mapped_ext_att.equals(nameField)  ) {
								num_ext_att[extpos] = index_pos_col[num_att_read];
								emsg += "(" + mapped_ext_att + " col#" + num_att_read + ") ";
							}
							extpos++;
						}
					}
					if( this.name_components != null ) {
						int namepos=0;
						for( AttributeHandler nah: this.name_components ) {
							if( nah.getNameattr().equals(nameField)) {
								num_name_att[namepos] = index_pos_col[num_att_read];	
								nmsg += "(" + nah.getNameattr() + " col#" + num_att_read + ") ";
							}
							namepos++;
						}
					}									
					num_att_read++;
				}
				if( this.name_components != null ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,nmsg);
				}
				if( this.extended_attributes != null ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,emsg);
				}
			} // first line processing
			line++;
			/*
			 * If cell values are vectors, we take the first element
			 * http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/types.html
			 */
			values = row;
			/*
			 * Build the Saada instance (collection level)
			 */
			if( num_col_ra != -1 )
				this.ra_attribute.setValue(values[num_col_ra].toString());
			if( num_col_dec != -1 )
				this.dec_attribute.setValue(values[num_col_dec].toString());
			if( num_col_ra_err != -1 )
				this.maj_err_attribute.setValue(values[num_col_ra_err].toString());
			if( num_col_dec_err != -1 )
				this.min_err_attribute.setValue(values[num_col_dec_err].toString());
			if( num_col_angle_err != -1 )
				this.angle_err_attribute.setValue(values[num_col_angle_err].toString());
			if( this.name_components != null ) {
				int namepos = 0;
				for( AttributeHandler nah: this.name_components ) {
					if( num_name_att[namepos] != -1 ) {
						nah.setValue(values[num_name_att[namepos]].toString());
					}
					namepos++;
				}
			}
			/*
			 * Set extended attribute
			 */
			if( this.extended_attributes != null ) {
				int extpos = 0;
				//for( AttributeHandler nah: this.extended_attributes.keySet() ) {
				for( String ext_att_name: this.extended_attributes.keySet() ) {
					if( num_ext_att[extpos] != -1 ) {
						this.extended_attributes.get(ext_att_name).setValue(values[num_ext_att[extpos]].toString());
					}
					extpos++;
				}
			}
			/*
			 * Set instance name
			 */
			if( this.name_components != null ) {
				for( int i=0 ; i<num_name_att.length ; i++ ) {
					if( num_name_att[i] != -1 ) {
						this.name_components.get(i).setValue(values[num_name_att[i]].toString());
					}
				}
			}

			this.setPositionFields(line);
			entrysaada.setOid(newoid);
			entrysaada.setOidtable(oidtable);
			instanceName = this.getInstanceName("#" + line);
			entrysaada.setNameSaada(instanceName);
			this.setBasicCollectionFields();
			this.loadAttrExtends();
			/*
			 * Build the SQL query for business table
			 */
			file_bus_sql = "" ;
			String val;
			for( int i=0 ; i<nb_bus_att ; i++  ) {
				file_bus_sql += "\t";
				if( index_pos_att[i] != -1 ) {
					val = values[index_pos_att[i]].toString();
				} else {
					val = Database.getWrapper().getAsciiNull();
				}

				if( val.equals("NaN") || val.equals("") || val.equals("Infinity")) {					
					file_bus_sql +=Database.getWrapper().getAsciiNull();;
				} else if( val.equalsIgnoreCase("false") ) {	
					file_bus_sql +=Database.getWrapper().getBooleanAsString(false);
				}  else if(  val.equalsIgnoreCase("true")) {	
					file_bus_sql +=Database.getWrapper().getBooleanAsString(true);
				} else {
					file_bus_sql += val;
				}
				if( index_pos_md5[i] ) {
					md5KeySQL += val;
				}
			}
			mD5keySaada = MD5Key.calculMD5Key(md5KeySQL);
			file_bus_sql = newoid + "\t" + instanceName + "\t" + mD5keySaada + file_bus_sql;
			file_bus_sql += "\n";
			bustmpfile.write(file_bus_sql);
			/*
			 * Store the object
			 */
			this.saadainstance.storeCollection(coltmpfile);
			//SQLTable.runQueryUpdateSQL("insert into " + saada_class.getName() + "(" + sqlPara + ") values (" + sql + ")");
			/*
			 * Limit transaction size and messaging
			 */
			if( (line%1000) == 0 ) {
				Messenger.printMsg(Messenger.TRACE,
						this.mapping.getName()
						+ " <" + line + ((table_size <= 0 )?"":("/" + table_size))
						+ "> : Entries  read ");
				if( (line % 5000) == 0 ) {
					Database.gc();
				}
			}
			newoid = SaadaOID.newOid(this.metaclass.getName());
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
			String ecoll_table =Database.getCachemeta().getCollectionTableName(this.saadainstance.getCollection().getName()
					, this.saadainstance.getCategory());

			SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Collection attributes copied");
			SQLTable.addQueryToTransaction("Update " + tcoll_table
					+ " Set nb_rows_csa=" + line
					+ " Where oidsaada=" + table.getTableOid()
					, tcoll_table);
		}
		else {
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
	public Table getTable() {
		return table;
	}

	/**
	 * @throws SaadaException 
	 * 
	 */
	@Override
	protected void mapCollectionAttributes() throws SaadaException {
		this.mapInstanceName();
		/*
		 * Coo sys is defined in the table header. 
		 * This definition is moved here at entry level
		 */
		this.astroframe = this.table.astroframe;
		this.system_attribute = this.table.system_attribute;
		this.equinox_attribute = this.table.equinox_attribute;
		/*
		 * Don't map position if there is o astroframe
		 */
		if( this.astroframe != null || this.system_attribute != null) {
			this.mapCollectionPosAttributes();
			this.mapCollectionPoserrorAttributes();
		}
		this.mapIgnoredAndExtendedAttributes();
	}

	/* ######################################################
	 * 
	 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
	 * 
	 *#######################################################*/
	@Override
	public void initProductFile(ProductMapping mapping) throws SaadaException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Init ENTRY instance");
		loadProductFile(mapping);
		this.mapCollectionAttributes();		
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "ENTRY initProductFile " + this.fmtsignature);
	}
	/* (non-Javadoc)
	 * @see saadadb.products.Product#loadProductFile(saadadb.prdconfiguration.ConfigurationDefaultHandler)
	 */
	@Override
	public void loadProductFile(ProductMapping mapping) throws IgnoreException {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Init ENTRY instance");
		this.typeFile      = this.table.typeFile;
		this.mapping = mapping;
		this.productFile   = this.table.productFile;
		this.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		this.productFile.setKWEntry(this.productAttributeHandler);
		this.setFmtsignature();
	}

	/**
	 * Set product url and date of loading
	 * Name is set on the fly
	 * @throws AbortException 
	 */
	@Override
	public void setBasicCollectionFields() throws AbortException {
		this.saadainstance.setProduct_url_csa(((File) this.productFile).getName());	
		this.saadainstance.setDateLoad(new java.util.Date().getTime());
	}

	/**
	 * Build the instance name from the configuration or take the filename
	 * if the configuration can not be used, the name is made with the 
	 * colelction name followed by the position 
	 * @param suffix not used here 
	 * @return
	 */
	@Override
	protected String getInstanceName(String suffix) {
		String name = "";
		if( this.name_components != null ) {
			int cpt = 0;
			for( AttributeHandler ah: this.name_components ) {
				if( cpt > 0 ) {
					name += " " + ah.getValue();
				}
				else {
					name += ah.getValue();					
				}
				cpt++;
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Instance name <" + name + ">");
		}
		/*
		 * If no name has been set right now, put the position after collection.class
		 * Take the suffix is there is no position
		 */
		if( name == null || name.length() == 0 ) {
			name = this.file.getName();
			EntrySaada es= (EntrySaada) this.saadainstance;
			name = SaadaOID.getCollectionName(es.getOid()) + "-" + SaadaOID.getClassName(es.getOid());
			double ra =  es.getPos_ra_csa();
			double dec = es.getPos_dec_csa();
			if( ra != SaadaConstant.DOUBLE && dec != SaadaConstant.DOUBLE ) {
				Astrocoo coo =new Astrocoo(Database.getAstroframe(), es.getPos_ra_csa(), es.getPos_dec_csa());
				coo.setPrecision(5);
				name += coo.toString("s");
			}
			else {
				name +=  suffix;
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Default instance name  <"+ name + ">");
		}
		return name;
	}
}
