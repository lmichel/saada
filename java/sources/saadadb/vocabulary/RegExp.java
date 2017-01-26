package saadadb.vocabulary;

import java.io.File;

import saadadb.collection.Category;

/**
 * This class is a simple dictionnary of regular expressions used in Saada
 * @author michel
 * * @version $Id$

 */
public class RegExp {
	/*
	 * Regular expresssion used to filter Saada entities
	 */
	public static final String FILE_SEPARATOR = (File.separator.equals("\\"))?"\\\\":File.separator;
	public static final String CLASSNAME    = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String COLLNAME     = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String FILENAME     = "\\w+(?:\\.\\w*)?";
	public static final String FILEPATH     = "(?:[A-Za-z]:)?" + FILE_SEPARATOR + "?(?:" + FILENAME + FILE_SEPARATOR + ")*" + "\\w+(?:\\.\\w*)?";
	public static final String DBNAME       = "[_a-zA-Z][_a-zA-Z0-9]*";
	public static final String EXTATTRIBUTE = "[a-zA-Z][_a-zA-Z0-9]*";

	//public static final String PSEUDO_TABLE = "(ANY)|(" + COLLNAME + ")|\\[(" + COLLNAME + ")(,(" + COLLNAME + "))*\\]|\\[(" + COLLNAME + ")\\((" + CLASSNAME + ")(,(" + CLASSNAME + "))*\\)\\]";
	public static final String PSEUDO_TABLE = "(ANY)|(" + COLLNAME + ")";

	public static final String FORBIDDEN_CLASSNAME = "(?i)((table)|(create)|(alter)|(index))";
	/*
	 * Regular expression used to filter command line parameters
	 */
	public static final String MAPPING             = "(?i)((only)|(first)|(last))";
	public static final String CATEGORY            = "(?i)" + Category.buildRegExp();
	public static final String ALLOWED_ERROR_UNITS = "(deg)|(arcmin)|(arcsec)|(mas)|(uas)";
	public static final String REPOSITORY          = "(?i)((no)|(move))";
	public static final String FOLLOWLINKS         = "(?i)((follow)|(ignore))";
	public static final String QUOTED_EXPRESSION   = "^'(.*)'$";
	public static final String NUMERIC_PARAM       = "^(?:(" + RegExp.NUMERIC + "))$";
	public static final String NUMERIC_UNIT_PARAM  = "^(?:(" + RegExp.NUMERIC + ")(.*))$";
	
	/*
	 * Regular expression used to filter input datafiles
	 */
	public static final String FITS_FILE    = "(?i)(.*(\\.((((fit)|(fits))(\\.gz)?)|((ftz)|(fgz)))))$";
	public static final String VOTABLE_FILE = "(?i)(.*(\\.((vot)|(votable)|(xml))(\\.gz)?))$";
	public static final String JSON_FILE = "(?i)(.*\\.json)$";
	public static final String IMAGE_FILE   = "(?i)(.*(\\.(((gif)|(jpeg)|(jpg)|(png)|(tiff)|(bmp))))$";
	public static final String[] PICT_FORMAT  = new String[]{"jpg",  "gif", "jpeg", "png", "tiff", "bmp", "GIF", "JPEG", "JPG", "PNG", "TIFF", "BMP"};
	
	

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
    + "(?:[0-9]+))";
	
	/*
	 * Query parsing et autres cochonneries
	 */
	public static final String ONE_COORDINATE = "[+-]?(?:(?:\\.[0-9]+)|(?:[0-9]+\\.?[0-9]*))(?:[eE][+-]?[0-9]+)?";
	public static final String POSITION_COORDINATE = "^(" + ONE_COORDINATE + ")((?:[+-]|(?:[,:;\\s]+[+-]?))" +  ONE_COORDINATE + ")$";

