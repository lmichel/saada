package saadadb.products;

import hecds.LibLog;
import hecds.wcs.Modeler;
import hecds.wcs.descriptors.CardDescriptor;
import hecds.wcs.descriptors.CardMap;
import hecds.wcs.transformations.Projection;
import hecds.wcs.types.AxeType;
import hecds.wcs.types.CardFilters;

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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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
import saadadb.products.datafile.DataFile;
import saadadb.products.datafile.FitsDataFile;
import saadadb.products.datafile.VOTableDataFile;
import saadadb.products.inference.Image2DCoordinate;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.products.setter.ColumnRowSetter;
import saadadb.products.setter.ColumnSetter;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
import saadadb.util.MessengerLogger;
import saadadb.util.SaadaConstant;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.PriorityMode;

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
//	public ColumnSetter s_resolution_unitSetter=new ColumnExpressionSetter("s_resolution_unit");
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
	public ColumnSetter em_unitSetter=new ColumnExpressionSetter("em_unit");
	public PriorityMode energyMappingPriority = PriorityMode.LAST;
	/*
	 * Time Axis
	 */
	public ColumnSetter t_minSetter=new ColumnExpressionSetter("t_min");
	public ColumnSetter t_maxSetter=new ColumnExpressionSetter("t_max");
	public ColumnSetter t_exptimeSetter=new ColumnExpressionSetter("t_exptime");
	public ColumnSetter t_resolutionSetter=new ColumnExpressionSetter("t_resolution");
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


	//	protected ColumnSetter system_attribute;
	//	protected ColumnSetter equinox_attribute;
	//	protected ColumnSetter astroframeSetter;

	/** The file type ("FITS" or "VO") * */
	public String typeFile;
	public MetaClass metaClass;
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
		this.observationMappingPriority  = conf.getObservationAxisMapping().getPriority();
		this.spaceMappingPriority        = conf.getSpaceAxisMapping().getPriority();
		this.energyMappingPriority       = conf.getEnergyAxisMapping().getPriority();
		this.timeMappingPriority         = conf.getTimeAxisMapping().getPriority();
		this.observableMappingPriority   = conf.getObservableAxisMapping().getPriority();
		this.polarizationMappingPriority = conf.getPolarizationAxisMapping().getPriority();
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
			this.completeEnergyWCS(cm);	
			this.quantityDetector = new QuantityDetector(this.productAttributeHandler, null, this.mapping, this.wcsModeler);
		}
	}

	/**
	 * This is an experimental feature aiming at extracting missing WCS keywords from the user mapping parameters
	 * 
	 * @param cardMap cardMap sent to the WCS lib and possibly extended with synthetic keywords
	 * @throws Exception
	 */
	private void completeEnergyWCS(CardMap cardMap ) throws Exception {
		Projection projection = this.wcsModeler.getProjection(AxeType.SPECTRAL);
		if( !projection.isUsable() ) {
			ColumnMapping columnMapping;
			for( int axeNum=1 ; axeNum<=4 ; axeNum++){
				CardDescriptor fb = cardMap.get("CTYPE" + axeNum);
				if( fb != null && fb.getValue().matches(CardFilters.SPECTRAL_CTYPE)){
					String unitWK = "CUNIT" +	axeNum;
					if( cardMap.get(unitWK) == null 
							&& (columnMapping = this.mapping.getEnergyAxisMapping( ).getColumnMapping("em_unit")) != null 
							&& columnMapping.byValue()) {
						AttributeHandler ah = new AttributeHandler();
						ah.setNameattr("_" + unitWK.toLowerCase());
						ah.setNameorg(unitWK);
						ah.setType("String");
						ah.setValue(columnMapping.getValue());
						cardMap.put(ah);
						Messenger.printMsg(Messenger.TRACE, "Add user defined spectral unit (" + columnMapping.getValue() + ") to the WCS projection (axe #" + axeNum + ")");
						System.out.println(projection);
						this.wcsModeler = new Modeler(cardMap);	
					}
				}
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
	public abstract int getCategory();

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
		Messenger.locateCode();
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
		Messenger.locateCode();
		this.dataFile = dataFile;
		this.dataFile.bindBuilder(this);
		Messenger.printMsg(Messenger.TRACE, this.getClass().getName() + " map the data file " + this.getName());
		//	this.bindDataFile(dataFile);
		this.mapCollectionAttributes();
	}

	/**
	 * @throws Exception
	 */
	public void mapDataFile() throws Exception{
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, this.getClass().getName() + " map the data file " + this.getName());
		this.mapCollectionAttributes();
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
		Messenger.locateCode();
		this.wcsModeler.updateValues();
		this.obs_idSetter.calculateExpression();
		this.obs_collectionSetter.calculateExpression();
		this.obs_publisher_didSetter.calculateExpression();
		this.calib_levelSetter.calculateExpression();
		this.target_nameSetter.calculateExpression();
		this.facility_nameSetter.calculateExpression();
		this.instrument_nameSetter.calculateExpression();
		this.astroframeSetter.calculateExpression();
		if( this instanceof EntryBuilder ){
			String s = "";
			//			System.out.println("CHECK " + Integer.toHexString(this.productAttributeHandler.get("_e_d25").hashCode()) + " " + this.productAttributeHandler.get("_e_d25"));
			//			for( AttributeHandler ah: this.s_resolutionSetter.getExprAttributes()) 
			//				System.out.println(Integer.toHexString(ah.hashCode()) + " " +  ah);
		}
		this.s_resolutionSetter.calculateExpression();
		//this.s_resolution_unitSetter.calculateExpression();
		this.s_raSetter.calculateExpression();
		this.s_decSetter.calculateExpression();
		this.s_fovSetter.calculateExpression();
		this.s_regionSetter.calculateExpression();
		this.em_minSetter.calculateExpression(this.dataFile);
		this.em_maxSetter.calculateExpression(this.dataFile);
		this.em_binsSetter.calculateExpression(this.dataFile);
		this.em_res_powerSetter.calculateExpression(this.dataFile);
		this.em_unitSetter.calculateExpression(this.dataFile);
		this.t_minSetter.calculateExpression(this.dataFile);
		this.t_maxSetter.calculateExpression(this.dataFile);
		this.t_exptimeSetter.calculateExpression(this.dataFile);
		this.t_resolutionSetter.calculateExpression(this.dataFile);
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
		Messenger.locateCode();
		this.dataFile.updateAttributeHandlerValues();
		this.productIngestor.bindInstanceToFile();
		this.productIngestor.loadValue();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Processing file <" + this.getName() + "> complete");
	}

	/**
	 * Stores the saada instance as a row in an ASCII file used later to lot a bunch of product in one shot
	 * Check the uniqueness (with a warning) of the product in debug mode. 
	 * @param colwriter : file where are store collection level attributes
	 * @param buswfriter: file where are store class level attributes
	 * @throws Exception
	 */
	public void loadProduct(BufferedWriter colwriter, BufferedWriter buswriter, BufferedWriter loadedfilewriter) throws Exception  {
		Messenger.locateCode();
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
	/**
	 * Build a {@link ColumnExpressionSetter} from the user {@link ColumnMapping}. If there is no user mapping for the column, 
	 * an free ColumnExpressionSetter is returned
	 * @param colmunName  Name of the column mapping
	 * @param columnMapping Object returned by user mapping processing
	 * @return a new ColumnExpressionSetter
	 * @throws Exception
	 */
	protected ColumnExpressionSetter getSetterForMappedColumn(String colmunName, ColumnMapping columnMapping) throws Exception {
		ColumnExpressionSetter retour ;
		/*
		 * No user mapping: return a free  ColumnExpressionSetter
		 */
		if ( columnMapping.notMapped())	{
			retour = new ColumnExpressionSetter(colmunName);
			retour.completeUserMappingMsg("-");
			return retour;
			/*
			 * User mapping by value: return a "constant" ColumnExpressionSetter
			 * (no reference to any AH) 
			 */
		} else if( columnMapping.byValue() ){
			retour = new ColumnExpressionSetter(colmunName, columnMapping.getValue());
			retour.setUnit(columnMapping.getUnit());
			retour.completeUserMappingMsg( columnMapping.getMessage() + " By " + columnMapping.getMode() + "=" + columnMapping.getValue() + columnMapping.getUnit() + " " );
			return retour;
			/*
			 * Mapped either by expression or keyword: use keywords which must be retrieved with the product AHS
			 */
		} else {
			/*
			 * Checks the consistency of the AH lits referenced by the mapping
			 */
			Set<AttributeHandler> handlersReferencedByMapping = columnMapping.getHandlers();
			if( handlersReferencedByMapping == null || handlersReferencedByMapping.size() == 0 ){
				retour = new ColumnExpressionSetter(colmunName);
				retour.completeUserMappingMsg("Column " + colmunName  + " = " + columnMapping.getExpression() + " mapped by keyword or expression must reference at least one attribute");
				return retour;
//
//				FatalException.throwNewException(SaadaException.INTERNAL_ERROR
//						, "Column " + columnMapping + " mapped by keyword or expression must reference at least one attribute");
			}
			/*
			 * By keyword: just one AH must be used
			 */
			if( columnMapping.byKeyword() ) {
				if(  handlersReferencedByMapping.size() != 1){
					retour = new ColumnExpressionSetter(colmunName);
					retour.completeUserMappingMsg("Column " + colmunName  + " = " + columnMapping.getExpression() + " mapped by keyword: must reference one attribute");
//
//					FatalException.throwNewException(SaadaException.INTERNAL_ERROR
//							, "Column " + colmunName + " mapped by keyword: must reference one attribute");
				}
				String ahname=null;
				AttributeHandler mappingSingleHandler=null;			
				/*
				 * Take the first AH referenced by the mapping
				 */
				for( AttributeHandler ah: handlersReferencedByMapping ){
					ahname = ah.getNameattr();
					break;
				}
				/*
				 * Identify this AH among those of the builder
				 */
				for( AttributeHandler ah: this.productAttributeHandler.values() ){
					if( ah.isNamedLike(ahname)) {
						mappingSingleHandler = ah;
						break;
					}
				}
				if( mappingSingleHandler == null ){
					//FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Attribute " + ahname + " used to map the column " + colmunName + " does not exist");					
					retour = new ColumnExpressionSetter(colmunName);
					retour.completeUserMappingMsg("Attribute <" + ahname + "> used to map the column " + colmunName + " does not exist");
					return retour;
				}
				/*
				 * Build a ColumnExpressionSetter using tehe builder AH
				 */
				retour = new ColumnExpressionSetter(colmunName, mappingSingleHandler);
				retour.completeUserMappingMsg(columnMapping.getMessage() + "By " + columnMapping.getMode() + " " + ahname);
				return retour;
				/*
				 * By expression: multiple AHs are used
				 */
			} else if( columnMapping.byExpression() ){
				Map<String,AttributeHandler> handlersUsedByMapping=new LinkedHashMap<String, AttributeHandler>();
				String ahname=null;
				String missingAhs = "";
				/*
				 * identify all AHs referenced by the mapping among those of the builder
				 * All AHs referenced by the mapping are checked in any case in order to get a complete list of missing attributes 
				 */
				for( AttributeHandler ah: handlersReferencedByMapping ){
					ahname = ah.getNameattr();
					boolean found = false;
					for( AttributeHandler builderAh: this.productAttributeHandler.values() ){
						if( builderAh.isNamedLike(ahname)) {
							handlersUsedByMapping.put(ahname, builderAh);
							found = true;
							break;
						}
					}
					if( !found ) {
						missingAhs += ah.getNameorg() + " ";						
					}
				}
				if(missingAhs.length() > 0  ){
					retour = new ColumnExpressionSetter(colmunName);
					retour.completeUserMappingMsg("Attributes [" + missingAhs + "] used to map the column " + colmunName + " are missing");
					return retour;

					//FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Attributes [" + missingAhs + "] used to map the column " + colmunName + " are missing");										
				} else {
					retour = new ColumnExpressionSetter(colmunName, columnMapping.getExpression(), handlersUsedByMapping, true);					
					retour.completeUserMappingMsg(columnMapping.getMessage() + "By " + columnMapping.getMode());
					return retour;
				}
			} else {
				//FatalException.throwNewException(SaadaException.INTERNAL_ERROR, "Column mapping " + columnMapping + " not understood");		
				retour = new ColumnExpressionSetter(colmunName);
				retour.completeUserMappingMsg("Column mapping " + columnMapping + " not understood");
				return retour;
			}
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
		Messenger.locateCode();
		if( this.productAttributeHandler != null ) {
			Messenger.printMsg(Messenger.TRACE, "Mapping collection attribute (" +  this.getClass().getName() + ")" + this.productAttributeHandler.size());
			this.mapObservationAxe();
			this.mapSpaceAxe();
			this.mapEnergyAxe();
			this.mapTimeAxe();
			//System.exit(1);
			this.mapObservableAxe();
			this.mapPolarizationAxe();
			this.mapIgnoredAndExtendedAttributes();
		}
	}

	/**
	 * @throws Exception
	 */
	protected void mapObservationAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Observation Axis");
		AxisMapping mapping = this.mapping.getObservationAxisMapping();
		this.setQuantityDetector();
		this.mapInstanceName();
		String message;

		switch(this.observationMappingPriority){
		case ONLY:	
			PriorityMessage.only("Observation");
			this.obs_collectionSetter = getSetterForMappedColumn("obs_collection", mapping.getColumnMapping("obs_collection"));
			this.obs_publisher_didSetter = getSetterForMappedColumn("obs_publisher_did", mapping.getColumnMapping("obs_publisher_did"));
			this.calib_levelSetter = getSetterForMappedColumn("calib_level", mapping.getColumnMapping("calib_level"));
			this.target_nameSetter = getSetterForMappedColumn("target_name", mapping.getColumnMapping("target_name"));
			this.facility_nameSetter = getSetterForMappedColumn("facility_name", mapping.getColumnMapping("facility_name"));
			this.instrument_nameSetter = getSetterForMappedColumn("instrument_name", mapping.getColumnMapping("instrument_name"));
			break;

		case FIRST:
			PriorityMessage.first("Observation");
			this.obs_collectionSetter = getSetterForMappedColumn("obs_collection", mapping.getColumnMapping("obs_collection"));
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				message = this.obs_collectionSetter.getUserMappingMsg();
				this.obs_collectionSetter = this.quantityDetector.getCollectionName();
				this.obs_collectionSetter.completeUserMappingMsg(message);
			} 
			this.obs_publisher_didSetter = getSetterForMappedColumn("obs_publisher_did", mapping.getColumnMapping("obs_publisher_did"));
			if( !this.isAttributeHandlerMapped(this.obs_publisher_didSetter) ) {
				message = this.obs_publisher_didSetter.getUserMappingMsg();
				this.obs_publisher_didSetter = this.quantityDetector.getObsPublisherDid();
				this.obs_publisher_didSetter.completeUserMappingMsg(message);
			} 
			this.calib_levelSetter = getSetterForMappedColumn("calib_level", mapping.getColumnMapping("calib_level"));
			if( !this.isAttributeHandlerMapped(this.obs_publisher_didSetter) ) {
				message = this.calib_levelSetter.getUserMappingMsg();
				this.calib_levelSetter = this.quantityDetector.getCalibLevel();
				this.calib_levelSetter.completeUserMappingMsg(message);
			} 
			this.target_nameSetter = getSetterForMappedColumn("target_name", mapping.getColumnMapping("target_name"));
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				message = this.target_nameSetter.getUserMappingMsg();
				this.target_nameSetter = this.quantityDetector.getTargetName();
				this.target_nameSetter.completeUserMappingMsg(message);
			}
			this.facility_nameSetter = getSetterForMappedColumn("facility_name", mapping.getColumnMapping("facility_name"));
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				message = this.facility_nameSetter.getUserMappingMsg();
				this.facility_nameSetter = this.quantityDetector.getFacilityName();
				this.facility_nameSetter.completeUserMappingMsg(message);
			}
			this.instrument_nameSetter = getSetterForMappedColumn("instrument_name", mapping.getColumnMapping("instrument_name"));
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				message = this.instrument_nameSetter.getUserMappingMsg();
				this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
				this.instrument_nameSetter.completeUserMappingMsg(message);
			}
			break;
		case LAST:
			PriorityMessage.last("Observation");
			this.obs_publisher_didSetter = this.quantityDetector.getObsPublisherDid();
			if( !this.isAttributeHandlerMapped(this.obs_publisher_didSetter) ) {
				message = this.obs_publisher_didSetter.getDetectionMsg();
				this.obs_publisher_didSetter = getSetterForMappedColumn("obs_publisher_did", mapping.getColumnMapping("obs_publisher_did"));
				this.obs_publisher_didSetter.completeDetectionMsg(message);
			}
			this.obs_collectionSetter = this.quantityDetector.getCollectionName();
			if( !this.isAttributeHandlerMapped(this.obs_collectionSetter) ) {
				message = this.obs_collectionSetter.getDetectionMsg();
				this.obs_collectionSetter = getSetterForMappedColumn("obs_collection", mapping.getColumnMapping("obs_collection"));
				this.obs_collectionSetter.completeDetectionMsg(message);
			}
			this.target_nameSetter = this.quantityDetector.getTargetName();
			if( !this.isAttributeHandlerMapped(this.target_nameSetter) ) {
				message = this.target_nameSetter.getDetectionMsg();
				this.target_nameSetter = getSetterForMappedColumn("target_name", mapping.getColumnMapping("target_name"));
				this.target_nameSetter.completeDetectionMsg(message);
			}
			this.facility_nameSetter = this.quantityDetector.getFacilityName();
			if( !this.isAttributeHandlerMapped(this.facility_nameSetter) ) {
				message = this.facility_nameSetter.getDetectionMsg();
				this.facility_nameSetter = getSetterForMappedColumn("facility_name", mapping.getColumnMapping("facility_name"));
				this.facility_nameSetter.completeDetectionMsg(message);
			}
			this.instrument_nameSetter = this.quantityDetector.getInstrumentName();
			if( !this.isAttributeHandlerMapped(this.instrument_nameSetter) ) {
				message = this.instrument_nameSetter.getDetectionMsg();
				this.instrument_nameSetter = getSetterForMappedColumn("instrument_name", mapping.getColumnMapping("instrument_name"));
				this.instrument_nameSetter.completeDetectionMsg(message);
			}
			break;
		}	
		/*
		 * Take the Saada collection as default obs_collection
		 */
		if( this.obs_collectionSetter.isNotSet() ){
			this.obs_collectionSetter = new ColumnExpressionSetter(this.mapping.getCollection() + "_" + Category.explain(this.mapping.getCategory()));
			this.obs_collectionSetter.completeDetectionMsg("Derived from Saada collection name");
		}
		traceReportOnAttRef(this.obs_idSetter);
		traceReportOnAttRef(this.obs_publisher_didSetter);
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
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Building the name");
		/*
		 * Uses the config first
		 */
		this.obs_idSetter = this.getSetterForMappedColumn("obs_id", mapping.getObservationAxisMapping().getColumnMapping("obs_id"));

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
			String message = this.obs_idSetter .getUserMappingMsg();
			this.obs_idSetter = new ColumnExpressionSetter("obs_id", expression);
			this.obs_idSetter.completeDetectionMsg(message);
		}
		this.traceReportOnAttRef(this.obs_idSetter);
	}

	/**
	 * @throws Exception
	 */
	protected void mapEnergyAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Energy Axe");
		this.setQuantityDetector();
		String message;
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
				message = t_maxSetter.getUserMappingMsg();
				this.em_minSetter = this.quantityDetector.getEMin();
				this.em_minSetter.completeUserMappingMsg(message);
				message = t_maxSetter.getUserMappingMsg();
				this.em_maxSetter = this.quantityDetector.getEMax();				
				this.em_maxSetter.completeUserMappingMsg(message);
			}
			if( this.em_unitSetter.isNotSet()  ) {
				message = em_unitSetter.getUserMappingMsg();
				this.em_unitSetter = this.quantityDetector.getEUnit();
				this.em_unitSetter.completeUserMappingMsg(message);
			}
			if( this.em_res_powerSetter.isNotSet()  ) {
				message = em_res_powerSetter.getUserMappingMsg();
				this.em_res_powerSetter = this.quantityDetector.getResPower();
				this.em_res_powerSetter = this.quantityDetector.getEUnit();
			}
			if( this.em_binsSetter.isNotSet()  ) {
				message = em_binsSetter.getUserMappingMsg();
				this.em_binsSetter = this.quantityDetector.getEbins();
				this.em_binsSetter = this.quantityDetector.getEUnit();
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
				this.em_minSetter = this.getSetterForMappedColumn("em_min", mapping.getColumnMapping("em_min"));
				this.em_minSetter.completeDetectionMsg(qdMin);
				this.em_maxSetter = this.getSetterForMappedColumn("em_max", mapping.getColumnMapping("em_max"));			
				this.em_maxSetter.completeDetectionMsg(qdMax);
			} else {
				this.em_minSetter = qdMin;
				this.em_maxSetter = qdMax;				
			}
			if( qdUnit.isNotSet()  ) {
				this.em_unitSetter = this.getSetterForMappedColumn("em_unit", mapping.getColumnMapping("em_unit"));
				this.em_unitSetter.completeDetectionMsg(qdUnit);
			} else {
				this.em_unitSetter = qdUnit;
			}
			if( qdRPow.isNotSet()  ) {
				this.em_res_powerSetter = this.getSetterForMappedColumn("em_respower", mapping.getColumnMapping("em_res_power"));
				this.em_res_powerSetter.completeDetectionMsg(qdRPow);
			} else {
				this.em_res_powerSetter = qdRPow;
			}
			if( qdBins.isNotSet()  ) {
				this.em_binsSetter = this.getSetterForMappedColumn("em_bins", mapping.getColumnMapping("em_bins"));
				this.em_binsSetter.completeDetectionMsg(qdBins);
			} else {
				this.em_binsSetter = qdBins;
			}
		}
		this.traceReportOnAttRef(this.em_unitSetter);
		this.traceReportOnAttRef(this.em_minSetter);
		this.traceReportOnAttRef(this.em_maxSetter);
		this.traceReportOnAttRef(this.em_res_powerSetter);
	}

	/**
	 * @throws Exception
	 */
	protected void mapTimeAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Time Axe");
		AxisMapping mapping = this.mapping.getTimeAxisMapping();
		String message;
		this.setQuantityDetector();

		switch(this.timeMappingPriority){
		case ONLY:			
			PriorityMessage.only("Time");
			this.t_maxSetter = this.getSetterForMappedColumn("t_max", mapping.getColumnMapping("t_max"));
			this.t_minSetter = this.getSetterForMappedColumn("t_min", mapping.getColumnMapping("t_min"));
			this.t_exptimeSetter = this.getSetterForMappedColumn("t_exptime", mapping.getColumnMapping("t_exptime"));
			this.t_resolutionSetter = this.getSetterForMappedColumn("t_resolution", mapping.getColumnMapping("t_resolution"));
			break;

		case FIRST:
			PriorityMessage.first("Time");
			this.t_maxSetter = this.getSetterForMappedColumn("t_max", mapping.getColumnMapping("t_max"));
			if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				message = t_maxSetter.getUserMappingMsg();
				this.t_maxSetter = this.quantityDetector.getTMax();
				this.t_maxSetter.completeUserMappingMsg(message);
			}
			this.t_minSetter = getSetterForMappedColumn("t_min", mapping.getColumnMapping("t_min"));
			if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				message = t_minSetter.getUserMappingMsg();
				this.t_minSetter = this.quantityDetector.getTMin();
				this.t_minSetter.completeUserMappingMsg(message);
			}
			this.t_exptimeSetter = getSetterForMappedColumn("t_exptime", mapping.getColumnMapping("t_exptime"));
			if( !this.isAttributeHandlerMapped(this.t_exptimeSetter) ) {
				message = t_exptimeSetter.getUserMappingMsg();
				this.t_exptimeSetter = this.quantityDetector.getExpTime();
				this.t_exptimeSetter.completeUserMappingMsg(message);
			}
			this.t_resolutionSetter = getSetterForMappedColumn("t_resolution", mapping.getColumnMapping("t_resolution"));
			if( !this.isAttributeHandlerMapped(this.t_resolutionSetter) ) {
				message = t_resolutionSetter.getUserMappingMsg();
				this.t_resolutionSetter = this.quantityDetector.getTResolution();
				this.t_resolutionSetter.completeUserMappingMsg(message);
			}
			break;

		case LAST:
			PriorityMessage.last("Time");
			ColumnExpressionSetter cs = this.quantityDetector.getTMax();
			if( cs.isSet() ){
				this.t_maxSetter = cs;
			} else 	if( !this.isAttributeHandlerMapped(this.t_maxSetter) ) {
				this.t_maxSetter = getSetterForMappedColumn("t_max", mapping.getColumnMapping("t_max"));
				this.t_maxSetter.completeDetectionMsg(cs);
			}
			cs = this.quantityDetector.getTMin();
			if( cs.isSet() ){
				this.t_minSetter = cs;
			} else if( !this.isAttributeHandlerMapped(this.t_minSetter)) {
				this.t_minSetter = getSetterForMappedColumn("t_min", mapping.getColumnMapping("t_min"));
				this.t_minSetter.completeDetectionMsg(cs);
			}
			cs = this.quantityDetector.getExpTime();
			if( cs.isSet() ){
				this.t_exptimeSetter = cs;
			} else if( !this.isAttributeHandlerMapped(this.t_exptimeSetter)) {
				this.t_exptimeSetter = getSetterForMappedColumn("t_exptime", mapping.getColumnMapping("t_exptime"));
				this.t_exptimeSetter.completeDetectionMsg(cs);
			}
			cs = this.quantityDetector.getTResolution();
			if( cs.isSet() ){
				this.t_resolutionSetter = cs;
			} else if( !this.isAttributeHandlerMapped(this.t_resolutionSetter)) {
				this.t_resolutionSetter = getSetterForMappedColumn("t_resolution", mapping.getColumnMapping("t_resolution"));
				this.t_resolutionSetter.completeDetectionMsg(cs);
			}
			break;
		}
		traceReportOnAttRef(this.t_minSetter);
		traceReportOnAttRef(this.t_maxSetter);
		traceReportOnAttRef(this.t_exptimeSetter);
		traceReportOnAttRef(this.t_resolutionSetter);
	}

	protected void mapObservableAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Observable Axe");
		AxisMapping mapping = this.mapping.getObservableAxisMapping();
		this.setQuantityDetector();

		switch(this.observableMappingPriority){
		case ONLY:			
			PriorityMessage.only("Observable");
			this.o_ucdSetter = this.getSetterForMappedColumn("o_ucd", mapping.getColumnMapping("o_ucd"));
			this.o_unitSetter = this.getSetterForMappedColumn("o_unit", mapping.getColumnMapping("o_unit"));
			this.o_calib_statusSetter = this.getSetterForMappedColumn("o_calib_status", mapping.getColumnMapping("o_calib_status"));
			break;

		case FIRST:
			PriorityMessage.first("Observable");
			this.o_ucdSetter = this.getSetterForMappedColumn("o_ucd", mapping.getColumnMapping("o_ucd"));
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				String msg = o_ucdSetter.getUserMappingMsg();
				this.o_ucdSetter = this.quantityDetector.getObservableUcd();
				this.o_ucdSetter.completeUserMappingMsg(msg);
			}
			this.o_unitSetter = getSetterForMappedColumn("o_unit", mapping.getColumnMapping("o_unit"));
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				String msg = o_unitSetter.getUserMappingMsg();
				this.o_unitSetter = this.quantityDetector.getObservableUnit();
				this.o_unitSetter.completeUserMappingMsg(msg);
			}
			this.o_calib_statusSetter = getSetterForMappedColumn("o_calib_status", mapping.getColumnMapping("o_calib_status"));
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				String msg = o_calib_statusSetter.getUserMappingMsg();
				this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
				this.o_calib_statusSetter.completeUserMappingMsg(msg);
			}
			break;

		case LAST:
			PriorityMessage.last("Observable");
			this.o_ucdSetter = this.quantityDetector.getObservableUcd();
			if( !this.isAttributeHandlerMapped(this.o_ucdSetter) ) {
				String msg = o_ucdSetter.getDetectionMsg();
				this.o_ucdSetter = this.getSetterForMappedColumn("o_ucd", mapping.getColumnMapping("o_ucd"));
				this.o_ucdSetter.completeDetectionMsg(msg);
			}
			this.o_unitSetter = this.quantityDetector.getObservableUnit();
			if( !this.isAttributeHandlerMapped(this.o_unitSetter)) {
				String msg = o_unitSetter.getDetectionMsg();
				this.o_unitSetter = getSetterForMappedColumn("o_unit", mapping.getColumnMapping("o_unit"));
				this.o_unitSetter.completeDetectionMsg(msg);
			}
			this.o_calib_statusSetter = this.quantityDetector.getCalibStatus();
			if( !this.isAttributeHandlerMapped(this.o_calib_statusSetter) ) {
				String msg = o_calib_statusSetter.getDetectionMsg();
				this.o_calib_statusSetter = getSetterForMappedColumn("o_calib_status", mapping.getColumnMapping("o_calib_status"));
				this.o_calib_statusSetter.completeDetectionMsg(msg);
			}
			break;
		}

		traceReportOnAttRef( this.o_ucdSetter);
		traceReportOnAttRef(this.o_unitSetter);
		traceReportOnAttRef(this.o_calib_statusSetter);
	}


	protected void mapPolarizationAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Polarization Axe");
		AxisMapping mapping = this.mapping.getPolarizationAxisMapping();
		this.setQuantityDetector();

		switch(this.polarizationMappingPriority){
		case ONLY:			
			PriorityMessage.only("Polarization");
			this.pol_statesSetter = this.getSetterForMappedColumn("pol_states", mapping.getColumnMapping("pol_states"));
			break;

		case FIRST:
			PriorityMessage.first("Polarization");
			this.pol_statesSetter = this.getSetterForMappedColumn("pol_states", mapping.getColumnMapping("pol_states"));
			if( !this.isAttributeHandlerMapped(this.pol_statesSetter) ) {
				String msg = pol_statesSetter.getUserMappingMsg();
				this.pol_statesSetter = this.quantityDetector.getPolarizationStates();
				this.pol_statesSetter.completeUserMappingMsg(msg);
			}
			break;

		case LAST:
			PriorityMessage.last("Polarization");
			this.pol_statesSetter = this.quantityDetector.getPolarizationStates();
			if( !this.isAttributeHandlerMapped(this.pol_statesSetter) ) {
				String msg = pol_statesSetter.getDetectionMsg();
				this.pol_statesSetter = this.getSetterForMappedColumn("pol_states", mapping.getColumnMapping("pol_states"));
				this.pol_statesSetter.completeDetectionMsg(msg);
			}
			break;
		}

		traceReportOnAttRef( this.pol_statesSetter);
	}

	/**
	 * @throws Exception
	 */
	protected void mapSpaceAxe() throws Exception {
		Messenger.locateCode();
		Messenger.printMsg(Messenger.TRACE, "Map Space Axe");
		/*
		 * Coo sys done in 2nd: can use position mapping to detect the coord system
		 */
		this.mapCollectionCooSysAttributes();
		/*
		 * Error must be mapped first because it is used by the fov with is processed in  
		 */
		this.mapCollectionPoserrorAttributes();
		this.mapCollectionPosAttributes();
		traceReportOnAttRef(astroframeSetter);
		traceReportOnAttRef(s_raSetter);
		traceReportOnAttRef(s_decSetter);
		traceReportOnAttRef(s_regionSetter);
		traceReportOnAttRef(s_fovSetter);
		traceReportOnAttRef(s_resolutionSetter);
	//	traceReportOnAttRef(s_resolution_unitSetter);
	}

	/**
	 * @throws Exception 
	 * @throws FatalException 
	 * 
	 */
	public void mapIgnoredAndExtendedAttributes () throws Exception {
		Messenger.locateCode();

		Map<String , AttributeHandler> eatt = Database.getCachemeta().getAtt_extend(this.getCategory());
		for( Entry<String , AttributeHandler> ea: eatt.entrySet()){
			this.extended_attributesSetter.put(ea.getKey(), this.getSetterForMappedColumn(ea.getKey(),this.mapping.getExtenedAttMapping().getColumnMapping(ea.getKey())));
		}
	}


	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionCooSysAttributes() throws Exception {
		Messenger.locateCode();
		AxisMapping mapping = this.mapping.getSpaceAxisMapping();
		this.setQuantityDetector();

		switch(this.spaceMappingPriority){
		case ONLY:			
			PriorityMessage.only("Coosys");
			this.astroframeSetter = this.getSetterForMappedColumn("system", mapping.getColumnMapping("system"));
			break;

		case FIRST:
			PriorityMessage.first("Coosys");
			this.astroframeSetter = this.getSetterForMappedColumn("system", mapping.getColumnMapping("system"));
			if( !this.isAttributeHandlerMapped(this.astroframeSetter) ) {
				String msg = astroframeSetter.getUserMappingMsg();
				this.astroframeSetter = this.quantityDetector.getFrame();
				this.astroframeSetter.completeUserMappingMsg(msg);
			}
			break;

		case LAST:
			PriorityMessage.last("Coosys");
			this.astroframeSetter = this.quantityDetector.getFrame();
			if( !this.isAttributeHandlerMapped(this.astroframeSetter) ) {
				String msg = astroframeSetter.getDetectionMsg();
				this.astroframeSetter = this.getSetterForMappedColumn("system", mapping.getColumnMapping("system"));
				this.astroframeSetter.completeDetectionMsg(msg);
			}
			break;
		}
	}



	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPosAttributes() throws Exception {
		Messenger.locateCode();
		AxisMapping mapping = this.mapping.getSpaceAxisMapping();
		this.setQuantityDetector();

		switch(this.spaceMappingPriority){
		case ONLY:			
			PriorityMessage.only("Position");
			this.s_raSetter = this.getSetterForMappedColumn("s_ra", mapping.getColumnMapping("s_ra"));
			this.s_decSetter = this.getSetterForMappedColumn("s_dec", mapping.getColumnMapping("s_dec"));
			this.s_fovSetter = this.getSetterForMappedColumn("s_fov", mapping.getColumnMapping("s_fov"));
			this.s_regionSetter = this.getSetterForMappedColumn("s_region", mapping.getColumnMapping("s_region"));
			break;

		case FIRST:
			PriorityMessage.first("Position");
			this.s_raSetter = this.getSetterForMappedColumn("s_ra", mapping.getColumnMapping("s_ra"));
			if( !this.isAttributeHandlerMapped(this.s_raSetter) ) {
				String msg = s_raSetter.getUserMappingMsg();
				this.s_raSetter = this.quantityDetector.getAscension();
				this.s_raSetter.completeUserMappingMsg(msg);
			}
			this.s_decSetter = this.getSetterForMappedColumn("s_dec", mapping.getColumnMapping("s_dec"));
			if( !this.isAttributeHandlerMapped(this.s_decSetter) ) {
				String msg = s_decSetter.getUserMappingMsg();
				this.s_decSetter = this.quantityDetector.getDeclination();
				this.s_decSetter.completeUserMappingMsg(msg);
			}
			this.s_fovSetter = this.getSetterForMappedColumn("s_fov", mapping.getColumnMapping("s_fov"));
			if( !this.isAttributeHandlerMapped(this.s_fovSetter) ) {
				String msg = s_fovSetter.getUserMappingMsg();
				this.s_fovSetter = this.quantityDetector.getfov();
				this.s_fovSetter.completeUserMappingMsg(msg);
			}
			this.s_regionSetter = this.getSetterForMappedColumn("s_region", mapping.getColumnMapping("s_region"));
			if( !this.isAttributeHandlerMapped(this.s_regionSetter) ) {
				String msg = s_regionSetter.getUserMappingMsg();
				this.s_regionSetter = this.quantityDetector.getRegion();
				this.s_regionSetter.completeUserMappingMsg(msg);
			}
			break;

		case LAST:
			PriorityMessage.last("Position");
			this.s_raSetter = this.quantityDetector.getAscension();
			if( !this.isAttributeHandlerMapped(this.s_raSetter) ) {
				String msg = this.s_raSetter.getDetectionMsg();
				this.s_raSetter = this.getSetterForMappedColumn("s_ra", mapping.getColumnMapping("s_ra"));
				this.s_raSetter.completeDetectionMsg(msg);
			}
			this.s_decSetter = this.quantityDetector.getDeclination();
			if( !this.isAttributeHandlerMapped(this.s_decSetter) ) {
				String msg = s_decSetter.getDetectionMsg();
				this.s_decSetter = this.getSetterForMappedColumn("s_dec", mapping.getColumnMapping("s_dec"));
				this.s_decSetter.completeDetectionMsg(msg);
			}
			this.s_fovSetter = this.quantityDetector.getfov();
			if( !this.isAttributeHandlerMapped(this.s_fovSetter) ) {
				String msg = s_fovSetter.getDetectionMsg();
				this.s_fovSetter = this.getSetterForMappedColumn("s_fov", mapping.getColumnMapping("s_fov"));
				this.s_fovSetter.completeDetectionMsg(msg);
			}
			this.s_regionSetter = this.quantityDetector.getRegion();
			if( !this.isAttributeHandlerMapped(this.s_regionSetter) ) {
				String msg = s_regionSetter.getDetectionMsg();
				this.s_regionSetter = this.getSetterForMappedColumn("s_region", mapping.getColumnMapping("s_region"));
				this.s_regionSetter.completeDetectionMsg(msg);
			}
			break;
		}
		/*
		 * Id the astroframe hasn't been discovered before, we take this inferred from the position keywords
		 */
		if( this.astroframeSetter.isNotSet() ){
			ColumnExpressionSetter ces = this.quantityDetector.getFrame();
			if( !ces.isNotSet() ){
				this.astroframeSetter = ces;
			}
		}

	}

	/**
	 * @throws FatalException 
	 * 
	 */
	protected void mapCollectionPoserrorAttributes() throws Exception {
		Messenger.locateCode();
		AxisMapping mapping = this.mapping.getSpaceAxisMapping();
		this.setQuantityDetector();

		switch(this.spaceMappingPriority){
		case ONLY:			
			PriorityMessage.only("Position resolution");
			this.s_resolutionSetter = this.getSetterForMappedColumn("s_resolution", mapping.getColumnMapping("s_resolution"));
			//this.s_resolution_unitSetter = this.getSetterForMappedColumn("s_resolution_unit", mapping.getColumnMapping("s_resolution_unit"));
			break;

		case FIRST:
			PriorityMessage.first("Position resolution");
			this.s_resolutionSetter = this.getSetterForMappedColumn("s_resolution", mapping.getColumnMapping("s_resolution"));
			if( !this.isAttributeHandlerMapped(this.s_resolutionSetter) ) {
				String msg = s_resolutionSetter.getUserMappingMsg();
				this.s_resolutionSetter = this.quantityDetector.getSpatialError();
				this.s_resolutionSetter.completeUserMappingMsg(msg);
			}
			break;

		case LAST:
			PriorityMessage.last("Position resolution");
			this.s_resolutionSetter = this.quantityDetector.getSpatialError();
			if( !this.isAttributeHandlerMapped(this.s_resolutionSetter) ) {
				String msg = s_resolutionSetter.getDetectionMsg();
				this.s_resolutionSetter = this.getSetterForMappedColumn("s_resolution", mapping.getColumnMapping("s_resolution"));
				this.s_resolutionSetter.completeDetectionMsg(msg);
			}
			break;
		}
	}

	/**
	 * @throws Exception 
	 * 
	 */
	private void mapCollectionSpectralCoordinateFromMapping() throws Exception {
		Messenger.locateCode();
		AxisMapping mapping     = this.mapping.getEnergyAxisMapping();
		ColumnMapping sc_col    = mapping.getColumnMapping("dispertion_column");
		ColumnMapping emin_col    = mapping.getColumnMapping("em_min");
		ColumnMapping emax_col    = mapping.getColumnMapping("em_max");

		this.em_unitSetter   = this.getSetterForMappedColumn("em_unit", mapping.getColumnMapping("em_unit"));
		this.em_res_powerSetter = this.getSetterForMappedColumn("em_res_power", mapping.getColumnMapping("em_res_power"));
		this.em_binsSetter = this.getSetterForMappedColumn("em_bins", mapping.getColumnMapping("em_bins"));
		this.em_minSetter = new ColumnExpressionSetter("em_min");
		this.em_maxSetter = new ColumnExpressionSetter("em_max");

		if( !(emin_col.notMapped() || emax_col.notMapped()) ) {
			this.em_minSetter   = this.getSetterForMappedColumn("em_min", emin_col);
			this.em_maxSetter   = this.getSetterForMappedColumn("em_max", emax_col);			
		} else if( !sc_col.notMapped())
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
				if( this.em_unitSetter.isNotSet()) {
					AttributeHandler ah ;
					if( (ah = this.dataFile.getEntryAttributeHandlerCopy().get(col) ) != null ) {
						this.em_unitSetter = new ColumnExpressionSetter("em_unit", ah.getUnit());						
					}
				}

			} else {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "No mapping given for the dispersion column");
				return ;				
			}
	}


	/**
	 * @return Returns the metaclass.
	 * @param metaClass
	 * @throws IgnoreException if mc is null
	 * @throws Exception 
	 */
	public void setMetaclass(MetaClass metaClass) throws IgnoreException, Exception {
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
		return  this.getClass().getName() + " " + ((this.dataFile == null)? "Foo product foo/Foo": this.dataFile.getAbsolutePath().toString());
	}
	/**
	 * @return
	 */
	public long length() {
		return  (this.dataFile == null)? 1234: this.dataFile.length();
	}

	/**
	 * Returns a possible classname derived from the data file product name
	 * size limited to 48
	 * @return
	 */
	public String  possibleClassName() {
		String ret = new File(this.dataFile.getName()).getName().split("\\.")[0].replaceAll("[^\\w]+", "_").toLowerCase();
		/*
		 * PSQL limits the table name to 64.
		 * We take 48 because that is long enough
		 */
		if( ret.length() > 48 ){
			ret = ret.substring(0, 48);
		}
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
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, att.toString());	
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
			msg += SaadaConstant.NOTSET;
		}
		return msg;	
	}


}
