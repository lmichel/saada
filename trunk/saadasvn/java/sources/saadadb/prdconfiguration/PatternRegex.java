package saadadb.prdconfiguration;

import java.io.IOException;
import java.util.regex.Pattern;

import saadadb.products.Product;
import saadadb.util.Messenger;
/**
 *This class serves for describing the constraints of file name in mode "pattern regex"
 *The constraint is on the name of the tested file (product)
 *This name does contain the forces name
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see ProductIdentificationConstraint
 */
public class PatternRegex implements ProductIdentificationConstraint{
    /**Name of the product identification constraint in the case this one is the pattern regex**/
    private String name;
    /**Constructor
     *@param String the constraint name in pattern regex 
     */
    public PatternRegex(String name){
	//Initializes the forces name of the file
	this.name = name;
    }
    /**This method confirms integrity of this constraint in a configuration
     * and sending message in case of no validation
     *@return boolean true or false if this constraint is confirmed
     */
    public boolean isValid(){
	if(name==null){
	   Messenger.printMsg(Messenger.ERROR, getClass().getName()+": Configuration invalid");
	   return false;
	   }
	return true;
    }
    /**This method confirms integrity of a product with this configuration constraint
     * and sending message and IOException in case of no validation
     *@param Product that we want to validate
     *@return boolean true or false if this constraint is confirmed
     */
    public boolean valid(Product product){
	boolean valid = false;
	try{
	    //Tells whether or not this file name matches the given regular expression
	    //The canonical path of the product file is tested
	    //See class Pattern: compile(String regex) Compiles the given expression into a pattern
	    // and matcher(CharSequence input) Creates a matcher that will match the given input against this pattern
	    valid = Pattern.compile(name).matcher(product.getCanonicalPath()).find();
	}catch(IOException e){
	    Messenger.printMsg(Messenger.ERROR, getClass().getName()+": "+name+" invalid in "+product.getName());
	}
	return valid;
    }
    /**Returns the constraint name in pattern regex
     *@return String the constraint name
     */   
    public String getConstraintType(){
	return name;
    }
}
  
