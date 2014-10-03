package saadadb.vocabulary;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.LinkedHashMap;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.meta.AttributeHandler;
import saadadb.products.setter.ColumnExpressionSetter;

/**
 * 
 * @author hahn
 *
 */
public class KeyMapper {

	private Boolean initialized;
	
	protected SaadaInstance saada;

//	protected String utypeKey;
//	protected String ucdKey;
//	protected String nameKey;
	
	protected Map<String, String> utypeMap;
	protected Map<String, String> ucdMap;
	protected Map<String, String> nameMap;
	protected Map<String, ColumnExpressionSetter> expressionSttrMap;

	/**
	 * 
	 * @param collectionName
	 * @param strings
	 */
	public KeyMapper(SaadaInstance saada) {
		//this.collectionName = collectionName.toLowerCase().trim();
		//this.dataModel = dataModel.toLowerCase().trim();
		this.saada =saada;
		initialized=false;
	}
	/**
	 * @throws Exception 
	 * 
	 */
	public AttributeHandler search(String ucd, String utype, String name) throws Exception {
		
		//Format the parameters to minimize chance of failure
		String fUcd = ucd.trim().toLowerCase();
		fUcd = removePrefix(fUcd);
		String fUtype = utype.trim().toLowerCase();
		fUtype = removePrefix(fUtype);
		String fName = name.trim().toLowerCase();
		fName = removePrefix(fName);
		
		AttributeHandler tmp = new AttributeHandler();
		Boolean matchFound =false;
		
		if(!fUtype.isEmpty() && matchFound == false) {
			tmp = searchByUtype(fUtype);
			if(tmp != null) {
				matchFound =true;
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KeyMapper just found a match by Utype");
			}
		}
		
		else if(!fUcd.isEmpty()  && matchFound == false) {
			tmp = searchByUcd(fUcd);
			if(tmp != null) {
				matchFound =true;
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KeyMapper just found a match by Ucd");
		}
}
		else if (!fName.isEmpty()  && matchFound ==false) {
			tmp = searchByName(fName);
			if(tmp != null) {
				matchFound = true;
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KeyMapper just found a match by Name");
			}
		}
		
		if(matchFound =true){
			return tmp;
		}
		
		return null;
	}
	/**
	 * 
	 * @throws Exception
	 */
	protected void init() throws Exception {
		initExpressionStrrMap();
		initUtypeMap();
		initUcdMap();
		initnameMap();
		initialized =true;
	}
	
	protected String removePrefix(String value) {
		String result = value;
		
		//TODO Finish
			result = result.replace("ssa:", "");
			result = result.replace("sia:", "");
			result = result.replace("vox:", "");
			result = result.replace("obscore:", "");
			result = result.replace("vs:", "");
			result = result.replace("vr:", "");
		return result;
	}
/**
 * 
 * @param keyword
 * @return
 * @throws Exception
 */
	protected AttributeHandler searchByUtype(String keyword) throws Exception {
		if(keyword ==null) {
			//TODO Should Throws an exception
		}
		if (!initialized) {
			init();
		}
System.out.println("KeyWord: "+keyword);
		String expKey;
		//Try to find a match in the utypeMap
		expKey = utypeMap.get(keyword);
		//If a match is found then build the resulting attributehandler
		if(expKey != null) {
			System.out.println("expKey not null : "+expKey);
			return buildAttribute(expKey);
		}
		System.out.println("expkey is null :(");
	//	if no match is found, return null
		return null;
	}
/**
 * 
 * @param keyword
 * @return
 * @throws Exception
 */
	protected AttributeHandler searchByUcd(String keyword) throws Exception {
		if(keyword ==null) {
			//TODO Should Throws an exception
		}
		
		if (!initialized) {
			init();
		}

		String expKey;
		//Try to find a match in the ucdMap
		expKey = ucdMap.get(keyword);
		//If a match is found then build the resulting attributehandler
		if(expKey != null) {
			return buildAttribute(expKey);
		}
	//	if no match is found, return null
		return null;
	}
/**
 * 
 * @param keyword
 * @return
 * @throws Exception
 */
	protected AttributeHandler searchByName(String keyword) throws Exception {
		if(keyword ==null) {
			//TODO Should Throws an exception
		}
		if (!initialized) {
			init();
		}

		String expKey;
		//Try to find a match in the nameMap
		expKey = nameMap.get(keyword);
		//If a match is found then build the resulting attributehandler
		if(expKey != null) {
			return buildAttribute(expKey);
		}
	//	if no match is found, return null
		return null;
	}
	
