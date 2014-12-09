package saadadb.vo.request.formator.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import saadadb.collection.Category;
import saadadb.collection.SaadaOID;
import saadadb.collection.obscoremin.SaadaInstance;
import saadadb.database.Database;
import saadadb.dataloader.testprov.FooClassBuilder;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.SaadaException;
import saadadb.meta.AttributeHandler;
import saadadb.meta.UTypeHandler;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.products.setter.AttributeHandlerExtractor;
import saadadb.products.setter.ColumnExpressionSetter;
import saadadb.util.Messenger;
import saadadb.util.SaadaConstant;
import saadadb.vo.request.SIAPRequest;

/**
 * @author hahn This class deals with the field "expression" in the VoRessource files It computes a ColumnExpressionSetter expression and
 *         can be parameterized to allow fine tuned "data collection"
 */
public class AhValueFinder {

	protected SaadaInstance saada;
	protected UTypeHandler currentUh;
	protected String query;
	protected Extractor extractor;

	/*
	 * Save the collection AttributeHandler map as there is no need to rebuild it for every UtypeHandler unless saada is reassigned
	 */
	protected LinkedHashMap<String, AttributeHandler> colAttrMap;
	protected boolean needToRebuiltColAttrMap;

	/*
	 * Parameters flags
	 */

	boolean arithmetic;

	public AhValueFinder(SaadaInstance si) {
		saada = si;
		init();
	}

	protected void init() {

		colAttrMap = new LinkedHashMap<String, AttributeHandler>();
		needToRebuiltColAttrMap = true;
	}

