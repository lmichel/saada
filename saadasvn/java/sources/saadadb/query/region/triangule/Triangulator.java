package saadadb.query.region.triangule;

import java.util.ArrayList;

import saadadb.util.Messenger;

/**
 * Class Test executing the main method for the triangulation
 * @author jremy
 * @version $Id$
 *
 */
public class Triangulator {

	/**
	 * Attribute polygone : Polygone currently computing
	 */
	private Polygone polygone;

	/**
	 * Attribute triangleSegments : Representing the list of current Segments of the Triangles
	 */
	private ArrayList<Segment> triangleSegments=new ArrayList<Segment>();

	/**
	 * Attribute triangles : Representing the list of current Triangles created by the Triangulator
	 */
	private ArrayList<Triangle> triangles=new ArrayList<Triangle>();

	/**
	 * Attribute polygoneOriginal : Representing the initial polygone to triangule
	 */
	private Polygone polygoneOriginal;

	/**
	 * These point are variable local to operate on them, create Triangle, Segment..
	 */
	private Point p0;
	private Point p1;
	private Point p2;

	/**
	 * Constructor Triangulator
	 * @param polygone : polygone to process
	 */
	public Triangulator (Polygone polygone) {
		this.polygone=polygone;
		this.polygoneOriginal=this.polygone;
	}

	/**
	 * This method allows to know if there the Triangle in parameter already exists in the list of triangle
	 * @param t : Triangle to test
	 * @return boolean : true if the triangle already exists
	 */
	public boolean isEqualTriangle (Triangle t) {
		boolean res=false;
		for (Triangle tri : triangles) {
			if (tri.triangleEquals(t)) {
				res=true;
			}	
		}
		return res;
	}
	/**
	 * This method allows to print all the current Triangle
	 */
	public void getCurrentTriangles() {
		int count=1;
		for (Triangle t : triangles) {
			Messenger.printMsg(Messenger.DEBUG, "Triangle "+count+" : "+t);
			count++;
		}
	}

	/**
	 * This method allows to get the List of current Triangles
	 * @return ArrayList<Triangle> : List of Triangles
	 */
	public ArrayList<Triangle> getTriangles() {
		return triangles;
	}

	/**
	 * This method allows to get the initial polygone
	 * @return Polygone
	 */
	public Polygone getPolygoneOriginal() {
		return polygoneOriginal;
	}

	/**
	 * This method allows to know if the triangle contains one of the point of the polygon
	 * @param t : Triangle to check
	 * @return boolean : true if the triangle contains a point
	 */
	private boolean triangleContainsPoints(Triangle t) {
		int i;
		int PL = polygone.points.size();
		for ( i = 0; i < PL; i++ ) {
			Point p = polygone.points.get(i);
			if ((!p.equals(p0)) && (!p.equals(p1)) && (!p.equals(p2)) && (p!= null)) {
				if (t.contains(p)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * This method execute the main part to get the list of Triangle
	 * @return ArrayList<Triangle> : List of Triangle
	 * @throws Exception
	 */
	public ArrayList<Triangle> process() throws Exception{
		int currentEar = 0;
		int compteur=polygone.points.size()+1;
		int oldPointActif=polygone.points.size();
		int currentPointActif=polygone.points.size();
		while (!polygone.isOver()) {
			currentPointActif=polygone.points.size();
			compteur--;
			if (compteur==0) {
				if (oldPointActif!=currentPointActif) {
					compteur=polygone.points.size()+1;
					oldPointActif=currentPointActif;
				}
				else {
					this.getCurrentTriangles();
					Messenger.printMsg(Messenger.ERROR, "Triangulation infini");
					throw new Exception ("Triangulation infini");
				}
			}

			p1 = polygone.getNext(currentEar);
			p0 = polygone.getPrev(p1);
			p2 = polygone.getNext(p1);
			Triangle t = new Triangle (p0,p1,p2);
			boolean containsPoint = this.triangleContainsPoints(t);
			boolean concave = polygone.pointConcave(p0, p1, p2);
			boolean intersectTriangle = t.intersect(triangleSegments);
			boolean intersectPerimetre = t.intersect(polygone.segments);
			boolean intersectPoint = t.intersectPointPerimetre(polygone.points);
			/*System.out.println(t);
			System.out.println("conc : "+concave);
			System.out.println("intertri : "+intersectTriangle);
			System.out.println("interp : "+intersectPoint);
			System.out.println("interperi : "+intersectPerimetre);
			System.out.println("cont : "+containsPoint );
			System.out.println("");*/
			if (!concave && !intersectTriangle && !intersectPerimetre && !intersectPoint && !containsPoint) {
				if (polygone.is3SegmentPerimetre(t)) {
					p0.setOut();
					p2.setOut();
				}
				//Adding the triangle to the list of triangles
				triangles.add(t);
				p1.setOut();
				//Creating a polygon with the new points
				this.polygone=new Polygone(this.polygone.newPolygone());

				//Adding the Segment the list of segments of the triangle
				triangleSegments.add(new Segment(p0,p1));
				triangleSegments.add(new Segment(p1,p2));
				triangleSegments.add(new Segment(p2,p0));	
			}
			currentEar++;
			if (currentEar>=polygone.points.size()-1) {
				currentEar=0;
			}
		}
		if (polygone.isTriangle()) {
			Triangle t = polygone.getTriangle();
			triangles.add(t);
		}
		return triangles;
	}

}