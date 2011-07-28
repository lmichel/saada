package saadadb.classmapping;

import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.prdconfiguration.ConfigurationDefaultHandler;
import saadadb.util.SaadaConstant;
/** This class provides a skeletal implementation of a object describing the mapping type,
 * to minimize the effort required to create a new mapping type.
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public abstract class TypeMapping{
	public static final int MAPPING_11 = 1;
	public static final int MAPPING_USER = 2;
	public static final int MAPPING_CLASSIFIER = 3;
	
	/**
	 * @param m
	 * @return
	 * @throws SaadaException 
	 */
	public static final String explain(int m) throws FatalException {
		switch(m){
		case MAPPING_11: return "MAPPING_11";
		case MAPPING_USER: return "MAPPING_USER";
		case MAPPING_CLASSIFIER: return "MAPPING_CLASSIFIER";
		default: FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, " Attempt to use a inexistant mapping number: " + m);
		return null;
		}
	}
	
	/**
	 * @param map
	 * @return
	 * @throws SaadaException 
	 */
	public static final int getMapping(String map) throws FatalException {
		String lcmap = map.toLowerCase();
		if( lcmap.matches("mapping.*1.*1")) {
			return MAPPING_11;
		}
		else if( lcmap.matches("mapping.*user")) {
			return MAPPING_USER;
		}
		else if( lcmap.matches("mapping.*class.*")) {
			return MAPPING_CLASSIFIER;
		}
		else {
			FatalException.throwNewException(SaadaException.UNSUPPORTED_MODE, " Attempt to use a inexistant mapping mode: " + map);
			return SaadaConstant.INT;
		}
	}
    /**Configuration of this mapping**/
    public ConfigurationDefaultHandler configuration;
    /**Configuration type**/
    public String typeMapping;
    /**Class name of this configuration**/
    public String className;
    /**Tests if the class name is null.
     *@return boolean true or false if the class name is null, or not.
     */
    public abstract boolean isValid();
    /**Returns the configuration type.
     *@return String The configuration type.
     */
    public String getTypeMapping(){
	return typeMapping;
    }
    /**Returns the class name of this configuration.
     *@return String The class name of this configuration.
     */
    public String getClassName(){
	return className;
    }
    /**Returns the configuration of this mapping.
     *@return Configuration The configuration of this mapping.
     */
    public ConfigurationDefaultHandler getConfiguration(){
	return configuration;
    }
}
  
