package saadadb.prdconfiguration;

import saadadb.classmapping.MappingMisc;
import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.IgnoreException;

/**
 * @author laurentmichel
 * * @version $Id$

 */
public class ConfigurationFlatfile extends ConfigurationDefaultHandler {

	public ConfigurationFlatfile(String configName, ArgsParser tabArg) throws FatalException, IgnoreException  {
		
		super(configName, tabArg);
			//Initializes the Saada type of generated class in Saada
		this.categorySaada = Category.FLATFILE;
		//Create the new object containing all mapping information of this configuration
		this.mapping = new MappingMisc(this, tabArg);
	}

}
