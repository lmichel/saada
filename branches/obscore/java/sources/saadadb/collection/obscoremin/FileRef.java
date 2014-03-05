package saadadb.collection.obscoremin;

import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;

public class FileRef extends SaadaInstance {

	/*
	 * Public fields are persistent
	 */
	/*
	 * Saada Axe
	 */
	public long date_load          = saadadb.util.SaadaConstant.LONG;
	/*
	 * Observation Axe
	 */	
    public String access_url = saadadb.util.SaadaConstant.STRING;
	public String access_format = saadadb.util.SaadaConstant.STRING;
	public String access_estsize = saadadb.util.SaadaConstant.STRING;
	/*
	 * Observation Axe
	 */	
	public double s_fov = saadadb.util.SaadaConstant.DOUBLE;
	public String s_region = saadadb.util.SaadaConstant.STRING;
	
	@Override
	public void setAccess_url(String access_url) throws AbortException {
	   	if( access_url == null ) {
    		AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "access_url cannot be set to null");
    	}
		this.access_url = access_url;
		
	}
	@Override
	public String getAccess_url() {
		return access_url;
	}
	@Override
	public void setDate_load(long time) throws AbortException {
		this.date_load = time;
		
	}
	@Override
	public long getDate_load() {
		return this.date_load;
	}

	
}
