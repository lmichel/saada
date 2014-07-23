package saadadb.util;  

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
/**This class defines all standard integers allowing the use of a switch/case.
 * Attention: All methods are statics, and this class must be initialized before every use (methods "initTypeMapping" and "initFieldsNames").
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see Hashtable
 * @version $Id$
 */
public class DefineType extends Hashtable{
	/**The standard integer corresponding to the "java.util.Date" type**/
	public static final int FIELD_DATE = 0;
	/**The standard integer corresponding to the "java.lang.String" type**/
	public static final int FIELD_STRING = 1;
	/**The standard integer corresponding to the "java.lang.Integer" type**/
	public static final int FIELD_INT = 2;
	/**The standard integer corresponding to the "java.lang.Double" type**/
	public static final int FIELD_DOUBLE = 3;
	/**The standard integer corresponding to the "java.lang.Float" type**/
	public static final int FIELD_FLOAT = 4;
	/**The standard integer corresponding to the "java.lang.Long" type**/
	public static final int FIELD_LONG = 5;
	/**The standard integer corresponding to the "java.lang.Short" type**/
	public static final int FIELD_SHORT = 6;
	/**The standard integer corresponding to the "java.lang.Boolean" type**/
	public static final int FIELD_BOOLEAN = 7;
	/**The standard integer corresponding to the "java.lang.Byte" type**/
	public static final int FIELD_BYTE = 8;
	/**The standard integer corresponding to the "java.lang.Character" type**/
	public static final int FIELD_CHAR = 9;
	/**The standard integer corresponding to the mapping with the user mode**/
	public static final int TYPE_MAPPING_USER = 0;
	/**The standard integer corresponding to the mapping with the 1_1 mode**/
	public static final int TYPE_MAPPING_1_1 = 1;
	/**The standard integer corresponding to the mapping with the classifier mode**/
	public static final int TYPE_MAPPING_CLASSIFIER = 2;
	/**Extension of the intermediate classes**/  
	public static final String TYPE_EXTEND_COLL = "UserColl";
	/**Data type number that must correspond to the data type table**/
	public static final int nbDataType = 4;
	/** Mapping priority level */
	public static final int FIRST = 1; /* use mapping rule and then autodetect */
	public static final int LAST  = 2; /* use autodetect first and then mapping rule */
	public static final int ONLY  = 3; /* use only mapping rule */
	/*
	 * List of supported data models
	 */
	public static final String VO_SDM = "IVOA Spectral Data Model";
	/**Data type table**/
	public static final String[] tabDataType = {"table", "spectrum", "misc", "image"};
	/**List which maps Java type names (keys) to their standard integer in Saada (values)**/
	protected static Hashtable<String, Integer> nameField;
	protected static Hashtable<String, String> collection_ucds;
	protected static Hashtable<String, String> collection_name_org;
	protected static Hashtable<String, String> collection_units;
	protected static Hashtable<String, String> coll_sdm_utypes;
	private static boolean already_init = false;
	/**Initializes this class for this use.
	 * Use for the determining of the mappings integer.
	 *@return void.
	 */     


	/**
	 * @throws FatalException 
	 * 
	 */
	public static void init() throws FatalException { 
		if( !already_init ) {
			initFieldNames();
			initCollUCDs();
			initCollSDMUtypes();
			initCollNameOrg();
			initCollUnits();
			appendObsCore();
			already_init = true;

		}
	}

