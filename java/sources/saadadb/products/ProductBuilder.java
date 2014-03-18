package saadadb.products;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import nom.tam.fits.FitsException;
import saadadb.collection.Category;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.AxisMapping;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.dataloader.mapping.PriorityMode;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.prdconfiguration.CoordSystem;
import saadadb.products.inference.Coord;
import saadadb.products.inference.EnergyKWDetector;
import saadadb.products.inference.Image2DCoordinate;
import saadadb.products.inference.ObservationKWDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.products.inference.TimeKWDetector;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.ICRS;

/**
 * This class is the central class managing all specificities connected in the
 * file type (at the moment "FITS" or "VO"), and the product type (at the moment
 * Sprectra, Image2D, Misc, Table) The entries management extends indirectly of
 * this class, but with specificites appropriate for tables.
 * 
 */
/**
 * @author michel
 * @version $Id$
 *
 */
/**
 * @author michel
 * @version $Id$
 */
public class ProductBuilder {
	private static final long serialVersionUID = 1L;
	protected static String separ = System.getProperty("file.separator");
	/*
	 * This file instance is just used to stored the file name
	 * It is used to encapsulate java.io.File methods
	 * Data are handled by the Product file implementation "productFile"
	 */
	protected File file;
	/**
	 * Data product to load
	 */
	protected ProductFile productFile;
	protected String mimeType;
	/**
	 * loader mapping
	 */
	protected ProductMapping mapping;

	/**The list which maps attribute names formated in the standard of Saada (keys) to their objects modelling attribute informations (values)**/
	protected Map<String, AttributeHandler> productAttributeHandler;
	protected String fmtsignature;
	/*
	 * references of attributes handlers used to map the collection level
	 */
	/*
	 * Observation Axis
	 */
	protected List<AttributeHandler> name_components;
	protected AttributeHandler obs_collection_ref=null;
	protected AttributeHandler target_name_ref=null;
	protected AttributeHandler facility_name_ref=null;
	protected AttributeHandler instrument_name_ref=null;
	protected PriorityMode observationMappingPriority = PriorityMode.LAST;
	protected ObservationKWDetector observationKWDetector = null;
	/*
	 * Space Axis
	 */
	protected AttributeHandler error_maj_ref=null;
	protected AttributeHandler error_min_ref=null;
	protected AttributeHandler error_angle_ref=null;
	protected AttributeHandler s_ra_ref=null;
	protected AttributeHandler s_dec_ref=null;
	protected PriorityMode spaceMappingPriority = PriorityMode.LAST;
	protected SpaceKWDetector spaceKWDetector = null;

	/*
	 * Energy Axis
	 */
	protected AttributeHandler em_min_ref=null;
	protected AttributeHandler em_max_ref=null;
	private SpectralCoordinate spectralCoordinate;
	protected AttributeHandler x_unit_org_ref=null;
	protected PriorityMode energyMappingPriority = PriorityMode.LAST;
	protected EnergyKWDetector energyKWDetector = null;
	/*
	 * Time Axis
	 */
	protected AttributeHandler t_min_ref=null;
	protected AttributeHandler t_max_ref=null;
	protected PriorityMode timeMappingPriority = PriorityMode.LAST;
	protected TimeKWDetector timeKWDetector = null;

	/* map: name of the collection attribute => attribute handler of the current product*/
	protected Map<String,AttributeHandler> extended_attributes_ref;
	protected List<AttributeHandler> ignored_attributes_ref;

	protected AttributeHandler system_attribute;
	protected AttributeHandler equinox_attribute;
	protected Astroframe astroframe;

	/** The file type ("FITS" or "VO") * */
	protected String typeFile;
	protected MetaClass metaclass;
	protected ProductIngestor productIngestor;	
	/** sed by subclasses */
	protected Image2DCoordinate wcs;

	/**
	 * Constructor. This is a product constructor for the new loader.
	 * @param file
	 * @param conf
	 * @throws FatalException 
	 */
	public ProductBuilder(File file, ProductMapping conf) throws FatalException{		
		this.file = file;
		this.mapping = conf;
		/*
		 * priority ref copied for convenience
		 */
		this.observationMappingPriority = conf.getObservationAxisMapping().getPriority();
		this.spaceMappingPriority = conf.getSpaceAxisMapping().getPriority();
		this.energyMappingPriority = conf.getEnergyAxisMapping().getPriority();
		this.timeMappingPriority = conf.getTimeAxisMapping().getPriority();
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	public void close() throws QueryException {
		if( productFile != null) {
			productFile.closeStream();
		}
	}

	/**
	 * @throws SaadaException
	 */
	protected void setObservationKWDetector() throws SaadaException {
		if( this.observationKWDetector == null) {
			// unit tst purpose
			if(this.productFile == null ) {
				this.observationKWDetector = new ObservationKWDetector(this.productAttributeHandler);
			} else {
				this.observationKWDetector = this.productFile.getObservationKWDetector(false);
			}
		}
	}
	/**
	 * @throws SaadaException
	 */
	protected void setSpaceKWDetector() throws SaadaException {
		if( this.spaceKWDetector == null) {
			// unit tst purpose
			if(this.productFile == null ) {
				this.spaceKWDetector = new SpaceKWDetector(this.productAttributeHandler);
			} else {
				this.spaceKWDetector = this.productFile.getSpaceKWDetector(false);
			}
		}
	}
	/**
	 * @throws SaadaException
	 */
	protected void setEnergyKWDetector() throws SaadaException {
		if( this.energyKWDetector == null) {
			// unit tst purpose
			if(this.productFile == null ) {
				this.energyKWDetector = new EnergyKWDetector(this.productAttributeHandler);
			} else {
				this.energyKWDetector = this.productFile.getEnergyKWDetector(false);
			}
		}
	}
	/**
	 * @throws SaadaException
	 */
	protected void setTimeKWDetector() throws SaadaException {
		if( this.timeKWDetector == null) {
			// unit tst purpose
			if(this.productFile == null ) {
				this.timeKWDetector = new TimeKWDetector(this.productAttributeHandler);
			} else {
				this.timeKWDetector = this.productFile.getTimeKWDetector(false);
			}
		}
	}
	/**
	 * Mark ah_ref as undefined (not mapped at all)
	 * Avoid to deal with null ah references
	 * @param ah_ref
	 */
	private AttributeHandler setAsUndefined(){
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr(ColumnMapping.UNDEFINED);
		ah.setNameorg(ColumnMapping.UNDEFINED);
		return ah;
	}

	/**
	 * Returns the list which maps attribute names formated in the standard of
	 * Saada (keys) to their objects modelling attribute informations (values).
	 * Generally the object modelling attribute informations is a
	 * AttributeHandler. This method contrary in the method getKW() does not
	 * model the list in memory. So that this one does not return null, it is
	 * beforehand necessary to have already created the list with the other
	 * method (getKW()).
	 * 
	 * @return Hashtable The list which maps attribute names to their
	 *         informations.
	 */
	public Map<String, AttributeHandler> getProductAttributeHandler() {
		return this.productAttributeHandler;
	}

	/**
	 * @return
	 */
	public ProductMapping getMapping() {
		return this.mapping;
	}
	/**
	 * Returns the algorithmics value for the product characteristics
	 * (attributes without values) with md5. Cross_reference of the homonymous
	 * method defined in the current product file.
	 * 
	 * @return String this algorithmics value with md5.
	 */
	public String getFmtsignature() {
		return fmtsignature;
	}

	/**
	 * @return
	 */
	public ProductFile getProducFile() {
		return productFile;
	}

	/**
	 * @return
	 */
	public long getActualOidsaada() {
		return (this.productIngestor == null )? SaadaConstant.LONG: this.productIngestor.saadaInstance.oidsaada;
	}
	public String getContentSignature() {
		return (this.productIngestor == null )? SaadaConstant.STRING: this.productIngestor.saadaInstance.contentsignature;
	}

	/**
	 * In case of the product can have table: Returns an enumeration of the
	 * entries in this table. Initializes the enumeration in the product file.
	 * 
	 * @throws SaadaException 
	 */
	@SuppressWarnings("rawtypes")
	public Enumeration elements() throws SaadaException {
		// Initializes the enumeration of table rows (essential in stream mode).
		productFile.initEnumeration();
		return productFile;
	}

	/**
	 * In case of the product can have table: Returns the row number in the
	 * table. If there is no table for this product format, this method will
	 * return 0. Cross_reference of the homonymous method defined in the current
	 * product file.
	 * 
	 * @return int The row number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public int getNRows() throws SaadaException {
		return productFile.getNRows();
	}



	/**
	 * This method converts the equinox String into the corresponding double
	 * value, and returns this double value. By default, the equinox value is
	 * "2000.0"
	 * 
	 * @param String
	 *            The equinox value.
	 * @return double The value corresponding into this value.
	 */
	public static double getEquinox(String equ) {
		double value = 2000.0;
		if (equ.indexOf("1950") >= 0) {
			value = 1950.0;
		}
		return value;
	}


	/*************************************************
	 * Initialization of the product
	 *************************************************/

	/**
	 * invoked by the {@link SchemaMapper} to connect the product with both the datafile and the mapping rules
	 * @param mapping
	 * @throws SaadaException
	 */
	public void initProductFile() throws SaadaException{

		this.readProductFile();
		try {
			this.mapCollectionAttributes();
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, e);
		}	
	}

