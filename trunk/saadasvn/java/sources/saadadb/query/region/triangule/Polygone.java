package saadadb.query.region.triangule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saadadb.query.region.triangule.utils.Util;
import saadadb.util.Messenger;
/**
 * Class Polygone representing the polygon to process with the list of points and segments
 * @author jremy
 * @version $Id$
 * 01/2014: Detection if 2 adjacent points have the same position
 */
public class Polygone {

	/**
	 * Attribute ArrayList<Point>
	 * Representing the list of Points ordered in the clockwise order
	 */
	public ArrayList<Point> points;

	/**
	 * Attribute ArrayList<Segment>
	 * Representing the list of Segments of the polygon
	 */
	public ArrayList<Segment> segments;

	/**
	 * Constructor Polygone :  
	 * Set the list in the clockwise order
	 * Set the index of the points 
	 * Build the list of segments
	 * Check if the polygon if self-intersecting
	 * Check if the polygon has a wrong direction
	 * @param pts : List of Points
	 * @throws Exception 
	 */

	public Polygone (List<Point> pts) throws Exception {
		points=new ArrayList<Point>();
		/*
		 * Remove duplicate points because they generate flat triangles which are removed 
		 * That make the triangulator failing
		 */
		for( int p=0 ; p<(pts.size()-1) ; p++){
			Point pi = pts.get(p);
			if( pi.equals(pts.get(p+1)) ) {
				Messenger.printMsg(Messenger.DEBUG, "Point " + pi + " duplicated: ignored ");
			} else {
				points.add(pi);
			}
		}
		if (!this.getDirection()) {
			Collections.reverse(points);
			Messenger.printMsg(Messenger.DEBUG, "Inversion Polygon");
		}
		for( int i=0 ; i<points.size() ; i++){
			points.get(i).setIndex(i);
		}
		for (Point p : points) {
			for (Point pp : points) {
				if (p.equals(pp) && p.getIndex()!=pp.getIndex()) {
					p.setAlternateIndex(pp.getIndex());
					pp.setAlternateIndex(p.getIndex());
				}
			}
		}
		this.buildSegments();

		if (this.selfIntersection()) {
			Messenger.printMsg(Messenger.ERROR, "SelfIntersecting Polygon");
			throw new Exception("SelfIntersecting polygon");
		}
		if (!this.sensParcours()) {
			Messenger.printMsg(Messenger.ERROR, "Wrong Direction");
			throw new Exception("Wrong Direction");
		}
	}

