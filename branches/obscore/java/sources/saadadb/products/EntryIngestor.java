/**
 * 
 */
package saadadb.products;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Enumeration;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.products.datafile.DataFile;
import saadadb.products.mergeandcast.ClassMerger;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.util.DateUtils;
import saadadb.util.Merger;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;

/**
 * @author michel
 * @version $Id$
 */
public final class EntryIngestor extends ProductIngestor {
	/**
	 *  Position of the extended attributes .
	 * index in the array: num of the attribute
	 * value: number of the read column matching the attribute
	 * takes -1 if no column matches
	 */
	private    int[] num_ext_att   = new int[0];
	/**
	 *  Position of the components of the name.
	 * index in the array: num of the component
	 * value: number of the read column matching the component
	 * takes -1 if no column matches
	 */
	private    int[] num_name_att  = new int[0];
	/**
	 * Position of the class attribute in Saada.
	 * index in the array: num of the atribute
	 * value: number of the read column matching the attribute
	 * takes -1 if no column matches
	 */
	protected  int[] index_pos_att = new int[0];
	/**
	 * Index in  the array: Number or position of the attribute to store
	 * value: Number of the read column populating that attribute
	 * ex: read in data product the columns C1 C2 C3 but C2 is ignored
	 *     then index_pos_col = {0, 2}
	 */
	private  int[] index_pos_col = new int[0];
	/**
	 * Position of the component used to compute the MD5 .
	 * index in the array: num of the attribute
	 * value: number of the read column matching the attribute
	 * takes -1 if no column matches
	 */
	protected  boolean[] index_pos_md5 = new boolean[0];
	/**
	 * Maps the type of  the business attribute types as they are read with the data product
	 */
	private  String[]  type_bus_att  = new String[0];
	/**
	 * Maps the type of  the business attribute names as they are read with the data product
	 */
	private  String[]  sqlfields     = new String[0];
	/** 
	 * Pointers on mapped columns
	 * Refer to the position in the data read within the produc
	 */
	private  int num_col_ra      = -1;
	private  int num_col_dec     = -1;
	private  int num_col_fov  = -1;
	private  int num_col_err  = -1;
	//	private  int num_col_dec_err = -1;
	//	private  int num_col_angle_err = -1;
	private  int num_col_em_max  = -1;
	private  int num_col_em_min  = -1;
	private  int num_col_em_res_power  = -1;
	private  int num_col_t_max   = -1;
	private  int num_col_t_min   = -1;
	private  int num_col_t_exptime   = -1;
	/** number of business attribute in the Saada class*/
	protected int nb_bus_att = -1;
	/**
	 * Enumerator on the product row
	 */
	@SuppressWarnings("rawtypes")
	private Enumeration enumerateRow;
	/**
	 * Values read in the current row
	 */
	protected  Object[] values;
	/**
	 * the same for all row
	 */
	//private long oidTable;
	/**
	 * oidsaada must be incremented when false;
	 */
	private boolean firstCall = true;
	private long lineNumber=0;;

