package saadadb.util;

import cds.astro.Astrotime;

/**
 * @author michel
 * @version $Id$
 */
public class DateUtils {

	/**
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String getMJD(String input) throws Exception {
	    Astrotime af = new Astrotime();
		if( input.matches(RegExp.FITS_FLOAT_NDN) ) {
			af.set(Double.parseDouble(input));
			return Double.toString(af.getMJD());
		} else if( input.matches(RegExp.FITS_INT_VAL) ) {
			af.set(Integer.parseInt(input));
			return Double.toString(af.getMJD());
		} else {
			af.set(input);
			return Double.toString(af.getMJD());
		}
	}
	/**
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static double getFMJD(String input) throws Exception {
	    Astrotime af = new Astrotime();
		if( input.matches(RegExp.FITS_FLOAT_VAL) ) {
			af.set(Double.parseDouble(input));
			return af.getMJD();
		} else if( input.matches(RegExp.FITS_INT_VAL) ) {
			af.set(Integer.parseInt(input));
			return af.getMJD();
		} else {
			af.set(input);
			return af.getMJD();
		}
	}
}
