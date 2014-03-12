/**
 * 
 */
package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.dataloader.mapping.RepositoryMode;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.Coord;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.util.CopyFile;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astrocoo;

/**
 * Contains all the logic of the product storing.
 * Must only be used by {@link ProductBuilder} and subclasses
 * @author michel
 * @version $Id$
 */
class ProductIngestor {
	protected SaadaInstance saadaInstance;
	protected ProductBuilder product;

	ProductIngestor(ProductBuilder product) throws Exception {
		this.product = product;
		this.buildInstance();
	}
	/**
	 * This method builds a SaadaInstance 
	 * @param saada_class
	 * @throws Exception
	 */
	protected void buildInstance() throws Exception {
		/*
		 * Build the Saada instance
		 */
		if( this.product.metaclass != null ) {
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(this.product.metaclass.getName()).newInstance();
			long newoid = SaadaOID.newOid(this.product.metaclass.getName());
			this.saadaInstance.oidsaada = newoid;	
			this.setBusinessFields();
		} else {
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(Category.explain(this.product.mapping.getCategory()) + "UserColl").newInstance();
			this.saadaInstance.oidsaada = SaadaConstant.LONG;	
		}
		this.setObservationFields();
		this.setSpaceFields();
		this.setEnegryFields();		
		this.setTimeFields();
		this.loadAttrExtends();

	}

	/**
	 * Used by FLatFileMapper to load flafiles by burst using a single instance of the this class
	 * @param si
	 * @throws Exception 
	 */
	public void bindInstanceToFile(SaadaInstance si) throws Exception {
		this.saadaInstance = si;
		this.setObservationFields();
		this.setSpaceFields();
		this.setEnegryFields();		
		this.setTimeFields();
		this.loadAttrExtends();
	}


    /**
	 * Populate native (business) attributes if the current instance
	 * Checks for each field if a type conversion must be done
	 * @throws Exception
	 */
	protected void setBusinessFields() throws Exception {
		List<Field> fld = this.saadaInstance.getClassLevelPersisentFields();
		Map<String, AttributeHandler> tableAttributeHandler  = this.product.getProductAttributeHandler();

		String md5Value = "";

		for (Field f: fld ) {
			String keyObj = f.getName();
			if (tableAttributeHandler.containsKey(keyObj)) {
				AttributeHandler attr = tableAttributeHandler.get(keyObj);
				String value = attr.getValue();
				if (value != null) {
					md5Value += value;
					this.saadaInstance.setInField(f, value);
				}
			} else
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No KW in <"+ this.product.file.getName() + "> matches field <"+ this.saadaInstance.getClass().getName() + "." + keyObj + ">");	
		}
		this.saadaInstance.computeContentSignature(md5Value);
	}
	
	/*
	 * 
	 * Set Fields attached to the Observation Axe
	 *
	 */

