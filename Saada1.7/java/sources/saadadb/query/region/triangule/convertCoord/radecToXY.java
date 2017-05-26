package saadadb.query.region.triangule.convertCoord;

import healpix.core.HealpixIndex;
import healpix.tools.SpatialVector;

import java.util.ArrayList;

import saadadb.query.region.triangule.Point;
import saadadb.query.region.triangule.Polygone;
import saadadb.query.region.triangule.Triangle;
import saadadb.query.region.triangule.Triangulator;
import saadadb.util.Messenger;

/**
 * Class radecToXY
 * Allows to convert from (ra,dec) to (x,y) and conversely
 * @author jremy
 * @version $Id$
 *
 */
public class radecToXY {

	/**
	 * Attribute list : Representing the list of points of the initial polygon
	 */
	public ArrayList<Point> list;

	/**
	 * Atribute center : Representing the center of the polygon by calculating average ra,dec
	 */
	public Point center;

	/**
	 * Constructor radecToXY
	 * Check if the coordinates are good
	 * Set the coordinate for a good processing
	 * @param list : List of Spatial Point (ra,dec)
	 * @throws Exception
	 */
	public radecToXY(ArrayList<Point> list) throws Exception{
		super();
		this.list= new ArrayList<Point>();

		int INDEX=0;
		for (Point ps : list) {
			this.list.add(new Point(INDEX,ps.getRa(),ps.getDec()));
			INDEX++;
		}
			this.check();
		center = new Point(this.getRaMoyenne(),this.getDecMoyenne());
	}
	
	public radecToXY(Polygone poly) throws Exception{
		super();
		this.list= new ArrayList<Point>();

		int INDEX=0;
		for (Point ps : poly.points) {
			this.list.add(new Point(INDEX,ps.getRa(),ps.getDec()));
			INDEX++;
		}
			this.check();
		center = new Point(this.getRaMoyenne(),this.getDecMoyenne());
	}

	/**
	 * This method allow to check : if the polygon is not too big
	 * It also set the ra if the polygon is between 350 and 10 for an example
	 * @throws Exception
	 */
		public void check() throws Exception{
		this.isTooBig();
		this.setRa();
		while (!this.isOkCoordinate()) {
			this.setRa();
		}
		this.isTooBig();
	}

	/**
	 * This method allows to get the "center" of the polygon by calcuting the average of the points 
	 * @return center : Point
	 */
	public Point getCenter() {
		return center;
	}

	/**
	 * This method allows to set the list of points compared with the center to get correct cartesian coordinate
	 * @throws Exception
	 */
	public void convert() throws Exception {
			for (Point ps : list) {
				ps.convertPoint(center);
			}
	}

	/**
	 * This method allows to convert the coordinate of the points to have a good processing
	 * Create the polygon and execute the Triangulator
	 * @return ArrayList<Triangle> : List of Triangle
	 * @throws Exception
	 */
	public ArrayList<Triangle> process() throws Exception {	
		this.convert();		
		Polygone poly = new Polygone(list);
		Triangulator machine = new Triangulator(poly);
		ArrayList<Triangle> ret = machine.process();
		return ret;
	}

	/**
	 * This method allows to get the average ra of the points
	 * @return double : average ra
	 */
	public double getRaMoyenne() {
		double moyenne=0;
		int count=0;
		for (Point ps : list) {
			moyenne+=ps.getX();
			count++;
		}
		return (moyenne/=count);
	}

	/**
	 * This method allow to get the average dec of the points
	 * @return double : average dec
	 */
	public double getDecMoyenne() {
		double moyenne=0;
		int count=0;
		for (Point ps : list) {
			moyenne+=ps.getDec();
			count++;
		}
		return (moyenne/=count);
	}

	/**
	 * This method allows to know if the polygone are too big or if the points are not included in the sky
	 * @throws Exception
	 */
	public void isTooBig() throws Exception {
		for (Point ps : list) {
			for (Point pss : list) {
				if (ps.getX()>360 || pss.getX()>360) {
					Messenger.printMsg(Messenger.ERROR, "Too big ra");
					throw new Exception ("Too big ra");
				}
				Point pss1 = new Point(ps.getX(),0);
				Point pss2 = new Point(pss.getX(),0);
				SpatialVector svv1=pss1.getVector();
				SpatialVector svv2=pss2.getVector();
				double dist2 = Math.toDegrees(HealpixIndex.angDist(svv1, svv2));
				//on teste si la distance angulaire avec dec = 0 est > 180
				if (dist2>180) {
					Messenger.printMsg(Messenger.ERROR, "Too big polygon");
					throw new Exception ("Too big polygon");

				}
			}
		}
	}

	/**
	 * This method allows to know if the list of spatial points are not entierly well made
	 * @return boolean : true if the list is ok
	 */
	public boolean isOkCoordinate() {
		boolean ret = true;
		for (Point ps : list) {
			for (Point pss : list) {
				if (ps.getDistance(pss)>=360) {
					ret=false;
				}
			}
		}
		return ret;
	}

	/**
	 * This method allows to set the ra of each point for a good computing
	 * @throws Exception
	 */
	public void setRa() throws Exception {
		for (int i=0;i<list.size();i++) {
			Point ps1; 
			Point ps2;
			if (list.size()==2) {
				ps2 = list.get(list.size()-1);
				ps1 = list.get(0);
			}
			else {
				if (i==list.size()-1) {
					ps1 = list.get(list.size()-1);
					ps2 = list.get(0);
				}
				else {
					ps1 = list.get(i);
					ps2 = list.get(i+1);
				}
			}
			Point pss1 = new Point(ps1.getX(),0);
			Point pss2 = new Point(ps2.getX(),0);
			double deltara = ps2.getX()-ps1.getX();
			SpatialVector svv1=pss1.getVector();
			SpatialVector svv2=pss2.getVector();
			double dist2 = Math.toDegrees(HealpixIndex.angDist(svv1, svv2));
			if (deltara > dist2-0.0001 && deltara < dist2+0.0001 || Math.abs(deltara) > dist2-0.0001 && Math.abs(deltara) < dist2+0.0001) {
			}
			else {
				if (deltara != Math.abs(deltara)) {
					//deltara negatif => point 1 a gauche
					ps1.setX(-(360-ps1.getX()));
				}
				else {
					ps2.setX(-(360-ps2.getX()));
				}
			}
		}
	}
}