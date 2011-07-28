package saadadb.prdconfiguration;

import java.io.File;

import saadadb.classmapping.Mapping;
import saadadb.classmapping.TypeMapping;
import saadadb.products.Product;
/**
 * Interface for the specification of a configuration.
 * An object that implements the Configuration interface can be used in a standard way in all the Saada application.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public interface Configuration{
    /**This method confirms integrity of all (constraints, mapping, collection name ...) in configuration
     *@return boolean true or false if all are confirmed
     */    
    public boolean isValid();
    /**This method confirms integrity of a product with configuration
     *@param Product that we want to validate
     *@return boolean true or false if product is valid
     */
    public boolean valid(Product product);
    /**Returns the file configuration XML
     *@return File the file configuration XML 
     */
    public File getFileConfigurationXML();
    /**Returns the object containing all mapping information of configuration
     *@return TypeMapping the object containing all mapping information of configuration 
     */
    public Mapping getMapping();
    /**Returns the collection name that correspond to configuration
     *@return String the collection name that correspond to configuration 
     */
    public String getCollectionName();
    /**Returns the configuration type
     *@return String the configuration type
     */
    public int getCategorySaada();
    /**Returns the configuration type
     *@return String the configuration type
     */
     public CoordSystem getCoordSystem();
    /**Returns the object containing product signatures of configuration
     *@return CoordSytem the object containing product signatures of configuration 
     */
    public ProductSignature getProductSignature();
    /**Returns the type of the object containing all mapping information of configuration
     *@return TypeMapping the type of the object containing all mapping information of configuration 
     */
    public TypeMapping getTypeMapping();
    /**Returns the product name that correspond to configuration
     *@return String the product name that correspond to configuration 
     */ 
    public String getNameProduct();
    /**Returns the algorithmics value of configuration whithout collection indication with md5
     *@return String the algorithmics value of configuration whithout collection indication with md5
     */ 
    public String getMD5WithoutCol();
    /**Returns configuration algorithmics value with md5
     *@return String configuration algorithmics value with md5
     */ 
    public String getMD5();
    /**Returns Header reference
     *@return Header reference
     */ 
    public HeaderRef getHeaderRef();
    /**This method confirms integrity of mapping in configuration
     *@return boolean true or false if mapping is confirmed
     */
    public boolean isMappingValid();
    /**This method confirms integrity of all product identification constraints in configuration
     *@return boolean true or false if all product identification constraints are confirmed
     */ 
    public boolean isProductIndentificationValid();
    /**This method confirms the equinox and coordinated system in configuration
     *@return boolean true or false if the equinox and coordinated system is confirmed
     */
    public boolean isCoordSystemValid();
}
  