	/**
	 * Set instance name mapping rule and with the file name if not set
	 * Set product url and date of loading
	 * @throws AbortException 
	 */
	protected void setObservationFields() throws SaadaException {
		this.saadaInstance.obs_id = this.getInstanceName(null);
		this.saadaInstance.setAccess_url(this.saadaInstance.getDownloadURL(false));	
		this.saadaInstance.setAccess_format(this.product.mimeType);
		this.saadaInstance.setDate_load(new java.util.Date().getTime());
		this.saadaInstance.setAccess_estsize(this.product.file.length());
		setField("target_name"    , this.product.target_name_ref);
		setField("instrument_name", this.product.instrument_name_ref);
		setField("facility_name"  , this.product.facility_name_ref);
		setField("obs_collection" , this.product.obs_collection_ref);

	}
	/**
	 * Build the instance name fom the configuration or take the filename
	 * if the configuration can not be used, tge name is made withthe 
	 * filename followerd with the suffix
	 * @param line 
	 * @return
	 */
	private String getInstanceName(String suffix) {
		String name = "";
		if( this.product.name_components != null ) {
			int cpt = 0;
			for( AttributeHandler ah: this.product.name_components ) {
				if( cpt > 0 ) {
					name += " " + ah.getValue();
				} else {
					name += ah.getValue();					
				}
				cpt++;
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Instance name <" + name + ">");
		}
		/*
		 * If no name has been set right now, put the filename.
		 * That is better than an empty name
		 */
		if( name == null || name.length() == 0 ) {
			name = this.product.file.getName();
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Default instance name (file name) <"+ name + ">");

			if( suffix == null ) {
				name =  name.trim().replaceAll("'", "");
			} else {
				name = name.trim().replaceAll("'", "") + "_" + suffix;			
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,"Default instance name (file name) <"+ name + ">");
		}
		return name;
	}
	
	/*
	 * 
	 * Set Fields attached to the Space Axe
	 *
	 */
	
	/**
	 * @throws Exception
	 */
	protected void setSpaceFields() throws Exception {
		this.setAstrofFrame();
		this.setPositionFields(0);
	}
	
	/**
	 * @throws Exception
	 */
	protected void setAstrofFrame()throws Exception {
		/*
		 * Compute first the astroframe if it is not already done
		 * With constant values given in the configuration
		 */
		if( this.product.astroframe == null && this.product.system_attribute != null ) {
			if( this.product.equinox_attribute == null ) {
				this.product.astroframe = Coord.getAstroframe(this.product.system_attribute.getValue(), null);
			}
			else {
				this.product.astroframe = Coord.getAstroframe(this.product.system_attribute.getValue(), this.product.equinox_attribute.getValue());				
			}	
		}	
	}
	/**
	 * Set all fields related to the position at collection level
	 * @param number: no message if number != 0
	 * @throws Exception
	 */
	protected void setPositionFields(int number) throws Exception {
		if( this.product.astroframe != null && this.product.s_ra_ref != null && this.product.s_dec_ref != null ) {
			String ra_val;
			/*
			 * Position values can either be read in keyword or be constants values
			 */
			if( this.product.s_ra_ref.isConstantValue() ) {
				ra_val = this.product.s_ra_ref.getValue();
			} else {
				ra_val = this.product.s_ra_ref.getValue().replaceAll("'", "");
			}
			String dec_val;
			if( this.product.s_dec_ref.isConstantValue() ) {
				dec_val = this.product.s_dec_ref.getValue();
			} else {
				dec_val = this.product.s_dec_ref.getValue().replaceAll("'", "");
			}

			/*
			 * Errors are not set when positions are not set
			 */
			if( ra_val == null || ra_val.equals("") || dec_val == null || dec_val.equals("") ||
					ra_val.equals("Infinity") ||dec_val.equals("Infinity") ||
					ra_val.equals("NaN") ||dec_val.equals("NaN") 
			) {
				if( number == 0 ) Messenger.printMsg(Messenger.WARNING, "Coordinates can not be set: keywords not set");
				return;
			} else {
				Astrocoo acoo;
				if( this.product.s_ra_ref == this.product.s_dec_ref) {
					acoo= new Astrocoo(this.product.astroframe, ra_val ) ;
				}
				/*
				 * or in separate columns
				 */
				else {
					acoo= new Astrocoo(this.product.astroframe, ra_val + " " + dec_val) ;
				}

				double converted_coord[] = Coord.convert(this.product.astroframe, new double[]{acoo.getLon(), acoo.getLat()}, Database.getAstroframe());
				if( number == 0 ) Messenger.printMsg(Messenger.TRACE, "Coordinates converted from <" + this.product.astroframe + "> to <" + Database.getAstroframe() + ">");				
				double ra = converted_coord[0];
				double dec = converted_coord[1];
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, acoo.getLon() + "," + acoo.getLat() + " converted " + ra + "," + dec);
				if(Double.isNaN(ra))
					this.saadaInstance.s_ra = Double.POSITIVE_INFINITY;
				else
					this.saadaInstance.s_ra = ra;
				if(Double.isNaN(dec))
					this.saadaInstance.s_dec = Double.POSITIVE_INFINITY;
				else
					this.saadaInstance.s_dec = dec;
				if( !Double.isNaN(ra) && !Double.isNaN(dec) ){
					this.saadaInstance.calculSky_pixel_csa();
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Native Coordinates <" + ra_val + "," + dec_val 
							+ "> set to <" + this.saadaInstance.s_ra + "," + this.saadaInstance.s_dec + ">" );
				} else {
					if( number == 0 ) Messenger.printMsg(Messenger.WARNING, "Coordinates can not be set");
				}
				this.setPosErrorFields(number);
			} // if position really found
		} //if position mapped
	}

	/**
	 * Set all fields related to the position error at collection level
	 * @param this.saadaInstance Saadainstance to be populated
	 * @param number
	 * @throws Exception
	 */
	protected void setPosErrorFields(int number) throws Exception {
		String error_unit = this.product.mapping.getSpaceAxisMapping().getErrorUnit();
		if( this.product.error_min_ref != null &&  error_unit != null ){
			double angle, maj_err=0, min_err=0, convert = -1;
			/*
			 * Errors are always stored in degrees in Saada
			 * let's make a simple convertion;
			 * Sometime arcsec/min is written arcsec/mins
			 */
			if( error_unit.equals("deg") ) convert = 1;
			else if( error_unit.startsWith("arcmin") ) convert = 1/60.0;
			else if( error_unit.startsWith("arcsec") ) convert = 1/3600.0;
			else if( error_unit.equals("mas") ) convert = 1/(1000*3600.0);
			else if( error_unit.equals("uas") ) convert = 1/(1000*1000*3600.0);
			else {
				if( number == 0 ) Messenger.printMsg(Messenger.WARNING, "Unit <" + error_unit + "> not supported for errors. Error won't be set for this product");
				return ;
			}
			if( this.product.error_angle_ref == null ) {
				angle = 90.0;
			} else {
				angle = Double.parseDouble(this.product.error_angle_ref.getValue());				
			}
			/*
			 * Position errors are the same on both axes by default
			 */
			if( this.product.error_min_ref == null && this.product.error_maj_ref != null ) {
				maj_err = convert*Double.parseDouble(this.product.error_maj_ref.getValue());
				min_err = convert*Double.parseDouble(this.product.error_maj_ref.getValue());
				this.saadaInstance.setError(maj_err, min_err, angle);
			} else if( this.product.error_min_ref != null && this.product.error_maj_ref == null ) {
				maj_err = convert*Double.parseDouble(this.product.error_min_ref.getValue());
				min_err = convert*Double.parseDouble(this.product.error_min_ref.getValue());
				this.saadaInstance.setError(maj_err, min_err, angle);
			} else if( this.product.error_min_ref != null && this.product.error_maj_ref != null ) {
				maj_err = convert*Double.parseDouble(this.product.error_min_ref.getValue());
				min_err = convert*Double.parseDouble(this.product.error_maj_ref.getValue());
				this.saadaInstance.setError(maj_err, min_err, angle);
			}
		} else {
			if( number == 0 ) Messenger.printMsg(Messenger.WARNING, "Position error not mapped or without unit: won't be set for this product");					
		}// if error mapped 	
	}
	/*
	 * Set Time field
	 */
	
	 /**
	 * @throws SaadaException
	 */
	protected void setTimeFields() throws SaadaException {
		setField("t_min"    , this.product.t_min_ref);
		setField("t_max"    , this.product.t_max_ref);
	}

	/*
	 * 
	 * Set Energy fields
	 * 
	 */
	
	/**
	 * @throws FatalException 
	 */
	protected void setEnegryFields() throws FatalException {
		setField("em_min"    , this.product.em_min_ref);
		setField("em_max"    , this.product.em_max_ref);
	}


	
	/**
	 * This default method load the extended attributes in the parametered
	 * product.
	 * 
	 * @param Configuration
	 *            The configuration of the product.
	 * @param SaadaInstance
	 *            The product SaadaInstance (the business object).
	 * @return void.
	 * @throws SaadaException 
	 */
	public void loadAttrExtends() throws SaadaException {
		if( this.product.extended_attributes_ref != null ) {
			for( String ext_att_name: this.product.extended_attributes_ref.keySet() ) {
				this.setField(ext_att_name, this.product.extended_attributes_ref.get(ext_att_name));
			}
		}
	}
	
	
	/*
	 * 
	 * Database storage
	 *
	 */
	
	/**
	 * Stores the saada instance within the DB
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param saada_class
	 * @throws Exception
	 */
	public void loadValue() throws Exception  {
		if( Messenger.debug_mode == true && Table_Saada_Loaded_File.productAlreadyExistsInDB(this.product) ) {
			Messenger.printMsg(Messenger.WARNING, " The object <"
					+ this.saadaInstance.obs_id+ "> in Collection <"
					+ this.saadaInstance.getCollection().getName() + "> with md5 <"
					+ this.saadaInstance.contentsignature + "> exists in the data base <"
					+ Database.getName() + ">");	
		}
		/*
		 * Store the Saada instance
		 */
		this.storeCopyFileInRepository();
		this.saadaInstance.store();
	}

	/**
	 * Stores the saada instance as a row in anASCII file used later to lot a bunch of product in one shot
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	public void loadValue(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		if( Messenger.debug_mode == true && Table_Saada_Loaded_File.productAlreadyExistsInDB(this.product) ) {
			Messenger.printMsg(Messenger.WARNING, " The object <"
					+ this.saadaInstance.obs_id+ "> in Collection <"
					+ this.saadaInstance.getCollection().getName() + "> with md5 <"
					+ this.saadaInstance.contentsignature + "> exists in the data base <"
					+ Database.getName() + ">");	
		}
		this.storeCopyFileInRepository(loadedfilewriter);
		this.saadaInstance.store(colwriter, buswriter);
	}

	/*
	 * 
	 * Actions related to the repository
	 *
     */
	
	/**
	 * Copy the product file into the repository with the name given as parameter
	 * @param rep_name
	 * @throws Exception
	 */
	public void storeCopyFileInRepository() throws Exception {
		String repname = "";
		/*
		 * In these case, the input file is copied or moved to the repository.
		 * product_url_csa is set with the file name
		 */
		if( this.product.mapping.getRepositoryMode() == RepositoryMode.COPY || 
				this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE) {
			repname = Table_Saada_Loaded_File.recordLoadedFile(this.product, null);
			String reportFile = Database.getRepository() 
			+ File.separator + this.product.mapping.getCollection() 
			+ File.separator + Category.explain(this.product.mapping.getCategory()) 
			+ File.separator;
			CopyFile.copy(this.product.file.getAbsolutePath(), reportFile + repname);
			if( this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Remove input file <" + this.product.file.getAbsolutePath() + ">");
				this.product.file.delete();
			}
		}
		/*
		 * In this case, the input file is used itself as repository.
		 * Product_url_csa is set with the absolute path
		 */
		else if( this.product.mapping.getRepositoryMode() == RepositoryMode.KEEP ) {
			repname = this.product.file.getAbsolutePath();
			Table_Saada_Loaded_File.recordLoadedFile(this.product, repname);
		}
		this.saadaInstance.setRepository_location(repname);
	}

	/**
	 * Copy the product file into the repository with the name given as parameter
	 * @param rep_name
	 * @throws Exception
	 */
	public void storeCopyFileInRepository(BufferedWriter loadedfilewriter) throws Exception {
		String repname = "";
		/*
		 * In these case, the input file is copied or moved to the repository.
		 * product_url_csa is set with the file name
		 */
		if( this.product.mapping.getRepositoryMode() == RepositoryMode.COPY || 
				this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE) {
			repname = Table_Saada_Loaded_File.recordLoadedFile(this.product, null, loadedfilewriter);
			String reportFile = Database.getRepository() 
			+ File.separator + this.product.mapping.getCollection() 
			+ File.separator + Category.explain(this.product.mapping.getCategory()) 
			+ File.separator;
			CopyFile.copy(this.product.file.getAbsolutePath(), reportFile + repname);
			if( this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Remove input file <" + this.product.file.getAbsolutePath() + ">");
				this.product.file.delete();
			}
		}
		/*
		 * In this case, the input file is used itself as repository.
		 * Product_url_csa is set with the absolute path
		 */
		else if( this.product.mapping.getRepositoryMode() == RepositoryMode.KEEP ) {
			repname = this.product.file.getAbsolutePath();
			Table_Saada_Loaded_File.recordLoadedFile(this.product, repname, loadedfilewriter);
		}
		this.saadaInstance.setRepository_location(repname);
	}

	/*
	 * 
	 * Utilities
	 * 
	 */
	
	/**
	 * Set the saada instance's field "fieldName" with the value read in ah_ref
	 * @param fieldName
	 * @param ah_ref
	 * @throws FatalException
	 */
	private void setField(String fieldName, AttributeHandler ah_ref) throws FatalException{
		String value = ah_ref.getValue();
		Field f=null;
		try {
			f = saadaInstance.getClass().getField(fieldName);
			this.saadaInstance.setInField(f, value);
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,
					"Extended attribute " + fieldName 
					+ " set with the KW  <" + ah_ref.getNameorg()
					+ "=" + value + ">");
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Extended attribute " + fieldName 
					+ " can not be set with the KW  <" + ah_ref.getNameorg()
					+ "=" + value + ">");
		}
	}


}
