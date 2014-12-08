package saadadb.products.datafile;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.PaddingException;
import nom.tam.fits.TableHDU;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.Cursor;
import saadadb.collection.Category;
import saadadb.dataloader.mapping.EntryMapping;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.ExtensionSetter;
import saadadb.products.Image2DBuilder;
import saadadb.products.ProductBuilder;
import saadadb.products.SpectrumBuilder;
import saadadb.products.TableBuilder;
import saadadb.products.inference.QuantityDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.util.ChangeKey;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.util.TileRiceDecompressor;
import saadadb.vocabulary.RegExp;
import saadadb.vocabulary.enums.DataFileExtensionType;
import saadadb.vocabulary.enums.ExtensionSetMode;

/**
 * @author laurent MICHEL
 *  * @version $Id$

 * 06/2011: Returns table rows a an array with the right type taken from the TFORM 
 *          keyword instead of using the reflexion of on data itself (fails on NULL value)
 * 02/2014  Make it working with a {@link ProductBuilder} without configuration, so that the code 
 *          can be used out of a DB context
 */

public final class FitsDataFile extends FSDataFile{

	private static final long serialVersionUID = 1L;
	private TableHDU tableEnumeration;
	private int nextIndex = 0;
	private int nb_rows = 0;
	protected Fits fitsData=null;
	//	protected int good_header_number;
	protected ExtensionSetter  extensionSetter = new ExtensionSetter();
	protected BasicHDU goodHeader;
	protected BasicHDU firstHeader;

	private String ra="";

	private String fmtsignature;
	protected SpaceKWDetector space_frame;
	private int[] colform ;

	/**Constructor (constructor of the super class "File").
	 * Creates a new File instance by converting the given pathname string into an abstract patname.
	 * @param String The name file of the product.
	 * @throws FitsException 
	 */
	public FitsDataFile(String name, ProductBuilder product) throws Exception{
		super(name);
		System.out.println("@@@@@@ FITS Const 1");
		this.productBuilder = product;
		this.fitsData = new Fits(getCanonicalPath()); 
		this.getProductMap();
	}
	/**
	 * @param name
	 * @throws Exception
	 */
	public FitsDataFile(String name) throws Exception {
		super(name);
		System.out.println("@@@@@@ FITS Const 2");
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Reading FITS file " +name);
		this.fitsData = new Fits(name);
		this.getProductMap();	
	}
	/**
	 * @param product
	 * @throws Exception
	 */
	public FitsDataFile(ProductBuilder product) throws Exception {
		super(product.dataFile.getAbsolutePath());
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "reading FIT file " + product.getName());
		this.productBuilder = product;
		this.fitsData = new Fits(product.dataFile.getCanonicalPath());
		this.getProductMap();
		try {
			this.firstHeader = fitsData.getHDU(0);
		} catch (PaddingException e) {
			this.fitsData.addHDU(e.getTruncatedHDU());
			this.firstHeader = this.fitsData.getHDU(0);
			Messenger.printMsg(Messenger.WARNING, e.getMessage());
		}

