package saadadb.prdconfiguration;

import java.util.Enumeration;
import java.util.Vector;

import saadadb.products.Product;
/**This class serves of use as center to the constraints management imposed in files.
 *It is a list of these constraints, in the form of a table (exactly a Vector),
 *with a method of iterative global validation on all these components (constraints) 
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see Vector
 */
public class ProductSignature extends Vector{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**Default constructor
     *See Vector
     *Constructs a empty vector so that its internal data array has size 10 and its standard capacity increment is zero
     */
    public ProductSignature(){
	//See the super class Vector
	super();
    }
    /**Appends the specified constraint to the end of this Vector
     *@param ProductIdentificationConstraint the specified constraint
     *@return boolean true (as per the general contract of Collection.add)
     */
    public boolean add(ProductIdentificationConstraint constraint){
	//See the super class Vector
	return super.add(constraint);
    }
    /**Returns the elemnt at the specified position in this Vector
     *@param int index of element to return
     *@return ProductIdentificationConstraint at the specified index
     *@throws ArrayIndexOutOfBoundsException index is out of range (index<0 || index>=size())
     */
    public ProductIdentificationConstraint getConstraint(int index){
	//See the super class Vector
	return (ProductIdentificationConstraint)super.get(index);
    }
    
    
    /**This method confirms integrity of all constraints in a configuration
     *@return boolean true or false if all constraints are confirmed
     */ 
    public boolean isValid(){
		
    	//Initializes an enumeration of the components of this vector
		Enumeration e = elements();
		
		//Localize a ProductIdentificationConstraint
		ProductIdentificationConstraint constraint;
		
		//Tests if this enumeration contains more elements
		while(e.hasMoreElements()){
		    //The localized constraint equals the next element of this enumeration
		    //if this enumeration of constraints has at least one more element to provide 
		    constraint = (ProductIdentificationConstraint)e.nextElement();
		    
		    //If this constraint is not valid, return false
		    //(see the interface ProductIdentificationConstraint)
		    if(!constraint.isValid())
		    	return false;
		    
		}
		return true;
    }
    /**This method confirms integrity of a product with all constraints in a configuration
     *@param Product that we want to validate
     *@return boolean true or false if all constraints are confirmed
     */
    public boolean valid(Product product){
		//Initializes an enumeration of the components of this vector
		Enumeration e = elements();
		//Localize a ProductIdentificationConstraint
		ProductIdentificationConstraint constraint;
		//Tests if this enumeration contains more elements
		while(e.hasMoreElements()){
		    //The localized constraint equals the next element of this enumeration
		    //if this enumeration of constraints has at least one more element to provide
		    constraint = (ProductIdentificationConstraint)e.nextElement();
		    //If this constraint is not valid in the product, return false
		    //(see the interface ProductIdentificationConstraint)
		    if(!constraint.valid(product)){
		    	return false;
		    }
		}
		//else return true
		return true;
    }
}
  
