package saadadb.products.inference;


import saadadb.database.Database;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import cds.astro.Astroframe;
import cds.astro.Ecliptic;
import cds.astro.Equatorial;
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
public final class CooSysResolver {
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

	/**
	 * Compare astroframe. The scope of the comparison is name/equinox and epoch
	 * Equinox and name are taken in consideration only  if both are not NaN
	 * Bonjour Laurent,

ICRS n'a pas d'équinoxe, mais pour FK4/FK5, ces base_epochs sont
B1950. et J2000. La transformation FK4/FK5 définie par l'UAI
convertit
(positions et mouvements propres dans FK4 à l'époque et equinoxe B1950)
  <==>
(positions et mouvements propres dans FK5 à l'époque et equinoxe J2000)

C'est cette valeur (B1950 ou J2000) qui est stockée dans base_epoch.
Elle ne doit pas être modifiée.

Parmi les classes dérivées, seules Ecliptic et Equatorial peuvent
posséder une équinoxe (qui n'a pas besoin d'être identique à B1950
ou J2000)

En pratique, seulement dans le cas où base_epoch vaut B1950 (exprimé en
année julienne) tu peux en déduire que le référentiel est de type FK4.
Mais le plus sûr est le typeof, il me semble...

J'espère que c'est assez clair -- ça le sera peut-être moins après le pot

François 
	 * @param af1
	 * @param af2
	 * @return
	 */
	public static boolean compareFrames(Astroframe af1, Astroframe af2) {
		if( !af1.name.equals(af2.name) ){
			return false;
		} else {
			double ep1 =  af1.getEpoch();
			double ep2 =  af2.getEpoch();
			if( (Double.isNaN(ep1) || Double.isNaN(ep2)) || (ep1 == ep2) ){
				return true;
			}
		}
		if( (af1 instanceof Ecliptic && af2 instanceof Ecliptic))	{
			Ecliptic e1 = (Ecliptic) af1;
			Ecliptic e2 = (Ecliptic) af2;
			double eq1 =  e1.getEquinox();
			double eq2 =  e2.getEquinox();
			if( (Double.isNaN(eq1) || Double.isNaN(eq2)) || ( eq1 == eq2) ){
				double ep1 =  e1.getEpoch();
				double ep2 =  e2.getEpoch();
				if( (Double.isNaN(ep1) || Double.isNaN(ep2)) || (ep1 == ep2) ){
					return true;
				}
			}
			return false;
		}
		if( (af1 instanceof Equatorial && af2 instanceof Equatorial))	{
			Equatorial e1 = (Equatorial) af1;
			Equatorial e2 = (Equatorial) af2;
			double eq1 =  e1.getEquinox();
			double eq2 =  e2.getEquinox();
			if( (Double.isNaN(eq1) || Double.isNaN(eq2)) || ( eq1 == eq2) ){
				double ep1 =  e1.getEpoch();
				double ep2 =  e2.getEpoch();
				if( (Double.isNaN(ep1) || Double.isNaN(ep2)) || (ep1 == ep2) ){
					return true;
				}
			}
			return false;
		}
	return false;
	}

	/**
	 * Compare the astroframe with the database astroframe see {@link CooSysResolver#compareFrames(Astroframe, Astroframe)}
	 * @param af astroframe to be compared with this of the DB
	 * @return
	 */
	public static boolean isSameAsDatabaseFrame(Astroframe af){
		boolean retour = compareFrames(af, Database.getAstroframe());
		if (Messenger.debug_mode)
			Messenger.printMsg(Messenger.DEBUG, "Is astroframe " + af + " the same as " + Database.getAstroframe() + "? " + retour);
		return retour;
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		Astroframe af1 =  new CooSysResolver("ICRS,2000.0,2020.0").getCooSys();
		System.out.println(af1);
		Astroframe af2 =  new CooSysResolver("FK5,2000.0,2020.0").getCooSys();
		System.out.println(af2);
		Astroframe af3 =  new CooSysResolver("ICRS").getCooSys();
		System.out.println(af3);
		Astroframe af =  new CooSysResolver("ICRS,2000.0,2020.0").getCooSys();
		System.out.println(af);
		System.out.println(CooSysResolver.compareFrames(af1, af));
		af =  new CooSysResolver("ICRS,2000.0").getCooSys();
		System.out.println(af);
		System.out.println(CooSysResolver.compareFrames(af1, af));	
		af =  new CooSysResolver("ICRS,2000.0,2020.4").getCooSys();
		System.out.println(af);
		System.out.println(CooSysResolver.compareFrames(af1, af));	
		
	}
}
