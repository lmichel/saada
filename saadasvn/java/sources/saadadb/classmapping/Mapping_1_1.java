package saadadb.classmapping;


import saadadb.prdconfiguration.ConfigurationDefaultHandler;
/** This class models the mapping in 1_1 mode.
 * (One product creates one class).
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see TypeMapping
 */
public class Mapping_1_1 extends TypeMapping{
    /**Constructor.
     *@param Configuration the configuration of this mapping.
     *@param String the mapping type of this configuration.
     *@param String the class name of products described by this configuration.
     */
    public Mapping_1_1(ConfigurationDefaultHandler configuration, String typeMapping, String className){
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
  
