package saadadb.classmapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.products.Product;
import saadadb.util.Messenger;

public class MappingEntry extends Mapping {
 
    /**This method validates the mapping of the product in parameter by default, 
     * validates the right ascencion and the declension in the equatorial system of coordinates of a celestial body in the product,
     * and sends trace message if necessary. 
     *@param Product the tested product.
     *@return boolean true or false if the tested product is valid.
     */
    public boolean isProductValid(Product product){
    	if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "Check if ENTRY mapping is valid " + product.getClass().getName());

    	return true;
    }
    /**Sets the class name of products in this mapping configuration of generated class Saada,
     * and creates the object modelling the mapping type used in this configuration.
     * If the mapping type (the value in parameter) is the name of the loaded product in configuration file,
     * the used mode is the user mode.
     *@param String The name of mapping type, or the class name in user mode. 
     *@return void.
     */
    public void setTypeMapping(String typeMapping){
    	if(typeMapping.equals("MAPPING_1_1_SAADA")){
    		//this.className = typeMapping;
    		mapping = new Mapping_1_1(configuration, typeMapping, className);
    		if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_1_1_SAADA class: " + className);
    	}else  if(typeMapping.equals("MAPPING_CLASSIFIER_SAADA")){
    		//this.className = typeMapping;
    		mapping = new Mapping_Classifier(configuration, typeMapping, className);
    		if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_CLASSIFIER_SAADA class: " + className);
    	}else{
    		//If the mapping type is the name of the loaded product in configuration file, the used mode is the user mode
    		//this.className = typeMapping+"Entry";
    		mapping = new Mapping_User(configuration, "MAPPING_USER_SAADA", className);
    		if (Messenger.debug_mode)
				Messenger.printMsg(Messenger.DEBUG, "Mapping: MAPPING_USER_SAADA class: " + className);
    		
    	}
    }
     
    
				
					/* ######################################################
					 * 
					 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
					 * 
					 *#######################################################*/
    
    /**
     * @param configuration
     * @param tabArg
     * @throws SaadaException 
     */
    public MappingEntry(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws SaadaException{

		super(configuration, tabArg);
    	this.mapPosition(tabArg);
    	this.mapPoserror(tabArg);
		if( Messenger.debug_mode ) Messenger.printMsg(Messenger.DEBUG, "The mapping configuration is over.");

	}
}
  
