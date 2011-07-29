package saadadb.prdconfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import nom.tam.fits.FitsException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import saadadb.classmapping.Mapping;
import saadadb.classmapping.MappingTable;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.products.Product;
import saadadb.products.Table;
import saadadb.util.DefineType;
import saadadb.util.Messenger;

public class ConfigurationTable extends ConfigurationDefaultHandler {
	/**Configuration of all entries in this table configuration**/
	public ConfigurationEntry configurationEntry;
	/**Boolean indicating the recording of the deliberate element mapping for the entries**/
	protected boolean recordEntryMapping;

	/** * @version $Id$

	 * @param configName
	 * @param tabArg
	 * @throws SaadaException 
	 * @throws ParsingException 
	 */
	public ConfigurationTable(String configName, ArgsParser tabArg) throws SaadaException {

		super(configName, tabArg);
			//Initializes the Saada type of generated class in Saada
		this.categorySaada = Category.TABLE;
		Messenger.printMsg(Messenger.TRACE, "Extract the ENTRY configuration");
			//Create the entries configuration of this table configuration
		this.configurationEntry = new ConfigurationEntry(this, tabArg);
		//Create the new object containing all mapping information of this configuration
		this.mapping = new MappingTable(this, tabArg);
	}

	/**Returns the name of this table configuration
	 *@return String the name of this table configuration 
	 */
	public String getNameTable(){
		return nameProduct;
	}
	/**Returns the entries configuration of this table configuration
	 *@return Configuration the entries configuration of this table configuration 
	 */
	public ConfigurationEntry getConfigurationEntry(){
		return configurationEntry;
	}

