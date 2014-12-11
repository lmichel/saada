package saadadb.vocabulary;  

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import saadadb.database.Database;
import saadadb.exceptions.AbortException;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.UTypeHandler;
import saadadb.meta.VOResource;
import saadadb.util.Messenger;

/**This class defines all standard integers allowing the use of a switch/case.
 * Attention: All methods are statics, and this class must be initialized before every use (methods "initTypeMapping" and "initFieldsNames").
 *@author Millan Patrick
 *@version 2.0
 *@since 2.0
 *@see Hashtable
 * @version $Id$
 */
public class DefineType {
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
	protected static Map<String, Integer> nameField;
	protected static Map<String, String> collection_ucds;
	protected static Map<String, String> collection_name_org;
	protected static Map<String, String> collection_units;
	protected static Map<String, String> coll_sdm_utypes;
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
			initCollUtypes();
			initCollNameOrg();
			initCollUnits();
			//appendObsCore();
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
			nameField = new HashMap<String, Integer> ();
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
			collection_ucds = new  HashMap<String, String>();
			collection_ucds.put("oidsaada"  , "meta.id;meta.main");
			collection_ucds.put("oidproduct", "meta.id;meta.file");
			collection_ucds.put("date_load" , "time.processing");
			collection_ucds.put("product_url_csa", "meta.file"); 
			collection_ucds.put("nb_rows_csa"    , "meta.number");
			//collection_ucds.put("pos_ra_csa"     , "pos.eq.ra;meta.main");
			//collection_ucds.put("pos_dec_csa"    , "pos.eq.dec;meta.main");
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
			collection_ucds.put("em_unit_csa", "spect.index;meta.unit");
			//added fields sept 2014
			collection_ucds.put("dataproduct_type",	"meta.id");
			collection_ucds.put("calib_level", 	"meta.code;obs.calib");
			collection_ucds.put("obs_collection",	"meta.id");
			collection_ucds.put("obs_id" , 	"meta.id");
			collection_ucds.put("obs_publisher_did",	"meta.ref.url;meta.curation");
			collection_ucds.put("access_url",	"meta.ref.url");
			collection_ucds.put("access_format",	"meta.code.mime");
			collection_ucds.put("access_estsize",	"phys.size;meta.file");
			collection_ucds.put("target_name",	"meta.id:src");
			collection_ucds.put("s_ra",	"pos.eq.ra");
			collection_ucds.put("s_dec",	"pos.eq.dec");
			collection_ucds.put("s_fov",	"phys.angSize;instr.fov");
			collection_ucds.put("s_region",	"phys.angArea;obs");
			collection_ucds.put("s_resolution",	"pos.angResolution");
			collection_ucds.put("t_min",	"time.start;obs.exposure");
			collection_ucds.put("t_max",	"time.end;obs.exposure");
			collection_ucds.put("t_exptime",	"time.duration;obs.exposure");
			collection_ucds.put("t_resolution",	 "time.resolution");
			collection_ucds.put("em_min",	"em.wl;stat.min");
			collection_ucds.put("em_max",	"em.wl;stat.max");
			collection_ucds.put("em_res_power",	"spect.resolution");
			collection_ucds.put("o_ucd",	"meta.ucd");
			collection_ucds.put("pol_states",	"meta.code;phys.polarization");
			collection_ucds.put("facility_name",	"meta.id;instr.tel");
			collection_ucds.put("instrument_name", "meta.id;instr");
			//Optionals field
			collection_ucds.put("dataproduct_subtype",	"meta.id");
			collection_ucds.put("target_class",	"src.class");
			collection_ucds.put("obs_creation_date",	"time;meta.dataset");
			collection_ucds.put("obs_creator_name",	"meta.id");
			collection_ucds.put("obs_creator_did",	"meta.id");
			collection_ucds.put("obs_title",	"meta.title;obs");
			collection_ucds.put("publisher_id",	"meta.ref.url;meta.curation");
			collection_ucds.put("bib_reference",	"meta.bib.bibcode");
			collection_ucds.put("data_rights",	"meta.code");
			collection_ucds.put("obs_release_date",	"time.release");
			collection_ucds.put("s_ucd",	"meta.ucd");
			collection_ucds.put("s_unit",	"meta.unit");
			collection_ucds.put("s_resolution_min",	"pos.andResolution;stat.min");
			collection_ucds.put("s_resolution_max",	"pos.angResolution;stat.max");
			collection_ucds.put("s_calib_status",	"meta.code.qual");
			collection_ucds.put("s_stat_error",	"stat.error;pos.eq");
			collection_ucds.put("t_calib_status",	"meta.code.qual");
			collection_ucds.put("t_stat_error",	"stat.error;time");
			collection_ucds.put("em_ucd",	"meta.ucd");
			collection_ucds.put("em_unit",	"meta.unit");
			collection_ucds.put("em_calib_status",	"meta.code.qual");
			collection_ucds.put("em_res_power_min",	"spect.resolution;stat.min");
			collection_ucds.put("em_res_power_max",	"spect.resolution;stat.max");
			collection_ucds.put("em_resolution",	"spect.resolution;stat.mean");
			collection_ucds.put("em_stat_error",	"stat.error;em");
			collection_ucds.put("o_unit",	"meta.unit");
			collection_ucds.put("o_calib_status",	"meta.code.qual");
			collection_ucds.put("o_stat_error",	"stat.error;phot.flux");
			collection_ucds.put("proposal_id",	"meta.id;obs.proposal");
		}
	}
	
	public static void initCollNameOrg(){
		if( collection_name_org == null ) {
			collection_name_org = new  HashMap<String, String>();
			collection_name_org.put("oidsaada"       , "Saada OID");
			collection_name_org.put("oidproduct"     , "Product File OID");
			collection_name_org.put("obs_id"      , "Observation ID");
			collection_name_org.put("obs_collection", "Name of the data collection");
			collection_name_org.put("date_load"      , "Ingestion Date");
		//	collection_name_org.put("product_url_csa", "File Name"); 
			collection_name_org.put("nb_rows_csa"    , "Row Number");
		//	collection_name_org.put("error_maj_csa"   , "Major Axis Error");
		//	collection_name_org.put("error_min_csa"    , "Minor Axis Error");
		//	collection_name_org.put("error_angle_csa"  , "Error Ellipse Angle");
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
		//	collection_name_org.put("x_min_csa", "Saada Range Start");
		//	collection_name_org.put("x_max_csa", "Saada Range End");
		//	collection_name_org.put("x_unit_csa", "Saada Range Unit");
		//	collection_name_org.put("x_colname_csa", "Dispersion Column name");
		//	collection_name_org.put("x_min_org_csa", "Org. Range Start");
		//	collection_name_org.put("x_max_org_csa", "Org. Range End");
		//	collection_name_org.put("em_unit_csa", "Org. Range Unit");
		//	collection_name_org.put("y_min_csa", "Saada Flux Range Start");
		//	collection_name_org.put("y_max_csa", "Saada Flux Range End");
		//	collection_name_org.put("y_unit_csa", "Saada Flux Range Unit");
		//	collection_name_org.put("y_colname_csa", "Flux Column name");
			
			collection_name_org.put("dataproduct_type","Logical data product type (image ect.)");
			collection_name_org.put("calib_level","Calibration level {0, 1, 2, 3}");
			collection_name_org.put("obs_publisher_did","Dataset identifier given by the publisher");
			collection_name_org.put("access_url","URL used to access (download) dataset");
			collection_name_org.put("access_format","File content format");
			collection_name_org.put("access_estsize","Estimated size of dataset in kilo bytes");
			collection_name_org.put("target_name","Astronomical object observed, if any");
			collection_name_org.put("s_ra","Central right ascension, ICRS");
			collection_name_org.put("s_dec","Central declination, ICRS");
			collection_name_org.put("s_fov","Diameter (bounds) of the covered region");
			collection_name_org.put("s_region","Region covered as specified in STC or ADQL");
			collection_name_org.put("s_resolution","Spatial resolution of data as FWHM");
			collection_name_org.put("t_min","Start time in MJD");
			collection_name_org.put("t_max","Stop time in MJD");
			collection_name_org.put("t_exptime","Total exposure time");
			collection_name_org.put("t_resolution","Temporal resolution FWHM");
			collection_name_org.put("em_min","Start in spectral coordinates (Should be in meter)");
			collection_name_org.put("em_max","Stop in spectral coordinates (Should be in meter)");
			collection_name_org.put("em_res_power","Spectral resolving power");
			collection_name_org.put("o_ucd","UCD of observable (e.g  phot.flux.density)");
			collection_name_org.put("o_calib_status", "Level of calibration for the observable coordinate ");
			collection_name_org.put("o_unit", "Units used for the observable values");
			collection_name_org.put("pol_states","List of polarization states or NULL if not aplicable");
			collection_name_org.put("facility_name","Name of the facility used for this observation");
			collection_name_org.put("instrument_name","Name of the instrument used for this observation");
			
		}
	}
	/**
	 * 
	 */
	public static void initCollUnits(){
		if( collection_units == null ) {
			collection_units = new  HashMap<String, String>();
		//	collection_units.put("pos_ra_csa"     , "deg");
		//	collection_units.put("pos_dec_csa"    , "deg");
		//	collection_units.put("error_maj_csa"   , "deg");
		//	collection_units.put("error_min_csa"    , "deg");
		//	collection_units.put("error_angle_csa"  , "deg");
		//	collection_units.put("x_min", Database.getConnector().getSpect_unit());
		//	collection_units.put("x_max", Database.getConnector().getSpect_unit());
			//added sept 2014
			collection_units.put("access_estsize", "kbyte");
			collection_units.put("s_ra","deg");
			collection_units.put("s_dec","deg");
			collection_units.put("s_fov","deg");
			collection_units.put("s_resolution","arcsec");
			collection_units.put("t_min","MJD");
			collection_units.put("t_max","MJD");
			collection_units.put("t_exptime","s");
			collection_units.put("t_resolution","s");
			collection_units.put("em_min","m"); 	
			collection_units.put("em_max","m");		
			collection_units.put("s_resolution_min","arcsec");
			collection_units.put("s_resolution_max","arcsec");
			collection_units.put("s_stat_error","arcsec");
			collection_units.put("t_stat_error","s");
			collection_units.put("em_resolution","m");
			collection_units.put("em_stat_error","m");
			collection_units.put("o_stat_error","o_unit");




		}
	}
	/**
	 * 
	 */
	public static void initCollUtypes(){
		if( coll_sdm_utypes == null ) {
			coll_sdm_utypes = new  HashMap<String, String>();
			coll_sdm_utypes.put("x_min_csa", " Char.SpectralAxis.Coverage.Bounds.Start");
			coll_sdm_utypes.put("x_max_csa", " Char.SpectralAxis.Coverage.Bounds.Stop");
			coll_sdm_utypes.put("x_min_org_csa", "SpectralAxis.Coverage.Bounds.Start");
			coll_sdm_utypes.put("x_max_org_csa", "SpectralAxis.Coverage.Bounds.Stop");
			coll_sdm_utypes.put("x_colname_csa", "Dataset.SpectralAxis");
			coll_sdm_utypes.put("pos_ra_csa", "Spectrum.Target.Pos");
			coll_sdm_utypes.put("pos_dec_csa", "Spectrum.Target.Pos");
			
			coll_sdm_utypes.put("dataproduct_type","Obs.dataProductType");
			coll_sdm_utypes.put("calib_level","Obs.calibLevel");
			coll_sdm_utypes.put("obs_collection","DataID.Collection");
			coll_sdm_utypes.put("obs_id","DataID.observationID");
			coll_sdm_utypes.put("obs_publisher_did","Curation.PublisherDID");
			coll_sdm_utypes.put("access_url","Access.Reference");
			coll_sdm_utypes.put("access_format","Access.Format");
			coll_sdm_utypes.put("access_estsize","Access.Size");
			coll_sdm_utypes.put("target_name","Target.Name");
			coll_sdm_utypes.put("s_ra","Char.SpatialAxis.Coverage.Location.Coord.Position2D.Value2.C1");
			coll_sdm_utypes.put("s_dec","Char.SpatialAxis.Coverage.Location.Coord.Position2D.Value2.C2");
			coll_sdm_utypes.put("s_fov","Char.SpatialAxis.Coverage.Bounds.Extent.diameter");
			coll_sdm_utypes.put("s_region","Char.SpatialAxis.Coverage.Support.Area");
			coll_sdm_utypes.put("s_resolution","Char.SpatialAxis.Resolution.refval");
			coll_sdm_utypes.put("t_min","Char.TimeAxis.Coverage.Bounds.Limits.Interval.StartTime");
			coll_sdm_utypes.put("t_max","Char.TimeAxis.Coverage.Bounds.Limits.Interval.StopTime");
			coll_sdm_utypes.put("t_exptime","Char.TimeAxis.Coverage.Support.Extent");
			coll_sdm_utypes.put("t_resolution","Char.TimeAxis.Resolution.refval");
			coll_sdm_utypes.put("em_min","Char.SpectralAxis.Coverage.Bounds.Limits.Interval.LoLim");
			coll_sdm_utypes.put("em_max","Char.SpectralAxis.coverage.Bounds.Limits.Interval.HiLim");
			coll_sdm_utypes.put("em_res_power","Char.SpectralAxis.Resolution.ResolPower.refval");
			coll_sdm_utypes.put("o_ucd","Char.ObservableAxis.ucd");
			coll_sdm_utypes.put("pol_states","Char.PolarizationAxis.stateList");
			coll_sdm_utypes.put("facility_name","Provenance.ObsConfig.facility.name");
			
			coll_sdm_utypes.put("dataproduct_subtype","Obs.dataProductSubType");
			coll_sdm_utypes.put("target_class","Target.Class");
			coll_sdm_utypes.put("obs_creation_date","DataID.Date");
			coll_sdm_utypes.put("obs_creator_name","DataID.Creator");
			coll_sdm_utypes.put("obs_creator_did","DataID.CreatorDID");
			coll_sdm_utypes.put("obs_title","DataID.Title");
			coll_sdm_utypes.put("publisher_id","Curation.PublisherID");
			coll_sdm_utypes.put("bib_reference","Curation.Reference");
			coll_sdm_utypes.put("data_rights","Curation.Rights");
			coll_sdm_utypes.put("obs_release_date","Curation.realeaseDate");
			coll_sdm_utypes.put("s_ucd","Char.SpatialAxis.ucd");
			coll_sdm_utypes.put("s_unit","Char.SpatialAxis.unit");
			coll_sdm_utypes.put("s_resolution_min","Char.SpatialAxis.Resolution.Bounds.Limits.Interval.LoLim");
			coll_sdm_utypes.put("s_resoltion_max","Char.Spatialaxis.Resolution.Bounds.Limits.Interval.HiLim");
			coll_sdm_utypes.put("s_calib_status","Char.SpatialAxis.calibStatus");
			coll_sdm_utypes.put("s_stat_error","Char.SpatialAxis.Accuracy.StatError.refval.value");
			coll_sdm_utypes.put("t_calib_status","Char.TimeAxis.calibStatus");
			coll_sdm_utypes.put("t_stat_error","Char.TimeAxis.Accuracy.StatError.refval.value");
			coll_sdm_utypes.put("em_ucd","Char.SpectralAxis.ucd");
			coll_sdm_utypes.put("em_unit","Char.SpectralAxis.unit");
			coll_sdm_utypes.put("em_calib_status","Char.SpectralAxis.calibStatus");
			coll_sdm_utypes.put("em_res_power_min","Char.SpectralAxis.Resolution.ResolPower.LoLim");
			coll_sdm_utypes.put("em_res_power_max","Char.SpectralAxis.Resolution.ResolPower.HiLim");
			coll_sdm_utypes.put("em_resolution","Char.SpectralAxis.Resolution.refval.value");
			coll_sdm_utypes.put("em_stat_error","Char.SpectralAxis.Accuracy.StatError.refval.value");
			coll_sdm_utypes.put("o_unit","Char.ObservableAxis.unit");
			coll_sdm_utypes.put("o_calib_status","Char.ObservableAxis.calibStatus");
			coll_sdm_utypes.put("o_stat_error","Char.ObservableAxis.Accuracy.StatError.refval.value");
			coll_sdm_utypes.put("proposal_id","Provenance.Proposal.identifier");
			coll_sdm_utypes.put("instrument_name", "Provenance.ObsConfig.instrument.name");
			

			
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
	public static Map<String, String> getColl_sdm_utypes() throws FatalException {
		init();
		return coll_sdm_utypes;
	}
	/**
	 * @return Returns the collection_ucds.
	 * @throws FatalException 
	 */
	public static Map<String, String> getCollection_ucds() throws FatalException {
		init();
		return collection_ucds;
	}

	/**
	 * @return Returns the collection_name_org.
	 * @throws FatalException 
	 */
	public static Map<String, String> getCollection_name_org() throws FatalException {
		init();
		return collection_name_org;
	}

	/**
	 * @return Returns the collection_units.
	 * @throws FatalException 
	 */
	public static Map<String, String> getCollection_units() throws FatalException {
		init();
		return collection_units;
	}

	public static void main(String[] args ) throws Exception
	{
		Messenger.debug_mode =true;
		Database.init("saadaObscore");
		init();

		Messenger.printMsg(Messenger.DEBUG, "Number of entries: "+collection_name_org.size());
		for(Map.Entry<String, String> e:collection_name_org.entrySet()){
			Messenger.printMsg(Messenger.DEBUG, "Key\t"+e.getKey()+"\tValue\t"+e.getValue());
		}
		
		Database.close();
	}
}

