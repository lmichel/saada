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
			af.set(formatStringDate(formatStringDate(input)));
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
			af.set(formatStringDate(input));
			return af.getMJD();
		}
		} catch(Exception e){
			return SaadaConstant.LONG;
		}
	}
	
	/**
	 * Check whether the stringDate is like DD/MM/YY. in this case, it is reformatted 
	 * as DD/MM/20YY
	 * @param stringDate
	 * @return
	 */
	public static String formatStringDate(String stringDate){
		String[] xp = stringDate.split("/");
		if( xp.length == 3 ){
			String le = xp[xp.length - 1];
			if( le.length() == 2){
				le = "20" + le;
				xp[xp.length - 1]  = le;
				return Merger.getMergedArray("/", xp);
			} 
		}
		return stringDate;

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

	public static void main(String[] args) throws Exception{
		String x = "01/01/12";
		System.out.println(DateUtils.getMJD(x));
	}
}
