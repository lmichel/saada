package saadadb.query.region.triangule.convertCoord;





import java.util.ArrayList;

import saadadb.query.region.triangule.Point;
import saadadb.util.Messenger;
import cds.astro.Astrocoo;
import cds.astro.Astroframe;
import cds.astro.Galactic;
import cds.astro.ICRS;

/**
 * Class ConvertGalac
 * Allows to convert from equatorial coordinate to galactic coordinate or conversely
 * @author jremy
 * @version $Id$
 *
 */
public class ConvertGalac {

	/**
	 * Attribute list : Representing the list of points of the initial polygon
	 */
	public ArrayList<Point> list;

	/**
	 * Constructor ConvertGalac
	 * @param list ArrayList<Point> : Representing the list of the points of the polygon
	 */
	public ConvertGalac (ArrayList<Point> list) {
		this.list= new ArrayList<Point>();

		int INDEX=0;
		for (Point ps : list) {
			this.list.add(new Point(INDEX,ps.getRa(),ps.getDec()));
			INDEX++;
		}
	}

	/**
	 * This method allows to set the coordinate system from one to another
	 * @param af : Current Astroframe
	 * Method to call only where the Zone is at the pole
	 */
	public void setModeCoordonnees (Astroframe af) throws Exception{
		if( "ICRS".equals(af.name)) {
			Messenger.printMsg(Messenger.DEBUG, "Converting from ICRS to Galactic");
			Astroframe afGalac = new Galactic();
			for (Point p : this.list) {
				Astrocoo aicrs = new Astrocoo(af,p.getRa(),p.getDec());
				aicrs.convertTo(afGalac);
				p.setRa(aicrs.getLon());
				p.setDec(aicrs.getLat());
			}
		}		
		else if( "Galactic".equals(af.name)) { 
			Messenger.printMsg(Messenger.DEBUG, "Converting from Galactic to ICRS");
			Astroframe afIcrs = new ICRS();
			for (Point p : this.list) {
				Astrocoo agalac = new Astrocoo(af,p.getRa(),p.getDec());
				agalac.convertTo(afIcrs);
				p.setRa(agalac.getLon());
				p.setDec(agalac.getLat());
			}
		}
		else { 
			Messenger.printMsg(Messenger.ERROR, "Coordinate system ( Astroframe ) unhandled"); 
			throw new Exception("Wrong Astroframe");
		}
	}
}