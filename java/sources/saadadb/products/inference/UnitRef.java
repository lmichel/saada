/**
 * 
 */
package saadadb.products.inference;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import saadadb.enums.DispersionType;

/**
 * @author laurentmichel
 *
 */
public class UnitRef {
	String unit;
	DispersionType type;	
	public static final Map<String, UnitRef> allowed_units ;
	
	static {
		allowed_units =  new LinkedHashMap<String, UnitRef>();
		allowed_units.put("angstrom", new UnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("angstroms", new UnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("a"       , new UnitRef("Angstrom" , DispersionType.WAVELENGTH));
		allowed_units.put("nm"      , new UnitRef("nm"       , DispersionType.WAVELENGTH));
		allowed_units.put("um"      , new UnitRef("um"       , DispersionType.WAVELENGTH)); 
		allowed_units.put("m"       , new UnitRef("m"        , DispersionType.WAVELENGTH));
		allowed_units.put("mm"      , new UnitRef("mm"       , DispersionType.WAVELENGTH));
		allowed_units.put("cm"      , new UnitRef("cm"       , DispersionType.WAVELENGTH));
		allowed_units.put("km"      , new UnitRef("km"       , DispersionType.WAVELENGTH));

		allowed_units.put("hz" , new UnitRef("Hz" , DispersionType.FREQUENCY));
		allowed_units.put("khz", new UnitRef("KHz", DispersionType.FREQUENCY));
		allowed_units.put("mhz", new UnitRef("MHz", DispersionType.FREQUENCY));
		allowed_units.put("ghz", new UnitRef("GHz", DispersionType.FREQUENCY));

		allowed_units.put("ev" , new UnitRef("eV"  ,  DispersionType.ENERGY));
		allowed_units.put("kev", new UnitRef("keV" ,  DispersionType.ENERGY));
		allowed_units.put("mev", new UnitRef("MeV" ,  DispersionType.ENERGY));
		allowed_units.put("gev", new UnitRef("GeV" ,  DispersionType.ENERGY));
		allowed_units.put("tev", new UnitRef("TeV" ,  DispersionType.ENERGY));

		allowed_units.put("", new UnitRef("channel",  DispersionType.CHANNEL));
	}

	/**
	 * @param unit
	 * @param type
	 */
	public UnitRef(String unit, DispersionType type) {
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
		for( UnitRef ur: allowed_units.values()){
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
	public static UnitRef getUniRef(String unit){
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
		for(UnitRef ur: allowed_units.values()){
			if( ur.type == type ){
				v.add(ur.unit);
			}
		}
		return (String[])(v.toArray(new String[0]));
	}


}
