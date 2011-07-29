package saadadb.vo.translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.util.Merger;
import saadadb.vo.PseudoTableParser;
import saadadb.vo.formator.QueryInfos;


/** * @version $Id$

 * Ancestor for the VO translators to SaadaQL/s
 */
public class VOTranslator {

	protected String xmlString;
	protected String saadaString;
	protected boolean metadataRequired = false;

	// name of the class when the query in only on one class
	protected String nameClass = "";
	protected LinkedHashMap<String, String> params;

	public static final int I_COVERS = 1, I_ENCLOSED = 2, I_CENTER = 3, I_OVERLAPS = 4;
	protected static String fAV[] = {"image/fits", "image/jpeg", "text/html", "ALL", "GRAPHIC", "METADATA", "GRAPHIC-ALL", "GRAPHIC-jpeg", "GRAPHIC-fits", "jpeg", "fits"};

	/**
	 * Access this object once you've called 'translate'.
	 * class QueryFormator shall need it.
	 */
	public QueryInfos queryInfos;    
	/**
	 * @param req
	 */
	public VOTranslator(HttpServletRequest req) {
		params = new LinkedHashMap<String, String> ();
		for( Object p: req.getParameterMap().entrySet()) {
			Entry<String, String[]> e = (Entry<String, String[]>)p;
			if( e.getValue().length > 0 ) {
				params.put(e.getKey(), e.getValue()[0]);
			}
		}
		this.init();
		this.queryInfos.setUrl(req.getRequestURL().toString());
	}  

	/**
	 * @param hand_params
	 */
	public VOTranslator(LinkedHashMap<String, String> hand_params) {
		this.params = hand_params;
		this.init();
	}

	/**
	 * 
	 */
	public VOTranslator() {
	}

	/**
	 * 
	 */
	protected void init() {
		saadaString = null;
		queryInfos = new QueryInfos();
		String str;
		if( (str = this.params.get("withrel")) != null ) {
			if( str.equals("true") ) {
				this.queryInfos.setExtensionAllowed(true);
			}
		}
		if( (str = this.params.get("collection")) != null ) {    		
			this.queryInfos.setInputSaadaTable(str);   
		}
		if( "METADATA".equalsIgnoreCase(this.params.get("format")) ) {    
			this.metadataRequired = true;
		}
		this.queryInfos.setParams(this.params);

	}	


	/**
	 * The translation method
	 * @return a SaadaQL/s query
	 * @throws SaadaException 
	 * @throws SaadaException 
	 */
	public String translate() throws SaadaException, SaadaException{
		this.saadaString = this.params.get("query");
		this.queryInfos.setSaadaqlQuery(this.saadaString);
		return this.saadaString;
	}


	/**
	 * @return the metadataRequired
	 */
	public boolean isMetadataRequired() {
		return metadataRequired;
	}

	/**
	 * @param metadataRequired the metadataRequired to set
	 */
	public void setMetadataRequired(boolean metadataRequired) {
		this.metadataRequired = metadataRequired;
	}

	public static String getXMLFromFile(String nameFile) throws Exception {
		FileReader in = new FileReader(nameFile);
		BufferedReader reader = new BufferedReader(in);
		String str="",stringXML="";
		while ((str = reader.readLine()) != null) {
			if (str.indexOf("#") < 0) // we skip comments
				stringXML = stringXML + str;
		}
		return stringXML;
	}