	/**
	 * This method allows to know if the polygon is selfintersecting
	 * @return boolean : true if the polygon is selfintersecting
	 */
	public boolean selfIntersection() {
		Point ip;
		for (Segment s : this.segments)
		{
			for (Segment ss : this.segments)
			{
				ip = s.segmentIntersct( ss, true );
				if ( ip != null ) {
					if (ip.isOnSegment(s) && ip.isOnSegment(ss)) {
						
						if (!(s.getP0().equals(ss.getP0()) || s.getP0().equals(ss.getP1())) && !(s.getP1().equals(ss.getP0()) || s.getP1().equals(ss.getP1()) )) {
							Messenger.printMsg(Messenger.DEBUG, "SelfIntersection on : "+s+ " || "+ss);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method allows to know if the polygon keep the same direction
	 * @return boolean : true if the direction is good
	 */
	public boolean sensParcours () {
		boolean ret = true;
		for (Point p : points) {
			for (Point pp : points) {
				if (p.equals(pp) && p.getIndex()<pp.getIndex()) {

					Point pprev = this.getPrev(p);
					Point pnext = this.getNext(p);
					Point ppprev = this.getPrev(pp);
					Segment s = new Segment (p,pprev);
					Segment ss = new Segment (p,pnext);
					Segment sss = new Segment (p,ppprev);

					double ang1 = s.getAngle(ss);
					double ang2 = s.getAngle(sss);
					if (Util.isSuperior(ang1, ang2)) { 
						Messenger.printMsg(Messenger.DEBUG, "Wrong Side of Polygon after this point : "+p);
						ret=false;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Construct the list of segments of the polygon in the order of the points of the polygon
	 */
	public void buildSegments() {
		Segment s;
		segments=new ArrayList<Segment>();
		for ( int i = 0; i < this.points.size()-1; i++ )
		{
			Point point1 = this.points.get(i);
			Point point2 = this.points.get(i+1);
			s = new Segment( point1, point2 );
			segments.add( s );
		}
		s = new Segment( this.points.get(this.points.size()-1), this.points.get(0));
		segments.add(s);
	}

	/**
	 * This method allows to get the next point of the polygon
	 * @param p : current point
	 * @return Point : next point
	 */
	public Point getNext(Point p) {
		return getNext(p.getIndex());
	}

	/**
	 * This method allows to get the next point of the polygon
	 * @param index : index of the current point
	 * @return Point : next point
	 */
	public Point getNext(int index) {
		if (index == points.size()-1) {
			index=0;
		}
		else {
			index++;
		}
		return points.get(index);
	}

	/**
	 * This method allows to get the previous point of the polygon
	 * @param p : current point
	 * @return : previous point
	 */
	public Point getPrev(Point p) {
		return getPrev(p.getIndex());
	}

	/**
	 * This method allows to get the previous point of the polygon
	 * @param index : index of the current point
	 * @return Point : previous point
	 */
	public Point getPrev(int index) {
		if (index == 0) {
			index=points.size()-1;
		}
		else {
			index--;
		}
		return points.get(index);
	}

	/**
	 * This method allows to know if the the built triangle is concave or not
	 * @param p0 : Point
	 * @param p1 : Point
	 * @param p2 : Point
	 * @return boolean : true if the triangle is concave
	 */
	public boolean pointConcave (Point p0,Point p1, Point p2){
		double zed = getZ(p0,p1,p2);
		if (Util.isSuperior(zed, 0)) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to get the value of the concavity of the 3 points
	 * @param p0 : Point
	 * @param p1 : Point
	 * @param p2 : Point
	 * @return double : Z
	 */
	public double getZ (Point p0,Point p1, Point p2){
		double z;
		z  = (p1.getX() - p0.getX()) * (p2.getY() - p1.getY());
		z -= (p1.getY() - p0.getY()) * (p2.getX() - p1.getX());
		return z;
	}

	/**
	 * This method allows to get the direction of the the list of points of the polygon
	 * clockwise or counterclockwise
	 * @return boolean : true if it's clockwise
	 */
	public boolean getDirection() {
		boolean clockwise=true;
		double z=0;
		double zplus=0;
		Point center = this.getXtraPoint();
		for (int i=0;i<points.size()-1;i++) {
			Point p0=this.points.get(i);
			Point p1=this.points.get(i+1);
			z=getZ(p0, center, p1);
			zplus+=z;
		}
		Point p0=this.points.get(points.size()-1);
		Point p1=this.points.get(0);
		z=getZ(p0, center, p1);
		zplus+=z;
		if (zplus<0) {
			clockwise=false;
		}
		return clockwise;
	}

	/**
	 * This method returns the String of the polygon to use it on gnuplot
	 * @return Gnuplot Command
	 */
	public String getPolyGnuplot() {
		String retour;
		double xmin = this.points.get(0).getRa();
		double xmax = xmin;
		double ymin = this.points.get(0).getDec();
		double ymax = ymin;
		for( Point p: this.points ){
			double ra = p.getRa();
			double dec = p.getDec();
			if( ra < xmin ) xmin = ra;
			if( ra > xmax ) xmax = ra;
			if( dec < ymin ) ymin = dec;
			if( dec > ymax ) ymax = dec;
		}
		retour = "set xrange[" + ((int)(xmin-1.)) + ":" + ((int)(xmax+2.)) + "]\n";
		retour = "set xyrange[" + ((int)(ymin-1.)) + ":" + ((int)(ymax+2.)) + "]\n";
		
		int i=1;
		retour += "set object 1 polygon";
		retour+=" from "+points.get(0);
		while (i<points.size()) {
			String pointeur = " to "+points.get(i);
			retour+=pointeur;
			i++;
		}
		retour+=" to " + points.get(0);
		retour+=" behind fillcolor rgbcolor \"green\" fs solid";
		return retour;
	}
	/**
	 * This method allows to know if the current polygon is a triangle or not
	 * @return boolean : true if the current polygon is a triangle
	 */
	public boolean isTriangle() {
		int i=0;
		for (Point p : points) {
			if (!p.out) {
				i++;
			}
		}
		if (i==3) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allow to know if the current polygone is a triangle or less
	 * Meaning that the triangulation is over
	 * @return boolean : true if the triangulation has to finish
	 */
	public boolean isOver() {
		int i=0;
		for (Point p : points) {
			if (!p.out) {
				i++;
			}
		}
		if (i==1 || i==2 || i==3) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to get the last triangle of the polygon
	 * @return Triangle
	 */
	public Triangle getTriangle() throws Exception{
		ArrayList<Point> array = new ArrayList<Point>();
		for (Point p : points) {
			if (!p.out) {
				array.add(p);
			}
		}
		Triangle t = new Triangle(array);
		return t;
	}

	/**
	 * returns true if the segment binding the points index1 to the point index2
	 * belongs to the perimeter of the polygon
	 * @param index1: index of the first ear
	 * @param index2: index of the first ear
	 * @return boolean : true if the segment belongs to the perimeter
	 */
	public boolean belongsToThePerimeter(int index1, int index2){
		return ( index2 == this.getNext(index1).getIndex() || index2 == this.getPrev(index1).getIndex());
	}

	/**
	 * This method allows to know if the segment is a perimeter of a polygon
	 * @param s : Segment to test
	 * @return boolean : true if the Segment belongs to the perimeter
	 */
	public boolean belongsToThePerimeter (Segment s) {
		boolean sfound = false;
		Point p0= s.getP0();
		Point p1= s.getP1();
		ArrayList<Integer> alterp0 = p0.getAlternateIndex();
		ArrayList<Integer> alterp1 = p1.getAlternateIndex();
		for (int i : alterp0) {
			for (int j : alterp1) {
				sfound = this.belongsToThePerimeter(i,j);
				if (sfound) {
					return true;
				}
			}
		}
		return sfound;
	}

	/**
	 * This method allows to know if the triangle is a entire part of the polygon
	 * @param t : Triangle to test
	 * @return boolean : true if the 3 segments of the triangle belongs to the perimeter
	 */
	public boolean is3SegmentPerimetre(Triangle t) {
		Segment s0 = t.getS0();
		Segment s1 = t.getS1();
		Segment s2 = t.getS2();

		boolean s0found=this.belongsToThePerimeter(s0);
		boolean s1found=this.belongsToThePerimeter(s1);
		boolean s2found=this.belongsToThePerimeter(s2);

		if (s0found && s1found && s2found) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method allows to know if the external segment of the current triangle cut the perimeter of the polygon
	 * @return boolean : true if it's intersecting
	 */
	public boolean isSecantPerimetre(Segment s) {
		int count = 0;
		for (Point p : points) {
			if (p.isOnSegment(s)) {
				count++;
			}
		}
		if (count > 2) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * This method alows to create a new Polygon with only the valid point
	 * (only with the points which aren't out
	 * @return ArrayList<Point> : new polygone
	 */
	public ArrayList<Point> newPolygone () {
		ArrayList<Point> ret = new ArrayList<Point>();
		for (Point p : points) {
			if (p.out==false) {
				p.getAlternateIndex().clear();
				ret.add(p);
			}
		}
		return ret;
	}
	/**
	 * This method allows to get the point with the best Y and the lowest X to calculate the direction of the polygon
	 * @return Point
	 */
	public Point getXtraPoint () {
		return new Point(getLeftX(),getTopY());
	}

	public double getXMoyenne() {
		double moyenne=0;
		int count=0;
		for (Point ps : points) {
			moyenne+=ps.getX();
			count++;
		}
		return (moyenne/=count);
	}

	public double getYMoyenne() {
		double moyenne=0;
		int count=0;
		for (Point ps : points) {
			moyenne+=ps.getX();
			count++;
		}
		return (moyenne/=count);
	}

	/**
	 * This method allows to get the the the lowest X of the list of point
	 * @return double : X
	 */
	public double getLeftX() {
		double x = Double.MAX_VALUE;
		for (Point ps : points) {
			if (ps.getX() < x) {
				x=ps.getX();
			}
		}
		return x-200;
	}

	/**
	 * This method allows to get the the the biggest Y of the list of point
	 * @return double : Y
	 */
	public double getTopY() {
		double y = Double.MIN_VALUE;
		for (Point ps : points) {
			if (ps.getY() > y) {
				y=ps.getY();
			}
		}
		return y+200;
	}

}