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
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.AxisMapping;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.enums.ColumnSetMode;
import saadadb.enums.PriorityMode;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.prdconfiguration.CoordSystem;
import saadadb.products.inference.Coord;
import saadadb.products.inference.Image2DCoordinate;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.inference.SpatialResolutionUnitRef;
import saadadb.products.setter.ColumnSetter;
import saadadb.products.setter.ColumnSingleSetter;
import saadadb.query.parser.PositionParser;
import saadadb.util.DateUtils;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/**
 * This class is the central class managing all specificities connected in the
 * file type (at the moment "FITS" or "VO"), and the product type (at the moment
 * Sprectra, Image2D, Misc, Table) The entries management extends indirectly of
 * this class, but with specificites appropriate for tables.
 * 
 * @author michel
 * @version $Id$
 */
public class ProductBuilder {
	private static final long serialVersionUID = 1L;
	protected static String separ = System.getProperty("file.separator");
	/**
	 * Data product to load
	 */
	protected DataFile dataFile;
	protected String mimeType;
	/**
	 * loader mapping
	 */
	protected ProductMapping mapping;

	/**The list which maps attribute names formated in the standard of Saada (keys) to their objects modeling attribute informations (values)**/
	protected Map<String, AttributeHandler> productAttributeHandler;
	protected String fmtsignature;
	/*
	 * references of attributes handlers used to map the collection level
	 */
	/*
	 * Observation Axis
	 */
	protected List<AttributeHandler> name_components;
	protected ColumnSetter obs_collectionSetter=new ColumnSingleSetter();
	protected ColumnSetter target_nameSetter=new ColumnSingleSetter();
	protected ColumnSetter facility_nameSetter=new ColumnSingleSetter();
	protected ColumnSetter instrument_nameSetter=new ColumnSingleSetter();
	protected PriorityMode observationMappingPriority = PriorityMode.LAST;
	/*
	 * Space Axis
	 */
	protected ColumnSetter s_resolutionSetter=new ColumnSingleSetter();
	protected ColumnSetter s_raSetter=new ColumnSingleSetter();
	protected ColumnSetter s_decSetter=new ColumnSingleSetter();
	protected ColumnSetter s_fovSetter=new ColumnSingleSetter();
	protected ColumnSetter s_regionSetter=new ColumnSingleSetter();
	protected ColumnSetter astroframeSetter;
	protected PriorityMode spaceMappingPriority = PriorityMode.LAST;

	/*
	 * Energy Axis
	 */
	protected ColumnSetter em_minSetter=new ColumnSingleSetter();
	protected ColumnSetter em_maxSetter=new ColumnSingleSetter();
	protected ColumnSetter em_res_powerSetter=new ColumnSingleSetter();
	//	private SpectralCoordinate spectralCoordinate;
	protected ColumnSetter x_unit_orgSetter=new ColumnSingleSetter();
	protected PriorityMode energyMappingPriority = PriorityMode.LAST;
	/*
	 * Time Axis
	 */
	protected ColumnSetter t_minSetter=new ColumnSingleSetter();
	protected ColumnSetter t_maxSetter=new ColumnSingleSetter();
	protected ColumnSetter t_exptimeSetter=new ColumnSingleSetter();
	protected PriorityMode timeMappingPriority = PriorityMode.LAST;
	/*
	 * Observable Axis
	 */
	protected ColumnSetter o_ucdSetter=new ColumnSingleSetter();
	protected ColumnSetter o_unitSetter=new ColumnSingleSetter();
	protected ColumnSetter o_calib_statusSetter=new ColumnSingleSetter();
	protected PriorityMode observableMappingPriority = PriorityMode.LAST;
	/**
	 * Manage all tools used to detect quantities in keywords
	 */
	protected QuantityDetector quantityDetector=null;
	/* map: name of the collection attribute => attribute handler of the current product*/
	protected Map<String,ColumnSingleSetter> extended_attributesSetter;
	protected List<AttributeHandler> ignored_attributesSetter;

