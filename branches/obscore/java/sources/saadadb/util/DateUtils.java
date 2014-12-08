package saadadb.util;

import java.util.List;

import saadadb.vocabulary.RegExp;
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
	    String retour=input;
	    boolean needQuotes = false;
	    if(input.contains("'"))
	    {
	    	input=input.replace("'", "");
	    	needQuotes=true;
	    }
		if( input.matches(RegExp.FITS_FLOAT_NDN) ) {
			af.set(Double.parseDouble(input));
			retour= Double.toString(af.getMJD());
		} else if( input.matches(RegExp.FITS_INT_VAL) ) {
			af.set(Integer.parseInt(input));
			retour= Double.toString(af.getMJD());
		} else {
			af.set(input);
			retour= Double.toString(af.getMJD());
		}
		if(needQuotes)
			return "'"+retour+"'";
		else
			return retour;
	}
	/**
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static double getFMJD(String input) throws Exception {
		try {
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
		} catch(Exception e){
			return SaadaConstant.LONG;
		}
	}
	
	/**
	 * The duration is supposed to be a tile in second. It it can be interpreted as a numeric value it is returned without formating
	 * If not and its format matches {@link RegExp#HMS_DURATION}, the duration is computed and returned as a String
	 * @param duration input parameter
	 * @return
	 */
	public static String getDuration(String duration){
		try {
			double v = Double.parseDouble(duration);
			return duration;
		} catch(Exception e){
			RegExpMatcher rem = new RegExpMatcher(RegExp.HMS_DURATION, 4);
			List<String> fields = rem.getMatches(duration);
			if( fields != null ) {
				double valeur = Double.parseDouble(fields.get(1))*3600 + Double.parseDouble(fields.get(2))*60 + Double.parseDouble(fields.get(3)); 
				return String.valueOf(valeur);
			} else {
				return null;
			}
		}
		
	}
}