	/**
	 * @throws Exception
	 */
	public AttributeHandler compute() throws Exception {

		/* ==================== compute() logic ========================
		 * if(expession NOT empty OR there is at least ONE param){
		 * 		if(!arithmetic){
		 * 			//This is a constant expression
		 * 		}
		 * 		else{
		 * 			//This is not a constant expression
		 * 			if (isAsSimpleMapping) {
		 * 				//This is a simple mapping
		 * 				Use ColumnExpressionSetter#ColumnExpressionSetter(String fieldName, AttributeHandler attr, boolean arithmeticExpression)
		 * 			}
		 * 			else{
		 * 				//This is a complexe mapping
		 * 				Use ColumnExpressionSetter#ColumnExpressionSetter(String fieldName, String expression, Map<String, AttributeHandler> attributes, boolean arithmeticExpression)
		 * 			}
		 * 		}
		 * }
		 *\//Both expression AND Param are empty
		 * else{
		 * 		//Look if it is not a special action
		 * 		Use findStaticMatch(String ucd, String utype)
		 * 		if(searchResult != null){
		 * 			//A match has been found
		 * 		}
		 * 		else{
		 * 			//Compare UtypeHandler's ucd, utype, nameAttr & nameOrg to every AttributeHandler available
		 * 			//This was the last chance to get a value
		 * 		}
		 * }
		 ===============================================================
		 */
		String value = "";
		System.out.println("Expression= '" + query + "'");
		if (!query.isEmpty() || extractor.getParamSize() > 0) {
			/*
			 * There is an Expression or params
			 */

			// if (constant) {
			if (!arithmetic) {
				System.out.println("Is a constant expression");
				// Constant expression (as a fixed string),
				value = query;
			} else {
				/*
				 * Expression is not constant
				 */
				System.out.println("Expression has been given");

				// Build an AhMap containing all possible AH
				LinkedHashMap<String, AttributeHandler> possibleAh = new LinkedHashMap<String, AttributeHandler>();
				possibleAh.putAll(getAllPossibleAH());

				// Analyse the expression to get a map of the Used AH
				LinkedHashMap<String, AttributeHandler> usedAh = new LinkedHashMap<String, AttributeHandler>();
				// usedAh.putAll(buildAhMapforExpression(possibleAh));
				ArrayList<AttributeHandler> ahList = buildAhMapforExpression(possibleAh);
				Iterator<AttributeHandler> it = ahList.iterator();
				while(it.hasNext()) {
					AttributeHandler tmp = it.next();
					usedAh.put(tmp.getNameattr(), tmp);
					System.out.println(("..."));
				}
				// if the used ahmap is empty -> then it s an exception
				if (usedAh == null || usedAh.size() == 0) {
					// Throws an exception, something is not normal
					// TODO Add a proper exception
					FatalException.throwNewException(
							SaadaException.METADATA_ERROR,
							"Wrong paramaters: No attribute handler can be found in Expression" + extractor);
				}

				// Fill the used AH with values (by using SaadaInstance and/or
				// ...... ?)
				usedAh = fillAhMapWithValues(usedAh);
				ColumnExpressionSetter ces;
				boolean isASimpleMapping = false;
				AttributeHandler simpleMappingAh = new AttributeHandler();

				// If usedAh contains only one member, we want to know if
				// the expression is a real one
				// (which involves math, specific functions ..)
				// Or just a a mapping to obscore Ah
				if (usedAh.size() == 1) {
					// Load the first and only value in simpleMappingAh
					simpleMappingAh = usedAh.values().iterator().next();
					String tmp = query.replace(simpleMappingAh.getNameattr(), "").replace(simpleMappingAh.getNameorg(), "").trim();
					if (tmp.isEmpty()) {
						// It just a mapping
						isASimpleMapping = true;
						System.out.println("Expression is a simple mapping");
					}
				}
				boolean canCompute = true;
				for (Map.Entry<String, AttributeHandler> e : usedAh.entrySet()) {
					System.out.println("Used AH\t'" + e.getValue().getNameattr() + "' value: " + e.getValue().getValue());
					String eValue = e.getValue().getValue();
					if (eValue.equals("NaN") || eValue.equalsIgnoreCase("null") || eValue.equalsIgnoreCase("NotSet")
							|| checkIfMaxValue(eValue, e.getValue().getType())) {
						System.out.println("Value = NaN or null, can't compute");
						canCompute = false;
						break;
					}
				}
				// depending on the result above (real expression or simple mapping),
				// we create the appropriate ColumnExpression Setter
				if (canCompute) {
					if (isASimpleMapping) {
						// The try/catch is here because we have no way to know for sure that the values are suitable for the CES
						try {
							ces = new ColumnExpressionSetter(currentUh.getNickname(), simpleMappingAh, arithmetic);
							ces.calculateExpression();
							value = ces.getExpressionResult();
						} catch (Exception e) {
							value = "";
						}

						// Creates an Ah similar to mapped ah so a unit check can be performed later
						return buildAttributeHandlerFromAh(simpleMappingAh, value);

					} else { // else, it's not a simple mapping
						// The try/catch is here because we have no way to know for sure that the values are suitable for the CES
						try {
							ces = new ColumnExpressionSetter(currentUh.getNickname(), query, usedAh, arithmetic);
							ces.calculateExpression();
							value = ces.getExpressionResult();
						} catch (Exception e) {
							value = "";
						}
						return buildAttributeHandlerFromCurrentUh(value);
					}

				} else {
					value = "";
				}
			}
		}
		/*
		 * No expression or param has been given
		 */
		else {
			System.out.println("No expresion given");

			// Use currentUh's utype and ucd to find
			// a match in utypeMap or ucdMap
			String searchResult = findStaticMatch(currentUh.getUtype(), currentUh.getUcd());
			if (searchResult != null) {
				System.out.println("Static search found a match");
				value = searchResult;
			} else {
				// Build an AhMap containing all possible AH
				LinkedHashMap<String, AttributeHandler> possibleAh = new LinkedHashMap<String, AttributeHandler>();
				possibleAh.putAll(getAllPossibleAH());
				AttributeHandler tmpCurrentAh = new AttributeHandler();
				String ucd = removePrefix(currentUh.getUcd());
				String utype = removePrefix(currentUh.getUtype());
				String nickname = currentUh.getNickname();
				boolean matched = false;
				// TODO Remove prefix to e's ucd utype
				// for every entry, we compare its Utype, ucd and name to the UtypeHandler
				for (Map.Entry<String, AttributeHandler> e : possibleAh.entrySet()) {
					tmpCurrentAh = e.getValue();
					if (ucd.equalsIgnoreCase(tmpCurrentAh.getUcd()) && !ucd.isEmpty() || utype.equalsIgnoreCase(tmpCurrentAh.getUtype())
							&& !utype.isEmpty() || nickname.equalsIgnoreCase(tmpCurrentAh.getNameattr()) && !nickname.isEmpty()
							|| nickname.equalsIgnoreCase(tmpCurrentAh.getNameorg()) && !nickname.isEmpty()) {

						matched = true;
						break;
					}
				}
				if (matched) {
					value = fillAhWithValue(tmpCurrentAh).getValue();
					System.out.println("The manual search found a match for ucd/utype <" + ucd + "/" + utype + ">: " + value);
					// Creates an Ah similar to tmpCurrentAh so a unit check can be performed later
					return buildAttributeHandlerFromAh(tmpCurrentAh, value);
				} else {
					System.out.println("No AttributeHandler has been found for ucd/utype: <" + ucd + "/" + utype + ">");
				}
			}
			// if the browsing returned something, get the value of the Ah
			// if not, leave the value field empty ?
			// }
		}
		return buildAttributeHandlerFromCurrentUh(value);
	}

