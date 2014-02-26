package saadadb.classmapping;

import saadadb.prdconfiguration.ConfigurationDefaultHandler;
/** This class models the mapping in user mode.
 *@author Millan Patrick
 *@version $Id: Mapping_User.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 */
public class Mapping_User extends TypeMapping{ 
    /**Constructor.
     *@param Configuration the configuration of this mapping.
     *@param String the mapping type of this configuration.
     *@param String the class name of products described by this configuration.
     */
    public Mapping_User(ConfigurationDefaultHandler configuration, String typeMapping, String className){
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
  
