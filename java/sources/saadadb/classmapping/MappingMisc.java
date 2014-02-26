package saadadb.classmapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
/** This class manages the specific mapping in miscs, according to the present methods by default.
 * At present, it exists nay difference enters this class managing this mapping and the mapping class by default.
 * This class exits only for ease in the furure the development of new specific features.
 *@author Millan Patrick
 *@version $Id: MappingMisc.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class MappingMisc extends Mapping {
	/**Constructor of the miscs mapping (Default constructor).
	 *@param Configuration the configuration of this mapping. 
	 * @throws IgnoreException 
	 * @throws FatalException 
	 */    
	public MappingMisc(ConfigurationDefaultHandler configuration) throws IgnoreException, FatalException{
		//See MappingDefault (Default constructor)
		super(configuration, null);
	}
	

    
			    /* ######################################################
				 * 
				 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
				 * 
				 *#######################################################*/

	public MappingMisc(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws IgnoreException, FatalException{

		super(configuration, tabArg);

	}
}