	//	protected ColumnSetter system_attribute;
	//	protected ColumnSetter equinox_attribute;
	//	protected ColumnSetter astroframeSetter;

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
	public ProductBuilder(DataFile file, ProductMapping conf) throws SaadaException{		
		Messenger.printMsg(Messenger.TRACE, "New Builder for " + file.getName());
		this.mapping = conf;
		/*
		 * priority ref copied for convenience
		 */
		this.observationMappingPriority = conf.getObservationAxisMapping().getPriority();
		this.spaceMappingPriority = conf.getSpaceAxisMapping().getPriority();
		this.energyMappingPriority = conf.getEnergyAxisMapping().getPriority();
		this.timeMappingPriority = conf.getTimeAxisMapping().getPriority();

		try {
			this.bindDataFile(file);
			this.mapCollectionAttributes();
		} catch (SaadaException e) {
			IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, e);
		} catch (Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.MAPPING_FAILURE, e);
		}	

	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#closeStream()
	 */
	public void close() throws QueryException {
		if( dataFile != null) {
			dataFile.closeStream();
		}
	}

	/**
	 * @throws SaadaException
	 */
	protected void setQuantityDetector() throws Exception {
		if( this.quantityDetector == null) {
			// unit tst purpose
			if(this.dataFile == null ) {
				this.quantityDetector = new QuantityDetector(this.productAttributeHandler, null, this.mapping);
			} else {
				this.quantityDetector = this.dataFile.getQuantityDetector(this.mapping);
			}
		}
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
	 * (attributes without values) with md5. CrossSettererence of the homonymous
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
	public DataFile getProducFile() {
		return dataFile;
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
		dataFile.initEnumeration();
		return dataFile;
	}

	/**
	 * In case of the product can have table: Returns the row number in the
	 * table. If there is no table for this product format, this method will
	 * return 0. CrossSettererence of the homonymous method defined in the current
	 * product file.
	 * 
	 * @return
	 * @throws SaadaException
	 */
	public int getNRows() throws SaadaException {
		return dataFile.getNRows();
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
		try {
			this.bindDataFile(null);
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
	public void bindDataFile(DataFile dataFile) throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Binding data file with the product builder");
		this.dataFile = dataFile;
		if( this.dataFile instanceof FitsDataFile ) {
			this.mimeType = "application/fits";
		} else if( this.dataFile instanceof VOTableDataFile ) {
			this.mimeType = "application/x-votable+xml";
		}
		this.dataFile.bindBuilder(this);
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
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.getName() + "> complete");
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
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.getName() + "> complete");
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
	 * Buiold a 
	 * @param columnMapping
	 * @param label
	 * @return
	 * @throws FatalException 
	 */
	protected ColumnSingleSetter getMappedAttributeHander(ColumnMapping columnMapping) throws FatalException {
		AttributeHandler cmah = columnMapping.getAttributeHandler();
		if( columnMapping.byValue() ){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, columnMapping.label + ": take constant value <" + columnMapping.getValue()+ ">");
			ColumnSingleSetter retour = new ColumnSingleSetter(cmah, ColumnSetMode.BY_VALUE, true, false);
			retour.completeMessage("Using user mapping");
			return retour;
		} else if( columnMapping.byAttribute() ){
			for( AttributeHandler ah: this.productAttributeHandler.values()) {
				String keyorg  = ah.getNameorg();
				String keyattr = ah.getNameattr();
				if( (keyorg.equals(cmah.getNameorg()) || keyattr.equals(cmah.getNameattr())) ) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  columnMapping.label +  ": take keyword <" + ah.getNameorg() + ">");
					ColumnSingleSetter retour = new ColumnSingleSetter(cmah, ColumnSetMode.BY_KEYWORD, true, false);
					retour.completeMessage("Using user mapping");
					return retour;
				}
			}
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  "No mapping for " + columnMapping.label );
		ColumnSingleSetter retour = new ColumnSingleSetter();
		//retour.completeMessage("Not found by mapping");
		return retour;
	}

	/**
	 * @param ah
	 * @return
	 */
	protected boolean isAttributeHandlerMapped(ColumnSetter ah) {
		return (ah != null && !ah.notSet());
	}
	/**
	 * @throws Exception 
	 * 
	 */
	protected void mapCollectionAttributes() throws Exception {
		if( this.productAttributeHandler != null ) {
			System.out.println("@@@@@@@@@@@ MAP " + this.getClass().getName());
			this.mapObservationAxe();
			this.mapSpaceAxe();
			this.mapEnergyAxe();
			this.mapTimeAxe();
			this.mapObservableAxe();
			this.mapIgnoredAndExtendedAttributes();
			System.out.println("@@@@@@@@@@@ OVER " + this.getClass().getName());
		}
	}

	/**
	 * @throws Exception
	 */
	protected void mapObservationAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Observation Axis");
		AxisMapping mapping = this.mapping.getObservationAxisMapping();
		this.setQuantityDetector();
		this.mapInstanceName();

		switch(this.observationMappingPriority){
		case ONLY:	
			PriorityMessage.only("Observation");
			this.obs_collectionSetter = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			this.target_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			this.facility_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			this.instrument_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			break;

		case FIRST:
			PriorityMessage.first("Observation");
			this.obs_collectionSetter = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				this.obs_collectionSetter = this.quantityDetector.getCollectionName();
			} 
			this.target_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				this.target_nameSetter = this.quantityDetector.getTargetName();
			}
			this.facility_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				this.facility_nameSetter = this.quantityDetector.getFacilityName();
			}
			this.instrument_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
			}
			break;
		case LAST:
			PriorityMessage.last("Observation");
			this.obs_collectionSetter = this.quantityDetector.getCollectionName();
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				this.obs_collectionSetter = getMappedAttributeHander(mapping.getColumnMapping("obs_collection"));
			}
			this.target_nameSetter = this.quantityDetector.getTargetName();
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				this.target_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("target_name"));
			}
			this.facility_nameSetter = this.quantityDetector.getFacilityName();
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				this.facility_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("facility_name"));
			}
			this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				this.instrument_nameSetter = getMappedAttributeHander(mapping.getColumnMapping("instrument_name"));
			}
			break;
		}	
		/*
		 * Take the Saada collection as default obs_collection
		 */
		if( this.obs_collectionSetter.notSet() ){
			this.obs_collectionSetter = new ColumnSingleSetter(this.mapping.getCollection() + "_" + Category.explain(this.mapping.getCategory()), false);
		}
		if( this.target_nameSetter == null) {
			this.target_nameSetter =  new ColumnSingleSetter();
		}
		if( this.facility_nameSetter == null) {
			this.facility_nameSetter = new ColumnSingleSetter();
		}
		if( this.instrument_nameSetter == null) {
			this.instrument_nameSetter = new ColumnSingleSetter();
		}
		traceReportOnAttRef("obs_collection", this.obs_collectionSetter);
		traceReportOnAttRef("target_name", this.target_nameSetter);
		traceReportOnAttRef("facility_name", this.facility_nameSetter);
		traceReportOnAttRef("instrument_name", this.instrument_nameSetter);
	}
	/**
	 * Set attributes used to build nstance names.
	 * Take attributes defined into the configuration if defined. 
	 * Otherwise take attribute with UCD = meta.id;meta.main or meta.id; 
	 * @throws FatalException 
	 */
	public void mapInstanceName() throws SaadaException {
		Messenger.printMsg(Messenger.TRACE, "Building the name");
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
					for( AttributeHandler ahp: this.productAttributeHandler.values()) {
						if( ah.getNameorg().equals(ahp.getNameorg()) || ah.getNameorg().equals(ahp.getNameorg())) {
							this.name_components.add(ah);					
							if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Attribute <"+ ah.getNameorg() + "> added to name components ");
						}
					}
				}
			}
		} else {
			this.name_components = new ArrayList<AttributeHandler>();
			String obsid="";
			ColumnSingleSetter cs;
			if( !(cs = quantityDetector.getFacilityName()).notSet() ) {
				obsid += cs.getValue();
			}
			if( !(cs = quantityDetector.getTargetName()).notSet() ){
				if( obsid.length() >= 0 ) obsid += " [";
				obsid += cs.getValue() + "]";
			}
			AttributeHandler ah = new AttributeHandler();
			if( obsid.length() >= 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Build with facility_name and target_name");
				ah.setAsConstant();
				ah.setValue(obsid);
				ah.setNameorg(cs.getAttNameOrg());
				ah.setNameattr(cs.getAttNameAtt());
			} else {
				ah.setValue(this.dataFile.getName());
			}
			this.name_components.add(ah);
			Messenger.printMsg(Messenger.TRACE, "Take <" + ah.getValue() + "> as name");
		}
	}

	/**
	 * @throws Exception
	 */
	protected void mapEnergyAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Energy Axe");
		this.setQuantityDetector();
		switch(this.energyMappingPriority){
		case ONLY:		
			PriorityMessage.only("Energy");
			this.mapCollectionSpectralCoordinateFromMapping() ;
			break;

		case FIRST:
			PriorityMessage.first("Energy");
			this.mapCollectionSpectralCoordinateFromMapping();
			if( this.em_minSetter.notSet() || this.em_maxSetter.notSet() ) {
				this.em_minSetter = this.quantityDetector.getEMin();
				this.em_maxSetter = this.quantityDetector.getEMax();				
			}
			if( this.x_unit_orgSetter.notSet()  ) {
				this.x_unit_orgSetter = this.quantityDetector.getEUnit();
			}
			if( this.em_res_powerSetter.notSet()  ) {
				this.em_res_powerSetter = this.quantityDetector.getResPower();
			}
			break;

		case LAST:
			PriorityMessage.last("Energy");
			this.mapCollectionSpectralCoordinateFromMapping();
			ColumnSingleSetter qdMin = this.quantityDetector.getEMin();
			ColumnSingleSetter qdMax = this.quantityDetector.getEMax();
			ColumnSingleSetter qdUnit = this.quantityDetector.getEUnit();
			ColumnSingleSetter qdRPow= this.quantityDetector.getResPower();
			if( qdMin.notSet() || qdMax.notSet() ) {
				this.em_minSetter = this.quantityDetector.getEMin();
				this.em_maxSetter = this.quantityDetector.getEMax();				
			} else {
				this.em_minSetter = qdMin;
				this.em_maxSetter = qdMax;				
			}
			if( qdUnit.notSet()  ) {
				this.x_unit_orgSetter = this.quantityDetector.getEUnit();
			} else {
				this.x_unit_orgSetter = qdUnit;
			}
			if( qdRPow.notSet()  ) {
				this.em_res_powerSetter = this.quantityDetector.getResPower();
			} else {
				this.em_res_powerSetter = qdRPow;
			}
		}
		this.traceReportOnAttRef("x_unit_org", this.x_unit_orgSetter);
		this.traceReportOnAttRef("em_min", this.em_minSetter);
		this.traceReportOnAttRef("em_max", this.em_maxSetter);
		this.traceReportOnAttRef("em_res_power", this.em_res_powerSetter);
	}

	/**
	 * @throws Exception
	 */
	protected void mapTimeAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Time Axe");
		AxisMapping mapping = this.mapping.getTimeAxisMapping();
		this.setQuantityDetector();

		switch(this.timeMappingPriority){
		case ONLY:			
			PriorityMessage.only("Time");
			this.t_maxSetter = this.getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			this.t_minSetter = this.getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			this.t_exptimeSetter = this.getMappedAttributeHander(mapping.getColumnMapping("t_exptime"));
			break;

		case FIRST:
			PriorityMessage.first("Time");
			this.t_maxSetter = this.getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				ColumnSingleSetter cs = this.quantityDetector.getTMax();
				this.t_maxSetter = cs.getConverted(DateUtils.getFMJD(cs.getValue()), "mjd", true);
			}
			this.t_minSetter = getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				ColumnSingleSetter cs = this.quantityDetector.getTMin();
				this.t_minSetter = cs.getConverted(DateUtils.getFMJD(cs.getValue()), "mjd", true);
			}
			this.t_exptimeSetter = getMappedAttributeHander(mapping.getColumnMapping("t_exptime"));
			if( !this.isAttributeHandlerMapped(this.t_exptimeSetter) ) {
				this.t_exptimeSetter = this.quantityDetector.getExpTime();
			}
			break;

		case LAST:
			PriorityMessage.last("Time");
			ColumnSingleSetter cs = this.quantityDetector.getTMax();
			this.t_maxSetter = cs.getConverted(DateUtils.getFMJD(cs.getValue()), "mjd", true);
			if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				this.t_maxSetter = getMappedAttributeHander(mapping.getColumnMapping("t_max"));
			}
			cs = this.quantityDetector.getTMin();
			this.t_minSetter = cs.getConverted(DateUtils.getFMJD(cs.getValue()), "mjd", true);
			if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				this.t_minSetter = getMappedAttributeHander(mapping.getColumnMapping("t_min"));
			}
			this.t_exptimeSetter = this.quantityDetector.getExpTime();
			if( !this.isAttributeHandlerMapped(this.t_exptimeSetter) ) {
				this.t_exptimeSetter = getMappedAttributeHander(mapping.getColumnMapping("t_exptime"));
			}
			break;
		}

		if( this.t_minSetter.notSet() && !this.t_maxSetter.notSet() && !this.t_exptimeSetter.notSet() ) {
			double v = Double.parseDouble(this.t_maxSetter.getValue()) - Double.parseDouble(this.t_exptimeSetter.getValue())/86400;
			this.t_minSetter = new ColumnSingleSetter();
			this.t_minSetter.setByValue(String.valueOf(v), false);
			this.t_minSetter.completeMessage("Computed from t_max and t_exptime");				
		} else if( !this.t_minSetter.notSet() && this.t_maxSetter.notSet() && !this.t_exptimeSetter.notSet() ) {
			double v = Double.parseDouble(this.t_minSetter.getValue()) + Double.parseDouble(this.t_exptimeSetter.getValue())/86400;
			this.t_maxSetter = new ColumnSingleSetter();
			this.t_maxSetter.setByValue(String.valueOf(v), false);
			this.t_maxSetter.completeMessage("Computed from t_min and t_exptime");	
		} else if( !this.t_minSetter.notSet() && !this.t_maxSetter.notSet() && this.t_exptimeSetter.notSet() ) {
			double v = Double.parseDouble(this.t_maxSetter.getValue()) - Double.parseDouble(this.t_minSetter.getValue());
			this.t_exptimeSetter = new ColumnSingleSetter();
			this.t_exptimeSetter.setByValue(String.valueOf(v), false);
			this.t_exptimeSetter.completeMessage("Computed from t_min and t_max");	
			this.t_exptimeSetter.setUnit("s");
		}

		traceReportOnAttRef("t_min", this.t_minSetter);
		traceReportOnAttRef("t_max", this.t_maxSetter);
		traceReportOnAttRef("t_exptime", this.t_exptimeSetter);
	}

	protected void mapObservableAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Observable Axe");
		AxisMapping mapping = this.mapping.getObservableAxisMapping();
		this.setQuantityDetector();

		switch(this.observableMappingPriority){
		case ONLY:			
			PriorityMessage.only("Observable");
			this.o_ucdSetter = this.getMappedAttributeHander(mapping.getColumnMapping("o_ucd"));
			this.o_unitSetter = this.getMappedAttributeHander(mapping.getColumnMapping("o_unit"));
			this.o_calib_statusSetter = this.getMappedAttributeHander(mapping.getColumnMapping("o_calib_status"));
			break;

		case FIRST:
			PriorityMessage.first("Observable");
			this.o_ucdSetter = this.getMappedAttributeHander(mapping.getColumnMapping("o_ucd"));
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				this.o_ucdSetter = this.quantityDetector.getObservableUcd();
			}
			this.o_unitSetter = getMappedAttributeHander(mapping.getColumnMapping("o_unit"));
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				this.o_unitSetter = this.quantityDetector.getObservableUnit();
			}
			this.o_calib_statusSetter = getMappedAttributeHander(mapping.getColumnMapping("o_calib_status"));
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
			}
			break;

		case LAST:
			PriorityMessage.last("Observable");
			this.o_ucdSetter = this.quantityDetector.getObservableUcd();
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				this.o_ucdSetter = this.getMappedAttributeHander(mapping.getColumnMapping("o_ucd"));
			}
			this.o_unitSetter = this.quantityDetector.getObservableUnit();
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				this.o_unitSetter = getMappedAttributeHander(mapping.getColumnMapping("o_unit"));
			}
			this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				this.o_calib_statusSetter = getMappedAttributeHander(mapping.getColumnMapping("o_calib_status"));
			}
			break;
		}

		traceReportOnAttRef("o_ucd", this.o_ucdSetter);
		traceReportOnAttRef("o_unit", this.o_unitSetter);
		traceReportOnAttRef("o_calib_status", this.o_calib_statusSetter);
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
		 * Don't map position if there is no astroframe
		 */
		//if( this.astroframe != null || this.system_attribute != null) {
		this.mapCollectionPosAttributes();
		this.mapCollectionPoserrorAttributes();
		//}
		traceReportOnAttRef("frame", astroframeSetter);
		traceReportOnAttRef("s_ra", s_raSetter);
		traceReportOnAttRef("s_dec", s_decSetter);
		traceReportOnAttRef("s_region", s_regionSetter);
		traceReportOnAttRef("s_fov", s_fovSetter);
		traceReportOnAttRef("s_resolution", s_resolutionSetter);
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	public void mapIgnoredAndExtendedAttributes () throws SaadaException {
		//LinkedHashMap<String, String> mapped_extend_att = this.mapping.getExtenedAttMapping().getClass() getAttrExt();
		Set<String> extendedAtt = this.mapping.getExtenedAttMapping().getColmunSet();
		this.extended_attributesSetter = new LinkedHashMap<String, ColumnSingleSetter>();
		this.ignored_attributesSetter  = new ArrayList<AttributeHandler>();
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
				this.extended_attributesSetter.put(att_ext_name , new ColumnSingleSetter(columnMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false));
			}
			/*
			 * Flatfile have not tableAttributeHandler
			 */
			else if (this.productAttributeHandler != null ) {
				String cm = (columnMapping.getAttributeHandler() != null)? columnMapping.getAttributeHandler().getNameattr(): null;
				for( AttributeHandler ah : this.productAttributeHandler.values() ) {
					if( ah.getNameattr().equals(cm)) {
						this.extended_attributesSetter.put(att_ext_name,new ColumnSingleSetter( ah, ColumnSetMode.BY_KEYWORD, true, false));
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
	protected void mapCollectionCooSysAttributes() throws Exception {
		String msg = "";
		switch(this.spaceMappingPriority) {
		case ONLY :
			PriorityMessage.only("Coo system");
			this.mapCollectionCooSysAttributesFromMapping();
			break;
		case FIRST :
			PriorityMessage.first("Coo system");
			if( !this.mapCollectionCooSysAttributesFromMapping() ) {
				this.mapCollectionCooSysAttributesAuto();
				msg = " (auto.detection) ";
			}			
			break;
		case LAST :
			PriorityMessage.last("Coo system");
			if( !this.mapCollectionCooSysAttributesAuto()) {
				this.mapCollectionCooSysAttributesFromMapping();
			}			
			else {
				msg = " (auto.detection) ";
			}
			break;
		default:
			this.mapCollectionCooSysAttributesAuto();
			msg = " (auto.detection) ";
		}
		//		/*
		//		 * If the mapping given in ONLY mode is wrong, we don't use any default coord sys.
		//		 */
		//		if( this.astroframe == null && this.system_attribute == null ) {
		//			if( this.mapping.getSpaceAxisMapping().mappingOnly() ) {
		//				this.s_raSetter = new ColumnSetter();
		//				this.s_decSetter = new ColumnSetter();
		//				Messenger.printMsg(Messenger.WARNING, "No coord system " + msg + " found: position won't be set");
		//			}
		//			else {
		//				this.astroframe = new ICRS();
		//				Messenger.printMsg(Messenger.TRACE, "Product coordinate system taken (default value) <" + this.astroframe +  "> ");
		//			}
		//		}
		//		/*
		//		 * The following test suit is just made to display proper messages
		//		 */
		//		else if( this.astroframe != null) {
		//			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken  " + msg + "<" + this.astroframe + "> ");
		//		} else if( this.equinox_attribute != null ) {
		//			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken " + msg + "<" 
		//					+ this.system_attribute.message + " "
		//					+ this.equinox_attribute.message
		//					+ "> ");
		//		} else{
		//			Messenger.printMsg(Messenger.TRACE, "Product coordinate System taken  " + msg + "<" 
		//					+ this.system_attribute.message
		//					+ "> ");
		//		} 		
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionCooSysAttributesAuto() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to find out the coord system in kw");

		this.setQuantityDetector();
		this.astroframeSetter = this.quantityDetector.getFrame();
		if( this.astroframeSetter.notSet() &&  Messenger.debug_mode) {
			Messenger.printMsg(Messenger.DEBUG, "No coosys found");
			return false;
		}
		return true;
	}

	/**
	 * Attemps to apply the Coord system mappping rules to the current product
	 * @return
	 * @throws FatalException 
	 */
	private boolean mapCollectionCooSysAttributesFromMapping() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to map the coord system from mapping");
		/*
		 * Do nothing if no mapping
		 */
		CoordSystem cs = this.mapping.getSpaceAxisMapping().getCoordSystem();
		if( cs.getSystem().length() == 0 && cs.getSystem_value().length() == 0 ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No coordinate system given into the mapping." );
			return false;			
		}
		/*
		 * Consider first the case where the system is given as constant values
		 */
		String system_attribute="", equinox_attribute="";
		if( cs.getSystem_value().length() != 0 ) {
			system_attribute = cs.getSystem_value().replaceAll("'", "");
		}
		if( cs.getEquinox_value().length() != 0 ) {
			equinox_attribute = cs.getEquinox_value().replaceAll("'", "");
		}
		/*
		 * Both system parameters have been given as constant value
		 * We check that can be used to build an astroframe
		 */
		if( system_attribute.length() > 0/* && equinox_attribute.length() > 0*/ ) {
			try {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Found " + system_attribute + " in mapping");
				Astroframe astroFrame = Coord.getAstroframe(system_attribute, equinox_attribute) ;
				this.astroframeSetter = new ColumnSingleSetter();
				this.astroframeSetter.setByValue(astroFrame.toString(), true);
				this.astroframeSetter.completeMessage("From both mapped value");
				this.astroframeSetter.storedValue = astroFrame;
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
				if( ah.getNameorg().equals(cs.getSystem()) ) {
					system_attribute = ah.getValue();
				} else if( ah.getNameorg().equals(cs.getEquinox()) ) {
					equinox_attribute = ah.getValue();
				}
			}
			/*
			 * Equinox may be null but not the system
			 */
			if( cs.getSystem().length() > 0 && system_attribute.length() == 0) {
				Messenger.printMsg(Messenger.TRACE, "No attribute matches the Coord system given into the mapping: <" + cs.getSystem() + ">");
				return false;			
			}
			/*
			 * But if a KW is given for the equinox which is not found, the mapping is considered as wrong
			 */
			else if( cs.getEquinox().length() > 0 &&  equinox_attribute.length() == 0 ) {
				Messenger.printMsg(Messenger.TRACE, "No attribute matches the equinox given into the mapping: <" + cs.getEquinox() + ">");
				return false;							
			} else {
				try {
					Astroframe astroFrame = Coord.getAstroframe(system_attribute, equinox_attribute) ;
					this.astroframeSetter = new ColumnSingleSetter();
					this.astroframeSetter.setByValue(astroFrame.toString(), true);
					this.astroframeSetter.completeMessage("From both system and equinox keywords");
					this.astroframeSetter.storedValue = astroFrame;
				} catch(SaadaException e) {
					return false;
				}
				return true;			
			}
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPosAttributes() throws Exception {
		switch( this.spaceMappingPriority) {
		case ONLY:
			PriorityMessage.only("Position");
			this.mapCollectionPosAttributesFromMapping() ;
			break;
		case FIRST:
			PriorityMessage.first("Position");
			if( !this.mapCollectionPosAttributesFromMapping() ) {
				this.mapCollectionPosAttributesAuto();
			}	
			if( this.s_fovSetter.notSet() ){
				this.s_fovSetter = new ColumnSingleSetter();
				this.s_fovSetter.completeMessage("Default value");
				this.s_fovSetter.setByValue("0", false);
			}
			break;
		case LAST:
			PriorityMessage.last("Position");
			if( !this.mapCollectionPosAttributesAuto()) {
				this.mapCollectionPosAttributesFromMapping();
			}			
			break;
		default: 
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No position mapping priority: Only Position KWs will be infered");
			this.mapCollectionPosAttributesAuto();
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPoserrorAttributes() throws Exception {
		String msg="";
		switch( this.spaceMappingPriority) {
		case ONLY:
			PriorityMessage.only("Pos error");
			this.mapCollectionPoserrorAttributesFromMapping();
			break;
		case FIRST:
			PriorityMessage.first("Pos error");
			if( !this.mapCollectionPoserrorAttributesFromMapping() ) {
				this.mapCollectionPoserrorAttributesAuto();
				msg = " (auto. detection) ";
			}			
			break;
		case LAST:
			PriorityMessage.last("Pos error");
			if( !this.mapCollectionPoserrorAttributesAuto()) {
				this.mapCollectionPoserrorAttributesFromMapping();
			}	 else {
				msg = " (auto. detection) ";
			}
			break;
		default: 
			this.mapCollectionPoserrorAttributesAuto();
			msg = " (auto. detection) ";
		}

		/*
		 * Map errors on positions
		 */
		if( !this.s_resolutionSetter.notSet() ){
			this.setError_unit();
		} 
	}

	/**
	 * Set the error unit according to the error mapping priority
	 * @throws FatalException 
	 * 
	 */
	private void setError_unit() throws SaadaException {
		String unit_read = this.s_resolutionSetter.getUnit();

		switch( this.spaceMappingPriority) {
		case FIRST: 
			PriorityMessage.first("Error unit");
			if( this.mapping.getSpaceAxisMapping().getErrorUnit() == null && unit_read.length() > 0) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No unit for pos error in mapping: take <" + unit_read + "> (infered)");
				this.mapping.getSpaceAxisMapping().setErrorUnit(unit_read);
			}
			break;
		case LAST: 
			PriorityMessage.last("Error unit");
			if( unit_read.length() > 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Take <" + unit_read + "> as pos error unit (infered)");
				this.mapping.getSpaceAxisMapping().setErrorUnit(unit_read);
			}
			break;
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void mapCollectionSpectralCoordinateFromMapping() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "try to map the Energy axis from mapping");
		AxisMapping mapping     = this.mapping.getEnergyAxisMapping();
		ColumnMapping sc_col    = mapping.getColumnMapping("dispertion_column");

		this.x_unit_orgSetter   = this.getMappedAttributeHander(mapping.getColumnMapping("x_unit_org_csa"));
		this.em_res_powerSetter = this.getMappedAttributeHander(mapping.getColumnMapping("em_res_power"));
		this.em_minSetter = new ColumnSingleSetter();
		this.em_maxSetter = new ColumnSingleSetter();

		if( sc_col.notMapped() ){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "No mapping given for the dispersion column");
			return ;
		} else
			/*
			 * The mapping gives numeric values for the spectral range
			 */
			if( sc_col.byValue() ) {
				List<String> vals = sc_col.getValues();
				if( vals.size() == 2 ) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Spectral range given as numeric values <" + vals.get(0) + " " + vals.get(1) + ">");
					this.em_minSetter = new ColumnSingleSetter();
					this.em_minSetter.setByValue(vals.get(0), true);
					this.em_maxSetter = new ColumnSingleSetter();
					this.em_maxSetter.setByValue(vals.get(1), true);
				} else {
					Messenger.printMsg(Messenger.TRACE, "spectral coord. <" + sc_col.getValue() + "> can not be interptreted");						
					return;
				}
			}
		/*
		 * If no range set in params, find it out from table columns
		 */	
		String mappedName = sc_col.getAttributeHandler().getNameorg();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Checking if column <" + mappedName + "> exists" );
		for( AttributeHandler ah : this.dataFile.getEntryAttributeHandler().values() ) {
			String key = ah.getNameorg();
			if(key.equals(mappedName) ){
				Messenger.printMsg(Messenger.TRACE, "Spectral dispersion column <" + mappedName + "> found");
				/*
				 * Although the mapping priority is ONLY, if no unit is given in mapping, 
				 * the unit found in the column description is taken
				 */
				if( ah.getUnit() != null && ah.getUnit().length() > 0 ) {
					this.x_unit_orgSetter = new ColumnSingleSetter(ah.getUnit(), true);
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "spectral coord. unit <" + ah.getUnit() + "> taken from column  description");
				} else {
					Messenger.printMsg(Messenger.WARNING, "spectral coord. unit found neither in column description nor in mapping");		
					return;
				}
				this.em_minSetter.setByKeyword(Double.toString(this.dataFile.getExtrema(key)[0]), true);
				this.em_maxSetter.setByKeyword(Double.toString(this.dataFile.getExtrema(key)[0]), true);
				return;
			}
		}
		return ;	
	}

	/**
	 * Look first for fields with good UCDs. 
	 * Parse field names if not
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionPosAttributesAuto() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to find the position in keywords");
		this.setQuantityDetector();
		System.out.println(this.productAttributeHandler.get("_ra_pnt"));
		System.out.println(this.productAttributeHandler.get("RA_PNT"));
		if( this.quantityDetector.arePosColFound() ) {
			if( this.astroframeSetter.notSet() ){
				this.astroframeSetter = this.quantityDetector.getFrame();
			}
			this.s_raSetter = this.quantityDetector.getAscension();
			this.s_decSetter = this.quantityDetector.getDeclination();		
			this.s_fovSetter = this.quantityDetector.getfov();
			this.s_regionSetter = this.quantityDetector.getRegion();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Position OK");
			return true;
		} else {
			this.s_raSetter = new ColumnSingleSetter();
			this.s_decSetter = new ColumnSingleSetter();		
			this.s_fovSetter = new ColumnSingleSetter();
			this.s_regionSetter = new ColumnSingleSetter();
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		}
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPosAttributesFromMapping() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to map the position from mapping");
		ColumnMapping raMapping  =  this.mapping.getSpaceAxisMapping().getColumnMapping("s_ra");
		if (raMapping.notMapped() && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "No mapping for s_ra");
		ColumnMapping decMapping =  this.mapping.getSpaceAxisMapping().getColumnMapping("s_dec");
		if (decMapping.notMapped() && Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "No mapping for s_dec");
		boolean ra_found = false;
		boolean dec_found = false;
		/*
		 * Process first the case where the position mapping is given as constant values
		 */
		if( raMapping.byValue() ) {
			this.s_raSetter = new ColumnSingleSetter(raMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Right Ascension set with the constant value <" +raMapping.getAttributeHandler().getValue() + ">");
			ra_found = true;
		} else {
			this.s_raSetter = new ColumnSingleSetter();
		}
		if( decMapping.byValue() ) {
			this.s_decSetter = new ColumnSingleSetter(decMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Declination set with the constant value <" + decMapping.getAttributeHandler().getValue() + ">");
			dec_found = true;
		} else {
			this.s_decSetter = new ColumnSingleSetter();
		}
		this.s_regionSetter = getMappedAttributeHander(this.mapping.getSpaceAxisMapping().getColumnMapping("s_region"));
		this.s_fovSetter = getMappedAttributeHander(this.mapping.getSpaceAxisMapping().getColumnMapping("s_fov"));

		/*
		 * Look for attributes mapping the position parameters without constant values
		 */
		if( !(ra_found && dec_found) ) {
			String raCol =  (raMapping.getAttributeHandler() != null)? raMapping.getAttributeHandler().getNameorg() : null;
			String decCol =  (decMapping.getAttributeHandler() != null)? decMapping.getAttributeHandler().getNameorg() : null;
			for( AttributeHandler ah: this.productAttributeHandler.values()) {
				String keyorg  = ah.getNameorg();
				String keyattr = ah.getNameattr();
				if( this.s_raSetter.notSet() && (keyorg.equals(raCol) || keyattr.equals(raCol)) ) {
					this.s_raSetter = new ColumnSingleSetter(ah,  ColumnSetMode.BY_KEYWORD, true, false);
					ra_found = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as right ascension");
				}
				if( this.s_decSetter.notSet() && (keyorg.equals(decCol) || keyattr.equals(decCol)) ) {
					this.s_decSetter = new ColumnSingleSetter(ah,  ColumnSetMode.BY_KEYWORD, true, false);;
					dec_found = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as declination");
				}
			}

			if( this.astroframeSetter.notSet() && (ra_found && dec_found) ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Try to infer the astroframe from the mapped keywords");
				String raName = this.s_raSetter.getAttNameOrg();
				String raUcd = (this.s_raSetter.getUcd() != null)? this.s_raSetter.getUcd(): "";				
				String decName = this.s_decSetter.getAttNameOrg();
				String decUcd = (this.s_decSetter.getUcd() != null)? this.s_decSetter.getUcd(): "";
				Astroframe af = null;
				if( (raName.matches(RegExp.ICRS_RA_KW) || raUcd.matches(RegExp.RA_MAINUCD) || raUcd.matches(RegExp.RA_UCD)) &&
						(decName.matches(RegExp.ICRS_DEC_KW) || decUcd.matches(RegExp.DEC_MAINUCD) || decUcd.matches(RegExp.DEC_UCD)) ) {
					af = new ICRS();				
				} else if( (raName.matches(RegExp.FK5_RA_KW) || raUcd.matches(RegExp.RA_MAINUCD) || raUcd.matches(RegExp.RA_UCD)) &&
						(decName.matches(RegExp.FK5_DEC_KW) || decUcd.matches(RegExp.DEC_MAINUCD) || decUcd.matches(RegExp.DEC_UCD)) ) {
					af = new FK5();				
				} else if( (raName.matches(RegExp.FK4_RA_KW) || raUcd.matches(RegExp.RA_MAINUCD) || raUcd.matches(RegExp.RA_UCD)) &&
						(decName.matches(RegExp.FK4_DEC_KW) || decUcd.matches(RegExp.DEC_MAINUCD) || decUcd.matches(RegExp.DEC_UCD)) ) {
					af = new FK4();				
				} else if( (raName.matches(RegExp.GALACTIC_RA_KW) || raUcd.matches(RegExp.GALACTIC_RA_MAINUCD) || raUcd.matches(RegExp.GALACTIC_RA_UCD)) &&
						(decName.matches(RegExp.GALACTIC_DEC_KW) || decUcd.matches(RegExp.GALACTIC_DEC_MAINUCD) || decUcd.matches(RegExp.GALACTIC_DEC_UCD)) ) {
					af = new Galactic();				
				} else if( (raName.matches(RegExp.ECLIPTIC_RA_KW) || raUcd.matches(RegExp.ECLIPTIC_RA_MAINUCD) || raUcd.matches(RegExp.ECLIPTIC_RA_UCD)) &&
						(decName.matches(RegExp.ECLIPTIC_DEC_KW) || decUcd.matches(RegExp.ECLIPTIC_DEC_MAINUCD) || decUcd.matches(RegExp.ECLIPTIC_DEC_UCD)) ) {
					af = new Galactic();				
				}
				if( af != null ){
					this.astroframeSetter.setByValue("", true);
					this.astroframeSetter.storedValue= af;
					this.astroframeSetter.completeMessage("Inferred from key words orf UCDs of " + raName + " and " + decName);
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Take " + af + ": inferred from key words orf UCDs of " + raName + " and " + decName);					
				} else {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, "Failed");
				}
			}
		}
		if (ra_found && dec_found){
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Position OK");
			return true;
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		}
	}

	/**
	 * @return
	 * @throws SaadaException 
	 */
	private boolean mapCollectionPoserrorAttributesAuto() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try detect the error on position in keywords");
		this.setQuantityDetector();
		this.s_resolutionSetter = this.quantityDetector.getSpatialError();

		if (this.s_resolutionSetter.notSet() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		} else if( !SpatialResolutionUnitRef.isUnitValid(this.s_resolutionSetter.getUnit())) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getUnit() + " is not a valid unit for the spatial resolution");		
			this.s_resolutionSetter = new ColumnSingleSetter();
			return false;
		} else {
			/*
			 * If no value: probably a table entry: no check 
			 */
			if( this.s_resolutionSetter.getValue().length() > 0 ) {
				try {
					Double.parseDouble(this.s_resolutionSetter.getValue());
				} catch (Exception e) {
					if (Messenger.debug_mode)
						Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getValue() + " is not a valid value for the spatial resolution");		
					this.s_resolutionSetter = new ColumnSingleSetter();
					return false;
				}
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Position error OK");
			this.s_resolutionSetter.completeMessage("orgVal:" + this.s_resolutionSetter.getValue() + this.s_resolutionSetter.getUnit());
			return true;
		} 
	}

	/**
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPoserrorAttributesFromMapping() throws SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to map the error on position from mapping");
		ColumnMapping sResolutionMapping   =  this.mapping.getSpaceAxisMapping().getColumnMapping("s_resolution");
		String rUnit = (sResolutionMapping.getAttributeHandler() == null)? "": sResolutionMapping.getAttributeHandler().getUnit();
		if( !SpatialResolutionUnitRef.isUnitValid(rUnit) ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, rUnit + " is not a valid unit");
			return false;		
		}

		boolean errMaj=false;
		/*
		 * Process first the case where the position mapping is given as cnstant values
		 */
		if( sResolutionMapping.byValue() ) {
			this.s_resolutionSetter = new ColumnSingleSetter(sResolutionMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);
			this.s_resolutionSetter.completeMessage("orgVal:" + this.s_resolutionSetter.getValue() + rUnit);
			errMaj = true;
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Major error axis set with the constant value <" + sResolutionMapping.getAttributeHandler().getValue() + ">");
		} else {
			/*
			 * Look for attributes mapping the position parameters without constant values
			 */
			String sResCol   =  (sResolutionMapping.getAttributeHandler() != null)?sResolutionMapping.getAttributeHandler().getNameorg(): null;
			for( AttributeHandler ah: this.productAttributeHandler.values()) {
				String keyorg  = ah.getNameorg();
				String keyattr = ah.getNameattr();
				if( this.s_resolutionSetter == null && (keyorg.equals(sResCol) || keyattr.equals(sResCol)) ) {
					this.s_resolutionSetter = new ColumnSingleSetter(ah, ColumnSetMode.BY_KEYWORD, true, false);
					this.s_resolutionSetter.completeMessage("unitOrg: " + rUnit);
					errMaj = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as error Maj axis");
				}
			}
		}
		if( !SpatialResolutionUnitRef.isUnitValid(this.s_resolutionSetter.getUnit())) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getUnit() + " is not a valid unit for the spatial resolution");		
			this.s_resolutionSetter = new ColumnSingleSetter();
			return false;
		} else if (errMaj ) {
			try {
				Double.parseDouble(this.s_resolutionSetter.getValue());
			} catch (Exception e) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getValue() + " is not a valid value for the spatial resolution");		
				this.s_resolutionSetter = new ColumnSingleSetter();
				return false;
			}
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Position error OK");
			return true;
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		}
	}

	/**
	 * @return Returns the metaclass.
	 */
	public void setMetaclass(MetaClass mc) {
		metaclass = mc;
	}
	/**
	 * @return Returns the metaclass.
	 */
	public MetaClass getMetaclass() {
		return metaclass;
	}
	/*
	 * Format merging with an existingt class
	 */
	/**
	 * @param file_to_merge
	 * @throws FitsException
	 * @throws IOException
	 * @throws SaadaException
	 */
	public void mergeProductFormat(DataFile file_to_merge) throws FitsException, IOException, SaadaException {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge format with file <" + file_to_merge.getName() + ">");

		/*
		 * Build a new set of attribute handlers from the product given as a parameter
		 */
		ProductBuilder prd_to_merge = this.mapping.getNewProductBuilderInstance(file_to_merge);
		prd_to_merge.mapping = this.mapping;

		try {
			prd_to_merge.dataFile = new FitsDataFile(prd_to_merge);		
			this.typeFile = "FITS";
		} catch(Exception ef) {
			try {
				prd_to_merge.dataFile = new VOTableDataFile(prd_to_merge);
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
			if( (old_att = this.productAttributeHandler.get(new_att.getNameorg())) != null ||
					(old_att = this.productAttributeHandler.get(new_att.getNameattr())) != null	) {
				old_att.mergeAttribute(new_att);
			}
			else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Add attribute <" + new_att.getNameorg() + ">");
				this.productAttributeHandler.put(new_att.getNameorg(), new_att);
			}
		}
		this.setFmtsignature();
	}

	/*
	 * Methods overloading the most used accessors of File
	 */
	/**
	 * @return
	 */
	public String getName() {
		return (this.dataFile == null)? "Foo": this.dataFile.getName();
	}
	/**
	 * @return
	 * @throws IOException
	 */
	public CharSequence getCanonicalPath() throws IOException {
		return (this.dataFile == null)? "foo/Foo": this.dataFile.getCanonicalPath();
	}
	/**
	 * @return
	 */
	public String getParent() {
		return  (this.dataFile == null)? "foo": this.dataFile.getParent();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return  (this.dataFile == null)? "Foo product foo/Foo": this.dataFile.getAbsolutePath().toString();
	}
	/**
	 * @return
	 */
	public long length() {
		return  (this.dataFile == null)? 1234: this.dataFile.length();
	}

	/**
	 * Returns a possible classname derived from the data file product name
	 * @return
	 */
	public String  possibleClassName() {
		String ret = new File(this.dataFile.getName()).getName().split("\\.")[0].replaceAll("[^\\w]+", "_").toLowerCase();
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
	private void traceReportOnAttRef(String col, ColumnSetter att){		
		//	Messenger.printMsg(Messenger.TRACE, this.getReportOnAttRef(col, att));	
		Messenger.printMsg(Messenger.TRACE, col + " "+ att);	
	}

	/**
	 * @param col
	 * @param att
	 * @return
	 */
	protected String getReportOnAttRef(String col, ColumnSetter att){
		String msg = col + " ";
		switch(att.getSettingMode() ){
		case BY_KEYWORD:
			msg += "Taken by keyword <" + att.getAttNameOrg() +"> "+ att.getComment();
			break;
		case BY_TABLE_COLUMN:
			msg += "Taken by table column <" + att.getAttNameOrg() +"> "+ att.getComment();
			break;
		case BY_PIXELS:
			msg += "Taken by pixels";
			break;
		case BY_VALUE:
			msg += "Taken by value <" + att.getValue() +"> " + att.getComment();
			break;
		case BY_WCS:
			msg += "Taken by WCS";
			break;
		default:
			msg += "not set";	
		}
		return msg;	
	}

	/**
	 * @return
	 */
	public List<ExtensionSetter> getReportOnLoadedExtension() {
		return this.dataFile.reportOnLoadedExtension();
	}
	/**
	 * Build a map with all collection level value of the current instance.
	 * Values are stored in AttributeHandler having the mapping mode into the comment field
	 * @throws Exception
	 */
	public Map<String, ColumnSetter> getEntryReport() throws Exception {
		return null;
	}

	/**
	 * Build a map with all collection level value of the current instance.
	 * Values are stored in AttributeHandler having the mapping mode into the comment field
	 * @throws Exception
	 */
	public Map<String, ColumnSetter> getReport() throws Exception {
		this.setProductIngestor();
		SaadaInstance si = this.productIngestor.saadaInstance;
		Map<String, ColumnSetter> retour = new LinkedHashMap<String, ColumnSetter>();
		retour.put("obs_collection", obs_collectionSetter);
		this.obs_collectionSetter.storedValue = si.getObs_collection();
		retour.put("target_name", target_nameSetter);
		this.target_nameSetter.storedValue = si.target_name;
		retour.put("facility_name", facility_nameSetter);
		this.facility_nameSetter.storedValue = si.facility_name;
		retour.put("instrument_name", instrument_nameSetter);
		this.instrument_nameSetter.storedValue = si.instrument_name;

		retour.put("s_ra", s_raSetter);
		this.s_raSetter.storedValue = si.s_ra;
		retour.put("s_dec", s_decSetter);
		this.s_decSetter.storedValue = si.s_dec;
		retour.put("s_resolution",s_resolutionSetter);
		this.s_resolutionSetter.storedValue = si.s_resolution;
		retour.put("s_fov", s_fovSetter);
		this.s_fovSetter.storedValue = si.getS_fov();
		retour.put("s_region", s_regionSetter);
		this.s_regionSetter.storedValue = si.getS_region();

		retour.put("em_min", em_minSetter);
		this.em_minSetter.storedValue = si.em_min;
		retour.put("em_max", em_maxSetter);
		this.em_maxSetter.storedValue = si.em_max;
		retour.put("em_res_power", em_res_powerSetter);
		this.em_res_powerSetter.storedValue = si.em_res_power;
		retour.put("x_unit_org", x_unit_orgSetter);
		this.x_unit_orgSetter.storedValue = this.x_unit_orgSetter.getValue();


		retour.put("t_max", t_maxSetter);
		this.t_maxSetter.storedValue = si.t_max;
		retour.put("t_min", t_minSetter);
		this.t_minSetter.storedValue = si.t_min;
		retour.put("t_exptime", t_exptimeSetter);
		this.t_exptimeSetter.storedValue = si.t_exptime;

		retour.put("o_ucd", o_ucdSetter);
		this.o_ucdSetter.storedValue = si.getO_ucd();
		retour.put("o_unit", o_unitSetter);
		this.o_unitSetter.storedValue = si.getO_unit();
		retour.put("o_calib_status", o_calib_statusSetter);
		this.o_calib_statusSetter.storedValue = si.getO_calib_status();

		for( ColumnSingleSetter eah: this.extended_attributesSetter.values()){
			retour.put(eah.getAttNameOrg(), eah);      	
		}

		for( Field f: si.getCollLevelPersisentFields() ){
			String fname = f.getName();
			if( retour.get(fname) == null ){
				AttributeHandler ah = new AttributeHandler();
				ah.setNameattr(fname); ah.setNameorg(fname); 
				Object o = si.getFieldValue(fname);
				ah.setValue((o == null)? SaadaConstant.STRING:o.toString());
				ah.setComment("Computed internally by Saada");		
				ColumnSingleSetter cs = new ColumnSingleSetter(ah, ColumnSetMode.BY_SAADA);
				cs.storedValue = ah.getValue();
				retour.put(fname, cs);
			}
		}
		return retour;
	}

	/**
	 * Print out the report
	 * @throws Exception
	 */
	public void printReport() throws Exception {
		for( java.util.Map.Entry<String, ColumnSetter> e: this.getReport().entrySet()){
			System.out.print(e.getKey() + "=");
			ColumnSetter ah = e.getValue();
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

		System.out.println("s_resolution       : " + s_resolutionSetter);
		System.out.println("ra_attribute       : " + s_raSetter);
		System.out.println("dec_attribute      : " + s_decSetter);
		System.out.println("name_components    : ");
		for(AttributeHandler ah:  name_components) {
			System.out.println("  " + ah);
		}
		System.out.println("ext att handlers   :");
		for(ColumnSingleSetter ah:  extended_attributesSetter.values()) {
			System.out.println("  " + ah);
		}
		System.out.println("ignored att        : ");
		for(AttributeHandler ah:  ignored_attributesSetter) {
			System.out.println("  " + ah);
		}
		System.out.println("Astroframe         : " + astroframeSetter);

	}
}