	/**
	 * Fills the given attribute handler with its value and returns it
	 * 
	 * @param ah
	 * @return
	 * @throws Exception
	 */
	protected AttributeHandler fillAhWithValue(AttributeHandler ah) throws Exception {
		/*
		 * You can add some logic to retrieve value from different locations (depending of the expression's parameters)
		 */
		return saada.fillAttrWithValue(ah);
	}

	/**
	 * Retrieves and return an Hashmap similar to toFill but hit AttributeHandler filled with their values
	 * 
	 * @param toFill
	 * @return
	 * @throws Exception
	 */
	protected LinkedHashMap<String, AttributeHandler> fillAhMapWithValues(LinkedHashMap<String, AttributeHandler> toFill) throws Exception {
		/*
		 * You can add some logic to retrieve values from different location/technics (depending of the expression's parameters)
		 */
		LinkedHashMap<String, AttributeHandler> result = new LinkedHashMap<String, AttributeHandler>();
		result.putAll(saada.fillMapAttrWithValues(toFill));

		return result;

	}

	/**
	 * Searches for a special handler for the given ucd/utype
	 * 
	 * @param utype
	 * @param ucd
	 * @return The value calculated if there was a match or null if none
	 * @throws Exception
	 */
	protected String findStaticMatch(String utype, String ucd) throws Exception {
		System.out.println("Looking for a static match");
		String cUtype = removePrefix(utype);
		String cUcd = removePrefix(ucd);
		System.out.println("Utype " + cUtype);
		System.out.println("Ucd " + cUcd);

		if (cUcd.equalsIgnoreCase("STC_CoordRefFrame")) {
			// TODO should not be done like this
			try {
				return saada.getFieldString("_radecsys");
			} catch (Exception e) {
				return "";
			}
		}
		if (cUcd.equalsIgnoreCase("STC_CoordEquinox")) {
			return String.valueOf(Database.getAstroframe().epoch);
		}
		if (cUcd.equalsIgnoreCase("WCS_CoordProjection")) {
			String val;
			try {
				val = saada.getFieldValue("ctype1_csa").toString();
			} catch (Exception e) {
				val = "";
			}
			// RA---TAN -> TAN e.g.
			if (val != null && val.length() > 3) {
				val = val.substring(val.length() - 3);
			}
			return val;
		}

		if (cUcd.equalsIgnoreCase("Image_Scale")) {
			String[] var = new String[3];
			// Get filled Attributehandler
			var[0] = fillAhWithValue(colAttrMap.get("s_fov")).getValue();
			var[1] = fillAhWithValue(colAttrMap.get("naxis1")).getValue();
			var[2] = fillAhWithValue(colAttrMap.get("naxis2")).getValue();
			// Try to proceed to the calculation
			try {
				Double s_fov = Double.parseDouble(var[0]);
				Double naxis1 = Double.parseDouble(var[1]);
				Double naxis2 = Double.parseDouble(var[2]);

				String val = s_fov / naxis1 + " " + s_fov / naxis2;
				return val;
			} catch (Exception e) {
				return "";
			}
		}
		/* 
		 * 
		 * Energy Axe requires specials actions to convert values from database unit to meters
		 * 
		 * */

		if (cUtype.equalsIgnoreCase("Char.SpectralAxis.Coverage.Location.Value")) {
			String val;
			try {
				// /*
				// * Conversion must be first done because it could be non
				// * linear (e.g. Kev -> m) Hence converting the mean is no
				// * equivalent to the mean of converted values
				// */
				double em_min = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_min")).getValue());
				double em_max = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_max")).getValue());
				double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_min);
				double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_max);
				if (Double.isNaN(v1) || Double.isNaN(v2)) {
					return "";
				}
				val = (Double.toString((v1 + v2) / 2));
			} catch (Exception e) {
				e.printStackTrace();
				val = "";
			}
			return val;
		}
		if (cUtype.equalsIgnoreCase("Char.SpectralAxis.Coverage.Bounds.Extent")) {
			String val;
			try {
				double em_min = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_min")).getValue());
				double em_max = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_max")).getValue());
				double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_min);
				double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_max);
				if (Double.isNaN(v1) || Double.isNaN(v2)) {
					return "";
				}

				double v = v2 - v1;

				if (v < 0)
					v *= -1.;
				val = (Double.toString(v));
			} catch (Exception e) {
				e.printStackTrace();
				val = "";
			}
			return val;
		}

		if (cUtype.equalsIgnoreCase("Char.SpectralAxis.Coverage.Bounds.Start") || cUcd.equalsIgnoreCase("VOX:BandPass_LoLimit")) {
			String val;
			try {
				double em_min = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_min")).getValue());
				double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_min);
				if (Double.isNaN(v1)) {
					val = "";
				} else {
					val = String.valueOf(v1);
				}
			} catch (Exception e) {
				val = "";
			}
			return val;
		}
		if (cUtype.equalsIgnoreCase("Char.SpectralAxis.Coverage.Bounds.Stop") || cUcd.equalsIgnoreCase("BandPass_HiLimit")) {
			String val;
			try {
				double em_max = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_max")).getValue());
				double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_max);
				if (Double.isNaN(v1)) {
					val = "";
				} else {
					val = String.valueOf(v1);
				}
			} catch (Exception e) {
				val = "";
			}
			return val;
		}
		if (cUcd.equalsIgnoreCase("BandPass_RefValue")) {
			String val;
			try {
				double em_min = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_min")).getValue());
				double em_max = Double.parseDouble(fillAhWithValue(getAllPossibleAH().get("em_max")).getValue());
				double v1 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_min);
				double v2 = SpectralCoordinate.convertSaada(Database.getSpect_unit(), "m", em_max);
				if (Double.isNaN(v1) || Double.isNaN(v2)) {
					return "";
				}

				double v = (v1 + v2) / 2;

				if (v < 0)
					v *= -1.;
				val = (Double.toString(v));
			} catch (Exception e) {
				e.printStackTrace();
				val = "";
			}
			return val;
		}

		if (cUtype.equalsIgnoreCase("CoordSys.SpaceFrame.Name")) {
			return Database.getAstroframe().name;
		}
		System.out.println("No static match for ucd/utype : '" + cUcd + "/" + cUtype + "'");
		return "";
	}

	/**
	 * Analyse the expression and return an AH map of all attributeHandler in use
	 * 
	 * @param baseMap
	 *            Must be a map containing all the AH that can be used in the expression.
	 * @return
	 */
	protected ArrayList<AttributeHandler> buildAhMapforExpression(LinkedHashMap<String, AttributeHandler> baseMap) {
		System.out.println("Expression for extractor: " + query);
		AttributeHandlerExtractor ahExt = new AttributeHandlerExtractor(query, baseMap);
		List<AttributeHandler> ahMap = new LinkedList<AttributeHandler>();

		ahMap = ahExt.extractAH();
		return (ArrayList)ahMap;
	}

	/**
	 * creates an AttributeHandler similar to the current UtypeHandler and assigns it the given value
	 * 
	 * @param value
	 *            The value that is put in the value field of the returned AttributeHandler
	 * @return
	 * @throws Exception
	 */
	protected AttributeHandler buildAttributeHandlerFromCurrentUh(String value) throws Exception {
		AttributeHandler result = currentUh.getAttributeHandler();
		result.setValue(value);
		return result;
	}

	/**
	 * creates an AttributeHandler similar to the given AttributeHandler and assigns it the given value
	 * 
	 * @param value
	 *            The value that is put in the value field of the returned AttributeHandler
	 * @return
	 * @throws Exception
	 */
	protected AttributeHandler buildAttributeHandlerFromAh(AttributeHandler model, String value) throws Exception {
		AttributeHandler result = model;
		result.setValue(value);
		return result;
	}

	/**
	 * Build an HashMap containing all possible AttributeHandlers for these parameters
	 * 
	 * @return a map containing all possible Ah
	 * @throws FatalException
	 */
	protected LinkedHashMap<String, AttributeHandler> getAllPossibleAH() throws FatalException {
		/*
		 * You can add some logic here to load class AH to be used when expression is computed
		 */
		/*
		 * You can add some logic to load collection/category specific AH
		 */
		LinkedHashMap<String, AttributeHandler> result = new LinkedHashMap<String, AttributeHandler>();
		result = getCollectionAttr();
		return result;
	}

	/**
	 * Gets and return a map from the Database which contains AHs for the Category and collection computed from SaadaInstance oidsaada
	 * 
	 * @return
	 * @throws FatalException
	 */
	@SuppressWarnings("static-access")
	protected LinkedHashMap<String, AttributeHandler> getCollectionAttr() throws FatalException {
		// There is no need to rebuild this map if the saadaInstance has not changed
		if (needToRebuiltColAttrMap) {

			int category = SaadaOID.getCategoryNum(saada.oidsaada);
			int collectionNum = SaadaOID.getCollectionNum(saada.oidsaada);
			switch (category) {
			case Category.SPECTRUM:
				System.out.println("Loading spectrum atributes handlers");
				colAttrMap = Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_spectrum();
				break;
			case Category.IMAGE:
				System.out.println("Loading image atributes handlers");
				colAttrMap = Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_image();
				break;
			case Category.ENTRY:
				System.out.println("Loading entry attributes Handlers");
				colAttrMap = Database.getCachemeta().getCollection(collectionNum).getAttribute_handlers_entry();
				break;
			default:
				// TODO should throws an exception ?
				break;
			}
			needToRebuiltColAttrMap = false;
		}
		return colAttrMap;
	}

	/**
	 * Get and return an Ah map for the given collection name and category number
	 * 
	 * @param collection
	 * @param category
	 * @return
	 * @throws FatalException
	 */
	@SuppressWarnings("static-access")
	protected Map<String, AttributeHandler> getCollectionAttr(String collection, int category) throws FatalException {
		return Database.getCachemeta().getCollection(collection).getAttribute_handlers(category);
	}

	/**
	 * Removes potential prefixes to ensure a better detection of ucd and utypes
	 * 
	 * @param value
	 * @return
	 */
	protected String removePrefix(String value) {
		String currentVal = value.toLowerCase().trim();
		/*
		 * Note : String.replace is pretty expensive (it compiles regex) 
		 * Therefore after each replace, the value is tester in order to avoid useless call to replace
		 */
		currentVal = value.replace("ssa:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		currentVal = value.replace("sia:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		currentVal = value.replace("vox:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		currentVal = value.replace("obscore:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		currentVal = value.replace("vs:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		currentVal = value.replace("vr:", "");
		if (!currentVal.equals(value)) {
			return currentVal.trim();
		}
		return currentVal;
	}

	/**
	 * Checks if the value is equal to a undifined value for the followin type :
	 * int, double, long, float 
	 * 
	 */
	protected boolean checkIfMaxValue(String value, String type) {

		switch (type.toLowerCase().trim()) {
		case "int":
			return Integer.parseInt(value) == SaadaConstant.INT ? true : false;
		case "double":
			return Double.parseDouble(value) == SaadaConstant.DOUBLE ? true : false;
		case "long":
			return Long.parseLong(value) == SaadaConstant.LONG ? true : false;
		case "float":
			return Float.parseFloat(value) == SaadaConstant.FLOAT ? true : false;
		default:
			return false;
		}
	}

	/**
	 * sets the saada attribute with a new SaadaInstance
	 * 
	 * @param si
	 */
	public void setSaadaInstance(SaadaInstance si) {
		saada = si;
		needToRebuiltColAttrMap = true;
	}

	/**
	 * Sets the currentUh attribute with a new Utypehandlers Automatically extracts the expression & parameters
	 * 
	 * @param uh
	 * @throws Exception 
	 */
	public void setUtypeHandler(UTypeHandler uh) throws Exception {
		System.out.println("New UtypeHandler set : " + uh.getNickname());
		this.currentUh = uh;
		// resetDefaultParams();
		extractor = new Extractor();
		extractor.setExpression(currentUh.getExpression());
		arithmetic = extractor.getArithmetic();
		query = extractor.getQuery();
		// extract();
	}

	public static void main(String[] args) throws Exception {

		// Database.init("saadaObscore");

		/*
		 * SIAP Request tester
		 */
		System.out.println("============Start===========");
		Messenger.debug_mode = true;
		Database.init(args[args.length - 1]);
		String[] fileNames = { "image_xmm.json" };
		System.out.println("============New Foo builder===========");
		FooClassBuilder foo = new FooClassBuilder(fileNames);
		System.out.println("============Foo Process===========");
		foo.process();
		System.out.println("============End of Foo process===========");
		LinkedHashMap<String, String> pmap = new LinkedHashMap<String, String>();
		Messenger.printMsg(Messenger.TRACE, "Parameters:");
		for (int i = 0; i < (args.length - 1); i++) {
			String[] ps = args[i].split("=");
			if (ps.length != 2) {
				FatalException.throwNewException(SaadaException.WRONG_PARAMETER, "Param " + args[i] + " badly formed");
			}
			pmap.put(ps[0], ps[1]);
			Messenger.printMsg(Messenger.TRACE, "  " + ps[0] + " = " + ps[1]);
		}
		// TODO Change ile PATH
		// SIAPRequest request = new SIAPRequest("NoSession",
		// "/home/michel/Desktop");
		SIAPRequest request = new SIAPRequest("NoSession", "/home/hahn/Desktop");
		request.addFormator("votable");
		request.setResponseFilePath("SIAP");
		request.processRequest(pmap);
		Database.close();
	}

}
