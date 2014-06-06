package saadadb.collection.obscoremin;

import saadadb.collection.Category;
import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.SaadaException;
import saadadb.util.SaadaConstant;

/**
 * @author michel
 * @version $Id$
 */
public class FileRef extends SaadaInstance {

	/*
	 * Public fields are persistent
	 */
	/*
	 * Saada Axe
	 */
	public long date_load = saadadb.util.SaadaConstant.LONG;
	public String repository_location = saadadb.util.SaadaConstant.STRING;
	/*
	 * Observation Axe
	 */	
    public String access_url = saadadb.util.SaadaConstant.STRING;
	public String access_format = saadadb.util.SaadaConstant.STRING;
	public long access_estsize = saadadb.util.SaadaConstant.LONG;
	/*
	 * Space Axe
	 */	
	public double s_fov = saadadb.util.SaadaConstant.DOUBLE;
	public String s_region = saadadb.util.SaadaConstant.STRING;
	/*
	 * Observable Axe
	 */		
	public String o_ucd = saadadb.util.SaadaConstant.STRING;
	public String o_unit = saadadb.util.SaadaConstant.STRING;
	public String o_calib_status = saadadb.util.SaadaConstant.STRING; //{uncalibrated, raw, calibrated
	
	@Override
	public void setAccess_url(String access_url) throws AbortException {
	   	if( access_url == null ) {
    		AbortException.throwNewException(SaadaException.WRONG_PARAMETER, "access_url cannot be set to null");
    	}
		this.access_url = access_url;
		
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#getAccess_url()
	 */
	@Override
	public String getAccess_url() {
		return access_url;
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#setDate_load(long)
	 */
	@Override
	public void setDate_load(long time) {
		this.date_load = time;
		
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#getDate_load()
	 */
	@Override
	public long getDate_load() {
		return this.date_load;
	}
    /* (non-Javadoc)
     * @see saadadb.collection.obscoremin.SaadaInstance#setAccess_format(java.lang.String)
     */
	@Override
    public void setAccess_format(String access_format){
    	this.access_format = access_format;
    }
    /* (non-Javadoc)
     * @see saadadb.collection.obscoremin.SaadaInstance#setAccess_estsize(long)
     */
	@Override
    public void setAccess_estsize (long access_estsize){
    	this.access_estsize = access_estsize;
    }
    /* (non-Javadoc)
     * @see saadadb.collection.obscoremin.SaadaInstance#setS_fov(double)
     */
	@Override
    public void setS_fov(double s_fov){
    	this.s_fov = s_fov;
    }
    /* (non-Javadoc)
     * @see saadadb.collection.obscoremin.SaadaInstance#setS_region(java.lang.String)
     */
	@Override
    public void setS_region(String s_region) {
    	this.s_region = s_region;
    }

	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#getS_fov()
	 */
	@Override
	public double getS_fov() {
		return this.s_fov;
	}
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#getS_region()
	 */
	@Override
	public String getS_region() {
		return this.s_region;
	}

	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#setRepository_location(java.lang.String)
	 */
	@Override
	public void setRepository_location(String repository_location) {
    	this.repository_location = repository_location;
    }
	/* (non-Javadoc)
	 * @see saadadb.collection.obscoremin.SaadaInstance#getRepository_location()
	 */
	@Override
	public String getRepository_location() throws SaadaException {
		if( this.repository_location == null ) {
			return null;
		}
		else if( this.repository_location.indexOf(Database.getSepar()) != -1 ) {
			return this.repository_location;
		} else {
			return Database.getRepository() + Database.getSepar() 
			+ this.getCollection().getName() + Database.getSepar() 
			+ Category.explain(this.getCategory()).toUpperCase() + Database.getSepar() 
			+ this.repository_location;
		}
	}

}