	/*
	 * Spectral axis column names			
	 */
	public static final String SPEC_AXIS_KW      = "(?i)((channel)|(wavelength)|(freq)|(frequency)|(spectral_value))";
	public static final String SPEC_AXIS_DESC    = "(?i)(.*\\s((channel)|(wavelength)|(freq)|(frequency)|(spectral_value)))";
	public static final String SPEC_FLUX_KW      = "(?i)((flux)|(count[s]?))";
	public static final String SPEC_FLUX_DESC    = "(?i)(.*\\s((flux)|(count[s]?)))";
	public static final String SPEC_MIN_UCD      = "(?i)((em\\.wl;stat\\.min)|(sed:SpectralMinimumWavelength))";
	public static final String SPEC_MIN_KW       = "(?i)((lam\\.min)|(lambda1)|(wmin))";
	public static final String SPEC_MAX_UCD      = "(?i)((em\\.wl;stat\\.max)|(sed:SpectralMaximumWavelength))";
	public static final String SPEC_MAX_KW       = "(?i)((lam\\.max)|(lambda2)|(wmax))";
	public static final String SPEC_BAND_UCD     = "(?i)(em\\.wl)";
	public static final String SPEC_RESPOWER_UCD = "(?i)((instr\\.dispersion)|(spect\\.resolution))";
	public static final String SPEC_RESPOWER_KW  = "(?i)((power)|(res)|(spres)|(r))";
	public static final String UNIT_IN_KW_COMMENT  = "(?i)(?:.*(?:(?:wavelength)|(?:dispersion))\\s.*((?:A)|(?:Angstrom))(?:(?:\\s.*)|$))";
	
	/*
	 * TIme axis column names
	 */
	public static final String TIME_REF_KW       = "(?i)(mjdref)";
	public static final String TIME_START_KW     = "(?i)((tstart)|(obs.*start)|(obs.*date)|(date.*obs)|(start.*date)|(mjd(.*obs)?)|(time))";
	public static final String TIME_START_UCD    = "(?i)(time.start;obs)";
	public static final String TIME_END_KW       = "(?i)((tstop)|(obs.*end)|(end.*obs)|(end.*date)|(date.*end))";
	public static final String TIME_END_UCD      = "(?i)(time.end;obs)";
	public static final String TIME_RESOLUTION_KW  = "(?i)((TimeRes)|(deltat))";
	public static final String TIME_RESOLUTION_UCD = "(?i)(time.resolution)";
	public static final String EXPOSURE_TIME_KW  = "(?i)((exptime)|(texp)|(obs.*time)|(time.*obs))";
	public static final String EXPOSURE_TIME_UCD = "(?i)(time.duration;obs.exposure)";	
	public static final String NUMERIC_TIME_UNIT = "(?i)((?:seconds)|(?:hours)|(?:minutes))";
	public static final String HMS_DURATION      = "(?i)(([0-9]+)[h:\\.]([0-9]+)[m:\\.]([0-9]+)s?)";
	/*
	 * URL
	 */
	public static final String URL = "(http|https):\\/\\/(\\w+:{0,1}\\w*@)?(\\S+)(:[0-9]+)?(\\/|\\/([\\w#!:.?+=&%@!\\-\\/]))?";
	public static final String BIBCODE = "\\d{4}[\\w\\.]{10}\\d{4}\\w";		
	/*
	 * VO resources
	 */
	public static final String UTYPE = "(?:\\w[\\w;:-]*\\.)*(?:\\w[\\w;:-]*)";
	//public static final String UCD = "(?:(?:(?:\\w+\\.)*\\w+);)*(?:(?:\\w+\\.)*\\w+)";*
	public static final String UCD = "(?:" + UTYPE + ";)*" + UTYPE;
	/*
	 * Used to detect not set field. Only the most current not set values are set here
	 */
	public static final String NOT_SET_VALUE = "(NULL)|(NaN)|(Infinity)|(" + Integer.MAX_VALUE + ")";
	/**
	 * Observation keywords
	 */
	public static final String COLLNAME_KW      = "(?i)((collection)|(obs_id))";
	public static final String COLLNAME_UCD     = "(?i)(.*obs\\.param;obs.*)";
	public static final String TARGET_KW        = "(?i)((object)|(target(.*name)?))";
	public static final String TARGET_UCD       = "(?i)(meta\\.id)";
	public static final String INSTRUMENT_UCD   = "(?i)(meta\\.id;instr.*)";
	public static final String INSTRUMENT_KW    = "(?i)((instrume.*)|(.*instrument))";
	public static final String FACILITY_UCD     = "(?i)(.*instr\\.tel.*)";
	public static final String FACILITY_KW      = "(?i)((facility)|(telescop))";
	/**
	 * Polarization keywords
	 */
	public static final String POLARIZATION_UCD = "(?i)((phys\\.polarization)|(.*stokes.*))";
	public static final String POLARIZATION_KW  = "(?i)((pol)|(.*stokes.*))";
	/**
	 * Coordinate system
	 */
	public static final String ECL_SYSTEM = "(?i)(.*((ecl_FK5)|(ecliptic)))";
	public static final String FK4_SYSTEM = "(?i)(.*((eq_FK4)|(FK4)).*)";
	public static final String FK5_SYSTEM = "(?i)(.*((eq_FK5)|(FK5)).*)";
	public static final String GALACTIC_SYSTEM   = "(?i)(galactic)";		
	public static final String ICRS_SYSTEM       = "(?i)(icrs)";		
	/**
	 * Position error
	 */
	public static final String ERROR_KW    = "(?i)((radec.*err.*)|(hpbw)|(e_ra.*)|(e_de.*))";
	public static final String ERROR_UCD    = "(?i)((stat.error;pos.eq\\..*)|(pos\\.angResolution)|(pos\\.posAng;pos\\.errorEllipse))";

