package saadadb.query.region.request;

import cds.astro.Astroframe;
import saadadb.query.region.triangule.Point;
import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;

/**
 * Class representing a Cone by the center and the rayon
 * @author jremy
 * @version $Id$
 */
public class Cone extends Zone{

	/**
	 * Attribute rayon which simbolized the rayon of the cone
	 */
	private double rayon;

	/**
	 * This method allows to get the rayon of the Cone
	 * @return double : rayon
	 */
	public double getRayon() {
		return rayon;
	}

	/**
	 * Constructor of a Cone with Double parameters
	 * @param ra : right ascension
	 * @param dec : declinaison
	 * @param rayon : rayon of the cone in degrees
	 * @param Astroframe : current Astroframe (coordinate system)
	 */
	public Cone (double ra, double dec, double rayon, Astroframe currentFrame) throws Exception{
		super(ra,dec);
		//Converting the rayon from degree => radian
		this.rayon = (Math.toRadians(rayon));
		//Check if the cone is at the pole
		super.poleVerification(currentFrame);
	}

	public Cone (double ra, double dec, double rayon, Astroframe currentFrame, String alias) throws Exception{
		super(ra,dec);
		this.alias = alias;
		//Converting the rayon from degree => radian
		this.rayon = (Math.toRadians(rayon));
		//Check if the cone is at the pole
		super.poleVerification(currentFrame);

	}

	/**
	 * Constructor of a Cone with String parameters
	 * @param ra : right ascension
	 * @param dec : declinaison
	 * @param rayon : rayon of the cone in degrees
	 * @param Astroframe : current Astroframe (coordinate system)
	 */
	public Cone (String ra, String dec, String rayon, Astroframe currentFrame) throws Exception{
		super(ra,dec);
		//Converting the rayon from degree => radian
		this.rayon = (Math.toRadians(Double.parseDouble(rayon)));
		//Check if the cone is at the pole
		super.poleVerification(currentFrame);
	}

	/**
	 * This method returns an array of long corresponding to the pixels which are in the Cone
	 * It uses Healpix Method
	 * @param resolution : resolution of the HealpixIndex
	 * @param inclus : boolean
	 * @return long[] : tab of pixels
	 */
	public long[] getPixels(int resolution, boolean inclus) throws Exception{
		int nside = 1 << resolution;
		long [] array = null;
		HealpixIndex hpx = new HealpixIndex(nside);

		//Method from Healpix which returns the array of pixels corresponding to the Cone
		LongRangeSet lrs= hpx.queryDisc(this.getSv(),this.rayon,inclus);
		array = lrs.toArray();
		return array;
	}

	/**
	 * This method allow to get the SQL request with the max Resolution
	 * @return String : SQL request
	 * @throws Exception
	 */
	public String getSQL() throws Exception {
		RequeteCreator rc = new RequeteCreator(this);
		return rc.getWhere();
	}

	/**
	 * This method allows to get the SQL request for the cone depending on the number of segments
	 * @param segmentLimit : number of segments maximum to have
	 * @return String : SQL request
	 * @throws Exception
	 */
	public String getSQL(int segmentLimit) throws Exception {
		RequeteCreator rc = new RequeteCreator(this,segmentLimit);
		if( this.alias != null && this.alias.length() > 0 ){
			rc.setColmunName(this.alias + ".healpix_csa" );
		} 
		return rc.getWhere();
	}

	/**
	 * This method allows to know if the cone is near the pole of the actual system of coordinate
	 * @return boolean : true if the cone is near the pole
	 */
	public boolean checkForPole() {
		boolean ret = false;
		double rayonDeg = Math.toDegrees(this.rayon);

		return( (this.dec + rayonDeg ) > 85 || (this.dec - rayonDeg ) < -85)? true: false;
		//		double rayonDeg = Math.toDegrees(this.rayon);
		//		Point p = new Point(this.ra+rayonDeg,this.dec);
		//		Point pmin = new Point(super.ra+rayonDeg,this.dec);
		//		Point pmax = new Point(this.ra+rayonDeg,this.dec);
		//		if (p.getRa()>85 || p.getRa()<-85 || pmin.getRa()>85 || pmin.getRa()<-85 || pmax.getRa()>85 || pmax.getRa()<-85) {
		//			ret=true;
		//		}
		//		
		//		return ret;
	}

	/**
	 * This method allows to change the system of coordinate of the cone
	 * (only changing the center)
	 */
	public void changeAstroFrame() {
		Point p = new Point(super.ra,super.dec);
		p.changeFrame(currentFrame,processingFrame);		
		super.ra=p.getRa();
		super.dec=p.getDec();
	}







}
