package saadadb.vo.translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import saadadb.collection.Category;
import saadadb.command.ArgsParser;
import saadadb.database.Database;
import saadadb.exceptions.FatalException;
import saadadb.exceptions.QueryException;
import saadadb.exceptions.SaadaException;
import saadadb.query.parser.PositionParser;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import saadadb.vo.formator.ConeSearchToVOTableFormator;

/**
 * @author michel
 *
 */
public class ConeSearchTranslator extends VOTranslator {
	
	public ArrayList formatTypes = new ArrayList();
	public int intersectionType;
	
	/**
	 * @param req
	 */
	public ConeSearchTranslator(HttpServletRequest req) {
		super(req);
	}
	
	/**
	 * @param hand_params
	 */
	public ConeSearchTranslator(LinkedHashMap<String, String> hand_params) {
		super(hand_params);
	}
	
	@Override
	public String translate() throws SaadaException  {
		double posRa = 1000.0, posDec = 1000.0;
		String pos = "";
		double size = 1.0;
		String fromString = "From * In * ";
		intersectionType = I_OVERLAPS;
		formatTypes.add("ALL");
		String param, value;
		String limit = "";
		
		queryInfos.setCategory(Category.ENTRY);
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
				
				value = params.get("RA");
				if( value != null ) {
					pos = value;
				}
				else {
					QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"Position parameter missing");
				}
				value = params.get("DEC");
				if( value != null ) {
					if( value.startsWith("-") || value.startsWith("+")) {
						pos += value;
					}
					else if( !value.matches(RegExp.FITS_INT_VAL) && !value.matches(RegExp.FITS_FLOAT_VAL)) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"DEC parameter badly formed");						
					}
					else {
						pos += "+" + value;						
					}
				}
				/*
				 * Throws an exception if the position is badly formated
				 */
				PositionParser pp = new PositionParser(pos);
				
				value = params.get("SR");
				if( value != null) {
					size =  Double.parseDouble(value);
					/*
					 * default value defined by the service metadata
					 */
					if( size == 0.0 ) {
						size = 0.1;
					}
					else if ( size < 0.0) {
						QueryException.throwNewException(SaadaException.WRONG_PARAMETER,"SIZE should be greater than 0.0 ");
					}					/*
					 * SaadaQL 1.4 takes search size in arcmn instead of sec
					 */
					size *= 60;
				}        		
				value = params.get("format");
				if( value != null) {
					//formatTypes = getFormatValues(value);
					
				} 
				value = params.get("collection");
				if( value != null) {
					if (value.length() == 0) {
						QueryException.throwNewException(SaadaException.SYNTAX_ERROR,"'collection' parameter should have a value : collection=coll1[class1,class2]");
					}
					fromString = getFromQueryPart(value);
				} 
				value = params.get("withrel");
				/*
				 * Cone search output with relation are build in metadata tree of Aladin with one row per entry.
				 * For this reason, we limit their sizes to a small number (100)
				 */
				if( value != null &&  value.equalsIgnoreCase("true")) {
					limit = "Limit 100";
				}
//				else {
//					limit = "Limit 1000";
//				}
			}
		}
		// Now we build the SaadaQL query
		saadaString = "Select ENTRY " + fromString;
		if( pos.length() > 0 ) {
			saadaString  += "\nWherePosition{isInCircle(\"" + pos + "\"," + size + ",J2000,FK5)}";
		}
		queryInfos.setSaadaqlQuery(saadaString + "\n" + limit);
		return saadaString + "\n" + limit;
	}

	@Override
	public boolean isMetadataRequired() {
		if( "METADATA".equals(this.getParam("FORMAT")) ) {
			return true;
		}
		else {
			return false;
		}
	}

	public static void main(String[] args) throws FatalException {
		ArgsParser ap = new ArgsParser(args);
		Database.init(ap.getDBName());
		LinkedHashMap<String, String> hand_params = new LinkedHashMap<String, String>();
		Messenger.debug_mode = false;
		hand_params.put("SR", "0.2");
		//	 hand_params.put("withrel", "true");	 
		hand_params.put("RA", "185.08");
		hand_params.put("DEC", "6.6606");
		hand_params.put("collection", "[XMM_Data]");
		hand_params.put("RELATION", "AAA");
		hand_params.put("primoid", "864972650676486148");
		try {
			String rf = (new ConeSearchToVOTableFormator(null)).processVOQuery(864972650676486148L, "AAA");
			BufferedReader br = new BufferedReader(new FileReader(rf));
			BufferedWriter bw = new BufferedWriter(new FileWriter("/home/michel/Desktop/cs.xml"));
			String str;
			while( (str = br.readLine()) != null  ) {
				System.out.println(str);
				bw.write(str);
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
//		hand_params.put("relation","AssSources");
//		hand_params.put("primoid","865254095588425729");
//		try {
////			String rf = (new ConeSearchToVOTableFormator(null)).processVOQuery(new ConeSearchTranslator(hand_params));
//			String rf = (new ConeSearchToVOTableFormator(null)).processVOQuery(865254095588425729L, "AssSources");
//			BufferedReader br = new BufferedReader(new FileReader(rf));
//			BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Documents and Settings\\michel\\Bureau\\cs.xml"));
//			String str;
//			while( (str = br.readLine()) != null  ) {
//				System.out.println(str);
//				bw.write(str);
//			}
//			bw.close();
//			br.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