	//TODO complete description
	/**
	 * 
	 * @param expKey
	 * @return
	 * @throws Exception
	 */
protected AttributeHandler buildAttribute(String expKey) throws Exception {
	ColumnExpressionSetter expsetter;
	
	//Retrieve the ExpressionSetter and compute the result
	expsetter = expressionSttrMap.get(expKey);
	expsetter.calculateExpression();
	
	//Build the AttributeHandler
	AttributeHandler tmp = new AttributeHandler();
	tmp.setNameattr(expKey);
	tmp.setNameorg(expKey);
	tmp.setValue(expsetter.getExpressionResult());
	tmp.setUnit(expsetter.getUnit());
	return tmp;
}
	/*
	 * Initialization Part
	 */
	/**
	 * Instantiates the ColumnExpressionSeter and puts them in expressionStrrMap
	 * with their keys
	 * 
	 * @throws Exception
	 */
	protected void initExpressionStrrMap() throws Exception {
		// TODO Fill the expressionStrrMap with its content 
		//Get the attributehandlers of the right category of the collection (the values of those attributeHandler are still empty!)
		LinkedHashMap<String,AttributeHandler> colAttr = getCollectionAttr();
		
		//Fill the map with the values contained in saada (Saadainstance)
		colAttr = saada.FillMapAttrWithValues(colAttr);

		expressionSttrMap = new LinkedHashMap<String, ColumnExpressionSetter>();

		putNewExpression("access_ref", "THISISJUSTATEST");
		putNewExpression("access_format", colAttr.get("access_format"));
		putNewExpression("access_size", colAttr.get("access_estsize"));
		putNewExpression("dataid_title", colAttr.get("obs_id"));
		putNewExpression("char_spa_cov_loc_val","strcat(s_ra,' ',s_dec)",colAttr,false);
		putNewExpression("char_spa_cov_bou_ext",colAttr.get("s_fov"));
		putNewExpression("calib_level", colAttr.get("calib_level"));
		putNewExpression("char_spa_res", colAttr.get("s_resolution"));
		putNewExpression("char_spect_cov_bou_val", "(em_min+em_max)/2", colAttr,true);
		putNewExpression("char_spect_cov_bou_ext", "em_max-em_min",colAttr,true);
		putNewExpression("em_min", colAttr.get("em_min"));
		putNewExpression("em_max", colAttr.get("em_max"));
		putNewExpression("char_tim_cov_loc_val", "((t_max-t_min)/86400)/2",colAttr,true);
		putNewExpression("char_tim_cov_bou_ext", "t_max-t_min",colAttr,true);
		putNewExpression("coord_spaceframe_name", Database.getAstroframe().name);
		putNewExpression("o_ucd", colAttr.get("o_ucd"));
		
		putNewExpression("image_format", saada.getMimeType());
		putNewExpression("instrument_id", colAttr.get("instrument_name"));
		putNewExpression("s_ra", colAttr.get("s_ra"));
		putNewExpression("s_dec", colAttr.get("s_dec"));
		putNewExpression("image_naxis", "strcat(naxis1,' ',naxis2)",colAttr,false);
		putNewExpression("image_naxes", "2");
		putNewExpression("image_scale", getImageScale(colAttr));
		putNewExpression("t_min", colAttr.get("t_min"));
		putNewExpression("t_max", colAttr.get("t_max"));
		putNewExpression("coord_ref_frame", getSaadaInstanceField("_radesys"));
		putNewExpression("coord_equi", String.valueOf(Database.getAstroframe().getEpoch()));
	//	putNewExpression("coord_projection", getCoordProjection());
		putNewExpression("coord_ref_pixel", "strcat(crpix1_csa,' ',crpix2_csa)", colAttr, false);
		putNewExpression("coord_ref_value", "strcat(crval1_csa,' ',crval2_csa)", colAttr, false);
	putNewExpression("cdmatrix", "strcat(cd1_1_csa,' ',cd1_2_csa,' ',cd2_1_csa,' ',cd2_2_csa)", colAttr, false);
	putNewExpression("image_pixflags", "C");
	}
/**
 * 
 */
	protected void initUtypeMap() {
		// TODO Fill the utypeMap with its content
		utypeMap = new LinkedHashMap<String, String>();
		utypeMap.put("access.reference", "access_ref");
		utypeMap.put("access.format","access_format");
		utypeMap.put("access.size", "access_size");
		utypeMap.put("dataid.title", "dataid_title");
		utypeMap.put("char.spatialaxis.coverage.location.value", "char_spa_cov_loc_val");
		utypeMap.put("char.spatialaxis.coverage.bounds.extent", "char_spa_cov_bou_ext");
		utypeMap.put("char.spatialaxis.calibration", "calib_level");
		utypeMap.put("char.spatialaxis.resolution", "char_spa_res");
		utypeMap.put("char.spectralaxis.coverage.location.value", "char_spect_cov_bou_val");
		utypeMap.put("char.spectralaxis.coverage.bounds.extent", "char_spect_cov_bou_ext");
		utypeMap.put("char.spectralaxis.coverage.bounds.start", "em_min");
		utypeMap.put("char.spectralaxis.coverage.bounds.stop", "em_max");
		utypeMap.put("char.spectralaxis.calibration", "calib_level");
		utypeMap.put("char.timeaxis.coverage.location.value", "char_tim_cov_loc_val");
		utypeMap.put("char.timeaxis.coverage.bounds.extent", "char_tim_cov_bou_ext");
		utypeMap.put("coordsys.spaceframe.name", "coord_spaceframe_name");
		utypeMap.put("char.fluxaxis.ucd","o_ucd");
		utypeMap.put("char.fluxaxis.calibration", "calib_level");

		// create a mapping between a Utype and its corresponding
		// ColumnExpressionSetter by mapping a utype (a key of utypeMap) and
		// a ColumExpressionSetter (a key in expressionStrrMap)
		// e.g utypeMap.put("MyUcd.Example", "example");
	}
/**
 * 
 */
	protected void initUcdMap() {
		// TODO Fill the ucdMap with its content
		ucdMap = new LinkedHashMap<String, String>();
		

		ucdMap.put("image_format", "image_format");
		ucdMap.put("image_AccessReference", "access_reference");
		ucdMap.put("Image_FileSize", "access_size");
		ucdMap.put("inst_id", "instrument_id");
		ucdMap.put("POS_EQ_RA_MAIN".toLowerCase(), "s_ra");
		ucdMap.put("POS_EQ_DEC_MAIN".toLowerCase(), "s_dec");
		ucdMap.put("image_naxis", "image_naxis");
		ucdMap.put("image_naxes", "image_naxes");
		ucdMap.put("image_scale", "image_scale");
		ucdMap.put("image_title", "dataid_title");
		ucdMap.put("image_dateobs", "t_min");
		ucdMap.put("stc_coordrefframe","coord_ref_frame");
		ucdMap.put("stc_coordequinox", "coord_equi");
	//	ucdMap.put("wcs_coordprojection","coord_projection");
		ucdMap.put("wcs_coordrefpixel", "coord_ref_pixel");
		ucdMap.put("wcs_coordrefvalue", "coord_ref_value");
		ucdMap.put("wcs_cdmatrix", "cdmatrix");
		ucdMap.put("wcs_image_pixflags", "image_pixflags");
		ucdMap.put("bandpass_refvalue", "char_spect_cov_bou_val");
		ucdMap.put("bandpass_hilimit", "em_max");
		ucdMap.put("bandpass_lolimit", "em_min");
	}
/**
 * 
 */
	protected void initnameMap() {
		// TODO Fill the nameMap with its content
		nameMap = new LinkedHashMap<String, String>();

	}
	
