package saadadb.prdconfiguration;

import saadadb.products.Product;
import saadadb.util.Messenger;
/**
 *This class serves for describing the constraints of the file contents
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see ProductIdentificationConstraint
 */
public class Content implements ProductIdentificationConstraint{
    /**Name of the attribute constraint in file contents**/
    private String attribute = "";
    /**Value that must correspond to this attribute in file contents**/
    private String value = "";
    /**Default constructor 
     */
    public Content(){}
    /**Constructor
     *@param String Name of the attribute constraint in file contents
     *@param String Value that must correspond to this attribute in file contents
     */
    public Content(String attribute, String value){
	//Initializes the attribute constaint in file contents
	this.attribute = attribute;
	//Initialzes the attribute value in the file contents
	this.value = value;
    }
     /**This method confirms integrity of this constraint in a configuration
     * and sending message in case of value, attribute, or value and attribute no validation
     *@return boolean true or false if this constraint is confirmed
     */   
    public boolean isValid(){
	if(value.equals("") && attribute.equals("")){
	    Messenger.printMsg(Messenger.ERROR, getClass().getName()+": configuration <value and attribute> is invalid");
	    return false;
	}
	if(value.equals("")){
	    Messenger.printMsg(Messenger.ERROR, getClass().getName()+": configuration <value> is invalid");
	    return false;
	}
	if(attribute.equals("")){
	    Messenger.printMsg(Messenger.ERROR, getClass().getName()+": configuration <attribute> is invalid");
	    return false;
	}
	return true;
    }
    /**Returns the value constraint
     *@return String the value constraint 
     */
    public String getValue(){
	return value;
    }
    /**Returns the attribute constraint
     *@return String the attribute constraint 
     */
    public String getAttribute(){
	return attribute;
    }
    /**Method to set the value constraint
     *@param String the value constraint
     *@return void
     */
    public void setValue(String value){
	this.value = value;
    }
    /**Method to set the attribute constraint
     *@param String the attribute constraint
     *@return void
     */
    public void setAttribute(String attribute){
	this.attribute = attribute;
    }
    /**This method confirms integrity of a product with this configuration constraint
     * and sending message in case of attribute or corresponding value no validation
     *@param Product that we want to validate
     *@return boolean true or false if this constraint is confirmed
     */
    public boolean valid(Product product){
    	//Takes the value corresponding to the attribute specified, in the constraint, in the product file
    	String test = product.getKWValueQuickly(attribute);
    	if(test!=null){
    		if(test.equals(value)){
    			return true;
    		}else{
    			Messenger.printMsg(Messenger.TRACE, getClass().getName()+": Attribute <" + attribute + "=" +value+"> doesn't match the config in product "+product.getName());
    			return false;
    		}
    	}
		Messenger.printMsg(Messenger.TRACE, getClass().getName()+": Attribute <" + attribute + "=" +value+"> doesn't match the config in product "+product.getName());
    	return  false;
    	      }

    /**Returns the attribute and value constraint in file content
     *@return String the attribute and value constraint according to this syntax: "attribute_value" 
     */ 
    public String getConstraintType(){
	return attribute+"_"+value;
    }
}
  