	protected String parseTableItem(String s) throws SaadaException  {
		String retour="";
		if (s.equals("*") ||s.equals("any") || s.equals("[any]") || s.equals("ANY") || s.equals("[ANY]") )  {
			retour+= "From * In * ";
			queryInfos.setQueryTarget(QueryInfos.N_COLL);

		} else if (s.startsWith("[")) {
			retour+= "From ";
			int ind = s.indexOf('(');
			boolean isStar = false;
			if (ind != -1) {
				// [coll1(class1, class2)] ==> From class1,class2 In col1
				String collName = s.substring(1, ind);
				int indStart = ind + 1;
				int indEnd, indComma = 0;
				while((indEnd = s.indexOf(',', indStart)) != -1) {
					if (indComma != 0) retour+= ",";
					indComma++;
					nameClass = s.substring(indStart, indEnd).trim();
					if (nameClass.equals("*")) {
						isStar = true;
					}
					if (nameClass.length() == 0) {
						QueryException.throwNewException(SaadaException.FILE_FORMAT, "Table syntax : comma");
					}
					retour+= nameClass;
					indStart = indEnd + 1;
				}
				// we add the last class name
				nameClass = s.substring(indStart, s.length() - 2).trim();
				if (nameClass.equals("*")) {
					isStar = true;
				}
				if (nameClass.length() == 0) {
					QueryException.throwNewException(SaadaException.FILE_FORMAT, "Table syntax : comma");
				}
				if (indComma > 0) {
					retour+= ",";
					queryInfos.setQueryTarget(QueryInfos.ONE_COLL_N_CLASS);
				} else {
					if (isStar == true) {
						queryInfos.setQueryTarget(QueryInfos.ONE_COLL_N_CLASS);
					} else {
						queryInfos.setQueryTarget(QueryInfos.ONE_COLL_ONE_CLASS);
						queryInfos.setClassName(nameClass);
					}
				}
				retour+= nameClass + " In " + collName;

			} else {
				// [coll1,coll2,coll3]     ==> From * In col1,coll2,coll3
				retour+= "* In ";
				int indStart = 1;
				int indEnd, indComma = 0;
				String nameColl;
				while((indEnd = s.indexOf(',', indStart + 1)) != -1) {
					if (indComma != 0) retour+= ",";
					indComma++;
					nameColl = s.substring(indStart, indEnd).trim();
					if (nameColl.length() == 0) {
						QueryException.throwNewException(SaadaException.FILE_FORMAT,"Table syntax : comma");
					}
					retour+= nameColl;
					indStart = indEnd + 1;
				}
				if (indComma == 0) {
					retour+= s.substring(1, s.length() - 1);
					queryInfos.setQueryTarget(QueryInfos.ONE_COLL_N_CLASS);

				} else {
					// we add the last collection name
					nameColl = s.substring(indStart, s.length() - 1).trim();
					if (nameColl.length() == 0) {
						QueryException.throwNewException(SaadaException.FILE_FORMAT, "Table syntax : comma");
					}
					retour+= "," + nameColl;
					queryInfos.setQueryTarget(QueryInfos.N_COLL);
				}
			}
			return retour;
		} else {
			QueryException.throwNewException(SaadaException.FILE_FORMAT, "Table format");
		}
		return retour;
	}
	/**
	 * Parse the 'collection' argument
	 */
	protected String getFromQueryPart(String value) throws SaadaException {
		PseudoTableParser ptp = new PseudoTableParser(value);
		return "From " + Merger.getMergedArray(ptp.getclasses()) + " In "  + Merger.getMergedArray(ptp.getCollections());
//		try {
//			this.parseTableItem(value);  
//		}
//		catch(Exception e) {
//			Messenger.printStackTrace(e);
//			QueryException.throwNewException(SaadaException.FILE_FORMAT, "Can't parse table item");
//		}
//		String adql = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
//		adql += "<Select xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.ivoa.net/xml/ADQL/v0.7.4\">\n";
//		adql += "  <SelectionList>\n";
//		adql += "    <Item xsi:type=\"columnReferenceType\" Table=\"a\" Name=\"*\" />\n";
//		adql += "  </SelectionList>\n";
//		adql += "  <From>\n";
//		if( value.startsWith("[") ) {
//			adql += "    <Table xsi:type=\"tableType\" Name=\"" + value +  "\" Alias=\"a\" />\n";
//		} else {
//			// ADQL translator doesn't support saada tables not embraced in []
//			adql += "    <Table xsi:type=\"tableType\" Name=\"[" + value +  "]\" Alias=\"a\" />\n";
//		}
//		adql += "  </From>\n";
//		adql += "</Select>";
//		VOQLTranslator translator = new VOQLTranslator(adql);
//		translator.initParseFile();
//		translator.parseFromClause();
//		return translator.getTranslation();
	}

	/**
	 * @param param_name
	 * @return
	 */
	String getParam(String param_name) {
		if( param_name == null ) {
			return null;
		}
		String value = params.get(param_name.toLowerCase());
		if( value == null ) {
			value = params.get(param_name.toUpperCase());    			
		}
		return value;

	}

}


