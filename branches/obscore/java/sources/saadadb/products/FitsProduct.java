package saadadb.products;

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
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.EnergyKWDetector;
import saadadb.products.inference.KWDetector;
import saadadb.products.inference.ObservationKWDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.products.inference.TimeKWDetector;
import saadadb.util.ChangeKey;
import saadadb.util.JavaTypeUtility;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.util.TileRiceDecompressor;

/**
 * @author laurent MICHEL
 *  * @version $Id$

 * 06/2011: Returns table rows a an array with the right type taken from the TFORM 
 *          keyword instead of using the reflexion of on data itself (fails on NULL value)
 * 02/2014  Make it working with a {@link Product} without configuaration, so that the code 
 *          can be used out of a DB context
 */

public class FitsProduct extends File implements ProductFile{

	private static final long serialVersionUID = 1L;

	private TableHDU tableEnumeration;
	private int nextIndex = 0;
	private int nb_rows = 0;

	protected Fits fits_data=null;
	protected int good_header_number;
	protected BasicHDU good_header;
	protected BasicHDU first_header;

	private String ra="";

	private String fmtsignature;
	private Product product;
	protected SpaceKWDetector space_frame;
	private Map<String, AttributeHandler> attributeHandlers = null;
	private Map<String, AttributeHandler> entryAttributeHandlers = null;

	private int[] colform ;
	
	/**Constructor (constructor of the super class "File").
	 * Creates a new File instance by converting the given pathname string into an abstract patname.
	 *@param String The name file of the product.
	 * @throws FitsException 
	 */
	public FitsProduct(String name, Product product) throws Exception{

		//See the super class "File"(package java.io)
		super(name);
		//Initialzes the current file name
		this.product = product;
		this.fits_data = new Fits(getCanonicalPath()); 
	}

	public void closeStream() {
		if( this.fits_data != null && this.fits_data.getStream() != null) {
			try {
				this.fits_data.getStream().close();
			} catch (IOException e) {
				Messenger.printMsg(Messenger.WARNING, "Closing stream of " + this.product.getName() +  " " + e.getMessage());
			}
		}
	}


	/**In case of the product can have table:
	 * This method is used for the valdation of the product by this configuration.
	 * Returns the list which maps entry names not formated (keys) to their position number in the table (values).
	 * This list is maked with the n'th HDU in parameter.
	 * Attention: In the case of entries, their header in tables product can be different from standards of the other products.
	 *@param numHDU The n'th HDU.
	 *@return Hashtable The list which maps entry names to their position number in the table.
	 */
	public Map<String, Integer> getTableEntry(){

		//The HDU with index 1 coorresponding to the Object TableHDU
		TableHDU table = null;
		//Initialize the list which maps entry names no formated to their position number in the table.
		LinkedHashMap<String, Integer>entry = new LinkedHashMap<String, Integer>();
		//Returns the first HDU (in the current Fits file) corresponding to the first Table HDU
		//The initilization in this method allows not to overload the computer memory and to provoke an memory exception
		//by modelling wrongly these data (as in the global header).
		//This allows the load of a very big number of entry (but this little to the detriment of the load performance).
		table = (TableHDU)(this.good_header);

		//Initializes the header with the associated header of this table HDU
		Header tableHeader = table.getHeader();
		//Foreach columns, maps the column name to this index.
		//In a table header, the column name is described by the card associated with the given key: "TTYPEXX" (XX equals the column index).
		//Attention: the column index in the given key begins to "1", and the iteration with the method getNCols() begins to "0".
		//The index maximum of the key equals the number maximum returned by the "getNCols()" method add to one.
		for(int j = 0; j < table.getNCols(); j++){
			entry.put(tableHeader.findCard("TTYPE"+(j+1)).getValue().trim(), new Integer(j));
		}
		return entry;
	}

