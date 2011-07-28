package saadadb.api;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;

/**
 * @author michel
 *
 */
public class SaadaAttribute extends SaadaDMBrik{
    private AttributeHandler handler = null;	
    private SaadaClass saada_class = null;
    
    /**
     * @param saadadb
     * @param name
     * @throws SaadaException 
     */
	SaadaAttribute(SaadaClass saada_class, String name) throws FatalException{
		super(name);
		this.saada_class = saada_class;
        AttributeHandler[] hdls = Database.getCachemeta().getClassAttributes(this.saada_class.getName());
        if( hdls != null ) {
            for( int i=0 ; i<hdls.length ; i++ ) {
            	if( hdls[i].getNameattr().equals(name) ) {
            		this.handler = hdls[i];
            		break;
            	}
            }
            if( this.handler == null ) {
               	FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't get attribute handler for class  " + this.saada_class.getName());   	    	
            }
        }
        else {
        	FatalException.throwNewException(SaadaException.METADATA_ERROR, "Can't get attributes for class  " + this.saada_class.getName());         	
        }
   }
	
   /**
    * @return
    */
	public SaadaClass getSaadaClass() {
	   	return this.saada_class;
	}
	
	/**
     * @return
     */
    public String getNameorg() {
		if( this.handler != null ) {
			return this.handler.getNameorg();
			}
		else {
			return null;
			}
        }
	/**
     * @return
     */
    public String getUcd() {
		if( this.handler != null ) {
			return this.handler.getUcd();
			}
		else {
			return null;
			}
        }
   /**
     * @return
     */
    public String getType() {
		if( this.handler != null ) {
			return this.handler.getType();
			}
		else {
			return null;
			}
        }
    /**
     * @return
     */
    public String getComment() {
		if( this.handler != null ) {
				return this.handler.getComment();
			}
		else {
			return null;
			}
        }
    
    /**
     * @return
     */
    public String getUnit() {
		if( this.handler != null ) {
			return this.handler.getUnit();
			}
		else {
			return null;
			}
        }
		
    /* (non-Javadoc)
     * @see saadadb.api.Saada_DM_Brik#explain()
     */
    public void explains() {
		System.out.print("Attribute : " + this.name + " of class " + this.saada_class.getName() + "(" + this.saada_class.getCategory() + ")");
		System.out.println("  original name:" + this.getNameorg()
		           + " UCD:" + this.getUcd()
		           + " type:" + this.getType()
		           + " comment:" + this.getComment()
		           ) ;	
    	
    }

	/**
	 * @return Returns the handler.
	 */
	public AttributeHandler getHandler() {
		return handler;
	}
}
  