		/* ****************************************************
		 * Now we can start the product configuration.
		 * First we have to create the tableAttributeHandler.
		 * To do that we have to know which extension use and if
		 * it exists. Then when we'll know that, we're going 
		 * to create the tableAttributeHandler.
		 ******************************************************/

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the extension to use.");
		//look if the name of the extension to use is defined
		String ext_name = null;
		if( product.getMapping() != null ) {
			ext_name = product.mapping.getExtension();
		}
		if( ext_name != null && !ext_name.equals("") ){	//if it is defined
			/*
			 * Extension is given as a number: check if it exists and if the category is OK
			 */
			int ext_num;
			if( ext_name.matches("#[0-9]+") ) {
				ext_num = Integer.parseInt(ext_name.substring(ext_name.indexOf('#') + 1));
			}
			/*
			 * Extension is given as a name: check if it exists and if the category is OK
			 */
			else {
				ext_num = getHeaderNumber(product.mapping.getExtension());
			}
			/*
			 * Once the extension found, we need to check that it has the good category
			 * a BINTABLE for spectra and tables...
			 */
			BasicHDU bHDU = null;
			if(  ext_num >= 0 ) {
				boolean found = false;
				for( DataFileExtension dfe: this.productMap.values()){
					if( dfe.tableNum == ext_num && !dfe.isDataTable() ) {
						found = true;
						if(checkExtensionCategory(dfe, this.getProductCategory()) ){
							String msg = "Required extension : "+product.mapping.getExtension()+" found (number: " + ext_num + ")";
							this.extensionSetter = new ExtensionSetter(ext_num
									, ExtensionSetMode.GIVEN
									, msg);
							this.goodHeader = bHDU;
							Messenger.printMsg(Messenger.TRACE, msg);							
						} else if( product.getMapping().getCategory() == Category.SPECTRUM && checkExtensionCategory(dfe, Category.IMAGE) ){
							this.goodHeader = bHDU;
							String msg = "Take" + dfe.getSType()+ " HDU# " + ext_num +  " as spectral chanels";
							this.extensionSetter = new ExtensionSetter(ext_num
									, ExtensionSetMode.DETECTED
									, msg);
							Messenger.printMsg(Messenger.TRACE, msg);
						} else {
							IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE, "Required extension : "+product.mapping.getExtension() + " has a wrong type: " + bHDU.getClass().getName());
						}
					} 
				}
				if( !found ) {
					IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE, "Required extension : "+product.mapping.getExtension() + " not found");
				}
			}
		} else {
			this.setFirstGoodHeader(product);
			Messenger.printMsg(Messenger.TRACE, "The first  "
					+  Category.explain(product.mapping.getCategory())
					///	+ this.productBuilder.getClass().getName()
					+ " extension is #"+this.getGood_header_number());				
		}
		if( this.productBuilder.mapping != null )
			this.productBuilder.mapping.getHeaderRef().setNumber(this.getGood_header_number());	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Creation of the tableAttributHandler...");
		this.productBuilder.productAttributeHandler = this.getAttributeHandlerCopy();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The tableAttributeHandler is OK.");
	}

	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#bindBuilder(saadadb.products.ProductBuilder)
	 */
	@Override
	public void bindBuilder(ProductBuilder builder) throws Exception{
		System.out.println("@@@@@@@@@ FITS BndBuilder");
		this.productBuilder = builder;
		try {
			this.firstHeader = fitsData.getHDU(0);
		} catch (PaddingException e) {
			fitsData.addHDU(e.getTruncatedHDU());
			this.firstHeader = fitsData.getHDU(0);
			Messenger.printMsg(Messenger.WARNING, e.getMessage());
		}

		/* ****************************************************
		 * Now we can start the product configuration.
		 * First we have to create the tableAttributeHandler.
		 * To do that we have to know which extension use and if
		 * it exists. Then when we'll know that, we're going 
		 * to create the tableAttributeHandler.
		 ******************************************************/

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the extension to use.");
		//look if the name of the extension to use is defined
		String ext_name = null;
		if( this.productBuilder.getMapping() != null ) {
			ext_name = this.productBuilder.mapping.getExtension();
		}
		if( ext_name != null && !ext_name.equals("") ){	//if it is defined
			/*
			 * Extension is given as a number: check if it exists and if the category is OK
			 */
			int ext_num;
			if( ext_name.matches("#[0-9]+") ) {
				ext_num = Integer.parseInt(ext_name.substring(ext_name.indexOf('#') + 1));
			}
			/*
			 * Extension is given as a name: check if it exists and if the category is OK
			 */
			else {
				ext_num = getHeaderNumber(this.productBuilder.mapping.getExtension());
			}
			/*
			 * Once the extension found, we need to check that it has the good category
			 * a BINTABLE for spectra and tables...
			 */
			if(  ext_num >= 0 ) {
				boolean found = false;
				for( DataFileExtension dfe: this.productMap.values()){
					if( dfe.tableNum == ext_num  ) {
						found = true;
						/**
						 * TABLE_COLUMNS extension reserved to access table data
						 */
						if( dfe.type == DataFileExtensionType.TABLE_COLUMNS){
							continue;
						} else 	if( this.checkExtensionCategory(dfe, this.getProductCategory()) ){
							String msg = "Required extension : "+this.productBuilder.mapping.getExtension()+" found (number: " + ext_num + ")";
							this.extensionSetter = new ExtensionSetter(ext_num
									, ExtensionSetMode.GIVEN
									, msg);
							this.goodHeader = (BasicHDU) dfe.resource;
							Messenger.printMsg(Messenger.TRACE, msg);							
						} else if( this.productBuilder.getMapping().getCategory() == Category.SPECTRUM && checkExtensionCategory(dfe, Category.IMAGE) ){
							this.goodHeader = (BasicHDU) dfe.resource;
							String msg = "Take " + dfe.getSType() + "  HDU# " + ext_num +  " as spectral chanels";
							this.extensionSetter = new ExtensionSetter(ext_num
									, ExtensionSetMode.DETECTED
									, msg);
							Messenger.printMsg(Messenger.TRACE, msg);
						} else {
							IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE
									, "Required extension : "+this.productBuilder.mapping.getExtension() + " has a wrong type: " + dfe.type);
						}
					} 
				}
				if( !found ) {
					IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE
							, "Required extension : "+this.productBuilder.mapping.getExtension() + " not found");
				}
			}
		} else {
			this.setFirstGoodHeader(this.productBuilder);
			Messenger.printMsg(Messenger.TRACE, "The first  "
					+  Category.explain(this.productBuilder.mapping.getCategory())
					+ " extension is #"+this.getGood_header_number());				
		}

		if( this.productBuilder.mapping != null )
			this.productBuilder.mapping.getHeaderRef().setNumber(this.getGood_header_number());	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Creation of the tableAttributHandler...");
		this.updateAttributeHandlerValues();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The tableAttributeHandler is OK.");
	}

	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#closeStream()
	 */
	@Override
	public void closeStream() {
		if( this.fitsData != null && this.fitsData.getStream() != null) {
			try {
				this.fitsData.getStream().close();
			} catch (IOException e) {
				Messenger.printMsg(Messenger.WARNING, "Closing stream of " + this.productBuilder.getName() +  " " + e.getMessage());
			}
		}
	}


	/**
	 * Build an attributeHandler map the columns definition of the extension # hduNum
	 * @param hduNum
	 * @return
	 * @throws IgnoreException
	 */
	private Map<String, AttributeHandler> getEntryAttributeHandler(int hduNum) throws IgnoreException {
		Map<String, AttributeHandler> retour = new LinkedHashMap<String, AttributeHandler>();
		try {
			BasicHDU bHDU = fitsData.getHDU(hduNum);
			if(  !(bHDU instanceof  nom.tam.fits.TableHDU) ){
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "No column names for a " + bHDU.getClass().getName());
				return retour;  		
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Read the meta data of the columns of the extension #" + hduNum);
			/*
			 * product can be null when used to build a product map
			 */
			EntryMapping entryconf = null;
			List<String> kWIgnored = null;
			/*
			 * kWIgnored are porcessed in the case of a table loading but the method can be called in others contexts such as the
			 * Spectrum coordinate detection
			 */
			if( this.productBuilder != null && this.productBuilder.mapping.getCategory() == Category.TABLE  ) {
				entryconf = this.productBuilder.mapping.getEntryMapping();
				kWIgnored = entryconf.getIgnoredAttributes();
			}
			//Creates the new list which maps entry names formated in the standard of Saada to their objects modelling entry informations
			AttributeHandler attribute;
			//Creates a sorted list for the md5 calculation
			String keyChanged = "";
			//The HDU with index 1 coorresponding to the Object TableHDU
			TableHDU table = (TableHDU)(bHDU);
			boolean isascii = isASCIITable(table);
			//Initializes the TableHDU header
			Header tableHeader = table.getHeader();
			//Localizes the card associated with the entry name
			HeaderCard cardType;
			HeaderCard cardUnit;
			String unitValue = "";
			String unitComment = "";
			String typeValue;
			String format;
			//Foreach card in this table
			for(int j = 0; j < table.getNCols(); j++){
				cardType = tableHeader.findCard("TTYPE"+(j+1));
				typeValue = cardType.getValue().trim();
				//cardFormat = tableHeader.findCard("TFORM"+(j+1));
				cardUnit = tableHeader.findCard("TUNIT"+(j+1));
				//if there is a unit card for this entry, sets comment and unit values (else preserved the default values)
				if(cardUnit!=null){
					unitValue = cardUnit.getValue().trim();
					String tmpUnitComment = cardUnit.getComment();
					if(tmpUnitComment != null){
						unitComment = tmpUnitComment;
					}
				}
				else {
					unitValue = "";	
					unitComment	= "";			
				}
				//Initializes the md5 value for this attribute
				//Creates a new attribute Object
				attribute = new AttributeHandler();
				//Sets the original name of this entry to this field in the attribute object
				attribute.setNameorg(typeValue);
				if( kWIgnored != null ) {
					boolean ignore = false;
					for( String ign: kWIgnored) {
						if( typeValue.matches(ign)) {
							if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The column : "+typeValue+" is ignored (pattern: " + ign + ")");
							ignore = true;
							break;
						}
					}
					/*
					 * Store the number of the ignored columns
					 */
					if( entryconf != null && ignore ){
						entryconf.addToFreeIndex(j);
						continue;
					}
				}
				//Transforms this original name according to the Saada standard
				keyChanged = ChangeKey.changeKey(typeValue);
				keyChanged = ChangeKey.renameDuplicateKey(retour, keyChanged);
				//Sets this standardized name of this entry to this field in the attribute object
				attribute.setNameattr(keyChanged);
				//Puts the current attribute object to the current list of product attributes
				retour.put(keyChanged, attribute);
				//Sets the collection name of this entry (of this product)
				if( this.productBuilder != null)
					attribute.setCollname(this.productBuilder.mapping.getCollection());
				//Sets the unit of this entry to this field in the attribute object
				attribute.setUnit(unitValue);				
				attribute.setComment(cardType.getComment());
				//				if(!unitComment.equals("no Comment")){
				//					attribute.setComment(unitComment);
				//				}
				//Initiazes the reality format of this entry
				format = table.getColumnFormat(j);
				int javatypecode = JavaTypeUtility.convertFitsFormatToJavaType(format, isascii);
				attribute.setType(JavaTypeUtility.convertJavaTypeCodeToName(javatypecode));
			}
			return retour;

		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
		}
		return retour;
	}


	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWEntry(java.util.LinkedHashMap)
	 */
	@Override
	public void mapEntryAttributeHandler() throws IgnoreException {
		int hduNum = this.extensionSetter.getGoodHeaderNumber();
		this.entryAttributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		try {
			BasicHDU bHDU = fitsData.getHDU(hduNum);
			if(  !(bHDU instanceof  nom.tam.fits.TableHDU) ){
				IgnoreException.throwNewException(SaadaException.FITS_FORMAT, "No column names for a " + bHDU.getClass().getName());
			}
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Read the meta data of the columns of the extension #" + hduNum);
			/*
			 * product can be null when used to build a product map
			 */
			EntryMapping entryconf = null;
			List<String> kWIgnored = null;
			/*
			 * kWIgnored are porcessed in the case of a table loading but the method can bu called in others contexts such as the
			 * Spectrum coordinate detection
			 */
			if( this.productBuilder != null && this.productBuilder.mapping.getCategory() == Category.TABLE  ) {
				entryconf = this.productBuilder.mapping.getEntryMapping();
				kWIgnored = entryconf.getIgnoredAttributes();
			}
			//Creates the new list which maps entry names formated in the standard of Saada to their objects modelling entry informations
			AttributeHandler attribute;
			//Creates a sorted list for the md5 calculation
			String keyChanged = "";
			//The HDU with index 1 corresponding to the Object TableHDU
			TableHDU table = (TableHDU)(bHDU);
			boolean isascii = isASCIITable(table);
			//Initializes the TableHDU header
			Header tableHeader = table.getHeader();
			//Localizes the card associated with the entry name
			HeaderCard cardType;
			HeaderCard cardUnit;
			String unitValue = "";
			String typeValue;
			String format;
			//Foreach card in this table
			for(int j = 0; j < table.getNCols(); j++){
				cardType = tableHeader.findCard("TTYPE"+(j+1));
				typeValue = cardType.getValue().trim();
				//cardFormat = tableHeader.findCard("TFORM"+(j+1));
				cardUnit = tableHeader.findCard("TUNIT"+(j+1));
				//if there is a unit card for this entry, sets comment and unit values (else preserved the default values)
				if(cardUnit!=null){
					unitValue = cardUnit.getValue().trim();
				}
				else {
					unitValue = "";	
				}
				//Creates a new attribute Object
				attribute = new AttributeHandler();
				//Sets the original name of this entry to this field in the attribute object
				attribute.setNameorg(typeValue);
				if( kWIgnored != null ) {
					boolean ignore = false;
					for( String ign: kWIgnored) {
						if( typeValue.matches(ign)) {
							if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The column : "+typeValue+" is ignored (pattern: " + ign + ")");
							ignore = true;
							break;
						}
					}
					/*
					 * Store the number of the ignored columns
					 */
					if( entryconf != null && ignore ){
						entryconf.addToFreeIndex(j);
						continue;
					}
				}
				//Transforms this original name according to the Saada standard
				keyChanged = ChangeKey.changeKey(typeValue);
				keyChanged = ChangeKey.renameDuplicateKey(this.entryAttributeHandlers, keyChanged);
				//Sets this standardized name of this entry to this field in the attribute object
				attribute.setNameattr(keyChanged);
				//Puts the current attribute object to the current list of product attributes
				this.entryAttributeHandlers.put(keyChanged, attribute);
				//Sets the collection name of this entry (of this product)
				if( this.productBuilder != null)
					attribute.setCollname(this.productBuilder.mapping.getCollection());
				//Sets the unit of this entry to this field in the attribute object
				attribute.setUnit(unitValue);				
				attribute.setComment(cardType.getComment());
				//				if(!unitComment.equals("no Comment")){
				//					attribute.setComment(unitComment);
				//				}
				//Initiazes the reality format of this entry
				format = table.getColumnFormat(j);
				int javatypecode = JavaTypeUtility.convertFitsFormatToJavaType(format, isascii);
				attribute.setType(JavaTypeUtility.convertJavaTypeCodeToName(javatypecode));
			}

		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
		}
	}  


	/**In case of the product can have table:
	 * Returns the algorithmics value for the entries characteristics (all sorted entries) with md5.
	 *@return String this algorithmics value with md5 for all entries.
	 */
	public String getfmtsignature(){
		return fmtsignature;
	}
	/**In case of the product can have table:
	 * Returns one objects row for one table row with table index in parameter (one object equals one rows box).
	 * This row is maked with the first HDU (index 1).
	 * If there is no row with this table index, returns null.
	 *@param numHDU The n'th HDU.
	 *@return Object[] The objects row in the the table with the index in parameter.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public Object[] getRow(int index) throws IgnoreException{
		return getRow(index, 1);
	}
	/**In case of the product can have table:
	 * Returns one objects row for one table row with table index in parameter (one object equals one rows box).
	 * This row is maked with the n'th HDU in parameter.
	 * If there is no row with this table index, returns null. 
	 */
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getRow(int, int)
	 */
	public Object[] getRow(int index, int numHDU) throws IgnoreException{
		try {
			//The initilization in this method allows not to overload the computer memory and to provoke an memory exception
			//by modelling wrongly these data (as in the global header).
			return ((TableHDU)this.fitsData.getHDU(numHDU)).getRow(index);
		} catch(Exception e) {
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return null;
		}
	}


	/**In case of the product can have table:
	 * Tests if this enumeration contains more elements (more table rows).
	 *@return boolean true if and only if this enumeration object contains at least one more element (table row) to provide; false otherwise.
	 * @throws FitsException 
	 */
	public boolean hasMoreElements() {
		/* 
		 * FITS columns with 1A type rise 
		 * java.lang.ArrayIndexOutOfBoundsException: -1
		 * at nom.tam.fits.BinaryTable.columnToArray(Unknown Source)
		 * at nom.tam.fits.BinaryTable.getFileRow(Unknown Source)
		 * at nom.tam.fits.BinaryTable.getRow(Unknown Source)
		 * at nom.tam.fits.TableHDU.getRow(Unknown Source)
		 */
		try {
			/*
			 * Must check nb_rows because table can be followed with padding (e.g. vizier) 
			 * That can duplicate the last non empty line.
			 */
			if( nextIndex >= nb_rows ) {
				nextIndex = 0;
				return false;  			
			}
			if(!tableEnumeration.getRow(nextIndex).equals(null)){
				return true;
			}else{
				nextIndex = 0;
				return false;
			}
		} catch (FitsException e) {
			Messenger.printStackTrace(e);
			return false;
		}
	}
	/**In case of the product can have table:
	 * Returns the next element of this enumeration if this enumeration object has at least one more element to provide.
	 * This method returns one objects row for one table row (one object equals one rows box).
	 *@return int the next element (the next table row) of this enumeration.
	 *@throws NoSuchElementException if no more elements (table rows)  exist.
	 */
	public Object nextElement(){
		try{
			Object[] data = tableEnumeration.getRow(nextIndex);
			for( int i=0 ; i<data.length ; i++ ){
				Object cell = data[i];
				switch( colform[i] ) {
				case JavaTypeUtility.DOUBLE: if( cell == null ) {
					data[i] = SaadaConstant.DOUBLE;
				}
				else {
					data[i] = ((double[])(cell))[0];		
				}
				break;

				case JavaTypeUtility.FLOAT:	if( cell == null ) {
					data[i] = SaadaConstant.FLOAT;
				}
				else {
					data[i] = ((float[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.BOOLEAN:	if( cell == null ) {
					data[i] = false;
				}
				else {
					data[i] = ((boolean[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.BYTE: if( cell == null ) {
					data[i] = SaadaConstant.BYTE;
				}
				else {
					data[i] = ((byte[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.CHAR: if( cell == null ) {
					data[i] = SaadaConstant.CHAR;
				}
				else {
					data[i] = ((char[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.INT: 
					if( cell == null ) {
						data[i] = SaadaConstant.INT;
					}
					else {
						data[i] = ((int[])(cell))[0];		
					}
					break;
				case JavaTypeUtility.SHORT: if( cell == null ) {
					data[i] = SaadaConstant.SHORT;
				}
				else {
					data[i] = ((short[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.LONG: if( cell == null ) {
					data[i] = SaadaConstant.LONG;
				}
				else {
					data[i] = ((long[])(cell))[0];		
				}
				break;
				case JavaTypeUtility.STRING: if( cell == null ) {
					data[i] = SaadaConstant.STRING;
				}
				else {
					String s = ((String[])cell)[0];		
					int pos_0 = s.indexOf(0);
					if( pos_0 >= 0) s =  s.substring(0, pos_0);
					data[i] = s;

				}
				break;
				case JavaTypeUtility.CHARARRAY: if( cell == null ) {
					data[i] = SaadaConstant.STRING;
				}
				else {
					String s = ((String)cell);		
					int pos_0 = s.indexOf(0);
					if( pos_0 >= 0) s =  s.substring(0, pos_0);
					data[i] = s;
				}
				break;
				case JavaTypeUtility.UNSUPPORTED:  data[i] = "";
				break;
				default:if( cell == null ) {
					data[i] = SaadaConstant.STRING;
				}
				else {
					data[i] = cell;				
					int pos_0 = ((String)data[i]).indexOf(0);
					if( pos_0 >= 0)
						data[i] =  ((String) data[i]).substring(0, pos_0);
				}
				break;
				}
				//System.out.println(i + " " + colform[i] + " " + cell + " " + data[i]);
			}
			nextIndex++;
			return data;
		}catch(FitsException e){
			Messenger.printMsg(Messenger.ERROR, " in table enumeration in "+getAbsolutePath());
			return null;
		}
	}

	/**In case of the product can have table:
	 * Returns the column number in the table.
	 *@param numHDU The n'th table Header.
	 *@return int The column number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public int getNCols(int numHDU) throws FitsException, IOException{

		//The initilization in this method allows not to overload the computer memory and to provoke an memory exception
		//by modelling wrongly these data (as in the global header).
		return ((TableHDU)this.fitsData.getHDU(numHDU)).getNCols();

	}
	/**Tests if the product files contains the key word in parameter.
	 *@param String The key word.
	 *@return boolean true or false, if the product file contains this key word, or not.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public boolean hasValuedKW(String key) throws FitsException, IOException{
		return this.fitsData.readHDU().getHeader().containsKey(key);
	}


	/**Returns the algorithmics value for the product characteristics (sorted attributes without values) with md5.
	 *@return String this algorithmics value with md5.
	 */ 
	public String getFmtsignature(){
		return fmtsignature;
	}
	/**In case of the product can have 2D image:
	 * Gets the image in this product file, if she exists, else null.
	 *@return Image the image in this product file.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public Image getBitMapImage() throws FitsException, IOException{
		ImageHDU himage = (ImageHDU)this.fitsData.getHDU(this.productBuilder.mapping.getHeaderRef().getNumber());
		int[] size = himage.getAxes();
		int ww = size[0];
		int hh = size[1];
		Object img =  himage.getTiler().getTile(new int[]{0,0}, size);
		int nbit = himage.getBitPix() ;
		int[] pixel = new int[ww*hh];
		if(nbit==32){
			pixel = (int[])img;
		}else{
			if(nbit==-32){
				for(int i=0; i<ww*hh;i++){
					pixel[i] = java.lang.Math.round(((float[])img)[i]);
				}
			}
		}
		for(int i=0; i<ww*hh;i++){
			pixel[i] = 3000*(0-pixel[i]);
		}
		return Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(ww, hh,ColorModel.getRGBdefault() ,pixel, 0, ww));

	}
	/**In case of the product can have spectrum:
	 * Gets the spectrum image in this product file, if she exists, else null.
	 *@return Image the spectrum image in this product file.
	 */
	public Image getSpectraImage(){
		return null;
	}



	/* ######################################################
	 * 
	 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
	 * 
	 *#######################################################*/

	private TreeMap<String, String> attMd5Tree = new TreeMap<String, String>();
	/**


	/**
	 * @param fileName
	 * @param extName
	 * @return
	 * @throws FitsException
	 * @throws IOException
	 * @throws IgnoreException 
	 */
	public int getHeaderNumber(String extName) throws FitsException, IOException, IgnoreException{		
		BasicHDU bHDU=null;
		int i=1;
		while( (bHDU = fitsData.getHDU(i)) != null ) {
			//for(int i=1; i<size && !findGoodExt; i++){	//loop that scan all extension to find the good one
			Header header = bHDU.getHeader();
			Cursor it = header.iterator();
			while(it.hasNext() ){
				HeaderCard hCard = (HeaderCard) it.next();
				String key = hCard.getKey();
				if( key != null && (key.equals("EXTNAME") || key.equals("END")) ){
					String value = hCard.getValue();
					if( value != null && value.equals(extName) ){
						return i;
					}
				}
			}
			i++;
		}
		IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Can't find extension <" + extName + ">");
		return -1;
	}


	/**
	 * @param product
	 * @throws Exception
	 */
	public void setFirstGoodHeader(ProductBuilder product) throws Exception{
		/*
		 * Extension containing ENTRIES are handled by the table product, not by the ENTRY product
		 * Should never occur
		 */
		if( product.getMapping() != null && product.mapping.getCategory() == Category.ENTRY ) {
			return;
		}
		int category = this.getProductCategory();
		/*
		 * MISC products take no extension by default, just the 1HDU
		 */
		if( category == Category.MISC ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "MISC products no extension taken by default, just the 1HDU");
			return;
		}
		if( this.productMap.size() == 1) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mo extension, take just the 1HDU");
			return;
		}
		DataFileExtension dfe=null;
		switch (category) {
		case Category.ENTRY:
		case Category.FLATFILE:
		case Category.MISC:
			return;
		case Category.IMAGE:
			dfe = this.getFirstImageExtension();
			break;
		case Category.SPECTRUM:
			dfe = this.getFirstSpectralExtension();
			break;
		case Category.TABLE:
			dfe = this.getFirstTableExtension();
			break;
		default:
			FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Unknow category" + category);
		}

		if( dfe != null ){
			this.goodHeader = fitsData.getHDU(dfe.tableNum);
			String msg = "Take " + dfe.getSType() + " of HDU# " + dfe.tableNum +  " as data extension for " + Category.explain(category);
			this.extensionSetter = new ExtensionSetter(dfe.tableNum
					, ExtensionSetMode.DETECTED
					, msg);
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, msg);
			return;			
		}
		IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Can't find a " + Category.explain(category) + " header");
	}






	/* (non-Javadoc)
	 * @see saadadb.products.datafile.DataFile#mapAttributeHandler()
	 */
	@Override
	public  void mapAttributeHandler() {
		System.out.println("@@@@@@@@@ Fits mapAttributeHandler");
		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		this.addFirstHDU();
		int cat_prd = -1;
		cat_prd = getProductCategory();

		List<String> kWIgnored = (this.productBuilder.mapping != null)? this.productBuilder.mapping.getIgnoredAttributes()
				: new ArrayList<String>();

		if( this.goodHeader != null && this.goodHeader != this.firstHeader) {
			Cursor it = this.goodHeader.getHeader().iterator();

			while( it.hasNext() ){

				HeaderCard hcard = null;
				hcard = (HeaderCard) it.next();
				AttributeHandler attribute = new AttributeHandler(hcard);
				String name_org = attribute.getNameorg();
				boolean ignore = false;
				for( String ign: kWIgnored) {
					if( name_org.matches(ign)) {
						if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The key : "+name_org+" is ignored (pattern: " + ign + ")");	
						ignore = true;
						break;
					}
				}
				if( ignore || name_org.length() == 0 ) {
					continue;
				}
				/*
				 * We do not store full column description for table
				 * Column description is reported into the entry class
				 */
				else if( cat_prd == Category.TABLE 
						&& (name_org.startsWith("TFORM") || name_org.startsWith("TUNIT") || name_org.startsWith("TDISP") || name_org.startsWith("TRUEN")) ) {
					continue;
				} else {
					/*
					 * attribute read from the Xtension must squash former attribute with the same name
					 * They are not considered as duplicated. (EXTENSION, BITPIX...)
					 */
					this.attributeHandlers.put(attribute.getNameattr(), attribute);
					attribute.setCollname(this.productBuilder.mapping.getCollection());				
					this.attMd5Tree.put(attribute.getNameorg(), attribute.getType());
				}
			}
		}
		System.out.println("@@@@@@@@@ Fits mapAttributeHandler FINI");
	}

	/**
	 * @return
	 */
	public void addFirstHDU() {

		System.out.println(" addFirstHDU" + this.attributeHandlers.size());
		Header header = this.firstHeader.getHeader();
		Cursor it = header.iterator();
		List<String> kWIgnored = (this.productBuilder.mapping != null)?this.productBuilder.mapping.getIgnoredAttributes()
				: new ArrayList<String>();

		while(it.hasNext()){
			HeaderCard hcard = null;
			hcard = (HeaderCard) it.next();

			String key = hcard.getKey().trim();
			if( key.startsWith("COMMENT")) {
				this.comments.add(hcard.getComment());
				continue;
			} 
			AttributeHandler attribute = new AttributeHandler(hcard);
			key = attribute.getNameorg();
			if( attribute.getNameorg().length() == 0 ) {
				continue;
			}
			boolean ignore = false;
			for( String ign: kWIgnored) {
				if( key.matches(ign)) {
					if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The key : "+key+" is ignored (pattern: " + ign + ")");	
					ignore = true;
					break;
				}
			}
			if( ignore  ){
				continue;
			}
			//if the key is not the last one nor an ignored one nor a FITS comment then we can create and add an attributeHandler corresponding
			else  {

				//Sets this standardized name of this attribute to this field in the modelling object
				String value = hcard.getValue();
				attribute.setValue(value);

				String keyChanged = ChangeKey.renameDuplicateKey(this.attributeHandlers, attribute.getNameattr());
				if( !keyChanged.equals(attribute.getNameattr())) {
					attribute.setNameattr(keyChanged);
				}
				this.attributeHandlers.put(keyChanged, attribute);
				if( this.productBuilder.mapping != null )
					attribute.setCollname(this.productBuilder.mapping.getCollection());				
				this.attMd5Tree.put(attribute.getNameorg(), attribute.getType());
			}
		}
		//this.attMd5Tree.put(md5Key, md5Type);
	}


	/**
	 * @return Returns the good_header.
	 */
	public BasicHDU getGood_header() {
		return goodHeader;
	}

	/**
	 * @return Returns the good_header_number.
	 */
	public int getGood_header_number() {
		return (this.extensionSetter != null)?this.extensionSetter.goodHeaderNumber: 0;
	}

	/**
	 * @param hdu
	 * @return
	 */
	static public final boolean isImage(BasicHDU hdu) {
		if( hdu.getClass().getName().equals("nom.tam.fits.ImageHDU") ) {
			int[] size;
			try {
				size = ((ImageHDU)(hdu)).getAxes();
				if( size != null &&  size.length >= 1 && size[0] > 0  ) {
					return true;
				}
			} catch (FitsException e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * @param hdu
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	static public final boolean isTileCompressedImage(BasicHDU hdu) {
		if( hdu.getClass().getName().equals("nom.tam.fits.BinaryTableHDU") ) {
			Iterator it = hdu.getHeader().iterator();
			while( it.hasNext()) {
				HeaderCard hcard = null;
				hcard = (HeaderCard) it.next();
				AttributeHandler attribute = new AttributeHandler(hcard);
				if( attribute.getNameorg().equals("ZIMAGE") && attribute.getValue().toString().equals("true"))
					return true;
			}
		}
		return false;
	}
	/**
	 * @param hdu
	 * @return
	 */
	static public final boolean isTable(BasicHDU hdu) {
		return (isASCIITable(hdu) | isBinTable(hdu));
	}
	/**
	 * @param hdu
	 * @return
	 */
	static public final boolean isASCIITable(BasicHDU hdu) {
		return hdu.getClass().getName().equals("nom.tam.fits.AsciiTableHDU");
	}
	/**
	 * @param hdu
	 * @return
	 */
	static public final boolean isBinTable(BasicHDU hdu) {
		return hdu.getClass().getName().equals("nom.tam.fits.BinaryTableHDU");
	}	

	/**
	 * @return
	 * @throws IgnoreException 
	 * @throws FitsException 
	 */
	public int[] getImageSize() throws IgnoreException, FitsException {
		int[] retour = new int[2];
		if( FitsDataFile.isImage(this.goodHeader)) {
			int[] size=  ((ImageHDU)this.goodHeader).getAxes();				
			retour[0] = size[size.length - 1];
			retour[1] = size[size.length - 2];
			return retour;
		}
		else if( FitsDataFile.isTileCompressedImage(this.goodHeader) ) {
			BasicHDU imghdu = (BasicHDU)this.goodHeader;
			retour[0] = imghdu.getHeader().getIntValue("ZNAXIS1");
			retour[1] = imghdu.getHeader().getIntValue("ZNAXIS2");
			return retour;
		}
		else {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Unknown image format");
			return null;
		}
	}

	public int getBitPIx() throws FitsException, IgnoreException {
		if( FitsDataFile.isImage(this.goodHeader)) {
			ImageHDU himage = ((ImageHDU)this.goodHeader);
			return  himage.getBitPix() ;
		}
		else if( FitsDataFile.isTileCompressedImage(this.goodHeader) ) {
			return  ((BasicHDU)this.goodHeader).getHeader().getIntValue("ZBITPIX");
		}
		else {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Unknown image format" );
			return SaadaConstant.INT;
		}

	}

	/**
	 * @param corner
	 * @param size
	 * @return
	 * @throws Exception
	 */
	public Object getImagePixels(int[] corner, int[] size) throws Exception{
		if( FitsDataFile.isImage(this.goodHeader)) {
			ImageHDU himage = ((ImageHDU)this.goodHeader);
			return   himage.getTiler().getTile(corner, size);
		}
		else if( FitsDataFile.isTileCompressedImage(this.goodHeader) ) {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not generate vignette fo tile compressed images" );
		}
		else {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not generate vignette: HEADER type not recognized" );			
		}
		return null;
	}
	/**
	 * @return
	 * @throws Exception
	 */
	public Object getImagePixels() throws Exception {
		int size[] ;
		int ww, hh;
		if( FitsDataFile.isImage(this.goodHeader)) {
			ImageHDU himage = ((ImageHDU)this.goodHeader);
			size = himage.getAxes();
			ww = size[size.length - 1];
			hh = size[size.length - 2];
			return himage.getTiler().getTile(new int[size.length], size);
		}
		else if( FitsDataFile.isTileCompressedImage(this.goodHeader) ) {
			BasicHDU imghdu = (BasicHDU)this.goodHeader;
			int tile = imghdu.getHeader().getIntValue("ZTILE1");
			size = this.getImageSize();
			ww = size[0];
			hh = size[1];
			int nbit = this.getBitPIx();
			int npix  = Math.abs(nbit)/8;    // Nombre d'octets par valeur
			int nnaxis2 = imghdu.getHeader().getIntValue("NAXIS2");
			int pcount=imghdu.getHeader().getIntValue("PCOUNT");    // nombres d'octets a lire en tout

			int taille=ww*hh*npix;    // Nombre d'octets
			byte[] byte_pixels = new byte[taille];
			byte [] table = new byte[nnaxis2*4*2];
			byte [] buf = new byte[pcount];
			int offset=0;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			this.goodHeader.getData().write(new BufferedDataOutputStream(baos));
			table = baos.toByteArray();

			for( int i=0 ; i<pcount ; i++ ) {
				buf[i] = table[nnaxis2*8 + i];
			}
			offset = 0;
			for( int row=0; row<nnaxis2; row++ ) {
				int pos = TileRiceDecompressor.getInt(table,row*8+4);
				TileRiceDecompressor.decomp(buf,pos,(byte[]) byte_pixels,offset,tile,32,nbit);
				offset+=tile;
			}

			/*
			 * Convert the byte array to a int array (works with bitpix=32)
			 */
			nbit=8;
			int[] img_pixel = new int[ww*hh];
			byte[] tmp = (byte[])byte_pixels;
			int pos = 0;
			int p=0;
			for( int h=0 ; h<hh ; h ++ ) {
				for( int w=0 ; w<ww ; w ++ ) {
					img_pixel[pos] = TileRiceDecompressor.getInt(tmp, p);
					p += 4;
					pos++;
				}
			}
			return img_pixel;
		}
		else {
			IgnoreException.throwNewException(SaadaException.UNSUPPORTED_OPERATION, "Can not generate vignette: HEADER type not recognized" );
			return null;
		}

	}

	/****************************
	 * Interface Implementation
	 ****************************/

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getColumnValues(java.lang.String)
	 */
	public Object[] getExtrema(String key)  {
		try {
			/*
			 * This method can be called by Spectrum findSpectralCoordinateInPixels before the spectrum extension is found
			 */
			if( goodHeader == null ) {
				return null;
			}
			/*
			 * Spectral data can be stored in image pixels. In this case extrem values are given by
			 * number of pixels in the first dimension
			 */
			if( goodHeader instanceof nom.tam.fits.ImageHDU ) {
				if( ((ImageHDU)goodHeader).getAxes().length == 1 ) {
					return new Double[]{0.0, (double)(((ImageHDU)goodHeader).getAxes()[0]), (double)(((ImageHDU)goodHeader).getAxes()[0])};					
				}
				/*
				 * The largest size is supposed to contain data
				 */
				else {
					double min1 = (double)(((ImageHDU)goodHeader).getAxes()[0]);
					double min2 = (double)(((ImageHDU)goodHeader).getAxes()[1]);
					if( min1 > min2 ) {
						return new Double[]{0.0, min1, min1};						
					} else {
						return new Double[]{0.0, min2, min2};						
					}
				}
			}
			/*
			 * key is null when extrema are taken from image pixel (test above)
			 */
			if( key == null ) {
				return null;
			}
			Object o;
			Double[] retour = new Double[3];
			initEnumeration();
			o =  tableEnumeration.getColumn(key);
			/*
			 * Simplest case: the column is a value array
			 */
			String column_class = o.getClass().getName();
			if( column_class.matches("\\[[\\w]+") ) {
				if( column_class.equals("[F") ) {
					float too[] = (float[])(o);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)(too[0]);
							retour[1] = (double)(too[0]);
						}
						double val = (double)(too[i]);
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				}
				else if( column_class.equals("[D") ) {
					double too[] = (double[])(o);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = too[0];
							retour[1] = too[0];
						}
						double val = too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				}
				else if( column_class.equals("[I") ) {
					int too[] = (int[])(o);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)too[0];
							retour[1] = (double)too[0];
						}
						double val = (double)too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				}
				else if( column_class.equals("[S") ) {
					short too[] = (short[])(o);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)too[0];
							retour[1] = (double)too[0];
						}
						double val = (double)too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				}
				else if( column_class.equals("[java.lang.String") ) {
					String too[] = (String[])(o);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = Double.parseDouble(too[0]);
							retour[1] = Double.parseDouble(too[0]);
						}
						double val = Double.parseDouble(too[i]);
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				}
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Extrema: " + retour[0] + " " + retour[1]);
				return retour;
			}
			/*
			 * Otherwise, the column is modeled as an array of arrays.
			 */
			Object to[] = (Object[])o;
			/*
			 * First case: the column contains one cell with a vector of values
			 * Atomic types are processed separately in order to avoid heavy casts.
			 */
			if( to.length == 1 ) {
				if( to[0].getClass().getName().equals("[F") ) {
					float too[] = (float[])(to[0]);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)(too[0]);
							retour[1] = (double)(too[0]);
						}
						double val = (double)(too[i]);
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				} else if( to[0].getClass().getName().equals("[D") ) {
					double too[] = (double[])(to[0]);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = too[0];
							retour[1] = too[0];
						}
						double val = too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				} else if( to[0].getClass().getName().equals("[I") ) {
					int too[] = (int[])(to[0]);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)too[0];
							retour[1] = (double)too[0];
						}
						double val = (double)too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				} else if( to[0].getClass().getName().equals("[S") ) {
					short too[] = (short[])(to[0]);					
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							retour[0] = (double)too[0];
							retour[1] = (double)too[0];
						}
						double val = (double)too[i];
						if( val < retour[0] ) {
							retour[0] = val;
						}
						if( val > retour[1] ) {
							retour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				} else if( to[0].getClass().getName().equals("[java.lang.String") ) {
					String too[] = (String[])(to[0]);	
					String[] sretour = new String[3];
					for( int i=0 ; i<too.length ; i++ ) {
						if( i == 0 ) {
							sretour[0] = too[0];
							sretour[1] = too[0];
						}
						String val = too[i];
						if( sretour[0].compareTo(val) < 0) {
							sretour[0] = val;
						}
						if( sretour[1].compareTo(val) < 0 ) {
							sretour[1] = val;
						}						
					}
					retour[2] = (double) too.length;
				} else {
					IgnoreException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Type " + to[0].getClass().getName() + " not suported to read column extrema");
					return null;
				}
			}
			/*
			 * Second case: the column contains N vectors of M values 
			 * We take then the first of the M values of individuals vectors
			 */
			else {
				for( int i=0 ; i<to.length ; i++ ) {
					double val = 0;
					if( to[0].getClass().getName().equals("[F") ) {
						val = ((float[])(to[i]))[0];
					} else if( to[0].getClass().getName().equals("[D") ) {
						val = ((double[])(to[i]))[0];
					} else if( to[0].getClass().getName().equals("[I") ) {
						val = ((int[])(to[i]))[0];
					} else if( to[0].getClass().getName().equals("[java.lang.String") ) {
						val = Double.parseDouble(((String[])(to[i]))[0]);
					} else {
						IgnoreException.throwNewException(SaadaException.UNSUPPORTED_TYPE, "Type " + to[0].getClass().getName() + " not suported to read column extrema");
						return null;
					}
					if( i == 0 ) {
						retour[0] = val;
						retour[1] = val;
					}
					if( val < retour[0] ) {
						retour[0] = val;
					}
					if( val > retour[1] ) {
						retour[1] = val;
					}						
					retour[2] = (double) ((Object[])(to[i])).length;
				}				
			}
			return retour;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			Messenger.printMsg(Messenger.ERROR, e.getMessage());
			return null;
		}
	}
	/**In case of the product can have table:
	 * Returns the row number in the table.
	 *@return int The row number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public int getNRows() throws IgnoreException{
		try {
			return nb_rows;
		} catch(Exception e) {
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return SaadaConstant.INT;
		}
	}
	/**In case of the product can have table:
	 * Returns the column number in the table.
	 *@return int The column number in the table.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public int getNCols() throws IgnoreException{
		try {
			//The initilization in this method allows not to overload the computer memory and to provoke an memory exception
			//by modelling wrongly these data (as in the global header).
			return ((TableHDU)this.fitsData.getHDU(1)).getNCols();
		} catch(Exception e) {
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return SaadaConstant.INT;
		}
	}
	/**In case of the product can have table:
	 * Initializes the enumeration of table rows (essential in stream mode).
	 * This method is necessary in the class Product (package saadadb.products) for return a initialized enumeration:
	 * See method elements() in class Product (she returns a Enumeration).
	 * This method models the TableHDU in memory (the first HDU (in the current Fits file) corresponding to the first Table HDU).
	 */
	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#initEnumeration()
	 */
	public void initEnumeration() throws IgnoreException{
		try {
			//Initializes the current index of the table
			nextIndex = 0;
			//Initializes the TableHDU for the enumeration:
			//The first HDU (in the current Fits file) corresponding to the first Table HDU
			tableEnumeration = (TableHDU)this.fitsData.getHDU(this.extensionSetter.goodHeaderNumber);
			nb_rows = ((TableHDU)this.fitsData.getHDU(this.extensionSetter.goodHeaderNumber)).getNRows();
			/*
			 * Format of all columns are stored to do correct casting when reading lines
			 * 		Binary		ASCII
			 * A	String		String
			 * L	Boolean
			 * X	Bit
			 * B 	Byte
			 * I 	Short		Integer
			 * J	Int
			 * K	Long
			 * E	FLoat		Float
			 * F				Float
			 * D	Double		Double
			 * C	Complex
			 * M	Comp Double
			 */
			TableHDU table = (TableHDU)(this.goodHeader);
			colform = new int[table.getNCols()];
			boolean isascii = isASCIITable(table);
			for(int j = 0; j < table.getNCols(); j++){
				String format = table.getColumnFormat(j);
				colform[j] = JavaTypeUtility.convertFitsFormatToJavaType(format, isascii);

				if(  colform[j] == JavaTypeUtility.UNSUPPORTED ) {
					Messenger.printMsg(Messenger.WARNING, "FITS Type <" + format + "> unsupported by Saada: set values as NULL");
				}	
				//System.out.println("@@@@ " + j + " " + format + " " + colform[j]);
			}

		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
		}

	}
	/**Returns the value corresponding finded in the product file to the key word in parameter.
	 *@param String The key word.
	 *@return String The value corresponding to this key word, if he exists, else null.
	 */
	public String getKWValueQuickly(String key){
		HeaderCard card;
		if( (card = this.firstHeader.getHeader().findCard(key)) != null ) {
			return card.getValue();			
		}
		else  if( this.goodHeader != null && (card = this.goodHeader.getHeader().findCard(key)) != null ) {
			return card.getValue();
		}
		/*
		 * Takes FK5 by default
		 */
		else if( key.equals("SYSTEM") ) {
			return "FK5";
		}
		/*
		 * Attemp to guest the equinox from RA/DEC keywords
		 * and takes J2000 by default
		 */
		else if ( key.equals("EQUINOX") ){
			if( ra.matches("RA.?(2000)?") )
				return "J2000";
			else if( ra.matches("RA.?(1950)?") )
				return "J1950";
			else
				return "J2000";
		}
		return null;
	}




	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#getMap()
	 */
	@Override
	public Map<String, DataFileExtension> getProductMap() throws Exception {
		if( this.productMap == null ) {
			if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Build product map");
			this.productMap = new LinkedHashMap<String, DataFileExtension>();
			BasicHDU bHDU = null;
			/*
			 * Some file crash javafits when running the while loop
			 */
			for( int i=0 ; i<=fitsData.getNumberOfHDUs() ; i++ ) {
				//while( (bHDU = fits_data.getHDU(i))  != null) {
				bHDU = fitsData.getHDU(i);
				if( bHDU == null ) {
					break;
					//IgnoreException.throwNewException(SaadaException.FITS_FORMAT, "Cannnot read FITS header");
				}
				ArrayList<AttributeHandler> attrs = new ArrayList<AttributeHandler>();
				DataFileExtensionType ext_type = DataFileExtensionType.BASIC;
				String ext_name = "primary";
				this.goodHeader = bHDU;
				Iterator it = this.goodHeader.getHeader().iterator();
				if( isTileCompressedImage(this.goodHeader)) {
					ext_type = DataFileExtensionType.TILE_COMPRESSED_IMAGE;
				} else if( isBinTable(this.goodHeader)) {
					ext_type = DataFileExtensionType.BINTABLE;
				} else if( isASCIITable(this.goodHeader)) {
					ext_type = DataFileExtensionType.ASCIITABLE;
				} else if( isImage(this.goodHeader)) {
					ext_type = DataFileExtensionType.IMAGE;
				}
				while( it.hasNext()) {
					HeaderCard hcard = null;
					hcard = (HeaderCard) it.next();
					AttributeHandler attribute = new AttributeHandler(hcard);
					String name_org = attribute.getNameorg();

					if (name_org.length() == 0 || name_org.startsWith("TFORM") || 
							name_org.startsWith("TUNIT") || name_org.startsWith("TDISP") || 
							name_org.startsWith("TRUEN") ) {
						continue;
					} else if( name_org.equals("EXTNAME") ) {
						ext_name = attribute.getValue();
					}
					attrs.add(attribute);
				}
				this.productMap.put("#" + i + " " + ext_name + " (" + ext_type + ")", new DataFileExtension(i, ext_name, ext_type, bHDU,attrs));
				/*
				 * In case of table, the column meta data are stored into a specific DataFileExtension of type TABLE_COLUMNS
				 */
				if( ext_type == DataFileExtensionType.BINTABLE || ext_type  == DataFileExtensionType.ASCIITABLE ) {
					Map<String, AttributeHandler> tahe = new LinkedHashMap<String, AttributeHandler>();
					//tahe = this.getEntryAttributeHandler();
					tahe = this.getEntryAttributeHandler(i);
					attrs = new ArrayList<AttributeHandler>(tahe.values());					
					this.productMap.put("#" + i + " " + ext_name + " (" + ext_type + " COLUMNS)"
							, new DataFileExtension(i, ext_name, DataFileExtensionType.TABLE_COLUMNS, bHDU, attrs));
				}
				//i++;			
			}
		}
		return this.productMap;
	}


	/* (non-Javadoc)
	 * @see saadadb.products.DataFile#reportOnLoadedExtension()
	 */
	public List<ExtensionSetter> reportOnLoadedExtension() {
		List<ExtensionSetter> retour = new ArrayList<ExtensionSetter>();
		if( this.extensionSetter != null )retour.add(this.extensionSetter);
		return retour;
	}

	@Override
	public String getCanonicalPath() throws IOException {
		return this.file.getCanonicalPath();
	}
	@Override
	public String getAbsolutePath() {
		return this.file.getAbsolutePath();
	}
	@Override
	public String getParent() {
		return  this.file.getParent();
	}
	@Override
	public long length() {
		return this.file.length();
	}
	@Override
	public boolean delete() {
		return this.file.delete();
	}
	@Override
	public String getName() {
		return this.file.getName();
	}

}


