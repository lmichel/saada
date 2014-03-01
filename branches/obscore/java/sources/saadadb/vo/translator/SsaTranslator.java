package saadadb.vo.translator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.products.inference.SpectralCoordinate;
import saadadb.util.RegExp;
import saadadb.util.SaadaConstant;
import saadadb.vo.formator.SsaToVOTableFormator;

/**
 * @author michel
 *
 * Parser which translates a URL representing a HTTP GET request into a SaadaQL/s String
 *
 * This parser implements the 1.0 version of SSA which can be found at :
 *         http://www.ivoa.net/Documents/WD/SSA/ssa-20040524.html
 * The parser is compliant with the specification given above.
 *  @version 07/2011
 */
public class SsaTranslator extends VOTranslator {



	public int intersectionType;

	public ArrayList<String> formatTypes = new ArrayList<String>();
	public static ArrayList<String> formatAllowedValues = new ArrayList<String>(Arrays.asList(fAV));


	/**
	 * @param req
	 */
	public SsaTranslator(HttpServletRequest req) {
		super(req);
	}

	/**
	 * @param hand_params
	 */
	public SsaTranslator(LinkedHashMap<String, String> hand_params) {
		super(hand_params);
	}


	/**
	 * The parsing function
	 * @return a SaadaQL/s String if parsing succeded.
	 * @throws SaadaException 
	 */
	public String translate() throws SaadaException {
		String pos = "";
		double size = 1.0;
		String fromString = "From * In * ";
		String bandString = "";
		intersectionType = I_OVERLAPS;
		formatTypes.add("ALL");
		String param, value;

		queryInfos.setCategory(Category.SPECTRUM);
		if( params != null ) {
			if (params.size() == 0) {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"No parameters ==> I do nothing");
			}
			String query;
			if( (query = params.get("query")) != null ) {
				return query;
			}
			for(Entry<String, String> e: params.entrySet()) {
				value = e.getValue().trim();
				param = e.getKey().toLowerCase(); // parameter names must be case-insensitive

				if (param.toLowerCase().equals("pos")) {
					pos = value.replaceAll(",", " ");    			
				} else if (param.equals("size")) {
					size = Double.parseDouble(value);
					/*
					 * default value defined by the service metadata
					 */
					if( size == 0.0 ) {
						size = 0.1;
					}
					else if (size < 0.0) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"SIZE should be greater than 0");
					}
					/*
					 * SaadaQL 1.4 takes search size in arcmn instead of sec
					 */
					size *= 60;
				} else if (param.toLowerCase().equals("format")) {
					formatTypes = getFormatValues(value);

				} else if (param.toLowerCase().equals("collection")) {
					if (value.length() == 0) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"'collection' parameter should have a value : collection=coll1[class1,class2]");
					}
					fromString = getFromQueryPart(value);

				} else if (param.toLowerCase().equals("band")) {
					if (value.length() == 0) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"'band' parameter should have a value: Xmin/Xmax[unit]");
					}
					else {
						bandString = getBandQueryPart(value);
					}

				} else if (param.toLowerCase().equals("verb")) {
					// do nothing. Parameter not supported, but silently accepted
				}
			}
		}
		if (isMetadataRequired()) {
			return "";
		}    	
		// Now we build the SaadaQL query
		saadaString = "Select SPECTRUM " + fromString;
		if( pos.length() > 0 ) {
			saadaString  += "\nWherePosition{isInCircle(\"" + pos + "\"," + size + ",J2000,FK5)}";
		}
		if( bandString.equals("") == false ) {
			saadaString += "\nWhereAttributeSaada{" + bandString + "}";
		}
		saadaString += "\nLimit 1000";
		queryInfos.setSaadaqlQuery(saadaString);
		return saadaString;
	}

	/**
	 * @param s
	 * @return
	 * @throws SaadaException
	 */
	private ArrayList getFormatValues(String s) throws SaadaException {
		ArrayList<String> values = new ArrayList<String>();

		if (s == null) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"FORMAT parameter should have a value");
		} else {
			int indStart = 0;
			int indEnd, indComma = 0;
			String prm;
			while((indEnd = s.indexOf(',', indStart + 1)) != -1) {
				indComma++;
				prm = s.substring(indStart, indEnd);
				if (formatAllowedValues.contains(prm)) {
					values.add(prm);
					if (prm.equals("METADATA")) {
						setMetadataRequired(true);
					}
				} else {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unsupported FORMAT value : " + prm);
				}
				indStart = indEnd + 1;
			}
			prm = s.substring(indStart, s.length());
			if (formatAllowedValues.contains(prm)) {
				values.add(prm);
				if (prm.equals("METADATA")) {
					setMetadataRequired(true);
				}
			} else {
				QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Unsupported FORMAT value : " + prm);
			}

		}
		return values;
	}

	/**
	 * @param value
	 * @return
	 * @throws SaadaException 
	 * @throws SaadaException 
	 */
	private static String getBandQueryPart(String value) throws SaadaException, SaadaException {
		String unit="m", retour = "";
		String regexp = "(" + RegExp.NUMERIC + ")/("+ RegExp.NUMERIC + ")(?:\\[(.*)\\])?";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(value);
		double v0 = -1, v1=-1;
		if( m.find()  ) {
			switch(m.groupCount()) {
			case 2: v0 = Double.parseDouble(m.group(1).trim());
			        v1 = Double.parseDouble(m.group(2).trim());
			        break;
			case 3: v0 = Double.parseDouble(m.group(1).trim());
	                v1 = Double.parseDouble(m.group(2).trim());
	                unit = m.group(3).trim();
	                break;
			default: QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Band Parameter badly formed");
			}
		}
		v0 = SpectralCoordinate.convertSaada(unit, Database.getSpect_unit(), v0);
		v1 = SpectralCoordinate.convertSaada(unit, Database.getSpect_unit(), v1);
		if( v0 == SaadaConstant.DOUBLE || v1 == SaadaConstant.DOUBLE ) {
			QueryException.throwNewException(SaadaException.WRONG_PARAMETER, "Band parameters cannot be converted from " + unit + " to " + Database.getSpect_unit());
		}
		if( v0 > v1 ) {
			double x = v0;
			v0 = v1;
			v1 = x;
		}
		if( v0 > 0 ) {
//			retour = "x_min_csa >= " + v0;
			retour = v0 + " BETWEEN x_min_csa  AND  x_min_csa";
		}
		if( v1 > 0 ) {
//			if(  v0 > 0 ) {
//				retour += " and ";
//			}
//			retour += "x_max_csa <= " + v1;
			retour = v0 + " BETWEEN x_min_csa  AND  x_max_csa OR "
			       + v1 + " BETWEEN x_min_csa  AND  x_max_csa OR "
			       + "(x_min_csa < " + v0 + " and  x_max_csa  >" +  v1 + ")" ;
			
		}
		
		return retour;
	}

	
	public static void main(String[] args) throws Exception  {
		
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		LinkedHashMap<String, String> hand_params = new LinkedHashMap<String, String>();
		//http://localhost:8080/AppWG/ssa?pos=13%3A47%3A37.24056%2C-60%3A37%3A01.2360%20&size=0.7&collection=*
   	    hand_params.put("pos", "12:34:17.805+39:24:37.26");
		hand_params.put("withrel", "false");
		hand_params.put("size", "0.08");
		hand_params.put("format", "ALL");
		hand_params.put("band", "0.2/2222[GHz]");
		hand_params.put("collection", "ANY");

		try {
			String rf = (new SsaToVOTableFormator("SSA EPIC Spectra")).processVOQuery(new SsaTranslator(hand_params));
			BufferedReader br = new BufferedReader(new FileReader(rf));
			//BufferedWriter bw = new BufferedWriter(new FileWriter("/tmpx/lmichel/ssa.xml"));
			String str;
			while( (str = br.readLine()) != null  ) {
				System.out.println(str.replaceAll("<TD", "\n<TD"));
				//bw.write(str);
			}
			//bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