	/**This method confirms integrity of a product with this table configuration (and his entries configuration)
	 * and sending trace message
	 *@param Product that we want to validate
	 *@return boolean true or false if this product is valid
	 * @throws IOException 
	 * @throws FitsException 
	 */
	public boolean isProductValid(Product product) throws FitsException, IOException{

		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Checking if product "+product.getName()+" matches the TABLE configuration ");
		if(!sign.valid(product)){
			//No message is necessary here, because the method valid() in the class ProductSignaturee (Object sign) sends one
			return false;
		}
		if(!mapping.isProductValid(product)){
			//No message is necessary here, because the method valid() in the class who has to implement the inteface Mapping (or generaly in the class MappingDefault) sends one
			return false;
		}
		if(!configurationEntry.isProductValid(((Table)product).getEntry())){
			//No message is necessary here, because the method valid() in the entries configuration sends one
			return false;
		}
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "OK: product "+product.getName()+" matches the TABLE configuration "+this.nameProduct);
		return true;
	}
	/**Receive notification of the start of an element
	 *@param Attributes the specified or defaulted attributes
	 *@param String qualifiedName the qualified name (with prefix), or the empty string if qualified names are not available
	 *@param String simpleName the local name (without prefix), or the empty string if Namespace processing is not being performed
	 *@param String namespacesURI the Namespace URI
	 *@return void
	 *@throws SAXException Any SAX exception, possibly wrapping another exception
	 */
	public void startElement(String namespaceURI, String simpleName, String qualifiedName, Attributes attrs) throws SAXException {
		String  elementName = simpleName;
		if(elementName.equals("")){
			elementName = qualifiedName;
			str = elementName;
		}
		//Tests of data type coherence in the xml configuration file
		start = DefineType.getDataType(elementName);
		/*if(elementName.equals(typeData)){
	  if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Product: "+attrs.getValue("name").trim()+ " is present in "+fileConfigXML.getName());
	  }*/
		//Tests of recording element
		try {
			if(Category.getCategory(elementName) == this.categorySaada && attrs.getValue("name").trim().equals(nameProduct)){
				if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Recording element: "+attrs.getValue("name").trim());
				isRecording = true;
				recordElement = true;
			}
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
		}
		//Tests if the product in the xml configuration file must be read
		if(recordElement){
			//If an element name in the recorded product has a simple name "identification", record the character data inside this element
			if(elementName.equals("identification")){
				recordIdentification = true;
			}else{
				//If an element name in the recorded product has a simple name "table_mapping", record the character data inside this element
				if(elementName.equals("table_mapping")){
					recordMapping = true;
				}else{
					//If an element name in the recorded product has a simple name "entry_mapping", record the character data inside this element
					if(elementName.equals("entry_mapping")){
						recordEntryMapping = true;
					}else{
						//If an element name in the recorded product has a simple name "coord_system", record the character data inside this element
						if(recordEntryMapping && elementName.equals("coord_system")){
							recordCoordSystem = true;
						}else{
							//If an element name in the recorded product has a simple name "content" and you can recorded the identification
							//record the character date inside this element
							if(elementName.equals("content") && recordIdentification){
								recordContent = true;
							}else{
								//If an element name in the recorded product has a simple name define in the variable "ATTR_EXT" define in class DefineType
								//generaly "attr_ext", and if you can recorded the "TABLE mapping" (recordMapping equals true)
								//record the character date inside this element in the data of the extended attibutes
								if(elementName.equals(Mapping.ATTR_EXT) && recordMapping){
									String name = attrs.getValue("name").trim();
									String mappingValue = attrs.getValue("mapping").trim();
									//Get the attributes "Hashtable" which maps attribute name to their value
									((LinkedHashMap<String, String>)mapping.getAttrExt()).put(name, mappingValue);
									//Update the 2 products md5 (with and without collection)
									s_md5 += name+mappingValue;
									s_md5_without_col += name+mappingValue;
								}else{
									//If an element name in the recorded product has a simple name define in the variable "ATTR_EXT" define in class DefineType
									//generaly "attr_ext", and if you can recorded the "ENTRY mapping" (recordEntryMapping equals true)
									//record the character date inside this element in the data of the extended attibutes
									if(elementName.equals(Mapping.ATTR_EXT) && recordEntryMapping){
										String name = attrs.getValue("name").trim();
										String mappingValue = attrs.getValue("mapping").trim();
										//Get the attributes "Hashtable" which maps attribute name to their value
										((LinkedHashMap<String, String>)configurationEntry.getMapping().getAttrExt()).put(name, mappingValue);
										//Update the 2 products md5 (with and without collection)
										s_md5 += name+mappingValue;
										s_md5_without_col += name+mappingValue;
									}
								}
							}
						}
					}
				}
			}
		}
	}
	/**Receive notification of the end of an element
	 *@param String qualifiedName the qualified name (with prefix), or the empty string if qualified names are not available
	 *@param String simpleName the local name (without prefix), or the empty string if Namespace processing is not being performed
	 *@param String namespacesURI the Namespace URI
	 *@return void
	 *@throws SAXException Any SAX exception, possibly wrapping another exception
	 */
	public void endElement(String namespaceURI, String simpleName, String qualifiedName) throws SAXException{
		String  elementName = simpleName;
		if(elementName.equals("")){
			elementName = qualifiedName;
		}
		//Tests of data type coherence in the xml configuration file
		stop = DefineType.getDataType(elementName);
		if(stop.equals(start)){
			dataType = stop;
		}
		//If an element name in the recorded product has the data type name, and if you can record the selected product
		//stop this record
		try {
			if(Category.getCategory(elementName) == this.categorySaada && recordElement){
				recordElement = false;
			}
		} catch (SaadaException e) {
			Messenger.printStackTrace(e);
		}
		//If an element name in the recorded product has a simple name "identification", and if you can record this identification
		//stop this record
		if(recordIdentification && elementName.equals("identification")){
			recordIdentification = false;
		}
		//If an element name in the recorded product has a simple name "coord_system", and if you can record this piece of information
		//stop this record
		if(recordEntryMapping && recordCoordSystem && elementName.equals("coord_system")){
			recordCoordSystem = false;
		}
		//If an element name in the recorded product has a simple name "table_mapping", and if you can record this piece of information
		//stop this record
		if(recordMapping && elementName.equals("table_mapping")){
			recordMapping = false;
		}
		//If an element name in the recorded product has a simple name "entry_mapping", and if you can record this piece of information
		//stop this record
		if(recordEntryMapping && elementName.equals("entry_mappisetPosDecng")){
			recordEntryMapping = false;
		}
		//If an element name in the recorded product has a simple name "content", and if you can record this piece of information
		//stop this record
		if(recordContent && recordIdentification && elementName.equals("content")){
			recordContent = false;
		}
	}
	/**Receive notification of character data inside an element, and sends trace message for every recorded characters arrays
	 *@param char[] buf the characters
	 *@param int offset the start position in the character array
	 *@param int len the number of characters to use from the character array
	 *@return void
	 *@throws SAXException Any SAX exception, possibly wrapping another exception
	 */
	public void characters(char buf [], int offset, int len) throws SAXException{
		//Initializes the String value corresponding of this current character array
		String s = new String(buf, offset, len).trim();
		//Tests if you can record all characters arrays of this product
		if(recordElement){
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, s+", in "+str);
			//If this character array is empty, sends warning and exits
			if(s.equals("")){
				Messenger.printMsg(Messenger.WARNING, "The element <"+str+"> is empty");
				System.exit(1);
			}else{
				//If you can record this character array (if this array corresponds in the identification array of this recorded product)
				//Records this array
				if(recordIdentification){
					//See the super class "ConfigurationDefaultHandler"
					recordIdentification(s);
				}else{
					//There is two mappings in this configuration file and one parsing
					//If this array corresponds in the mapping array of this recorded product, record this array
					//The tables mapping corresponds in the default mapping of this configuration 
					if(recordMapping){
						//See the super class "ConfigurationDefaultHandler"
						recordDefaultMapping(s);
					}else{
						//Tests if the simple element is the element of entries mapping
						//(New precision: The tables mapping corresponds in the default mapping of this configuration) 
						if(recordEntryMapping){
							//If this array corresponds in the "coordinating" array of this recorded product, record this array
							if(recordCoordSystem){
								//See the super class "ConfigurationDefaultHandler"
								recordCoordSystem(s);
							}else{
								//If the element corresponds in the RA position, records this in the entries mapping
								if(str.equals("pos_ra_csa")){
									configurationEntry.getMapping().setPosRa(s);
									//Update the 2 products md5 (with and without collection)
									s_md5 += s;
									s_md5_without_col += s;
								}else{
									//If the element corresponds in the DEC position, records this in the entries mapping
									if(str.equals("pos_dec_csa")){
										configurationEntry.getMapping().setPosDec(s);
										//Update the 2 products md5 (with and without collection)
										s_md5 += s;
										s_md5_without_col += s;
									}else{
										//If the element corresponds in the error RA position, records this in the entries mapping
										if(str.equals("error_ra_csa")){
											s_md5 += s;
											s_md5_without_col += s;
										}else{
											//If the element corresponds in the error DEC position, records this in the entries mapping
											if(str.equals("error_dec_csa")){
												s_md5 += s;
												s_md5_without_col += s;
											}else{
												//Default: records (if that possible) the entries mapping (generaly instance name and ignored attributes) 
												recordEntryDefaultMapping(s);
											}
										}
									}
								}
							}
						}else{
							//Default: records (if that possible) the collection or class mapping element for tables and entries
							recordCollectionOrClassMapping(s);
						}
					}
				}
			}
		}
	}
	/**Record the mapping element for entries
	 *@param String the mapping element
	 *@return void
	 */
	protected void recordEntryDefaultMapping(String s){
		//If the element name equals not the name define in the variable "ATTR_EXT" define in class DefineType
		//or if you recorded the instance name or the ignored attributes
		//set this in the corresponding board in the "Mapping" class
		if(!str.equals(Mapping.ATTR_EXT)){
			ArrayList<String> value = configurationEntry.getMapping().getVector(str);
			if(value != null){
				//record the list
				while(s.indexOf(",")>0){
					value.add(s.substring(0, s.indexOf(",")).trim());
					s = s.substring(s.indexOf(",")+1);
					//Update the 2 products md5 (with and without collection)
					s_md5 += s;
					s_md5_without_col += s;
				}
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
		//See the super class "ConfigurationDefaultHandler"
		//This method duplicates the super method, with a new feature for entries
		super.recordCollectionOrClassMapping(s);
		//If the element name equals "class_mapping", record the class mapping in entries configuration (exactly in entries mapping)
		if(str.equals("class_mapping")){
			configurationEntry.getMapping().setTypeMapping(s);
		}
	}

	/* (non-Javadoc)
	 * @see saadadb.prdconfiguration.Configuration#getCategorySaada()
	 */
	public int getCategorySaada() {
		return this.categorySaada;
	}
}

