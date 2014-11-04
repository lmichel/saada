package saadadb.products.inference;

import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.FK4;
import cds.astro.FK5;
import cds.astro.Galactic;
import cds.astro.ICRS;

/**
 * Simple utility transforming a CooSys descriptor as it is returned by the {@link QuantityDetector} in an
 * {@link Astroframe} instances.
 * The descriptor is a merged list of params
 * param#1 : frame: string
 * param#2 (opt) : equinox: double
 * param#3 (opt, requires param#2 to be set) : epoch: double
 * ex: FK5,2000,2007
 * 
 * @author michel
 * @version $Id$
 */
public class CooSysResolver {
	private String[] fields;

	/**
	 * @param expression coosys descriptor e.g. FK5,2000,2007
	 * @throws Exception
	 */
	public CooSysResolver(String expression) throws Exception{
		fields = expression.split("[,; ']");
	}

	/**
	 * @return the astroframe matching the expression
	 * @throws Exception if something goes wrong (non numeric parameters e.g.)
	 */
	public Astroframe getCooSys() throws Exception{
		Astroframe retour;
		double equinox = (fields.length == 1)? SaadaConstant.DOUBLE: Double.parseDouble(fields[1]);
		double epoch = (fields.length < 3)? SaadaConstant.DOUBLE: Double.parseDouble(fields[2]);
		switch(this.fields[0]){
		case "ICRS": retour = (equinox == SaadaConstant.DOUBLE)?  new ICRS(): new ICRS(equinox);
		break;
		case "FK4": retour  = (equinox == SaadaConstant.DOUBLE)?  new FK4(): new FK4(equinox);
		break;
		case "FK5": retour  = (equinox == SaadaConstant.DOUBLE)?  new FK5(): new FK5(equinox);
		break;
		case "Galactic": retour = new Galactic();break;
		case "Ecliptic": retour = new Ecliptic(); break;
		default: return null;
		}
		if( epoch != SaadaConstant.DOUBLE ){
			retour.setFrameEpoch(epoch);
		}
		return retour;
	}
}
