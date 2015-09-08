package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.database.Repository;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.generationclass.SaadaClassReloader;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.CooSysResolver;
import saadadb.products.inference.Coord;
import saadadb.products.inference.STC;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.products.mergeandcast.ClassMerger;
import saadadb.products.setter.ColumnSetter;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.query.parser.PositionParser;
import saadadb.sqltable.SQLTable;
import saadadb.sqltable.Table_Saada_Loaded_File;
import saadadb.unit.Unit;
import saadadb.util.CopyFile;
import saadadb.util.DateUtils;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.enums.RepositoryMode;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;

/**
 * Contains all the logic of the product storing.
 * Basically: populate the SaadaInstance fields with values read from the input file or from the mapping rules
 * Must only be used by {@link ProductBuilder} and subclasses
 * @author michel
 * @version $Id$
 */
public class ProductIngestor {
	public SaadaInstance saadaInstance;
	protected ProductBuilder product;
	/** allows the ColumnSetter to append messages after conversion */
	protected boolean addMEssage = true;	
	/**
	 * Used to disable the messaging in case of multiple use of the object (Entry)
	 */
	protected int numberOfCall=0;
	private final Unit fovUnitConverter = new Unit("deg");
	/**
	 * Inner class storing th action to be done in order to run the ingestor several times 
	 * without re-doing the analysis
	 */
	class TaskMap{
		public boolean convertUnits = false;
	}
	protected TaskMap taskMap = new TaskMap();
	/**
	 * Copy of AHs will be used to build the SQL line corresponding to one row.
	 */
	protected List<AttributeHandler> orderedBusinessAttributes;

