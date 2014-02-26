package saadadb.query.region.triangule.convertCoord;

import java.util.ArrayList;

import saadadb.query.region.triangule.Point;


/**
 * @author jremy
 * @version $Id$
 * Box is the zone where the polygon have to be
 * This algorithm have a slight margin of error
 *
 */
public class ConvertBox {

	/**
	 * Point of min ra and min dec of the box
	 */
	public Point boxmin;

	/**
	 * Point of max ra and max dec of the box
	 */
	public Point boxmax;

	/**
	 * Point of min ra and min dec of the polygon
	 */
	public Point polymin;

	/**
	 * Point of max ra and max dec of the polygon
	 */
	public Point polymax;

	/**
	 * Double representing the coefficient of scaling in x-axis
	 */
	public double ax;

	/**
	 * Double representing the coefficient of scaling in y-axis
	 */
	public double ay;

	/**
	 * Double representing the coefficient of translation in x-axis
	 */
	public double bx;

	/**
	 * Double representing the coefficient of translation in y-axis
	 */
	public double by;

	/**
	 * Point representing the center of the box
	 */
	public Point boxcenter;

	/**
	 * Point representing the center of the polygon
	 */
	public Point polycenter;

	/**
	 * List of points representing the polygon to move
	 */
	public ArrayList<Point> points;


	/**
	 * Constructor of ConvertBox
	 * @param points : list of points of the polygon
	 * @param ramin : ramin of the box
	 * @param ramax : ramax of the box
	 * @param decmin : decmin of the box
	 * @param decmax : decmax of the box
	 * @throws Exception
	 */
	public ConvertBox (ArrayList<Point> points, double ramin, double ramax, double decmin, double decmax) throws Exception{
		this.points=points;
		Point min = new Point (ramin,decmin);
		Point max = new Point (ramax,decmax);
		ArrayList<Point> extrem= new ArrayList<Point>();
		extrem.add(min);
		extrem.add(max);
		radecToXY conv= new radecToXY(extrem);
		extrem=conv.list;
		this.boxmin=extrem.get(0);
		this.boxmax=extrem.get(1);
		this.getMinMax();
		this.getCoeff();
		this.boxcenter=conv.getCenter();
		radecToXY conv2 = new radecToXY (points);
		this.polycenter=conv2.getCenter();
		this.getVecteur();

	}

	/**
	 * Constructor ConvertBox
	 * @param points : ArrayList<Point>
	 * Set le min and the max of the list of points
	 * @throws Exception
	 */
	public ConvertBox(ArrayList<Point> points) throws Exception{
		this.points=points;
		radecToXY conv= new radecToXY(points);
		this.points=conv.list;
		this.getMinMax();
	}

	public Point getMin() {
		return polymin;
	}
	public Point getMax() {
		return polymax;
	}

	/**
	 * This method allows to check if the polygon corresponding to the list of points is smaller than the box
	 * @param points : list of points
	 * @return boolean : true if one of the points beyonde the limit of the box
	 */
	public boolean isSized (ArrayList<Point> points) {
		boolean ret=true;
		double xmin=Double.MAX_VALUE;
		double xmax=Double.MIN_VALUE;
		double ymin=Double.MAX_VALUE;
		double ymax=Double.MIN_VALUE;
		for (Point p : points) {
			if (p.getX() < xmin) {
				xmin=p.getX();
			}
			if (p.getX() > xmax) {
				xmax=p.getX();
			}
			if (p.getY() < ymin) {
				ymin=p.getY();
			}
			if (p.getY() > ymax) {
				ymax=p.getY();
			}
		}
		double xdelta=xmax-xmin;
		double ydelta=ymax-ymin;
		double radelta=boxmax.getX()-boxmin.getX();
		double decdelta=boxmax.getY()-boxmin.getY();
		if (xdelta>radelta || ydelta>decdelta) {
			ret=false;
		}
		return ret;
	}

