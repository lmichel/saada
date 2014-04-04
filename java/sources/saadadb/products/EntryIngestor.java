/**
 * 
 */
package saadadb.products;

import java.util.Enumeration;

import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.EntrySaada;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;

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
	private  int num_col_ra_err  = -1;
	private  int num_col_dec_err = -1;
	private  int num_col_angle_err = -1;
	private  int num_col_em_max  = -1;
	private  int num_col_em_min  = -1;
	private  int num_col_t_max   = -1;
	private  int num_col_t_min   = -1;
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
	private long oidTable;
	/**
	 * oidsaada must ne incremented when false;
	 */
	private boolean firstCall = true;
	private long lineNumber=0;;
	
	/**
	 * @param product
	 * @throws Exception
	 */
	EntryIngestor(EntryBuilder product) throws Exception {
		super(product);
		this.enumerateRow = product.table.elements();
		this.oidTable = product.table.productIngestor.saadaInstance.oidsaada;
		((EntrySaada)(this.saadaInstance)).oidtable = this.oidTable;

		System.out.println("Creator =====================================");
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#bindInstanceToFile(saadadb.collection.obscoremin.SaadaInstance, long)
	 */
	@Override
	public void bindInstanceToFile(SaadaInstance si) throws Exception {
		System.out.println("BInd =====================================");
		this.nextElement();
		if( si != null ) this.saadaInstance = si;
		if( this.product.metaclass != null && ! this.firstCall) {
			this.saadaInstance.oidsaada = SaadaOID.newOid(this.product.metaclass.getName());
			this.firstCall = false;
		}
		this.setObservationFields();
		this.setSpaceFields();
		this.setEnegryFields();		
		this.setTimeFields();
		this.loadAttrExtends();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#hasNextElement()
	 */
	@Override
	public boolean hasMoreElements() {
		return this.enumerateRow.hasMoreElements();
	}

	/**
	 * Iterate once on the data rows
	 */
	private void nextElement() {
		this.values = (Object[])enumerateRow.nextElement();
		/*
		 * Set first raws values in the attribute handlers
		 * can be used for reporting
		 */
		if( this.lineNumber == 0 ) {
			int cpt = 0;
			for( AttributeHandler ah: this.product.productAttributeHandler.values()) {
				ah.setValue(this.values[cpt].toString());
				cpt++;
			}
		}

		this.lineNumber++;

	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setObservationFields()
	 */
	@Override
	protected void setObservationFields() throws SaadaException {
		super.setObservationFields();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setPositionFields(int)
	 */
	@Override
	protected void setPositionFields(int number) throws Exception {
		if( this.values != null ){
			if( this.num_col_ra != -1 )
				this.product.s_ra_ref.setByValue(this.values[this.num_col_ra].toString(), true);
			if( this.num_col_dec != -1 )
				this.product.s_dec_ref.setByValue(this.values[this.num_col_dec].toString(), true);
			if( this.num_col_ra_err != -1 )
				this.product.error_maj_ref.setByValue(this.values[this.num_col_ra_err].toString(), true);
			if( this.num_col_dec_err != -1 )
				this.product.error_min_ref.setByValue(this.values[this.num_col_dec_err].toString(), true);
			if( this.num_col_angle_err != -1 )
				this.product.error_angle_ref.setByValue(this.values[this.num_col_angle_err].toString(), true);
		}
		super.setPositionFields(number);
		/*
		 * Belongs to the obseravtion axis but needs the coordinates
		 */
		this.saadaInstance.obs_id = this.getInstanceName("#" + this.lineNumber);
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setEnegryFields()
	 */
	@Override
	protected void setEnegryFields() throws SaadaException {
		if( this.values != null ){
			if( this.num_col_em_max != -1 )
				this.product.em_max_ref.setByValue(this.values[this.num_col_em_max].toString(), true);
			if( this.num_col_em_min != -1 )
				this.product.em_min_ref.setByValue(this.values[this.num_col_em_min].toString(), true);
		}
		super.setEnegryFields();
	}
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#setTimeFields()
	 */
	@Override
	protected void setTimeFields() throws SaadaException {
		if( this.values != null ){
			if( this.num_col_t_max != -1 )
				this.product.t_max_ref.setByValue(this.values[this.num_col_t_max].toString(), true);
			if( this.num_col_t_min != -1 )
				this.product.t_min_ref.setByValue(this.values[this.num_col_t_min].toString(), true);
		}
		super.setTimeFields();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.products.ProductIngestor#loadAttrExtends()
	 */
	@Override
	public void loadAttrExtends() throws SaadaException {
		if( this.product.extended_attributes_ref != null ) {
			int extpos = 0;
			//for( AttributeHandler nah: this.extended_attributes.keySet() ) {
			for( String ext_att_name: this.product.extended_attributes_ref.keySet() ) {
				if( num_ext_att[extpos] != -1 ) {
					this.product.extended_attributes_ref.get(ext_att_name).setByValue(values[num_ext_att[extpos]].toString(), true);
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
		String name = "";
		if( this.product.name_components != null ) {
			int cpt = 0;
			for( AttributeHandler ah: this.product.name_components ) {
				name += ( cpt > 0)? " " + ah.getValue(): ah.getValue();
				cpt++;
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Instance name <" + name + ">");
		}
		/*
		 * If no name has been set right now, put the position after collection.class
		 * Take the suffix is there is no position
		 */
		if( name == null || name.length() == 0 ) {
			EntrySaada es= (EntrySaada) this.saadaInstance;
			name = SaadaOID.getCollectionName(es.oidsaada) + "-" + SaadaOID.getClassName(es.oidsaada);
			double ra =  es.s_ra;
			double dec = es.s_dec;
			if( ra != SaadaConstant.DOUBLE && dec != SaadaConstant.DOUBLE ) {
				Astrocoo coo =new Astrocoo(Database.getAstroframe(), es.s_ra, es.s_dec);
				coo.setPrecision(5);
				name += coo.toString("s");
			} else {
				name +=  suffix;
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Default instance name  <"+ name + ">");
		}
		return name;
	}

	/**
	 * 
	 */
	protected void mapIndirectionTables() {
		//AttributeHandler[] saada_ah = this.product.metaclass.getClassAttributes();
		AttributeHandler[] saada_ah ;
		if(this.product.metaclass == null  ) {
			saada_ah = this.product.productAttributeHandler.values().toArray(new AttributeHandler[0]);			
		} else {
			saada_ah = this.product.metaclass.getClassAttributes();
		}
		int num_att_read=0;
		nb_bus_att    = saada_ah.length;
		type_bus_att  = new String[nb_bus_att];
		index_pos_col = new int[nb_bus_att];
		index_pos_att = new int[nb_bus_att];
		index_pos_md5 = new boolean[nb_bus_att];
		sqlfields     = new String[this.product.productAttributeHandler.size()];
		num_ext_att   = new int[this.product.extended_attributes_ref.size()];
		for( int i=0 ; i<num_ext_att.length ; i++ ) {
			num_ext_att[i] = -1;
		}
		num_name_att = new int[this.product.name_components.size()];
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
			if( this.product.s_ra_ref != null && nameField.equals(this.product.s_ra_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA col (" + nameField + ") read in column #" + num_att_read);
				num_col_ra = index_pos_col[num_att_read];
			} else if( this.product.s_dec_ref != null && nameField.equals(this.product.s_dec_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC col (" + nameField + ") read in column #" + num_att_read);
				num_col_dec = index_pos_col[num_att_read];
			} else if( this.product.error_maj_ref != null && nameField.equals(this.product.error_maj_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "RA error col (" + nameField + ") read in column #" + num_att_read);
				num_col_ra_err = index_pos_col[num_att_read];
			} else if( this.product.error_min_ref != null && nameField.equals(this.product.error_min_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "DEC error col (" + nameField + ") read in column #" + num_att_read);
				num_col_dec_err = index_pos_col[num_att_read];
			} else if( this.product.error_angle_ref != null && nameField.equals(this.product.error_angle_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Angle error col (" + nameField + ") read in column #" + num_att_read);
				num_col_angle_err = index_pos_col[num_att_read];
			} else if( this.product.em_max_ref != null && nameField.equals(this.product.em_max_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "em_max column col (" + nameField + ") read in column #" + num_att_read);
				num_col_em_max = index_pos_col[num_att_read];
			} else if( this.product.em_min_ref != null && nameField.equals(this.product.em_min_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "em_min column col (" + nameField + ") read in column #" + num_att_read);
				num_col_em_min = index_pos_col[num_att_read];
			} else if( this.product.t_max_ref != null && nameField.equals(this.product.t_max_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "t_max column col (" + nameField + ") read in column #" + num_att_read);
				num_col_t_max = index_pos_col[num_att_read];
			} else if( this.product.t_min_ref != null && nameField.equals(this.product.t_min_ref.getAttNameAtt()) ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "t_min column col (" + nameField + ") read in column #" + num_att_read);
				num_col_t_min = index_pos_col[num_att_read];
			}

			if( this.product.extended_attributes_ref != null ) {
				int extpos=0;
				for( String ext_att_name: this.product.extended_attributes_ref.keySet() ) {
					String mapped_ext_att = this.product.extended_attributes_ref.get(ext_att_name).getAttNameAtt();
					if( mapped_ext_att.equals(nameField)  ) {
						num_ext_att[extpos] = index_pos_col[num_att_read];
						emsg += "(" + mapped_ext_att + " col#" + num_att_read + ") ";
					}
					extpos++;
				}
			}
			if( this.product.name_components != null ) {
				int namepos=0;
					for( AttributeHandler nah: this.product.name_components ) {
					if( nah.getNameattr().equals(nameField)) {
						num_name_att[namepos] = index_pos_col[num_att_read];	
						nmsg += "(" + nah.getNameattr() + " col#" + num_att_read + ") ";
					}
					namepos++;
				}
			}									
			num_att_read++;
		}
		if( this.product.name_components != null ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,nmsg);
		}
		if( this.product.extended_attributes_ref != null ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,emsg);
		}
	}
}
