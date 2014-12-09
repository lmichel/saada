package saadadb.query.region.triangule;

import healpix.tools.SpatialVector;

import java.util.ArrayList;

import saadadb.query.region.triangule.utils.Util;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;

public class Point {
	/**
	 * Attribute indexorigin : Representing the initial position of the point
	 */
	public final int indexorigin;

	/**
	 * Attribute index : Representing the actual position of the point in the polygon
	 */
	private  int  index;

	/**
	 * Attribute ra : Representing the Right Ascension of the point
	 */
	private double ra;

	/**
	 * Attribute dec : Representing the declinaison of the point
	 */
	private double dec;

	/**
	 * Attribute x : Representing the x-axis of the point calculate with the ra.
	 */
	private double x;

	/**
	 * Attribute y : Representing the y-axis of the point calculate with the dec.
	 */
	private double y;

	/**
	 * Attribute alternateIndex : Representing the list of points having the same position in the polygon
	 */
	private ArrayList<Integer> alternateIndex;

	/**
	 * Attribute out : true if the polygon is out of the polygon ( because of the reduced perimeter ) 
	 */
	public boolean out;

	/**
	 * Constructor Point : allows to set the initial index of the point
	 * @param INDEX : Initial index
	 * @param ra : right ascension of the point
	 * @param dec : declinaison of the point
	 */
	public Point (int index, double ra, double dec) {
		alternateIndex = new ArrayList<Integer>();
		this.indexorigin = index;
		this.ra=ra;
		this.dec=dec;
		this.x=ra;
		this.y=dec;
	}

	/**
	 * Constructor Point : allow to create a new Point only with the equatorial coordinate
	 * @param ra : right ascension of the point
	 * @param dec : declinaison of the point
	 */
	public Point (double ra, double dec) {
		alternateIndex = new ArrayList<Integer>();
		this.indexorigin = -1;
		this.ra=ra;
		this.dec=dec;
		this.x=ra;
		this.y=dec;
	}

	/**
	 * This method allow to get the right ascension of the point
	 * @return ra : double
	 */
	public double getRa() {
		return ra;
	}

	/**
	 * This method allow to get the declinaison of the point
	 * @return dec : double
	 */
	public double getDec() {
		return dec;
	}

	/**
	 * This method allows to set the x-axis coordinate of the point
	 * @param x : double
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * This method allow to set the y-axis coordinate of the point
	 * @param y : double
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * This method allows to return the list of the points at the same position
	 * @return ArrayList<Integer> : List of Index of alternate point
	 */
	public ArrayList<Integer> getAlternateIndex() {
		return alternateIndex;
	}

	/**
	 * This method allows to add a point to the list of alternate point 
	 * @param index : index of the point
	 */
	public void setAlternateIndex(int index) {
		alternateIndex.add(index);
	}


	/**
	 * This method returns the index of the current point
	 * @return int : index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * This method allows to modify the index of the point
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
		alternateIndex.add(index);
	}

	/**
	 * This method returns the X coordinate of the point
	 * @return double: X
	 */
	public double getX() {
		return x;
	}

	/**
	 * This method returns the Y coordinate of the point
	 * @return double : Y
	 */
	public double getY() {
		return y;
	}

	/**
	 * This method returns the coordinate of the point and the index
	 * @return String
	 */
	public String toString() {
		return ( ra + "," +  dec+ " , "+indexorigin);
	}

	/**
	 * This method allows to get the String of the equatorial coordinate of the point
	 * @return String
	 */
	public String toStringPolaire() {
		return ( ra + "," +  dec);
	}
	
	/**
	 * Thism ethod allows to get the String of the cartesian coordinate of the point
	 * @return String
	 */
	public String toStringCart() {
		return ( x + "," +  y);
	}

	/**
	 * This method allows to get the distance between 2 points
	 * @param A : first point
	 * @param P : second point
	 * @return double : distance
	 */
	public static double distance(Point A, Point P)
	{
		double dx,dy;
		dx=A.x-P.x;
		dy=A.y-P.y;
		return java.lang.Math.sqrt(dx*dx+dy*dy);
	}

	/**
	 * This method allows to set the point "out"
	 * Meaning the point is out of the current perimeter of the polygon
	 */
	public void setOut () {
		this.out=true;
	}

	/**
	 * This method allows to compare 2 points
	 * @param p : second point to compare
	 * @return boolean : true if the points are at the same coordinate
	 */
	public boolean equals (Point p) {
		if (Util.compare(this.x, p.x) && Util.compare(this.y, p.y)) {
			return true;
		}
		else {
			return false;
		}
	}	

	/**
	 * This method allows to know if the point is on the segment 
	 * @param s : Segment
	 * @return boolean : true if the point is on the segment
	 */
	public boolean isOnSegment (Segment s) {
		double X=s.getX();
		double Y=s.getY();
		double res = X*this.x+Y;
		if ((Util.compare(res, y)) && (x>s.getP0().x && x<s.getP1().x || x<s.getP0().x && x>s.getP1().x) && (y>s.getP0().y && y<s.getP1().y || y<s.getP0().y && y>s.getP1().y)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to know if the point is on the straight line 
	 * @param s : Segment
	 * @return boolean : true if the point is on the straight line
	 */
	public boolean isOnDroite (Segment s) {
		double X=s.getX();
		double Y=s.getY();
		double res = X*this.x+Y;
		if (Util.compare(res, y)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to construct a SpatialVector from the Healpix package from the Point(x,y)
	 * @return SpatialVector
	 */
	public SpatialVector getVector() {
		return new SpatialVector(x,y);
	}

	/**
	 * This method allows to construct a SpatialVector from the Healpix package from the Point(ra,dec)
	 * @return SpatialVector
	 */
	public SpatialVector getSpatialVector() {
		return new SpatialVector(ra,dec);
	}
	/**
	 * This method allow to get the distance between two Point on the X-axis
	 * @param ps : second Point
	 * @return double : distance
	 */
	public double getDistance (Point ps) {
		return Math.abs(this.x-ps.getX());			
	}

	/**
	 * This method allows to set the point in comparaison with the center for a good computing in Cartesian coordinate
	 * @param center : Point (center of the polygon)
	 */
	public void convertPoint(Point center) {
		this.x = (this.getX() - center.getX());
		this.y = (this.getDec() - center.getDec());
	}
	
	/**
	 * This method allow to set the ra of the point
	 * @param ra : new right ascension
	 */
	public void setRa(double ra) {
		this.ra=ra;
	}

	/**
	 * This method allow to set the dec of the point
	 * @param dec : new declinaison
	 */
	public void setDec(double dec) {
		this.dec=dec;
	}
	
	/**
	 * This method allow to change the point from a coordinate system to another
	 * ICRS => Galactic or conversely
	 * @param currentFrame : actual frame of the point
	 * @param processingFrame : frame of converting
	 */
	public void changeFrame(Astroframe currentFrame, Astroframe processingFrame) {
		Astrocoo coo = new Astrocoo(currentFrame,this.getRa(),this.getDec());
		coo.convertTo(processingFrame);
		this.setRa(coo.getLon());
		this.setDec(coo.getLat());
	}
}
