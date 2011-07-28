package saadadb.classmapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;

public class MappingTable extends Mapping {
	/**Constructor of the tables mapping (Default constructor).
	 *@param Configuration the configuration of this mapping. 
	 * @throws IgnoreException 
	 */   

				/* ######################################################
				 * 
				 * ATTRIBUTES, CONSTRUCTOR AND METHODE FOR THE NEW LOADER
				 * 
				 *#######################################################*/

	/**
	 * @param configuration
	 * @param tabArg
	 * @throws IgnoreException
	 * @throws FatalException 
	 */
	public MappingTable(ConfigurationDefaultHandler configuration, ArgsParser tabArg) throws IgnoreException, FatalException{
		super(configuration, tabArg);
	}
}

