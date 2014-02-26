package saadadb.prdconfiguration;

import java.util.LinkedHashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import saadadb.classmapping.Mapping;
import saadadb.classmapping.MappingSpectrum;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.util.DefineType;
import saadadb.util.Messenger;
/**
 *This class is specification of a configuration spectra
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see ConfigurationDefaultHandler
 *@see Configuration
 */
/**
 * @author abouchacra
 * * @version $Id: ConfigurationSpectrum.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class ConfigurationSpectrum extends ConfigurationDefaultHandler {
    protected boolean recordSpectralCoordinate = false; 
 
    /**
     * @param configName
     * @param tabArg
     * @throws SaadaException 
     * @throws ParsingException 
     */
    public ConfigurationSpectrum(String configName, ArgsParser tabArg) throws SaadaException {
    	
    	super(configName, tabArg);
    	//Initializes the Saada type of generated class in Saada
    	this.categorySaada = Category.SPECTRUM;
    	//Create the new object containing all mapping information of this configuration
    	this.mapping = new MappingSpectrum(this, tabArg);
    }

   /**Returns the name of this spectra configuration
     *@return String the name of this spectra configuration 
     */
    public String getNameSpectra(){
	return nameProduct;
    }
    /**Receive notification of character data inside an element, and sends trace message for every recorded characters arrays
     *@param char[] buf the characters
     *@param int offset the start position in the character array
     *@param int len the number of characters to use from the character array
     *@return void
     */
    public void characters(char buf [], int offset, int len){
    	
    		//Initializes the String value corresponding of this current character array
    	String s = new String(buf, offset, len).trim();
    		//Tests if you can record all characters arrays of this product
    	if (recordElement) {
			if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, s + ", in " + str);
			// If this character array is empty, sends warning and exits
			if (s.equals("")) {
				Messenger.printMsg(Messenger.WARNING, "The element <" + str
						+ "> is empty");
				System.exit(1);
			} else {
				// If you can record this character array (if this array
				// corresponds in the identification array of this recorded
				// product)
				// Records this array
				if (recordIdentification) {
					// See the super class "ConfigurationDefaultHandler"
					recordIdentification(s);
				} else {
					// If this array corresponds in the "coordinating" array of
					// this recorded product, record this array
					if (recordCoordSystem) {
						// See the super class "ConfigurationDefaultHandler"
						recordCoordSystem(s);
					} else {
						// If this array corresponds in the mapping array of
						// this recorded product, record this array
						if (recordMapping) {
							// If the element corresponds in the RA position,
							// records this in the spectra mapping
							if (str.equals("pos_ra")) {
								mapping.setPosRa(s);
								// Update the 2 products md5 (with and without
								// collection)
								s_md5 += s;
								s_md5_without_col += s;
							} else {
								// If the element corresponds in the DEC
								// position, records this in the spectra mapping
								if (str.equals("pos_dec")) {
									mapping.setPosDec(s);
									// Update the 2 products md5 (with and
									// without collection)
									s_md5 += s;
									s_md5_without_col += s;
								} else {
									// Default: records (if that possible) the
									// spectra mapping (generaly instance name
									// and ignored attributes)
									// See the super class
									// "ConfigurationDefaultHandler"
									recordDefaultMapping(s);
								}
							}
						} else{
    	    					// Default: records (if that possible) the
								// collection or class mapping element
    	    					//See the super class "ConfigurationDefaultHandler"
    	    				recordCollectionOrClassMapping(s);
    	    			}
    	    		}
    	    	}
    	    }
		}
	}
    
    /**Receive notification of the start of an element
     *@param Attributes the specified or defaulted attributes
     *@param String qualifiedName the qualified name (with prefix), or the empty string if qualified names are not available
     *@param String simpleName the local name (without prefix), or the empty string if Namespace processing is not being performed
     *@param String namespacesURI the Namespace URI
     *@return void
     */
    public void startElement(String namespaceURI, String simpleName, String qualifiedName, Attributes attrs) {
		
    	String elementName = simpleName;
		if(elementName.equals("")){
		    elementName = qualifiedName;
		    str = elementName;
		}
			//Tests of data type coherence in the xml configuration file
		start = DefineType.getDataType(elementName);
		/*if(elementName.equals(typeData)){
		  if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The product: "+attrs.getValue("name").trim()+ " is present in "+fileConfigXML.getName());
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
		    }else if(elementName.equals("coord_system")){
		    		//If an element name in the recorded product has a simple name "coord_system", record the character data inside this element
			    recordCoordSystem = true;
			}else if(elementName.equals("mapping")){
			    	//If an element name in the recorded product has a simple name "mapping", record the character data inside this element
				recordMapping = true;
		    }else if(elementName.equals("content") && recordIdentification){
					//If an element name in the recorded product has a simple name "content" and if you can record the identification
					//record the character date inside this element
		    	recordContent = true;
			}else if(elementName.equals(Mapping.ATTR_EXT) && recordMapping){
				    //If an element name in the recorded product has a simple name define in the variable "ATTR_EXT" define in class DefineType
				    //generaly "attr_ext", and if you can recorded the "mapping"
				    //record the character date inside this element in the data of the extended attibutes
				String name = attrs.getValue("name").trim();
				String mappingValue = attrs.getValue("mapping").trim();
					//Get the attributes "Hashtable" which maps attribute name to their value
				((LinkedHashMap)mapping.getAttrExt()).put(name, mappingValue);
					//Update the 2 products md5 (with and without collection)
				s_md5 += name+mappingValue;
				s_md5_without_col += name+mappingValue;
		    }else if(elementName.equals("spectral_coordinate")){
			    recordSpectralCoordinate = true;
			}else if(recordSpectralCoordinate){
				if(elementName.equals("abscisse")){
				    String columnA = attrs.getValue("column").trim();
				    String typeA = attrs.getValue("type").trim();
				    String unitA = attrs.getValue("unit").trim();
				    if(columnA!=null || typeA!=null || unitA!=null){
						((MappingSpectrum)mapping).setAbcisseColumn(columnA);
						((MappingSpectrum)mapping).setAbcisseType(typeA);
						((MappingSpectrum)mapping).setAbcisseUnit(unitA);
							//Update the 2 products md5 (with and without collection)
						s_md5 += columnA+typeA+unitA;
						s_md5_without_col += columnA+typeA+unitA;
				    }
				}else if(elementName.equals("ordinate")){
					String columnO = attrs.getValue("column").trim();
					String unitO = attrs.getValue("unit").trim();
					if(columnO!=null || unitO!=null){
					    ((MappingSpectrum)mapping).setOrdinateColumn(columnO);
					    ((MappingSpectrum)mapping).setOrdinateUnit(unitO);
						    //Update the 2 products md5 (with and without collection)
					    s_md5 += columnO+unitO;
					    s_md5_without_col += columnO+unitO;
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
		
    	String elementName = simpleName;
		if(elementName.equals(""))
		    elementName = qualifiedName;
		
		super.endElement(namespaceURI, simpleName, qualifiedName);
		if(recordSpectralCoordinate && elementName.equals("spectral_coordinate"))
		    recordSpectralCoordinate = false;
		
    }
    
 
	public int getSpectralCoordinatePriority() {
		return ((MappingSpectrum)(this.mapping)).getScPriority();
	}


	/**
	 * @return
	 */
	public String getSpectralCordinateUnit() {
		return ((MappingSpectrum)(this.mapping)).getAbcisseUnit();
	}


	/**
	 * @param spectralCordinateUnit
	 */
	public void setSpectralCordinateUnit(String spectralCordinateUnit) {
		((MappingSpectrum)(this.mapping)).setAbcisseUnit(spectralCordinateUnit);
	}


	/* (non-Javadoc)
	 * @see saadadb.prdconfiguration.Configuration#getCategorySaada()
	 */
	public int getCategorySaada() {
		return this.categorySaada;
	}
	/**
	 * @return Returns the spectralCordinateColumn.
	 */
	public String getSpectralCordinateColumn() {
		return ((MappingSpectrum)(this.getMapping())).getAbcisseColumn();
	}
}
  
