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
		
		if(fUtype != null && matchFound == false) {
			tmp = searchByUtype(fUtype);
			if(tmp != null) {
				matchFound =true;
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KeyMapper just found a match by Utype");
		}
		}
		
		else if(fUcd != null && matchFound == false) {
			tmp = searchByUcd(fUcd);
			if(tmp != null) {
				matchFound =true;
				System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ KeyMapper just found a match by Ucd");
		}
}
		else if (fName != null && matchFound ==false) {
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
	}
	
	protected String removePrefix(String value) {
		String result = value;
		
		//TODO Finish
			result = result.replace("ssa:", "");
			result = result.replace("sia:", "");
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
		//putNewExpression("char_spa_cov_loc_val","s_ra s_dec");
		//putNewExpression(", attributeHandler);
//		ColumnExpressionSetter accessRef = new ColumnExpressionSetter("access_ref","THISISJUSTATEST");
//		expressionSttrMap.put("access_ref", accessRef);
		
		/*ColumnExpressionSetter accessFormat = new ColumnExpressionSetter("access_format",ColAttr.get("access_format"));
		expressionSttrMap.put("access_format", accessFormat);
		
		ColumnExpressionSetter accessSize = new ColumnExpressionSetter("access_size", ColAttr.get("access_estsize"));
		
		ColumnExpressionSetter texptime = new ColumnExpressionSetter("t_exptime", "((t_max - t_min)/86400)", ColAttr);
		texptime.setUnit("s");
		expressionSttrMap.put("t_exptime", texptime);
		
		ColumnExpressionSetter char_TimCovBouExt = new ColumnExpressionSetter("Char_TimCovBouExt".toLowerCase(),"t_max-t_min",ColAttr);
		expressionSttrMap.put("char_timcovbouex", char_TimCovBouExt);*/
		
		//ColumnExpressionSetter access_right  = new ColumnExpressionSetter("access_right",ColAttr.get("access_right"));
		// ====================================== How To
		// ==========================================
		// 1. Create the ColumnexpressionSetter
		// 2. Fill the map with the created columnExpressionSetters

		// EXAMPLE\\

		// Creates a ColumnexpressionSetter of name and fieldName "example" that
		// will finds the value of AttributeHandler EXAMPLE in ColAttr
		// And which would return the value of EXAMPLE -1 when computed:
		// 1. ColumnExpressionSetter example = new
		// ColumnExpressionSetter("example","EXAMPLE-1", ColAttr);
		// 1.1 Set a unit for this field
		//	example.setUnit(unit);
		// 2. expressionSttrMap.put("example", example);

		// Creates a ColumnexpressionSetter of name and fieldName "secondExple"
		// that will return the constant value "MyTelescopeName"
		// When Computed:
		// 1. ColumnExpressionSetter secondExple = new
		// ColumnExpressionSetter("secondExple","MyTelescopeName");
		// 2. expressionSttrMap.put("secondExple", secondExple);

		//\\//\\
	}
/**
 * 
 */
	protected void initUtypeMap() {
		// TODO Fill the utypeMap with its content
		utypeMap = new LinkedHashMap<String, String>();
		
		utypeMap.put("access.reference", "access_ref");
		utypeMap.put("char.timeaxis.coverage.location.value", "t_exptime");
		utypeMap.put("char.timeaxis.coverage.bounds.extent", "char_timcovbouex");
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
	}
/**
 * 
 */
	protected void initnameMap() {
		// TODO Fill the nameMap with its content
		nameMap = new LinkedHashMap<String, String>();

	}
/**
 * Returns an LinkedHashMap of AttributeHandler of the category for the collection given by collectionName
 * @return
 * @throws FatalException 
 */
	
	@SuppressWarnings("static-access")
	protected  LinkedHashMap<String, AttributeHandler> getCollectionAttr() throws FatalException{
		
		int category = SaadaOID.getCategoryNum(saada.oidsaada);
		int collectionNum = SaadaOID.getCollectionNum(saada.oidsaada);
		switch(category) {
		case Category.SPECTRUM : return Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_spectrum();
		case Category.IMAGE : return Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_image();
		}
		//TODO should throws an exception if null?
		return null;
	}

	protected void putNewExpression(String fieldName, String constantValue) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, constantValue));
	}
	protected void putNewExpression(String fieldName, AttributeHandler attributeHandler) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, attributeHandler));
	}
	protected void putNewExpression(String fieldName, String expression, LinkedHashMap<String, AttributeHandler> attrMap) throws Exception {
		expressionSttrMap.put(fieldName, new ColumnExpressionSetter(fieldName, expression, attrMap));
	}
}