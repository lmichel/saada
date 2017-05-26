package saadadb.prdconfiguration;

import saadadb.products.Product;
/** * @version $Id$

 *Interface for specification of product identification constraint
 *An object that implements the ProductIdentificationConstraint interface can be used in a standard in all Saada application
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 */
public interface ProductIdentificationConstraint {
    
	/**This method must confirms integrity of a product with configuration constraint
     * and must sending message in case of no validation
     *@param Product that we want to validate
     *@return boolean true or false if this constraint is confirmed
     */   
    public boolean valid(Product product);
    
    /**This method confirms integrity of this constraint in a new configuration
     * and must sending message in case of no validation
     *@return boolean true or false if this constraint is confirmed
     */  
    public boolean isValid();
    
    /**Returns name constraint
     *@return String name constraint 
     */ 
    public String getConstraintType();
}
  
