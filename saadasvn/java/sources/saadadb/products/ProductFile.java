package saadadb.products;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.IgnoreException;
import saadadb.meta.AttributeHandler;
/**
 * Interface for the specification of a product file.
 * An object that implements the ProductFile interface can be used in a standard way in all the Saada application.
 * This interface extends the Enumeration interface.
 * For the record, an object that implements the Enumeration interface generates a series of elements, one at a time.
 * Successive calls to the nextElement method return successive elements of the series.
 * This enumeration is necessary for the reading of entries table, she returns one objects row for one table row (one object equals one rows box).
 * She has to allow a reading of the information in stream mode.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see Enumeration
 */
public interface ProductFile extends Enumeration{
    /**Returns the value corresponding finded in the product file to the key word in parameter.
     *@param String The key word.
     *@return String The value corresponding to this key word, if he exists, else null.
     */
    public String getKWValueQuickly(String key);
     /**In case of the product can have table:
     * This method is used for the valdation of the product by this configuration.
     * Returns the list which maps entry names not formated (keys) to their position number in the table (values).
     * Attention: In the case of entries, their header in tables product can be different from standards of the other products.
     * This list is maked with the n'th HDU in parameter.
     * If there is no table for this product format, this method will return null.
     *@param numHDU The n'th HDU.
     *@return Hashtable The list which maps entry names to their position number in the table.
     * @throws AbortException 
     * @throws IgnoreException 
     */
    public LinkedHashMap<String, Integer> getTableEntry() throws IgnoreException;
    /**In case of the product can have table:
     * Returns the list which maps entry names formated in the standard of Saada (keys) to their objects modelling entry informations (values).
     * This list is maked with the first table Header (index 1).
     * Generally the object modelling entry informations is a AttributeHandler.
     * If there is no table for this product format, this method will return null.
     *@param Configuration The configuration of this product file.
     *@return Hashtable The list which maps entry names to their informations.
     * @throws FitsException 
     */
    public void getKWEntry(LinkedHashMap<String, AttributeHandler> tah) throws IgnoreException;
    /**Returns the algorithmics value for the product characteristics (attributes without values) with md5.
     *@return String this algorithmics value with md5.
     */  
     public Image getSpectraImage();
     /**In case of the product can have table:
     * Returns one objects row for one table row with table index in parameter (one object equals one rows box).
     * This row is maked with the first table Header (index 1).
     * If there is no table for this product format, this method will return null.
     *@param int The row index in the table. 
     *@return Object[] The objects row in the the table with the index in parameter.
     * @throws IOException 
     * @throws FitsException 
     */
    public Object[] getRow(int index) throws IgnoreException;
	public double[] getExtrema(String key) throws Exception ;
    /**In case of the product can have table:
     * Returns one objects row for one table row with table index in parameter (one object equals one rows box).
     * This row is maked with the n'th table Header in parameter.
     * If there is no row with this table index, returns null.
     *@param int The row index in the table.
     *@param numHDU The n'th table Header.
     *@return Object[] The objects row in the the table with the index in parameter.
     * @throws IOException 
     * @throws FitsException 
     */
    public Object[] getRow(int index, int numHDU) throws IgnoreException;
    /**In case of the product can have table:
     * Returns the row number in the table.
     * If there is no table for this product format, this method will return 0.
     *@return int The row number in the table.
     * @throws IOException 
     * @throws FitsException 
     */
    public int getNRows() throws IgnoreException;

    /**In case of the product can have table:
     * Returns the column number in the table.
     * If there is no table for this product format, this method will return 0.
     *@param numHDU The n'th table Header.
     *@return int The column number in the table.
     * @throws IOException 
     * @throws FitsException 
     */
    public int getNCols() throws IgnoreException;
    /**In case of the product can have table:
     * Initializes the enumeration of table rows (essential in stream mode).
     * This method is necessary in the class Product (package saadadb.products) for return a initialized enumeration:
     * See method elements() in class Product (she returns a Enumeration).
     *@return void.
     * @throws IOException 
     * @throws FitsException 
     */
    public void initEnumeration() throws IgnoreException;
    /*
     * Builds a map of the product attribute 
     */
	public LinkedHashMap<String, ArrayList<AttributeHandler>> getMap(String category) throws IgnoreException ;
	
	/**
	 * @return
	 */
	public SpaceFrame getSpaceFrame();
	/**
	 * Look for position frame and KW in column names
	 * @throws IgnoreException 
	 */
	public void setSpaceFrameForTable() throws IgnoreException;
	
	/**
	 * Look for position frame and KW in column names
	 */
	public void setSpaceFrame();
	

 }
  
