package saadadb.products;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import nom.tam.fits.FitsException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.EnergyKWDetector;
import saadadb.products.inference.ObservationKWDetector;
import saadadb.products.inference.SpaceKWDetector;
import saadadb.products.inference.TimeKWDetector;
/**v * @version $Id$

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
@SuppressWarnings("rawtypes")
public interface ProductFile extends Enumeration{
    /**Returns the value corresponding finded in the product file to the key word in parameter.
     *@param String The key word.
     *@return String The value corresponding to this key word, if he exists, else null.
     */
    public String getKWValueQuickly(String key);
   
 	public double[] getExtrema(String key) throws Exception ;
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
	public void closeStream() throws QueryException;
    /**
     * Builds a map of the product attribute 
     * key = HDU
     * value = attribute handlers
     */
	public Map<String, ArrayList<AttributeHandler>> getProductMap(int category) throws IgnoreException ;
	/**
	 * Returns the map of the attribute handlers modeling the columns of te data tables
	 * @return
	 */
	public Map<String, AttributeHandler> getEntryAttributeHandler() throws SaadaException ;
	/**
	 * Returns the map of the attribute handlers modeling the KW of all headers loaded
	 * @return
	 */
	public Map<String, AttributeHandler> getAttributeHandler() throws SaadaException ;
	/**
	 * Returns an instance of the detector of keywords covering the observation axe
	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
	 */
	public ObservationKWDetector getObservationKWDetector(boolean entryMode) throws SaadaException;
	/**
	 * Returns an instance of the detector of keywords covering the observation axe
	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
	 */
	public SpaceKWDetector getSpaceKWDetector(boolean entryMode) throws SaadaException;
	/**
	 * Returns an instance of the detector of keywords covering the observation axe
	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
	 */
	public EnergyKWDetector getEnergyKWDetector(boolean entryMode) throws SaadaException;
	/**
	 * Returns an instance of the detector of keywords covering the observation axe
	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
	 */
	public TimeKWDetector getTimeKWDetector(boolean entryMode) throws SaadaException;
	public String getName();
 }
  