	public static final String ERROR_MIN_KW     = "(?i)((radecerr.*)|(errmin)|(minaxis)|(hpbw))";
	public static final String ERROR_MIN_UCD    = "(?i)((pos\\.posAng;pos\\.errorEllipse)|(stat\\.error;pos\\.eq.*)|(pos\\.angResolution))";
	public static final String ERROR_MAJ_KW     = "(?i)((radecerr.*)|(errmaj)|(majaxis)|(hpbw))";
	public static final String ERROR_MAJ_UCD    = "(?i)((pos\\.posAng;pos\\.errorEllipse)|(stat\\.error;pos\\.eq.*)|(pos\\.angResolution))";
	public static final String ERROR_ANGLE_KW   = "(?i)((theta)|(PosAng))";
	public static final String ERROR_ANGLE_UCD  = "(?i)((pos\\.posAng))";
	/**
	 * Fov
	 */
	public static final String FOV_KW  = "(?i)(fov.*)";
	public static final String FOV_UCD = "(?i)(instr\\.fov)";
	/**
	 * Region
	 */
	public static final String REGION_KW  = "(?i)(.*region.*)";
	public static final String REGION_UCD = "(?i)(obs\\.field)";

	/**
	 * Position kw
	 */
	public static final String RA_MAINUCD   = "(?i)((POS_EQ_RA_MAIN)|(pos\\.eq\\.ra;meta\\.main))";
	public static final String DEC_MAINUCD  = "(?i)((POS_EQ_DEC_MAIN)|(pos\\.eq\\.dec;meta\\.main))";
	public static final String RA_UCD       = "(?i)((POS_EQ_RA)|(pos\\.eq\\.ra))";
	public static final String DEC_UCD      = "(?i)((POS_EQ_DEC)|(pos\\.eq\\.dec))";

	public static final String FK5_RA_KW        = "(?i)((_*ra)|(_*ra[^b]?(2000)?)|(_*ra.?[^(dec)]?)|(_*ra.?obj))";
	public static final String FK5_DEC_KW       = "(?i)((_*dec)|(_*de[^A-Za-z])|(_*dec[^b]?(2000)?)|(_*de[^b]?(2000)?)|(_*dec.?[^(ra)]?)|(_*dec.?obj)|(_*de.?obj))";

	public static final String FK4_RA_KW        = "(?i)(_*ra[^(dec)]*b1950)";
	public static final String FK4_DEC_KW       = "(?i)(_*de[^(ra)]*b1950)";
	
	public static final String ICRS_RA_KW        = "(?i)((_*ra[^(dec)]*)|(.*right.*ascension))";
	public static final String ICRS_DEC_KW       = "(?i)((_*de[^(ra)]*)|(.*declination))";

	public static final String ECLIPTIC_RA_MAINUCD   = "(?i)((POS_EC_RA_MAIN)|(pos\\.ecliptic\\.lon;meta\\.main))";
	public static final String ECLIPTIC_DEC_MAINUCD  = "(?i)((POS_EC_DEC_MAIN)|(pos\\.ecliptic\\.lat;meta\\.main))";
	public static final String ECLIPTIC_RA_UCD       = "(?i)((POS_EC_RA)|(pos\\.ecliptic\\.lon))";
	public static final String ECLIPTIC_DEC_UCD      = "(?i)((POS_EC_DEC)|(pos\\.ecliptic\\.lat))";
	public static final String ECLIPTIC_RA_KW        = "(?i)(_elon\\.*)";
	public static final String ECLIPTIC_DEC_KW       = "(?i)(_elat\\.*)";

