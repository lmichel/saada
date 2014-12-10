package saadadb.products.datafile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nom.tam.fits.FitsException;
import saadadb.collection.Category;
import saadadb.dataloader.mapping.EntryMapping;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.EntryBuilder;
import saadadb.products.ExtensionSetter;
import saadadb.products.Image2DBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.inference.QuantityDetector;
import saadadb.util.Messenger;
import saadadb.vocabulary.RegExp;

/**
 * @author michel
 * @version $Id$
 */
@SuppressWarnings("rawtypes")
public abstract class DataFile implements Enumeration {
	/**
	 * Map if the header attribute handler, key are database names (name attr)
	 * Contains only the keys selected by the builder
	 */
	public Map<String, AttributeHandler> attributeHandlers = null;
	/**
	 * Map if the table columns attribute handlers, key are database names (name attr)
	 * Contains only the keys selected by the builder
	 */
	public Map<String, AttributeHandler> entryAttributeHandlers = null;
	/**
	 * Complete map of the product.
	 */
	public Map<String, DataFileExtension> productMap;
	/**
	 * Reference on the product builder using this datafile
	 */
	public ProductBuilder productBuilder;
	/**
	 * In case of
	 */
	public List<String> comments = new ArrayList<String>();
	/**
	 * List of RegExp filtering  the accepted attributes
	 */
	private List<String> attributeFilter;
	/**
	 * List of RegExp filtering  the rejected attributes
	 */
	private List<String> ignoredAttribute;
	/**
	 * List of RegExp filtering  the rejected column names
	 */
	private List<String> ignoredEntryAttribute;
	/**
	 * List of RegExp filtering  the accepted column names
	 */
	private List<String>  entryAttributeFilter;
	/**
	 * 
	 */
	protected ProductMapping productMapping;
	
	/**
	 * @param productMapping
	 */
	DataFile(ProductMapping productMapping){
		this.setMapping(productMapping);
	}

	/**
	 * Returns the first extension possibly spectra data
	 * @return
	 */
	public DataFileExtension getFirstTableExtension() {
		for( DataFileExtension dfe: this.productMap.values() ) {
			if( dfe.isDataTable() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Extension #" + dfe.tableNum + " is a table");
				return dfe;				
			}
		}
		return null;
	}

	/**
	 * Return the category of the FITS extension matching the product category
	 * @return
	 * @throws FatalException 
	 */
	public int getProductCategory() {
		if( productBuilder == null ){
			return Category.UNKNOWN;
		} else if( productBuilder.getMapping() != null) {
			return productBuilder.mapping.getCategory();
		} else if( productBuilder instanceof Image2DBuilder) {
			return Category.IMAGE;
		} else if( productBuilder instanceof SpectrumBuilder) {
			return Category.SPECTRUM;
		}  else if( productBuilder instanceof TableBuilder) {
			return Category.TABLE;
		} else {
			return Category.MISC;
		}
	}

	/**
	 * Returns the first extension possibly image data
	 * @return
	 */
	public DataFileExtension getFirstImageExtension() {
		/*
		 * Look  at images
		 */
		for( DataFileExtension dfe: this.productMap.values() ) {
			if( dfe.isImage() ) {
				boolean foundAsc = false;
				boolean foundDec = false;
				for( AttributeHandler ah: dfe.attributeHandlers ) {
					if( ah.getNameorg().startsWith("CTYP") ) {
						if( !foundAsc && ah.getValue().matches(RegExp.FITS_CTYPE_ASC)){
							foundAsc = true;
						}
						if( !foundDec && ah.getValue().matches(RegExp.FITS_CTYPE_DEC)){
							foundDec = true;
						}
					}
					if( foundAsc && foundDec ) {
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Extension #" + dfe.tableNum + " has image WCS (both coords found)");
						return dfe;					
					}					
				}
			}
		}
		return null;
	}


