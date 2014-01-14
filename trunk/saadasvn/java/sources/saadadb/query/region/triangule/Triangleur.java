package saadadb.query.region.triangule;

import java.util.ArrayList;

import saadadb.query.region.triangule.convertCoord.radecToXY;
//import cds.aladin.Aladin;

/**
 * Class calling the converter to triangule the polygon
 * This class can also call Aladin to show the polygon or the triangles
 * @author jremy
 * @version $Id$
 *
 */
public class Triangleur {

	/**
	 * Atribute points representing the List of points of the polygon
	 */
	private ArrayList<Point> points;

	/**
	 * Attribute triangle representing the list of triangles returned by the Triangulator
	 */
	private ArrayList<Triangle> triangles;

	/**
	 * Constructor Triangleur from a list of points
	 * @param points : ArrayList<Point>
	 */
	public Triangleur (ArrayList<Point> points) {
		this.points=points;
		triangles = new ArrayList<Triangle>();
	}

	/**
	 * Constructor Triangleur from a polygone
	 * @param p : Polygon
	 */
	public Triangleur (Polygone p) throws Exception{
		this.points=p.points;
		triangles = new ArrayList<Triangle>();
		radecToXY convert = new radecToXY(points);
		triangles=convert.process();
	}

	/**
	 * This method allows to get the list of Triangles relevant for Aladin
	 * @return ArrayList<String>
	 */
	public ArrayList<String> getTriangleAladin() {
		ArrayList<String> trianglesAladin = new ArrayList<String>();
		for (Triangle t : this.triangles) {
			trianglesAladin.add(t.getAladin());
		}
		return trianglesAladin;
	}

	/**
	 * This method allows to get the String associate with the polygon relevant for Aladin
	 * @return String
	 */
	public String getPolygonAladin () {
		String ret = "draw blue polygon ";
		for (Point p : this.points) {
			ret+=" "+p.getRa()+","+p.getDec();
		}
		return ret;
	}

//	/**
//	 * This method allows to launch Aladin, set the sky and draw the polygon and the triangles
//	 * @param onlyPolygon : boolean at true if you only need the draw of the polygon
//	 * @throws Exception
//	 */
//	public void startAladin(boolean onlyPolygon) throws Exception{
//		Aladin aladin = Aladin.launch("-trace");
//		aladin.execCommand("get allsky(\"DSS colored\")");
//		aladin.execCommand(this.getPolygonAladin());
//		if (!onlyPolygon) {
//			for (String s : this.getTriangleAladin()) {
//				aladin.execCommand(s);
//			}
//		}
//	}
	
	/**
	 * This method allows to impress the gnuplot commands for the polygon and the triangles
	 * @param onlyPolygon : boolean at true if you only need the draw of the polygon
	 * @throws Exception
	 */
	public void getGnuplot(boolean onlyPolygon) throws Exception {
		Polygone p = new Polygone (points);
		System.out.println(p.getPolyGnuplot());
		if (!onlyPolygon) {
			int iterator=2;
			for (Triangle t : this.triangles) {
				System.out.println("set object "+iterator+t.getTriangleGnuplot());
				iterator++;
			}
		}
	}

	/**
	 * This method allows to convert from radec to xy and then return the triangles
	 * @return ArrayList<Triangle>
	 * @throws Exception
	 */
	public ArrayList<Triangle> execute() throws Exception{
		radecToXY convert = new radecToXY(this.points);		
		return convert.process();		
	}
	
}
