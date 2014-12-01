package saadadb.products;

import hecds.LibLog;
import hecds.wcs.Modeler;
import hecds.wcs.descriptors.CardDescriptor;
import hecds.wcs.descriptors.CardMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import nom.tam.fits.FitsException;
import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.dataloader.SchemaMapper;
import saadadb.dataloader.mapping.AxisMapping;
import saadadb.dataloader.mapping.ColumnMapping;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.MetaClass;
import saadadb.prdconfiguration.CoordSystem;
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.products.inference.Coord;
import saadadb.products.inference.Image2DCoordinate;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.inference.SpatialResolutionUnitRef;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnRowSetter;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.MessengerLogger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.PriorityMode;
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
public abstract class ProductBuilder {
	private static final long serialVersionUID = 1L;
	protected static String separ = System.getProperty("file.separator");
	/**
	 * Data product to load
	 */
	public DataFile dataFile;
	protected String mimeType;
	/**
	 * loader mapping
	 */
	public ProductMapping mapping;

	/**The list which maps attribute names formated in the standard of Saada (keys) to their objects modeling attribute informations (values)**/
	public Map<String, AttributeHandler> productAttributeHandler;
	public String fmtsignature;
	/*
	 * references of attributes handlers used to map the collection level
	 */
	/*
	 * Observation Axis
	 */
	public ColumnSetter obs_idSetter=new ColumnExpressionSetter("obs_id");
	public ColumnSetter obs_collectionSetter=new ColumnExpressionSetter("obs_collection");
	public ColumnSetter obs_publisher_didSetter=new ColumnExpressionSetter("obs_publisher_did");
	public ColumnSetter calib_levelSetter=new ColumnExpressionSetter("calib_level");
	public ColumnSetter target_nameSetter=new ColumnExpressionSetter("target_name");
	public ColumnSetter facility_nameSetter=new ColumnExpressionSetter("facility_name");
	public ColumnSetter instrument_nameSetter=new ColumnExpressionSetter("instrument_name");
	public PriorityMode observationMappingPriority = PriorityMode.LAST;
	/*
	 * Space Axis
	 */
	public ColumnSetter s_resolutionSetter=new ColumnExpressionSetter("s_resolution");
	public ColumnSetter s_raSetter=new ColumnExpressionSetter("s_ra");
	public ColumnSetter s_decSetter=new ColumnExpressionSetter("s_dec");
	public ColumnSetter s_fovSetter=new ColumnExpressionSetter("s_fov");
	public ColumnSetter s_regionSetter=new ColumnExpressionSetter("s_region");
	public ColumnSetter astroframeSetter=new ColumnExpressionSetter("astroframe");
	public PriorityMode spaceMappingPriority = PriorityMode.LAST;

	/*
	 * Energy Axis
	 */
	public ColumnSetter em_minSetter=new ColumnExpressionSetter("em_min");
	public ColumnSetter em_maxSetter=new ColumnExpressionSetter("em_max");
	public ColumnSetter em_binsSetter=new ColumnExpressionSetter("em_bins");
	public ColumnSetter em_res_powerSetter=new ColumnExpressionSetter("em_res_power");
	//	private SpectralCoordinate spectralCoordinate;
	public ColumnSetter x_unit_orgSetter=new ColumnExpressionSetter("x_unit_org");
	public PriorityMode energyMappingPriority = PriorityMode.LAST;
	/*
	 * Time Axis
	 */
	public ColumnSetter t_minSetter=new ColumnExpressionSetter("t_min");
	public ColumnSetter t_maxSetter=new ColumnExpressionSetter("t_max");
	public ColumnSetter t_exptimeSetter=new ColumnExpressionSetter("t_exptime");
	public PriorityMode timeMappingPriority = PriorityMode.LAST;
	/*
	 * Observable Axis
	 */
	public ColumnSetter o_ucdSetter=new ColumnExpressionSetter("o_ucd");
	public ColumnSetter o_unitSetter=new ColumnExpressionSetter("o_unit");
	public ColumnSetter o_calib_statusSetter=new ColumnExpressionSetter("o_calib_status");
	public PriorityMode observableMappingPriority = PriorityMode.LAST;
	/*
	 * Polarization axis
	 */
	public ColumnSetter pol_statesSetter=new ColumnExpressionSetter("pol_states");
	public PriorityMode polarizationMappingPriority = PriorityMode.LAST;
	/**
	 * Manage all tools used to detect quantities in keywords
	 */
	public QuantityDetector quantityDetector=null;
	/* map: name of the collection attribute => attribute handler of the current product*/
	public Map<String,ColumnExpressionSetter> extended_attributesSetter = new LinkedHashMap<String, ColumnExpressionSetter>();
	protected List<AttributeHandler> ignored_attributesSetter= new ArrayList<AttributeHandler>();


	//	protected ColumnSetter system_attribute;
	//	protected ColumnSetter equinox_attribute;
	//	protected ColumnSetter astroframeSetter;

	/** The file type ("FITS" or "VO") * */
	protected String typeFile;
	protected MetaClass metaClass;
	public ProductIngestor productIngestor;	
	/** sed by subclasses */
	protected Image2DCoordinate wcs;
	public Modeler wcsModeler;