	/**
	 * @param mapping
	 * @throws SaadaException
	 */
	public void readProductFile() throws SaadaException{
		String filename = this.file.getName();
		boolean try_votable = false;
		try {
			this.productFile = new FitsProduct(this);			
			this.mimeType = "application/fits";
		} catch(IgnoreException ei) {
			if( ei.getMessage().equals(SaadaException.MISSING_RESOURCE) ) {
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + filename + "> can't be read: " + ei.getContext());	
			} else {
				Messenger.printMsg(Messenger.TRACE, "Not a FITS file (try VOTable) " + ei.getMessage());
				//Messenger.printStackTrace(ei);
				try_votable = true;
			}
		} catch(Exception ef) {
			Messenger.printMsg(Messenger.TRACE, "Not a FITS file (try VOTable) " + ef.getMessage());
			try_votable = true;
		}

		if( try_votable ) {			
			try {
				this.productFile = new VOTableProduct(this);				
				this.mimeType = "application/x-votable+xml";
			} catch(SaadaException ev) {
				Messenger.printStackTrace(ev);
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + filename + "> can't be read: " + ev.getContext());			
			} catch(Exception ev) {
				Messenger.printStackTrace(ev);
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + filename + "> can't be read: " + ev.toString());			
			}
		}
		this.setFmtsignature();
	}


	/**
	 * Compute the MD key of the format read in the tabel of attribte handler.
	 * The key is independent from the attribute order
	 */
	@SuppressWarnings("rawtypes")
	public void setFmtsignature() {
		/*
		 * Build an ordered map with name/type of each attribute
		 */
		TreeMap<String, String> md5tree = new TreeMap<String, String>();
		String md5Key = "", md5Type = "";
		for( AttributeHandler ah: this.productAttributeHandler.values()) {
			md5tree.put(ah.getNameorg(), ah.getType());
		}
		/*
		 * Compute the MD5 signature
		 */
		Iterator it = md5tree.keySet().iterator();
		while( it.hasNext() ) {
			String key = (String)it.next();
			md5Key += key;
			md5Type += md5tree.get(key);
		}
		this.fmtsignature =  MD5Key.calculMD5Key(md5Key+md5Type);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "fmtsignature " + this + " " + this.getName() + " " +  this.fmtsignature );
	}


	/**
	 * Can be overloaded to use another ingestor
	 * @throws Exception
	 */
	protected void setProductIngestor() throws Exception{
		if( this.productIngestor == null ){
			this.productIngestor = new ProductIngestor(this);
		}		
	}

	/********************************************
	 * Code loading data within the DB
	 *********************************************/
	/**
	 * Stores the saada instance within the DB
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param saada_class
	 * @throws Exception
	 */
	public void loadValue() throws Exception  {
		this.setProductIngestor();
		this.productIngestor.loadValue();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.file.getName() + "> complete");
	}

	/**
	 * Stores the saada instance as a row in anASCII file used later to lot a bunch of product in one shot
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	public void loadValue(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		this.setProductIngestor();
		this.productIngestor.loadValue(colwriter, buswriter, loadedfilewriter);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.file.getName() + "> complete");
	}
	/**
	 * @throws Exception 
	 * 
	 */
	public void storeCopyFileInRepository() throws Exception {
		this.setProductIngestor();
		this.productIngestor.storeCopyFileInRepository();		
	}
	/************************************************************************************************
	 * Code doing the mapping between the collection KW, the mapping rule and the KW of the data file
	 *************************************************************************************************/

	/************************************************************************
	 * Mapping of the axe field references
	 */
	/**
	 * @param columnMapping
	 * @param label
	 * @return
	 */
	protected AttributeHandler getMappedAttributeHander(ColumnMapping columnMapping) {
		AttributeHandler cmah = columnMapping.getHandler();
		if( columnMapping.byValue() ){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, columnMapping.label + ": take constant value <" + columnMapping.getValue()+ ">");
			return cmah;
		} else if( columnMapping.byAttribute() ){
			for( AttributeHandler ah: this.productAttributeHandler.values()) {
				String keyorg  = ah.getNameorg();
				String keyattr = ah.getNameattr();
				if( (keyorg.equals(cmah.getNameorg()) || keyattr.equals(cmah.getNameattr())) ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  columnMapping.label +  ": take keyword <" + ah.getNameorg() + ">");
					return ah;
				}
			}
		} else {
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr(ColumnMapping.UNDEFINED);
			ah.setNameorg(ColumnMapping.UNDEFINED);
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  columnMapping.label +  ": undefined");
			return ah;
		}

		return null;
	}
	/**
	 * @throws Exception 
	 * 
	 */
	protected void mapCollectionAttributes() throws Exception {
		System.out.println("@@@@@@@@@@@@@@ MAP COLLLLLLLLLLLLLLLLLLLL" + this);
		(new Exception()).printStackTrace();
		for(AttributeHandler ah: this.productAttributeHandler.values()) System.out.print(ah.getNameattr() + " " );
		System.out.println("\n");
		this.mapObservationAxe();
		this.mapSpaceAxe();
		this.mapEnergyAxe();
		this.mapTimeAxe();
		this.mapIgnoredAndExtendedAttributes();
	}

	/**
	 * @throws Exception
	 */
	protected void mapObservationAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Observation Axe");
		this.mapInstanceName();
		AxisMapping mapping = this.mapping.getObservationAxisMapping();
		setObservationKWDetector();

		switch(this.observationMappingPriority){
		case ONLY:			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Observation mapping priority: ONLY: only mapped keywords will be used");
			this.obs_collection_ref = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			this.target_name_ref = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			this.facility_name_ref = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			this.instrument_name_ref = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			break;

		case FIRST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Observation mapping priority: FIRST: Mapped keywords will first be searched and then KWs will be infered");
			this.obs_collection_ref = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			if( this.obs_collection_ref == null) {
				this.obs_collection_ref = this.observationKWDetector.getCollNameAttribute();
			} 
			this.target_name_ref = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			if( this.target_name_ref == null) {
				this.target_name_ref = this.observationKWDetector.getTargetAttribute();
			}
			this.facility_name_ref = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			if( this.facility_name_ref == null) {
				this.facility_name_ref = this.observationKWDetector.getFacilityAttribute();
			}
			this.instrument_name_ref = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			if( this.instrument_name_ref == null) {
				this.instrument_name_ref = this.observationKWDetector.getInstrumentAttribute();
			}
			break;
		case LAST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Observation mapping priority: LAST: KWs will first be infered and then mapped keywords will be used");
			this.obs_collection_ref = this.observationKWDetector.getCollNameAttribute();
			if( this.obs_collection_ref == null) {
				this.obs_collection_ref = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			}
			this.target_name_ref = this.observationKWDetector.getTargetAttribute();
			if( this.target_name_ref == null) {
				this.target_name_ref = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			}
			this.facility_name_ref = this.observationKWDetector.getFacilityAttribute();
			if( this.facility_name_ref == null) {
				this.facility_name_ref = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			}
			this.instrument_name_ref = this.observationKWDetector.getInstrumentAttribute();
			if( this.instrument_name_ref == null) {
				this.instrument_name_ref = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			}
			break;
		}	
		/*
		 * Take the Saada collection as default obs_collection
		 */
		if( this.obs_collection_ref.getNameattr().equals(ColumnMapping.UNDEFINED) ){
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr(ColumnMapping.NUMERIC);
			ah.setNameorg(ColumnMapping.NUMERIC);
			ah.setValue(this.mapping.getCollection() + "_" + Category.explain(this.mapping.getCategory()));
			this.obs_collection_ref = ah;
		}
		if( this.target_name_ref == null) {
			this.target_name_ref = this.setAsUndefined();
		}
		if( this.facility_name_ref == null) {
			this.facility_name_ref = this.setAsUndefined();
		}
		if( this.instrument_name_ref == null) {
			this.instrument_name_ref = this.setAsUndefined();
		}

		traceReportOnAttRef("obs_collection", this.obs_collection_ref);
		traceReportOnAttRef("target_name", this.target_name_ref);
		traceReportOnAttRef("facility_name", this.facility_name_ref);
		traceReportOnAttRef("instrument_name", this.instrument_name_ref);
	}

	/**
	 * @throws Exception
	 */
	protected void mapEnergyAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Energy Axe");
		setEnergyKWDetector();
		this.spectralCoordinate = this.energyKWDetector.getSpectralCoordinate();
		if(!spectralCoordinate.isConfigurationValid(1
				, 1
				,SpectralCoordinate.getDispersionCode(Database.getSpect_type())
				, Database.getSpect_unit())) {
			IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, "Spectral Configuration not valid");
		}
		switch(this.energyMappingPriority){
		case ONLY:			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Energy mapping priority: ONLY: only mapped keywords will be used");
			if( !this.mapCollectionSpectralCoordinateFromMapping() ) {
				this.em_min_ref = new AttributeHandler();
				this.em_min_ref.setNameattr(ColumnMapping.UNDEFINED);
				this.em_max_ref = new AttributeHandler();
				this.em_max_ref.setNameattr(ColumnMapping.UNDEFINED);
			}
			break;

		case FIRST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Energy mapping priority: FIRST: Mapped keywords will first be searched and then KWs will be infered");
			if( !this.mapCollectionSpectralCoordinateFromMapping() ) {
				if( this.x_unit_org_ref !=  null ) {
					spectralCoordinate.setOrgUnit(this.x_unit_org_ref.getValue());				
				}
				if( this.spectralCoordinate.convert() ) {
					this.em_min_ref = new AttributeHandler();
					this.em_min_ref.setNameattr(ColumnMapping.NUMERIC);
					this.em_min_ref.setValue(Double.toString(spectralCoordinate.getConvertedMin()));
					this.em_max_ref = new AttributeHandler();
					this.em_max_ref.setNameattr(ColumnMapping.NUMERIC);
					this.em_max_ref.setValue(Double.toString(spectralCoordinate.getConvertedMax()));
				} else {
					this.em_min_ref = new AttributeHandler();
					this.em_min_ref.setNameattr(ColumnMapping.UNDEFINED);
					this.em_max_ref = new AttributeHandler();
					this.em_max_ref.setNameattr(ColumnMapping.UNDEFINED);
				}
			}
			break;

		case LAST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Energy mapping priority LAST: KWs will first be inefered and then mapped keywords will be searched");
			if( this.x_unit_org_ref !=  null ) {
				spectralCoordinate.setOrgUnit(this.x_unit_org_ref.getValue());				
			}
			if( !this.spectralCoordinate.convert() ) {
				if( !this.mapCollectionSpectralCoordinateFromMapping() ) {
					this.em_min_ref = new AttributeHandler();
					this.em_min_ref.setNameattr(ColumnMapping.UNDEFINED);
					this.em_max_ref = new AttributeHandler();
					this.em_max_ref.setNameattr(ColumnMapping.UNDEFINED);
				}
			} else {
				this.em_min_ref = new AttributeHandler();
				this.em_min_ref.setNameattr(ColumnMapping.NUMERIC);
				this.em_min_ref.setValue(Double.toString(spectralCoordinate.getConvertedMin()));
				this.em_max_ref = new AttributeHandler();
				this.em_max_ref.setNameattr(ColumnMapping.NUMERIC);
				this.em_max_ref.setValue(Double.toString(spectralCoordinate.getConvertedMax()));				
			}
			break;
		}
		traceReportOnAttRef("x_unit_org", this.x_unit_org_ref);
		traceReportOnAttRef("em_min", this.em_min_ref);
		traceReportOnAttRef("em_max", this.em_max_ref);
	}
	/**
	 * @throws Exception
	 */
	protected void mapTimeAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Time Axe");
		AxisMapping mapping = this.mapping.getTimeAxisMapping();
		setTimeKWDetector();

		switch(this.timeMappingPriority){
		case ONLY:			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Time mapping priority: ONLY: only mapped keywords will be used");
			this.t_max_ref = getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			this.t_min_ref = getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			break;

		case FIRST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Time mapping priority: FIRST: Mapped keywords will first be searched and then KWs will be infered");
			this.t_max_ref = getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			if( this.t_max_ref == null) {
				this.t_max_ref = this.timeKWDetector.getTmaxName();
			}
			this.t_min_ref = getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			if( this.t_min_ref == null) {
				this.t_min_ref = this.timeKWDetector.getTminName();
			}
			break;

		case LAST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Time mapping priority: LAST: KWs will first be infered and then mzpped keywords will be used");
			this.t_max_ref = this.timeKWDetector.getTmaxName();
			if( this.t_max_ref == null) {
				this.t_max_ref = getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			}
			this.t_min_ref = this.timeKWDetector.getTminName();
			if( this.t_min_ref == null) {
				this.t_min_ref = getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			}

			break;
		}
		traceReportOnAttRef("t_min", this.t_min_ref);
		traceReportOnAttRef("t_max", this.t_max_ref);
	}

	/**
	 * @throws Exception
	 */
	protected void mapSpaceAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Space Axe");
		/*
		 * Coo sys done in 2nd: can use position mapping to detect the coord system
		 */
		this.mapCollectionCooSysAttributes();
		/*
		 * Don't map position if there is o astroframe
		 */
		if( this.astroframe != null || this.system_attribute != null) {
			this.mapCollectionPosAttributes();
			this.mapCollectionPoserrorAttributes();
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	public void mapIgnoredAndExtendedAttributes () throws SaadaException {
		//LinkedHashMap<String, String> mapped_extend_att = this.mapping.getExtenedAttMapping().getClass() getAttrExt();
		Set<String> extendedAtt = this.mapping.getExtenedAttMapping().getColmunSet();
		this.extended_attributes_ref = new LinkedHashMap<String, AttributeHandler>();
		this.ignored_attributes_ref  = new ArrayList<AttributeHandler>();
		/*
		 * Ignored attribute are discarded on the fly when data files are readout.
		 * (see FITSProduct and VOProduct 
		 * Only extended attr are mapped here
		 */
		for( String att_ext_name: extendedAtt ) {
			ColumnMapping columnMapping = this.mapping.getExtenedAttMapping().getColumnMapping(att_ext_name);
			/*
			 * Attribute extends can be populated with constant values within "'"
			 * or from read values
			 */
			if( columnMapping.byValue() ) {
				this.extended_attributes_ref.put(att_ext_name , columnMapping.getHandler());
			}
			/*
			 * Flatfile have not tableAttributeHandler
			 */
			else if (this.productAttributeHandler != null ) {
				String cm = (columnMapping.getHandler() != null)?columnMapping.getHandler().getNameattr(): null;
				for( AttributeHandler ah : this.productAttributeHandler.values() ) {
					if( ah.getNameorg().equals(cm)) {
						this.extended_attributes_ref.put(att_ext_name, ah);
						break;
					}
				}
			}
		}
	}


	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionCooSysAttributes() throws SaadaException {
		String msg = "";
		switch(this.spaceMappingPriority) {
		case ONLY :
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Coord system mapping priority: ONLY: only mapped keywords will be used");
			this.mapCollectionCooSysAttributesFromMapping();
			break;
		case FIRST :
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Coord system mapping priority: FIRST: Mapped keywords will be first searched and then coosys KWs will be infered");
			if( !this.mapCollectionCooSysAttributesFromMapping() ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Try to find out the cood system");
				this.mapCollectionCooSysAttributesAuto();
				msg = " (auto.detection) ";
			}			
			break;
		case LAST :
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Coord system mapping priority: LAST: Coo sys KWs will be infered and then mapped keywords will be searched");
			if( !this.mapCollectionCooSysAttributesAuto()) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Look for coord system defined into the mapping");
				this.mapCollectionPosAttributesFromMapping();
			}			
			else {
				msg = " (auto.detection) ";
			}
			break;
		default:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Coord system mapping priority: ONLY: Coo sys  KWs will be infered");
			this.mapCollectionCooSysAttributesAuto();
			msg = " (auto.detection) ";
		}
		/*
		 * If the mapping given in ONLY mode is wrong, we don't use any default coord sys.
		 */
		if( this.astroframe == null && this.system_attribute == null ) {
			if( this.mapping.getSpaceAxisMapping().mappingOnly() ) {
				this.s_ra_ref = null;
				this.s_dec_ref = null;
				Messenger.printMsg(Messenger.WARNING, "No coord system " + msg + " found: position won't be set");
			}
			else {
				this.astroframe = new ICRS();
				Messenger.printMsg(Messenger.TRACE, "Product coordinate system taken (default value) <" + this.astroframe +  "> ");
			}
		}
		/*
		 * The following test suit is just made to display proper messages
		 */
		else if( this.astroframe != null) {
			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken  " + msg + "<" + this.astroframe + "> ");
		}
		else if( this.equinox_attribute != null ) {
			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken " + msg + "<" 
					+ ((this.system_attribute.isConstantValue())? ("System="+this.system_attribute.getValue()): ("System KW="+this.system_attribute.getNameorg()))
					+ ((this.equinox_attribute.isConstantValue())? (" Equinox="+this.equinox_attribute.getValue()): (" Equinox kw="+this.equinox_attribute.getNameorg()))
					+ "> ");
		} 
		else{
			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken  " + msg + "<" 
					+ ((this.system_attribute.isConstantValue())? ("System="+this.system_attribute.getValue()): ("System KW="+this.system_attribute.getNameorg()))
					+ "> ");
		} 		
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionCooSysAttributesAuto() throws SaadaException {
		this.setSpaceKWDetector();
		if( (this.astroframe = this.spaceKWDetector.getFrame()) != null ) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Attemps to apply the Coord system mappping rules to the current product
	 * @return
	 * @throws FatalException 
	 */
	private boolean mapCollectionCooSysAttributesFromMapping() throws SaadaException {
		/*
		 * Do nothing if no mapping
		 */
		CoordSystem cs = this.mapping.getSpaceAxisMapping().getCoordSystem();
		if( cs.getSystem().length() == 0 && cs.getSystem_value().length() == 0 ) {
			this.equinox_attribute = null;
			this.system_attribute = null;
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coordinate system given into the mapping." );
			return false;			
		}
		/*
		 * Consider first the case where the system is given as constant values
		 */
		if( cs.getSystem_value().length() != 0 ) {
			this.system_attribute = new AttributeHandler();
			this.system_attribute.setAsConstant();
			this.system_attribute.setValue(cs.getSystem_value().replaceAll("'", ""));
		}
		if( cs.getEquinox_value().length() != 0 ) {
			this.equinox_attribute = new AttributeHandler();
			this.equinox_attribute.setAsConstant();
			this.equinox_attribute.setValue(cs.getEquinox_value().replaceAll("'", ""));
		}
		/*
		 * Both system parameters have been given as constant value
		 * We check that can be used to build an astroframe
		 */
		if( this.equinox_attribute != null && this.system_attribute != null ) {
			try {
				this.astroframe = Coord.getAstroframe(this.system_attribute.getValue()
						, this.equinox_attribute.getValue()) ;
			} catch(SaadaException e) {
				return false;
			}
			return true;
		}
		/*
		 * At least one parameter is null, is is mapped with a keyword.
		 */
		else {
			for( AttributeHandler ah : this.productAttributeHandler.values()) {
				if( this.system_attribute == null && ah.getNameorg().equals(cs.getSystem()) ) {
					this.system_attribute = ah;
				}
				else if( this.equinox_attribute == null && ah.getNameorg().equals(cs.getEquinox()) ) {
					this.equinox_attribute = ah;
				}
			}
			/*
			 * Equinox may be null but not the system
			 */
			if( cs.getSystem().length() > 0 && this.system_attribute == null ) {
				this.equinox_attribute = null;
				Messenger.printMsg(Messenger.TRACE, "No attribute matches the Coord system given into the mapping: <" + cs.getSystem() + ">");
				return false;			
			}
			/*
			 * But if a KW is given for the equinox which is not found, the mapping is considered as wrong
			 */
			else if( cs.getEquinox().length() > 0 && this.equinox_attribute == null ) {
				this.system_attribute = null;
				Messenger.printMsg(Messenger.TRACE, "No attribute matches the equinox given into the mapping: <" + cs.getEquinox() + ">");
				return false;							
			}
			else {
				return true;			
			}
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPosAttributes() throws SaadaException {
		String msg = "";
		switch( this.spaceMappingPriority) {
		case ONLY:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position mapping priority: ONLY: only mapped keywords will be used");
			this.mapCollectionPosAttributesFromMapping() ;
			break;
		case FIRST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position mapping priority: FIRST: Mapped keywords will first be searched and then position KWs will be infered");
			if( !this.mapCollectionPosAttributesFromMapping() ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Mapped keywords not found: try to find out position keywords");
				this.mapCollectionPosAttributesAuto();
				msg = " (auto. detection) ";
			}			
			break;
		case LAST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position mapping priority: LAST: Position KWs will be infered and then mapped keywords will be searched");
			if( !this.mapCollectionPosAttributesAuto()) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position KWs not found: look for mapped keywords");
				this.mapCollectionPosAttributesFromMapping();
			}			
			else {
				msg = " (auto. detection) ";
			}
			break;
		default: 
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No position mapping priority: Only Position KWs will be infered");
			this.mapCollectionPosAttributesAuto();
			msg = " (auto. detection) ";
		}
		/*
		 * Printout the position status
		 */
		if( this.s_ra_ref == null || this.s_dec_ref == null ) {
			/*
			 * For image, position can still be set from WCS keywords
			 */
			if(  this.mapping.getSpaceAxisMapping().mappingOnly() || this.mapping.getCategory() != Category.IMAGE ) {
				Messenger.printMsg(Messenger.WARNING, "Position neither found " + msg + " in keywords nor by value");
			} 
		} else {
			Messenger.printMsg(Messenger.TRACE, "Position found " + msg + "<" 
					+ ((this.s_ra_ref.isConstantValue())? ("value="+this.s_ra_ref.getValue()): ("keyword="+this.s_ra_ref.getNameorg()))
					+ ((this.s_dec_ref.isConstantValue())? (" value="+this.s_dec_ref.getValue()): (" keyword="+this.s_dec_ref.getNameorg()))
					+ ">");
		} 
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPoserrorAttributes() throws SaadaException {
		String msg="";
		switch( this.mapping.getSpaceAxisMapping().getPriority()) {
		case ONLY:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position error mapping priority: ONLY: only mapped keywords will be used");
			this.mapCollectionPoserrorAttributesFromMapping();
			break;
		case FIRST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position error mapping priority: FIRST: Mapped keywords will be first searched and then position KWs will be infered");
			if( !this.mapCollectionPoserrorAttributesFromMapping() ) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Try to find out position keywords");
				this.mapCollectionPoserrorAttributesAuto();
				msg = " (auto. detection) ";
			}			
			break;
		case LAST:
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Position error mapping priority: LAST: Position KWs will be infered and then mapped keywords will be searched");
			if( !this.mapCollectionPoserrorAttributesAuto()) {
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Look for position keywords defined into the mapping");
				this.mapCollectionPoserrorAttributesFromMapping();
			}	
			else {
				msg = " (auto. detection) ";
			}
			break;
		default: 
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No position error mapping priority: Only Position KWs will be infered");
			this.mapCollectionPoserrorAttributesAuto();
			msg = " (auto. detection) ";
		}

		/*
		 * Mapp errors on positions
		 */
		if( this.error_maj_ref == null || this.error_min_ref == null ) {
			Messenger.printMsg(Messenger.WARNING, "Error ellipse neither found " + msg + " in keywords nor by value");
		} 
		else {
			this.setError_unit();
			Messenger.printMsg(Messenger.TRACE, "Error ellipse mapped " + msg + "<" 
					+ ((this.error_min_ref.isConstantValue())? ("maj value="+this.error_min_ref.getValue()): ("maj keyword="+this.error_min_ref.getNameorg()))
					+ ((this.error_maj_ref.isConstantValue())? (" min value="+this.error_maj_ref.getValue()): (" min keyword="+this.error_maj_ref.getNameorg()))
					+ ((this.error_angle_ref.isConstantValue())? (" angle value="+this.error_angle_ref.getValue()): (" angle keyword="+this.error_angle_ref.getNameorg()))
					+ "> unit: " + this.mapping.getSpaceAxisMapping().getErrorUnit());
		} 
	}

	/**
	 * Set the error unit according to the error mapping priority
	 * @throws FatalException 
	 * 
	 */
	private void setError_unit() throws SaadaException {
		String unit_read = this.error_min_ref.getUnit();
		if( unit_read == null ) {
			unit_read = this.error_maj_ref.getUnit();			
		}
		switch( this.mapping.getSpaceAxisMapping().getPriority()) {
		case FIRST: 
			if( this.mapping.getSpaceAxisMapping().getErrorUnit() == null ) {
				this.mapping.getSpaceAxisMapping().setErrorUnit(unit_read);
			}
			break;
		case LAST: 
			if( unit_read != null ) {
				this.mapping.getSpaceAxisMapping().setErrorUnit(unit_read);
			}
			break;
		}

	}

	/**
	 * @throws Exception 
	 * 
	 */
	private boolean mapCollectionSpectralCoordinateFromMapping() throws Exception {
		AxisMapping mapping = this.mapping.getEnergyAxisMapping();
		ColumnMapping sc_col  = mapping.getColumnMapping("dispertion_column");
		ColumnMapping sc_unit = mapping.getColumnMapping("x_unit_org_csa");
		this.spectralCoordinate.setOrgUnit(SaadaConstant.STRING);		
		this.x_unit_org_ref = getMappedAttributeHander(sc_unit);
		if( sc_col.notMapped() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No mapping given for the dispersion column");
			return false;
		} else
			/*
			 * The mapping gives numeric values for the spectral range
			 */
			if( sc_col.byValue() ) {
				List<String> vals = sc_col.getValues();
				if( vals.size() == 2 ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Spectral range given as numeric values <" + vals.get(0) + " " + vals.get(1) + ">");
					this.spectralCoordinate.setOrgMin(Double.parseDouble(vals.get(0)));
					this.spectralCoordinate.setOrgMax(Double.parseDouble(vals.get(1)));	

					if( sc_unit.equals("AutoDetect") ) {
						spectralCoordinate.setOrgUnit("channel");
					} else {
						spectralCoordinate.setOrgUnit(sc_unit.getValue());					
					}
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Spectral unit set as " + spectralCoordinate.getOrgUnit());
					boolean retour = spectralCoordinate.convert() ;
					this.em_min_ref = new AttributeHandler();
					this.em_min_ref.setNameattr(ColumnMapping.NUMERIC);
					this.em_min_ref.setValue(Double.toString(spectralCoordinate.getConvertedMin()));
					this.em_max_ref = new AttributeHandler();
					this.em_max_ref.setNameattr(ColumnMapping.NUMERIC);
					this.em_max_ref.setValue(Double.toString(spectralCoordinate.getConvertedMax()));
					if( this.x_unit_org_ref !=  null ) {
						spectralCoordinate.setOrgUnit(this.x_unit_org_ref.getValue());				
					}
					return retour;
				}
				else {
					Messenger.printMsg(Messenger.WARNING, "spectral coord. <" + sc_col.getValue() + "> ca not be interptreted");						
					return false;
				}
			}
		/*
		 * If no range set in params, try to find it out from fields
		 */	
		String mappedName = sc_col.getHandler().getNameattr();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Checking if column <" + mappedName + "> exists" );
		for( AttributeHandler ah : this.productFile.getEntryAttributeHandler().values() ) {
			String key = ah.getNameorg();
			if(key.equals(mappedName) ){
				Messenger.printMsg(Messenger.TRACE, "Spectral dispersion column <" + mappedName + "> found");
				this.setEnergyMinMaxValues(this.productFile.getExtrema(key));
				if( this.x_unit_org_ref !=  null ) {
					spectralCoordinate.setOrgUnit(this.x_unit_org_ref.getValue());				
				}
				/*
				 * Although the mapping priority is ONLY, if no unit is given in mapping, 
				 * the unit found in the column description is taken
				 */
				else  if( ah.getUnit() != null && ah.getUnit().length() > 0 ) {
					spectralCoordinate.setOrgUnit(ah.getUnit());
					this.x_unit_org_ref = new AttributeHandler();
					this.x_unit_org_ref.setNameattr(ColumnMapping.NUMERIC);
					this.x_unit_org_ref.setValue(ah.getUnit());
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "spectral coord. unit <" + ah.getUnit() + "> taken from column  description");
				} else {
					Messenger.printMsg(Messenger.WARNING, "spectral coord. unit found neither in column description nor in mapping");						
				}
			}
			boolean retour = spectralCoordinate.convert() ;
			this.em_min_ref = new AttributeHandler();
			this.em_min_ref.setNameattr(ColumnMapping.NUMERIC);
			this.em_min_ref.setValue(Double.toString(spectralCoordinate.getConvertedMin()));
			this.em_max_ref = new AttributeHandler();
			this.em_max_ref.setNameattr(ColumnMapping.NUMERIC);
			this.em_max_ref.setValue(Double.toString(spectralCoordinate.getConvertedMax()));

			return retour;
		}



		return  false;	
	}

	/**
	 * @param ds
	 * @return
	 */
	private void setEnergyMinMaxValues(double[] ds) {
		if( ds == null || ds.length != 2 ) {
			this.spectralCoordinate.setOrgMax(SaadaConstant.DOUBLE);
			this.spectralCoordinate.setOrgMin(SaadaConstant.DOUBLE);
		} else {
			this.spectralCoordinate.setOrgMax(ds[1]);
			this.spectralCoordinate.setOrgMin(ds[0]);		
		}
	}

	/**
	 * Set attributes used to build nstance names.
	 * Take attributes defined into the configuration if defined. 
	 * Otherwise take attribute with UCD = meta.id;meta.main or meta.id; 
	 * @throws FatalException 
	 */
	public void mapInstanceName() throws SaadaException {
		/*
		 * Uses the config first
		 */
		ColumnMapping cm = this.mapping.getObservationAxisMapping().getColumnMapping("obs_id");
		this.name_components = new ArrayList<AttributeHandler>();
		if(  !cm.notMapped()) {
			for( AttributeHandler ah: cm.getHandlers()) {
				/*
				 * If name component is a constant (enclosed in " or '),
				 * an attribute hndler is created. Otherwise the product
				 * attribute handler matching the component is added to 
				 * component list
				 */
				if( ah.isConstantValue() ) {
					this.name_components.add(ah);					
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Constant string <" + ah.getValue() + "> added to name components");
				}
				/*
				 * flatfiles have no tableAttributeHandler
				 */
				else if( this.productAttributeHandler != null ){
					/*
					 * Attribte names reacin the command line vcan match either original name or 
					 * saada names: We must check AH one bu one witout using the Map
					 */
					for( AttributeHandler ahp: this.productAttributeHandler.values()) {
						if( ah.getNameattr().equals(ahp.getNameattr()) || ah.getNameorg().equals(ahp.getNameorg())) {
							this.name_components.add(ah);					
							if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Attribute <"+ ah.getNameorg() + "> added to name components ");
						}
					}
				}
			}
			return;
		}
		/*
		 * Uses UCDs after
		 */
		else if( this.productAttributeHandler != null ) {
			@SuppressWarnings("rawtypes")
			Iterator it = this.productAttributeHandler.keySet().iterator();
			while( it.hasNext() ) {
				AttributeHandler ah = this.productAttributeHandler.get(it.next());
				String ucd = ah.getUcd();
				if( ucd.equals("meta.id;meta.main") ) {
					this.name_components = new ArrayList<AttributeHandler>();
					this.name_components.add(ah);
					Messenger.printMsg(Messenger.TRACE, "Attribute "+ ah.getNameorg() + " taken as name (ucd=" + ucd + ")");
					return;
				}
			}
			it = this.productAttributeHandler.keySet().iterator();
			while( it.hasNext() ) {
				AttributeHandler ah = this.productAttributeHandler.get(it.next());
				String ucd = ah.getUcd();
				if( ucd.equals("meta.id") ) {
					this.name_components = new ArrayList<AttributeHandler>();
					this.name_components.add(ah);
					Messenger.printMsg(Messenger.TRACE, "Attribute "+ ah.getNameorg() + " taken as name (ucd=" + ucd + ")");
					return;
				}
			}

		}
	}
	/**
	 * Look first for fields with good UCDs. 
	 * Parse field names if not
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionPosAttributesAuto() throws SaadaException {
		this.setSpaceKWDetector();
		if( this.spaceKWDetector.arePosColFound() ) {
			this.s_ra_ref = this.spaceKWDetector.getAscension_kw();
			this.s_dec_ref = this.spaceKWDetector.getDeclination_kw();				
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Look first for fields with good UCDs. 
	 * Parse field names if not
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionPoserrorAttributesAuto() throws SaadaException {
		this.setSpaceKWDetector();
		this.error_min_ref = this.spaceKWDetector.getErrorMin();
		this.error_maj_ref = this.spaceKWDetector.getErrorMaj();
		this.error_angle_ref = this.spaceKWDetector.getErrorAngle();

		if( this.error_angle_ref == null ) {
			this.error_angle_ref = new AttributeHandler();
			this.error_angle_ref.setNameattr(ColumnMapping.NUMERIC);
			this.error_angle_ref.setValue("0");
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Set angle=0 for orror ellipses");
		}
		return (this.error_min_ref != null) & (this.error_maj_ref != null);
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPosAttributesFromMapping() throws SaadaException {
		ColumnMapping raMapping  =  this.mapping.getSpaceAxisMapping().getColumnMapping("s_ra");
		ColumnMapping decMapping =  this.mapping.getSpaceAxisMapping().getColumnMapping("s_dec");
		boolean ra_found = false;
		boolean dec_found = false;
		/*
		 * Process first the case where the position mapping is given as cnstant values
		 */
		if( raMapping.byValue() ) {
			this.s_ra_ref = raMapping.getHandler();
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Right Ascension set with the constant value <" +raMapping.getHandler().getValue() + ">");
		}
		if( decMapping.byValue() ) {
			this.s_dec_ref = raMapping.getHandler();	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Declination set with the constant value <" + decMapping.getHandler().getValue() + ">");
		}

		/*
		 * Look for attributes mapping the position parameters without constant values
		 */
		String raCol =  (raMapping.getHandler() != null)? raMapping.getHandler().getNameattr() : null;
		String decCol =  (decMapping.getHandler() != null)? decMapping.getHandler().getNameattr() : null;
		for( AttributeHandler ah: this.productAttributeHandler.values()) {
			String keyorg  = ah.getNameorg();
			String keyattr = ah.getNameattr();
			if( this.s_ra_ref == null && (keyorg.equals(raCol) || keyattr.equals(raCol)) ) {
				this.s_ra_ref = ah;
				ra_found = true;
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as right ascension");
			}
			if( this.s_dec_ref == null && (keyorg.equals(decCol) || keyattr.equals(decCol)) ) {
				this.s_dec_ref = ah;
				dec_found = true;
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as declination");
			}
		}
		return (ra_found && dec_found);		
	}


	/**
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPoserrorAttributesFromMapping() throws SaadaException {
		ColumnMapping errMajMapping  =  this.mapping.getSpaceAxisMapping().getColumnMapping("error_maj_csa");
		ColumnMapping errMinMapping =  this.mapping.getSpaceAxisMapping().getColumnMapping("error_min_csa");
		ColumnMapping errAngleMapping =  this.mapping.getSpaceAxisMapping().getColumnMapping("error_angle_csa");

		boolean ra_found=false, dec_found=false, angle_found=false;
		/*
		 * Process first the case where the position mapping is given as cnstant values
		 */
		if( errMajMapping.byValue() ) {
			this.error_min_ref = errMajMapping.getHandler();	
			ra_found = true;
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Major error axis set with the constant value <" + errMajMapping.getHandler().getValue() + ">");
		}
		if( errMinMapping.byValue() ) {
			this.error_maj_ref = errMinMapping.getHandler();	
			dec_found = true;
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Minor error axis set with the constant value <" + errMinMapping.getHandler().getValue() + ">");
		}
		if( errAngleMapping.byValue() ) {
			this.error_angle_ref = errAngleMapping.getHandler();	
			angle_found = true;
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Error ellipse angle set with the constant value <" + errAngleMapping.getHandler().getValue() + ">");
		}
		/*
		 * Look for attributes mapping the position parameters without constant values
		 */
		String minCol   =  (errMajMapping.getHandler() != null)?errMajMapping.getHandler().getNameattr(): null;
		String maxCol   =  (errMinMapping.getHandler() != null)?errMinMapping.getHandler().getNameattr(): null;
		String angleCol =  (errAngleMapping.getHandler() != null)?errAngleMapping.getHandler().getNameattr(): null;
		for( AttributeHandler ah: this.productAttributeHandler.values()) {
			String keyorg  = ah.getNameorg();
			String keyattr = ah.getNameattr();
			if( this.error_min_ref == null && (keyorg.equals(minCol) || keyattr.equals(minCol)) ) {
				this.error_min_ref = ah;
				ra_found = true;
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as error Maj axis");
			}
			if( this.error_maj_ref == null && (keyorg.equals(maxCol) || keyattr.equals(maxCol)) ) {
				this.error_maj_ref = ah;
				dec_found = true;
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as error mn axis");
			}
			if( this.error_angle_ref == null && (keyorg.equals(angleCol) || keyattr.equals(angleCol)) ) {
				this.error_angle_ref = ah;
				angle_found = true;
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as error ellipse orientation");
			}
		}
		/*
		 * The 3 parameters must be set at mapping time (Mapping.java) So if one is found the others are presents
		 */
		return (ra_found & dec_found & angle_found);		
	}

	/**
	 * @return Returns the metaclass.
	 */
	public MetaClass getMetaclass() {
		return metaclass;
	}

	/**
	 * @param file_to_merge
	 * @throws FitsException
	 * @throws IOException
	 * @throws SaadaException
	 */
	public void mergeProductFormat(File file_to_merge) throws FitsException, IOException, SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge format with file <" + file_to_merge.getName() + ">");

		/*
		 * Build a new set of attribute handlers from the product given as a parameter
		 */
		ProductBuilder prd_to_merge = this.mapping.getNewProductInstance(file_to_merge);
		prd_to_merge.mapping = this.mapping;

		try {
			prd_to_merge.productFile = new FitsProduct(prd_to_merge);		
			this.typeFile = "FITS";
		} catch(Exception ef) {
			try {
				prd_to_merge.productFile = new VOTableProduct(prd_to_merge);
				this.typeFile = "VO";
			} catch(Exception ev) {
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, "<" + file_to_merge + "> neither FITS nor VOTable");			
			}
		}
		this.mergeAttributeHandlers(prd_to_merge.getProductAttributeHandler());
		prd_to_merge.close();
	}

	/**
	 * @param ah_to_merge
	 * @throws FatalException 
	 */
	public void mergeAttributeHandlers(Map<String, AttributeHandler>ah_to_merge) throws SaadaException {
		/*
		 * Merge old a new sets of attribute handlers
		 */
		Iterator<AttributeHandler> it = ah_to_merge.values().iterator();
		while( it.hasNext()) {
			AttributeHandler new_att = it.next();
			AttributeHandler old_att = null;
			if( (old_att = this.productAttributeHandler.get(new_att.getNameattr())) != null ) {
				old_att.mergeAttribute(new_att);
			}
			else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameattr() + ">");
				this.productAttributeHandler.put(new_att.getNameattr(), new_att);
			}
		}
		this.setFmtsignature();
	}

	/**
	 * @return Returns the metaclass.
	 */
	public void setMetaclass(MetaClass mc) {
		metaclass = mc;
	}

	public void printKW() {
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Keyword list of " + this.getClass().getName());
		String[] keys = this.productAttributeHandler.keySet().toArray(new String[0]);
		for( int i=0 ; i<keys.length ; i++ ) {
			System.out.println("- " + (i+1) + ": " + keys[i]);
		}
	}


	/**
	 * @return
	 */
	public String getName() {
		return this.file.getName();
	}


	public CharSequence getCanonicalPath() throws IOException {
		return this.file.getCanonicalPath();
	}


	public String getParent() {
		return this.file.getParent();
	}

	public String toString() {
		return this.file.getAbsolutePath();
	}
	/**
	 * Returns a possible classname derived from the data file product name
	 * @return
	 */
	public String  possibleClassName() {
		String ret = new File(this.productFile.getName()).getName().split("\\.")[0].replaceAll("[^\\w]+", "_").toLowerCase();
		if( !ret.matches(RegExp.CLASSNAME) ) {
			ret = "C_" + ret;
		}
		return ret;
	}

	/*
	 * 
	 * Reporting
	 *
	 */

	/**
	 * @param col
	 * @param att
	 */
	private void traceReportOnAttRef(String col, AttributeHandler att){
		String msg = col + " ";
		if( att == null ){
			msg += "not set";
		} else if( att.getNameattr().equals(ColumnMapping.NUMERIC) ) {
			msg += "Taken by value <" + att.getValue() +">";
		} else if( att.getNameattr().equals(ColumnMapping.UNDEFINED) ) {
			msg += "Undefined";
		} else {
			msg += "Taken by keyword <" + att.getNameorg() +">";
		}
		Messenger.printMsg(Messenger.TRACE, msg);	
	}

	protected String getReportOnAttRef(String col, AttributeHandler att){
		String msg = col + " ";
		if( att == null ){
			msg += "not set";
		} else if( att.getNameattr().equals(ColumnMapping.NUMERIC) ) {
			msg += "Taken by value <" + att.getValue() +">";
		} else if( att.getNameattr().equals(ColumnMapping.UNDEFINED) ) {
			msg += "Undefined";
		} else {
			msg += "Taken by keyword <" + att.getNameorg() +">";
		}
		return msg;	
	}
	/**
	 * Build a map with all collection level value of the current instance.
	 * Values are stored in AttributeHandler having the mapping mode into the comment field
	 * @throws Exception
	 */
	public Map<String, AttributeHandler> getReport() throws Exception {
		this.setProductIngestor();
		SaadaInstance si = this.productIngestor.saadaInstance;
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		AttributeHandler ah = new AttributeHandler();
		ah.setNameattr("obs_collection"); ah.setNameorg("obs_collection"); 
		ah.setValue(si.getFieldValue("obs_collection").toString());
		ah.setComment(this.getReportOnAttRef("obs_collection", obs_collection_ref));
		retour.put("obs_collection", ah);

		ah = new AttributeHandler();
		ah.setNameattr("target_name"); ah.setNameorg("target_name"); 
		ah.setValue(si.getFieldValue("target_name").toString());
		ah.setComment(this.getReportOnAttRef("target_name", target_name_ref));
		retour.put("target_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("facility_name"); ah.setNameorg("facility_name"); 
		ah.setValue(si.getFieldValue("facility_name").toString());
		ah.setComment(this.getReportOnAttRef("facility_name", facility_name_ref));
		retour.put("facility_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("instrument_name"); ah.setNameorg("instrument_name"); 
		ah.setValue(si.getFieldValue("instrument_name").toString());
		ah.setComment(this.getReportOnAttRef("instrument_name", instrument_name_ref));
		retour.put("instrument_name", ah);

		ah = new AttributeHandler();
		ah.setNameattr("s_ra"); ah.setNameorg("s_ra"); 
		ah.setValue(si.getFieldValue("s_ra").toString());
		ah.setComment(this.getReportOnAttRef("s_ra", s_ra_ref));
		retour.put("s_ra", ah);

		ah = new AttributeHandler();
		ah.setNameattr("s_dec"); ah.setNameorg("s_dec"); 
		ah.setValue(si.getFieldValue("s_dec").toString());
		ah.setComment(this.getReportOnAttRef("s_dec", s_dec_ref));
		retour.put("s_dec", ah);

		ah = new AttributeHandler();
		ah.setNameattr("error_maj_csa"); ah.setNameorg("error_maj_csa"); 
		ah.setValue(si.getFieldValue("error_maj_csa").toString());
		ah.setComment(this.getReportOnAttRef("error_maj_csa", error_maj_ref));
		retour.put("error_maj_csa", ah);

		ah = new AttributeHandler();
		ah.setNameattr("error_min_csa"); ah.setNameorg("error_min_csa"); 
		ah.setValue(si.getFieldValue("error_min_csa").toString());
		ah.setComment(this.getReportOnAttRef("error_min_csa", error_min_ref));
		retour.put("error_min_csa", ah);

		ah = new AttributeHandler();
		ah.setNameattr("error_angle_csa"); ah.setNameorg("error_angle_csa"); 
		ah.setValue(si.getFieldValue("error_angle_csa").toString());
		ah.setComment(this.getReportOnAttRef("error_angle_csa", error_angle_ref));
		retour.put("error_angle_csa", ah);

		ah = new AttributeHandler();
		ah.setNameattr("em_min"); ah.setNameorg("em_min"); 
		ah.setValue(si.getFieldValue("em_min").toString());
		ah.setComment(this.getReportOnAttRef("em_min", em_min_ref));
		retour.put("em_min", ah);

		ah = new AttributeHandler();
		ah.setNameattr("em_max"); ah.setNameorg("em_max"); 
		ah.setValue(si.getFieldValue("em_max").toString());
		ah.setComment(this.getReportOnAttRef("em_max", em_max_ref));
		retour.put("em_max", ah);

		ah = new AttributeHandler();
		ah.setNameattr("t_max"); ah.setNameorg("t_max"); 
		ah.setValue(si.getFieldValue("t_max").toString());
		ah.setComment(this.getReportOnAttRef("t_max", t_max_ref));
		retour.put("t_max", ah);

		ah = new AttributeHandler();
		ah.setNameattr("t_min"); ah.setNameorg("t_min"); 
		ah.setValue(si.getFieldValue("t_min").toString());
		ah.setComment(this.getReportOnAttRef("t_min", t_min_ref));
		retour.put("t_min", ah);

		for( AttributeHandler eah: this.extended_attributes_ref.values()){
			ah = new AttributeHandler();
			String ahname = eah.getNameattr();
			ah.setNameattr(ahname); ah.setNameorg(ahname); 
			ah.setValue(si.getFieldValue(ahname).toString());
			ah.setComment(this.getReportOnAttRef(ahname, eah));
			retour.put(ahname, ah);      	
		}

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
		return retour;
	}

	/**
	 * Print out the report
	 * @throws Exception
	 */
	public void printReport() throws Exception {
		for( java.util.Map.Entry<String, AttributeHandler> e: this.getReport().entrySet()){
			System.out.print(e.getKey() + "=");
			AttributeHandler ah = e.getValue();
			System.out.print(ah.getValue());
			System.out.println(" <" + ah.getComment() + ">");
		}
	}


	/**
	 * @param ap
	 * @param attributes
	 * @throws Exception
	 */
	public void testMapping(ArgsParser ap, String[][] attributes) throws Exception {
		this.mapping = ap.getProductMapping();
		System.out.println("Mapping            : " + this.mapping);
		this.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		for(int i = 0; i < attributes.length; i++ ) {
			AttributeHandler ah = new AttributeHandler();
			ah.setNameattr(attributes[i][0]);
			ah.setNameorg(attributes[i][0]);
			ah.setUcd(attributes[i][1]);
			ah.setUnit(attributes[i][2]);
			this.productAttributeHandler.put(attributes[i][0], ah);
		}
		this.mapCollectionAttributes();
		System.out.println("prod att handlers  :");
		for(AttributeHandler ah:  productAttributeHandler.values()) {
			System.out.println("  " + ah);
		}
		System.out.println("min_err_attribute  : " + error_maj_ref);
		System.out.println("maj_err_attribute  : " + error_min_ref);
		System.out.println("angle_err_attribute: " + error_angle_ref);
		System.out.println("ra_attribute       : " + s_ra_ref);
		System.out.println("dec_attribute      : " + s_dec_ref);
		System.out.println("name_components    : ");
		for(AttributeHandler ah:  name_components) {
			System.out.println("  " + ah);
		}
		System.out.println("ext att handlers   :");
		for(AttributeHandler ah:  extended_attributes_ref.values()) {
			System.out.println("  " + ah);
		}
		System.out.println("ignored att        : ");
		for(AttributeHandler ah:  ignored_attributes_ref) {
			System.out.println("  " + ah);
		}
		System.out.println("system_attribute   : " + system_attribute);
		System.out.println("equinox_attribute  : " + equinox_attribute);
		System.out.println("Astroframe         : " + astroframe);

	}
}
