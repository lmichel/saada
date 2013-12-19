package saadadb.query.region.request;

import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;
import healpix.tools.SpatialVector;

import java.util.ArrayList;

import saadadb.query.region.triangule.Point;
import saadadb.query.region.triangule.Polygone;
import saadadb.query.region.triangule.Triangle;
import saadadb.query.region.triangule.Triangleur;
import saadadb.query.region.triangule.convertCoord.ConvertGalac;
import saadadb.util.Messenger;
import cds.astro.Astroframe;

/**
 * Class representing a Region
 * With a list of points of the region
 * @author jremy
 * @version $Id$
 */
public class Region extends Zone{

	/**
	 * Attribute points representing the list of points of the polygon
	 */

	private ArrayList<Point> points;

	/**
	 * Attribute triangles representing the list of triangles of the polygone
	 */
	private ArrayList<Triangle> triangles;
	
	/**
	 * Attribute ListeSegmentListSegmentFinal
	 * Representing the final list of Segment of pixels of the Zone
	 */
	protected ListeSegment ListSegmentFinal;

	/**
	 * Constructor Region
	 * @param p : Polygone to treat
	 * @param af : current Astroframe
	 */
	public Region (Polygone p, Astroframe currentFrame) throws Exception{
		this.points=p.points;	
		super.poleVerification(currentFrame);
	}
	/**
	 * Constructor
	 * @param points : Array of Point (Polygon)
	 * @param af : current Astroframe
	 */
	public Region (ArrayList<Point> points, Astroframe currentFrame) throws Exception {
		this.points=points;
		super.poleVerification(currentFrame);
	}

	/**
	 * This method allows to get the list of Triangle for the concave polygon
	 * It can put the polygone into the wished box
	 * It also delete the aligned triangled and update the triangle in the good coordinate system (Astroframe)
	 * @throws Exception
	 */
	public void getTriangles() throws Exception{
		//this.points=ConvertBox.execute(this.points,-1,1,-1,1); // this method puts roughly the polygon into the box
		Triangleur t = new Triangleur(this.points);
		this.triangles=t.execute();
		for (int i=0;i<this.triangles.size();i++) {
			Triangle tri = this.triangles.get(i);
			if (!tri.isAligned()) {
				if (nearPole) {
					tri.reSetFrame(processingFrame);
				}
			}
			else {
				Messenger.printMsg(Messenger.DEBUG, "Aligned triangle removed : "+tri);
				this.triangles.remove(tri);
			}
		}
		Messenger.printMsg(Messenger.TRACE, "Triangle number : "+this.triangles.size());
	}

/**
 * This method allows to get the list of segment of healpix pixel from the list of triangles	
 * @throws Exception
 */
	public void setListSegmentFinal() throws Exception{
		
		ListSegmentFinal = new ListeSegment();
		for (Triangle t : triangles) {
			RequeteCreator rc = new RequeteCreator(t);
			ListeSegment fusion = rc.getSegmentList();
			ListSegmentFinal.fusion(fusion);
		}
		ListSegmentFinal.sort();
		ListSegmentFinal.degrade();
	}
	/**
	 * This method allows to get the SQL request with the max Resolution
	 * @return String : SQL request
	 * @throws Exception
	 */
	public String getSQL() throws Exception {
		/*Here you can put a method to test if the region is convex
		 * In this case, use the getPixels() method
		 * then use Constructor RequeteCreator(this)
		 * and get the where with getWhere()
		 * else, execute this.getTriangles()
		 */
		this.getTriangles();
		this.setListSegmentFinal();
		RequeteCreator Rket = new RequeteCreator(ListSegmentFinal);
		String where = Rket.getWhere();
		return where;
	}

	/**
	 * Method only used for Convex Polygon
	 * @param resolution : resolution of the HealpixIndex
	 * @param inclus : boolean
	 * @return long[] : tab of pixels
	 */
	public long[] getPixels(int resolution, boolean inclus) throws Exception {
		ArrayList<SpatialVector> VectorList = new ArrayList<SpatialVector>();
		for (Point p : points) {
			VectorList.add(p.getVector());
		}
		int nside = (int) Math.pow(2,resolution);
		long [] array = null;
		HealpixIndex hpx = new HealpixIndex(nside);
		long inclusive=0;
		if (inclus) {
			inclusive=1;
		}
		//Method from Healpix which returns the array of pixels corresponding to the Region
		LongRangeSet lrs= hpx.query_polygon(nside, VectorList,1,inclusive);
		array = lrs.toArray();
		return array;
	}

	/**
	 * This method allows to get the SQL request for the polygon depending on the number of segments
	 * @param segmentLimit : number of segments maximum to have
	 * @return String : SQL request
	 * @throws Exception
	 */
	public String getSQL(int segmentLimit) throws Exception {
		this.getTriangles();
		ListeSegment listSegmentFinal = new ListeSegment();
		for (Triangle t : triangles) {
			RequeteCreator rc = new RequeteCreator(t);
			ListeSegment fusion = rc.getSegmentList();
			listSegmentFinal.fusion(fusion);
		}
		listSegmentFinal.sort();
		listSegmentFinal.degrade();

		RequeteCreator Rket = new RequeteCreator(listSegmentFinal,segmentLimit);
		String where = Rket.getWhere();
		return where;
	}	

	/**
	 * This method allows to know if the points are not at the pole
	 * @return boolean : true if it's ok
	 */
	public boolean checkForPole() {
		boolean ret = false;
		for (Point ps : this.points) {
			if (ps.getDec()>85 || ps.getDec()<-85) {
				ret=true;
			}
			else {		
			}
		}
		return ret;
	}

	/**
	 * This method allows to change the coordinates of the points if the region is near the pole
	 * Coordinate equatorial to galactique or conversely
	 */
	public void changeAstroFrame() throws Exception{	
		if (nearPole) {
			ConvertGalac cg = new ConvertGalac(this.points);
			cg.setModeCoordonnees(currentFrame);;
			this.points=cg.list;
		}
	}
}