	protected String getImageScale(LinkedHashMap<String, AttributeHandler> attrMap) throws Exception {
		String axis1;
		String axis2;
		ColumnExpressionSetter tmp = new ColumnExpressionSetter("tmp", "s_fov/naxis1", attrMap, true);
		tmp.calculateExpression();
		axis1 = tmp.getExpressionResult();
		tmp = new ColumnExpressionSetter("tmp", "s_fov/naxis2", attrMap, true);
		tmp.calculateExpression();
		axis2 = tmp.getExpressionResult();
		System.out.println("computeImageScale result: "+axis1+" "+axis2);
		return axis1+" "+axis2;
	}
	
/*	protected  String getCoordProjection() {
		//TODO Avoid cast to ImageSaada
		String val = saada.ctype1_csa.toString();
		// RA---TAN -> TAN e.g.
		if( val != null && val.length() > 3 ) {
			val = val.substring(val.length() - 3);
		}
		return val;
	}*/
	protected String getSaadaInstanceField(String field) {
		String val;
		
		try {
			val = saada.getFieldString(field);
		} catch(Exception e) {
			val = "";
		}
		return val;
	}
/**
 * Returns an LinkedHashMap of AttributeHandler of the category for the collection given by collectionName
 * @return
 * @throws Exception 
 * @throws FatalException 
 */
	@SuppressWarnings("static-access")
	protected  LinkedHashMap<String, AttributeHandler> getCollectionAttr() throws FatalException{
		
		int category = SaadaOID.getCategoryNum(saada.oidsaada);
		int collectionNum = SaadaOID.getCollectionNum(saada.oidsaada);
		switch(category) {
		case Category.SPECTRUM : System.out.println("Loading spectrum atributes handlers");return Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_spectrum();
		case Category.IMAGE : System.out.println("Loading image atributes handlers");return Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_image();
		}
		//TODO should throws an exception if null?
		return null;
	}

	protected void putNewExpression(String fieldName, String constantValue) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, constantValue,false));
	}
	protected void putNewExpression(String fieldName, AttributeHandler attributeHandler) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, attributeHandler,false));
	}
	protected void putNewExpression(String fieldName, String expression, LinkedHashMap<String, AttributeHandler> attrMap, Boolean arithmeticExpression) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, expression, attrMap,arithmeticExpression));
	}
}