	/**
	 * @param product
	 * @throws Exception
	 */
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
		 * Build the Saada instance if there is a metaclass
		 * TODO check whether the metaclass=null case is still used somewhere out of the FlatFile
		 */
		if( this.product.metaClass != null ) {
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(this.product.metaClass.getName()).newInstance();
			this.buildOrderedBusinessAttributeList();
		} else {
			this.orderedBusinessAttributes = new ArrayList<AttributeHandler>();
			this.saadaInstance = (SaadaInstance) SaadaClassReloader.forGeneratedName(
					Category.explain(this.product.mapping.getCategory()) + "UserColl").newInstance();
		}
		this.numberOfCall++;
	}

	/**
	 * This copy of AHs will be used to build the SQL line corresponding to one row.
	 * This data indirection structure is overloaded for table entries
	 */
	protected void buildOrderedBusinessAttributeList(){
		this.orderedBusinessAttributes = new ArrayList<AttributeHandler>();
		HashMap<String, AttributeHandler> hm = this.product.metaClass.getAttributes_handlers();
		for( Entry<String, AttributeHandler> e: hm.entrySet()){
			this.orderedBusinessAttributes.add((AttributeHandler)(e.getValue().clone()));
		}		
	}

	/**
	 * Compute all expressions of the related {@link ProductBuilder}, and set the 
	 * collection level fields of the local instance. These fields values are sued to populate
	 * the collection level of the data.
	 * @throws Exception
	 */
	public void bindInstanceToFile() throws Exception {
		if( this.product.metaClass != null ) {
			this.saadaInstance.oidsaada =  SaadaOID.newOid(this.product.metaClass.getName());
		} else {
			this.saadaInstance.oidsaada = SaadaConstant.LONG;	
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Set the fields of the Saada instance");
		this.product.calculateAllExpressions();
		this.setObservationFields();
		this.setSpaceFields();
		this.setEnergyFields();		
		this.setTimeFields();
		this.setObservableFields();
		this.setPolarizationFields();
		this.setContentSignature();
		this.loadAttrExtends();
	}


	/**
	 * Used by subclasses to  iterate on tabular data
	 * @return
	 */
	public boolean hasMoreElements() {
		return false;
	}

	/**
	 * Compute the MD5 hash code with the loaded keywords
	 * @throws Exception
	 */
	protected void setContentSignature() throws Exception {
		String md5Value = "";
		if( this.product.metaClass != null ) {
			String[] fn = this.product.metaClass.getAttribute_names();
			Map<String, AttributeHandler> tableAttributeHandler  = this.product.getProductAttributeHandler();
			for (String s : fn ) {
				String keyObj = s;
				if (tableAttributeHandler.containsKey(keyObj)) {
					AttributeHandler attr = tableAttributeHandler.get(keyObj);
					String value = attr.getValue();
					if (value != null) {
						md5Value += value;
						// no longer used since business fields are loaded from an ASCII file
						//this.saadaInstance.setInField(f, value);
					}
				} else if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No KW in <"+ this.product.getName() + "> matches field <"+ this.saadaInstance.getClass().getName() + "." + keyObj + ">");	
			}
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
	 * @throws Exception 
	 * @throws AbortException 
	 */
	protected void setObservationFields() throws Exception {
		this.saadaInstance.obs_id  = this.getInstanceName(null);
		this.saadaInstance.setAccess_url(this.saadaInstance.getDownloadURL(false));	
		this.saadaInstance.setAccess_format(this.product.mimeType);
		this.saadaInstance.setDate_load(new java.util.Date().getTime());
		this.saadaInstance.setAccess_estsize(this.product.length());
		setField("target_name"    , this.product.target_nameSetter);
		setField("instrument_name", this.product.instrument_nameSetter);
		setField("facility_name"  , this.product.facility_nameSetter);
		setField("obs_collection" , this.product.obs_collectionSetter);
		setField("obs_publisher_did" , this.product.obs_publisher_didSetter);

	}
	/**
	 * Build the instance name fom the configuration or take the filename
	 * if the configuration can not be used, tge name is made withthe 
	 * filename followerd with the suffix
	 * @param line 
	 * @return
	 * @throws Exception 
	 */
	protected String getInstanceName(String suffix) throws Exception {
		this.product.obs_idSetter.calculateExpression();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Instance name <" + this.product.obs_idSetter.getValue() + ">");
		return this.product.obs_idSetter.getValue() ;
	}

	/************************************************************************************************************************
	 * 
	 * Set Fields attached to the Space Axe
	 * Actions are split in multiple methods in order to prevent the Entry ingestion to re-do all the analysis for each row
	 *
	 */

	/**
	 * Set all fields related to the position at collection level
	 * @throws Exception
	 */
	protected void setSpaceFields() throws Exception {
		if( this.product.s_raSetter.isNotSet() || this.product.s_decSetter.isNotSet()) {
			this.setPositionFieldsInError("Coordinates not set");
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Coordinates are not set");
		} else if( this.product.astroframeSetter.isNotSet() ) {
			this.setPositionFieldsInError("Coordinate conversion failed: no frame");
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Cannot set coordinates since there is no frame");
		} else {
			try {
				/*
				 * Convert astroframe if different from this of the database
				 */
				Astroframe x = new CooSysResolver(this.product.astroframeSetter.getValue()).getCooSys();
				if( x == null ){
					Messenger.printMsg(Messenger.TRACE, "Invalid coosys:" + this.product.astroframeSetter.getValue());
					this.setPositionFieldsInError("Invalid coosys:" + this.product.astroframeSetter.getValue());
				} 
				else if( !CooSysResolver.isSameAsDatabaseFrame(x) ) {
					this.taskMap.convertUnits = true;
					this.setConvertedCoordinatesAndRegion();
					/*
					 * no conversion required, just take the values
					 */
				} else {
					/*
					 * Fails when pos are in sexa.
					 */
					//this.setUnconvertedCoordinatesAndRegion();
					this.setConvertedCoordinatesAndRegion();
				}
				this.setXYZfields();
				this.setFoVFields();
				this.setPosErrorFields();
			} catch( Exception e ) {
				e.printStackTrace();
				this.setPositionFieldsInError("Error while setting the position " + e.getMessage());
			}

		} 
	} //if position mapped

	/**
	 * @param message
	 */
	protected void setPositionFieldsInError(String message) {
		this.product.s_raSetter.setFailed(message);
		this.product.s_decSetter.setFailed(message);
		this.product.s_regionSetter.setFailed(message);
		this.product.s_fovSetter.setFailed(message);
		this.product.s_resolutionSetter.setFailed(message);
		this.saadaInstance.s_ra = Double.NaN;
		this.saadaInstance.s_dec = Double.NaN;	
		this.saadaInstance.pos_x_csa  = Double.NaN;	
		this.saadaInstance.pos_y_csa  = Double.NaN;	
		this.saadaInstance.pos_z_csa  = Double.NaN;	
		this.saadaInstance.healpix_csa  = SaadaConstant.LONG;	
		this.saadaInstance.s_resolution = Double.NaN;	
		this.saadaInstance.setS_fov(Double.NaN);		
		this.saadaInstance.setS_region(SaadaConstant.STRING);		
	}
	/**
	 * Take the position as it is
	 * @throws Exception 
	 */
	protected void setUnconvertedCoordinatesAndRegion() throws Exception {

		if( !this.product.s_regionSetter.isNotSet() ) {
			this.product.s_regionSetter.setValue("Polygon " + Database.getAstroframe() + " " + this.product.s_regionSetter.getValue());
			this.saadaInstance.setS_region(this.product.s_regionSetter.getValue());
		}
		/*
		 * Position parameters can be in decimal or sexadecimal. 
		 */
		try {
			this.saadaInstance.s_ra = Double.parseDouble(this.product.s_raSetter.getValue());
			this.saadaInstance.s_dec = Double.parseDouble(this.product.s_decSetter.getValue());
		} catch(NumberFormatException e){
			PositionParser pp = new PositionParser(this.product.s_raSetter.getValue() + " " + this.product.s_decSetter.getValue());
			this.saadaInstance.s_ra = pp.getRa();
			this.saadaInstance.s_dec = pp.getDec();
		}
	}

	/**
	 * Convert the coords and region before to store it
	 */
	protected void setConvertedCoordinatesAndRegion() {
		try {
			Astrocoo acoo;
			Astroframe af = new CooSysResolver(this.product.astroframeSetter.getValue()).getCooSys();
			if( af != null ) {
				double ra;
				double dec;
				if( this.product.s_raSetter == this.product.s_decSetter) {
					acoo= new Astrocoo(af ,this.product.s_raSetter.getValue() ) ;
				} else {
					acoo= new Astrocoo(af, this.product.s_raSetter.getValue() + " " + this.product.s_decSetter.getValue()) ;
				}
				double converted_coord[] = Coord.convert(af, new double[]{acoo.getLon(), acoo.getLat()}, Database.getAstroframe());
				ra = converted_coord[0];
				dec = converted_coord[1];
				this.product.s_raSetter.setConvertedValue(ra, 	af.toString(), Database.getAstroframe().toString(), addMEssage);
				this.product.s_decSetter.setConvertedValue(dec, af.toString(), Database.getAstroframe().toString(), addMEssage);
				this.saadaInstance.s_ra = ra;
				this.saadaInstance.s_dec = dec;
				/*
				 * convert the region polygon
				 */
				if( !this.product.s_regionSetter.isNotSet() ) {
					STC stc = new STC(this.product.s_regionSetter.getValue());
					if( stc.isValid() ){
						String stcString = stc.getType() + " " + stc.getAstroFrame() + " ";
						List<Double> pts = stc.getCoords();						
						for( int i=0 ; i<(pts.size()/2) ; i++ ) {
							converted_coord = Coord.convert(stc.getAstroFrame(), new double[]{pts.get(2*i), pts.get((2*i) + 1)}, Database.getAstroframe());
							stcString += " " + converted_coord[0] + " " + converted_coord[1] + " " ;
						}		
						this.product.s_regionSetter.completeConversionMsg("Converted from " + stc.getAstroFrame() + " in " +  Database.getAstroframe());			
						this.product.s_regionSetter.setValue(stcString);
						this.saadaInstance.setS_region(this.product.s_regionSetter.getValue());
					} else {
						this.product.s_regionSetter.completeConversionMsg("Conv failed " + stc.message);
					}
				}
			} else {
				this.product.s_raSetter.completeConversionMsg("Conv failed: no astroframe");
				this.product.s_decSetter.completeConversionMsg("Conv failed: no astroframe");
				this.product.s_regionSetter.completeConversionMsg("Conv failed: no astroframe");
				this.saadaInstance.s_ra = Double.NaN;
				this.saadaInstance.s_dec = Double.NaN;									
			}
		} catch (Exception e) {
			e.printStackTrace();
			Messenger.printMsg(Messenger.TRACE, "Error while setting the position " + e.getMessage());
			this.product.s_raSetter.completeConversionMsg("Conv failed " + e.getMessage());
			this.product.s_decSetter.completeConversionMsg("Conv failed " + e.getMessage());
			this.product.s_regionSetter.completeConversionMsg("Conv failed " + e.getMessage());
			this.saadaInstance.s_ra = Double.NaN;
			this.saadaInstance.s_dec = Double.NaN;					
		}
	}

	/**
	 * Compute the cartesian coordinates
	 */
	protected void setXYZfields() {
		if( !Double.isNaN(this.saadaInstance.s_ra) && !Double.isNaN(this.saadaInstance.s_dec) ){
			this.saadaInstance.calculSky_pixel_csa();
			this.saadaInstance.pos_x_csa =Math.cos(Math.toRadians(this.saadaInstance.s_dec)) * Math.cos(Math.toRadians(this.saadaInstance.s_ra));
			this.saadaInstance.pos_y_csa =Math.cos(Math.toRadians(this.saadaInstance.s_dec)) * Math.sin(Math.toRadians(this.saadaInstance.s_ra));
			this.saadaInstance.pos_z_csa =Math.sin(Math.toRadians(this.saadaInstance.s_dec));
		}
	}

	/**
	 * Set the fiedl of view
	 */
	protected void setFoVFields() {
		try {
			if(this.product.s_fovSetter.isNotSet())
				this.saadaInstance.setS_fov(Double.POSITIVE_INFINITY);
			else {
				String unit = this.product.s_fovSetter.getUnit();
				if( unit.length() > 0 ) {
					if( "deg".equals(unit )) {
						this.saadaInstance.setS_fov(Double.parseDouble(this.product.s_fovSetter.getValue()));
					} else {
						this.fovUnitConverter.convertFrom(new Unit(this.product.s_fovSetter.getValue() + unit));
						this.product.s_fovSetter.setConvertedValue(this.fovUnitConverter.value, unit, this.fovUnitConverter.getUnit(), true);
						this.saadaInstance.setS_fov(this.fovUnitConverter.value);
					}
				} else {
					this.product.s_fovSetter.setFailed("No unit given: cannnot make a conversion in deg");
					this.saadaInstance.setS_fov(Double.POSITIVE_INFINITY);					
				}
			}
		} catch( Exception e ) {
			Messenger.printMsg(Messenger.TRACE, "Error while converting the FoV " + e.getMessage());
			this.product.s_fovSetter.completeConversionMsg("FoV conv failed " + e.getMessage());
			this.saadaInstance.setS_fov(Double.POSITIVE_INFINITY);					
		}
	}
	/**
	 * Set all fields related to the position error at collection level
	 * @param this.saadaInstance Saadainstance to be populated
	 * @param number
	 * @throws Exception
	 */
	protected void setPosErrorFields() throws Exception {
		try {
			String error_unit = this.product.s_resolutionSetter.getUnit();
			if( !this.product.s_resolutionSetter.isNotSet() &&  error_unit != null &&  error_unit.length() > 0){
				double maj_err=0, convert = -1;
				/*
				 * Errors are always stored in arcsec in Saada
				 * let's make a simple convertion;
				 * Sometime arcsec/min is written arcsec/mins
				 */
				if( error_unit.equals("deg") ) convert = 3600;
				else if( error_unit.startsWith("arcmin") ) convert = 60.0;
				else if( error_unit.startsWith("arcsec") ) convert = 1;
				else if( error_unit.equals("mas") ) convert = 1./1000.;
				else if( error_unit.equals("uas") ) convert = 1./(1000.*1000.);
				else {
					this.product.s_resolutionSetter.completeConversionMsg("Unit <" + error_unit + "> not supported for errors.");
					Messenger.printMsg(Messenger.TRACE, "Unit <" + error_unit + "> not supported for errors. Error won't be set for this product");
					return ;
				}
				if( convert != 1. ) {
					/*
					 * Position errors are the same on both axes by default
					 */
					maj_err = convert*Double.parseDouble(this.product.s_resolutionSetter.getValue());
					this.product.s_resolutionSetter.completeConversionMsg("orgVal:" + this.product.s_resolutionSetter.getValue() + this.product.s_resolutionSetter.getUnit());
					this.product.s_resolutionSetter.setConvertedValue(maj_err, error_unit, "arcsec", addMEssage);
					this.saadaInstance.s_resolution = maj_err;
				} else {
					this.product.s_resolutionSetter.completeConversionMsg("no convertion");
					this.saadaInstance.s_resolution = Double.parseDouble(this.product.s_resolutionSetter.getValue());
				}
			} else if( this.product.s_resolutionSetter.isNotSet()){
			} else {
				this.product.s_resolutionSetter.completeConversionMsg("Position error without unit");
				if( this.numberOfCall == 0 ){
					Messenger.printMsg(Messenger.TRACE, "Position error not mapped or without unit: won't be set for this product");					
				}
			}
		} catch(Exception e){
			this.product.s_resolutionSetter.setFailed("", e);
			this.saadaInstance.s_resolution = SaadaConstant.DOUBLE;
		}
	}


	/********************************************************************************
	 * Set Time field
	 */

	/**
	 * @throws Exception 
	 */
	protected void setTimeFields() throws Exception {
		ColumnSetter t_min = this.product.t_minSetter;
		ColumnSetter t_max = this.product.t_maxSetter;
		ColumnSetter t_exptime = this.product.t_exptimeSetter;
		ColumnSetter t_resolution = this.product.t_resolutionSetter;

		if( !t_min.isNotSet() ) {
			try {
				t_min.storedValue = DateUtils.getMJD(t_min.getValue());
				t_min.setConvertedValue(DateUtils.getMJD(t_min.getValue()), "String", "mjd", true);
			} catch (Exception e){
				t_min.setFailed("Conv to MJD failed",e);
			}
		}
		if( !t_max.isNotSet() ) {
			try {
				t_max.storedValue = DateUtils.getMJD(t_max.getValue());
				t_max.setConvertedValue(DateUtils.getMJD(t_max.getValue()), "String", "mjd", true);
			} catch (Exception e){
				t_max.setFailed("Conv to MJD failed", e);
			}
		}

		if( t_exptime.isSet()){
			String v = DateUtils.getDuration(t_exptime.getValue());
			if( v != null ) {
				t_exptime.setValue(v);
			} else {
				t_exptime.setFailed("Null value");
			}
		}
		double v;
		if( t_min.isNotSet() && !t_max.isNotSet() && !t_exptime.isNotSet() ) {
			try {
				v =Double.parseDouble(t_max.getValue()) - (Double.parseDouble(t_exptime.getValue())/86400);
				t_min.storedValue = v;
				t_min.completeConversionMsg("Computed from t_max and t_exptime");	
				t_min.setByValue(v, false);
			} catch (Exception e){
				t_min.setFailed("Computation from t_max and t_exptime failed", e);
			}
		} else if( !t_min.isNotSet() && t_max.isNotSet() && !t_exptime.isNotSet() ) {
			try {
				v = Double.parseDouble(t_min.getValue()) + (Double.parseDouble(t_exptime.getValue())/86400);
				t_max.storedValue = v;
				t_max.completeConversionMsg("Computed from t_min and t_exptime");	
				t_max.setByValue(v, false);
			} catch (Exception e){
				t_max.setFailed("Computation from t_min and t_exptime failed", e);
			}
		} else if( !t_min.isNotSet() && !t_max.isNotSet() && t_exptime.isNotSet() ) {
			try {
				v = 3600*24*( Double.parseDouble(t_max.getValue()) - Double.parseDouble(t_min.getValue())  );
				t_exptime.storedValue = v;
				t_exptime.setByValue(v, false);
				t_exptime.completeConversionMsg("Computed from t_min and t_max");	
				t_exptime.setUnit("s");
			} catch (Exception e){
				t_exptime.setFailed("Computation from t_max and t_min failed", e);
			}
		}
		setField("t_min"    , t_min);
		setField("t_max"    , t_max);
		setField("t_exptime", t_exptime);
		setField("t_resolution", t_resolution);
	}

	/*******************************************************************************************
	 * 
	 * Set Energy fields
	 * 
	 */

	/**
	 * @throws FatalException 
	 */
	protected void setEnergyFields() throws SaadaException {

		ColumnSetter qdMin = this.product.em_minSetter;
		ColumnSetter qdMax = this.product.em_maxSetter;
		ColumnSetter qdUnit = this.product.em_unitSetter;
		if( !qdMin.isNotSet() && !qdMax.isNotSet() ) {
			if( !qdUnit.isNotSet() ){

				SpectralCoordinate spectralCoordinate = new SpectralCoordinate();
				spectralCoordinate.setMappedUnit(qdUnit.getValue());
				spectralCoordinate.setOrgMin(Double.parseDouble(qdMin.getValue()));
				spectralCoordinate.setOrgMax(Double.parseDouble(qdMax.getValue()));
				if( !spectralCoordinate.convert() ) {
					this.product.em_minSetter = new ColumnSingleSetter();
					this.product.em_minSetter.completeConversionMsg("vorg="+spectralCoordinate.getOrgMin() + spectralCoordinate.getMappedUnit() + " Conv failed");
					this.product.em_maxSetter = new ColumnSingleSetter();
					this.product.em_maxSetter.completeConversionMsg( "vorg="+spectralCoordinate.getOrgMax() + spectralCoordinate.getMappedUnit()+ " Conv failed");
					this.product.em_res_powerSetter =  new ColumnSingleSetter();
				} else {
					this.product.em_minSetter.setConvertedValue(spectralCoordinate.getConvertedMin(), spectralCoordinate.getMappedUnit(), spectralCoordinate.getFinalUnit(), addMEssage);
					this.product.em_maxSetter.setConvertedValue(spectralCoordinate.getConvertedMax(), spectralCoordinate.getMappedUnit(), spectralCoordinate.getFinalUnit(), addMEssage);
				}
			} else {
				this.product.em_minSetter = new ColumnSingleSetter();
				this.product.em_minSetter.completeConversionMsg("No unit given, can not achieve the conversion ");
				this.product.em_maxSetter = new ColumnSingleSetter();
				this.product.em_maxSetter.completeConversionMsg("No unit given, can not achieve the conversion ");
				this.product.em_res_powerSetter =  new ColumnSingleSetter();				
			}
			if( !this.product.em_binsSetter.isNotSet() && this.product.em_res_powerSetter.isNotSet() ) {
				double v1  =  (this.product.em_minSetter.getNumValue() + this.product.em_maxSetter.getNumValue())/2.;
				double v2  =  (this.product.em_maxSetter.getNumValue() - this.product.em_minSetter.getNumValue())/this.product.em_binsSetter.getNumValue();
				this.product.em_res_powerSetter.setByValue(v1/v2, false);
				this.product.em_res_powerSetter.completeConversionMsg("Computed from em_min, em_max and em_bins");
			}	
		}
		setField("em_min"    , this.product.em_minSetter);
		setField("em_max"    , this.product.em_maxSetter);
		if(this.saadaInstance.em_max < this.saadaInstance.em_min){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Reorder em_minj and em_max");
			double t = this.saadaInstance.em_max;
			this.saadaInstance.em_max = this.saadaInstance.em_min;
			this.saadaInstance.em_min = t;
		}
		setField("em_res_power", this.product.em_res_powerSetter);
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
		if( this.product.extended_attributesSetter != null ) {
			for( String ext_att_name: this.product.extended_attributesSetter.keySet() ) {
				this.setField(ext_att_name, this.product.extended_attributesSetter.get(ext_att_name));
			}
		}
	}

	/*
	 * Observable axis
	 */
	protected void setObservableFields() throws SaadaException {
		setField("o_ucd"    , this.product.o_ucdSetter);
		setField("o_unit"    , this.product.o_unitSetter);
		setField("o_calib_status", this.product.o_calib_statusSetter);		
		this.saadaInstance.setO_ucd(this.product.o_ucdSetter.getValue());
		this.saadaInstance.setO_unit(this.product.o_unitSetter.getValue());
		this.saadaInstance.setO_calib_status(this.product.o_calib_statusSetter.getValue());	
	}
	/*
	 * polarization axis
	 */
	protected void setPolarizationFields() throws SaadaException {
		setField("pol_states"    , this.product.pol_statesSetter);
		this.saadaInstance.pol_states = this.product.pol_statesSetter.getValue();
	}
	/*
	 * 
	 * Database storage
	 *
	 */

	/**
	 * Stores the saada instance within the DB
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * This method is supposed to be used to load one product. It uses however
	 * ASCII file having in mind to get rid of generated classes
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
		String         ecoll_table = Database.getCachemeta().getCollectionTableName(this.product.metaClass.getCollection_name(), this.product.metaClass.getCategory());
		String        busdumpfile  = Repository.getTmpPath() + Database.getSepar()  + this.product.metaClass.getName() +  ".psql";
		BufferedWriter  busLevelWriter = new BufferedWriter(new FileWriter(busdumpfile));
		String        coldumpfile  = Repository.getTmpPath() + Database.getSepar()  + ecoll_table +  ".psql";
		BufferedWriter  colLevelWriter = new BufferedWriter(new FileWriter(coldumpfile));
		String        loadedfile   = Repository.getTmpPath() + Database.getSepar()  + "saada_loaded_file.psql";
		BufferedWriter loadedFileWriter = new BufferedWriter(new FileWriter(loadedfile));
		this.loadValue(colLevelWriter,busLevelWriter, loadedFileWriter);
		busLevelWriter.close();
		colLevelWriter.close();
		loadedFileWriter.close();
		/*
		 * Store the dump table
		 */
		SQLTable.addQueryToTransaction("LOADTSVTABLE " + ecoll_table + " -1 " + coldumpfile);
		SQLTable.addQueryToTransaction("LOADTSVTABLE " +  this.product.metaClass.getName() + " -1 " + busdumpfile);
		SQLTable.addQueryToTransaction("LOADTSVTABLE saada_loaded_file -1 " + loadedfile);
	}

	/**
	 * Stores the saada instance as a row in anASCII file used later to lot a bunch of product in one shot
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	/**
	 * @param colwriter
	 * @param buswriter
	 * @param loadedfilewriter
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
		//this.saadaInstance.store(colwriter, buswriter);
		this.saadaInstance.storeCollection(colwriter);
		/*
		 * The SQL line for business attributes is build without passing by the instance? 
		 * That avoids a double cast on the values
		 */
		for(AttributeHandler ah: this.orderedBusinessAttributes) {
			AttributeHandler pah = this.product.productAttributeHandler.get(ah.getNameattr());
			if( pah != null ) {
				ah.setValue(pah.getValue());
			} else {
				ah.setValue("NULL");
			}
		}
		String file_bus_sql = this.saadaInstance.oidsaada + "\t" + this.saadaInstance.obs_id + "\t" + this.saadaInstance.contentsignature;
		for(AttributeHandler ah: this.orderedBusinessAttributes) {
			file_bus_sql += "\t"  +  ClassMerger.getCastedSQLValue(ah, ah.getType().toString());
			//
			//			if( val.equals("Infinity") || val.equals("NaN") || val.equals("") 
			//					|| val.equals(SaadaConstant.NOTSET)|| val.equals(SaadaConstant.STRING) ||
			//					val.equalsIgnoreCase("NULL")|| val.equals("2147483647") || val.equals("9223372036854775807")) {
			//				file_bus_sql +=Database.getWrapper().getAsciiNull();
			//			} else {
			//				String type = ah.getType().toString();
			//				if( type.equals("char") || type.endsWith("String") ) {
			//					file_bus_sql += val.replaceAll("'", "");
			//				} else if( type.equals("boolean")  ) {
			//					file_bus_sql += Database.getWrapper().getBooleanAsString(Boolean.parseBoolean(val));
			//				} else {
			//					file_bus_sql +=  val;
			//				}
			//			}
		}
		buswriter.write(file_bus_sql + "\n");
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
			/*
			 * In case of FooProduct or SQL table
			 */
			if( this.product.dataFile != null) {
				CopyFile.copy(this.product.dataFile.getAbsolutePath(), reportFile + repname);
				if( this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE ) {
					Messenger.printMsg(Messenger.TRACE, "File moved to " + reportFile + repname);
					this.product.dataFile.delete();
				} else {
					Messenger.printMsg(Messenger.TRACE, "File copied to " + reportFile + repname);
				}
			}
		}
		/*
		 * In this case, the input file is used itself as repository.
		 * Product_url_csa is set with the absolute path
		 */
		else if( this.product.mapping.getRepositoryMode() == RepositoryMode.KEEP ) {
			repname = this.product.dataFile.getAbsolutePath();
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
			CopyFile.copy(this.product.dataFile.getAbsolutePath(), reportFile + repname);
			if( this.product.mapping.getRepositoryMode() == RepositoryMode.MOVE ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Remove input file <" + this.product.dataFile.getAbsolutePath() + ">");
				this.product.dataFile.delete();
			}
		}
		/*
		 * In this case, the input file is used itself as repository.
		 * Product_url_csa is set with the absolute path
		 */
		else if( this.product.mapping.getRepositoryMode() == RepositoryMode.KEEP ) {
			repname = this.product.dataFile.getAbsolutePath();
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
	 * @param columnSetter
	 * @throws FatalException
	 */
	protected void setField(String fieldName, ColumnSetter columnSetter) throws FatalException{
		if(columnSetter.isNotSet() ){
			return;
		}
		String value = "";
		try {
			value = columnSetter.getValue();
			Field f=null;
			f = saadaInstance.getClass().getField(fieldName);
			this.saadaInstance.setInField(f, value);
			if( Messenger.debug_mode ) {
				if( columnSetter.byKeyword()) {
					Messenger.printMsg(Messenger.DEBUG,
							"Attribute " + fieldName 
							+ " set with the KW  <" + columnSetter.getAttNameOrg()
							+ "=" + value + ">");
				} else {
					Messenger.printMsg(Messenger.DEBUG,
							"Attribute " + fieldName 
							+ " set with the value  <" + value + ">");
				}
			}
		} catch (NoSuchFieldException e) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, this.saadaInstance.getClassName() + " " + e.getMessage());
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Attribute " + fieldName 
					+ " can not be set with the KW  <" + columnSetter.getAttNameOrg()
					+ "=" + value + ">");
		}
	}

	public void showCollectionValues() throws Exception {

	}

}
