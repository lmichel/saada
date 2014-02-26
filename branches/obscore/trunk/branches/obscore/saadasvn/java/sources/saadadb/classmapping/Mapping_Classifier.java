package saadadb.classmapping;

import saadadb.prdconfiguration.ConfigurationDefaultHandler;

/**
 * @author laurent
 * @version $Id$*
 */
public class Mapping_Classifier extends TypeMapping{ 
    /**Constructor.
     *@param Configuration the configuration of this mapping.
     *@param String the mapping type of this configuration.
     *@param String the class name of products described by this configuration.
     */
    public Mapping_Classifier(ConfigurationDefaultHandler configuration, String typeMapping, String className){
	this.configuration = configuration;
	this.typeMapping = typeMapping;
	this.className = className;
    }  
    /**Tests if the class name is null.
     *@return boolean true or false if the class name is null, or not.
     */
    public boolean isValid(){
	return (className!=null);
    }
}
  
