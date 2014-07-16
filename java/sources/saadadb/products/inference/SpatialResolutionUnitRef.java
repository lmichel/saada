/**
 * 
 */
package saadadb.products.inference;

import java.util.HashSet;
import java.util.Set;

import saadadb.enums.DispersionType;

/**
 * Simple set of valid units for the spatial resolution
 * @author laurentmichel
 *
 */
public class SpatialResolutionUnitRef {
	String unit;
	DispersionType type;	
	public static final Set<String> allowed_units ;
	
	static {
		allowed_units =  new HashSet<String>();
		allowed_units.add("deg");
		allowed_units.add("arcmin");
		allowed_units.add("arcsec");
		allowed_units.add("mas");
		allowed_units.add("uas"); 
	}
	
	/**
	 * @param unit
	 * @return
	 */
	public static final boolean isUnitValid(String unit){
		return allowed_units.contains(unit);
	}

}