	/* (non-Javadoc)
	 * @see saadadb.products.ProductFile#getKWEntry(java.util.LinkedHashMap)
	 */
	public void mapEntryAttributeHandler() throws IgnoreException {
		this.entryAttributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		if(  !(this.good_header instanceof  nom.tam.fits.TableHDU) ){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Can not get column names for a " + this.good_header.getClass().getName());
			return ;  		
		}
		try {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Read the meta data of the table columns ");
			/*
			 * product can be null when used to build a product map
			 */
			EntryMapping entryconf = null;
			List<String> kWIgnored = null;
			/*
			 * kWIgnored are porcessed in the case of a table loading but the method can bu called in others contexts such as the
			 * Spectrum coordinate detection
			 */
			if( this.product != null && this.product.mapping.getCategory() == Category.TABLE  ) {
				entryconf = this.product.mapping.getEntryMapping();
				kWIgnored = entryconf.getIgnoredAttributes();
			}
			//Creates the new list which maps entry names formated in the standard of Saada to their objects modelling entry informations
			AttributeHandler attribute;
			//Creates a sorted list for the md5 calculation
			String keyChanged = "";
			//The HDU with index 1 coorresponding to the Object TableHDU
			TableHDU table = (TableHDU)(this.good_header);
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
				if( this.product != null)
					attribute.setCollname(this.product.mapping.getCollection());
				//Sets the unit of this entry to this field in the attribute object
				attribute.setUnit(unitValue);
				if(!unitComment.equals("no Comment")){
					attribute.setComment(unitComment);
				}
				//Initialzes the reality format of this entry
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
			return ((TableHDU)this.fits_data.getHDU(numHDU)).getRow(index);
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
		return ((TableHDU)this.fits_data.getHDU(numHDU)).getNCols();

	}
	/**Tests if the product files contains the key word in parameter.
	 *@param String The key word.
	 *@return boolean true or false, if the product file contains this key word, or not.
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public boolean hasValuedKW(String key) throws FitsException, IOException{
		return this.fits_data.readHDU().getHeader().containsKey(key);
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
		ImageHDU himage = (ImageHDU)this.fits_data.getHDU(this.product.mapping.getHeaderRef().getNumber());
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
	 * @param product
	 * @param mapping
	 * @param tabArg
	 * @throws IOException 
	 * @throws FitsException 
	 * @throws SaadaException 
	 * @throws SaadaException 
	 * @throws SaadaException 
	 */
	public FitsProduct(Product product) throws Exception {

		super(product.file.getAbsolutePath());
		this.product = product;
		this.fits_data = new Fits(product.file.getCanonicalPath());
		try {
			this.first_header = fits_data.getHDU(0);
		} catch (PaddingException e) {
			fits_data.addHDU(e.getTruncatedHDU());
			this.first_header = fits_data.getHDU(0);
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
			if( ext_num >= 0  && (bHDU = fits_data.getHDU(ext_num)) != null ) {
				if( this.checkExtensionCategory(bHDU, this.getFITSExtensionCategory() )) {
					this.good_header_number = ext_num;
					this.good_header = bHDU;
					Messenger.printMsg(Messenger.TRACE, "Required extension : "+product.mapping.getExtension()+" found (number: " + this.good_header_number + ")" );
				}
				/*
				 * If no BINTABLE has been found for spectra, we try to find out a one-row image
				 */
				else if( product.getMapping().getCategory() == Category.SPECTRUM ) {
					if( (bHDU = fits_data.getHDU(ext_num))  != null &&  checkExtensionCategory(bHDU, "IMAGE") ) {
						//if( image.getAxes().length == 1 ) {
						this.good_header = bHDU;
						this.good_header_number = ext_num;
						Messenger.printMsg(Messenger.TRACE, "Take pixels of image HDU# " + ext_num +  " as spectral chanels");
						//return;
						//}
					} else {
						IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE, "Required extension : "+product.mapping.getExtension() + " has a wrong type: " + bHDU.getClass().getName());
					}
				} else {
					IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE, "Required extension : "+product.mapping.getExtension() + " has a wrong type");
				}
			} else {
				IgnoreException.throwNewException(SaadaException.WRONG_RESOURCE, "Required extension : "+product.mapping.getExtension() + " not found");
			}
		} else {										//else, the extension name is undefined
			this.setFirstGoodHeader(product);
			Messenger.printMsg(Messenger.TRACE, "The first extension possibly a(n) "
					//+  Category.explain(product.getConfiguration().getCategorySaada())
					+ this.product.getClass().getName()
					+ " is #"+this.getGood_header_number());
		}
		if( this.product.mapping != null )
		this.product.mapping.getHeaderRef().setNumber(this.getGood_header_number());	
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Creation of the tableAttributHandler...");
		this.product.productAttributeHandler = this.getAttributeHandler();
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The tableAttributeHandler is OK.");
	}

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
		while( (bHDU = fits_data.getHDU(i)) != null ) {
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
	 * @return
	 * @throws IOException 
	 * @throws FitsException 
	 * @throws SaadaException 
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public void setFirstGoodHeader(Product product) throws SaadaException, FitsException, IOException{
		/*
		 * Extension containing ENTRIES are handled by the table product, not by the ENTRY product
		 * Should never occur
		 */
		if( product.getMapping() != null && product.mapping.getCategory() == Category.ENTRY ) {
			return;
		}
		String category = this.getFITSExtensionCategory();
		/*
		 * MISC product take no extension by default, just the 1HDU
		 */
		if( category.length() == 0 ) {
			return;
		}
		/*
		 * Modified to look for data extensions in the 1st HDU too
		 * where is no XTENSION keyword
		 */
		/*
		 * It FITS file is too long, Header.read() considers that a new extension begins, but it finds no key (null) there: IOException
		 * The same exception is rose if a new extension starts there but not starting with  XTENSION or SIMPLE
		 */
		int i=0;
		BasicHDU bHDU = null;
		try {
			while( (bHDU = fits_data.getHDU(i))  != null) {
				if( checkExtensionCategory(bHDU, category) ) {
					this.good_header = bHDU;
					this.good_header_number = i;	
					return;
				}
				i++;	
			}
		} catch(IOException ioe) {
			Messenger.printMsg(Messenger.WARNING, "Bad format for extension # " + i + ": Too many pixels or extension starting without XTENSION or SIMPLE");

		} catch(Exception oe) {
			if( i > 0 ) {
				Messenger.printMsg(Messenger.WARNING, "Bad format for extension # " + i + ": Stop to read file");
			}
			else {
				IgnoreException.throwNewException(SaadaException.FILE_FORMAT, oe);
			}
		}
		/*
		 * If no BINTABLE has been found for spectra, we try to find out a one-row image
		 */
		if( category.equals(Category.explain(Category.SPECTRUM)) ) {
			i=0;
			bHDU = null;
			while( (bHDU = fits_data.getHDU(i))  != null) {
				if( checkExtensionCategory(bHDU, "IMAGE") ) {
					ImageHDU image = (ImageHDU)bHDU;
					if( image.getAxes().length >= 1 ) {
						this.good_header = bHDU;
						this.good_header_number = i;
						Messenger.printMsg(Messenger.TRACE, "Take pixels of image HDU#" + i + " as spectral chanels");
						return;
					}
				}
				i++;
			}
		}
		IgnoreException.throwNewException(SaadaException.MISSING_RESOURCE, "Can't find a " + category + " header");
	}

	/**
	 * Return the category of the FIST extension matching the product category
	 * @return
	 * @throws FatalException 
	 */
	private String getFITSExtensionCategory() {
		try {
		if( product.getMapping() != null) {
			return Category.explain(product.mapping.getCategory());
		} else if( product instanceof Image2D) {
			return Category.explain(Category.IMAGE);
		} else if( product instanceof Spectrum) {
			return Category.explain(Category.SPECTRUM);
		}  else if( product instanceof Table) {
			return Category.explain(Category.TABLE);
		} else {
			return Category.explain(Category.MISC);
		}
		} catch(FatalException e){
			return null;
		}

	}

	/**
	 * Returns true if bHDU matches the data category (ASCIITABLE, BINTABLE or IMAGE)
	 * @param bHDU
	 * @param category
	 * @return
	 */
	private boolean checkExtensionCategory(BasicHDU hdu, String category) {
		if( hdu   != null) {
			/*
			 * If there is no specified category (BINTABLE or IMAGE) any extension 
			 * can be taken. That is the case for MISC products
			 */
			if( category == null || category.equals("") ) {
				return true;
			}
			/*
			 * 1st HDU can be seen as a 0x0 pixel image: Must look for the first not empty image
			 */
			else if( category.equals("IMAGE") && (isImage(hdu) || isTileCompressedImage(hdu)) ){
				return true;
			}					
			else if( category.endsWith("TABLE") && (isBinTable(hdu) || isASCIITable(hdu)) ){
				return true;
			}
			/*
			 * With V2 datamodel, the dataloader must be enabled to detect the energy range even for misc
			 */
			else if( (category.endsWith("SPECTRUM") ||category.endsWith("MISC")  ) && (isBinTable(hdu) || isASCIITable(hdu))) {
				return true;
			}
		}
		return false;		
	}
	/**
	 * @param fileName
	 * @param headerNumber
	 * @param mapping
	 * @throws FatalException 
	 */
	private void mapAttributeHandler() {
		this.attributeHandlers = new LinkedHashMap<String, AttributeHandler>();
		/*boolean findRA = false;
		 boolean findDEC = false;*/
		this.addFirstHDU();
		int cat_prd = -1;
		try {
			cat_prd = Category.getCategory(getFITSExtensionCategory());
		} catch (FatalException e) {}
		
		List<String> kWIgnored = (this.product.mapping != null)? this.product.mapping.getIgnoredAttributes()
				: new ArrayList<String>();

		if( this.good_header != null && this.good_header != this.first_header) {
			Cursor it = this.good_header.getHeader().iterator();

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
				 * We do not store full coloumn description for table
				 * The the column name.
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
					attribute.setCollname(this.product.mapping.getCollection());				
					this.attMd5Tree.put(attribute.getNameorg(), attribute.getType());
				}
			}
		}
	}

	/**
	 * @return
	 */
	public void addFirstHDU() {

		Header header = this.first_header.getHeader();
		Cursor it = header.iterator();
		List<String> kWIgnored = (this.product.mapping != null)?this.product.mapping.getIgnoredAttributes()
				: new ArrayList<String>();

		while(it.hasNext()){
			HeaderCard hcard = null;
			hcard = (HeaderCard) it.next();

			String key = hcard.getKey().trim();
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
				if( this.product.mapping != null )
					attribute.setCollname(this.product.mapping.getCollection());				
				this.attMd5Tree.put(attribute.getNameorg(), attribute.getType());
			}
			//this.attMd5Tree.put(md5Key, md5Type);
		}
	}

	/**
	 * @return Returns the good_header.
	 */
	public BasicHDU getGood_header() {
		return good_header;
	}

	/**
	 * @return Returns the good_header_number.
	 */
	public int getGood_header_number() {
		return good_header_number;
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
		if( FitsProduct.isImage(this.good_header)) {
			int[] size=  ((ImageHDU)this.good_header).getAxes();				
			retour[0] = size[size.length - 1];
			retour[1] = size[size.length - 2];
			return retour;
		}
		else if( FitsProduct.isTileCompressedImage(this.good_header) ) {
			BasicHDU imghdu = (BasicHDU)this.good_header;
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
		if( FitsProduct.isImage(this.good_header)) {
			ImageHDU himage = ((ImageHDU)this.good_header);
			return  himage.getBitPix() ;
		}
		else if( FitsProduct.isTileCompressedImage(this.good_header) ) {
			return  ((BasicHDU)this.good_header).getHeader().getIntValue("ZBITPIX");
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
		if( FitsProduct.isImage(this.good_header)) {
			ImageHDU himage = ((ImageHDU)this.good_header);
			return   himage.getTiler().getTile(corner, size);
		}
		else if( FitsProduct.isTileCompressedImage(this.good_header) ) {
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
		if( FitsProduct.isImage(this.good_header)) {
			ImageHDU himage = ((ImageHDU)this.good_header);
			size = himage.getAxes();
			ww = size[size.length - 1];
			hh = size[size.length - 2];
			return himage.getTiler().getTile(new int[size.length], size);
		}
		else if( FitsProduct.isTileCompressedImage(this.good_header) ) {
			BasicHDU imghdu = (BasicHDU)this.good_header;
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
			this.good_header.getData().write(new BufferedDataOutputStream(baos));
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
	public double[] getExtrema(String key)  {
		try {
			/*
			 * This method can be called by Spectrum findSpectralCoordinateInPixels before the spectrum extension is found
			 */
			if( good_header == null ) {
				return null;
			}
			/*
			 * Spectral data can be stored in image pixels. In this case extrem values are given by
			 * nhumber of pixels in the first dimension
			 */
			if( good_header.getClass().getName().equals("nom.tam.fits.ImageHDU")) {
				if( ((ImageHDU)good_header).getAxes().length == 1 ) {
					return new double[]{0, (double)(((ImageHDU)good_header).getAxes()[0])};					
				}
				/*
				 * The largest size is supposed to contain data
				 */
				else {
					double min1 = (double)(((ImageHDU)good_header).getAxes()[0]);
					double min2 = (double)(((ImageHDU)good_header).getAxes()[1]);
					if( min1 > min2 ) {
						return new double[]{0, min1};						
					}
					else {
						return new double[]{0, min2};						
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
			double[] retour = new double[2];
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
				}
				else if( to[0].getClass().getName().equals("[D") ) {
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
				}
				else if( to[0].getClass().getName().equals("[I") ) {
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
				}
				else if( to[0].getClass().getName().equals("[S") ) {
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
				}
				else if( to[0].getClass().getName().equals("[java.lang.String") ) {
					String too[] = (String[])(to[0]);					
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
				}
				else {
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
					}
					else if( to[0].getClass().getName().equals("[D") ) {
						val = ((double[])(to[i]))[0];
					}
					else if( to[0].getClass().getName().equals("[I") ) {
						val = ((int[])(to[i]))[0];
					}
					else if( to[0].getClass().getName().equals("[java.lang.String") ) {
						val = Double.parseDouble(((String[])(to[i]))[0]);
					}
					else {
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
			return ((TableHDU)this.fits_data.getHDU(1)).getNCols();
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
			tableEnumeration = (TableHDU)this.fits_data.getHDU(this.good_header_number);
			nb_rows = ((TableHDU)this.fits_data.getHDU(this.good_header_number)).getNRows();
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
			TableHDU table = (TableHDU)(this.good_header);
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
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
		}

	}
	/**Returns the value corresponding finded in the product file to the key word in parameter.
	 *@param String The key word.
	 *@return String The value corresponding to this key word, if he exists, else null.
	 */
	public String getKWValueQuickly(String key){
		HeaderCard card;
		if( (card = this.first_header.getHeader().findCard(key)) != null ) {
			return card.getValue();			
		}
		else  if( this.good_header != null && (card = this.good_header.getHeader().findCard(key)) != null ) {
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

	
	public Map<String, AttributeHandler> getEntryAttributeHandler() throws SaadaException {
		if( this.entryAttributeHandlers == null ){
			this.mapEntryAttributeHandler();
		}
		return this.entryAttributeHandlers;
	}

	public Map<String, AttributeHandler> getAttributeHandler() {
		if( this.attributeHandlers == null ){
			this.mapAttributeHandler();
		}
		return this.attributeHandlers;
	}


	public ObservationKWDetector getObservationKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new ObservationKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new ObservationKWDetector(this.getAttributeHandler());
		}		
	}
	public SpaceKWDetector getSpaceKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new SpaceKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new SpaceKWDetector(this.getAttributeHandler());
		}		
	}
	public EnergyKWDetector getEnergyKWDetector(boolean entryMode) throws SaadaException{
		return new EnergyKWDetector(this);		
	}
	public TimeKWDetector getTimeKWDetector(boolean entryMode) throws SaadaException{
		if( entryMode ){
			return  new TimeKWDetector(this.getAttributeHandler(), this.getEntryAttributeHandler());
		} else {
			return new TimeKWDetector(this.getAttributeHandler());
		}		
	}

	/**
	 * Returns a map of the current product
	 * @param category
	 * @return
	 * @throws FitsException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	public LinkedHashMap<String, ArrayList<AttributeHandler>> getProductMap(String category) throws IgnoreException {
		try {
			//int i=0;
			BasicHDU bHDU = null;
			LinkedHashMap<String, ArrayList<AttributeHandler>> retour = new LinkedHashMap<String, ArrayList<AttributeHandler>>();
			/*
			 * Some file crash javafits when running the while loop
			 */
			for( int i=0 ; i<=fits_data.getNumberOfHDUs() ; i++ ) {
				//while( (bHDU = fits_data.getHDU(i))  != null) {
				bHDU = fits_data.getHDU(i);
				if( bHDU == null ) {
					break;
					//IgnoreException.throwNewException(SaadaException.FITS_FORMAT, "Cannnot read FITS header");
				}
				ArrayList<AttributeHandler> attrs = new ArrayList<AttributeHandler>();
				String ext_type = "BASIC";
				String ext_name = "primary";
				if( category == null || checkExtensionCategory(bHDU, category) ) {
					this.good_header = bHDU;
					Iterator it = this.good_header.getHeader().iterator();
					if( isTileCompressedImage(this.good_header)) {
						ext_type = "TILE COMPRESSED IMAGE";
					}
					else if( isBinTable(this.good_header)) {
						ext_type = "BINTABLE";
					}
					else if( isASCIITable(this.good_header)) {
						ext_type = "ASCIITABLE";
					}
					else if( isImage(this.good_header)) {
						ext_type = "IMAGE";
					}
					while( it.hasNext()) {
						HeaderCard hcard = null;
						hcard = (HeaderCard) it.next();
						AttributeHandler attribute = new AttributeHandler(hcard);
						String name_org = attribute.getNameorg();

						if (name_org.length() == 0       || name_org.startsWith("TFORM") || 
								name_org.startsWith("TUNIT") || name_org.startsWith("TDISP") || 
								name_org.startsWith("TRUEN") ) {
							continue;
						}
						//						if( name_org.equals("XTENSION") ) {
						//						ext_type = attribute.getValue();
						//						}
						else if( name_org.equals("EXTNAME") ) {
							ext_name = attribute.getValue();
						}
						attrs.add(attribute);
					}
				}
				retour.put("#" + i + " " + ext_name + " (" + ext_type + ")", attrs);    	
				if( ext_type.equalsIgnoreCase("BINTABLE") ||  ext_type.equalsIgnoreCase("ASCIITABLE")) {
					Map<String, AttributeHandler> tahe = new LinkedHashMap<String, AttributeHandler>();
					tahe = this.getEntryAttributeHandler();
					attrs = new ArrayList<AttributeHandler>(tahe.values());					

					retour.put("#" + i + " " + ext_name + " (" + ext_type + " COLUMNS)", attrs);    	
					//					i++;			
				}
				//i++;			
			}
			return retour;
		} catch(Exception e) {
			Messenger.printStackTrace(e);
			IgnoreException.throwNewException(SaadaException.FITS_FORMAT, e);
			return null;
		}
	}
	


	/**
	 * @param args
	 */
	public static void main(String[] args ) {
		try {
			//			FitsProduct fp = new FitsProduct("/home/michel/Desktop/pop_1_9_kroupa_1e3_Z0.02.fits", null);
			//			FitsProduct fp = new FitsProduct("/home/michel/fuse.fits", null);
			FitsProduct fp = new FitsProduct("/home/michel/Desktop/xe001.fits", null);
			//			FitsProduct fp = new FitsProduct("/home/michel/Desktop/SSA.xml", null);
			//			ImageHDU himage = (ImageHDU)fp.fits_data.getHDU(0);
			//			int[] size = himage.getAxes();
			//			System.out.println(size.length + " " + size[0]);
			//System.exit(1);
			//			ImageHDU bHDU = (ImageHDU) fp.fits_data.getHDU(0);
			//			System.out.println(bHDU.getAxes()[0] + " " + bHDU.isData());
			//		
			//			ImageHDU.
			//			System.out.println(bHDU.getClass().getName());
			//			System.exit(1);
			//			Fits f = new Fits("/home/michel/Desktop/tile_eso.fit");
			//			BinaryTableHDU t = (BinaryTableHDU) f.getHDU(1);
			//			byte[] buf = new byte[4300000];
			//			ArrayDataInput arg0 = new BufferedDataInputStream(new ByteArrayInputStream(buf));
			//			t.readData(arg0);
			//			arg0.close();
			//			for( int i=0 ; i<buf.length ; i++ ) {
			//				if( buf[i] != 0 )
			//					System.out.println(buf[i]);
			//			}
			//			System.exit(1);
			LinkedHashMap<String, ArrayList<AttributeHandler>> retour = fp.getProductMap(null);
			for( String en: retour.keySet() ) {
				System.out.println(en);
				for( AttributeHandler ah: retour.get(en)) {
					System.out.println("   -" + ah.getNameorg() + " = " + ah.getValue());
				}
			}
		} catch (Exception e) {
			Messenger.printStackTrace(e);
		}


	}


}


