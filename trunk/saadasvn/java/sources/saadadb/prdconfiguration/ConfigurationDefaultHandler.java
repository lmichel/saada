package saadadb.prdconfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import nom.tam.fits.FitsException;

import org.xml.sax.helpers.DefaultHandler;

import saadadb.api.SaadaDB;
import saadadb.classmapping.Mapping;
import saadadb.classmapping.TypeMapping;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.products.FlatFile;
import saadadb.products.Image2D;
import saadadb.products.Misc;
import saadadb.products.Product;
import saadadb.products.Spectrum;
import saadadb.products.Table;
import saadadb.util.DefineType;
import saadadb.util.MD5Key;
import saadadb.util.Messenger;
/** * @version $Id$

 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see DefaultHandler
 */
public abstract class ConfigurationDefaultHandler extends DefaultHandler{
	/*
	 * Repository management strategy flags
	 */
	public static int COPY = 0;
	public static int MOVE = 1;
	public static int KEEP = 2;
	
	protected int repository_mode = COPY;
	
		/**Name of this product configuration**/
    protected String nameProduct;
    	/**Configuration type of generated class Saada**/
    protected int categorySaada;
    /*
     * class name or classname prefix
     */
    protected String prdclassname;
     	/**Object containing all mapping information of this configuration**/
    protected Mapping mapping;
    	/**Object table containing all product identifications constraints**/
    protected ProductSignature sign;
    private Content content=null;
    /**Object containing equinox and coordinated system of this configuration**/
    protected CoordSystem coordSystem;
    		/** Reference to the header (FITS) or the table (VOTable) where data mist be taken */
    protected HeaderRef headerRef = new HeaderRef(0);
        	/**Collection name that correspond to this configuration**/
    protected String collectionName = "";
    	/**Configuration type**/
    protected String dataType;
    	/**File configuration XML**/
    //protected File fileConfigXML;
    	/**Element name of the start of this**/
    protected String start = "";
    	/**Element name of the end of this**/
    protected String stop = "";
    	/**Element name**/
    protected String str = "";
    	/**Boolean indicating the recording of the deliberate element**/
    protected boolean recordElement = false;
    	/**Boolean indicating the recording of the deliberate element identificator**/
    protected boolean recordIdentification = false;
    	/**Boolean indicating the recording of the equinox and coordinated system of the deliberate element**/
    protected boolean recordCoordSystem = false;
    	/**Boolean indicating the recording of the deliberate element mapping**/
    protected boolean recordMapping = false;
    	/**Boolean indicating the recording of the file content constraint of the deliberate element**/
    protected boolean recordContent = false;
    	/**Boolean indicating if the selected product is recording in the xml configuration file**/
    protected boolean isRecording = false;
    	/**Configuration algorithmics value with md5**/
    protected String md5 = "";
    	/**String that correspond to configuration algorithmics value with md5**/
    protected String s_md5 = "";
    	/**Algorithmics value of this configuration whithout collection indication with md5**/
    protected String md5_without_col = "";
    	/**String that correspond to Algorithmics value of this configuration whithout collection indication with md5**/
    protected String s_md5_without_col = "";
	protected String extensionName;
	public String loader_param;
	public boolean load_vignette;
    

	/**
	 * @param configName
	 * @param tabArg
	 * @throws FatalException 
	 */
	public ConfigurationDefaultHandler(String configName, ArgsParser tabArg) throws FatalException {

		this.nameProduct = configName;
		this.loader_param = tabArg.toString();
		
		//Initialize the collection's name
		this.collectionName = tabArg.getCollection();
		this.extensionName = tabArg.getExtension();
		this.load_vignette = !tabArg.isNovignette();
		if( this.extensionName == null ) {
			this.extensionName = "";
		}
		//Create and initialize the new object containing all product identifications constraints
		this.sign = new ProductSignature();
		
		//Create the new object containing equinox and coordinated system of this configuration
		this.coordSystem = new CoordSystem();
		this.mapCoordSystem(tabArg);
		this.headerRef.setName(extensionName);			
	}
 