	/**
	 * @throws FatalException
	 */
	private static void appendObsCore() throws FatalException {
		try {
			VOResource vor = new VOResource(Database.getRoot_dir() + File.separator + "config" +  File.separator + "vodm.ObsCore.xml");
			List<UTypeHandler> uths = vor.getUTypeHandlers();
			for(UTypeHandler uth: uths ){
				collection_ucds.put(uth.getNickname(), uth.getUcd());
				collection_name_org.put(uth.getNickname(), uth.getNickname());
				collection_units.put(uth.getNickname(), uth.getUnit());
				coll_sdm_utypes.put(uth.getNickname(), uth.getUtype());
			}
		} catch (Exception e) {
			FatalException.throwNewException(SaadaException.MISSING_FILE, e);
		}
	}
	/**Initializes this class for this use.
	 * Use for the determining of the integer of java fields.
	 *@return void.
	 */    
	public static void initFieldNames() {
		if( nameField == null ) {
			nameField = new Hashtable<String, Integer> ();
			nameField.put("Date",new Integer(FIELD_DATE));
			nameField.put("long",new Integer(FIELD_LONG));
			nameField.put("int",new Integer(FIELD_INT));
			nameField.put("double",new Integer(FIELD_DOUBLE));
			nameField.put("float",new Integer(FIELD_FLOAT));
			nameField.put("boolean",new Integer(FIELD_BOOLEAN));
			nameField.put("String",new Integer(FIELD_STRING));
			nameField.put("short",new Integer(FIELD_SHORT));
			nameField.put("byte",new Integer(FIELD_BYTE));
			nameField.put("char",new Integer(FIELD_CHAR));
		}
	}
	/**
	 * 
	 */
	public static void initCollUCDs(){
		if( collection_ucds == null ) {
			collection_ucds = new  Hashtable<String, String>();
			collection_ucds.put("oidsaada"  , "meta.id;meta.main");
			collection_ucds.put("oidproduct", "meta.id;meta.file");
			collection_ucds.put("namesaada" , "meta.id");
			collection_ucds.put("date_load" , "time.processing");
			collection_ucds.put("product_url_csa", "meta.file"); 
			collection_ucds.put("nb_rows_csa"    , "meta.number");
			collection_ucds.put("pos_ra_csa"     , "pos.eq.ra;meta.main");
			collection_ucds.put("pos_dec_csa"    , "pos.eq.dec;meta.main");
			collection_ucds.put("error_maj_csa"   , "stat.error;phys.size");
			collection_ucds.put("error_min_csa"   , "stat.error;phys.size");
			collection_ucds.put("error_angle_csa"  , "stat.error;pos.posAng");
			collection_ucds.put("crpix1_csa", "pos.wcs.crpix");
			collection_ucds.put("crpix2_csa", "pos.wcs.crpix");
			collection_ucds.put("ctype1_csa", "pos.wcs.ctype");
			collection_ucds.put("ctype2_csa", "pos.wcs.ctype");
			collection_ucds.put("cd1_1_csa", "pos.wcs.cdmatrix");
			collection_ucds.put("cd2_1_csa", "pos.wcs.cdmatrix");
			collection_ucds.put("cd1_2_csa", "pos.wcs.cdmatrix");
			collection_ucds.put("cd2_2_csa", "pos.wcs.cdmatrix");
			collection_ucds.put("crval1_csa", "pos.wcs.crval");
			collection_ucds.put("crval2_csa", "pos.wcs.crval");
			collection_ucds.put("naxis1", "pos.wcs.naxis");
			collection_ucds.put("naxis2", "pos.wcs.naxis");
			collection_ucds.put("x_min_csa", "em.*;stat.min");
			collection_ucds.put("x_max_csa", "em.*;stat.max");
			collection_ucds.put("x_unit_csa", "spect.index;meta.unit");
			collection_ucds.put("x_min_org_csa", "em.*;stat.min");
			collection_ucds.put("x_max_org_csa", "em.*;stat.max");
			collection_ucds.put("x_unit_org_csa", "spect.index;meta.unit");
		}
	}
	/**
	 * 
	 */
	public static void initCollNameOrg(){
		if( collection_name_org == null ) {
			collection_name_org = new  Hashtable<String, String>();
			collection_name_org.put("oidsaada"       , "Saada OID");
			collection_name_org.put("oidproduct"     , "Product File OID");
			collection_name_org.put("namesaada"      , "Saada Name");
			collection_name_org.put("date_load"      , "Ingestion Date");
			collection_name_org.put("product_url_csa", "File Name"); 
			collection_name_org.put("nb_rows_csa"    , "Row Number");
			collection_name_org.put("pos_ra_csa"     , "Right Ascension");
			collection_name_org.put("pos_dec_csa"    , "Declination");
			collection_name_org.put("error_maj_csa"   , "Major Axis Error");
			collection_name_org.put("error_min_csa"    , "Minor Axis Error");
			collection_name_org.put("error_angle_csa"  , "Error Ellipse Angle");
			collection_name_org.put("crpix1_csa", "WCS CRPIX1");
			collection_name_org.put("crpix2_csa", "WCS CRPIX2");
			collection_name_org.put("ctype1_csa", "WCS CTYPE1");
			collection_name_org.put("ctype2_csa", "WCS CTYPE2");
			collection_name_org.put("cd1_1_csa", "WCS D1_1");
			collection_name_org.put("cd2_1_csa", "WCS D2_1");
			collection_name_org.put("cd1_2_csa", "WCS D1_2");
			collection_name_org.put("cd2_2_csa", "WCS D2_2");
			collection_name_org.put("crval1_csa", "WCS VAL1");
			collection_name_org.put("crval2_csa", "WCS VAL2");
			collection_name_org.put("crota_csa", "WCS CROTA");
			collection_name_org.put("size_alpha_csa", "Size Alpha");
			collection_name_org.put("size_delta_csa", "Size Delta");
			collection_name_org.put("naxis1", "Naxis1");
			collection_name_org.put("naxis2", "Naxis2");
			collection_name_org.put("x_min_csa", "Saada Range Start");
			collection_name_org.put("x_max_csa", "Saada Range End");
			collection_name_org.put("x_unit_csa", "Saada Range Unit");
			collection_name_org.put("x_colname_csa", "Dispersion Column name");
			collection_name_org.put("x_min_org_csa", "Org. Range Start");
			collection_name_org.put("x_max_org_csa", "Org. Range End");
			collection_name_org.put("x_unit_org_csa", "Org. Range Unit");
			collection_name_org.put("y_min_csa", "Saada Flux Range Start");
			collection_name_org.put("y_max_csa", "Saada Flux Range End");
			collection_name_org.put("y_unit_csa", "Saada Flux Range Unit");
			collection_name_org.put("y_colname_csa", "Flux Column name");
		}
	}
	/**
	 * 
	 */
	public static void initCollUnits(){
		if( collection_units == null ) {
			collection_units = new  Hashtable<String, String>();
			collection_units.put("pos_ra_csa"     , "deg");
			collection_units.put("pos_dec_csa"    , "deg");
			collection_units.put("error_maj_csa"   , "deg");
			collection_units.put("error_min_csa"    , "deg");
			collection_units.put("error_angle_csa"  , "deg");
			collection_units.put("x_min", Database.getConnector().getSpect_unit());
			collection_units.put("x_max", Database.getConnector().getSpect_unit());
		}
	}
	/**
	 * 
	 */
	public static void initCollSDMUtypes(){
		if( coll_sdm_utypes == null ) {
			coll_sdm_utypes = new  Hashtable<String, String>();
			coll_sdm_utypes.put("x_min_csa", " Char.SpectralAxis.Coverage.Bounds.Start");
			coll_sdm_utypes.put("x_max_csa", " Char.SpectralAxis.Coverage.Bounds.Stop");
			coll_sdm_utypes.put("x_min_org_csa", "SpectralAxis.Coverage.Bounds.Start");
			coll_sdm_utypes.put("x_max_org_csa", "SpectralAxis.Coverage.Bounds.Stop");
			coll_sdm_utypes.put("x_colname_csa", "Dataset.SpectralAxis");
			coll_sdm_utypes.put("pos_ra_csa", "Spectrum.Target.Pos");
			coll_sdm_utypes.put("pos_dec_csa", "Spectrum.Target.Pos");
		}
	}
	/**Returns the integer corresponding to the parametered Java type.
	 *@param String The tested Java type.
	 *@return int The integer corresponding to the Java type.
	 * @throws AbortException 
	 */
	public static int getType(String type) throws NullPointerException{
		initFieldNames();
		Integer r; 
		if( type == null || (r = nameField.get(type)) == null ) {			
			throw new NullPointerException("Type <" + type + "> not recognized");
		}
		return r;
	}
	/**Returns the integer corresponding to the parametered mapping type.
	 *@param String The tested mapping type.
	 *@return int The integer corresponding to the mapping type.
	 * @throws FatalException 
	 */    
	public int getTypeMapping(String type) throws FatalException{
		init();
		return ((Integer)get(type)).intValue();
	}
	/**Returns the data type if this type exists.
	 *@param String The tested data type.
	 *@return String The tested data type or null.
	 * @throws FatalException 
	 */
	public static String getDataType(String type) throws FatalException{
		init();
		String test;
		for(int i = 0; i<nbDataType; i++){
			test = tabDataType[i];
			if(type.equals(test)){
				return test;
			}
		}
		return "";
	}
	/**Returns the integer corresponding to the parametered data type.
	 *@param String The tested data type.
	 *@return int The integer corresponding to the data type.
	 * @throws FatalException 
	 */
	public static int getIntDataType(String type) throws FatalException{
		init();
		String test;
		for(int i = 0; i<nbDataType; i++){
			test = tabDataType[i];
			if(type.equals(test)){
				return i+1;
			}
		}
		return 0;
	}
	/**
	 * @return Returns the coll_sdm_utypes.
	 * @throws FatalException 
	 */
	public static Hashtable<String, String> getColl_sdm_utypes() throws FatalException {
		init();
		return coll_sdm_utypes;
	}
	/**
	 * @return Returns the collection_ucds.
	 * @throws FatalException 
	 */
	public static Hashtable<String, String> getCollection_ucds() throws FatalException {
		init();
		return collection_ucds;
	}

	/**
	 * @return Returns the collection_name_org.
	 * @throws FatalException 
	 */
	public static Hashtable<String, String> getCollection_name_org() throws FatalException {
		init();
		return collection_name_org;
	}

	/**
	 * @return Returns the collection_units.
	 * @throws FatalException 
	 */
	public static Hashtable<String, String> getCollection_units() throws FatalException {
		init();
		return collection_units;
	}
}

