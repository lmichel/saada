package saadadb.prdconfiguration;

import org.xml.sax.SAXException;

import saadadb.classmapping.MappingMisc;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.util.Messenger;

import com.sun.tools.doclets.internal.toolkit.Configuration;
/**
 *This class is specification of a misc configuration
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see ConfigurationDefaultHandler
 *@see Configuration
 */
public class ConfigurationMisc extends ConfigurationDefaultHandler{


	/**
	 * @param configName
	 * @param tabArg
	 * @throws FatalException 
	 * @throws IgnoreException 
	 * @throws ParsingException 
	 */
	public ConfigurationMisc(String configName, ArgsParser tabArg) throws FatalException, IgnoreException {
		
		super(configName, tabArg);
			//Initializes the Saada type of generated class in Saada
		this.categorySaada = Category.MISC;
		//Create the new object containing all mapping information of this configuration
		this.mapping = new MappingMisc(this, tabArg);
		
	}
	/**Returns the name of this misc configuration
	 *@return String the name of this misc configuration 
	 */
	public String getNameMisc(){
		return nameProduct;
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
					//If this array corresponds in the "coordinating" array of this recorded product, record this array
					if(recordCoordSystem){
						//See the super class "ConfigurationDefaultHandler"
						recordCoordSystem(s);
					}else{
						//If this array corresponds in the mapping array of this recorded product, record this array
						if(recordMapping){
							//See the super class "ConfigurationDefaultHandler"
							recordDefaultMapping(s);
						}else{
							//Default: records (if that possible) the collection or class mapping element
							//See the super class "ConfigurationDefaultHandler"
							recordCollectionOrClassMapping(s);
						}
					}
				}
			}
		}
	}
    

	/* (non-Javadoc)
	 * @see saadadb.prdconfiguration.Configuration#getCategorySaada()
	 */
	public int getCategorySaada() {
		return this.categorySaada;
	}
}

