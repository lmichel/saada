package saadadb.prdconfiguration;

import java.io.IOException;
import java.util.TreeSet;

import nom.tam.fits.FitsException;
import saadadb.classmapping.Mapping;
import saadadb.classmapping.MappingEntry;
import saadadb.classmapping.TypeMapping;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.products.Product;
import saadadb.util.Messenger;

public class ConfigurationEntry extends ConfigurationDefaultHandler{
	/**Configuration of the table for this entry configuration**/
	protected ConfigurationTable configurationTable;
	private TreeSet<Integer> ignored_cols = new TreeSet<Integer>();;

	/**
	 * @param configTable
	 * @param tabArg
	 * @throws SaadaException
	 */
	public ConfigurationEntry(ConfigurationTable configTable, ArgsParser tabArg) throws SaadaException{
		super(configTable.nameProduct, tabArg);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Building a new the ENTRY configuration ...");
			//Initializes the Saada type of generated class in Saada
		this.categorySaada = Category.ENTRY;
					//Initializes the configuration of the table for this entry configuration 
		this.configurationTable = configTable;
		//Create the new object containing all mapping information of this configuration
		this.mapping = new MappingEntry(this, tabArg);
				
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The configuration entry is over.");
	}


	/**Returns the name of this spectra configuration
	 *@return String the name of this spectra configuration 
	 */
	public String getNameEntry(){
		return nameProduct;
	}

	/**This method confirms integrity of a product with this entries configuration (ampping and coordinates)
	 *@param Product that we want to validate
	 *@return boolean true or false if this product is valid
	 * @throws IOException 
	 * @throws FitsException 
	 */    
	public boolean isProductValid(Product product) throws FitsException, IOException{
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Checking if product "+product.getName()+" matches the ENTRY configuration "+this.nameProduct);
		return (mapping.isProductValid(product) /*&& coordSystem.isProductValid(product)*/);
	}

	/**Returns the object containing all mapping information of this entries configuration
	 *@return TypeMapping the object containing all mapping information of this entries configuration 
	 */
	public Mapping getMapping(){
		return mapping;
	}
	/**Returns the collection name that correspond to this configuration
	 *(exactly the collection name of his tables configuration)
	 *@return String the collection name that correspond to this configuration 
	 */
	public String getCollectionName(){
		return configurationTable.getCollectionName();
	}
	/**Returns the configuration type of generated class Saada (generaly "EntrySaada")
	 *@return String the configuration type of generated class Saada
	 */
	public int getCategorySaada(){
		return categorySaada;
	}
	/**Returns the object containing product signatures of this entry configuration
	 *(exactly the object of his tables configuration)
	 *@return CoordSytem the object containing product signatures of this configuration 
	 */
	public ProductSignature getProductSignature(){
		return configurationTable.getProductSignature();
	}
	/**Returns the type of the object containing all mapping information of this entry configuration
	 *There is two mappings :entries mapping (for this configuration) and tables mapping (for his tables configuration) for one parsing
	 *The tables mapping corresponds in the default mapping of his tables configuration
	 *@return TypeMapping the type of the object containing all mapping information of this entry configuration 
	 */
	public TypeMapping getTypeMapping(){
		return mapping.getTypeMapping();
	}
	/**Returns the algorithmics value of this entries configuration whithout collection indication with md5
	 *(exactly the md5 of his tables configuration)
	 *@return String the algorithmics value of this entries configuration whithout collection indication with md5
	 */ 
	public String getMD5WithoutCol(){
		return configurationTable.getMD5WithoutCol();
	}
	/**Returns  this entries configuration algorithmics value with md5
	 *(exactly the md5 of his tables configuration)
	 *@return String this entries configuration algorithmics value with md5
	 */  
	public String getMD5(){
		return configurationTable.getMD5();
	}
	
	/* (non-Javadoc)
	 * @see saadadb.prdconfiguration.ConfigurationDefaultHandler#getHeaderRef()
	 */
	public HeaderRef getHeaderRef() {
		return configurationTable.getHeaderRef();
	}


	/**
	 * @param j
	 */
	public void addIgnoredCol(int j) {
		this.ignored_cols .add(j);
		
	}
	
	/**
	 * @param j
	 * @return
	 */
	public boolean isColIgnored(int j) {
		return this.ignored_cols.contains(j);
	}
}


