package saadadb.query.region.request;

import healpix.tools.SpatialVector;
import saadadb.util.Messenger;
import cds.astro.Astroframe;
import cds.astro.Galactic;
import cds.astro.ICRS;
/**
 * Class abstract representing to treat
 * @author jremy
 * @version $Id$
 */

public abstract class Zone {

	/**
	 * Attribute ra : corresponding to the right ascension of the center of the Zone
	 */
	protected double ra;

	/**
	 * Attribute dec : corresponding to the declinaison of the center of the Zone
	 */
	protected double dec;

	
	/**
	 * Attribute SpatialVector from Healpix
	 * Correspond to the Vector of the center of the Zone
	 */
	protected SpatialVector sv;
	
	/**
	 * Atribute boolean nearPole
	 * true if the Zone is near the pole
	 */
	protected boolean nearPole;
	
	/**
	 * Attribute Astroframe currentFrame
	 * Representing the entry Astroframe of the Zone
	 */
	protected Astroframe currentFrame;
	
	/**
	 * Attribute Astroframe processingFrame
	 * Representing the processing Astroframe of the Zone
	 */
	protected Astroframe processingFrame;
	protected String alias = "";
	/**
	 * Constructor with the center of the Zone
	 * @param ra : double
	 * @param dec : double
	 */
	
	public Zone(double ra, double dec) {
		this.ra = ra;
		this.dec = dec;
		sv=new SpatialVector(this.ra,this.dec);
	}

	/**
	 * Constructor with the Center of the Zone
	 * @param ra : String
	 * @param dec : String
	 */
	public Zone (String ra, String dec) {
		this.ra = Double.parseDouble(ra);
		this.dec = Double.parseDouble(dec);
	}
	
	/**
	 * This method allow to set the AstroFrame to process the Zone
	 * To handle the pole case
	 * @param currentFrame : Astroframe of the given Zone
	 */
	protected void poleVerification(Astroframe currentFrame) throws Exception{
		this.currentFrame = currentFrame;
		nearPole=checkForPole();
		if(nearPole) {
			Messenger.printMsg(Messenger.TRACE, "Changing coordinate - Polygon near pole");
			if (this.currentFrame.name=="Galactic") {
				this.processingFrame = new ICRS();
			}
			else {
				this.processingFrame = new Galactic();
			}
		} else {
			this.processingFrame = this.currentFrame ;
		}
		changeAstroFrame();
	}
	
	public Zone() {
	}
	/**
	 * This abstract method returns an array of long corresponding to the pixels which are in the Zone
	 * It use Healpix Method
	 * @param resolution : resolution of the HealpixIndex
	 * @return long[]
	 * @throws Exception 
	 */
	public abstract long [] getPixels(int resolution, boolean inclus) throws Exception;

	/**
	 * This method allow to get the SQL request with the max Resolution
	 * @return String : SQL request
	 * @throws Exception
	 */
	public abstract String getSQL() throws Exception;
	
	/**
	 * This method allows to get the SQL request for the polygon depending on the resolution
	 * @param resolution : Healpix resolution
	 * @return String : SQL request
	 * @throws Exception
	 */
	public abstract String getSQL(int resolution) throws Exception;
	
	/**
	 * This method returns the SpatialVector representing the center of the Zone
	 * @return SpatialVector
	 */
	public SpatialVector getSv() {
		return sv;
	}
	
	/**
	 * Abstract method to check if the Zone is near the pole
	 * @return boolean
	 */
	public abstract boolean checkForPole();
	
	/**
	 * Abstract method to change the coordinate system of the zone
	 * @throws Exception 
	 */
	public abstract void changeAstroFrame() throws Exception;

}