	/**
	 * The element position matches the position of the columns within the SaadaDB 
	 * The element value matches  the position of the value read in the file
	 */
	private int[] busIndirectionTable;
	/**
	 * The element position matches the position of the value read in the file
	 * The element value matches  the position of the columns within the SaadaDB 
	 */
	private int[] busReverseIndirectionTable;
	/**
	 * @param product
	 * @throws Exception
	 */
	EntryIngestor(EntryBuilder product) throws Exception {
		super(product);
		this.enumerateRow = product.elements();
		//		this.oidTable = product.productIngestor.saadaInstance.oidsaada;
		//		((EntrySaada)(this.saadaInstance)).oidtable = this.oidTable;
		this.addMEssage = false;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#buildInstance()
	 */
	@Override
	protected void buildInstance() throws Exception {
		/*
		 * Build the Saada instance
		 */
		if( this.product.metaClass != null ) {
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(this.product.metaClass.getName()).newInstance();
			this.saadaInstance.oidsaada =  SaadaOID.newOid(this.product.metaClass.getName());
			this.buildOrderedBusinessAttributeList();
		} else {
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(Category.explain(this.product.mapping.getCategory()) + "UserColl").newInstance();
			this.saadaInstance.oidsaada = SaadaConstant.LONG;	
		}
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#bindInstanceToFile(saadadb.collection.obscoremin.SaadaInstance, long)
	 */
	@Override
	public void bindInstanceToFile() throws Exception {
		//this.nextElement();
		if( this.product.metaClass != null ) {
			this.saadaInstance.oidsaada = SaadaOID.newOid(this.product.metaClass.getName());
		} else {
			this.firstCall = false;
		}
		this.product.calculateAllExpressions();
		this.setObservationFields();
		this.setSpaceFields();
		this.setEnergyFields();		
		this.setTimeFields();
		this.loadAttrExtends();
		this.setContentSignature();
		this.numberOfCall++;
	}

	/**
	 * @return
	 */
	@Override
	public boolean hasMoreElements() {
		return this.enumerateRow.hasMoreElements();
	}

	/**
	 * Iterate once on the data rows and update the values of the builder's attrobuteHandlers
	 * 
	 */
	private void nextElement() {
		this.values = (Object[])enumerateRow.nextElement();
		/*
		 * Set first raws values in the attribute handlers
		 * can be used for reporting
		 */
		int cpt = 0;
		for( AttributeHandler ah: this.product.productAttributeHandler.values()) {
			ah.setValue(this.values[cpt].toString());
			cpt++;
		}
		this.lineNumber++;
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setObservationFields()
	 */
	@Override
	protected void setObservationFields() throws SaadaException {

		this.saadaInstance.obs_id  = this.getInstanceName(null);
		this.saadaInstance.setAccess_url(this.saadaInstance.getDownloadURL(false));	
		this.saadaInstance.setAccess_format(this.product.mimeType);
		this.saadaInstance.setDate_load(new java.util.Date().getTime());
		this.saadaInstance.setAccess_estsize(this.product.length());
		setField("target_name"    , this.product.target_nameSetter);
		setField("instrument_name", this.product.instrument_nameSetter);
		setField("facility_name"  , this.product.facility_nameSetter);
		setField("obs_collection" , this.product.obs_collectionSetter);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setPositionFields(int)
	 */
	@Override
	protected void setSpaceFields() throws Exception {
		//		System.out.println("### " +this.num_col_ra);
		//		System.out.println("### " + this.values[this.num_col_ra]);
		//		if( this.values != null ){
		//			if( this.num_col_ra != -1 && this.product.s_raSetter.byKeyword())
		//				this.product.s_raSetter.setValue(this.values[this.num_col_ra].toString());
		//			if( this.num_col_dec != -1 && this.product.s_decSetter.byKeyword())
		//				this.product.s_decSetter.setValue(this.values[this.num_col_dec].toString());
		//			if( this.num_col_fov != -1 && this.product.s_fovSetter.byKeyword())
		//				this.product.s_fovSetter.setValue(this.values[this.num_col_fov].toString());
		//			if( this.num_col_err != -1 && this.product.s_resolutionSetter.byKeyword())
		//				this.product.s_resolutionSetter.setValue(this.values[this.num_col_err].toString());
		//		}
		if( this.numberOfCall == 0 ){
			super.setSpaceFields();
		} else {
			this.product.s_raSetter.calculateExpression();
			if( !this.product.astroframeSetter.isNotSet() && !this.product.s_raSetter.isNotSet() && !this.product.s_decSetter.isNotSet()) {
				try {
					Astroframe af = (Astroframe)(this.product.astroframeSetter.storedValue);
					String stc = "Polygon " + Database.getAstroframe();
					/*
					 * Convert astroframe if different from this of the database
					 */
					if( this.taskMap.convertUnits) {
						this.taskMap.convertUnits = true;
						this.setConvertedCoordinatesAndRegion();
						/*
						 * no conversion required, just take the values
						 */
					} else {
						this.setUnconvertedCoordinatesAndRegion();
					}
					this.setXYZfields();
					this.setFoVFields();
					this.setPosErrorFields();
				} catch( Exception e ) {
					e.printStackTrace();
					this.setPositionFieldsInError("Error while setting the position " + e.getMessage());
				}

			} else {
				this.setPositionFieldsInError("Coord conv failed: no frame");
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Cannot convert position since there is no frame");
			}
		}

		/*
		 * Belongs to the observation axis but needs the coordinates
		 */
		this.saadaInstance.obs_id = this.getInstanceName("#" + this.lineNumber);
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setEnegryFields()
	 */
	@Override
	protected void setEnergyFields() throws SaadaException {
		if( this.values != null ){
			if( this.num_col_em_max != -1 && this.product.em_maxSetter.byKeyword() )
				this.product.em_maxSetter.setByValue(this.values[this.num_col_em_max].toString(), true);
			if( this.num_col_em_min != -1 && this.product.em_minSetter.byKeyword() )
				this.product.em_minSetter.setByValue(this.values[this.num_col_em_min].toString(), true);
			if( this.num_col_em_res_power != -1 && this.product.em_res_powerSetter.byKeyword())
				this.product.em_res_powerSetter.setByValue(this.values[this.num_col_em_res_power].toString(), true);
		}
		super.setEnergyFields();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setTimeFields()
	 */
	@Override
	protected void setTimeFields() throws SaadaException {
		try {
			if( this.values != null ){
				if( this.num_col_t_max != -1 && this.product.t_maxSetter.byKeyword() ) {
					this.product.t_maxSetter.setByValue(this.values[this.num_col_t_max].toString(), true);
					this.product.t_maxSetter = this.product.t_maxSetter.setConvertedValue(DateUtils.getFMJD(this.product.t_maxSetter.getValue()), "String", "mjd", addMEssage);
				}
				if( this.num_col_t_min != -1&& this.product.t_minSetter.byKeyword() ){
					this.product.t_minSetter.setByValue(this.values[this.num_col_t_min].toString(), true);
					this.product.t_minSetter = this.product.t_minSetter.setConvertedValue(DateUtils.getFMJD(this.product.t_minSetter.getValue()), "String", "mjd", addMEssage);
				}
				if( this.num_col_t_exptime != -1 && this.product.t_exptimeSetter.byKeyword())
					this.product.t_exptimeSetter.setByValue(this.values[this.num_col_t_exptime].toString(), true);
			}
			super.setTimeFields();
		} catch (Exception e) {
			IgnoreException.throwNewException(SaadaException.FILE_FORMAT, e);
		}
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setObservableFields()
	 */
	@Override
	protected void setObservableFields() throws SaadaException {
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#loadAttrExtends()
	 */
	@Override
	public void loadAttrExtends() throws SaadaException {
		if( this.product.extended_attributesSetter != null ) {
			int extpos = 0;
			//for( AttributeHandler nah: this.extended_attributes.keySet() ) {
			for( String ext_att_name: this.product.extended_attributesSetter.keySet() ) {
				if( num_ext_att[extpos] != -1 ) {
					this.product.extended_attributesSetter.get(ext_att_name).setByValue(values[num_ext_att[extpos]].toString(), true);
				}
				extpos++;
			}
		}
	}

	/**
	 * Build the instance name from the configuration or take the filename
	 * if the configuration can not be used, the name is made with the 
	 * colelction name followed by the position 
	 * @param suffix not used here 
	 * @return
	 */
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#getInstanceName(java.lang.String)
	 */
	@Override
	protected String getInstanceName(String suffix) {
		/*
		 * If no name has been set right now, put the position after collection.class
		 * Take the suffix is there is no position
		 */
		if( this.product.obs_idSetter.isNotSet()) {
			String name = SaadaOID.getCollectionName(this.saadaInstance.oidsaada) + "-" + SaadaOID.getClassName(this.saadaInstance.oidsaada);
			double ra =  this.saadaInstance.s_ra;
			double dec = this.saadaInstance.s_dec;
			if( ra != SaadaConstant.DOUBLE && dec != SaadaConstant.DOUBLE ) {
				Astrocoo coo =new Astrocoo(Database.getAstroframe(), this.saadaInstance.s_ra, this.saadaInstance.s_dec);
				coo.setPrecision(5);
				name += coo.toString("s");
			} else {
				name +=  suffix;
			}
			this.product.obs_idSetter.setValue(name);
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Default instance name  <"+ name + ">");
		}
		return this.product.obs_idSetter.getValue();
	}
	
	/**
	 */
	@Override
	protected void buildOrderedBusinessAttributeList(){
		this.busIndirectionTable = new int[this.product.metaClass.getAttributes_handlers().size()];
		this.busReverseIndirectionTable = new int[this.product.metaClass.getAttributes_handlers().size()];
		for( int i=0 ; i<this.busIndirectionTable.length ; i++ ){
			this.busIndirectionTable[i] = -1;
			this.busReverseIndirectionTable[i] = -1;
		}
		int classCpt = 0 ;
		for( AttributeHandler ah:  this.product.metaClass.getAttributes_handlers().values() ){
			int readCpt = 0 ;
			String na = ah.getNameattr();
			for( AttributeHandler ah2: this.product.dataFile.entryAttributeHandlers.values()){
				if( ah2.getNameattr().equals(na)){
					this.busIndirectionTable[readCpt] = classCpt;
					this.busReverseIndirectionTable[classCpt] = readCpt;
					break;
				}
				readCpt++;
			}
			classCpt++;	
		}
	}

	/**
	 * 
	 */
	public void mapIndirectionTables() {
		//AttributeHandler[] saada_ah = this.product.metaclass.getClassAttributes();
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Map the indirection tables for the entry table");
		AttributeHandler[] saada_ah ;
		if(this.product.metaClass == null  ) {
			saada_ah = this.product.productAttributeHandler.values().toArray(new AttributeHandler[0]);			
		} else {
			saada_ah = this.product.metaClass.getClassAttributes();
		}
		int num_att_read=0;
		nb_bus_att    = saada_ah.length;
		type_bus_att  = new String[nb_bus_att];
		index_pos_col = new int[nb_bus_att];
		index_pos_att = new int[nb_bus_att];
		index_pos_md5 = new boolean[nb_bus_att];
		sqlfields     = new String[this.product.productAttributeHandler.size()];
		num_ext_att   = new int[this.product.extended_attributesSetter.size()];
		for( int i=0 ; i<num_ext_att.length ; i++ ) {
			num_ext_att[i] = -1;
		}
		//num_name_att = new int[this.product.name_components.size()];
		num_name_att = new int[8];
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
		for( AttributeHandler attribute: this.product.productAttributeHandler.values()) { 
			/*
			 * Columns ignored have been stored in the freeIndex by FitsProduct
			 */
			while( this.product.mapping.hasInFreeIndex(read_col)  ) read_col++;
			index_pos_col[num_att] = read_col;
			num_att++;
			read_col++;
		}
		//System.exit(1);
		for( AttributeHandler attribute: this.product.productAttributeHandler.values()) { 
			/*
			 * table index_pos_att gives for each class attribute the position of
			 * its value in the vector return by the product file
			 */
			String nameattr = attribute.getNameattr();
			for( int ba=0 ; ba<saada_ah.length ; ba++ ) {
				if( saada_ah[ba].getNameattr().equals(nameattr) ) {
					// att pos in meta class = pos in data read
					index_pos_att[ba] = index_pos_col[num_att_read];
					index_pos_md5[ba] = true;														
				}
			}
			/*
			 * Map business attributes. 
			 * They can be read in an order differenet as the class order
			 */
			type_bus_att[num_att_read] = attribute.getType();
			sqlfields[num_att_read] = attribute.getNameattr();
			/*
			 * map collection attributes
			 */
			String nameField = attribute.getNameattr();
			System.out.println(this.product.s_raSetter.getAttNameAtt() + " " +nameField );
			if( this.product.s_raSetter != null && nameField.equals(this.product.s_raSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA col (" + nameField + ") read in column #" + num_att_read);
				num_col_ra = index_pos_col[num_att_read];
			} else if( this.product.s_decSetter != null && nameField.equals(this.product.s_decSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC col (" + nameField + ") read in column #" + num_att_read);
				num_col_dec = index_pos_col[num_att_read];
			} else if( this.product.s_fovSetter != null && nameField.equals(this.product.s_fovSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "FoV col (" + nameField + ") read in column #" + num_att_read);
				num_col_fov = index_pos_col[num_att_read];
			} else if( this.product.s_resolutionSetter != null && nameField.equals(this.product.s_resolutionSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA error col (" + nameField + ") read in column #" + num_att_read);
				num_col_err = index_pos_col[num_att_read];
			} else if( this.product.em_maxSetter != null && nameField.equals(this.product.em_maxSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "em_max column col (" + nameField + ") read in column #" + num_att_read);
				num_col_em_max = index_pos_col[num_att_read];
			} else if( this.product.em_minSetter != null && nameField.equals(this.product.em_minSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "em_min column col (" + nameField + ") read in column #" + num_att_read);
				num_col_em_min = index_pos_col[num_att_read];
			} else if( this.product.em_res_powerSetter != null && nameField.equals(this.product.em_res_powerSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "num_col_em_res_power column col (" + nameField + ") read in column #" + num_att_read);
				num_col_em_res_power = index_pos_col[num_att_read];
			} else if( this.product.t_maxSetter != null && nameField.equals(this.product.t_maxSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "t_max column col (" + nameField + ") read in column #" + num_att_read);
				num_col_t_max = index_pos_col[num_att_read];
			} else if( this.product.t_minSetter != null && nameField.equals(this.product.t_minSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "t_min column col (" + nameField + ") read in column #" + num_att_read);
				num_col_t_min = index_pos_col[num_att_read];
			} else if( this.product.t_exptimeSetter != null && nameField.equals(this.product.t_exptimeSetter.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "t_exptime column col (" + nameField + ") read in column #" + num_att_read);
				num_col_t_exptime = index_pos_col[num_att_read];
			}

			if( this.product.extended_attributesSetter != null ) {
				int extpos=0;
				for( String ext_att_name: this.product.extended_attributesSetter.keySet() ) {
					String mapped_ext_att = this.product.extended_attributesSetter.get(ext_att_name).getAttNameAtt();
					if( mapped_ext_att.equals(nameField)  ) {
						num_ext_att[extpos] = index_pos_col[num_att_read];
						emsg += "(" + mapped_ext_att + " col#" + num_att_read + ") ";
					}
					extpos++;
				}
			}
			//			if( this.product.name_components != null ) {
			//				int namepos=0;
			//				for( AttributeHandler nah: this.product.name_components ) {
			//					if( nah.getNameattr().equals(nameField)) {
			//						num_name_att[namepos] = index_pos_col[num_att_read];	
			//						nmsg += "(" + nah.getNameattr() + " col#" + num_att_read + ") ";
			//					}
			//					namepos++;
			//				}
			//			}									
			num_att_read++;
		}
		//		if( this.product.name_components != null ) {
		//			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,nmsg);
		//		}
		if( this.product.extended_attributesSetter != null ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,emsg);
		}
	}

	public void showCollectionValues() throws Exception {
		for( Field f: EntrySaada.class.getFields() ) {
			System.out.print(f.get(this.saadaInstance).toString() + "|");
		}
		System.out.println();
	}

	
	
	/**
	 * @throws Exception
	 */
	public void loadValue(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception {
		EntryBuilder product = (EntryBuilder) this.product;
		DataFile dataFile = product.dataFile;
		this.buildOrderedBusinessAttributeList();
		String[] rowData = new String[dataFile.entryAttributeHandlers.size() + 3];
		((EntrySaada)(this.saadaInstance)).oidtable = product.oidTable;
		int numRow = 0;
		while (this.hasMoreElements()) {
			for( int i=0 ; i<rowData.length ; i++) rowData[i] = null;
			/*
			 * Data row is read by updateEntryAttributeHandlerValues()
			 */
			dataFile.updateEntryAttributeHandlerValues(product);
			this.bindInstanceToFile();
			rowData[0] = String.valueOf(this.saadaInstance.oidsaada);
			rowData[1] = this.saadaInstance.obs_id;
			rowData[2] = this.saadaInstance.contentsignature;
			int cpt = 0;
			for( AttributeHandler ah: dataFile.entryAttributeHandlers.values()){
				int index = this.busReverseIndirectionTable[cpt];
				rowData[index + 3] =  ClassMerger.getCastedSQLValue(ah, ah.getType());
				cpt++;
			}
			buswriter.write(Merger.getMergedArray("\t", rowData) + "\n");
			this.saadaInstance.storeCollection(colwriter);
			numRow ++;
			if( numRow % 1000 == 0 ){
				Messenger.printMsg(Messenger.TRACE, numRow + "/" + dataFile.getNRows() + " rows read");				
			}
		}
		Messenger.printMsg(Messenger.TRACE, numRow + "/" + dataFile.getNRows() + " rows read");

	}

	/**
	 * @param colwriter
	 * @param buswriter
	 * @param loadedfilewriter
	 * @throws Exception
	 */
	public void loadValueXXX(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		if( Messenger.debug_mode == true && Table_Saada_Loaded_File.productAlreadyExistsInDB(this.product) ) {
			Messenger.printMsg(Messenger.WARNING, " The object <"
					+ this.saadaInstance.obs_id+ "> in Collection <"
					+ this.saadaInstance.getCollection().getName() + "> with md5 <"
					+ this.saadaInstance.contentsignature + "> exists in the data base <"
					+ Database.getName() + ">");	
		}
		this.saadaInstance.storeCollection(colwriter);
		String file_bus_sql = this.saadaInstance.oidsaada + "\t" + this.saadaInstance.obs_id + "\t" + this.saadaInstance.contentsignature;
		for( int i=0 ; i<this.busIndirectionTable.length ; i++ ){
			int ind = this.busIndirectionTable[i];
			file_bus_sql += "\t" + ((ind == -1)? "NULL": values[this.busIndirectionTable[i]]);			
		}
		buswriter.write(file_bus_sql + "\n");
	}

}