	/**
	 * Returns the first extension possibly spectra data
	 * @return
	 */
	public DataFileExtension getFirstSpectralExtension() {
		/*
		 * Look first for an extension named SPECRUM
		 */
		for( DataFileExtension dfe: this.productMap.values() ) {
			if( (dfe.isImage() || dfe.isDataTable()) && dfe.tableName.equalsIgnoreCase("spectrum")) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Take " + dfe + " as spectrum extension because it is named SPECTRUM");
				return dfe;
			}
		}
		/*
		 * then Look at images
		 */
		for( DataFileExtension dfe: this.productMap.values() ) {
			if( dfe.isImage() ) {
				for( AttributeHandler ah: dfe.attributeHandlers ) {
					if( ah.getNameorg().startsWith("CTYP") && ah.getValue().matches(RegExp.FITS_CTYPE_SPECT)){
						if (Messenger.debug_mode)
							Messenger.printMsg(Messenger.DEBUG, "Extension #" + dfe.tableNum + " has spectral WCS");
						return dfe;
					}
				}
			}
		}
		/*
		 * else take the first table
		 */
		for( DataFileExtension dfe: this.productMap.values() ) {
			if( dfe.isDataTable() ) {
				if (Messenger.debug_mode)
					Messenger.printMsg(Messenger.DEBUG, "Extension #" + dfe.tableNum + " is a table: taken as spectra data");
				return dfe;				
			}
		}
		return null;
	}

	/**
	 * Returns a map of the current product
	 * @param category
	 * @return
	 * @throws FitsException
	 * @throws IOException
	 */
	public LinkedHashMap<String, List<AttributeHandler>> getProductMap(int category) throws IgnoreException {
		try {
			LinkedHashMap<String, List<AttributeHandler>> retour = new LinkedHashMap<String, List<AttributeHandler>>();
			Map<String, DataFileExtension> mapOrg = this.getProductMap();
			boolean taken = false;
			for( Entry<String, DataFileExtension> entry : mapOrg.entrySet()) {
				DataFileExtension extension = entry.getValue();
				if( category == Category.UNKNOWN || checkExtensionCategory(extension, category) ) {
					retour.put(entry.getKey(), extension.attributeHandlers);
					taken = true;
				} else {
					taken = false;
				}
				if( taken && extension.isDataTable() ) {
					retour.put(entry.getKey(), extension.attributeHandlers);
				}
			}
			return retour;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return null;
		}
	}
	/**
	 * return true if the hdu type is compliant with the category
	 * @param hdu
	 * @param category
	 * @return
	 */
	public boolean checkExtensionCategory(DataFileExtension hdu, int category) {
		if( hdu   != null) {
			/*
			 * If there is no specified category (BINTABLE or IMAGE) any extension 
			 * can be taken. That is the case for MISC products
			 */
			if( category == Category.UNKNOWN ) {
				return true;
			}
			/*
			 * 1st HDU can be seen as a 0x0 pixel image: Must look for the first not empty image
			 */
			else if( category == Category.IMAGE && hdu.isImage() ){
				return true;
			}					
			else if( category == Category.TABLE && hdu.isDataTable() ){
				return true;
			}
			/*
			 * With V2 datamodel, the dataloader must be enabled to detect the energy range even for misc
			 */
			else if( (category == Category.SPECTRUM || category == Category.MISC  ) && hdu.isDataTable() ) {
				return true;
			}
		}
		return false;		
	}

	/**
	 * Returns the map of the attribute handlers modeling the columns of te data tables
	 * @return
	 */
	public Map<String, AttributeHandler> getEntryAttributeHandlerCopy() throws SaadaException  {
		if( this.entryAttributeHandlers == null ){
			this.mapEntryAttributeHandler();
		}
		Map<String, AttributeHandler> mah = this.entryAttributeHandlers;
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		for( Entry<String, AttributeHandler > e: mah.entrySet()){
			System.out.println("DATAFILE " + e.getValue());
			retour.put(e.getKey(), (AttributeHandler)(e.getValue().clone()));
		}
		(new Exception()).printStackTrace();
		return retour;
	}
	/**
	 * Returns the map of the attribute handlers modeling the KW of all headers loaded
	 * @return
	 */
	public Map<String, AttributeHandler> getAttributeHandlerCopy() throws SaadaException{
		if( this.attributeHandlers == null ){
			this.mapAttributeHandler();
		}
		Map<String, AttributeHandler> mah = this.attributeHandlers;
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		for( Entry<String, AttributeHandler > e: mah.entrySet()){
			retour.put(e.getKey(), (AttributeHandler)(e.getValue().clone()));
		}
		return retour;
	}
	/**
	 * Return a list of comments which can be used to do the automatic mapping
	 * @return
	 * @throws SaadaException
	 */
	public List<String> getComments() {
		return this.comments;
	}

	
	/**
	 * Set the builder's attribute handlers with the values read in the product file
	 * @throws Exception
	 */
	public void updateAttributeHandlerValues() throws Exception {
		this.mapAttributeHandler();
		if( this.productBuilder.productAttributeHandler == null ){
			this.productBuilder.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		} else {
			for( AttributeHandler ah: this.productBuilder.productAttributeHandler.values()){
				ah.setValue("");
			}
		}

		for( Entry<String, AttributeHandler> eah: this.attributeHandlers.entrySet()){
			AttributeHandler localAh = eah.getValue();
			String localKey = eah.getKey();
			AttributeHandler builderAh = this.productBuilder.productAttributeHandler.get(localKey);

			if( builderAh == null ){
				System.out.println("ADD " + localAh.clone());
				this.productBuilder.productAttributeHandler.put(localKey, (AttributeHandler)(localAh.clone()));
			} else {
				builderAh.setValue(localAh.getValue());
			}
		}
	}

	/**
	 * Set the builder's attribute handlers with the values read in the table columns of the product file
	 * @throws Exception
	 */
	public void updateEntryAttributeHandlerValues(EntryBuilder entryBuilder) throws Exception {
		//this.mapAttributeHandler();
		if( entryBuilder.productAttributeHandler == null ){
			entryBuilder.productAttributeHandler = new LinkedHashMap<String, AttributeHandler>();
		} else {
			for( AttributeHandler ah: entryBuilder.productAttributeHandler.values()){
				ah.setValue("");
			}
		}

		Object[] rowData = (Object[]) this.nextElement();
		if( rowData != null ){
			int cpt = 0;
			for( AttributeHandler ah: this.entryAttributeHandlers.values()){
				ah.setValue(rowData[cpt].toString());
				cpt++;
			}

			for( Entry<String, AttributeHandler> eah: this.entryAttributeHandlers.entrySet()){
				AttributeHandler localAh = eah.getValue();
				String localKey = eah.getKey();
				AttributeHandler builderAh = entryBuilder.productAttributeHandler.get(localKey);

				if( builderAh == null ){
					entryBuilder.productAttributeHandler.put(localKey, (AttributeHandler)(localAh.clone()));
				} else {
					builderAh.setValue(localAh.getValue());

				}
			}
		}
	}

	/**
	 * Check if the cards pass the filters given by the mapping.
	 * If a card matches one expression of the attribute filters, 
	 * it is taken, if not and if it matches one of the ignored attributes 
	 * it is rejected otherwise is is accepted 
	 * @param cardName
	 * @return
	 */
	protected boolean isCardAccepted(String cardName){
		for(String regex : this .attributeFilter){
			if( cardName.matches(regex)){
				return true;
			} 
		}
		if( this .attributeFilter.size() > 0 ){
			return false;
		}

		for(String regex : this.ignoredAttribute){
			if( cardName.matches(regex)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the cards pass the filters given by the mapping.
	 * If a card matches one expression of the attribute filters, 
	 * it is taken, if not and if it matches one of the ignored attributes 
	 * it is rejected otherwise is is accepted 
	 * @param cardName
	 * @return
	 */
	protected boolean isCardAccepted(AttributeHandler card){
		return this.isCardAccepted(card.getNameorg());
	}

	/**
	 * Check if the cards pass the  column name filters given by the mapping.
	 * If a card matches one expression of the attribute filters, 
	 * it is taken, if not and if it matches one of the ignored attributes 
	 * it is rejected otherwise is is accepted 
	 * @param cardName
	 * @return
	 */
	protected boolean isEntryCardAccepted(String cardName){
		for(String regex : this.entryAttributeFilter){
			if( cardName.matches(regex)){
				return true;
			} 
		}
		if( this .entryAttributeFilter.size() > 0 ){
			return false;
		}
		for(String regex : this.ignoredEntryAttribute){
			if( cardName.matches(regex)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the cards pass the  column name filters given by the mapping.
	 * If a card matches one expression of the attribute filters, 
	 * it is taken, if not and if it matches one of the ignored attributes 
	 * it is rejected otherwise is is accepted 
	 * @param cardName
	 * @return
	 */
	protected boolean isEntryCardAccepted(AttributeHandler card){
		return this.isEntryCardAccepted(card.getNameorg());
	}

	/**
	 * @param builder
	 */
	protected void setBuilder(ProductBuilder builder){
		System.out.println("SETTTTTTTTTTTTTTTTTTTTTt");
		this.productBuilder = builder;
		this.setMapping(builder.mapping);
	}

	/**
	 * @param productMapping
	 */
	protected void  setMapping(ProductMapping productMapping){
		this.productMapping = productMapping;
		List<String> map;
		map = this.productMapping.getIgnoredAttributes();
		this.ignoredAttribute = (map != null)? map : new ArrayList<String>();
		map = this.productMapping.getAttributeFilter();
		this.attributeFilter  = (map!= null)? map : new ArrayList<String>();
		
		EntryMapping eMapping = productMapping.getEntryMapping();
		map = (eMapping != null)? eMapping.getIgnoredAttributes(): null;
		this.ignoredEntryAttribute = (map != null)? map	: new ArrayList<String>();
		map = (eMapping != null)? eMapping.getAttributeFilter(): null;
		this.entryAttributeFilter  = (map != null)?map: new ArrayList<String>();
	}
	/*******************************
	 * Abstract methods
	 */
	/**
	 * Returns a map with all extension detected within the data product
	 * @return
	 * @throws Exception 
	 */
	public abstract Map<String, DataFileExtension> getProductMap() throws Exception;

	/**
	 * Construct the local AH map from the keywords read in the file. This map is ordered as the keywords
	 */
	public abstract void mapAttributeHandler() throws IgnoreException ;
	/**
	 * Construct the local AH map from the tabel columns. This map is ordered as the keywords
	 */
	public abstract void mapEntryAttributeHandler() throws IgnoreException ;

	/**
	 * Connect the DataFile with the ProductBuilder. The header AHs of the DataFile are copied here to the Builder
	 * @param builder
	 */
	public abstract void bindBuilder(ProductBuilder builder) throws Exception;

	/**
	 * Connect the DataFile with the ProductBuilder. The table AHs of the DataFile table are copied here to the Builder
	 * The builder is not referenced by the DataFile since it just deal with a part of the product.
	 * @param builder
	 */
	public void bindEntryBuilder(ProductBuilder builder) throws Exception {
		builder.productAttributeHandler = this.getEntryAttributeHandlerCopy();			
	}
	/**Returns the value corresponding finded in the product file to the key word in parameter.
	 *@param String The key word.
	 *@return String The value corresponding to this key word, if he exists, else null.
	 */
	public abstract String getKWValueQuickly(String key);

	/**
	 * @param key  column name as it is in the file
	 * @return min,max,nbpoints
	 * @throws Exception
	 */
	public abstract Object[] getExtrema(String keyOrg) throws Exception ;
	/**In case of the product can have table:
	 * Returns the row number in the table.
	 * If there is no table for this product format, this method will return 0.
	 *@return int The row number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public abstract int getNRows() throws IgnoreException;

	/**In case of the product can have table:
	 * Returns the column number in the table.
	 * If there is no table for this product format, this method will return 0.
	 *@param numHDU The n'th table Header.
	 *@return int The column number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public abstract int getNCols() throws IgnoreException;
	/**In case of the product can have table:
	 * Initializes the enumeration of table rows (essential in stream mode).
	 * This method is necessary in the class Product (package saadadb.products) for return a initialized enumeration:
	 * See method elements() in class Product (she returns a Enumeration).
	 *@return void.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public abstract  void initEnumeration() throws IgnoreException;
	/**
	 * Close the data stream
	 * @throws QueryException
	 */
	public abstract void closeStream() throws QueryException;

	/**
	 * Returns the list of the loaded extension with the reason why they have been taken
	 * @return
	 */
	public abstract List<ExtensionSetter> reportOnLoadedExtension() ;	
	/**
	 * Method overloading a File accessors
	 * @return
	 * @throws IOException
	 */
	public abstract String getCanonicalPath() throws IOException ;
	/**
	 * Method overloading a File accessor
	 * @return
	 * @throws IOException
	 */
	public abstract String getAbsolutePath();
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public abstract String getParent() ;
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public abstract long length() ;
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public abstract boolean delete() ;

	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public abstract String getName();


}

