package saadadb.util;

import saadadb.collection.Category;
import saadadb.database.Database;

/**
 * This class is a simple dictionnary of regular expressions used in Saada
 * @author michel
 *
 */
public class RegExp {
	/*
	 * Regular expresssion used to filter Saada entities
	 */
	public static final String CLASSNAME    = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String COLLNAME     = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String FILENAME     = "\\w+(?:\\.\\w*)?";
	public static final String FILEPATH     = "(?:[A-Za-z]:)?" + Database.getRegexpSepar() + "?(?:" + FILENAME + Database.getRegexpSepar() + ")*" + "\\w+(?:\\.\\w*)?";
	public static final String DBNAME       = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String EXTATTRIBUTE = "[a-zA-Z][_a-zA-Z0-9]*";

	//public static final String PSEUDO_TABLE = "(ANY)|(" + COLLNAME + ")|\\[(" + COLLNAME + ")(,(" + COLLNAME + "))*\\]|\\[(" + COLLNAME + ")\\((" + CLASSNAME + ")(,(" + CLASSNAME + "))*\\)\\]";
	public static final String PSEUDO_TABLE = "(ANY)|(" + COLLNAME + ")";

	public static final String FORBIDDEN_CLASSNAME = "(?i)((table)|(create)|(alter)|(index))";
	/*
	 * Regular expression used to filter command line parameters
	 */
	public static final String MAPPING  = "(?i)((only)|(first)|(last))";
	public static final String CATEGORY = "(?i)" + Category.buildRegExp();
	public static final String ALLOWED_ERROR_UNITS = "(deg)|(arcmin)|(arcsec)|(mas)|(uas)";
	public static final String REPOSITORY  = "(?i)((no)|(move))";
	/*
	 * Regular expression used to filter input datafiles
	 */
	public static final String FITS_FILE    = "(?i)(.*(\\.(((fit|fits)(\\.gz)?)|(ftz|fgz))))$";
	public static final String VOTABLE_FILE = "(?i)(.*(\\.(vot|votable|xml)(\\.gz)?))$";
	public static final String IMAGE_FILE   = "(?i)(.*(\\.(gif|jpeg|jpg|png|tiff|bmp)))$";
	/*
	 * Regular expression used to filter values read in  FITS files.
	 * Can also be used in other contexts than FITS files
	 */
	public static final String FITS_KEYWORD     = "([_\\-a-zA-Z0-9\\s]+)=\\s*";
	public static final String FITS_INT_VAL     = "[+\\-]?[0-9]+";
	public static final String FITS_STR_VAL     = "'[^']*'";
	public static final String FITS_BOOLEAN_VAL = "[TF]{1}";
	public static final String FITS_COMMENT     = "/.*";
	/*
	 * EXP=power of 10, D=dot, N=numeric
	 */
	public static final String FITS_FLOAT_NDNEXP = "(?:[0-9]+\\.[0-9]+[Ee][+-]?[0-9]*)";
	public static final String FITS_FLOAT_DNEXP  = "(?:\\.[0-9]+[Ee][+-]?[0-9]*)";
	public static final String FITS_FLOAT_NDEXP  = "(?:[0-9]+\\.[Ee][+-]?[0-9]*)";
	public static final String FITS_FLOAT_NEXP   = "(?:[0-9]+[Ee][+-]?[0-9]+)";
	public static final String FITS_FLOAT_NDN    = "(?:[0-9]+\\.[0-9]+)";
	public static final String FITS_FLOAT_DN     = "(?:\\.[0-9]+)";
	public static final String FITS_FLOAT_ND     = "(?:[0-9]+\\.)";

    /*
     * Float regex must be classed from the most complex to the simplest in order
     * not to loose part of the number with capturing groups
     */
	public static final String FITS_FLOAT_VAL = "[+\\-]?(?:" + FITS_FLOAT_NDNEXP + "|" 
                                                             + FITS_FLOAT_DNEXP  + "|" 
                                                             + FITS_FLOAT_NDEXP  + "|" 
                                                             + FITS_FLOAT_NEXP   + "|" 
                                                             + FITS_FLOAT_NDN    + "|" 
                                                             + FITS_FLOAT_DN     + "|" 
                                                             + FITS_FLOAT_ND     + ")";
	public static final String NUMERIC = "[+\\-]?(?:" + FITS_FLOAT_NDNEXP + "|" 
    + FITS_FLOAT_DNEXP  + "|" 
    + FITS_FLOAT_NDEXP  + "|" 
    + FITS_FLOAT_NEXP   + "|" 
    + FITS_FLOAT_NDN    + "|" 
    + FITS_FLOAT_DN     + "|" 
    + FITS_FLOAT_ND     + "|"
    + "[0-9]+)";
	/*
	 * Query parsing et autres cochonneries
	 */
	public static final String ONE_COORDINATE = "[+-]?(?:(?:\\.[0-9]+)|(?:[0-9]+\\.?[0-9]*))(?:[eE][+-]?[0-9]+)?";
	public static final String POSITION_COORDINATE = "^(" + ONE_COORDINATE + ")((?:[+-]|(?:[,:;\\s]+[+-]?))" +  ONE_COORDINATE + ")$";

	/*
	 * Special keyword detection
	 */
	public static final String RA_KW  = "(_*ra)|(_*ra.?(2000)?)|(_*ra.?[^(dec)]?)|(_*ra.?obj)";
	public static final String DEC_KW = "(_*de)|(_*dec)|(_*dec.?(2000)?)|(_*de.?(2000)?)|(_*dec.?[^(ra)]?)|(_*dec.?obj)|(_*de.?obj)";
	/*
	 * Spectral axis column names			System.out.println("coucou");

	 */
	public static final String SPEC_AXIS = "(?i)((channel)|(wavelength)|(freq)|(frequency)|(spectral_value))";
	/*
	 * URL
	 */
	public static final String URL = "(http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?";
	public static final String BIBCODE = "\\d{4}[\\w\\.]{10}\\d{4}\\w";		
	/*
	 * VO resources
	 */
	public static final String UTYPE = "(?:\\w[\\w;:]*\\.)*(?:\\w[\\w;:]*)";
	public static final String UCD = "(?:(?:(?:\\w+\\.)*\\w+);)*(?:(?:\\w+\\.)*\\w+)";
	/*
	 * Used to detect not set field. Only the most current notset values are set here
	 */
	public static final String NOT_SET_VALUE = "(NULL)|(NaN)|(Infinity)|(" + Integer.MAX_VALUE + ")";
	
	public static void main(String[] args) {
		String ra = "__raj2000";
		System.out.println(ra.matches(RegExp.RA_KW));
		String dec = "__decj2000";
		System.out.println(dec.matches(RegExp.DEC_KW));
		String fits = "SW_HD117555_057_A.1.ms.fits";
		System.out.println(fits.matches(RegExp.FITS_FILE));
		System.out.println("SpectralAxis.cov.bounds.extent#stc:double1Type".matches(RegExp.UTYPE));
		System.out.println("SpectralAxis.cov.bounds.extent;stc:double1Type".matches(RegExp.UTYPE));
		System.out.println("SpectralAxis.cov.bounds.extentstcdouble1Type".matches(RegExp.UTYPE));
		System.out.println("0.2".matches(RegExp.NUMERIC));
	}
}