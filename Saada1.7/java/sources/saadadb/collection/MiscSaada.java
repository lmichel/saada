package saadadb.collection;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;
/**
 * <p>Title: System d'archivage automatique des donnes astrononique</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Observaroire Astronomique Strasbourg</p>
 * @author XXXX
 * @version 00000001
 */ 
public class MiscSaada extends SaadaInstance{
    
    public String product_url_csa = SaadaConstant.STRING;
 
    public void setProduct_url_csa(String product_url_csa) throws AbortException{
    	if( product_url_csa == null ) {
    		AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "product_url_csa cannot be set to null");
    	}
    	this.product_url_csa = product_url_csa;
    }


	/**
	 * @return
	 */
	public String getMimeType() {
		return getMimeType(this.product_url_csa);
	}
 
	@Override
	public String getProduct_url_csa() {
		return this.product_url_csa;
	}

}
  
