/**
 * 
 */
package saadadb.vocabulary.enums;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import saadadb.vocabulary.enums.DispersionType;

/**
 * @author laurentmichel
 *
 */
public class DispersionUnitRef {
	String unit;
	DispersionType type;	
	public static final Map<String, DispersionUnitRef> allowed_units ;
	
	static {
		allowed_units =  new LinkedHashMap<String, DispersionUnitRef>();
		allowed_units.put("angstrom", new DispersionUnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("angstroms", new DispersionUnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("a"       , new DispersionUnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("nm"      , new DispersionUnitRef("nm"       , DispersionType.WAVELENGTH));
		allowed_units.put("um"      , new DispersionUnitRef("um"       , DispersionType.WAVELENGTH)); 
		allowed_units.put("m"       , new DispersionUnitRef("m"        , DispersionType.WAVELENGTH));
		allowed_units.put("mm"      , new DispersionUnitRef("mm"       , DispersionType.WAVELENGTH));
		allowed_units.put("cm"      , new DispersionUnitRef("cm"       , DispersionType.WAVELENGTH));
		allowed_units.put("km"      , new DispersionUnitRef("km"       , DispersionType.WAVELENGTH));

		allowed_units.put("hz" , new DispersionUnitRef("Hz" , DispersionType.FREQUENCY));
		allowed_units.put("khz", new DispersionUnitRef("KHz", DispersionType.FREQUENCY));
		allowed_units.put("mhz", new DispersionUnitRef("MHz", DispersionType.FREQUENCY));
		allowed_units.put("ghz", new DispersionUnitRef("GHz", DispersionType.FREQUENCY));

		allowed_units.put("ev" , new DispersionUnitRef("eV"  ,  DispersionType.ENERGY));
		allowed_units.put("kev", new DispersionUnitRef("keV" ,  DispersionType.ENERGY));
		allowed_units.put("mev", new DispersionUnitRef("MeV" ,  DispersionType.ENERGY));
		allowed_units.put("gev", new DispersionUnitRef("GeV" ,  DispersionType.ENERGY));
		allowed_units.put("tev", new DispersionUnitRef("TeV" ,  DispersionType.ENERGY));

		allowed_units.put("", new DispersionUnitRef("channel",  DispersionType.CHANNEL));
	}

	/**
	 * @param unit
	 * @param type
	 */
	public DispersionUnitRef(String unit, DispersionType type) {
		this.unit = unit;
		this.type = type;
	}
	
	/**
	 * Returns the type {@link DispersionType} attached to unit
	 * Returns CHANNEL by default
	 * @param unit
	 * @return
	 */
	public static DispersionType getDispersionType(String unit){
		String sunit = (unit.startsWith("ANGSTROM") )? "Angstrom": unit;
		for( DispersionUnitRef ur: allowed_units.values()){
			if( ur.unit.equals(sunit)){
				return ur.type;
			}
		}
		return DispersionType.CHANNEL;		
	}
	
	/**
	 * Implement a map access case unsensitive
	 * @param unit
	 * @return
	 */
	public static DispersionUnitRef getUniRef(String unit){
		if( unit == null){
			return null;
		} else {
			return allowed_units.get(unit.toLowerCase());
		}
	}
	
	/**
	 * Return a table of available units for the given type
	 * @param type
	 * @return
	 */
	public static String[] getUnitsForType(DispersionType type) {
		Vector<String> v = new Vector<String>();
		for(DispersionUnitRef ur: allowed_units.values()){
			if( ur.type == type ){
				v.add(ur.unit);
			}
		}
		return (String[])(v.toArray(new String[0]));
	}


}