	/**
	 * Map the coordinate system
	 * @param configuration
	 * @param tabArg
	 * @throws ParsingException
	 */
	private void mapCoordSystem(ArgsParser tabArg) throws FatalException {
		/*
		 * Repository mode
		 */
		String rp = tabArg.getRepository();
		if( rp != null ) {
			if( rp.equalsIgnoreCase("no") ) {
				repository_mode = KEEP;
			}
			else if( rp.equalsIgnoreCase("move") ) {
				repository_mode = MOVE;				
			}
			else {
				FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unknown repository mode, must be [no|move]");			
			}
		}
		/*
		 * Coordinate system mapping
		 */
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Searching for the coordinate SYSTEM and EQUINOX");
		
		String[] tabSys_eq ;
		tabSys_eq = tabArg.getCoordinateSystem();
		/*
		 * just a system, no equinox (galact e.g.)
		 */
		if( tabSys_eq.length == 1 ) {
			/*
			 * System can be given as a value enclosed in  "'" 
			 * or as a keyword name
			 */
			this.coordSystem.setEquinox("");
			this.coordSystem.setEquinox_value("");
			if( tabSys_eq[0].startsWith("'") ) {
				this.coordSystem.setSystem_value(tabSys_eq[0].replaceAll("'", ""));
			}
			else {
				this.coordSystem.setSystem(tabSys_eq[0]);
			}
		}
		else if( tabSys_eq.length == 2 ) {
			if( tabSys_eq[0].startsWith("'") ) {
				this.coordSystem.setSystem_value(tabSys_eq[0].replaceAll("'", ""));
			}
			else {
				this.coordSystem.setSystem(tabSys_eq[0]);
			}
			if( tabSys_eq[1].trim().endsWith("'") ) {
				this.coordSystem.setEquinox_value(tabSys_eq[1].replaceAll("'", ""));
			}
			else {
				this.coordSystem.setEquinox(tabSys_eq[1]);
			}
		}
		else{	
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "You didn't specify a coordinate system. The loader'll try to detect them in the file.");
			this.coordSystem.setAutodedect();
		}
		String priority=null;
		priority = tabArg.getSysMappingPriority();
		
