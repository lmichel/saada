package saadadb.api;

import saadadb.exceptions.SaadaException;


/**
 * @author michel
 * * @version $Id$

 */
class SaadaDMBrik {
    protected String name;
    
    SaadaDMBrik(String name){
    	this.name = name;
    }
    
    /**
     * @return
     */
    public String getName() {
		return this.name;
        }
    
    /**
     * @throws Exception 
     * @throws CollectionException 
     * @throws Exception 
     * 
     */
    public void explains() throws SaadaException, Exception {
    	System.out.println("DM brik " + this.name);
    }
}
  