	/**
	 * This method allows to check if the points are in the box
	 * @param points : list of points
	 * @return boolean
	 */
	public boolean isInBox (ArrayList<Point> points) {
		boolean ret=true;
		for (Point p : points) {
			if (p.getX() > boxmax.getX() || p.getX() < boxmin.getX() || p.getY() > boxmax.getY() || p.getY() < boxmin.getY()) {
				ret = false;
			}
		}
		return ret;	
	}

	/**
	 * This method allows to set the extremities of the polygon
	 */
	public void getMinMax () {
		double xmin=Double.MAX_VALUE;
		double xmax=Double.MIN_VALUE;
		double ymin=Double.MAX_VALUE;
		double ymax=Double.MIN_VALUE;
		for (Point p : points) {
			if (p.getX() < xmin) {
				xmin=p.getX();
			}
			if (p.getX() > xmax) {
				xmax=p.getX();
			}
			if (p.getY() < ymin) {
				ymin=p.getY();
			}
			if (p.getY() > ymax) {
				ymax=p.getY();
			}
		}
		polymin=new Point(xmin,ymin);
		polymax=new Point(xmax,ymax);
	}

	/**
	 * This method allows to get the coefficients of translation
	 */
	public void getVecteur () {
		this.bx = boxcenter.getX()-polycenter.getX();
		this.by = boxcenter.getY()-polycenter.getY();
	}

	/**
	 * This method allows to get the coefficient of scaling 
	 */
	public void getCoeff() {
		this.ax = (boxmax.getX()-boxmin.getX()) / (polymax.getX()-polymin.getX());

		this.ay = (boxmax.getY()-boxmin.getY()) / (polymax.getY()-polymin.getY());
	}

	/**
	 * This method allows to scale the point
	 * @param p : Point to scale
	 * @return Point
	 */
	public Point scale (Point p) {
		double x = p.getX()*ax;
		double y = p.getY()*ay;
		return new Point (x,y);
	}

	/**
	 * This method allows to translate the point
	 * @param p : Point ot translate
	 * @return Point
	 */
	public Point translate (Point p) {
		double x = p.getX()+bx;
		double y = p.getY()+by;
		return new Point (x,y);
	}

	/**
	 * This method allows to impress the attributes of the class
	 */
	public void imprim () {
		System.out.println("Boxmin : "+boxmin.toStringCart());
		System.out.println("Boxmax : "+boxmax.toStringCart());
		System.out.println("");
		System.out.println("Polymin : "+polymin.toStringCart());
		System.out.println("polymax : "+polymax.toStringCart());
		System.out.println("");
		System.out.println("ax : "+ax);
		System.out.println("bx : "+bx);
		System.out.println("");
		System.out.println("ay : "+ay);
		System.out.println("by : "+by);
		System.out.println("");
	}

	/**
	 * This method allows to execute the main algorithm to put the polygon into the box
	 * @param arg : Polygon
	 * @param ramin of the box
	 * @param ramax of the box
	 * @param decmin of the box
	 * @param decmax of the box
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Point> execute (ArrayList<Point> arg, double ramin, double ramax, double decmin, double decmax) throws Exception{
		ArrayList<Point> array = new ArrayList<Point>(arg);
		ConvertBox cb = new ConvertBox(array,ramin,ramax,decmin,decmax);
		ArrayList<Point> array2 = new ArrayList<Point>();
		if (!cb.isSized(array)) {
			for (Point p : array) {
				Point ps = cb.scale(p);
				array2.add(ps);
			}
		}
		else {
			array2=array;
		}
		ArrayList<Point> array3 = new ArrayList<Point>();
		ConvertBox cb2=new ConvertBox(array2,ramin,ramax,decmin,decmax);
		for (Point p : array2) {
			Point ps = cb2.translate(p);
			array3.add(ps);
		}
		return array3;
	}
}