		if( priority == null ) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : first. The values you've specified'll be taken.");
			this.coordSystem.setPriority(DefineType.FIRST);
		}
		else if( priority.equals("only") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : only. The values you've specified'll be taken.");
			this.coordSystem.setPriority(DefineType.ONLY);
			if( this.coordSystem.getAutodedect() ) {
				Messenger.printMsg(Messenger.WARNING, "Coord system won't be set because it is required to be computed from mapping parameters, but mapping is not set.");
			}
		}
		else if( priority.equals("first") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : first. The values you've specified'll be taken first.");
			this.coordSystem.setPriority(DefineType.FIRST);
		}
		else if( priority.equals("last") ){			
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Priority : last. The values you've specified'll be taken if there are not already defined in the file.");
			this.coordSystem.setPriority(DefineType.LAST);
		}
		else {			
			FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Unknown coordinates system mapping priority <" + priority + ">");
		}

	}

    
    /**This method confirms integrity of the collection name
     * and if this collection is in data base
     * and sending trace message if that false
     *@return boolean true or false if this the collection name is not null and if the collection is in data base
     */
    public boolean validCollection(){
		
    	if(collectionName==null || collectionName.equals("")){
		    Messenger.printMsg(Messenger.ERROR, "No collection name in configuration "+nameProduct);
		    return false; 
		}
		try{
		    //Test if the collection is in data base
		    //This test uses the API of Saada (api package) and the classe Saada_DB
		    if(SaadaDB.getCollection(collectionName)==null){
			Messenger.printMsg(Messenger.ERROR,"No collection with the name "+collectionName+" in database");
			return false;
		    }
		}catch(Exception e){
	            Messenger.printMsg(Messenger.ERROR, getClass().getName()+" "+e);
		    return false;
		}
		return true;
    }
    
    
    /**This method confirms integrity of a product with this configuration
     * and sending trace message
     *@param Product that we want to validate
     *@return boolean true or false if this product is valid
     * @throws IOException 
     * @throws FitsException 
     */
    public boolean isProductValid(Product product) throws FitsException, IOException{
		
    	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Checking if product "+product.getName()+" matches the configuration "+nameProduct);
		if(!sign.valid(product)){
		    //No message is necessary here, because the method valid() in the class ProductSignaturee (Object sign) sends one
		    return false;
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The product "+product.getName()+" matches the configuration "+nameProduct);
		return  true;
    }
 
    
    /**
     * @param num
     */
    public HeaderRef getHeaderRef() {
    	return this.headerRef;
    }

   /**
     *  Build strings for config MD5 computation
     *  These string are computed oj the fly in case of XML filer parsing
     *  But they must be computed off-fly when the config is built from dataloader params
     */
    private void setMD5() {
    	this.s_md5 = "";
    	this.s_md5_without_col = "";
    	
    	this.s_md5 += this.collectionName;
    	this.s_md5 += this.mapping.getTypeMapping().getClass().getName();
    	this.s_md5_without_col += this.mapping.getTypeMapping().getClass().getName();
    	
    	ArrayList<String> vect = this.mapping.getVector(str);
    	if( vect != null ) {
    		for( int i=0 ; i<vect.size() ; i++  ) {
    			String s = (String)(vect.get(i));
    			s_md5 += s;	
    			s_md5_without_col += s;
    		} 
    	}
    	this.s_md5 += this.coordSystem.getMD5();
    	     	
    	for( int i=0 ; i<this.sign.size() ; i++  ) {
    		String s = (String)(vect.get(i));
    		s_md5 += s;
    		s_md5_without_col += s;
    	}
    	
    }
    
    /**Returns this configuration algorithmics value with md5
     *@return String this configuration algorithmics value with md5
     */  
    public String getMD5(){
    	if( s_md5 == null || s_md5.equals("")) {
    		this.setMD5();
    	}
		md5 = MD5Key.calculMD5Key(s_md5);
		return md5;
    }
    /**Returns the algorithmics value of this configuration whithout collection indication with md5
     *@return String the algorithmics value of this configuration whithout collection indication with md5
     */ 
    public String getMD5WithoutCol(){
    	if( md5_without_col == null || md5_without_col.equals("")) {
    		this.setMD5();
    	}
		md5_without_col = MD5Key.calculMD5Key(s_md5_without_col);
		return md5_without_col;
    }
    /**Returns the configuration type of generated class Saada
     *@return String the configuration type of generated class Saada
     */
    public int getTypeSaada(){
    	return categorySaada;
    }
 
    /**Returns the collection name that correspond to this configuration
     *@return String the collection name that correspond to this configuration 
     */
    public String getCollectionName(){
    	return collectionName;
    }
    /**Returns the product name that correspond to this configuration
     *@return String the product name that correspond to this configuration 
     */  
    public String getNameProduct(){
    	return nameProduct;
    }
    /**Returns the configuration type
     *@return String the configuration type
     */
    public String getDataType(){
    	return dataType;
    }
    /**Returns the object containing product signatures of this configuration
     *@return CoordSytem the object containing product signatures of this configuration 
     */
    public ProductSignature getProductSignature(){
    	return sign;
    }
 
    /**Returns the type of the object containing all mapping information of this configuration
     *@return TypeMapping the type of the object containing all mapping information of this configuration 
     */
    public TypeMapping getTypeMapping(){
    	return mapping.getTypeMapping();
    }
    /**Returns the object containing all mapping information of this configuration
     *@return TypeMapping the object containing all mapping information of this configuration 
     */
    public Mapping getMapping(){
    	return mapping;
    }
 
    /**Record the identification element
     *@param String the identification element
     *@return void
     */
    protected void recordIdentification(String s){
      	//If an element name in the recorded product has a simple name "path_name", record the character date inside this element
     	//create the new product signature "PathName" and add this in the class ProductSignature
     	if(str.equals("path_name")){
     		sign.add(new PathName(s));
     		//Update the 2 products md5 (with and without collection)
     		s_md5 += s;
     		s_md5_without_col += s;
     	}else if(str.equals("pattern_regex")){
     		//If an element name in the recorded product has a simple name "pattern_regex", record the character date inside this element
     		//create the new product signature "PatternRegex" and add this in the class ProductSignature
     		sign.add(new PatternRegex(s));
     		//Update the 2 products md5 (with and without collection)
     		s_md5 += s;
     		s_md5_without_col += s;
     	}
     	else if(recordContent){
     		if( content == null ) {
     			content = new Content();
     		}
     		//If the element name equals "value", set this in the "Content" Object
     		if(str.equals("value")){
     			content.setValue(s);
      			//Update the 2 products md5 (with and without collection)
     			s_md5 += s;
     			s_md5_without_col += s;
     		}else{
     			//If the element name equals "attribute", set this in the "Content" Object
     			if(str.equals("attribute")){
     				content.setAttribute(s);
     				//Update the 2 products md5 (with and without collection)
     				s_md5 += s;
     				s_md5_without_col += s;
     			}
     		}
     		//add the "Content" Object in the class ProductSignature
     		if(!content.getValue().equals("") && !content.getAttribute().equals("")){
     		        			sign.add(content);
     		}
     		
     	}
     }
    	
    /**Record the coordinate or system element
     *@param String the coordinate or system element
     *@return void
     */
    protected void recordCoordSystem(String s){
//			//If the element name equals "system", set this in the "CoordSystem" Object
//		if (str.equals("system")){
//		    getCoordSystem().setSystem(s);
//		    //Update the 2 products md5 (with and without collection)
//		    s_md5 += s;
//		    s_md5_without_col += s;
//		}else{
//		    //If the element name equals "equinox", set this in the "CoordSystem" Object
//		    if(str.equals("equinox")){
//			getCoordSystem().setEquinox(s);
//			//Update the 2 products md5 (with and without collection)
//			s_md5 += s;
//			s_md5_without_col += s;
//		    }
//		}
    }
    /**Record the mapping element
     *@param String the mapping element
     *@return void
     */
    protected void recordDefaultMapping(String s){
	//If the element name equals not the name define in the variable "ATTR_EXT" define in class DefineType
	//or if you recorded the instance name or the ignored attributes
	//set this in the corresponding board in the "Mapping" class
	if(!str.equals(Mapping.ATTR_EXT)){
	    ArrayList<String> value = mapping.getVector(str);
	    if(value != null){
		//record the list
		while(s.indexOf(",")>0){
		    //if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, " Value: "+s.substring(0, s.indexOf(",")).trim());
		    value.add(s.substring(0, s.indexOf(",")).trim());
		    s = s.substring(s.indexOf(",")+1);
		    //Update the 2 products md5 (with and without collection)
		    s_md5 += s;
		    s_md5_without_col += s;
		}
		//if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, " Value: "+s);
		value.add(s.trim());
		//Update the 2 products md5 (with and without collection)
		s_md5 += s;
		s_md5_without_col += s;
	    }
	}
    }
    /**Record the collection or class mapping element
     *@param String the collection or class mapping element
     *@return void
     */
    protected void recordCollectionOrClassMapping(String s){
		//If the element name equals "collection", record the collection name
		if(str.equals("collection")){
		    collectionName = s;
		    //Update one products md5 with collection
		    s_md5 += s;
		}else{
		    //If the element name equals "class_mapping", record the class mapping
		    if(str.equals("class_mapping")){
			mapping.setTypeMapping(s);
			//Update the 2 products md5 (with and without collection)
			s_md5 += s;
			s_md5_without_col += s;
		    }
		}
    }
    
    /**Returns the configuration type
     *@return String the configuration type
     */
    public int getCategorySaada(){
    	return this.categorySaada;
    }
	/**
	 * @return Returns the extensionName.
	 */
	public String getExtensionName() {
		return extensionName;
	}
 
	/**
	 * @return
	 */
	public int getRepository_mode() {
		return repository_mode;
	}
	/**
	 * @param filename
	 * @return
	 * @throws SaadaException
	 * @throws AbortException 
	 */
	public  Product getNewProductInstance(File file) throws IgnoreException {
		switch( this.categorySaada ) {
		case Category.TABLE: return new Table(file, this) ;
		case Category.MISC : return new Misc(file, this) ;
		case Category.SPECTRUM : return new Spectrum(file, this) ;
		case Category.IMAGE : return new Image2D(file, this) ;
		case Category.FLATFILE : return new FlatFile(file, this) ;
		default: IgnoreException.throwNewException(SaadaException.UNSUPPORTED_MODE, "Can't handle product category <" + this.categorySaada + ">");
		return null;
		}
		
	}


	public CoordSystem getCoordSystem() {
		return this.coordSystem;
	}

	/**
	 * @return Returns the loader_param.
	 */
	public String getLoader_param() {
		return loader_param;
	}

 }
  
