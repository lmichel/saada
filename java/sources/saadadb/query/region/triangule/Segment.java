package saadadb.query.region.triangule;

import saadadb.query.region.triangule.utils.Util;


/**
 * Class Segment : Representing a segment of two points
 * @author jremy
 * @version $Id$
 *
 */
public class Segment {
	/**
	 * Attribute p0 : First point of the Segment
	 */
	private Point p0;

	/**
	 * Attribute p1 : Second point of the Segment
	 */
	private Point p1;

	/**
	 * Constructor Segment
	 * @param p0 : first point
	 * @param p1 : second point
	 */
	public Segment(Point p0,Point p1) 
	{
		this.p0 = p0;
		this.p1 = p1;
	}


	/**
	 * This method allow to know if a segment intersect another segment
	 * Method found on http://keith-hair.net/blog/2008/08/04/find-intersection-point-of-two-lines-in-as3/
	 * @param s : second Segment
	 * @param as_seg : set true for a segment, false for a straight line
	 * @return Point : point of intersection of the segment or null if there is no intersection
	 */
	public Point segmentIntersct(Segment s,boolean as_seg ) 
	{
		boolean Booldenom=false;
		boolean Booldistance=false;
		Point ip;
		double a1;
		double a2;
		double b1;
		double b2;
		double c1;
		double c2;

		Point A = p0;
		Point B = p1;
		Point E = s.p0;
		Point F = s.p1;

		a1= B.getY()-A.getY();
		b1= A.getX()-B.getX();
		c1= B.getX()*A.getY() - A.getX()*B.getY();
		a2= F.getY()-E.getY();
		b2= E.getX()-F.getX();
		c2= F.getX()*E.getY() - E.getX()*F.getY();

		double denom=a1*b2 - a2*b1;
		if(Util.compare(denom, 0)){
			Booldenom=true;
		}
		ip=new Point(-1,-1);
		ip.setX((b1*c2 - b2*c1)/denom);
		ip.setY((a2*c1 - a1*c2)/denom);
		//---------------------------------------------------
		//Do checks to see if intersection to endpoints
		//distance is longer than actual Segments.
		//Return null if it is with any.
		//if the point is on the segment, there is no intersection
		//---------------------------------------------------

		if(as_seg){
			if(Point.distance(ip,B) >= Point.distance(A,B)){
				Booldistance=true;
			}
			if(Point.distance(ip,A) >= Point.distance(A,B)){
				Booldistance=true;
			}	
			if(Point.distance(ip,F) >= Point.distance(E,F)){
				Booldistance=true;
			}
			if(Point.distance(ip,E) >= Point.distance(E,F)){
				Booldistance=true;
			}
		}
		if (Booldenom || Booldistance) {
			return null;
		}
		else {
			return ip;
		}
	}

	/**
	 * This method allows to compare 2 segments
	 * @param s : second Segment
	 * @return boolean : true if the segment are equals
	 */
	public boolean equals(Segment s)
	{
		return ( ( p0 == s.p0 && p1 == s.p1 ) || ( p0 == s.p1 && p1 == s.p0 ) );
	}

	/**
	 * This method returns the String of the Segment with the two index of the points
	 * @return String
	 */
	public String toString() {
		return (p0+" & "+p1);
	}

	/**
	 * This method allows to get the distance of the segment
	 * @return double : distance
	 */
	public double getDistance () {
		return Point.distance(p0,p1);
	}

	/**
	 * This method allows to get the X of the equation of the segment
	 * @return double : X
	 */
	public double getX() {
		double res;
		if ((p1.getX()-p0.getX())!=0) {
			res=((p1.getY()-p0.getY())/(p1.getX()-p0.getX()));
		}
		else {
			res=((p0.getY()-p1.getY())/(p0.getX()-p1.getX()));
		}
		return res;
	}
	/**
	 * These methods allow to get the points of the Segment
	 * @return
	 */
	public Point getP0() {
		return p0;
	}

	public Point getP1() {
		return p1;
	}

	/**
	 * This method allows to get the Y of the equation of the segment
	 * @return double : Y
	 */
	public double getY() {
		return (p0.getY() - this.getX()*p0.getX());
	}

	/**
	 * This method calculate the cross product of two segments
	 * @param s : second segment
	 * @return double : cross product
	 */
	public double prodScalR(Segment s) {
		double x1 = s.getP1().getX()-s.getP0().getX();
		double x2 = this.getP1().getX()-this.getP0().getX();
		double y1 = s.getP1().getY()-s.getP0().getY();
		double y2 = this.getP1().getY()-this.getP0().getY();

		return x1*x2+y1*y2;
	}

	/**
	 * This method returns the Angle between two segments
	 * @param s : second segment
	 * @return double : Angle in degrees
	 */
	public double getAngle(Segment s) {
		double ds = s.getDistance();
		double dss = this.getDistance();
		double res = Math.acos(this.prodScalR(s)/(ds*dss));
		return Math.toDegrees(res);
	}

}