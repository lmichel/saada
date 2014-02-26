package saadadb.classmapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.Messenger;
/** This class manages the specific mapping in 2D images, according to the present methods by default.
 * At present, it exists nay difference enters this class managing this mapping and the mapping class by default.
 * This class exits only for ease in the furure the development of new specific features.
 *@author Millan Patrick
 *@version $Id: MappingImage.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class MappingImage extends Mapping {
    /**Constructor of the 2D images mapping (Default constructor).
     *@param Configuration the configuration of this mapping. 
     * @throws IgnoreException 
     * @throws FatalException 
     */ 
    public MappingImage(ConfigurationDefaultHandler configuration) throws IgnoreException, FatalException{
		//See MappingDefault (Default constructor)
		super(configuration, null);
    }
    
			    /* ######################################################
				 * 
				 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
				 * 
				 *#######################################################*/

	public MappingImage(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws SaadaException{
	
		super(configuration, tabArg);

    	this.mapPosition(tabArg);
    	this.mapPoserror(tabArg);
	
		Messenger.printMsg(Messenger.TRACE, "The mapping configuration is over.");

	}
}
  
