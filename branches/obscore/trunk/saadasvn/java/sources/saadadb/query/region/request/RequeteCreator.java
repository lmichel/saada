package saadadb.query.region.request;

import saadadb.database.Database;
import saadadb.util.Messenger;

/**
 * Class allow to create the Request with the Zone and the Segment's list
 * @author jremy
 * @version $Id$
 */
public class RequeteCreator {

	/**
	 * Attribute Zone used to create the SQL Request 
	 */
	private Zone searchZone;

	/**
	 * Attribut ListeSegment containing all the segment of pixels Healpix 
	 */
	private ListeSegment segmentList;

	/**
	 * Attribut int representing the max resolution for Healpix research in the Database
	 * Default value : 12
	 */
	private int maxResolution = Database.getHeapix_level();

	private String columnName = "healpix_csa";
	/**
	 * Attribute int representing the current resolution of the segment's list  
	 */
	private int currentResolution;

	/**
	 * @param z : 
	 * @throws Exception
	 * Constructor
	 */

	/**
	 * Attribute boolean inclus
	 * Set at true if you want the pixels overflowing
	 * else set at false if you only want the pixel inside the zone
	 */
	private final boolean inclus=true;

	/**
	 * Limit of the size of the request
	 * SQLite can't exceed this limit
	 */
	private final int LIMIT_STRUCTURES=950;

	/**
	 * Limit of the size of the request
	 * The utilisator can set this limit
	 */
	private int LIMIT_NB_SEGMENTS=0;

	/**
	 * Constructor RequeteCreator
	 * It gets the list of segments of pixels corresponding to the zone
	 * Default value of the resolution Healpix : 12
	 * @param searchZone : Zone to treat
	 * @param limit : limit asked by the user
	 * @throws Exception
	 */
	public RequeteCreator (Zone searchZone, int limit) throws Exception {	
		this.searchZone = searchZone;
		this.currentResolution = this.maxResolution;
		this.segmentList=new ListeSegment(this.searchZone.getPixels(this.maxResolution,inclus));
		this.LIMIT_NB_SEGMENTS=limit;
	}
	
	/**
	 * Constructor RequeteCreator
	 * It gets the list of segments of pixels corresponding to the zone
	 * @param searchZone : Zone to treat
	 * @param maxResolution : Resolution maximum of Healpix research in the database
	 * @param limit : limit asked by the user
	 * @throws Exception
	 */
	public RequeteCreator (Zone searchZone, int maxResolution, int limit) throws Exception {	
		this.maxResolution=maxResolution;
		this.searchZone = searchZone;
		this.currentResolution = this.maxResolution;
		this.segmentList=new ListeSegment(this.searchZone.getPixels(this.maxResolution,inclus));
		this.LIMIT_NB_SEGMENTS=limit;
	}

	/**
	 * Constructor RequeteCreator
	 * Default value of the resolution Healpix : 12
	 * @param ls : ListeSegment : List of segments of pixels
	 * @param limit : limit asked by the user
	 * @throws Exception
	 */
	public RequeteCreator (ListeSegment ls, int limit) throws Exception {
		this.segmentList=ls;
		this.currentResolution = this.maxResolution;
		this.LIMIT_NB_SEGMENTS=limit;
	}

	/**
	 * Constructor RequeteCreator
	 * Default value of the resolution Healpix : 12
	 * @param ls : ListeSegment : List of segments of pixels
	 * @throws Exception
	 */
	public RequeteCreator (ListeSegment ls) throws Exception {
		this.segmentList=ls;
		this.currentResolution = this.maxResolution;
	}

	/**
	 * Constructor RequeteCreator
	 * Default value of the resolution Healpix : 12
	 * It gets the list of segments of pixels corresponding to the zone
	 * @param searchZone : Zone to treat
	 * @throws Exception
	 */
	public RequeteCreator (Zone searchZone) throws Exception {	
		this.searchZone     = searchZone;
		this.currentResolution = this.maxResolution;
		this.segmentList=new ListeSegment(this.searchZone.getPixels(this.maxResolution,inclus));
	}

	public void setColmunName(String columnName){
		this.columnName = columnName;
	}
	/**
	 * This method returns the request corresponding to the zone
	 * The resolution decreases if the request is too big
	 * @return
	 * @throws Exception
	 */
	public String getWhere() throws Exception{
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Current Healpix resolution: " + this.currentResolution);
		while (this.limitReached()) {
			this.degradeResolution();
		}
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Request Size : "+this.segmentList.getListSize());
		return this.segmentList.sqlString(this.columnName);	
	}

	/**
	 * This method allows to degrade the resolution of Healpix by 1 for the Zone 
	 * @throws Exception
	 */
	public void degradeResolution() throws Exception{
		this.currentResolution --;
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Current Resolution : "+this.currentResolution);
		this.segmentList.setListeDeg(maxResolution-this.currentResolution);
	}

	/**
	 * This method is an access to the searchZone
	 * @return Zone
	 */
	public Zone getSearchZone() {
		return searchZone;
	}

	/**
	 * This method is an access to the segmentList
	 * @return ListeSegment
	 */
	public ListeSegment getSegmentList() {
		return segmentList;
	}

	/**
	 * This method is an access to the maxResolution
	 * @return int
	 */
	public int getMaxRresolution() {
		return maxResolution;
	}

	/**
	 * This method is an acces to the currentResolution
	 * @return int
	 */
	public int getCurrentResolution() {
		return currentResolution;
	}

	/**
	 * This method allow to know if the size of the request is too big for SQLite  
	 * @return boolean : true if it can't work
	 */
	public boolean limitReached() {
		boolean ret=false;
		if (isLimited()) {
			if (segmentList.getListSize() > LIMIT_NB_SEGMENTS) {
				ret=true;
			}
		}
		else {
			if (segmentList.getListSize() > LIMIT_STRUCTURES) {
				ret=true;
			}
		}
		return ret;
	}

	/**
	 * This method allows to check if the limit given is under the static limit
	 * @return boolean
	 */
	public boolean isLimited() {
		boolean ret=false;
		if (this.LIMIT_NB_SEGMENTS!=0) {
			if (LIMIT_STRUCTURES>LIMIT_NB_SEGMENTS) {
				return true;
			}
			else {
				return false;
			}
		}
		return ret;
	}
}

