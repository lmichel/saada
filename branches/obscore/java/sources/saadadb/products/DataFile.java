package saadadb.products;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import nom.tam.fits.FitsException;
import saadadb.dataloader.mapping.ProductMapping;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.products.inference.QuantityDetector;
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
public interface DataFile extends Enumeration{
    /**Returns the value corresponding finded in the product file to the key word in parameter.
     *@param String The key word.
     *@return String The value corresponding to this key word, if he exists, else null.
     */
    public String getKWValueQuickly(String key);
   
 	/**
 	 * @param key  column name as it is in the file
 	 * @return min,max,nbpoints
 	 * @throws Exception
 	 */
 	public double[] getExtrema(String keyOrg) throws Exception ;
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
	public Map<String, List<AttributeHandler>> getProductMap(int category) throws IgnoreException ;
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
	 * Return a lis of comments which can be used to do the automatic mapping
	 * @return
	 * @throws SaadaException
	 */
	public List<String> getComments() throws SaadaException ;
//	/**
//	 * Returns an instance of the detector of keywords covering the observation axis
//	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
//	 */
//	public ObservationKWDetector getObservationKWDetector(boolean entryMode) throws SaadaException;
//	/**
//	 * Returns an instance of the detector of keywords covering the space axis
//	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
//	 */
//	public SpaceKWDetector getSpaceKWDetector(boolean entryMode) throws SaadaException;
//	/**
//	 * Returns an instance of the detector of keywords covering the energy axis
//	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
//	 * @param entryMode
//	 * @param priority
//	 * @param defaultUnit
//	 * @return
//	 * @throws SaadaException
//	 */
//	public EnergyKWDetector getEnergyKWDetector(boolean entryMode, PriorityMode priority, String defaultUnit) throws SaadaException;
//	/**
//	 * Returns an instance of the detector of keywords covering the time axis
//	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
//	 */
//	public TimeKWDetector getTimeKWDetector(boolean entryMode) throws SaadaException;
//	/**
//	 * Returns an instance of the detector of keywords covering the observable axis
//	 * This instance is built by the ProcutFile because it could add to it some data which are not in the keywords
//	 */
//	public ObservableKWDetector getObservableKWDetector(boolean entryMode) throws SaadaException;
	/**
	 * @return
	 * @throws SaadaException
	 * @throws Exception 
	 */
	public QuantityDetector getQuantityDetector(ProductMapping productMapping) throws Exception;
	/**
	 * Returns a map with all extension detected within the data product
	 * @return
	 * @throws Exception 
	 */
	public Map<String, DataFileExtension> getProductMap() throws Exception;
	/**
	 * Connect the DataFile with the ProductBuilder
	 * @param builder
	 */
	public void bindBuilder(ProductBuilder builder) throws Exception;
	/**
	 * Returns the list of the loaded extension with the reason why they have been taken
	 * @return
	 */
	public List<ExtensionSetter> reportOnLoadedExtension() ;	
	/**
	 * Method overloading a File accessors
	 * @return
	 * @throws IOException
	 */
	public String getCanonicalPath() throws IOException ;
	/**
	 * Method overloading a File accessor
	 * @return
	 * @throws IOException
	 */
	public String getAbsolutePath();
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public String getParent() ;
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public long length() ;
	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public boolean delete() ;

	/**
	 * Method overloading a File accessor
	 * @return
	 */
	public String getName();


 }
  