	public static final String GALACTIC_RA_MAINUCD   = "(?i)((POS_GAL_LON_MAIN)|(pos\\.galactic\\.lat;meta\\.main))";
	public static final String GALACTIC_DEC_MAINUCD  = "(?i)((POS_GAL_LAT)|(pos\\.ecliptic\\.dec;meta\\.main))";
	public static final String GALACTIC_RA_UCD       = "(?i)((POS_GAL_LON)|(pos\\.galactic\\.lon))";
	public static final String GALACTIC_DEC_UCD      = "(?i)((POS_GAL_LAT)|(pos\\.galactic\\.lat))";
	public static final String GALACTIC_RA_KW        = "(?i)(_glon)";
	public static final String GALACTIC_DEC_KW       = "(?i)(_glat)";

	public static final String FITS_COOSYS_KW    = "(?i)((COORDS)|(COO.*SYS)|(RADECSYS)|(SYSTEM))";
	public static final String FITS_COOSYS_UCD   = "(?i)(pos.frame)";
	public static final String FITS_EQUINOX_KW   = "(?i)(EQUINOX)";
	public static final String FITS_EQUINOX_UCD  = "(?i)(time.equinox)";
	public static final String FITS_EPOCH_KW     = "(?i)(EPOCH)";
	public static final String FITS_EPOCH_UCD    = "(?i)(time.epoch)";
	/** http://www.aanda.org/articles/aa/full/2006/05/aa3818-05/table1.html */
	public static final String FITS_CTYPE_SPECT = "(?i)(((FREQ)|(ENER)|(WAVN)|(VRAD)|(WAVE)|(VOPT)|(ZOPT)|(AWAV)|(VELO)|(BETA)).*)";
	public static final String FITS_CTYPE_ASC   = "(?i)(((RA--)|(GLON)|(ELON)|(Solar-X)).*)";
	public static final String FITS_CTYPE_DEC   = "(?i)(((DEC-)|(GLAT)|(ELAT)|(Solar-Y)).*)";
	public static final String FITS_EQUINOX     = "(?i)(EQUINOX)";
	
	public static final String OBSERVABLE_UNIT_KW = "(?i)((BUNIT)|(.*flux.*unit)|(.*dispersion.*unit))";
	public static final String OBSERVABLE_UCD_KW = "(?i)((.*flux.*ucd)|(.*dispersion.*ucd))";
	
	/**
	 * DataFileChooser Shortcuts
	 */
	public static final String SHORTCUT_DESKTOP = "(?i)^(bureau|desktop)$";
	public static final String SHORTCUT_DOCUMENTS = "(?i)^documents?$";
	public static final String SHORTCUT_DOWNLOADS = "(?i)^(downloads?|t.l.chargements?)$";
	
	/**
	 * ColumnExpression setters regex
	 * @param args
	 */
	
	public static final String FUNCTION_NAME = "([^()]*)";
	public static final String FUNCTION_ARGS = "\\(([^)]+)\\)";
	public static final String KEYWORD = "((?:'.*')|(?:[_a-zA-Z][_a-zA-Z0-9]*))(?:(?:[\\)\\s\\*\\-\\+\\/,]+)|$)";//"([[_][a-z][A-Z][0-9]]+[^(])[ +-/\\*)]";
	//the above line has been modified on monday 13/10/14. this is the original: public static final String KEYWORD = "([_a-zA-Z][_a-zA-Z0-9]*)(?:(?:[\\)\\s\\*\\-\\+,]+)|$)";//"([[_][a-z][A-Z][0-9]]+[^(])[ +-/\\*)]";
	// 	([_a-zA-Z][_a-zA-Z0-9]*)(?:(?:[\)\s\*-\+]+)|$)
	
	/**
	 * Used to detect SQL statements which are not attributes  
	 */
	public static final  String NO_ATTR =
			 "((?:"  + RegExp.FITS_STR_VAL 
			+ ")|(?:" + RegExp.FITS_BOOLEAN_VAL
			+ ")|(?:" + RegExp.FITS_FLOAT_VAL
			+ ")|(?:" + RegExp.FITS_INT_VAL
			+ "))";
	/**
	 * Used to detect boolean operands in a where statement
	 */
	
	public static final  String TRUE_OPERAND = "([\\s,=]+)T([\\s,=!]*)";

	public static final  String FALSE_OPERAND = "([\\s,=]+)F([\\s,=!]*)";

//	public static final  String TRUE_OPERAND =
//			 "((?:"  + RegExp.FITS_STR_VAL 
//			+ ")|(?:" + "[T]{1}"
//			+ "))";
//	public static final  String FALSE_OPERAND =
//			 "((?:"  + RegExp.FITS_STR_VAL 
//			+ ")|(?:" + "[F]{1}"
//			+ "))";

}