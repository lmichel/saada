package saadadb.query.region.triangule;

import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;

import java.util.ArrayList;


import saadadb.util.Messenger;
import saadadb.query.region.request.Zone;
import saadadb.query.region.triangule.convertCoord.ConvertGalac;
import cds.astro.Astroframe;

/**
 * Class Triangle representing a built triangle by the program
 * @author jremy
 * @version $Id$
 *
 */
public class Triangle extends Zone{
	/**
	 * These points represent the three points of the triangle
	 */
	private Point p0;
	private Point p1;
	private Point p2;

	/**
	 * These segments represent the three semgents of the triangle
	 */
	private Segment s0;
	private Segment s1;
	private Segment s2;

	/**
	 * Constructor Triangle with three points in parameter
	 * @param p0
	 * @param p1
	 * @param p2
	 */
	public Triangle(Point p0,Point p1,Point p2) 
	{
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		s0= new Segment(p0,p1);
		s1= new Segment(p1,p2);
		s2= new Segment(p0,p2);
	}

	/**
	 * Constructor Triangle with a list of points
	 * @param array : List of points (3)
	 */
	public Triangle(ArrayList<Point> array) throws Exception{
		if (array.size()!=3) {
			Messenger.printMsg(Messenger.ERROR, "Created triangle with more or less than 3 points");
			throw new Exception ("Created triangle with more or less than 3 points");
		}
		this.p0=array.get(0);
		this.p1=array.get(1);
		this.p2=array.get(2);
	}
	/**
	 * These methods allow to get the Segments of the Triangle
	 */
	public Segment getS0() {
		return s0;
	}

	public Segment getS1() {
		return s1;
	}

	public Segment getS2() {
		return s2;
	}

	/**
	 * These methods allow to get the points of the Triangle
	 */
	public Point getP0() {
		return p0;
	}

	public Point getP1() {
		return p1;
	}

	public Point getP2() {
		return p2;
	}

	/**
	 * This method returns the String with the three points of the triangle
	 * @return String
	 */
	public String toString() {
		return (p0+" & "+p1+" & "+p2);
	}