	/**
	 * Constructor. This is a product constructor for the new loader.
	 * @param file
	 * @param conf
	 * @throws FatalException 
	 */
	public ProductBuilder(DataFile dataFile, ProductMapping conf, MetaClass metaClass) throws SaadaException{		
		Messenger.printMsg(Messenger.TRACE, "New Builder for " + dataFile.getName());
		this.mapping = conf;
		/*
		 * priority ref copied for convenience
		 */
		this.observationMappingPriority = conf.getObservationAxisMapping().getPriority();
		this.spaceMappingPriority = conf.getSpaceAxisMapping().getPriority();
		this.energyMappingPriority = conf.getEnergyAxisMapping().getPriority();
		this.timeMappingPriority = conf.getTimeAxisMapping().getPriority();
		this.metaClass = metaClass;
		this.dataFile = dataFile;
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
			/*
			 * The WCS modeler is external to Saada, it works with CardDescripors instead of AttributeHandler
			 */
			CardMap cm = new CardMap(new HashSet<CardDescriptor>(this.productAttributeHandler.values()));
			LibLog.setLogger(new MessengerLogger());
			this.wcsModeler = new Modeler(cm);			
			// unit tst purpose
			if(this.dataFile == null ) {
				this.quantityDetector = new QuantityDetector(this.productAttributeHandler, null, this.mapping, this.wcsModeler);
			} else {
				this.quantityDetector = this.dataFile.getQuantityDetector(this.mapping);
			}
		}
	}

	/**
	 * Returns the list which maps attribute names formated in the standard of
	 * Saada (keys) to their objects modeling attribute informations (values).
	 * Generally the object modeling attribute informations is a
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

	/**
	 * Used by sub classes
	 * @return
	 */
	public long getTableOid() {
		return SaadaConstant.INT;
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
		if( dataFile != null)	this.dataFile = dataFile;
		if( this.dataFile instanceof FitsDataFile ) {
			this.mimeType = "application/fits";
		} else if( this.dataFile instanceof VOTableDataFile ) {
			this.mimeType = "application/x-votable+xml";
		}
		this.setProductIngestor();
		this.dataFile.bindBuilder(this);
		this.setFmtsignature();
	}

	/**
	 * @param dataFile
	 * @throws Exception
	 */
	public void mapDataFile(DataFile dataFile) throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Map the data file " + this.getName());
		this.bindDataFile(dataFile);
		this.mapCollectionAttributes();
	}

	public void mapDataFile() throws Exception{
		Messenger.printMsg(Messenger.TRACE, "Map the data file " + this.getName());
		System.out.println("@@@@@@@@@@@@@@@@ mapDataFile " +this.getClass());
		for( AttributeHandler ah: this.productAttributeHandler.values() ) System.out.println(ah);
		this.bindDataFile(this.dataFile);
		for( AttributeHandler ah: this.productAttributeHandler.values() ) System.out.println(ah);
		this.mapCollectionAttributes();
		for( AttributeHandler ah: this.productAttributeHandler.values() ) System.out.println(ah);
	}

	/**
	 * Update the vlaues of the local AHs with those read within the data file
	 * @throws Exception
	 */
	public void updateAttributeHandlerValues() throws Exception {
		this.dataFile.updateAttributeHandlerValues();
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
	 * Compute all columns setter expressions
	 * @throws Exception
	 */
	protected void calculateAllExpressions() throws Exception {
		this.wcsModeler.updateValues();
		this.obs_idSetter.calculateExpression();
		this.obs_collectionSetter.calculateExpression();
		this.obs_publisher_didSetter.calculateExpression();
		this.calib_levelSetter.calculateExpression();
		this.target_nameSetter.calculateExpression();
		this.facility_nameSetter.calculateExpression();
		this.instrument_nameSetter.calculateExpression();
		this.astroframeSetter.calculateExpression();
		this.s_resolutionSetter.calculateExpression();

		System.out.println("1 " + this.s_raSetter);
		this.s_raSetter.calculateExpression();
		System.out.println("2 " + this.s_raSetter);
		this.s_decSetter.calculateExpression();
		this.s_fovSetter.calculateExpression();
		this.s_regionSetter.calculateExpression();
		this.em_minSetter.calculateExpression(this.dataFile);
		this.em_maxSetter.calculateExpression(this.dataFile);
		this.em_binsSetter.calculateExpression(this.dataFile);
		this.em_res_powerSetter.calculateExpression(this.dataFile);
		this.x_unit_orgSetter.calculateExpression(this.dataFile);
		this.t_minSetter.calculateExpression(this.dataFile);
		this.t_maxSetter.calculateExpression(this.dataFile);
		this.t_exptimeSetter.calculateExpression(this.dataFile);
		this.o_ucdSetter.calculateExpression();
		this.o_unitSetter.calculateExpression();
		this.o_calib_statusSetter.calculateExpression();
		this.pol_statesSetter.calculateExpression();
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
	public void loadProduct() throws Exception  {
		System.out.println("@@@@@@@@@@@@@ loadProduct " + this.productAttributeHandler.get("_crval1") + " " + System.identityHashCode(this.productAttributeHandler.get("_crval1")));
		this.dataFile.updateAttributeHandlerValues();
		this.productIngestor.bindInstanceToFile();
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
	public void loadProduct(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		System.out.println("@@@@@@@@@@@@@ loadProduct " + this.productAttributeHandler.get("_crval1") + " " + System.identityHashCode(this.productAttributeHandler.get("_crval1")));
		this.dataFile.updateAttributeHandlerValues();
		this.productIngestor.bindInstanceToFile();
		this.productIngestor.loadValue(colwriter, buswriter, loadedfilewriter);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.getName() + "> complete");
	}

	// TODO to be removed when flatfile will use the algo as others files
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
	 * @throws Exception 
	 */
	protected ColumnExpressionSetter getMappedAttributeHander(String colmunName, ColumnMapping columnMapping) throws Exception {
		List<AttributeHandler> mappingHandlers = columnMapping.getHandlers();
		AttributeHandler mappingSingleHandler=null;
		TreeMap<String,AttributeHandler> mappingHandlerMap=null;
		//We check if we have one or several ah
		if(mappingHandlers!=null)	{
			if(mappingHandlers.size()==1 && columnMapping.byValue())	{
				mappingSingleHandler = mappingHandlers.get(0);
			} else{
				mappingHandlerMap=new TreeMap<String,AttributeHandler>();
				for(AttributeHandler ah:mappingHandlers){
					mappingHandlerMap.put(ah.getNameattr(), ah);
				}
			}
		}

		if( columnMapping.byValue() ){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, columnMapping.label + ": take constant value <" + columnMapping.getValue()+ ">");
			ColumnExpressionSetter retour ;
			//On calcule l'expression à partir de la valeur de l'ah
			if(columnMapping.getExpression()==null || columnMapping.getExpression().isEmpty())
				retour = new ColumnExpressionSetter(colmunName, mappingSingleHandler);
			else 
				retour = new ColumnExpressionSetter(colmunName, columnMapping.getExpression());
			retour.completeMessage("Using user mapping");
			return retour;

			//Expression case
		} else if( columnMapping.byKeyword() || columnMapping.byExpression() ){
			boolean ahfound=false;
			ColumnExpressionSetter retour;
			String expression = columnMapping.getExpression();
			if(expression.startsWith("WCS.")){
				Messenger.printMsg(Messenger.TRACE,  colmunName + ": WCS mapping cannot be set by the user mapping ");
				retour = new ColumnExpressionSetter(colmunName);
				retour.completeMessage("WCS mapping cannot be set by the user mapping ");
			} else if( expression.startsWith("Column.") ){
				retour = new ColumnRowSetter(colmunName, expression) ;
			}
			//When there is only one ah 
			if(mappingSingleHandler!=null) {
				for( AttributeHandler ah: this.productAttributeHandler.values()) {
					String keyorg  = ah.getNameorg();
					String keyattr = ah.getNameattr();

					if( (keyorg.equals(mappingSingleHandler.getNameorg()) || keyattr.equals(mappingSingleHandler.getNameattr())) ) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  columnMapping.label +  ": take keyword <" + ah.getNameorg() + ">");
						retour = new ColumnExpressionSetter(colmunName);

						if(columnMapping.getExpression()==null || expression.isEmpty()) {
							retour = new ColumnExpressionSetter(colmunName, mappingSingleHandler);
						} else {
							retour = new ColumnExpressionSetter(colmunName, expression, mappingHandlerMap, true);
							//retour.calculateExpression();
						}
						retour.completeMessage("Using user mapping");
						return retour;
					}

				}
			} else {
				retour = new ColumnExpressionSetter(colmunName);
				if(columnMapping.getExpression()==null || columnMapping.getExpression().isEmpty()) {
					String msg = "The expression of the columnMapping : "+ columnMapping.label + "is empty or null (looks like an internal error).";
					Messenger.printMsg(Messenger.ERROR, msg);
					retour = new ColumnExpressionSetter(colmunName);
					retour.completeMessage(msg);
					return retour;
				}
				//for every ah contained by the columnMapping
				ArrayList<String> ma = new ArrayList<String>();
				boolean mah=true;
				for(AttributeHandler ahc : mappingHandlers) {
					String keyattr = ahc.getNameattr();
					ahfound=false;
					//Plusieurs attributHandlers
					for( AttributeHandler ah: this.productAttributeHandler.values())  {
						if( ah.isNamedLike(keyattr)) {
							ahfound=true;
							ahc.setValue(ah.getValue());
							ma.add(ah.getNameorg());
							break;
						}
					}
					if(! ahfound){
						mah = false; 
						ma.add(ahc.getNameorg());
					}
				}			
				//If no corresponding ah have been found
				if(!mah) {
					String msg = "One or Several attributes "+ma+" referenced by the setter of the column " + colmunName + " are missing";
					Messenger.printMsg(Messenger.TRACE, msg);
					retour = new ColumnExpressionSetter(colmunName);
					retour.completeMessage(msg);
					return retour;
				}

				//if the attributes of the columnMapping doesn't exist we stop here
			}
			//if we found the expression AND the ah, we calculate the expression
			retour = new ColumnExpressionSetter(colmunName, columnMapping.getExpression(), mappingHandlerMap, true);
			//	retour.calculateExpression();
			retour.completeMessage("Using user mapping");
			return retour;
		} else{
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG,  "No mapping for " + columnMapping.label );
			ColumnExpressionSetter retour = new ColumnExpressionSetter(colmunName);
			//retour.completeMessage("Not found by mapping");
			return retour;
		}
	}

	/**
	 * @param ah
	 * @return
	 */
	protected boolean isAttributeHandlerMapped(ColumnSetter ah) {
		return (ah != null && ah.isSet());
	}
	/**
	 * @throws Exception 
	 * 
	 */
	protected void mapCollectionAttributes() throws Exception {
		if( this.productAttributeHandler != null ) {
			System.out.println("@@@@@@@@@@@ MAP " + this.getClass().getName());
			//(new Exception()).printStackTrace();
			this.mapObservationAxe();
			this.mapSpaceAxe();
			this.mapEnergyAxe();
			this.mapTimeAxe();
			//System.exit(1);
			this.mapObservableAxe();
			this.mapPolarizationAxe();
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
			this.obs_collectionSetter = getMappedAttributeHander("obs_collection", mapping.getColumnMapping("obs_collection"));
			this.obs_publisher_didSetter = getMappedAttributeHander("obs_publisher_did", mapping.getColumnMapping("obs_publisher_did"));
			this.calib_levelSetter = getMappedAttributeHander("calib_level", mapping.getColumnMapping("calib_level"));
			this.target_nameSetter = getMappedAttributeHander("target_name", mapping.getColumnMapping("target_name"));
			this.facility_nameSetter = getMappedAttributeHander("facility_name", mapping.getColumnMapping("facility_name"));
			this.instrument_nameSetter = getMappedAttributeHander("instrument_name", mapping.getColumnMapping("instrument_name"));
			break;

		case FIRST:
			PriorityMessage.first("Observation");
			this.obs_collectionSetter = getMappedAttributeHander("obs_collection", mapping.getColumnMapping("obs_collection"));
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				this.obs_collectionSetter = this.quantityDetector.getCollectionName();
			} 
			this.obs_publisher_didSetter = getMappedAttributeHander("obs_publisher_did", mapping.getColumnMapping("obs_publisher_did"));
			if( !this.isAttributeHandlerMapped(this.obs_publisher_didSetter) ) {
				this.obs_publisher_didSetter = this.quantityDetector.getObsPublisherDid();
			} 
			this.calib_levelSetter = getMappedAttributeHander("calib_level", mapping.getColumnMapping("calib_level"));
			if( !this.isAttributeHandlerMapped(this.obs_publisher_didSetter) ) {
				this.calib_levelSetter = this.quantityDetector.getCalibLevel();
			} 
			this.target_nameSetter = getMappedAttributeHander("target_name", mapping.getColumnMapping("target_name"));
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				this.target_nameSetter = this.quantityDetector.getTargetName();
			}
			this.facility_nameSetter = getMappedAttributeHander("facility_name", mapping.getColumnMapping("facility_name"));
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				this.facility_nameSetter = this.quantityDetector.getFacilityName();
			}
			this.instrument_nameSetter = getMappedAttributeHander("instrument_name", mapping.getColumnMapping("instrument_name"));
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
			}
			break;
		case LAST:
			PriorityMessage.last("Observation");
			this.obs_collectionSetter = this.quantityDetector.getCollectionName();
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				this.obs_collectionSetter = getMappedAttributeHander("obs_collection", mapping.getColumnMapping("obs_collection"));
			}
			this.target_nameSetter = this.quantityDetector.getTargetName();
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				this.target_nameSetter = getMappedAttributeHander("target_name", mapping.getColumnMapping("target_name"));
			}
			this.facility_nameSetter = this.quantityDetector.getFacilityName();
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				this.facility_nameSetter = getMappedAttributeHander("facility_name", mapping.getColumnMapping("facility_name"));
			}
			this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				this.instrument_nameSetter = getMappedAttributeHander("instrument_name", mapping.getColumnMapping("instrument_name"));
			}
			break;
		}	
		/*
		 * Take the Saada collection as default obs_collection
		 */
		if( this.obs_collectionSetter.isNotSet() ){
			//this.obs_collectionSetter = new ColumnExpressionSetter(this.mapping.getCollection() + "_" + Category.explain(this.mapping.getCategory()), false);
			this.obs_collectionSetter = new ColumnExpressionSetter(this.mapping.getCollection() + "_" + Category.explain(this.mapping.getCategory()));
		}
		if( this.target_nameSetter == null) {
			this.target_nameSetter =  new ColumnExpressionSetter("target_name");
		}
		if( this.facility_nameSetter == null) {
			this.facility_nameSetter = new ColumnExpressionSetter("facility_name");
		}
		if( this.instrument_nameSetter == null) {
			this.instrument_nameSetter = new ColumnExpressionSetter("instrument_name");
		}
		traceReportOnAttRef(this.obs_idSetter);
		traceReportOnAttRef(this.obs_collectionSetter);
		traceReportOnAttRef(this.target_nameSetter);
		traceReportOnAttRef(this.facility_nameSetter);
		traceReportOnAttRef(this.instrument_nameSetter);
	}
	/**
	 * Set attributes used to build instance names.
	 * @throws Exception 
	 * @throws FatalException 
	 */
	public void mapInstanceName() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Building the name");
		/*
		 * Uses the config first
		 */
		this.obs_idSetter = this.getMappedAttributeHander("obs_id", mapping.getObservationAxisMapping().getColumnMapping("obs_id"));

		if( this.obs_idSetter.isNotSet() ) {
			String expression = "";
			ColumnExpressionSetter cs;
			if( !(cs = quantityDetector.getFacilityName()).isNotSet() ) {
				expression += cs.getValue();
			}
			if( !(cs = quantityDetector.getTargetName()).isNotSet() ){
				if( expression.length() >= 0 ) expression += " [";
				expression += cs.getValue() + "]";
			}
			if( expression.length() > 0 ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Build the obs_id with both facility_name and target_name");
			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Build the obs_id with the filename");
				expression = this.dataFile.getName();
			}
			this.obs_idSetter = new ColumnExpressionSetter("obs_id", expression);
		}
		this.traceReportOnAttRef(this.obs_idSetter);
	}

	/**
	 * @throws Exception
	 */
	protected void mapEnergyAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Energy Axe");
		this.setQuantityDetector();
		AxisMapping mapping = this.mapping.getEnergyAxisMapping();
		switch(this.energyMappingPriority){
		case ONLY:		
			PriorityMessage.only("Energy");
			this.mapCollectionSpectralCoordinateFromMapping() ;
			break;

		case FIRST:
			PriorityMessage.first("Energy");
			this.mapCollectionSpectralCoordinateFromMapping();
			if( this.em_minSetter.isNotSet() || this.em_maxSetter.isNotSet() ) {
				this.em_minSetter = this.quantityDetector.getEMin();
				this.em_maxSetter = this.quantityDetector.getEMax();				
			}
			if( this.x_unit_orgSetter.isNotSet()  ) {
				this.x_unit_orgSetter = this.quantityDetector.getEUnit();
			}
			if( this.em_res_powerSetter.isNotSet()  ) {
				this.em_res_powerSetter = this.quantityDetector.getResPower();
			}
			if( this.em_binsSetter.isNotSet()  ) {
				this.em_binsSetter = this.quantityDetector.getEbins();
			}
			break;

		case LAST:
			PriorityMessage.last("Energy");
		//	this.mapCollectionSpectralCoordinateFromMapping();
			ColumnExpressionSetter qdMin = this.quantityDetector.getEMin();
			ColumnExpressionSetter qdMax = this.quantityDetector.getEMax();
			ColumnExpressionSetter qdUnit = this.quantityDetector.getEUnit();
			ColumnExpressionSetter qdRPow= this.quantityDetector.getResPower();
			ColumnExpressionSetter qdBins= this.quantityDetector.getEbins();
			if( qdMin.isNotSet() || qdMax.isNotSet() ) {
				this.em_minSetter = this.getMappedAttributeHander("em_min", mapping.getColumnMapping("em_min"));
				this.em_maxSetter = this.getMappedAttributeHander("em_max", mapping.getColumnMapping("em_max"));			
			} else {
				this.em_minSetter = qdMin;
				this.em_maxSetter = qdMax;				
			}
			if( qdUnit.isNotSet()  ) {
				this.x_unit_orgSetter = this.getMappedAttributeHander("x_unit_org", mapping.getColumnMapping("x_unit_org"));
			} else {
				this.x_unit_orgSetter = qdUnit;
			}
			if( qdRPow.isNotSet()  ) {
				this.em_res_powerSetter = this.getMappedAttributeHander("em_respower", mapping.getColumnMapping("em_res_power"));
			} else {
				this.em_res_powerSetter = qdRPow;
			}
			if( qdBins.isNotSet()  ) {
				this.em_binsSetter = this.getMappedAttributeHander("em_bins", mapping.getColumnMapping("em_bins"));
			} else {
				this.em_binsSetter = qdBins;
			}
		}
		this.traceReportOnAttRef(this.x_unit_orgSetter);
		this.traceReportOnAttRef(this.em_minSetter);
		this.traceReportOnAttRef(this.em_maxSetter);
		this.traceReportOnAttRef(this.em_res_powerSetter);
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
			this.t_maxSetter = this.getMappedAttributeHander("t_max", mapping.getColumnMapping("t_max"));
			this.t_minSetter = this.getMappedAttributeHander("t_min", mapping.getColumnMapping("t_min"));
			this.t_exptimeSetter = this.getMappedAttributeHander("t_exptime", mapping.getColumnMapping("t_exptime"));
			break;

		case FIRST:
			PriorityMessage.first("Time");
			this.t_maxSetter = this.getMappedAttributeHander("t_max", mapping.getColumnMapping("t_max"));
			if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				this.t_maxSetter = this.quantityDetector.getTMax();
			}
			this.t_minSetter = getMappedAttributeHander("t_min", mapping.getColumnMapping("t_min"));
			if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				this.t_minSetter = this.quantityDetector.getTMin();
			}
			this.t_exptimeSetter = getMappedAttributeHander("t_exptime", mapping.getColumnMapping("t_exptime"));
			if( !this.isAttributeHandlerMapped(this.t_exptimeSetter) ) {
				this.t_exptimeSetter = this.quantityDetector.getExpTime();
			}
			break;

		case LAST:
			PriorityMessage.last("Time");
			ColumnExpressionSetter cs = this.quantityDetector.getTMax();
			if( cs.isSet() ){
				this.t_maxSetter = cs;
			} else 	if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				this.t_maxSetter = getMappedAttributeHander("t_max", mapping.getColumnMapping("t_max"));
			}
			cs = this.quantityDetector.getTMin();
			if( cs.isSet() ){
				this.t_minSetter = cs;
			} else if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				this.t_minSetter = getMappedAttributeHander("t_min", mapping.getColumnMapping("t_min"));
			}
			this.t_exptimeSetter = this.quantityDetector.getExpTime();
			if( cs.isNotSet() &&!this.isAttributeHandlerMapped(this.t_exptimeSetter) ) {
				this.t_exptimeSetter = getMappedAttributeHander("t_exptime", mapping.getColumnMapping("t_exptime"));
			}
			break;
		}
		traceReportOnAttRef(this.t_minSetter);
		traceReportOnAttRef(this.t_maxSetter);
		traceReportOnAttRef(this.t_exptimeSetter);
	}

	protected void mapObservableAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Observable Axe");
		AxisMapping mapping = this.mapping.getObservableAxisMapping();
		this.setQuantityDetector();

		switch(this.observableMappingPriority){
		case ONLY:			
			PriorityMessage.only("Observable");
			this.o_ucdSetter = this.getMappedAttributeHander("o_ucd", mapping.getColumnMapping("o_ucd"));
			this.o_unitSetter = this.getMappedAttributeHander("o_unit", mapping.getColumnMapping("o_unit"));
			this.o_calib_statusSetter = this.getMappedAttributeHander("o_calib_status", mapping.getColumnMapping("o_calib_status"));
			break;

		case FIRST:
			PriorityMessage.first("Observable");
			this.o_ucdSetter = this.getMappedAttributeHander("o_ucd", mapping.getColumnMapping("o_ucd"));
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				this.o_ucdSetter = this.quantityDetector.getObservableUcd();
			}
			this.o_unitSetter = getMappedAttributeHander("o_unit", mapping.getColumnMapping("o_unit"));
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				this.o_unitSetter = this.quantityDetector.getObservableUnit();
			}
			this.o_calib_statusSetter = getMappedAttributeHander("o_calib_status", mapping.getColumnMapping("o_calib_status"));
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
			}
			break;

		case LAST:
			PriorityMessage.last("Observable");
			this.o_ucdSetter = this.quantityDetector.getObservableUcd();
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				this.o_ucdSetter = this.getMappedAttributeHander("o_ucd", mapping.getColumnMapping("o_ucd"));
			}
			this.o_unitSetter = this.quantityDetector.getObservableUnit();
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				this.o_unitSetter = getMappedAttributeHander("o_unit", mapping.getColumnMapping("o_unit"));
			}
			this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				this.o_calib_statusSetter = getMappedAttributeHander("o_calib_status", mapping.getColumnMapping("o_calib_status"));
			}
			break;
		}

		traceReportOnAttRef( this.o_ucdSetter);
		traceReportOnAttRef(this.o_unitSetter);
		traceReportOnAttRef(this.o_calib_statusSetter);
	}


	protected void mapPolarizationAxe() throws Exception {
		Messenger.printMsg(Messenger.TRACE, "Map Polarization Axe");
		AxisMapping mapping = this.mapping.getPolarizationAxisMapping();
		this.setQuantityDetector();

		switch(this.observableMappingPriority){
		case ONLY:			
			PriorityMessage.only("Observable");
			this.pol_statesSetter = this.getMappedAttributeHander("pol_states", mapping.getColumnMapping("pol_states"));
			break;

		case FIRST:
			PriorityMessage.first("Observable");
			this.pol_statesSetter = this.getMappedAttributeHander("pol_states", mapping.getColumnMapping("pol_states"));
			if( !this.isAttributeHandlerMapped(this.pol_statesSetter) ) {
				this.pol_statesSetter = this.quantityDetector.getPolarizationStates();
			}
			break;

		case LAST:
			PriorityMessage.last("Observable");
			this.pol_statesSetter = this.quantityDetector.getPolarizationStates();
			if( !this.isAttributeHandlerMapped(this.pol_statesSetter) ) {
				this.pol_statesSetter = this.getMappedAttributeHander("pol_states", mapping.getColumnMapping("pol_states"));
			}
			break;
		}

		traceReportOnAttRef( this.pol_statesSetter);
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
		this.mapCollectionPosAttributes();
		this.mapCollectionPoserrorAttributes();
		traceReportOnAttRef(astroframeSetter);
		traceReportOnAttRef(s_raSetter);
		traceReportOnAttRef(s_decSetter);
		traceReportOnAttRef(s_regionSetter);
		traceReportOnAttRef(s_fovSetter);
		traceReportOnAttRef(s_resolutionSetter);
	}

	/**
	 * @throws Exception 
	 * @throws FatalException 
	 * 
	 */
	public void mapIgnoredAndExtendedAttributes () throws Exception {
		//LinkedHashMap<String, String> mapped_extend_att = this.mapping.getExtenedAttMapping().getClass() getAttrExt();
		Set<String> extendedAtt = this.mapping.getExtenedAttMapping().getColmunSet();
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
				this.extended_attributesSetter.put(att_ext_name , new ColumnExpressionSetter(columnMapping.getAttributeHandler().getValue()));//new ColumnExpressionSetter(columnMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false));
			}
			/*
			 * Flatfile have not tableAttributeHandler
			 */
			else if (this.productAttributeHandler != null ) {
				String cm = (columnMapping.getAttributeHandler() != null)? columnMapping.getAttributeHandler().getNameattr(): null;
				for( AttributeHandler ah : this.productAttributeHandler.values() ) {
					if( ah.getNameattr().equals(cm)) {
						this.extended_attributesSetter.put(att_ext_name,new ColumnExpressionSetter(ah.getNameattr(), ah));//new ColumnExpressionSetter( ah, ColumnSetMode.BY_KEYWORD, true, false));
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
		switch(this.spaceMappingPriority) {
		case ONLY :
			PriorityMessage.only("Coo system");
			this.mapCollectionCooSysAttributesFromMapping();
			break;
		case FIRST :
			PriorityMessage.first("Coo system");
			if( !this.mapCollectionCooSysAttributesFromMapping() ) {
				this.mapCollectionCooSysAttributesAuto();
			}			
			break;
		case LAST :
			PriorityMessage.last("Coo system");
			if( !this.mapCollectionCooSysAttributesAuto()) {
				this.mapCollectionCooSysAttributesFromMapping();
			}			
			else {
			}
			break;
		default:
			this.mapCollectionCooSysAttributesAuto();
		}
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
		if( this.astroframeSetter.isNotSet() &&  Messenger.debug_mode) {
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
				this.astroframeSetter = new ColumnExpressionSetter("astroframe");
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
					this.astroframeSetter = new ColumnExpressionSetter("astroframe");
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
			if( this.s_fovSetter.isNotSet() ){
				this.s_fovSetter = new ColumnExpressionSetter("s_fov");
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
		switch( this.spaceMappingPriority) {
		case ONLY:
			PriorityMessage.only("Pos error");
			this.mapCollectionPoserrorAttributesFromMapping();
			break;
		case FIRST:
			PriorityMessage.first("Pos error");
			if( !this.mapCollectionPoserrorAttributesFromMapping() ) {
				this.mapCollectionPoserrorAttributesAuto();
			}			
			break;
		case LAST:
			PriorityMessage.last("Pos error");
			if( !this.mapCollectionPoserrorAttributesAuto()) {
				this.mapCollectionPoserrorAttributesFromMapping();
			}	 else {
			}
			break;
		default: 
			this.mapCollectionPoserrorAttributesAuto();
		}

		/*
		 * Map errors on positions
		 */
		if( !this.s_resolutionSetter.isNotSet() ){
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
		default:
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

		this.x_unit_orgSetter   = this.getMappedAttributeHander("x_unit_org", mapping.getColumnMapping("x_unit_org"));
		this.em_res_powerSetter = this.getMappedAttributeHander("em_res_power", mapping.getColumnMapping("em_res_power"));
		this.em_binsSetter = this.getMappedAttributeHander("em_bins", mapping.getColumnMapping("em_bins"));
		this.em_minSetter = new ColumnExpressionSetter("em_min");
		this.em_maxSetter = new ColumnExpressionSetter("em_max");

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
					this.em_minSetter = new ColumnExpressionSetter("em_min");
					this.em_minSetter.setByValue(vals.get(0), true);
					this.em_maxSetter = new ColumnExpressionSetter("em_max");
					this.em_maxSetter.setByValue(vals.get(1), true);
				} else {
					Messenger.printMsg(Messenger.TRACE, "spectral coord. <" + sc_col.getValue() + "> can not be interptreted");						
					return;
				}
			} if( sc_col.byKeyword() ) {
				String col =  sc_col.getValue();
				Messenger.printMsg(Messenger.TRACE, "Take mapped column " + col + " as spectral dispersion");
				this.em_minSetter = new ColumnRowSetter("em_min", "Column.getMinValue(" + col + ")");
				this.em_maxSetter = new ColumnRowSetter("em_max", "Column.getAxValue(" + col + ")");
				this.em_binsSetter = new ColumnRowSetter("em_bins", "Column.getNbRows(" + col + ")");	
				if( this.x_unit_orgSetter.isNotSet()) {
					AttributeHandler ah ;
					if( (ah = this.dataFile.getEntryAttributeHandlerCopy().get(col) ) != null ) {
						this.x_unit_orgSetter = new ColumnExpressionSetter("x_unit_org", ah.getUnit());						
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
	private boolean mapCollectionPosAttributesAuto() throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Try to find the position in keywords");
		this.setQuantityDetector();
		if( this.quantityDetector.arePosColFound() ) {
			if( this.astroframeSetter.isNotSet() ){
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
			this.s_raSetter = new ColumnExpressionSetter("s_ra");
			this.s_decSetter = new ColumnExpressionSetter("s_dec");		
			this.s_fovSetter = new ColumnExpressionSetter("s_fov");
			this.s_regionSetter = new ColumnExpressionSetter("s_region");
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		}
	}

	/**
	 * @throws Exception 
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPosAttributesFromMapping() throws Exception {
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
			//this.s_raSetter = new ColumnExpressionSetter(raMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);
			this.s_raSetter = new ColumnExpressionSetter("s_ra", raMapping.getAttributeHandler().getValue());
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Right Ascension set with the constant value <" +raMapping.getAttributeHandler().getValue() + ">");
			ra_found = true;
		} else {
			this.s_raSetter = new ColumnExpressionSetter("s_ra");
		}
		if( decMapping.byValue() ) {
			//this.s_decSetter = new ColumnExpressionSetter(decMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);	
			this.s_decSetter = new ColumnExpressionSetter("s_ra", decMapping.getAttributeHandler().getValue());	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Declination set with the constant value <" + decMapping.getAttributeHandler().getValue() + ">");
			dec_found = true;
		} else {
			this.s_decSetter = new ColumnExpressionSetter("s_dec");
		}
		this.s_regionSetter = getMappedAttributeHander("s_region", this.mapping.getSpaceAxisMapping().getColumnMapping("s_region"));
		this.s_fovSetter = getMappedAttributeHander("s_fov", this.mapping.getSpaceAxisMapping().getColumnMapping("s_fov"));

		/*
		 * Look for attributes mapping the position parameters without constant values
		 */
		if( !(ra_found && dec_found) ) {
			//this.s_raSetter=getMappedAttributeHander(this.mapping.getSpaceAxisMapping().getColumnMapping("s_ra"));
			String raCol =  (raMapping.getAttributeHandler() != null)? raMapping.getAttributeHandler().getNameorg() : null;
			String decCol =  (decMapping.getAttributeHandler() != null)? decMapping.getAttributeHandler().getNameorg() : null;
			for( AttributeHandler ah: this.productAttributeHandler.values()) {
				String keyorg  = ah.getNameorg();
				String keyattr = ah.getNameattr();
				if( this.s_raSetter.isNotSet() && (keyorg.equals(raCol) || keyattr.equals(raCol)) ) {
					//this.s_raSetter = new ColumnExpressionSetter(ah,  ColumnSetMode.BY_KEYWORD, true, false);
					this.s_raSetter = new ColumnExpressionSetter("s_ra", ah);
					ra_found = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as right ascension");
				}
				if( this.s_decSetter.isNotSet() && (keyorg.equals(decCol) || keyattr.equals(decCol)) ) {
					//this.s_decSetter = new ColumnExpressionSetter(ah,  ColumnSetMode.BY_KEYWORD, true, false);;
					this.s_decSetter = new ColumnExpressionSetter("s_dec", ah);

					dec_found = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as declination");
				}
			}

			if( this.astroframeSetter.isNotSet() && (ra_found && dec_found) ) {
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

		if (this.s_resolutionSetter.isNotSet() ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Failed");
			return false;
		} else if( !SpatialResolutionUnitRef.isUnitValid(this.s_resolutionSetter.getUnit())) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getUnit() + " is not a valid unit for the spatial resolution");		
			this.s_resolutionSetter = new ColumnExpressionSetter("s_resolution");
			return false;
		} else {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Position error OK");
			return true;
		} 
	}

	/**
	 * @throws Exception 
	 * @throws FatalException 
	 * 
	 */
	private boolean mapCollectionPoserrorAttributesFromMapping() throws Exception {
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
			//this.s_resolutionSetter = new ColumnExpressionSetter(sResolutionMapping.getAttributeHandler(), ColumnSetMode.BY_VALUE, true, false);
			this.s_resolutionSetter = new ColumnExpressionSetter("s_resolution", sResolutionMapping.getAttributeHandler().getValue());
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
					//this.s_resolutionSetter = new ColumnExpressionSetter(ah, ColumnSetMode.BY_KEYWORD, true, false);
					this.s_resolutionSetter = new ColumnExpressionSetter("s_resolution", ah);
					this.s_resolutionSetter.completeMessage("unitOrg: " + rUnit);
					errMaj = true;
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Key word <" + ah.getNameorg() + "> taken as error Maj axis");
				}
			}
		}
		if( !SpatialResolutionUnitRef.isUnitValid(this.s_resolutionSetter.getUnit())) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getUnit() + " is not a valid unit for the spatial resolution");		
			this.s_resolutionSetter = new ColumnExpressionSetter("s_resolution");
			return false;
		} else if (errMaj ) {
			try {
				Double.parseDouble(this.s_resolutionSetter.getValue());
			} catch (Exception e) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, this.s_resolutionSetter.getValue() + " is not a valid value for the spatial resolution");		
				this.s_resolutionSetter = new ColumnExpressionSetter("s_resolution");
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
	 * @param metaClass
	 * @throws IgnoreException if mc is null
	 */
	public void setMetaclass(MetaClass metaClass) throws IgnoreException {
		if( metaClass == null ){
			IgnoreException.throwNewException(SaadaException.WRONG_PARAMETER, "Attempt to set the builder with a null data class");
		}
		this.metaClass = metaClass;
	}
	/**
	 * @return Returns the metaclass.
	 */
	public MetaClass getMetaclass() {
		return this.metaClass;
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
	public void mergeProductFormat(DataFile file_to_merge) throws Exception {
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Merge format with file <" + file_to_merge.getName() + ">");

		/*
		 * Build a new set of attribute handlers from the product given as a parameter
		 */
		ProductBuilder prd_to_merge = this.mapping.getNewProductBuilderInstance(file_to_merge, this.metaClass);
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
	private void traceReportOnAttRef(ColumnSetter att){		
		Messenger.printMsg(Messenger.TRACE, att.toString());	
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


}
