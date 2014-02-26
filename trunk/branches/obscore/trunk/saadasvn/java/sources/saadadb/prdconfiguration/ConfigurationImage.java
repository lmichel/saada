package saadadb.prdconfiguration;

import saadadb.classmapping.MappingImage;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.util.Messenger;

;
/** * @version $Id$

 *This class is specification of a image2D configuration
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see ConfigurationDefaultHandler
 *@see Configuration
 */
public class ConfigurationImage extends ConfigurationDefaultHandler {

	/**
	/**Returns the name of this image2D configuration
	 *@return String the name of this image2D configuration
	 */
	public String getNameImage2D(){
		return nameProduct;
	}
	
	public ConfigurationImage(String configName, ArgsParser tabArg) throws SaadaException {
		
		super(configName,tabArg );
		//Initializes the Saada type of generated class in Saada
		this.categorySaada = Category.IMAGE;
		//Create the new object containing all mapping information of this configuration
		this.mapping = new MappingImage(this, tabArg);

	}
	
	/* (non-Javadoc)
	 * @see saadadb.prdconfiguration.Configuration#getCategorySaada()
	 */
	public int getCategorySaada() {
		return this.categorySaada;
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
	
	
}