	/**
	 * This method allows to know if the triangle has equal point
	 * @return boolean : true if two points are equals
	 */
	public boolean hasEqualPoints () {
		if (p0.equals(p1) || p0.equals(p2) || p1.equals(p2)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to get the String of the triangle to use it on gnuplot
	 * @return String
	 */
	public String getTriangle() {
		return ("from "+p0+" to "+p1+" to "+p2+" to "+p0); 
	}

	/**
	 * This method allows to know if the triangle is flat
	 * @return boolean : true  if the three points of the triangle are aligned
	 */
	public boolean isAligned() {
		Segment s = new Segment(p0,p1);
		Segment ss = new Segment(p1,p2);
		double angle=Math.round(s.getAngle(ss));
		if (angle == 180 || angle == 0) {
			return true;
		}
		else return false;
	}

	/**
	 * This abstract method returns an array of long corresponding to the pixels which are in the Triangle
	 * It use Healpix Method
	 * @param resolution : resolution of the HealpixIndex
	 * @return long[]
	 * @throws Exception 
	 */
	public long[] getPixels(int resolution, boolean inclus) {
		long incl;
		if (inclus) {
			incl=1;
		}
		else {
			incl=0;
		}
		int nside = (int) Math.pow(2,resolution);
		long [] array = null;
		try {
			HealpixIndex hpx = new HealpixIndex(nside);
			//Method from Healpix which returns the array of pixels corresponding to the Region
			LongRangeSet lrs= hpx.query_triangle(nside,p0.getSpatialVector(),p1.getSpatialVector(),p2.getSpatialVector(),1,incl);
			array = lrs.toArray();
		}

		catch (Exception e) {
			e.printStackTrace();
		}


		return array;
	}

	/**
	 * This method allows to set the Coordinate from Galactic to Equatorial or conversely
	 * @throws Exception
	 */
	public void reSetFrame(Astroframe actualFrame) throws Exception{
		ArrayList<Point> array = new ArrayList<Point>();
		array.add(p0);
		array.add(p1);
		array.add(p2);
		ConvertGalac cg = new ConvertGalac(array);
		cg.setModeCoordonnees(actualFrame);
		array=cg.list;
		p0=array.get(0);
		p1=array.get(1);
		p2=array.get(2);
	}


	/**
	 * This method allows to know if the segments of the triangleintersect with one of the list of segments
	 * @param array : List of Segment
	 * @return boolean : true if there is an intersection
	 */
	public boolean intersect (ArrayList<Segment> array) {
		Segment s1 = new Segment(p0,p1);
		Segment s2 = new Segment(p1,p2);
		Segment s3 = new Segment(p0,p2);
		boolean intersect=false;
		for (Segment s : array) {
			Point punto = s1.segmentIntersct(s, true);
			if (punto!=null) {
				if (!punto.equals(s1.getP0()) && !punto.equals(s1.getP1())) {
					intersect=true;
				}
			}
			punto = s2.segmentIntersct(s, true);
			if (punto!=null) {
				if (!punto.equals(s2.getP0()) && !punto.equals(s2.getP1())) {
					intersect=true;
				}
			}
			punto = s3.segmentIntersct(s, true);
			if (punto!=null) {
				if ((!punto.equals(s3.getP0()) && !punto.equals(s3.getP1())) ) {
					intersect=true;
				}
			}
		}
		return intersect;
	}

	/**
	 * This method allows to know if the external segment of the triangle cut a point of the polygon
	 * @param array : List of point
	 * @return boolean : true if there is an intersection with a point
	 */
	public boolean intersectPointPerimetre (ArrayList<Point> array) {
		Segment s3 = new Segment(p0,p2);
		boolean intersect=false;
		for (Point punto : array) {
			if (punto!=null) {
				if ((!punto.equals(s3.getP0()) && !punto.equals(s3.getP1())) && (punto.isOnSegment(s3))) {
					intersect=true;
				}
			}
		}
		return intersect;
	}

	/**
	 * This method allows to know if a triangle is equal to another one
	 * @param t : another Triangle
	 * @return boolean : true if the triangles are the same
	 */
	public boolean triangleEquals (Triangle t) {
		boolean res = false;
		if ((this.p0.equals(t.p0) || this.p0.equals(t.p1) || this.p0.equals(t.p2)) && (this.p1.equals(t.p0) || this.p1.equals(t.p1) || this.p1.equals(t.p2)) && (this.p2.equals(t.p0) || this.p2.equals(t.p1) || this.p2.equals(t.p2))) {
			res=true;
		}
		else {
			res=false;
		}
		return res;
	}

	/**
	 * This method allows to get the Aladin command to draw the triangle
	 * @return String : Aladin command
	 */
	public String getAladin() {
		return ("draw polygon "+p0.toStringPolaire()+" "+p1.toStringPolaire()+" "+p2.toStringPolaire());
	}
	
	/**
	 * This method returns the String of the triangle to use it on gnuplot
	 * Warning : The calling method need to be preceded by "set object (number)"
	 * @return Gnuplot Command
	 */
	public String getTriangleGnuplot() {
		String ret=" polygon";
		ret+=" from "+p0;
		ret+=" to " + p1;
		ret+=" to " + p2;
		ret+=" to " + p0;
		return ret;
	}

	/**
	 * This method allows to know if the triangle contains the point
	 * @param p : Point
	 * @return boolean : true if the triangle contains the point
	 */
	public boolean contains(Point p) {
		if(( determinant( p0, p1, p ) >= 0 ) && ( determinant( p1, p2, p ) >= 0 ) && ( determinant( p2, p0, p ) >= 0 ))	{
			return true;
		}
		if(( determinant( p0, p1, p ) <= 0 ) && ( determinant( p1, p2, p ) <= 0 ) && ( determinant( p2, p0, p ) <= 0 ))	{
			return true;
		}
		return false;
	}

	/**
	 * This method allows to determinate the determinant of the points
	 * @param p0 : Point
	 * @param p1 : Point
	 * @param p2 : Point
	 * @return double : determinant
	 */
	public double determinant(Point p0,Point p1,Point p2) {
		return ((p0.getX()-p1.getX()) * ( p2.getY()-p1.getY())) - ((p2.getX()-p1.getX()) * (p0.getY()-p1.getY()));
	}

	/**
	 * Method unused in Triangle
	 */
	public String getSQL() throws Exception {
		return null;
	}

	/**
	 * Method unused in Triangle
	 */
	public String getSQL(int resolution) throws Exception {
		return null;
	}

	/**
	 * Unused method in Triangle
	 */
	public boolean checkForPole() {
		return false;
	}

	/**
	 * Unused method in Triangle
	 */
	public void changeAstroFrame() {
	}
}

